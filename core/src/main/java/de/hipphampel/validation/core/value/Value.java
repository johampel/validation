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
import java.util.function.Function;

/**
 * Strategy to obtain a value.
 * <p>
 * This interface is the abstraction to obtain a value during validation execution. In the most
 * simple case, a {@code Value} is just a wrapper for a constant, but it might also encapsulate more
 * complex strategies like service calls.
 *
 * @param <T> Type of the value
 * @see ConstantValue
 * @see PathValue
 */
@FunctionalInterface
public interface Value<T> {

  /**
   * Gets the associated value.
   *
   * @param context The {@link ValidationContext}
   * @param facts   The object being validated
   * @return The value
   */
  T get(ValidationContext context, Object facts);

  /**
   * Maps the value using {@code mapper}
   *
   * @param mapper The mapper
   * @param <S>    Type of mappe value
   * @return The mapped value
   */
  default <S> Value<S> map(Function<T, S> mapper) {
    return (context, facts) -> mapper.apply(Value.this.get(context, facts));
  }
}
