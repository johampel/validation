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
package de.hipphampel.validation.core.value;

import de.hipphampel.validation.core.execution.ValidationContext;

/**
 * {@link Value} implementation returning the object being validated
 *
 * @param <T> Type of teh value
 */
public class FactsValue<T> implements Value<T> {

  private static final Value<?> INSTANCE = new FactsValue<>();

  /**
   * The one and only instance.
   *
   * @param <S> Type of the facts
   * @return The instance
   */
  @SuppressWarnings("unchecked")
  public static <S> Value<S> instance() {
    return (Value<S>) INSTANCE;
  }

  private FactsValue() {

  }

  @Override
  @SuppressWarnings("unchecked")
  public T get(ValidationContext context, Object facts) {
    return (T) facts;
  }

  @Override
  public String toString() {
    return "facts";
  }
}
