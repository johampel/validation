/*
 * The MIT License
 * Copyright © 2022 Johannes Hampel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.hipphampel.validation.core.provider;

import de.hipphampel.validation.core.annotations.*;
import de.hipphampel.validation.core.condition.Condition;
import de.hipphampel.validation.core.condition.Conditions;
import de.hipphampel.validation.core.condition.RuleCondition;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.path.Path;
import de.hipphampel.validation.core.path.PathResolver;
import de.hipphampel.validation.core.provider.AnnotationRuleRepository.Handler;
import de.hipphampel.validation.core.rule.*;
import de.hipphampel.validation.core.rule.ReflectionRule.*;
import de.hipphampel.validation.core.utils.OneOfTwo;
import de.hipphampel.validation.core.value.Value;
import de.hipphampel.validation.core.value.Values;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.hipphampel.validation.core.utils.ReflectionUtils.*;

/**
 * {@link Handler} implementation for the {@link RuleDef} annotation.
 * <p>
 * The {@code RuleDef} annotation is intended to geenrate new {@link Rule Rules} and expose them to a {@link AnnotationRuleRepository}. The
 * annotation can be applied to:
 * <ol>
 *   <li>Static or instance fields, which are {@code final} and return either a {@code Condition} or a {@code Predicate}</li>
 *   <li>Static or instance methods, which have  at least one parameter</li>
 * </ol>
 * In any case, the field or method needs to be public.
 * <p>
 * Please refer to the {@code RuleDef} annotation for the exact details.
 *
 * @see RuleDef
 */
public class RuleDefHandler implements AnnotationRuleRepository.Handler<RuleDef> {

  private static final Set<Class<? extends Annotation>> BIND_ANNOTATIONS = Set.of(
      BindContext.class,
      BindContextParameter.class,
      BindFacts.class,
      BindParentFacts.class,
      BindRootFacts.class,
      BindMetadata.class,
      BindPath.class);

  private final PathResolver pathResolver;

  /**
   * Default constructor.
   */
  public RuleDefHandler() {
    this(null);
  }


  /**
   * Constructor
   *
   * @param pathResolver An optional {@link PathResolver}. This might be used to construct optimized {@link PathBinding PathBindings}
   */
  public RuleDefHandler(PathResolver pathResolver) {
    this.pathResolver = pathResolver;
  }

  @Override
  public Class<RuleDef> getAnnotation() {
    return RuleDef.class;
  }

  @Override
  public List<Rule<?>> handleAnnotationForField(Object repositoryInstance, Field field, RuleDef annotation) {
    ensurePublicFinalField(repositoryInstance, field);

    String ruleId = getRuleId(field, annotation);
    Class<?> factsType = annotation.factsType();
    List<Condition> preconditions = getPreconditions(field, annotation);
    Map<String, Object> metadata = getMetadata(field, annotation);
    ResultReason failReason = getFailReason(annotation);

    Object value = getMandatoryFieldValue(repositoryInstance, field);
    if (value instanceof Condition condition) {
      return List.of(newConditionBasedRule(ruleId, factsType, metadata, preconditions, condition, Values.val(failReason)));
    } else if (value instanceof Predicate<?> predicate) {
      Condition condition = Conditions.predicate(predicate);
      return List.of(newConditionBasedRule(ruleId, factsType, metadata, preconditions, condition, Values.val(failReason)));
    } else {
      throw new IllegalArgumentException("Field '" + field.getName() + "' has wrong type");
    }
  }

  @Override
  public List<Rule<?>> handleAnnotationForMethod(Object repositoryInstance, Method method, RuleDef annotation) {
    checkMethodSignature(repositoryInstance, method);

    String ruleId = getRuleId(method, annotation);
    Class<?> factsType = annotation.factsType();
    List<Condition> preconditions = getPreconditions(method, annotation);
    Map<String, Object> metadata = getMetadata(method, annotation);
    List<ParameterBinding> bindings = getParameterBindings(method);
    ResultReason failReason = getFailReason(annotation);
    ResultMapper resultMapper = nativeResult -> {
      if (nativeResult instanceof Result result) {
        return result;
      } else if (nativeResult instanceof Boolean b) {
        return Boolean.TRUE.equals(b) ? Result.ok() : Result.failed(failReason);
      }
      return Result.failed(failReason);
    };

    return List.of(newReflectionBasedRule(
        ruleId,
        factsType,
        metadata,
        preconditions,
        repositoryInstance,
        method,
        bindings,
        resultMapper));
  }

  private void checkMethodSignature(Object repositoryInstance, Method method) {
    ensurePublicMethod(repositoryInstance, method);
    if (method.getParameterCount() == 0) {
      throw new IllegalArgumentException("Method '" + method.getName() + "' must have at least one parameter");
    }
  }

