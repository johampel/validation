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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * {@link RuleRepository} combining multiple subordinated {@code RuleReopsitories} into one.
 *
 * The implementation combines all the {@code RuleRepositories} into one.
 *
 * @see RuleRepository
 */
public class MultiRuleRepository implements RuleRepository {

  private final Map<String, RuleRepository> rulesMap;

  /**
   * Constructor.
   *
   * @param repositories The subordinated {@link RuleRepository RuleRepositories}
   */
  public MultiRuleRepository(Collection<? extends RuleRepository> repositories) {
    this.rulesMap = new HashMap<>();
    for (RuleRepository repository : repositories) {
      for (String ruleId : repository.getRuleIds()) {
        if (rulesMap.containsKey(ruleId)) {
          throw new IllegalArgumentException("Rule '" + ruleId + "' in more than one repository");
        }
        rulesMap.put(ruleId, repository);
      }
    }
  }

  @Override
  public <T> Rule<T> getRule(String id) {
    RuleRepository repository = rulesMap.get(id);
    if (repository == null) {
      throw new RuleNotFoundException(id);
    }
    return repository.getRule(id);
  }

  @Override
  public Set<String> getRuleIds() {
    return Collections.unmodifiableSet(rulesMap.keySet());
  }
}
