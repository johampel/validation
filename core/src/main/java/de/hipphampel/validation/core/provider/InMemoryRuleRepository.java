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

import de.hipphampel.validation.core.exception.RuleNotFoundException;
import de.hipphampel.validation.core.rule.Rule;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple, {@link RuleRepository} backed by an in memory map.
 * <p>
 * Allows to add and remove {@link Rule Rules} during runtime.
 */
public class InMemoryRuleRepository implements RuleRepository {

  private final Map<String, Rule<?>> rules;

  /**
   * Constructs an empty instance.
   */
  public InMemoryRuleRepository() {
    this.rules = new ConcurrentHashMap<>();
  }

  /**
   * Constructs an instance initially knowning the given {@code rules}.
   *
   * @param rules The {@link Rule Rules} to add initially
   */
  public InMemoryRuleRepository(Rule<?>... rules) {
    this();
    addRules(rules);
  }

  /**
   * Constructs an instance initially knowning the given {@code rules}.
   *
   * @param rules The {@link Rule Rules} to add initially
   */
  public InMemoryRuleRepository(Collection<? extends Rule<?>> rules) {
    this();
    addRules(rules);
  }

  /**
   * Adds the given {@code rules}.
   *
   * @param rules The {@link Rule Rules} to add
   * @return This instance
   */
  public InMemoryRuleRepository addRules(Collection<? extends Rule<?>> rules) {
    for (Rule<?> rule : rules) {
      this.rules.put(rule.getId(), rule);
    }
    return this;
  }

  /**
   * Adds the given {@code rules}.
   *
   * @param rules The {@link Rule Rules} to add
   * @return This instance
   */
  public InMemoryRuleRepository addRules(Rule<?>... rules) {
    for (Rule<?> rule : rules) {
      this.rules.put(rule.getId(), rule);
    }
    return this;
  }

  /**
   * Removes the rules with the given ids.
   *
   * @param ruleIds The ids of the {@link Rule Rules} to remove
   * @return This instance
   */
  public InMemoryRuleRepository removeRules(Collection<String> ruleIds) {
    for(String ruleId: ruleIds) {
      rules.remove(ruleId);
    }
    return this;
  }

  /**
   * Removes the rules with the given ids.
   *
   * @param ruleIds The ids of the {@link Rule Rules} to remove
   * @return This instance
   */
  public InMemoryRuleRepository removeRules(String... ruleIds) {
    for(String ruleId: ruleIds) {
      rules.remove(ruleId);
    }
    return this;
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
}
