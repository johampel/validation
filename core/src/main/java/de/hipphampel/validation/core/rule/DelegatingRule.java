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
package de.hipphampel.validation.core.rule;

import de.hipphampel.validation.core.condition.Condition;
import de.hipphampel.validation.core.execution.ValidationContext;
import java.util.List;
import java.util.Map;

/**
 * {@link Rule} implementation that delegates all its work to a wrapped instance of {@code Rule}.
 * <p>
 * The basic idea of this class is to extend it in order to provide adaptions/extension for existing
 * {@code Rules}
 *
 * @param <T> Type of the objects being validated
 */
public class DelegatingRule<T> implements Rule<T> {

  private final Rule<T> delegate;

  /**
   * Constructor.
   *
   * @param delegate The {@code Rule} to delegate to
   */
  public DelegatingRule(Rule<T> delegate) {
    this.delegate = delegate;
  }

  /**
   * Gets the {@link Rule} this instance delegates to
   *
   * @return The delegate {@code Rule}
   */
  public Rule<T> getDelegate() {
    return delegate;
  }

  @Override
  public Class<? super T> getFactsType() {
    return delegate.getFactsType();
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public Map<String, Object> getMetadata() {
    return delegate.getMetadata();
  }

  @Override
  public List<Condition> getPreconditions() {
    return delegate.getPreconditions();
  }

  @Override
  public Result validate(ValidationContext context, T facts) {
    return delegate.validate(context, facts);
  }
}
