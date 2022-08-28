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

import de.hipphampel.validation.core.path.Path;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.Rule;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * A {@link Reporter} that uses a {@link Report} as intermediate result.
 * <p>
 * This basically collects all validation results into a {@code Report} and allows to transform that
 * {@code Report} into the final report when calling {@code getReport}
 *
 * @param <T> Type of teh report to produce
 * @see Reporter
 * @see Report
 */
public abstract class AbstractReportBasedReporter<T> implements Reporter<T> {

  private final Set<ReportEntry> entries;
  private final Predicate<ReportEntry> entryPredicate;

  /**
   * Constructor.
   *
   * @param entryPredicate A {@code Predicate} that decides, whether a {@link ReportEntry} should
   *                       become part of the {@code Report}, might be {@code null}
   */
  protected AbstractReportBasedReporter(Predicate<ReportEntry> entryPredicate) {
    this.entries = ConcurrentHashMap.newKeySet();
    this.entryPredicate = entryPredicate == null ? entry -> true : entryPredicate;
  }


  @Override
  public void add(Path path, Object facts, Rule<?> rule, Result result) {
    ReportEntry entry = new ReportEntry(path, rule, result);
    if (entryPredicate.test(entry)) {
      entries.add(entry);
    }
  }

  @Override
  public T getReport() {
    return buildReport(new Report(entries));
  }

  /**
   * Called to generate the final report.
   *
   * @param report The intermediate{@link Report}
   * @return The report
   */
  protected abstract T buildReport(Report report);
}
