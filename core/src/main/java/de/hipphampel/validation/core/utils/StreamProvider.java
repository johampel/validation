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

import java.util.Collection;
import java.util.stream.Stream;

/**
 * A object that provides a {@link Stream}.
 * <p>
 * The intention is that {@link #getStream()} always returns a new, unconsumed {@code Stream}
 *
 * @param <T>
 */
@FunctionalInterface
public interface StreamProvider<T> {

  /**
   * Gets a new, unconsumed {@link Stream}
   *
   * @return The {@code Stream}
   */
  Stream<T> getStream();

  /**
   * Creates a {@link StreamProvider} for a collection.
   *
   * @param collection The {@code Collection}
   * @param <S>        The element type
   * @return The provider
   */
  static <S> StreamProvider<S> of(Collection<S> collection) {
    return collection::stream;
  }

}
