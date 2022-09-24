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
import de.hipphampel.validation.core.report.Report;
import de.hipphampel.validation.core.report.ReportReporter;
import de.hipphampel.validation.core.report.Reporter;
import de.hipphampel.validation.core.report.ReporterFactory;
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
 * A typical implementation of this interface - such as the {@link DefaultValidator} - will delegate
 * most of its work to subordinated classes. You may use the {@link ValidatorBuilder} to create an
 * instance of an {@code Validator}
 */
public interface Validator {

  /**
   * Validates {@code facts} using the rules provided by the {@code ruleSelector} and produces a
   * {@link Report}.
   * <p>
   * Typically, an implementation creates a {@link ValidationContext} which is passed to a
   * {@link RuleExecutor} to execute the rules. The final result is built using a {@link Reporter}.
   *
   * @param facts        The object being validated
   * @param ruleSelector The {@link RuleSelector} selecting the rules to execute
   * @return The validation result (a {@code Report}).
   */
  default Report validate(Object facts, RuleSelector ruleSelector) {
    return validate(ReportReporter::new, facts, ruleSelector, Map.of());
  }

  /**
   * Validates {@code facts} using the rules provided by the {@code ruleSelector} and fills a report
   * created by the {@code reporterFactory}.
   * <p>
   * Typically, an implementation creates a {@link ValidationContext} which is passed to a
   * {@link RuleExecutor} to execute the rules. The final result is built using a {@link Reporter}.
   *
   * @param reporterFactory The {@link ReporterFactory} to use
   * @param facts           The object being validated
   * @param ruleSelector    The {@link RuleSelector} selecting the rules to execute
   * @param <T>             Type of the report to generate
   * @return The validation result
   */
  default <T> T validate(ReporterFactory<T> reporterFactory, Object facts,
      RuleSelector ruleSelector) {
    return validate(reporterFactory, facts, ruleSelector, Map.of());
  }

  /**
   * Validates {@code facts} using the rules provided by the {@code ruleSelector} and produces a
   * {@link Report}.
   * <p>
   * Typically, an implementation creates a {@link ValidationContext} which is passed to a
   * {@link RuleExecutor} to execute the rules. The final result is built using a {@link Reporter}.
   *
   * @param facts        The object being validated
   * @param ruleSelector The {@link RuleSelector} selecting the rules to execute
   * @param parameters   Additional parameters being set in the {@code V}
   * @return The validation result (a {@code Report}).
   */
  default Report validate(Object facts, RuleSelector ruleSelector, Map<String, Object> parameters) {
    return validate(ReportReporter::new, facts, ruleSelector, parameters);
  }

  /**
   * Validates {@code facts} using the rules provided by the {@code ruleSelector} and fills a report
   * created by the {@code reporterFactory}.
   * <p>
   * Typically, an implementation creates a {@link ValidationContext} which is passed to a
   * {@link RuleExecutor} to execute the rules. The final result is built using a {@link Reporter}.
   *
   * @param reporterFactory The {@link ReporterFactory} to use
   * @param facts           The object being validated
   * @param ruleSelector    The {@link RuleSelector} selecting the rules to execute
   * @param parameters      Additional parameters being set in the {@code V}
   * @param <T>             Type of the report to generate
   * @return The validation result
   */
  default <T> T validate(ReporterFactory<T> reporterFactory, Object facts,
      RuleSelector ruleSelector, Map<String, Object> parameters) {
    Reporter<T> reporter = reporterFactory.createReporter();
    ValidationContext context = createValidationContext(reporter, parameters);
    context.getRuleExecutor().validate(context, ruleSelector, facts);
    return reporter.getReport();
  }

  /**
   * Asynchronously validates {@code facts} using the rules provided by the {@code ruleSelector} and
   * produces a {@link Report}.
   * <p>
   * Typically, an implementation creates a {@link ValidationContext} which is passed to a
   * {@link RuleExecutor} to execute the rules. The final result is built using a {@link Reporter}.
   *
   * @param facts        The object being validated
   * @param ruleSelector The {@link RuleSelector} selecting the rules to execute
   * @return The validation result (a {@code Report}).
   */
  default CompletableFuture<Report> validateAsync(Object facts, RuleSelector ruleSelector) {
    return validateAsync(ReportReporter::new, facts, ruleSelector, Map.of());
  }

  /**
   * Asynchronously validates {@code facts} using the rules provided by the {@code ruleSelector} and
   * fills a report created by the {@code reporterFactory}.
   * <p>
   * Typically, an implementation creates a {@link ValidationContext} which is passed to a
   * {@link RuleExecutor} to execute the rules. The final result is built using a {@link Reporter}.
   *
   * @param reporterFactory The {@link ReporterFactory} to use
   * @param facts           The object being validated
   * @param ruleSelector    The {@link RuleSelector} selecting the rules to execute
   * @param <T>             Type of the report to generate
   * @return The validation result.
   */
  default <T> CompletableFuture<T> validateAsync(ReporterFactory<T> reporterFactory, Object facts,
      RuleSelector ruleSelector) {
    return validateAsync(reporterFactory, facts, ruleSelector, Map.of());
  }

  /**
   * Asynchronously validates {@code facts} using the rules provided by the {@code ruleSelector} and
   * produces a {@link Report}.
   * <p>
   * Typically, an implementation creates a {@link ValidationContext} which is passed to a
   * {@link RuleExecutor} to execute the rules. The final result is built using a {@link Reporter}.
   *
   * @param facts        The object being validated
   * @param ruleSelector The {@link RuleSelector} selecting the rules to execute
   * @param parameters   Additional parameters being set in the {@code V}
   * @return The validation result (a {@code Report}).
   */
  default CompletableFuture<Report> validateAsync(Object facts, RuleSelector ruleSelector,
      Map<String, Object> parameters) {
    return validateAsync(ReportReporter::new, facts, ruleSelector, parameters);
  }

  /**
   * Asynchronously validates {@code facts} using the rules provided by the {@code ruleSelector} and
   * fills a report created by the {@code reporterFactory}.
   * <p>
   * Typically, an implementation creates a {@link ValidationContext} which is passed to a
   * {@link RuleExecutor} to execute the rules. The final result is built using a {@link Reporter}.
   *
   * @param reporterFactory The {@link ReporterFactory} to use
   * @param facts           The object being validated
   * @param ruleSelector    The {@link RuleSelector} selecting the rules to execute
   * @param parameters      Additional parameters being set in the {@code V}
   * @param <T>             Type of the report to generate
   * @return The validation result (a {@code Report}).
   */
  default <T> CompletableFuture<T> validateAsync(ReporterFactory<T> reporterFactory, Object facts,
      RuleSelector ruleSelector,
      Map<String, Object> parameters) {
    Reporter<T> reporter = reporterFactory.createReporter();
    ValidationContext context = createValidationContext(reporter, parameters);
    return context.getRuleExecutor().validateAsync(context, ruleSelector, facts)
        .thenApply(ignore -> reporter.getReport());
  }

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
  <T> ValidationContext createValidationContext(Reporter<T> reporter,
      Map<String, Object> parameters);

}
