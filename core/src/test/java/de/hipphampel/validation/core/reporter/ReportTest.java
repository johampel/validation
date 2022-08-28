package de.hipphampel.validation.core.reporter;

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

import static org.assertj.core.api.Assertions.assertThat;

import de.hipphampel.validation.core.path.CollectionPathResolver;
import de.hipphampel.validation.core.path.Path;
import de.hipphampel.validation.core.report.Report;
import de.hipphampel.validation.core.report.ReportEntry;
import de.hipphampel.validation.core.report.ReportEntry.Field;
import de.hipphampel.validation.core.rule.OkRule;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.ResultCode;
import de.hipphampel.validation.core.rule.Rule;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class ReportTest {

  private static final Rule<?> rule1 = new OkRule<>("rule1");
  private static final Rule<?> rule2 = new OkRule<>("rule2");
  private static final Rule<?> rule3 = new OkRule<>("rule3");

  @Test
  public void isEmpty_empty() {
    Report report = new Report(Set.of());
    assertThat(report.isEmpty()).isTrue();
  }

  @Test
  public void isEmpty_notEmpty() {
    Report report = new Report(Set.of(
        new ReportEntry(pathOf("a"), rule1, Result.ok())
    ));
    assertThat(report.isEmpty()).isFalse();
  }

  @Test
  public void filter_byResultCode() {
    Report report = new Report(Set.of(
        new ReportEntry(pathOf("a"), rule1, Result.ok()),
        new ReportEntry(pathOf("b"), rule2, Result.skipped()),
        new ReportEntry(pathOf("c"), rule3, Result.failed())
    ));
    assertThat(report.filter(ResultCode.OK)).isEqualTo(
        new Report(Set.of(
            new ReportEntry(pathOf("a"), rule1, Result.ok()),
            new ReportEntry(pathOf("b"), rule2, Result.skipped()),
            new ReportEntry(pathOf("c"), rule3, Result.failed())
        ))
    );
    assertThat(report.filter(ResultCode.SKIPPED)).isEqualTo(
        new Report(Set.of(
            new ReportEntry(pathOf("b"), rule2, Result.skipped()),
            new ReportEntry(pathOf("c"), rule3, Result.failed())
        ))
    );
    assertThat(report.filter(ResultCode.FAILED)).isEqualTo(
        new Report(Set.of(
            new ReportEntry(pathOf("c"), rule3, Result.failed())
        ))
    );
  }

  @Test
  public void filter_byPredicate() {
    Report report = new Report(Set.of(
        new ReportEntry(pathOf("a"), rule1, Result.ok()),
        new ReportEntry(pathOf("b"), rule1, Result.skipped()),
        new ReportEntry(pathOf("c"), rule1, Result.failed()),
        new ReportEntry(pathOf("b"), rule2, Result.ok()),
        new ReportEntry(pathOf("b"), rule2, Result.skipped()),
        new ReportEntry(pathOf("c"), rule2, Result.failed()),
        new ReportEntry(pathOf("a"), rule3, Result.ok()),
        new ReportEntry(pathOf("b"), rule3, Result.skipped()),
        new ReportEntry(pathOf("c"), rule3, Result.failed())
    ));
    assertThat(report.filter(e -> e.rule().getId().equals("rule1"))).isEqualTo(
        new Report(Set.of(
            new ReportEntry(pathOf("a"), rule1, Result.ok()),
            new ReportEntry(pathOf("b"), rule1, Result.skipped()),
            new ReportEntry(pathOf("c"), rule1, Result.failed())
        ))
    );
  }

  @Test
  public void getResultCodeWithHighestSeverity() {
    Report report;

    report = new Report(Set.of());
    assertThat(report.getResultCodeWithHighestSeverity()).isEmpty();

    report = new Report(Set.of(
        new ReportEntry(pathOf("a"), rule1, Result.ok()),
        new ReportEntry(pathOf("b"), rule2, Result.skipped()),
        new ReportEntry(pathOf("c"), rule3, Result.failed())
    ));
    assertThat(report.getResultCodeWithHighestSeverity()).contains(ResultCode.FAILED);

    report = new Report(Set.of(
        new ReportEntry(pathOf("a"), rule1, Result.ok()),
        new ReportEntry(pathOf("b"), rule2, Result.skipped())
    ));
    assertThat(report.getResultCodeWithHighestSeverity()).contains(ResultCode.SKIPPED);

    report = new Report(Set.of(
        new ReportEntry(pathOf("a"), rule1, Result.ok())
    ));
    assertThat(report.getResultCodeWithHighestSeverity()).contains(ResultCode.OK);
  }

  @Test
  public void entriesSorted() {
    ReportEntry e1 = new ReportEntry(pathOf("a"), rule1, Result.ok());
    ReportEntry e2 = new ReportEntry(pathOf("b"), rule1, Result.skipped("msg1"));
    ReportEntry e3 = new ReportEntry(pathOf("c"), rule1, Result.failed("msg2"));
    ReportEntry e4 = new ReportEntry(pathOf("d"), rule1, Result.ok());
    ReportEntry e5 = new ReportEntry(pathOf("a"), rule1, Result.skipped("msg3"));
    ReportEntry e6 = new ReportEntry(pathOf("b"), rule2, Result.failed("msg4"));
    ReportEntry e7 = new ReportEntry(pathOf("c"), rule2, Result.ok());
    ReportEntry e8 = new ReportEntry(pathOf("d"), rule2, Result.skipped("msg5"));
    ReportEntry e9 = new ReportEntry(pathOf("a"), rule2, Result.failed("msg6"));
    Report report = new Report(Set.of(e1, e2, e3, e4, e5, e6, e7, e8, e9));

    assertThat(report.entriesSorted(Field.CODE.desc(), Field.PATH.asc())).containsExactly(
        e9, e6, e3, e5, e2, e8, e1, e7, e4
    );
  }

  @Test
  public void categoruze() {
    ReportEntry e1 = new ReportEntry(pathOf("a"), rule1, Result.ok());
    ReportEntry e2 = new ReportEntry(pathOf("b"), rule1, Result.skipped("msg1"));
    ReportEntry e3 = new ReportEntry(pathOf("c"), rule1, Result.failed("msg2"));
    ReportEntry e4 = new ReportEntry(pathOf("d"), rule1, Result.ok());
    ReportEntry e5 = new ReportEntry(pathOf("a"), rule1, Result.skipped("msg3"));
    ReportEntry e6 = new ReportEntry(pathOf("b"), rule2, Result.failed("msg4"));
    ReportEntry e7 = new ReportEntry(pathOf("c"), rule2, Result.ok());
    ReportEntry e8 = new ReportEntry(pathOf("d"), rule2, Result.skipped("msg5"));
    ReportEntry e9 = new ReportEntry(pathOf("a"), rule2, Result.failed("msg6"));
    Report report = new Report(Set.of(e1, e2, e3, e4, e5, e6, e7, e8, e9));

    Map<ResultCode, Report> reports = report.categorize(e -> e.result().code());
    assertThat(reports).isEqualTo(Map.of(
        ResultCode.OK, new Report(Set.of(e1, e4, e7)),
        ResultCode.SKIPPED, new Report(Set.of(e2, e5, e8)),
        ResultCode.FAILED, new Report(Set.of(e3, e6, e9))
    ));
  }

  private Path pathOf(String path) {
    return new CollectionPathResolver().parse(path);
  }
}
