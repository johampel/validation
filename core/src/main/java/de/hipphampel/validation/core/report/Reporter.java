package de.hipphampel.validation.core.report;

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

import de.hipphampel.validation.core.Validator;
import de.hipphampel.validation.core.execution.RuleExecutor;
import de.hipphampel.validation.core.path.Path;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.utils.Stacked;

/**
 * Generate a final validation report.
 * <p>
 * A {@code Reporter} is invoked by the {@link RuleExecutor} during validation to form the
 * validation report of the object being validated. For this, the {@code RuleExecutor} calls
 * {@link #add(Stacked, Object, Rule, Result) add} each time a {@link Rule} is executed. When all
 * validations are done, the {@link Validator} calls {@link #getReport() getReport} to obtain the
 * final validation result.
 * <p>
 * It is totally implementation dependent, what the validation report is, the {@link ReportReporter}
 * forms a report containing all results for each an every {@code RuleResult}; in contrast the
 * {@link BooleanReporter} just returns a boolean
 * <p>
 * Note that instances of the {@code Reporter} are stateful be design; normally the
 * {@code Validator} is responsible to create instances of this type.
 *
 * @param <T> Type of the report.
 * @see Validator
 * @see BooleanReporter
 * @see RuleExecutor
 * @see ReportReporter
 */
public interface Reporter<T> {

  /**
   * Adds the result of a {@link Rule}  execution to the report data.
   *
   * @param path   The {@link Path} to the object being validated, might be {@code null}
   * @param facts  The object being validated ({@code path} points to it)
   * @param rule   The {@code Rule} being executed.
   * @param result The {@link Result}
   */
  void add(Path path, Object facts, Rule<?> rule, Result result);

  /**
   * Called when the validation is done.
   * <p>
   * Returns the validation report.
   *
   * @return The report
   */
  T getReport();
}
