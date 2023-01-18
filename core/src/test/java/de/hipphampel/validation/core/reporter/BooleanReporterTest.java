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
package de.hipphampel.validation.core.reporter;

import static org.assertj.core.api.Assertions.assertThat;

import de.hipphampel.validation.core.report.BooleanReporter;
import de.hipphampel.validation.core.report.Reporter;
import de.hipphampel.validation.core.rule.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class BooleanReporterTest {

  @Test
  public void addSuccess() {
    Reporter<Boolean> reporter = new BooleanReporter(null);

    reporter.add(null, null, null, null, Result.ok());
    assertThat(reporter.getReport()).isTrue();
  }

  @Test
  public void addFailed() {
    Reporter<Boolean> reporter = new BooleanReporter(null);

    reporter.add(null, null, null, null, Result.failed());
    assertThat(reporter.getReport()).isFalse();
  }

  @ParameterizedTest
  @CsvSource({
      "true, false",
      "false, true"
  })
  public void addSkipped(boolean skippedIsFailed, boolean expected) {
    Reporter<Boolean> reporter = new BooleanReporter(null, skippedIsFailed);

    reporter.add(null, null, null, null, Result.skipped());
    assertThat(reporter.getReport()).isEqualTo(expected);
  }

  @Test
  public void falseStaysFalse() {
    Reporter<Boolean> reporter = new BooleanReporter(null);

    reporter.add(null, null, null, null, Result.failed());
    assertThat(reporter.getReport()).isFalse();
    reporter.add(null, null, null, null, Result.ok());
    assertThat(reporter.getReport()).isFalse();
  }
}
