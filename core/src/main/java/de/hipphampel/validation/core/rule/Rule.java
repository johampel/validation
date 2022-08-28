package de.hipphampel.validation.core.rule;

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
import de.hipphampel.validation.core.execution.RuleExecutor;
import de.hipphampel.validation.core.execution.ValidationContext;
import java.util.List;
import java.util.Map;

/**
 * Represents a single validation rule.
 * <p>
 * A validation rule is checks, whether a certain statement is true for a given object. Such a
 * statement can be quite simple (e.g. in case of numbers: "the number is positive"), or more
 * complex (e.g. in case oft polygons: "the polygon build a rectangular triangle")
 * <p>
 * In terms of this library, a validation rule consists of:
 * <ol>
 *   <li>An unique {@link #getId() id} to identify the rule</li>
 *   <li>a method called {@link #validate(ValidationContext, Object) validate} that checks,
 *   whether the statement to check is true or false</li>
 *   <li>Also it might contain {@link #getPreconditions() preconditions} that need to be truw before
 *   the statement itself can be checked</li>
 *   <li>Finally, {@link #getMetadata() metadata} allows to attach further fixed data to the
 *   rule</li>
 * </ol>
 * <p>
 * A rule is exeucted by a {@link RuleExecutor}, which ensures that the preconditions and the
 * validate methods are called in the correct order and only, if allowed.
 * <p>
 * It is recommended, to use the {@link AbstractRule} as a base class for complex rule
 * implementations or use the {@link RuleBuilder} to construct more simple rules.
 *
 * @param <T> The type of the object to validate.
 * @see RuleExecutor
 * @see AbstractRule
 * @see RuleBuilder
 */
public interface Rule<T> {

  /**
   * Gets the unique  id of the {@code Rule}.
   *
   * @return The id
   */
  String getId();

  /**
   * Gets the {@link Class} of the {@code facts} being validated by this rule.
   *
   * @return The {@code Class}
   */
  default Class<? super T> getFactsType() {
    return RuleUtils.determineRuleFactsType(this);
  }

  /**
   * Gets the metdata of this instance.
   * <p>
   * The metadata can serve different purposes: they can be used to filter or categorize rules, but
   * they can also be accessed during rule execution. The metdata should be constant during lifetime
   * of the rule.
   *
   * @return The metadata
   */
  Map<String, Object> getMetadata();

  /**
   * Gets a list of the preconditions of the rule.
   * <p>
   * The preconditions are checked, before the rule is actually executed (by calling the
   * {@link #validate(ValidationContext, Object) validate} method). In case that not all conditions
   * are valid, the {@code validate} method is not called and the result of the validation is never
   * {@link ResultCode#OK},
   *
   * @return List of {@link Condition Conditions} to evaluate.
   */
  List<Condition> getPreconditions();

  /**
   * Executes the rule.
   * <p>
   * It is guaranteed that this method is called only, if all the
   * {@link #getPreconditions() preconditions} are true.
   *
   * @param context Contextual information about the validation process.
   * @param facts   The object being validated.
   * @return A {@link Result} indicating the result of the validationø
   */
  Result validate(ValidationContext context, T facts);
}
