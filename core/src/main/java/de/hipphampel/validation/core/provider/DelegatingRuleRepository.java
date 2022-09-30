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

import de.hipphampel.validation.core.event.EventListener;
import de.hipphampel.validation.core.event.Subscription;
import de.hipphampel.validation.core.rule.Rule;
import java.util.Objects;
import java.util.Set;

/**
 * {@link RuleRepository} that delegates all calls to an underlying one.
 * <p>
 * This can be used to decorate existing providers with additional behaviour.
 */
public class DelegatingRuleRepository implements RuleRepository {

  private final RuleRepository delegate;

  /**
   * Constructor.
   *
   * @param delegate The {@link RuleRepository} to delegate to
   */
  public DelegatingRuleRepository(RuleRepository delegate) {
    this.delegate = Objects.requireNonNull(delegate);
  }

  /**
   * Gets the underlying {@link RuleRepository}.
   *
   * @return The {@code RuleRepository}
   */
  public RuleRepository getDelegate() {
    return delegate;
  }

  @Override
  public boolean knowsRuleId(String id) {
    return delegate.knowsRuleId(id);
  }

  @Override
  public <T> Rule<T> getRule(String id) {
    return delegate.getRule(id);
  }

  @Override
  public Set<String> getRuleIds() {
    return delegate.getRuleIds();
  }

  @Override
  public Subscription subscribe(EventListener listener) {
    return delegate.subscribe(listener);
  }
}
