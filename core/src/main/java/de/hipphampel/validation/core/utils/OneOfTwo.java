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

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

/**
 * Wrapper for one of two objects.
 * <p>
 * This is might contain either a {@code first} or a {@code second} component. In opposite to a
 * {@link Pair} it might not contain both or none of the two components.
 *
 * @param <A> Type of the first component
 * @param <B> Type of the second component
 */
public class OneOfTwo<A, B> {

  private final Object value;
  private final boolean firstSet;

  private OneOfTwo(Object value, boolean firstSet) {
    this.value = value;
    this.firstSet = firstSet;
  }

  /**
   * Factory method to create an instance with the first component being set.
   *
   * @param first The first component
   * @param <A1>  Type of the first component
   * @param <B1>  Type of the second component
   * @return The {@link OneOfTwo} instance
   */
  public static <A1, B1> OneOfTwo<A1, B1> ofFirst(A1 first) {
    return new OneOfTwo<>(first, true);
  }

  /**
   * Factory method to create an instance with the second component being set.
   *
   * @param second The second component
   * @param <A1>   Type of the first component
   * @param <B1>   Type of the second component
   * @return The {@link OneOfTwo} instance
   */
  public static <A1, B1> OneOfTwo<A1, B1> ofSecond(B1 second) {
    return new OneOfTwo<>(second, false);
  }

  /**
   * Gets the first component.
   * <p>
   * If this contains only the second component, a {@link NoSuchElementException} is thrown.
   *
   * @return The first component
   * @throws NoSuchElementException If component does not exists
   */
  @SuppressWarnings("unchecked")
  public A getFirst() {
    if (!firstSet) {
      throw new NoSuchElementException();
    }
    return (A) value;
  }

  /**
   * Returns {@code true} if it has the first component set
   *
   * @return {@code true}, if first component is set
   */
  public boolean hasFirst() {
    return firstSet;
  }

  /**
   * If the second component is set, it returns calls the {@code mapper} on it, otherwise it returns
   * the first component.
   *
   * @param mapper The mapper to execute
   * @return Either the mapped second or the first component
   */
  public A mapIfSecond(Function<B, A> mapper) {
    return hasSecond() ? mapper.apply(getSecond()) : getFirst();
  }

  /**
   * Gets the second component.
   * <p>
   * If this contains only the first component, a {@link NoSuchElementException} is thrown.
   *
   * @return The second component
   * @throws NoSuchElementException If component does not exists
   */
  @SuppressWarnings("unchecked")
  public B getSecond() {
    if (firstSet) {
      throw new NoSuchElementException();
    }
    return (B) value;
  }

  /**
   * Returns {@code true} if it has the second component set
   *
   * @return {@code true}, if second component is set
   */
  public boolean hasSecond() {
    return !firstSet;
  }

  /**
   * If the first component is set, it returns calls the {@code mapper} on it, otherwise it returns
   * the second component.
   *
   * @param mapper The mapper to execute
   * @return Either the mapped first or the second component
   */
  public B mapIfFirst(Function<A, B> mapper) {
    return hasFirst() ? mapper.apply(getFirst()) : getSecond();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OneOfTwo<?, ?> oneOfTwo = (OneOfTwo<?, ?>) o;
    return firstSet == oneOfTwo.firstSet && Objects.equals(value, oneOfTwo.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, firstSet);
  }

  @Override
  public String toString() {
    return  String.valueOf(value);
  }
}
