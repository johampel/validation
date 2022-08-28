package de.hipphampel.validation.core.condition;

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

import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.rule.ConditionRule;
import de.hipphampel.validation.core.rule.Rule;


/**
 * Represents a simple condition on {@code facts}.
 * <p>
 * Compared with a {@link Rule}, a {@code Condition} is much simplier, it can be seen as a simple
 * predicate on the {@code facts} to be validated. {@code Conditions} accept, beside the
 * {@code facts} parameter a reference to the current {@link ValidationContext}, in order to allow
 * access to external data not directly part of the object being checked.
 * <p>
 * {@code Conditions} are used for the preconditions of {@code Rules}, but it is also
 * possible to wrap a {@code Rule} around a {@code Condition} using the {@link ConditionRule}.
 *
 * @see Rule
 * @see ConditionRule
 */
@FunctionalInterface
public interface Condition {

  /**
   * Evaluates the condition.
   *
   * @param context The {@link ValidationContext}
   * @param facts   The facts to validate
   * @return {@code true}, if the condition is fulfilled, otherwise {@code false}
   */
  boolean evaluate(ValidationContext context, Object facts);

}
