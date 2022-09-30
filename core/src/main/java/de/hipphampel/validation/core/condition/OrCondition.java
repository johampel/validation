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
import de.hipphampel.validation.core.utils.StreamProvider;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Logical {@code OR} condition.
 *
 * Becomes {@code true}, if at least one of {@code conditions} is {@code true}.
 *
 * @param conditions {@link Condition Conditions} to evaluate
 */
public record OrCondition(StreamProvider<Condition> conditions) implements Condition {

  public OrCondition {
    Objects.requireNonNull(conditions);
  }

  @Override
  public boolean evaluate(ValidationContext context, Object facts) {
    return conditions.getStream().anyMatch(c -> c.evaluate(context, facts));
  }

  @Override
  public String toString() {
    return conditions.getStream().map(Condition::toString)
        .collect(Collectors.joining(", ", "or(", ")"));
  }
}
