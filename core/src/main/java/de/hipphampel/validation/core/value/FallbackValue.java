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
import java.util.Objects;

/**
 * {@link Value} implementation realizing a fallback.
 * <p>
 * If {@code value} evaluates to {@code null}, {@code fallback} is returned, otherwise the value
 *
 * @param value    The value
 * @param fallback The fallback
 * @param <T>      The type of the value
 */
public record FallbackValue<T>(Value<T> value, Value<T> fallback) implements Value<T> {

  public FallbackValue {
    Objects.requireNonNull(value);
    Objects.requireNonNull(fallback);
  }

  @Override
  public T get(ValidationContext context, Object facts) {
    T v = value.get(context, facts);
    return (T) v == null ? fallback.get(context, facts) : v;
  }

  @Override
  public String toString() {
    return "fallback(" + value + ", " + fallback + ')';
  }

}
