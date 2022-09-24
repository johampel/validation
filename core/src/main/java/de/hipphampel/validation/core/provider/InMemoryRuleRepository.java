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

import de.hipphampel.validation.core.event.EventListener;
import de.hipphampel.validation.core.event.NoopSubscribableEventPublisher;
import de.hipphampel.validation.core.event.SubscribableEventPublisher;
import de.hipphampel.validation.core.event.Subscription;
import de.hipphampel.validation.core.event.payloads.RulesChangedPayload;
import de.hipphampel.validation.core.exception.RuleNotFoundException;
import de.hipphampel.validation.core.rule.Rule;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple, {@link RuleRepository} backed by an in memory map.
 * <p>
 * Allows to add and remove {@link Rule Rules} during runtime.
 */
public class InMemoryRuleRepository implements RuleRepository {

  private final SubscribableEventPublisher subscribableEventPublisher;
  private final Map<String, Rule<?>> rules;

  /**
   * Constructs an empty instance.
   * <p>
   * {@linkplain #subscribe(EventListener) subscribing} does not inform the listener about changes
   */
  public InMemoryRuleRepository() {
    this(NoopSubscribableEventPublisher.INSTANCE);
  }

  /**
   * Constructs an empty instance.
   * <p>
   * It uses the {@code subscribableEventPublisher} to inform
   * {@linkplain #subscribe(EventListener) subscribed} listeners about changes.
   *
   * @param subscribableEventPublisher {@link SubscribableEventPublisher} to use.
   */
  public InMemoryRuleRepository(SubscribableEventPublisher subscribableEventPublisher) {
    this.subscribableEventPublisher = Objects.requireNonNull(subscribableEventPublisher);
    this.rules = new ConcurrentHashMap<>();
  }

  /**
   * Constructs an instance initially knowing the given {@code rules}.
   * <p>
   * It uses the {@code subscribableEventPublisher} to inform
   * {@linkplain #subscribe(EventListener) subscribed} listeners about changes.
   *
   * @param subscribableEventPublisher {@link SubscribableEventPublisher} to use.
   * @param rules                      The {@link Rule Rules} to add initially
   */
  public InMemoryRuleRepository(SubscribableEventPublisher subscribableEventPublisher,
      Rule<?>... rules) {
    this(subscribableEventPublisher);
    addRulesInternal(Arrays.asList(rules));
  }

  /**
   * Constructs an instance initially knowing the given {@code rules}.
   * <p>
   * {@linkplain #subscribe(EventListener) subscribing} does not inform the listener about changes
   *
   * @param rules The {@link Rule Rules} to add initially
   */
  public InMemoryRuleRepository(Rule<?>... rules) {
    this();
    addRulesInternal(Arrays.asList(rules));
  }

  /**
   * Constructs an instance initially knowning the given {@code rules}.
   * <p>
   * It uses the {@code subscribableEventPublisher} to inform
   * {@linkplain #subscribe(EventListener) subscribed} listeners about changes.
   *
   * @param subscribableEventPublisher {@link SubscribableEventPublisher} to use.
   * @param rules                      The {@link Rule Rules} to add initially
   */
  public InMemoryRuleRepository(SubscribableEventPublisher subscribableEventPublisher,
      Collection<? extends Rule<?>> rules) {
    this(subscribableEventPublisher);
    addRulesInternal(rules);
  }

  /**
   * Constructs an instance initially knowning the given {@code rules}.
   *
   * @param rules The {@link Rule Rules} to add initially
   */
  public InMemoryRuleRepository(Collection<? extends Rule<?>> rules) {
    this();
    addRulesInternal(rules);
  }

  /**
   * Adds the given {@code rules}.
   *
   * @param rules The {@link Rule Rules} to add
   * @return This instance
   */
  public InMemoryRuleRepository addRules(Collection<? extends Rule<?>> rules) {
    addRulesInternal(rules);
    subscribableEventPublisher.publish(this, new RulesChangedPayload());
    return this;
  }

  /**
   * Adds the given {@code rules}.
   *
   * @param rules The {@link Rule Rules} to add
   * @return This instance
   */
  public InMemoryRuleRepository addRules(Rule<?>... rules) {
    return addRules(Arrays.asList(rules));
  }

  private void addRulesInternal(Collection<? extends Rule<?>> rules) {
    for (Rule<?> rule : rules) {
      this.rules.put(rule.getId(), rule);
    }
  }

  /**
   * Removes the rules with the given ids.
   *
   * @param ruleIds The ids of the {@link Rule Rules} to remove
   * @return This instance
   */
  public InMemoryRuleRepository removeRules(Collection<String> ruleIds) {
    for (String ruleId : ruleIds) {
      rules.remove(ruleId);
    }
    subscribableEventPublisher.publish(this, new RulesChangedPayload());
    return this;
  }

  /**
   * Removes the rules with the given ids.
   *
   * @param ruleIds The ids of the {@link Rule Rules} to remove
   * @return This instance
   */
  public InMemoryRuleRepository removeRules(String... ruleIds) {
    return removeRules(Arrays.asList(ruleIds));
  }

  @Override
  public boolean knowsRuleId(String id) {
    return rules.containsKey(id);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Rule<T> getRule(String id) {
    Rule<?> rule = rules.get(id);
    if (rule == null) {
      throw new RuleNotFoundException(id);
    }
    return (Rule<T>) rule;
  }

  @Override
  public Set<String> getRuleIds() {
    return Collections.unmodifiableSet(rules.keySet());
  }

  @Override
  public Subscription subscribe(EventListener listener) {
    return subscribableEventPublisher.subscribe(listener);
  }
}
