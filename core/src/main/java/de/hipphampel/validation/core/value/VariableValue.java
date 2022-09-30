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
import java.util.function.Function;

/**
 * {@link Value} implemnentation returning a variable value.
 * <p>
 * In opposite to a {@link ConstantValue}, this implementation might return each time a different
 * value when it gets evaluated.
 *
 * @param <T> The type of the value
 */
public class VariableValue<T> implements Value<T> {

  private final Function<ValidationContext, T> valueSupplier;

  /**
   * Constructor.
   *
   * @param valueSupplier The value provider
   */
  public VariableValue(Function<ValidationContext, T> valueSupplier) {
    this.valueSupplier = Objects.requireNonNull(valueSupplier);
  }

  @Override
  public T get(ValidationContext context, Object facts) {
    return valueSupplier.apply(context);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VariableValue<?> that = (VariableValue<?>) o;
    return Objects.equals(valueSupplier, that.valueSupplier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(valueSupplier);
  }


}
