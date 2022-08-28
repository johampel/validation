package de.hipphampel.validation.core.condition;

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

import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.value.Value;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Compares {@code left} and {@code right} whether they are less or greater.
 * <p>
 * To check for equality, use {@link EqualsCondition}
 *
 * @param left  Left side argument
 * @param right Right side argument
 * @param mode  Compare mode
 * @param <T>   Type of the values
 * @see EqualsCondition
 */
public record CompareCondition<T extends Comparable<T>>(Value<T> left, Value<T> right,
                                                        Mode mode) implements Condition {

  public CompareCondition {
    Objects.requireNonNull(left);
    Objects.requireNonNull(right);
    Objects.requireNonNull(mode);
  }

  enum Mode {
    LT("<", i -> i < 0),
    LE("<=", i -> i <= 0),
    GE(">=", i -> i >= 0),
    GT(">", i -> i > 0);

    final String symbol;
    final Predicate<Integer> predicate;

    Mode(String symbol, Predicate<Integer> predicate) {
      this.symbol = symbol;
      this.predicate = predicate;
    }
  }

  @Override
  public boolean evaluate(ValidationContext context, Object facts) {
    T l = left.get(context, facts);
    T r = right.get(context, facts);
    return l != null && r != null && mode.predicate.test(l.compareTo(r));
  }

  @Override
  public String toString() {
    return "(" + left + " " + mode.symbol + " " + right + ")";
  }

}
