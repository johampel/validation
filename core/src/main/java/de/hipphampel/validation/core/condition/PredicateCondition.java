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
package de.hipphampel.validation.core.condition;

import de.hipphampel.validation.core.execution.ValidationContext;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * {@link Condition} implementation that wraps around a {@link Predicate}.
 * <p>
 * The predicate is evaluated on the object being validated.
 *
 * @param predicate The predicate
 */
public record PredicateCondition(Predicate<?> predicate) implements Condition {

  /**
   * Constructor
   *
   * @param predicate The predicate
   */
  public PredicateCondition {
    Objects.requireNonNull(predicate);
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean evaluate(ValidationContext context, Object facts) {
    return ((Predicate<Object>) predicate).test(facts);
  }
}
