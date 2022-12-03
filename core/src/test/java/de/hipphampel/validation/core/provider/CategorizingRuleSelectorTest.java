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

import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.rule.OkRule;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.value.Values;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class CategorizingRuleSelectorTest {

  private final Rule<Object> rule1 = new OkRule<>("rule1");
  private final Rule<Object> rule2 = new OkRule<>("rule2");
  private final Rule<Object> rule3 = new OkRule<>("rule3");
  private final Rule<Object> rule4 = new OkRule<>("rule4");

  private final InMemoryRuleRepository provider = new InMemoryRuleRepository(rule1, rule2, rule3, rule4);

  @ParameterizedTest
  @CsvSource({

      " , 'rule4'",
      "a, 'rule1,rule2'",
      "b, 'rule3'",
      "c, 'rule4'",
  })
  public void selectRules(String category, String expected) {
    CategorizingRuleSelector<String> selector = RuleSelectorBuilder.withCategorizer(Values.val(category))
        .forCategory("a").use(RuleSelector.of("rule1", "rule2"))
        .forCategory("b").use(RuleSelector.of("rule3"))
        .elseUse(RuleSelector.of("rule4"))
        .build();

    String ruleIds = selector.selectRules(provider, mock(ValidationContext.class), mock(Object.class))
        .stream().map(Rule::getId).collect(Collectors.joining(","));

    assertThat(ruleIds).isEqualTo(expected);

  }
}