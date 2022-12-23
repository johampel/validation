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
import static org.mockito.Mockito.mock;

import de.hipphampel.validation.core.condition.Conditions;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.rule.RuleBuilder;
import java.util.List;
import org.junit.jupiter.api.Test;

public class PredicateBasedRuleSelectorTest {

  private final Rule<?> rule1 = RuleBuilder.conditionRule("arule1", Object.class)
      .withMetadata("foo", "bar")
      .validateWith(Conditions.alwaysTrue())
      .build();
  private final Rule<?> rule2 = RuleBuilder.conditionRule("arule2", Object.class)
      .validateWith(Conditions.alwaysTrue())
      .build();
  private final Rule<?> rule3 = RuleBuilder.conditionRule("brule1", Object.class)
      .withMetadata("foo", "bar")
      .validateWith(Conditions.alwaysTrue())
      .build();
  private final RuleRepository repository = new InMemoryRuleRepository(rule1, rule2, rule3);

  @Test
  public void onePredicate() {
    PredicateBasedRuleSelector selector = RuleSelectorBuilder.withPredicate(rule -> rule.getId().startsWith("a"))
        .build();

    assertThat((List<Rule<?>>) selector.selectRules(repository, mock(ValidationContext.class), mock(Object.class)))
        .containsExactlyInAnyOrderElementsOf(List.of(rule1, rule2));
  }

  @Test
  public void twoPredicates() {
    PredicateBasedRuleSelector selector = RuleSelectorBuilder.withPredicate(rule -> rule.getId().startsWith("a"))
        .and(rule -> rule.getMetadata().containsKey("foo"))
        .build();

    assertThat(selector.selectRules(repository, mock(ValidationContext.class), mock(Object.class)))
        .isEqualTo(List.of(rule1));
  }
}
