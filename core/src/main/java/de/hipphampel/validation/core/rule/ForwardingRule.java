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
package de.hipphampel.validation.core.rule;

import de.hipphampel.validation.core.execution.ValidationContext;

/**
 * Supplemental interface for {@link Rule Rules} that forward the rule execution to zero, one or many other {@code Rules}.
 * <p>
 * A {@code ForwardingRule} has typically no own business logic to validate an object, but it orchestrates other {@code Rules} meaning that
 * it selects zero, one or more other {@code Rules} and executes them. The exact mechanism for selecting the other {@code Rules} is not part
 * of this interface, but typically implemented in the {@link #validate(ValidationContext, Object) validate} method.
 * <p>
 * If there are none or more than one {@code Rules} to execute, it is common to all {@code ForwardingRules} that they need to report a
 * {@link Result}, if no {@code Rule} or merge {@code Results} if more than one {@code Rule} is executed. For these purposes the interface
 * provides some helper methods with a default implementation.
 *
 * @param <T> Type of the object being validated
 */
public interface ForwardingRule<T> extends Rule<T> {

  /**
   * Returns the {@link Result}, if no {@link Rule} is executed by this {@code ForwardingRule}.
   * <p>
   * By default, this returns {@link Result#ok() ok}
   *
   * @return The {@code Result}
   */
  default Result noRuleResult() {
    return Result.ok();
  }

  /**
   * Called to merge the results of two validations.
   * <p>
   * Since a {@link Rule} returns only one {@link Result} but this executes potentially more than one {@code Rule}, this method is used
   * merge two into one. The merged result has no reason by default
   * <p>
   * This returns a {@code Result} with the highest severity of the two inputs.
   *
   * @param first  First {@code Result}
   * @param second Second {@code Result}
   * @return Merged {@code Result}
   */
  default Result mergeResults(Result first, Result second) {
    return new Result(ResultCode.getHighestSeverityOf(first.code(), second.code()), null);
  }

}
