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
package de.hipphampel.validation.core.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * A stacked object.
 * <p>
 * Instances of this class always wrap a {@link #getValue() value} but might have also a {@link #getParent() parent}. Using the {@code push}
 * and {@code pop} operations it is possible to emulate a stack (in opposite to the {@code Stack} class, a {@code Stacked} instance is
 * immutable, so that {@code push} and {@code pop} always return new instances).
 *
 * @param <T> The type of the value
 */
public class Stacked<T> {

  private static final Stacked<?> EMPTY = new Stacked<>(null, null);
  private final Stacked<T> parent;
  private final T value;

  /**
   * Returns the empty instance.
   * <p>
   * This always returns the same value indicating an empty stacled value.
   *
   * @param <S> Type of the value
   * @return The empty instance
   */
  @SuppressWarnings("unchecked")
  public static <S> Stacked<S> empty() {
    return (Stacked<S>) EMPTY;
  }

  /**
   * Creates a new {@link Stacked} instance with the given {@code value} and this instance as its parent
   *
   * @param value The value
   * @return The new instance
   */
  public Stacked<T> push(T value) {
    return new Stacked<>(this, value);
  }

  private Stacked(Stacked<T> parent, T value) {
    this.parent = parent;
    this.value = value;
  }

  /**
   * Gets the parent stack
   *
   * @return The parent stack
   */
  public Stacked<T> getParent() {
    return parent;
  }

  /**
   * Gets the value.
   *
   * @return The value
   */
  public T getValue() {
    return value;
  }

  /**
   * Alias for {@link #getParent() getParent},
   *
   * @return The parent
   */
  public Stacked<T> pop() {
    return parent;
  }

  /**
   * Checks, whether the instance is empty.
   * <p>
   * Note that this method returns only {@code true}, if the object is the same as {@link #empty()} returns.
   *
   * @return {@code true}, if empty
   */
  public boolean isEmpty() {
    return this == EMPTY;
  }

  /**
   * Checks, whether this stack contains any value the is matched by {@code matcher}
   *
   * @param matcher The predicate
   * @return {@code true}, if at least one value matches
   */
  public boolean exists(Predicate<T> matcher) {
    return matcher.test(value) || parent != null && parent.exists(matcher);
  }

  /**
   * Converts this instance to a {@link List}.
   * <p>
   * The last element of the list is the value of this, the last but one the value of the parent and so on.
   *
   * @return The list of values
   */
  public List<T> toList() {
    Stacked<T> current = this;
    List<T> list = new ArrayList<>();

    while (current != null && !current.isEmpty()) {
      list.add(current.value);
      current = current.parent;
    }

    Collections.reverse(list);
    return list;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Stacked<?> stacked = (Stacked<?>) o;
    return Objects.equals(parent, stacked.parent) && Objects.equals(value, stacked.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(parent, value);
  }

  @Override
  public String toString() {
    return "Stacked{" + "parent=" + parent + ", value=" + value + '}';
  }
}
