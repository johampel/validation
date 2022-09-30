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
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link Value} implementation providing a {@link Set} of the values provided by {@code Values}
 * <p>
 * When this instance is {@linkplain  #get(ValidationContext, Object) evaluated}, it builds a
 * {@code Set} based on the the evaluation result of the {@code entries}
 *
 * @param entries The {@code Values} providing the values for the set entries.
 * @param <T>     Type of the set values
 */
public record SetValue<T>(Collection<Value<T>> entries) implements Value<Set<T>> {

  @Override
  public Set<T> get(ValidationContext context, Object facts) {
    if (entries == null) {
      return null;
    }
    return entries.stream()
        .map(v -> v.get(context, facts))
        .collect(Collectors.toSet());
  }

  @Override
  public String toString() {
    return "set(" + entries + ')';
  }
}
