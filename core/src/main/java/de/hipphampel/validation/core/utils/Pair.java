package de.hipphampel.validation.core.utils;

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

/**
 * A pair of two objects.
 *
 * The objects might be {@code null}
 * @param first  The first object
 * @param second The second object
 * @param <A>   Type of first object
 * @param <B>   Type of sedond object
 */
public record Pair<A, B>(A first, B second) {

  /**
   * Factory method.
   *
   * @param first  The first object
   * @param second The second object
   * @param <A1>   Type of first object
   * @param <B1>   Type of sedond object
   * @return The {@code Pair}
   */
  public static <A1, B1> Pair<A1, B1> of(A1 first, B1 second) {
    return new Pair<>(first, second);
  }
}
