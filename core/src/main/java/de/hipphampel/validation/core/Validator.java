package de.hipphampel.validation.core;

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

import de.hipphampel.validation.core.execution.RuleExecutor;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.provider.RuleSelector;
import de.hipphampel.validation.core.report.Reporter;
import de.hipphampel.validation.core.rule.Rule;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Validates objects and generates a validation report.
 * <p>
 * In terms of this interface, validation means to execute the {@link Rule Rules} selected by some
 * {@link RuleSelector}, collect their results and form an according report. The kind of report
 * totally depends on the {@link Reporter} being used.
 * <p>
 * A typical implemntation of this interface - such as the {@link DefaultValidator} - will delegate
 * most of its work to subordinated classes. You may use the {@link ValidatorBuilder} to create an
 * instance of an {@code Validator}
 *
 * @param <T> Type of the report to return.
 */
public interface Validator<T> {

  /**
   * Validates {@code facts} using the rules provided by the {@code ruleSelector}.
   * <p>
   * Typically, an implementation creates a {@link ValidationContext} which is passed to a
   * {@link RuleExecutor} to execute the rules. The final result is built using a {@link Reporter}.
   *
   * @param facts        The object being validated
   * @param ruleSelector The {@link RuleSelector} selecting the rules to execute
   * @return The validation result.
   */
  default T validate(Object facts, RuleSelector ruleSelector) {
    return validate(facts, ruleSelector, Map.of());
  }

  /**
   * Validates {@code facts} using the rules provided by the {@code ruleSelector}.
   * <p>
   * Typically, an implementation creates a {@link ValidationContext} which is passed to a
   * {@link RuleExecutor} to execute the rules. The final result is built using a {@link Reporter}.
   *
   * @param facts        The object being validated
   * @param ruleSelector The {@link RuleSelector} selecting the rules to execute
   * @param parameters   Additional paramters being set in the {@code V}
   * @return The validation result.
   */
  default T validate(Object facts, RuleSelector ruleSelector, Map<String, Object> parameters) {
    Reporter<T> reporter = createReporter();
    ValidationContext context = createValidationContext(reporter, parameters);
    context.getRuleExecutor().validate(context, ruleSelector, facts);
    return reporter.getReport();
  }

  /**
   * Asynchronously validates {@code facts} using the rules provided by the {@code ruleSelector}.
   * <p>
   * Typically, an implementation creates a {@link ValidationContext} which is passed to a
   * {@link RuleExecutor} to execute the rules. The final result is built using a {@link Reporter}.
   *
   * @param facts        The object being validated
   * @param ruleSelector The {@link RuleSelector} selecting the rules to execute
   * @return The validation result.
   */
  default CompletableFuture<T> validateAsync(Object facts, RuleSelector ruleSelector) {
    return validateAsync(facts, ruleSelector, Map.of());
  }

  /**
   * Asynchronously validates {@code facts} using the rules provided by the {@code ruleSelector}.
   * <p>
   * Typically, an implementation creates a {@link ValidationContext} which is passed to a
   * {@link RuleExecutor} to execute the rules. The final result is built using a {@link Reporter}.
   *
   * @param facts        The object being validated
   * @param ruleSelector The {@link RuleSelector} selecting the rules to execute
   * @param parameters   Additional paramters being set in the {@code V}
   * @return The validation result.
   */
  default CompletableFuture<T> validateAsync(Object facts, RuleSelector ruleSelector,
      Map<String, Object> parameters) {
    Reporter<T> reporter = createReporter();
    ValidationContext context = createValidationContext(reporter, parameters);
    return context.getRuleExecutor().validateAsync(context, ruleSelector, facts)
        .thenApply(ignore -> reporter.getReport());
  }

  /**
   * Supplemental method to create a {@link Reporter} to store the validation result.
   * <p>
   * This method ic called for each call of {@code validate} or {@code validateAsync} once, so that
   * each validation run has its own {@code Reporter} instance. The returned instance might be
   * shared between different threads in case that the validation is spawned over multiple threads.
   *
   * @return The {@link Reporter}
   */
  Reporter<T> createReporter();

  /**
   * Creates a {@link ValidationContext} to store contextual information during validation.
   * <p>
   * This is called - similar to {@code createReporter} - for each call of {@code validate} or
   * {@code validateAsync}. But unlike the the {@code Reporter}, the {@code ValidationContext} is
   * not shread between different threads, instead based on this instance copies might be created on
   * demand by calling {@link ValidationContext#copy()}.
   *
   * @param reporter   The {@link Reporter} to use
   * @param parameters Additional validation parameters
   * @return The {@code ValidationContext}
   */
  ValidationContext createValidationContext(Reporter<T> reporter, Map<String, Object> parameters);

}
