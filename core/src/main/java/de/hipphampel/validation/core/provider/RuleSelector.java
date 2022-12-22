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

import de.hipphampel.validation.core.execution.RuleExecutor;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.rule.Rule;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Selects {@link Rule Rules} for validating a specific object-
 * <p>
 * Instances of this class are used in conjunction with the {@link RuleExecutor} to determine those
 * {@code Rules} that are selected to validate a given object.
 *
 * @see Rule
 * @see RuleExecutor
 */
public interface RuleSelector {

  /**
   * Returns those {@link Rule Rules} of the given {@code provider} that should be used to validate
   * {@code facts}.
   * <p>
   * A typical implementation will select all {@code Rules} of the {@code provider} that match some
   * criteria. The returned {@code Rules} are those that the {@link RuleExecutor} will use to
   * validate {@code facts}
   *
   * @param provider The {@link RuleRepository}
   * @param context  The {@link ValidationContext}
   * @param facts    The object being validated
   * @return The list of {@code Rules}
   * @see Rule
   * @see RuleExecutor
   * @see RuleRepository
   */
  List<? extends Rule<?>> selectRules(RuleRepository provider, ValidationContext context, Object facts);

  /**
   * Utility method to create a {@link RuleSelector} selecting the {@link Rule Rules} having the
   * given ids.
   *
   * @param ruleIds Ids of the {@code Rules} to select. This is a set of regular expressions.
   * @return The selector
   */
  static RuleSelector of(String... ruleIds) {
    return of(Arrays.asList(ruleIds));
  }

  /**
   * Utility method to create a {@link RuleSelector} selecting the {@link Rule Rules} having the
   * given ids.
   *
   * @param ruleIds Ids of the {@code Rules} to select. This is a set of regular expressions.
   * @return The selector
   */
  static RuleSelector of(Collection<String> ruleIds) {
    return SimpleRuleSelector.of(new HashSet<>(ruleIds));
  }
}
