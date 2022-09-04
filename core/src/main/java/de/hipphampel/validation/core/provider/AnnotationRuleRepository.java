package de.hipphampel.validation.core.provider;

/*-
 * #%L
 * validation-core
 * %%
 * Copyright (C) 2022 Johannes Hampel
 * %%
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
 * #L%
 */

import de.hipphampel.validation.core.annotations.Precondition;
import de.hipphampel.validation.core.annotations.RuleDef;
import de.hipphampel.validation.core.annotations.RuleRef;
import de.hipphampel.validation.core.condition.Condition;
import de.hipphampel.validation.core.condition.Conditions;
import de.hipphampel.validation.core.condition.RuleCondition;
import de.hipphampel.validation.core.exception.RuleNotFoundException;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.ResultReason;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.rule.RuleBuilder;
import de.hipphampel.validation.core.rule.StringResultReason;
import de.hipphampel.validation.core.rule.SystemResultReason;
import de.hipphampel.validation.core.rule.SystemResultReason.Code;
import de.hipphampel.validation.core.value.Values;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RuleRepository} using annotations to define {@link Rule Rules}.
 * <p>
 * This implementation supports the {@link RuleRef} and {@link RuleDef} annotations. Fields or
 * methods annotated with those define the actual {@code Rules}. Basically, there are two ways how
 * to construct an instance: Either by specifying a {@code Class} so that annotated static members
 * are used to define the {@code Rules} or by specifying an object instance, so that non-static
 * member are used. The latter way is useful in situations where the {@code Rules} depend on the
 * state of the instance.
 * <p>
 * Please refer to {@code RuleDef} and {@code RuleRef} for details, how {@code Rules} are defined.
 *
 * @see RuleDef
 * @see RuleRef
 */
