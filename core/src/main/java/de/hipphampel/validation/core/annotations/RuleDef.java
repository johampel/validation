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
package de.hipphampel.validation.core.annotations;

import de.hipphampel.validation.core.condition.Condition;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.provider.AnnotationRuleRepository;
import de.hipphampel.validation.core.provider.RuleDefHandler;
import de.hipphampel.validation.core.rule.ConditionRule;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.Rule;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Predicate;

/**
 * Annotates a class member to define a new {@link Rule}.
 * <p>
 * Annotations of this type are evaluated by the {@link RuleDefHandler}.
 * <p>
 * The exact behaviour of this annotation depends on the member being annotated:
 * <p>
 * If the annotated object is a field, then the field must be final, not-null and have the type {@link Condition} or {@link Predicate}. The
 * {@code AnnotationRuleRepository} then creates a {@link ConditionRule} for it, whereas the missing information (like the id) is taken from
 * the annotation.
 * <p>
 * If the annotated object is a method, then the following restrictions apply:
 * <ol>
 *   <li>The return type of the method must be either a {@link Result} or a {@link Boolean} or a {@code boolean}. Booleans are automatically
 *   converted to {@code Results}. Any other return type will cause the {@code Rule} to return a failed result (at least by default)</li>
 *   <li>The method must have at least one parameter; all parameters must be bound via one of the bind annotations: {@link BindContext},
 *   {@link BindFacts}, {@link BindParentFacts}, {@link BindRootFacts}, {@link BindContextParameter}, {@link BindPath}, or
 *   {@link BindMetadata}.</li>
 *   <li>For simplicity the following shortcuts exists:
 *   <ul>
 *     <li>If the method has exactly one parameter having none of the binding annotations mentioned above, {@code BindFacts} is implicitly
 *      assumed</li>
 *     <li>If the method has exactly two parameter having name of the binding annotations mentioned above and the first parameter has the
 *      type {@link ValidationContext}, {@code BindFacts} is implicitly assumed for the second and {@code BindContext} for the first
 *      parameter</li>
 *   </ul>
 *   </li>
 * </ol>
 * <p>
 * Generally, the member might be static or not. If it is not static, the {@code Rule} implementation might refer to the state that is
 * provided be the enclosing class.
 * <p>
 * In opposite to {@link RuleRef}, this annotation defines a new {@code Rule}.
 *
 * @see RuleRef
 * @see RuleDefHandler
 * @see AnnotationRuleRepository
 * @see Rule
 * @see BindContext
 * @see BindContextParameter
 * @see BindFacts
 * @see BindRootFacts
 * @see BindParentFacts
 * @see BindMetadata
 * @see BindPath
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface RuleDef {

  /**
   * The id of the rule to create.
   * <p>
   * If the setting is an empty string, the id is automatically generated with the format {@code <className>:<memberName>} (whereas the
   * {@code className} is the local class name without the package.
   *
   * @return The id of the {@link Rule}
   */
  String id() default "";

  /**
   * The type of object being validated by this definition.
   * <p>
   * This is only evaluated in case of field based rule definitions; in case of method based ones this is derived from the method signature
   *
   * @return The facts type
   */
  Class<?> factsType() default Object.class;

  /**
   * The fail message.
   * <p>
   * The message is only evaluated for rules constructed based on fields and for rules created for methods returning a {@code boolean} or
   * {@code Boolean}. The message is used, if the rule fails and contains the reason message then.
   *
   * @return The message
   */
  String message() default "";

  /**
   * A list of {@link Precondition Preconditions}.
   * <p>
   * This {@code Rule} is only executed if the {@code Rules} mentioned in the precondition have been executed with a {@code OK} result
   *
   * @return List of preconditions.
   */
  Precondition[] preconditions() default {};

  /**
   * A list of {@link Metadata} entries.
   * <p>
   * This {@code Rule} is annotated with the given metadata
   *
   * @return List of metadata entries
   */
  Metadata[] metadata() default {};
}
