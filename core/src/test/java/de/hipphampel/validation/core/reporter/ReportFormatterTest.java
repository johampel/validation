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

import de.hipphampel.validation.core.path.CollectionPathResolver;
import de.hipphampel.validation.core.path.Path;
import de.hipphampel.validation.core.report.Report;
import de.hipphampel.validation.core.report.ReportEntry;
import de.hipphampel.validation.core.report.ReportFormatter;
import de.hipphampel.validation.core.rule.OkRule;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.Rule;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ReportFormatterTest {
  
  private static final Rule<?> rule1 = new OkRule<>("rule1");
  private static final Rule<?> rule2 = new OkRule<>("rule2");
  private static final ReportEntry e1 = new ReportEntry(pathOf("a"), rule1, Result.ok());
  private static final ReportEntry e2 = new ReportEntry(pathOf("b"), rule1,
      Result.skipped("msg1"));
  private static final ReportEntry e3 = new ReportEntry(pathOf("c"), rule1,
      Result.failed("msg2"));
  private static final ReportEntry e4 = new ReportEntry(pathOf("d"), rule1, Result.ok());
  private static final ReportEntry e5 = new ReportEntry(pathOf("a"), rule1,
      Result.skipped("msg3"));
  private static final ReportEntry e6 = new ReportEntry(pathOf("b"), rule2,
      Result.failed("msg4"));
  private static final ReportEntry e7 = new ReportEntry(pathOf("c"), rule2, Result.ok());
  private static final ReportEntry e8 = new ReportEntry(pathOf("d"), rule2,
      Result.skipped("msg5"));
  private static final ReportEntry e9 = new ReportEntry(pathOf("a"), rule2,
      Result.failed("msg6"));
  private static final Report report = new Report(Set.of(e1, e2, e3, e4, e5, e6, e7, e8, e9));


  @Test
  public void csv() throws IOException {
    try(StringWriter out = new StringWriter()) {
      new ReportFormatter.Csv().format(report, out);
      assertThat(out.toString()).isEqualTo(
          """
          "Rule";"Path";"Code";"Reason"
          "rule1";"c";"FAILED";"msg2"
          "rule2";"a";"FAILED";"msg6"
          "rule2";"b";"FAILED";"msg4"
          "rule1";"a";"SKIPPED";"msg3"
          "rule1";"b";"SKIPPED";"msg1"
          "rule2";"d";"SKIPPED";"msg5"
          "rule1";"a";"OK";""
          "rule1";"d";"OK";""
          "rule2";"c";"OK";""
          """
      );
    }
  }
  private static Path pathOf(String path) {
    return new CollectionPathResolver().parse(path);
  }

}
