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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.hipphampel.validation.core.condition.Conditions;
import de.hipphampel.validation.core.event.DefaultSubscribableEventPublisher;
import de.hipphampel.validation.core.event.EventListener;
import de.hipphampel.validation.core.event.SubscribableEventPublisher;
import de.hipphampel.validation.core.exception.RuleNotFoundException;
import de.hipphampel.validation.core.rule.ConditionRule;
import de.hipphampel.validation.core.rule.Rule;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AggregatingRuleRepositoryTest {

  private SubscribableEventPublisher subscribableEventPublisher;

  @BeforeEach
  public void beforeEach() {
    this.subscribableEventPublisher = new DefaultSubscribableEventPublisher();
  }

  @Test
  public void getRuleIds() {
    InMemoryRuleRepository rep1 = newRepository("one", "two", "three");
    InMemoryRuleRepository rep2 = newRepository("three", "four", "five");
    AggregatingRuleRepository underTest = new AggregatingRuleRepository(subscribableEventPublisher);

    assertThat(underTest.getRuleIds()).containsExactlyInAnyOrder();

    underTest.addRepositories(rep1);
    assertThat(underTest.getRuleIds()).containsExactlyInAnyOrder("one", "two", "three");
    underTest.addRepositories(rep2);
    assertThat(underTest.getRuleIds()).containsExactlyInAnyOrder("one", "two", "three", "four",
        "five");

    rep1.addRules(newRule("six"));
    assertThat(underTest.getRuleIds()).containsExactlyInAnyOrder("one", "two", "three", "four",
        "five", "six");
  }

  @Test
  public void getRule() {
    InMemoryRuleRepository rep1 = newRepository("one", "two", "three");
    InMemoryRuleRepository rep2 = newRepository("three", "four", "five");
    AggregatingRuleRepository underTest = new AggregatingRuleRepository(subscribableEventPublisher,
        rep1, rep2);

    assertThat(underTest.getRule("one")).isSameAs(rep1.getRule("one"));
    assertThat(underTest.getRule("five")).isSameAs(rep2.getRule("five"));
    assertThatThrownBy(() -> underTest.getRule("six"))
        .isInstanceOf(RuleNotFoundException.class);
  }

  @Test
  public void addRepository() {
    InMemoryRuleRepository rep1 = newRepository("one", "two", "three");
    InMemoryRuleRepository rep2 = newRepository("three", "four", "five");
    EventListener listener = mock(EventListener.class);
    subscribableEventPublisher.subscribe(listener);
    AggregatingRuleRepository underTest = new AggregatingRuleRepository(subscribableEventPublisher,
        rep1);

    assertThat(underTest.getRuleIds()).containsExactlyInAnyOrder("one", "two", "three");
    verify(listener, times(0)).accept(any());

    underTest.addRepositories(rep2);
    assertThat(underTest.getRuleIds()).containsExactlyInAnyOrder("one", "two", "three", "four",
        "five");
    verify(listener, times(1)).accept(any());

  }

  @Test
  public void removeRepository() {
    InMemoryRuleRepository rep1 = newRepository("one", "two", "three");
    InMemoryRuleRepository rep2 = newRepository("four", "five");
    EventListener listener = mock(EventListener.class);
    subscribableEventPublisher.subscribe(listener);
    AggregatingRuleRepository underTest = new AggregatingRuleRepository(subscribableEventPublisher,
        rep1, rep2);

    assertThat(underTest.getRuleIds()).containsExactlyInAnyOrder("one", "two", "three", "four",
        "five");
    verify(listener, times(0)).accept(any());

    underTest.removeRepositories(rep2);
    assertThat(underTest.getRuleIds()).containsExactlyInAnyOrder("one", "two", "three");
    verify(listener, times(1)).accept(any());

  }

  @Test
  public void changeRepository() {
    InMemoryRuleRepository rep1 = newRepository("one", "two", "three");
    EventListener listener = mock(EventListener.class);
    subscribableEventPublisher.subscribe(listener);
    AggregatingRuleRepository underTest = new AggregatingRuleRepository(subscribableEventPublisher,
        rep1);

    assertThat(underTest.getRuleIds()).containsExactlyInAnyOrder("one", "two", "three");
    verify(listener, times(0)).accept(any());

    rep1.removeRules("three");
    assertThat(underTest.getRuleIds()).containsExactlyInAnyOrder("one", "two");
    verify(listener, times(2)).accept(any());
  }

  private InMemoryRuleRepository newRepository(String... ids) {
    InMemoryRuleRepository repository = new InMemoryRuleRepository(subscribableEventPublisher);
    for (String id : ids) {
      repository.addRules(newRule(id));
    }
    return repository;
  }

  private Rule<Object> newRule(String id) {
    return new ConditionRule<>(id, null, Map.of(), List.of(), Conditions.alwaysFalse(), null);
  }

}
