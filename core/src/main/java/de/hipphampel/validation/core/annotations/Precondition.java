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

import de.hipphampel.validation.core.rule.Rule;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used inside {@link RuleDef} annotations to define precondtions of a {@link Rule}.
 * <p>
 * This annotation allows to define the preconditions of the rule. A single precondition becomes {@code true}, if the {@code rules}
 * mentioned in the annotation evaluate to {@code OK} for all {@code paths}.
 * <p>
 * Usage example:
 * <pre>
 *     &#64;RuleDef(id = "parent",
 *       preconditions = {
 *           &#64;Precondition(rules = "child", paths="aPath")
 *       })
 * </pre>
 * <p>
 * In the example above, the {@code Rule} with the id {@code parent} has a precondition that becomes {@code true}, if the {@code Rule} with
 * the id {@code child} evaluates to {@code OK} (for path {@code aPath}).
 *
 * @see RuleDef
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Precondition {

  /**
   * The list of rule ids that need to be evaluated.
   *
   * @return The rule ids
   */
  String[] rules();

  /**
   * The paths the rules have to validated for.
   *
   * @return The paths
   */
  String[] paths() default {};
}
