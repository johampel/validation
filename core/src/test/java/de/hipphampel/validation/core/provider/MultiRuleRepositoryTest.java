package de.hipphampel.validation.core.provider;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.hipphampel.validation.core.condition.Conditions;
import de.hipphampel.validation.core.exception.RuleNotFoundException;
import de.hipphampel.validation.core.rule.RuleBuilder;
import java.util.List;
import org.junit.jupiter.api.Test;

public class MultiRuleRepositoryTest {

  @Test
  public void ctor_failsForDuplicateRules() {
    RuleRepository repo1 = testRepository("a");
    RuleRepository repo2 = testRepository("a");

    assertThatThrownBy(() -> new MultiRuleRepository(List.of(repo1, repo2)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void getRuleIds() {
    RuleRepository repo1 = testRepository("a");
    RuleRepository repo2 = testRepository("b");
    RuleRepository repository = new MultiRuleRepository(List.of(
        repo1,
        repo2
    ));

    assertThat(repository.getRuleIds()).containsExactlyInAnyOrder(
        "a1", "b1", "a2", "b2"
    );
  }

  @Test
  public void getRule_ok() {
    RuleRepository repo1 = testRepository("a");
    RuleRepository repo2 = testRepository("b");
    RuleRepository repository = new MultiRuleRepository(List.of(
        repo1,
        repo2
    ));

    assertThat(repository.getRule("a1")).isSameAs(repo1.getRule("a1"));
  }

  @Test
  public void getRule_fail() {
    RuleRepository repo1 = testRepository("a");
    RuleRepository repo2 = testRepository("b");
    RuleRepository repository = new MultiRuleRepository(List.of(
        repo1,
        repo2
    ));

    assertThatThrownBy(() -> repository.getRule("c1"))
        .isInstanceOf(RuleNotFoundException.class);
  }

  private RuleRepository testRepository(String prefix) {
    return new InMemoryRuleRepository(
        RuleBuilder.conditionRule(prefix + 1, String.class)
            .validateWith(Conditions.alwaysTrue())
            .build(),
        RuleBuilder.conditionRule(prefix + 2, String.class)
            .validateWith(Conditions.alwaysTrue())
            .build()
    );
  }
}
