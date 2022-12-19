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

import de.hipphampel.validation.core.annotations.RuleDef;
import de.hipphampel.validation.core.annotations.RuleRef;
import de.hipphampel.validation.core.event.EventListener;
import de.hipphampel.validation.core.event.NoopSubscribableEventPublisher;
import de.hipphampel.validation.core.event.Subscription;
import de.hipphampel.validation.core.exception.RuleNotFoundException;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.utils.Pair;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RuleRepository} using annotations to define {@link Rule Rules}.
 * <p>
 * This implementation uses {@link Handler annotation handlers} that know annotations to define new or refer to existing {@code Rules} and
 * makes them available. By default, the {@link RuleDefHandler} and {@link RuleRefHandler} are defined, but it is also possible to register
 * completely different handlers in order to support other kind of annotations.
 * <p>
 * Generally, a {@link AnnotationRuleRepository} recognizes all public fields and methods of the class or instance that have an annotation
 * that is accepted by a handler. For example, the following is a class that exposes two {@code Rules} that can be recognized by an
 * {@code AnnotationRuleRepository}:
 * <pre>
 *   public class AClass {
 *     &#64;RuleRef
 *     public static Rule&lt;String> aStaticRule = RuleBuilder
 *                    .functionRule("lengthIsThree", String.class)
 *                    .withFunction((context, facts) -> facts.length() == 3)
 *                    .build();
 *
 *     &#64;RuleDef
 *     public boolean isGreaterThan(
 *         &#64;BindFacts String left,
 *         &#64;BindContextParameter("value") String right) {
 *       return left.compareTo(right) > 0;
 *     }
 *   }
 *   ...
 *   RuleRepository repository = AnnotationRepository.ofInstance(new AClass());
 * </pre>
 * <p>
 * Instances of this class are static, so that the set of known {@code Rules} never changes
 *
 * @see RuleDef
 * @see RuleRef
 */
