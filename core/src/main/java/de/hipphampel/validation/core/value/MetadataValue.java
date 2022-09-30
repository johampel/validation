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

import de.hipphampel.validation.core.exception.ValueEvaluationException;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.rule.Rule;
import java.util.Map;
import java.util.Objects;

/**
 * A {@link Value} implementation that accesses the {@linkplain Rule#getMetadata() metadata} of the
 * current {@link Rule}.
 *
 * This implementation returns the value of the metadata map of the the current {@code Rule} that
 * is bound to the given {@code key}. If no {@code Rule} is active, an exception is thrown. If no
 * such key exists, {@code null} is returned.
 *
 * @param key The {@code Value} evaluating to the key to look up
 * @param <T> Type of the value
 */
public record MetadataValue<T>(Value<String> key) implements Value<T> {

  public MetadataValue {
    Objects.requireNonNull(key);
  }

  @Override
  @SuppressWarnings("unchecked")
  public T get(ValidationContext context, Object facts) {
    Rule<?> rule = context.getCurrentRule();
    if (rule == null) {
      throw new ValueEvaluationException("No active rule");
    }
    Map<String, Object> metadata = rule.getMetadata();
    String k = key.get(context, facts);
    return (T) metadata.get(k);
  }

  @Override
  public String toString() {
    return "metadata(" + key + ')';
  }
}
