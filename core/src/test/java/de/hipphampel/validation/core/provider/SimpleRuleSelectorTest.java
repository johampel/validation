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
package de.hipphampel.validation.core.provider;

import static org.assertj.core.api.Assertions.assertThat;

import de.hipphampel.validation.core.example.Member;
import de.hipphampel.validation.core.example.Unit;
import de.hipphampel.validation.core.example.Worker;
import de.hipphampel.validation.core.rule.OkRule;
import de.hipphampel.validation.core.rule.Rule;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class SimpleRuleSelectorTest {

  private final Rule<Worker> worker1 = new OkRule<>("worker.1") {
  };
  private final Rule<Worker> worker2 = new OkRule<>("worker.2") {
  };
  private final Rule<Unit> unit1 = new OkRule<>("unit.1") {
  };
  private final Rule<Unit> unit2 = new OkRule<>("unit.2") {
  };
  private final Rule<Member> member1 = new OkRule<>("member.1") {
  };
  private final InMemoryRuleRepository provider = new InMemoryRuleRepository(
      worker1, worker2, unit1, unit2, member1);

  @ParameterizedTest
  @CsvSource({
      // ruleIdFilter, Expected rules
      "  ,            'member.1,worker.1,worker.2'",
      "  '',          ''",
      "  '.*1',       'member.1,worker.1'",
  })
  public void selectRules(String ruleIdFilterString, String expected) {
    Set<String> ruleIds = ruleIdFilterString == null ? null
        : new HashSet<>(Arrays.asList(ruleIdFilterString.split(",")));

    RuleSelector selector = SimpleRuleSelector.of(ruleIds);
    String actual = selector.selectRules(provider, null, new Worker(null, null, 0, null))
        .stream()
        .map(Rule::getId)
        .sorted()
        .collect(Collectors.joining(","));
    assertThat(actual).isEqualTo(expected);
  }
}