public class AnnotationRuleRepository implements RuleRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationRuleRepository.class);

  /**
   * Handler for annotations.
   * <p>
   * A {@code Handler} defines how to interpret an {@link Annotation}. It is called by the repository whenever a matching annotation is
   * found in order to extract the {@link Rule Rules}
   *
   * @param <T> The annotation type
   */
  public interface Handler<T extends Annotation> {

    /**
     * Gets the annotation type handled by this handler.
     *
     * @return The annotation class.
     */
    Class<T> getAnnotation();

    /**
     * Called, when the {@code annotation} was found on a {@code field}.
     *
     * @param repositoryInstance The repository instance or {@code null}, if static
     * @param field              The {@link Field}
     * @param annotation         The annotation
     * @return A list of produced {@code Rules}
     */
    default List<Rule<?>> handleAnnotationForField(Object repositoryInstance, Field field, T annotation) {
      return List.of();
    }

    /**
     * Called, when the {@code annotation} was found on a {@code method}.
     *
     * @param repositoryInstance The repository instance or {@code null}, if static
     * @param method             The {@link Method}
     * @param annotation         The annotation
     * @return A list of produced {@code Rules}
     */
    default List<Rule<?>> handleAnnotationForMethod(Object repositoryInstance, Method method, T annotation) {
      return List.of();
    }

    /**
     * Called, when the {@code annotation} was found on a {@code type}.
     *
     * @param repositoryInstance The repository instance or {@code null}, if static
     * @param type               The {@link Class}
     * @param annotation         The annotation
     * @return A list of produced {@code Rules}
     */
    default List<Rule<?>> handleAnnotationForType(Object repositoryInstance, Class<?> type, T annotation) {
      return List.of();
    }
  }

  private final Object instance;
  private final Map<String, Rule<?>> rules;

  /**
   * Creates an instance based on a {@link Class}.
   * <p>
   * Instances created this way rely on static members of {@code clazz} only. The repository uses the {@link RuleDefHandler} and
   * {@link RuleRefHandler} to recognize rules.
   *
   * @param clazz The class to get the {@link Rule Rules} from
   * @return A {@link AnnotationRuleRepository}
   */
  public static AnnotationRuleRepository ofClass(Class<?> clazz) {
    return new AnnotationRuleRepository(null, clazz);
  }

  /**
   * Creates an instance based on a {@link Class}  using the given {@link Handler handlers}.
   * <p>
   * Instances created this way rely on static members of {@code clazz} only.
   *
   * @param clazz    The class to get the {@link Rule Rules} from
   * @param handlers The list of handlers
   * @return A {@link AnnotationRuleRepository}
   */
  public static AnnotationRuleRepository ofClass(Class<?> clazz, List<? extends Handler<?>> handlers) {
    return new AnnotationRuleRepository(null, clazz, handlers);
  }

  /**
   * Creates an instance based on an object.
   * <p>
   * Instances created this way rely on static and non-static members of {@code instance}. The repository uses the {@link RuleDefHandler}
   * and {@link RuleRefHandler} to recognize rules.
   *
   * @param instance The object instance
   * @return A {@link AnnotationRuleRepository}
   */
  public static AnnotationRuleRepository ofInstance(Object instance) {
    return new AnnotationRuleRepository(instance, instance.getClass());
  }

  /**
   * Creates an instance based on an object using the given {@link Handler handlers}.
   * <p>
   * Instances created this way rely on static and non-static members of {@code instance}.
   *
   * @param instance The object instance
   * @param handlers The list of handlers
   * @return A {@link AnnotationRuleRepository}
   */
  public static AnnotationRuleRepository ofInstance(Object instance, List<? extends Handler<?>> handlers) {
    return new AnnotationRuleRepository(instance, instance.getClass(), handlers);
  }

  /**
   * Creates an instance based on an object.
   * <p>
   * Instances created this way rely on static and static members of {@code instance}. The repository uses the {@link RuleDefHandler} and
   * {@link RuleRefHandler} to recognize rules.
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
   * Creates an instance based on an object using the given {@link Handler handlers}.
   * <p>
   * Instances created this way rely on non-static and static members of {@code instance}.
   *
   * @param instance The object instance
   * @param clazz    The type of the instance
   * @param handlers The list of handlers
   * @param <T>      The type of the instance
   * @return A {@link AnnotationRuleRepository}
   */
  public static <T> AnnotationRuleRepository ofInstance(T instance, Class<? extends T> clazz, List<? extends Handler<?>> handlers) {
    return new AnnotationRuleRepository(instance, clazz, handlers);
  }

  /**
   * Constructor.
   *
   * @param instance The instance the rules are bound to, might be {@code null}
   * @param clazz    The {@link Class} to use
   * @param handlers The {@link Handler Handlers} to use
   * @param <T>      The type of the instance
   */
  public <T> AnnotationRuleRepository(T instance, Class<? extends T> clazz, List<? extends Handler<?>> handlers) {
    this.instance = instance;
    this.rules = fillRuleMap(clazz, handlers);
  }

  /**
   * Constructor.
   * <p>
   * Creates an instance that knows the {@link RuleRef} and {@link RuleDef} annotations
   *
   * @param instance The instance the rules are bound to, might be {@code null}
   * @param clazz    The {@link Class} to use
   * @param <T>      The type of the instance
   */
  public <T> AnnotationRuleRepository(T instance, Class<? extends T> clazz) {
    this(instance, clazz, List.of(new RuleDefHandler(), new RuleRefHandler()));
  }

  private <T> Map<String, Rule<?>> fillRuleMap(Class<? extends T> clazz, List<? extends Handler<?>> handlers) {
    Map<Class<?>, Handler<?>> handlerMap = handlers.stream().collect(Collectors.toMap(Handler::getAnnotation, Function.identity()));

    Map<String, Rule<?>> rules = new HashMap<>();

    for (Method method : clazz.getMethods()) {
      Pair<Annotation, Handler<Annotation>> handler = getHandler(method, method.getAnnotations(), handlerMap);
      if (handler == null) {
        continue;
      }
      if (isStatic(method) || instance != null) {
        Object effectiveInstance = isStatic(method) ? null : instance;
        handler.second().handleAnnotationForMethod(effectiveInstance, method, handler.first())
            .forEach(rule -> rules.put(rule.getId(), rule));
      }
    }
    for (Field field : clazz.getFields()) {
      Pair<Annotation, Handler<Annotation>> handler = getHandler(field, field.getAnnotations(), handlerMap);
      if (handler == null) {
        continue;
      }
      if (isStatic(field) || instance != null) {
        Object effectiveInstance = isStatic(field) ? null : instance;
        handler.second().handleAnnotationForField(effectiveInstance, field, handler.first())
            .forEach(rule -> rules.put(rule.getId(), rule));
      }
    }

    return Collections.unmodifiableMap(rules);
  }

  @SuppressWarnings("unchecked")
  private <T extends Annotation> Pair<T, Handler<T>> getHandler(Member member, Annotation[] annotations,
      Map<Class<?>, Handler<?>> handlerMap) {
    List<Pair<T, Handler<T>>> handlers = Stream.of(annotations)
        .map(annotation -> Pair.of(annotation, handlerMap.get(annotation.annotationType())))
        .filter(pair -> pair.second() != null)
        .map(pair -> (Object) pair)
        .map(pair -> (Pair<T, Handler<T>>) pair)
        .toList();
    if (handlers.isEmpty()) {
      return null;
    }
    if (handlers.size() > 1) {
      throw new IllegalArgumentException("Found more than one rule related annotation on member " + member.getName());
    }
    return handlers.get(0);
  }

  private boolean isStatic(Member member) {
    return Modifier.isStatic(member.getModifiers());
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

  @Override
  public Subscription subscribe(EventListener listener) {
    return NoopSubscribableEventPublisher.INSTANCE.subscribe(listener);
  }

}
