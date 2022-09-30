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
package de.hipphampel.validation.core.path;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Represents the outcome of a {@linkplain PathResolver#resolve(Object, Path) resolve} operation.
 * <p>
 * This class has some similarities with {@link Optional}: It is a wrapper for a value that might be
 * present or not. The main difference to an {@code Optional} is that {@code null} is a valid
 * present value.
 *
 * @param <T> Type of the value
 */
public sealed interface Resolved<T> {

  /**
   * Returns an empty (not present) instance.
   *
   * @param <T> The type of the value
   * @return {@code true}, if no value is present
   */
  @SuppressWarnings("unchecked")
  static <T> Resolved<T> empty() {
    return (Resolved<T>) EmptyResolved.INSTANCE;
  }

  /**
   * Returns a present instance for the given {@code value}.
   * <p>
   * Even if {@code value} is {@code null} a present instance is created.
   *
   * @param value The value
   * @param <T>   The type of the value
   * @return The instance
   */
  static <T> Resolved<T> of(T value) {
    return new PresentResolved<>(value);
  }

  /**
   * Returns, whether this is an existing value.
   * <p>
   * Event {@code null} is a valid value
   *
   * @return {@code true} if present
   */
  boolean isPresent();

  /**
   * Returns, whether this is an empty instance.
   *
   * @return {@code true} if empty
   */
  default boolean isEmpty() {
    return !isPresent();
  }

  /**
   * Gets the associated value.
   * <p>
   * If this is an empty instance, an exception is thrown.
   *
   * @return The value
   * @throws NoSuchElementException If the instance is empty
   */
  T get();

  /**
   * Maps the instance to an new instance by applying {@code mapper} to its value
   * <p>
   * If this is an empty instance, the result is empty as well. Otherwise a new instance with the
   * mapped value returned.
   *
   * @param mapper The mapper to apply
   * @param <U>    The mapped type
   * @return The mapped instance
   */
  default <U> Resolved<U> map(Function<? super T, ? extends U> mapper) {
    Objects.requireNonNull(mapper);
    if (!isPresent()) {
      return empty();
    } else {
      return Resolved.of(mapper.apply(get()));
    }
  }

  /**
   * Returns the value, of - if it is empty - {@code ifEmpty}.
   *
   * @param ifEmpty Value to be returned, if this is empty
   * @return This value or {@code ifEmpty}
   */
  default T orElse(T ifEmpty) {
    return isPresent() ? get() : ifEmpty;
  }

  /**
   * Returns the value, of - if it is empty - the value of {@code ifEmpty}.
   *
   * @param ifEmpty Value provider to be called, if this is empty
   * @return This value or result of {@code ifEmpty}
   */
  default T orElseGet(Supplier<T> ifEmpty) {
    return isPresent() ? get() : ifEmpty.get();
  }

  /**
   * Get the associated value or throw the exception provided by {@code provider}
   *
   * @param provider The provider for the exception
   * @param <E>      Type of the exception
   * @return The value
   * @throws E If value not found
   */
  default <E extends Exception> T orElseThrow(Supplier<E> provider) throws E {
    if (isEmpty()) {
      throw provider.get();
    }
    return get();
  }

  /**
   * Returns a {@link Stream}.
   * <p>
   * If this is an empty instance, then an empty {@code Stream} is returned; otherwise a
   * {@code Stream} provideing just the wrapped value.
   *
   * @return A  {@code Stream}
   */
  default Stream<T> stream() {
    return isEmpty() ? Stream.empty() : Stream.of(get());
  }

  final class EmptyResolved<T> implements Resolved<T> {

    static Resolved<?> INSTANCE = new EmptyResolved<>();

    private EmptyResolved() {
    }

    @Override
    public boolean isPresent() {
      return false;
    }

    @Override
    public T get() {
      throw new NoSuchElementException();
    }

    @Override
    public String toString() {
      return "EmptyResolved";
    }
  }

  final class PresentResolved<T> implements Resolved<T> {

    private final T value;

    PresentResolved(T value) {
      this.value = value;
    }

    @Override
    public boolean isPresent() {
      return true;
    }

    @Override
    public T get() {
      return value;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      PresentResolved<?> that = (PresentResolved<?>) o;
      return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(value);
    }

    @Override
    public String toString() {
      return "Resolved(" + value + ')';
    }
  }
}
