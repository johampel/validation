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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A collection of utility methods related to {@link Collection Collections} and
 * {@link Stream Streams}.
 */
public class CollectionUtils {

  /**
   * Returns a {@link Stream} that returns the possible {@link Pair Pairs} that can be constructed
   * based on {@code list}.
   * <p>
   * This method returns not all permutations, meaning that if a pair {@code [a,b]} is already
   * returned, no {@code [b,a]} is returned
   * <p>
   * So if {@code list} is {@code [1,2,3,4]}, the method returns a {@code Stream} have the following
   * lists as elements: {@code [1,2]}, {@code [1,3]}, {@code [1,4]}, {@code [2,3]}, {@code [2,4]},
   * and {@code [3,4]}.
   *
   * @param list The list
   * @param <T> The element type
   * @return The {@code Stream} or {@code Pairs}
   */
  public static <T> Stream<Pair<T, T>> streamOfPossiblePairs(List<T> list) {
    return streamOf(
        new TakeNIterator<>(list, 2, array -> new Pair<>(array[0], array[1]))
    );
  }

  /**
   * Creates a {@link Stream} that returns the possible tuples of size {@code n} that can
   * constructed based on {@code list}.
   * <p>
   * This method returns not all permutations, meaning that if a tuple {@code [a,b]} is already
   * returned, no further tuple with permuatated elements are returned.
   * <p>
   * So if {@code list} is {@code [1,2,3,4,5]} and {@code n} is {@code 3}, the method returns a
   * {@code Stream} have the following lists as elements: {@code [1,2,3]}, {@code [1,2,4]},
   * {@code [1,2,5]}, {@code [2,3,4]}, {@code [2,3,5]}, and {@code [3,4,5]}.
   *
   * @param list The list
   * @param n    Tuple size
   * @param <T>  Element type
   * @return {@code Stream} or tuples
   */
  public static <T> Stream<List<T>> streamOfPossibleTuples(List<T> list, int n) {
    return streamOf(
        new TakeNIterator<>(list, n, List::of)
    );
  }

  /**
   * Creates a {@link Stream} for the given {@code iterator}
   *
   * @param iterator The {@link Iterator}
   * @param <T>      Element type
   * @return The {@code Stream}
   */
  public static <T> Stream<T> streamOf(Iterator<T> iterator) {
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(iterator, 0),
        false);
  }

  private static class TakeNIterator<T, V> implements Iterator<V> {

    private final Function<T[], V> converter;
    private final List<T> list;
    private final int size;
    private final int[] is;
    private final T[] values;


    @SuppressWarnings("unchecked")
    TakeNIterator(List<T> list, int n, Function<T[], V> converter) {
      this.list = Objects.requireNonNull(list);
      this.converter = converter;
      this.size = list.size();
      this.is = new int[Math.max(n, 0)];
      this.values = (T[]) new Object[this.is.length];
      if (is.length > 0) {
        Arrays.fill(this.is, size - 1);
        this.is[0] = -1;
      }
    }

    @Override
    public boolean hasNext() {
      for (int i = 0; i < is.length; i++) {
        if (is[i] < size - is.length + i) {
          return true;
        }
      }
      return false;
    }

    @Override
    public V next() {
      if (!increment(this.is.length - 1)) {
        throw new NoSuchElementException();
      }
      for (int i = 0; i < this.is.length; i++) {
        this.values[i] = this.list.get(is[i]);
      }
      return converter.apply(this.values);
    }

    private boolean increment(int index) {
      if (index < 0) {
        return false;
      } else if (this.is[index] < size - is.length + index) {
        this.is[index]++;
        return true;
      } else if (increment(index - 1)) {
        this.is[index] = this.is[index - 1] + 1;
        return true;
      } else {
        return false;
      }
    }
  }

}