  /**
   * Gets the {@link ResultReason}.
   * <p>
   * If the {@code annotation} has a message, the result is a {@code StringResultReason} with exactly this message, otherwise it is
   * {@code null}
   *
   * @param annotation The {@link RuleDef} annotation
   * @return The {@code ResultReason}
   */
  protected ResultReason getFailReason(RuleDef annotation) {
    if (annotation.message().isEmpty()) {
      return null;
    }
    return new StringResultReason(annotation.message());
  }

  /**
   * Creates a {@link ParameterBinding ParameterBindings} for the given {@code method}.
   *
   * @param method The method
   * @return The binding
   */
  protected List<ParameterBinding> getParameterBindings(Method method) {
    return Arrays.stream(method.getParameters())
        .map(this::getParameterBinding)
        .toList();
  }

  /**
   * Creates a {@link ParameterBinding} for the given {@code parameter}.
   *
   * @param parameter The parameter
   * @return The binding
   */
  protected ParameterBinding getParameterBinding(Parameter parameter) {
    List<Annotation> annotations = Arrays.stream(parameter.getAnnotations())
        .filter(annotation -> BIND_ANNOTATIONS.contains(annotation.annotationType()))
        .toList();
    if (annotations.size() > 1) {
      throw new IllegalArgumentException("Found more than one bind annotation for parameter '" + parameter.getName() + "'");
    }

    if (annotations.isEmpty()) {
      if (ValidationContext.class.isAssignableFrom(parameter.getType())) {
        return new ContextBinding();
      } else {
        return new FactsBinding();
      }
    }
    Annotation annotation = annotations.get(0);
    if (annotation instanceof BindContext bc) {
      return newContextParameterBinding(parameter, bc);
    } else if (annotation instanceof BindContextParameter bcp) {
      return newContextParameterParameterBinding(parameter, bcp);
    } else if (annotation instanceof BindFacts bf) {
      return newFactsParameterBinding(parameter, bf);
    } else if (annotation instanceof BindParentFacts bpf) {
      return newParentFactsParameterBinding(parameter, bpf);
    } else if (annotation instanceof BindRootFacts brf) {
      return newRootFactsParameterBinding(parameter, brf);
    } else if (annotation instanceof BindMetadata bm) {
      return newMetadataParameterBinding(parameter, bm);
    } else if (annotation instanceof BindPath bp) {
      return newPathParameterBinding(parameter, bp);
    } else {
      throw new IllegalArgumentException("Annotation '" + annotation.annotationType().getName() + "' not recognized");
    }
  }

  /**
   * Creates a {@link ParameterBinding} based on the given {@code parameter} and {@code annotation}.
   *
   * @param parameter  The {@link Parameter}
   * @param annotation The {@link BindContext} annotation
   * @return The resulting {@code ParameterBinding}
   */
  protected ParameterBinding newContextParameterBinding(Parameter parameter, BindContext annotation) {
    if (!ValidationContext.class.isAssignableFrom(parameter.getType())) {
      throw new IllegalArgumentException("Parameter '" + parameter.getName() + "' needs to be a ValidationContext");
    }
    return new ContextBinding();
  }

  /**
   * Creates a {@link ParameterBinding} based on the given {@code parameter} and {@code annotation}.
   *
   * @param parameter  The {@link Parameter}
   * @param annotation The {@link BindContextParameter} annotation
   * @return The resulting {@code ParameterBinding}
   */
  protected ParameterBinding newContextParameterParameterBinding(Parameter parameter, BindContextParameter annotation) {
    return new ContextParameterBinding(annotation.value());
  }

  /**
   * Creates a {@link ParameterBinding} based on the given {@code parameter} and {@code annotation}.
   *
   * @param parameter  The {@link Parameter}
   * @param annotation The {@link BindFacts} annotation
   * @return The resulting {@code ParameterBinding}
   */
  protected ParameterBinding newFactsParameterBinding(Parameter parameter, BindFacts annotation) {
    return new FactsBinding();
  }

  /**
   * Creates a {@link ParameterBinding} based on the given {@code parameter} and {@code annotation}.
   *
   * @param parameter  The {@link Parameter}
   * @param annotation The {@link BindParentFacts} annotation
   * @return The resulting {@code ParameterBinding}
   */
  protected ParameterBinding newParentFactsParameterBinding(Parameter parameter, BindParentFacts annotation) {
    return new ParentFactsBinding();
  }

  /**
   * Creates a {@link ParameterBinding} based on the given {@code parameter} and {@code annotation}.
   *
   * @param parameter  The {@link Parameter}
   * @param annotation The {@link BindRootFacts} annotation
   * @return The resulting {@code ParameterBinding}
   */
  protected ParameterBinding newRootFactsParameterBinding(Parameter parameter, BindRootFacts annotation) {
    return new RootFactsBinding();
  }

