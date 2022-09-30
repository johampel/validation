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
package de.hipphampel.validation.core.report;

import de.hipphampel.validation.core.path.Path;
import de.hipphampel.validation.core.path.Resolvable;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.ResultCode;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.utils.Stacked;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * {@link Reporter} creating a {@link Report}.
 * <p>
 * Produces {@code Reports} as final result.
 *
 * @see Reporter
 * @see Report
 */
public class ReportReporter extends AbstractReportBasedReporter<Report> {

  /**
   * Default constructor.
   * <p>
   * Records all entries.
   */
  public ReportReporter() {
    super(null);
  }


  /**
   * Constructor.
   * <p>
   * Records only {@link Result Results}, that match the given predicate
   *
   * @param predicate The predicate.
   */
  public ReportReporter(Predicate<ReportEntry> predicate) {
    super(predicate);
  }


  @Override
  protected Report buildReport(Report report) {
    return report;
  }
}
