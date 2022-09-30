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
package de.hipphampel.validation.core.rule;

import de.hipphampel.validation.core.condition.Condition;
import de.hipphampel.validation.core.execution.ValidationContext;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * {@link Rule} implementation composed of {@link BiFunction BiFunctions}.
 * <p>
 * In this implementation, the {@link #validate(ValidationContext, Object) validate} method simply
 * invokes a {@code BiFunction} that is passed at construction time
 *
 * @param <T> Type of the object being validated
 */
public class FunctionRule<T> extends AbstractRule<T> {

  private final BiFunction<ValidationContext, T, Result> function;

  /**
   * Constructor.
   *
   * @param id            The unique id of the rule
   * @param factsType     The {@link Class} for the facts type
   * @param metadata      Metadata of the rule
   * @param preconditions List of precondition
   * @param function      The {@link BiFunction} to evaluate if {@code validate} is called
   */
  public FunctionRule(String id, Class<? super T> factsType, Map<String, Object> metadata,
      List<Condition> preconditions, BiFunction<ValidationContext, T, Result> function) {
    super(id, factsType, metadata, preconditions);
    this.function = Objects.requireNonNull(function);
  }

  @Override
  public Result validate(ValidationContext context, T facts) {
    return function.apply(context, facts);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    FunctionRule<?> that = (FunctionRule<?>) o;
    return Objects.equals(function, that.function);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), function);
  }
}
