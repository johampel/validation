package de.hipphampel.validation.core.annotations;

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

import de.hipphampel.validation.core.condition.Condition;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.provider.AnnotationRuleRepository;
import de.hipphampel.validation.core.rule.ConditionRule;
import de.hipphampel.validation.core.rule.FunctionRule;
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
 * Annotations of this type are evaluated by the {@link AnnotationRuleRepository}.
 * <p>
 * The exact behaviour of this annotation depends on the member being annotated:
 * <p>
 * If the annotated object is a field, then the field must be final, not-null and have the type
 * {@link Condition} or {@link Predicate}. The {@code AnnotationRuleRepository} then creates a
 * {@link ConditionRule} for it, whereas the missing information, like the id is taken from the
 * annotation.
 * <p>
 * If the annotated object is a method, then the method must accept either exactly one argument or
 * two, whereas - in case of two arguments - the first must be {@link ValidationContext} and the
 * last one is always the object to be validated. Furthermore, the return type of the method must be
 * either a {@code boolean}, a {@code Boolean}, or a {@link Result}. The
 * {@code AnnotationRuleRepository} creates then a {@link FunctionRule} for it.
 * <p>
 * Generally, the member might be static or not. If it is not static, the {@code Rule}
 * implementation might refer to the state that is provided be the enclosing class.
 * <p>
 * In opposite to {@link RuleRef}, this annotation defines a new {@code Rule}
 *
 * @see RuleRef
 * @see AnnotationRuleRepository
 * @see Rule
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface RuleDef {

  /**
   * The id of the rule to create.
   * <p>
   * If the setting is an empty string, the id is automatically generated with the format
   * {@code <className>:<memberName>} (whereas the {@code className} is the local class name without
   * the package.
   *
   * @return The id of the {@link Rule}
   */
  String id() default "";

  /**
   * The type of object being validated by this definition.
   * <p>
   * This is only evaluated in case of field based rule definitions; in case of method based ones
   * this is derived from the method signature
   *
   * @return The facts type
   */
  Class<?> factsType() default Object.class;

  /**
   * The fail message.
   * <p>
   * The message is only evaluated for rules constructed based on fields and for rules created for
   * methods returning a {@code boolean} or {@code Boolean}. The message is used, if the rule fails
   * and contains the reason message then.
   *
   * @return The message
   */
  String message() default "";

  /**
   * A set of {@link Rule} ids that acts as precondtions. This {@code Rule} is only executed if the
   * {@code Rules} mentioned in the precondition have been executed with a {@code ok} result before
   *
   * @return List of preconditions.
   */
  Precondition[] preconditions() default {};
}
