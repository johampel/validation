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
 *
 * A typical implemntation of this interface - such as the {@link GenericValidator} - will delegate
 * most of its work to subordinated classes. You may use the {@link ValidatorBuilder} to create
 * an instance of an {@code Validator}
 *
 * @param <T> Type of the report to return.
 */
public interface Validator<T> {

  Reporter<T> createReporter();

  ValidationContext createValidationContext(Reporter<T> reporter, Map<String, Object> parameters);


  default T validate(Object facts, RuleSelector ruleSelector) {
    return validate(facts, ruleSelector, Map.of());
  }

  default T validate(Object facts, RuleSelector ruleSelector, Map<String, Object> parameters) {
    Reporter<T> reporter = createReporter();
    ValidationContext context = createValidationContext(reporter, parameters);
    context.getRuleExecutor().validate(context, ruleSelector, facts);
    return reporter.getReport();
  }

  default CompletableFuture<T> validateAsync(Object facts, RuleSelector ruleSelector) {
    return validateAsync(facts, ruleSelector, Map.of());
  }

  default CompletableFuture<T> validateAsync(Object facts, RuleSelector ruleSelector,
      Map<String, Object> parameters) {
    Reporter<T> reporter = createReporter();
    ValidationContext context = createValidationContext(reporter, parameters);
    return context.getRuleExecutor().validateAsync(context, ruleSelector, facts)
        .thenApply(ignore -> reporter.getReport());
  }
}
