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

import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.value.Value;
import de.hipphampel.validation.core.value.Values;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Simple basic {@link RuleSelector} implementation.
 * <p>
 * This implementation selects all {@link Rule rules} of the {@link RuleRepository} that
 * <ol>
 *   <li>Have a matching fact type (so the type of the object being validated is instance of the
 *   fact type of the {@code Rule}, and</li>
 *   <li>The id of the {@code Rule} either matches the pattern of the provided rule id filter or the filter
 *   is {@code null}</li>
 * </ol>
 */
public class SimpleRuleSelector implements RuleSelector {

  private final Value<Set<String>> ruleIdFilter;

  /**
   * Constructor.
   *
   * @param ruleIdFilter Set of regular expressions. The id of a {@code Rule} must match at least
   *                     one of them.
   */
  public SimpleRuleSelector(Value<Set<String>> ruleIdFilter) {
    this.ruleIdFilter = ruleIdFilter;
  }

  /**
   * Factory method to create a new instance selecting all rules.
   *
   * @return The new {@code RuleSelector}
   */
  public static SimpleRuleSelector all() {
    return new SimpleRuleSelector(null);
  }

  /**
   * Factory method to create a new instance.
   *
   * @param ruleIdFilter Set of regular expressions. The id of a {@code Rule} must match at least
   *                     one of them.
   * @return The new {@code RuleSelector}
   */
  public static SimpleRuleSelector of(String... ruleIdFilter) {
    return of(Values.val(new HashSet<>(Arrays.asList(ruleIdFilter))));
  }

  /**
   * Factory method to create a new instance.
   *
   * @param ruleIdFilter Set of regular expressions. The id of a {@code Rule} must match at least
   *                     one of them.
   * @return The new {@code RuleSelector}
   */
  public static SimpleRuleSelector of(Set<String> ruleIdFilter) {
    return of(Values.val(ruleIdFilter));
  }

  /**
   * Factory method to create a new instance.
   *
   * @param ruleIdFilter Set of regular expressions. The id of a {@code Rule} must match at least
   *                     one of them.
   * @return The new {@code RuleSelector}
   */
  public static SimpleRuleSelector of(Value<Set<String>> ruleIdFilter) {
    return new SimpleRuleSelector(ruleIdFilter);
  }

  @Override
  public List<Rule<?>> selectRules(RuleRepository provider, ValidationContext context,
      Object facts) {
    Set<String> allowedRuleIds = ruleIdFilter == null ? null : ruleIdFilter.get(context, facts);
    return provider.getRuleIds().stream()
        .map(provider::getRule)
        .filter(rule -> selectRule(rule, allowedRuleIds, facts))
        .collect(Collectors.toList());
  }

  private boolean selectRule(Rule<?> rule, Set<String> allowedRuleIds, Object facts) {
    if (facts != null) {
      Class<?> ruleFactsType = rule.getFactsType();
      if (!ruleFactsType.isInstance(facts)) {
        return false;
      }
    }

    if (allowedRuleIds == null) {
      return true;
    }

    for (String idPattern : allowedRuleIds) {
      if (rule.getId().matches(idPattern)) {
        return true;
      }
    }

    return false;
  }
}