public class AnnotationRuleRepository implements RuleRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationRuleRepository.class);

  private final Object instance;
  private final Map<String, Rule<?>> rules;

  /**
   * Creates an instance based on a {@link Class}.
   * <p>
   * Instances created this way rely on static members of {@code clazz} only.
   *
   * @param clazz The class to get the {@link Rule Rules} from
   * @return A {@link AnnotationRuleRepository}
   */
  public static AnnotationRuleRepository ofClass(Class<?> clazz) {
    return new AnnotationRuleRepository(null, clazz);
  }

  /**
   * Creates an instance based on an object.
   * <p>
   * Instances created this way rely on non-static members of {@code instance} only.
   *
   * @param instance The object instance
   * @return A {@link AnnotationRuleRepository}
   */
  public static AnnotationRuleRepository ofInstance(Object instance) {
    return new AnnotationRuleRepository(instance, (Class<?>) instance.getClass());
  }

  /**
   * Creates an instance based on an object.
   * <p>
   * Instances created this way rely on non-static members of {@code instance} only.
   *
   * @param instance The object instance
   * @param clazz    The type of the instance
   * @param <T>      The type of the instance
   * @return A {@link AnnotationRuleRepository}
   */
  public static <T> AnnotationRuleRepository ofInstance(T instance, Class<? extends T> clazz) {
    return new AnnotationRuleRepository(instance, clazz);
  }

  /**
   * Constructor.
   *
   * @param instance The instance the rules are bound to, might be {@code null}
   * @param clazz    The {@link Class} to use
   * @param <T>      The type of the instance
   */
  public <T> AnnotationRuleRepository(T instance, Class<? extends T> clazz) {
    this.instance = instance;
    this.rules = fillRuleMap(clazz);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Rule<T> getRule(String id) {
    Rule<?> rule = rules.get(id);
    if (rule == null) {
      throw new RuleNotFoundException(id);
    }
    return (Rule<T>) rule;
  }

  @Override
  public Set<String> getRuleIds() {
    return rules.keySet();
  }

  private Map<String, Rule<?>> fillRuleMap(Class<?> clazz) {
    Map<String, Rule<?>> ruleMap = new HashMap<>();
    Arrays.stream(clazz.getDeclaredFields())
        .map(this::fieldToRule)
        .filter(Objects::nonNull)
        .forEach(rule -> addRule(ruleMap, rule));
    Arrays.stream(clazz.getMethods())
        .map(this::methodToRule)
        .filter(Objects::nonNull)
        .forEach(rule -> addRule(ruleMap, rule));
    return Collections.unmodifiableMap(ruleMap);
  }

  private void addRule(Map<String, Rule<?>> ruleMap, Rule<?> rule) {
    String id = rule.getId();
    if (ruleMap.containsKey(id)) {
      throw new IllegalStateException("Duplicate rule definition '" + id + "'");
    }
    ruleMap.put(id, rule);
  }

  private Rule<?> fieldToRule(Field field) {
    RuleRef ruleRef = field.getAnnotation(RuleRef.class);
    RuleDef ruleDef = field.getAnnotation(RuleDef.class);
    if (ruleRef == null && ruleDef == null) {
      return null;
    } else if (ruleDef != null && ruleRef != null) {
      throw new IllegalStateException(
          "Field '" + field.getName() + "' annotated with both RuleRef and RuleDef");
    } else if (ruleDef != null) {
      return fieldToRuleFromRuleDef(field, ruleDef);
    } else {
      return fieldToRuleFromRuleRef(field, ruleRef);
    }
  }

  private Rule<?> fieldToRuleFromRuleDef(Field field, RuleDef ruleDef) {
    assertFieldModifier(field);
    Object fieldValue = getFieldValue(field, Condition.class, Predicate.class);
    Condition condition;
    if (fieldValue instanceof Condition c) {
      condition = c;
    } else if (fieldValue instanceof Predicate<?> p) {
      condition = Conditions.predicate(p);
    } else {
      throw new IllegalStateException("Unexpectd field value");
    }
    return RuleBuilder.conditionRule(
            determineRuleId(field, ruleDef),
            ruleDef.factsType())
        .validateWith(condition)
        .withFailReason(determineFailReason(ruleDef))
        .withPreconditions(determinePreconditions(ruleDef))
        .build();
  }

  private Rule<?> fieldToRuleFromRuleRef(Field field, RuleRef ruleRef) {
    assertFieldModifier(field);
    return getFieldValue(field, Rule.class);
  }

  @SuppressWarnings("unchecked")
  private <T> T getFieldValue(Field field, Class<?>... types) {
    Object fieldValue;
    try {
      field.setAccessible(true);
      fieldValue = field.get(instance);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Field+ '" + field.getName() + "' is not accessible", e);
    }

    for (Class<?> type : types) {
      if (type.isInstance(fieldValue)) {
        return (T) fieldValue;
      }
    }
    throw new IllegalStateException(
        "Field '" + field.getName() + "' must be a '" + Arrays.toString(types) + "' instance");
  }

  private void assertFieldModifier(Field field) {
    int modifier = field.getModifiers();
    if (!Modifier.isFinal(modifier)) {
      throw new IllegalStateException("Field '" + field.getName() + "' must be final");
    }
    if (instance == null && !Modifier.isStatic(modifier)) {
      throw new IllegalStateException("Field '" + field.getName() + "' must be static");
    }
  }

  private Rule<?> methodToRule(Method method) {
    RuleRef ruleRef = method.getAnnotation(RuleRef.class);
    RuleDef ruleDef = method.getAnnotation(RuleDef.class);
    if (ruleRef == null && ruleDef == null) {
      return null;
    } else if (ruleDef != null && ruleRef != null) {
      throw new IllegalStateException(
          "Method '" + method.getName() + "' annotated with both RuleRef and RuleDef");
    } else if (ruleDef != null) {
      return methodToRuleFromRuleDef(method, ruleDef);
    } else {
      return methodToRuleFromRuleRef(method, ruleRef);
    }
  }

  @SuppressWarnings("unchecked")
  private Rule<?> methodToRuleFromRuleDef(Method method, RuleDef ruleDef) {
    assertMethodModifier(method);
    Class<?>[] parameterTypes = method.getParameterTypes();
    if (parameterTypes.length < 1 ||
        parameterTypes.length > 2 ||
        (parameterTypes.length == 2 && parameterTypes[0] != ValidationContext.class)) {
      throw new IllegalStateException(
          "Method '" + method.getName()
              + "' must have one parameter or - if there are two - the first must be a ValidationContext");
    }

    Class<?> returnType = method.getReturnType();
    if (returnType != boolean.class && returnType != Boolean.class && returnType != Result.class) {
      throw new IllegalStateException(
          "Method '" + method.getName() + "' must return boolean, Boolean, or Result");
    }

    method.setAccessible(true);

    boolean requiresValidationContext = parameterTypes.length == 2;
    boolean returnsResult = returnType == Result.class;
    MethodInvoker invoker = null;
    if (returnsResult) {
      if (requiresValidationContext) {
        invoker = (context, facts) -> (Result) method.invoke(instance, context, facts);
      } else {
        invoker = (context, facts) -> (Result) method.invoke(instance, facts);
      }
    } else {
      ResultReason reason = determineFailReason(ruleDef);
      if (requiresValidationContext) {
        invoker = (context, facts) -> toResult((Boolean) method.invoke(instance, context, facts),
            reason);
      } else {
        invoker = (context, facts) -> toResult((Boolean) method.invoke(instance, facts), reason);
      }
    }

    return RuleBuilder.functionRule(
            determineRuleId(method, ruleDef),
            (Class<Object>) parameterTypes[parameterTypes.length - 1]
        )
        .validateWith(invoker)
        .withPreconditions(determinePreconditions(ruleDef))
        .build();
  }

  private static Result toResult(Boolean result, ResultReason failReason) {
    return Boolean.TRUE.equals(result) ? Result.ok() : Result.failed(failReason);
  }

  private Rule<?> methodToRuleFromRuleRef(Method method, RuleRef ruleRef) {
    assertMethodModifier(method);
    if (method.getParameterTypes().length > 0) {
      throw new IllegalStateException(
          "Method '" + method.getName() + "' must not expect any parameter");
    }

    Class<?> returnType = method.getReturnType();
    if (!Rule.class.isAssignableFrom(returnType)) {
      throw new IllegalStateException(
          "Method '" + method.getName() + "' must return a Rule");
    }

    try {
      if (!method.canAccess(instance)) {
        method.setAccessible(true);
      }
      Rule<?> rule = (Rule<?>) method.invoke(instance);
      if (rule == null) {
        throw new IllegalStateException(
            "Method '" + method.getName() + "' returns null");
      }
      return rule;
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(
          "Method '" + method.getName() + "' throws an exception", e);
    }
  }

  private void assertMethodModifier(Method field) {
    int modifier = field.getModifiers();
    if (Modifier.isAbstract(modifier)) {
      throw new IllegalStateException("Method '" + field.getName() + "' must not be abstract");
    }
    if (instance == null && !Modifier.isStatic(modifier)) {
      throw new IllegalStateException("Method '" + field.getName() + "' must be static");
    }
  }

  private ResultReason determineFailReason(RuleDef ruleDef) {
    if (ruleDef.message() == null || ruleDef.message().isEmpty()) {
      return null;
    } else {
      return new StringResultReason(ruleDef.message());
    }
  }

  private String determineRuleId(Member member, RuleDef ruleDef) {
    if (ruleDef.id() != null && !ruleDef.id().isBlank()) {
      return ruleDef.id();
    }
    return member.getDeclaringClass().getSimpleName() + ":" + member.getName();
  }

  private List<? extends Condition> determinePreconditions(RuleDef ruleDef) {
    return Arrays.stream(ruleDef.preconditions())
        .map(this::preconditionToCondition)
        .toList();
  }

  private Condition preconditionToCondition(Precondition precondition) {
    RuleSelector ruleSelector = RuleSelector.of(precondition.rules());
    Set<String> paths = Arrays.stream(precondition.paths()).collect(Collectors.toSet());
    return new RuleCondition(Values.val(ruleSelector), Values.val(paths));
  }

  interface MethodInvoker extends BiFunction<ValidationContext, Object, Result> {

    default Result apply(ValidationContext context, Object facts) {
      try {
        return validate(context, facts);
      } catch (Exception e) {
        LOGGER.error("Rule execution failed", e);
        return Result.failed(new SystemResultReason(
            Code.RuleExecutionThrowsException,
            e.getMessage()
        ));
      }
    }

    Result validate(ValidationContext context, Object facts) throws Exception;
  }
}
