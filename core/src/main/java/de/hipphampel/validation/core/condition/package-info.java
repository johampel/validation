/**
 * Classes describing reusable conditions on facts.
 * <p>
 * The most important classes of this package a
 * {@link de.hipphampel.validation.core.condition.Condition}, which is an interface for describing a
 * condition, and {@link de.hipphampel.validation.core.condition.Conditions}, which provide a set of
 * factory methods to create {@code Condition} instances.
 * <p>
 * Technically, a {@code Condition} is a predicate on the current
 * {@link de.hipphampel.validation.core.execution.ValidationContext ValidationContext} in
 * combination with the object being validated. But in most cases, a {@code Condition} is
 * constructed based on {@link de.hipphampel.validation.core.value.Value Value} objects and/or other
 * {@code Conditions}:
 * <p>
 * An example for a {@code Condition} based on {@code Values} is the
 * {@link de.hipphampel.validation.core.condition.IsNullCondition IsNullCondition}, that checks,
 * whether a {@code Value} is {@code null} or not and returns {@code true} or {@code false}
 * accordingly. An example for a {@code Condition} that is built based in other {@code Conditions},
 * is the {@link de.hipphampel.validation.core.condition.NotCondition NotCondition}, which negates
 * the outcome of a given one.
 * <p>
 * {@code Conditions} can be used to describe reusable, often recurring conditions that have to be
 * evaluated. They are also used to describe the
 * {@link de.hipphampel.validation.core.rule.Rule#getPreconditions() preconditions} of a
 * {@link de.hipphampel.validation.core.rule.Rule Rule}.
 *
 * @see de.hipphampel.validation.core.condition.Condition
 * @see de.hipphampel.validation.core.condition.Conditions
 * @see de.hipphampel.validation.core.value.Value
 * @see de.hipphampel.validation.core.rule.Rule
 */
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