  /**
   * Creates a {@link ParameterBinding} based on the given {@code parameter} and {@code annotation}.
   *
   * @param parameter  The {@link Parameter}
   * @param annotation The {@link BindMetadata} annotation
   * @return The resulting {@code ParameterBinding}
   */
  protected ParameterBinding newMetadataParameterBinding(Parameter parameter, BindMetadata annotation) {
    return new MetadataBinding(annotation.value());
  }

  /**
   * Creates a {@link ParameterBinding} based on the given {@code parameter} and {@code annotation}.
   *
   * @param parameter  The {@link Parameter}
   * @param annotation The {@link BindPath} annotation
   * @return The resulting {@code ParameterBinding}
   */
  protected ParameterBinding newPathParameterBinding(Parameter parameter, BindPath annotation) {
    OneOfTwo<String, Path> path = pathResolver == null ?
        OneOfTwo.ofFirst(annotation.value()) : OneOfTwo.ofSecond(pathResolver.parse(annotation.value()));
    return new PathBinding(path);
  }

  /**
   * Determines the preconditions of the {@link Rule}.
   * <p>
   * This creates a map based on the data extracted from the {@link Metadata} annotations in the {@link RuleDef} annotation.
   *
   * @param member     The class member
   * @param annotation The {@link RuleDef} annotation
   * @return The list of {@link Condition conditions}
   */
  protected List<Condition> getPreconditions(Member member, RuleDef annotation) {
    return Arrays.stream(annotation.preconditions())
        .map(this::preconditionToCondition)
        .toList();
  }

  private Condition preconditionToCondition(Precondition precondition) {
    RuleSelector ruleSelector = RuleSelector.of(precondition.rules());
    Set<String> paths = Arrays.stream(precondition.paths()).collect(Collectors.toSet());
    return new RuleCondition(Values.val(ruleSelector), Values.val(paths));
  }

  /**
   * Determines the metadata of the {@link Rule}.
   * <p>
   * This creates a map based on the data extracted from the {@link Metadata} annotations in the {@link RuleDef} annotation.
   *
   * @param member     The class member
   * @param annotation The {@link RuleDef} annotation
   * @return The metadata
   */
  protected Map<String, Object> getMetadata(Member member, RuleDef annotation) {
    return Arrays.stream(annotation.metadata())
        .collect(Collectors.toMap(
            Metadata::key,
            Metadata::value
        ));
  }

  /**
   * Determines the id of the {@link Rule} defined by the {@code annotation}.
   * <p>
   * By default, returns the {@code id} attribute of the {@code annotation}, or - if this is empty - the name of the {@code member} the
   * annotation is attached to
   *
   * @param member     The class member
   * @param annotation The {@link RuleDef} annotation
   * @return The id of the {@code Rule}
   */
  protected String getRuleId(Member member, RuleDef annotation) {
    if (!annotation.id().isEmpty()) {
      return annotation.id();
    }
    return member.getName();
  }

  /**
   * Creates a new {@link Rule} based on a {@link Condition}.
   *
   * @param id            The unique id of the rule
   * @param factsType     The {@link Class} for the facts type
   * @param metadata      Metadata of the rule
   * @param preconditions List of precondition
   * @param condition     The condition to evaluate if {@code validate} is called
   * @param failReason    {@link Value} providing the {@link ResultReason} in case of a failure, might be {@code null}
   * @return The {@code Rule}
   */
  protected <T> Rule<T> newConditionBasedRule(String id, Class<? super T> factsType, Map<String, Object> metadata,
      List<Condition> preconditions, Condition condition, Value<ResultReason> failReason) {
    return new ConditionRule<>(id, factsType, metadata, preconditions, condition, failReason);
  }

  /**
   * Creates a new {@link Rule} based on any method.
   *
   * @param id            The id of the {@link Rule}
   * @param factsType     {@link Class} of the objects being validated by this rule
   * @param metadata      The metadata
   * @param preconditions The list of {@linkplain Condition preconditions}
   * @param boundInstance The object that is bound to the method. In case of static methods it must be  {@code null}, in case of instance
   *                      method not {@code null}
   * @param ruleMethod    The {@code Method} to execute
   * @param bindings      The {@linkplain ParameterBinding bindings} for the method parameters
   * @param resultMapper  The {@link ResultMapper} to map the methods return type to a {@link Result}
   * @return The {@code Rule}
   */
  protected <T> ReflectionRule<T> newReflectionBasedRule(String id, Class<? super T> factsType, Map<String, Object> metadata,
      List<Condition> preconditions, Object boundInstance, Method ruleMethod, List<ParameterBinding> bindings, ResultMapper resultMapper) {
    return new ReflectionRule<>(id, factsType, metadata, preconditions, boundInstance, ruleMethod, bindings, resultMapper);
  }
}
