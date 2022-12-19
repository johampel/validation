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
package de.hipphampel.validation.spring.rule;

import static org.assertj.core.api.Assertions.assertThat;

import de.hipphampel.validation.core.Validator;
import de.hipphampel.validation.core.annotations.BindPath;
import de.hipphampel.validation.core.annotations.RuleDef;
import de.hipphampel.validation.core.provider.RuleSelector;
import de.hipphampel.validation.core.report.Report;
import de.hipphampel.validation.core.report.ReportEntry;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.spring.annotation.RuleContainer;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(classes = SpringReflectionRuleTest.Context.class)
public class SpringReflectionRuleTest {

  @Autowired
  private Validator validator;

  @Test
  public void validationOk() {
    Map<String, Object> facts = Map.of("left", "2", "right", 1.0);
    Report report = validator.validate(facts, RuleSelector.of("isGreaterThan"));

    ReportEntry entry = getReportEntryFor(report, "isGreaterThan");
    assertThat(entry.result()).isEqualTo(Result.ok());
    assertThat(entry.path().toString()).isEqualTo("");
  }

  @Test
  public void validationNormalValidationError() {
    Map<String, Object> facts = Map.of("left", "0", "right", 1.0);
    Report report = validator.validate(facts, RuleSelector.of("isGreaterThan"));

    ReportEntry entry = getReportEntryFor(report, "isGreaterThan");
    assertThat(entry.result()).isEqualTo(Result.failed("left is not greater than right"));
    assertThat(entry.path().toString()).isEqualTo("");
  }

  @Test
  public void validationConversionFailed() {
    Map<String, Object> facts = Map.of("left", "a", "right", 1.0);
    Report report = validator.validate(facts, RuleSelector.of("isGreaterThan"));

    ReportEntry entry = getReportEntryFor(report, "isGreaterThan");
    assertThat(entry.result()).isEqualTo(Result.failed("Expected a double for parameter 'arg0', but got a java.lang.String"));
    assertThat(entry.path().toString()).isEqualTo("");
  }

  private ReportEntry getReportEntryFor(Report report, String ruleId) {
    assertThat(report.filter(e -> e.rule().getId().equals(ruleId)).entriesSorted()).hasSize(1);
    return report.filter(e -> e.rule().getId().equals("isGreaterThan")).entriesSorted().get(0);
  }

  @ContextConfiguration
  @ComponentScan(basePackages = "de.hipphampel.validation.spring.config")
  @EnableAutoConfiguration
  @RuleContainer
  public static class Context {

    @RuleDef(message = "left is not greater than right")
    public static boolean isGreaterThan(@BindPath("left") double left, @BindPath("right") double right) {
      return left > right;
    }

  }

}
