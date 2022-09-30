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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.hipphampel.validation.core.condition.Conditions;
import de.hipphampel.validation.core.event.SubscribableEventPublisher;
import de.hipphampel.validation.core.event.payloads.RulesChangedPayload;
import de.hipphampel.validation.core.exception.RuleNotFoundException;
import de.hipphampel.validation.core.rule.ConditionRule;
import de.hipphampel.validation.core.rule.Rule;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class InMemoryRuleRepositoryTest {

  @Test
  public void knowsRuleId() {
    InMemoryRuleRepository provider = new InMemoryRuleRepository();

    assertThat(provider.knowsRuleId("TheRuleId")).isFalse();

    provider.addRules(newRule("TheRuleId"));
    assertThat(provider.knowsRuleId("TheRuleId")).isTrue();

    provider.removeRules("TheRuleId");
    assertThat(provider.knowsRuleId("TheRuleId")).isFalse();
  }

  @Test
  public void getRule() {
    InMemoryRuleRepository provider = new InMemoryRuleRepository();

    assertThatThrownBy(() -> provider.getRule("TheRuleId"))
        .isInstanceOf(RuleNotFoundException.class);

    Rule<?> rule = newRule("TheRuleId");
    provider.addRules(rule);
    assertThat(provider.getRule("TheRuleId")).isSameAs(rule);

    provider.removeRules("TheRuleId");
    assertThatThrownBy(() -> provider.getRule("TheRuleId"))
        .isInstanceOf(RuleNotFoundException.class);
  }

  @Test
  public void addRules() {
    Rule<?> rule1 = newRule("TheRuleId1");
    Rule<?> rule2 = newRule("TheRuleId2");
    SubscribableEventPublisher subscribableEventPublisher = mock(SubscribableEventPublisher.class);
    InMemoryRuleRepository provider = new InMemoryRuleRepository(subscribableEventPublisher);

    assertThatThrownBy(() -> provider.getRule("TheRuleId1"))
        .isInstanceOf(RuleNotFoundException.class);
    assertThatThrownBy(() -> provider.getRule("TheRuleId2"))
        .isInstanceOf(RuleNotFoundException.class);

    provider.addRules(rule1, rule2);

    assertThat(provider.getRule("TheRuleId1")).isSameAs(rule1);
    assertThat(provider.getRule("TheRuleId2")).isSameAs(rule2);
    verify(subscribableEventPublisher, times(1))
        .publish(eq(provider), eq(new RulesChangedPayload()));
  }

  @Test
  public void removeRules() {
    Rule<?> rule1 = newRule("TheRuleId1");
    Rule<?> rule2 = newRule("TheRuleId2");
    SubscribableEventPublisher subscribableEventPublisher = mock(SubscribableEventPublisher.class);
    InMemoryRuleRepository provider = new InMemoryRuleRepository(subscribableEventPublisher, rule1,
        rule2);

    assertThat(provider.getRule("TheRuleId1")).isSameAs(rule1);
    assertThat(provider.getRule("TheRuleId2")).isSameAs(rule2);

    provider.removeRules(rule1.getId(), rule2.getId());

    assertThatThrownBy(() -> provider.getRule("TheRuleId1"))
        .isInstanceOf(RuleNotFoundException.class);
    assertThatThrownBy(() -> provider.getRule("TheRuleId2"))
        .isInstanceOf(RuleNotFoundException.class);
    verify(subscribableEventPublisher, times(1))
        .publish(eq(provider), eq(new RulesChangedPayload()));
  }

  @Test
  public void getRuleIds() {
    InMemoryRuleRepository provider = new InMemoryRuleRepository();

    assertThat(provider.getRuleIds()).containsExactlyInAnyOrder();

    provider.addRules(newRule("a"), newRule("b"));
    assertThat(provider.getRuleIds()).containsExactlyInAnyOrder("a", "b");

    provider.addRules(newRule("c"), newRule("d"));
    assertThat(provider.getRuleIds()).containsExactlyInAnyOrder("a", "b", "c", "d");
  }

  private Rule<Object> newRule(String id) {
    return new ConditionRule<>(id, null, Map.of(), List.of(), Conditions.alwaysFalse(), null);
  }
}
