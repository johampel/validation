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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Most simple {@link Reporter} returning a boolean.
 * <p>
 * This {@code Reporter} returns {@code} true, if all rules are valid
 */
public class BooleanReporter implements Reporter<Boolean> {

  private final boolean skippedIsFailed;
  private final AtomicBoolean report;

  /**
   * Default constructor.
   * <p>
   * Reports {@code true} only if there where neither skipped or failed {@link Result Results}
   */
  public BooleanReporter() {
    this(true);
  }

  /**
   * Constructor.
   * <p>
   * Reports {@code true} if was no {@link Result} indicating a failure. If {@code skippedIsFailed}
   * is {@code true}, skipped results are counted as failure, otherwise as success.
   *
   * @param skippedIsFailed {@code true}, if count skipped {@code Results} as failure.
   */
  public BooleanReporter(boolean skippedIsFailed) {
    this.report = new AtomicBoolean(true);
    this.skippedIsFailed = skippedIsFailed;
  }

  @Override
  public void add(Path path, Object facts, Rule<?> rule, Result result) {
    report.compareAndSet(true, skippedIsFailed ? result.isOk() : !result.isFailed());
  }

  @Override
  public Boolean getReport() {
    return report.get();
  }
}
