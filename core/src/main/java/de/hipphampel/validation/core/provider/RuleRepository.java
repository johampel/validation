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

import de.hipphampel.validation.core.event.EventSubscriber;
import de.hipphampel.validation.core.exception.RuleNotFoundException;
import de.hipphampel.validation.core.rule.Rule;
import java.util.Set;

/**
 * Object that knows a set of {@link Rule Rules}.
 * <p>
 * Classes implementing this interface know a set of {@code Rules}. Typically, this is a kind of
 * repository providing a set of {@code Rules} generally available. In contrast to a
 * {@link RuleSelector} it returns {@code Rules} independent from the object being validated.
 *
 *
 * @see EventSubscriber
 * @see RuleSelector
 */
public interface RuleRepository extends EventSubscriber {

  /**
   * Returns, whether a {@link Rule} with the specified {@code id} is known by this instance.
   *
   * @param id The id of the {@code Rule}
   * @return {@code true}, if this knows a  {@code Rule} with that id.
   */
  default boolean knowsRuleId(String id) {
    return getRuleIds().contains(id);
  }

  /**
   * Gets the {@link Rule} with the specified {@code id}. If there is no such {@code Rule}, an
   * exception is thrown.
   *
   * @param id  The id of the {@code Rule}
   * @param <T> The type of the object being validated by the rule
   * @return The {@code Rule}
   * @throws RuleNotFoundException If the rule cannot be found
   */
  <T> Rule<T> getRule(String id);

  /**
   * Returns a list with all rule ids known by this instance.
   * <p>
   * A concrete implementaton always returns the same set of ids.
   *
   * @return Set of rule ids.
   */
  Set<String> getRuleIds();
}
