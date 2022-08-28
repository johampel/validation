package de.hipphampel.validation.core.value;

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

import de.hipphampel.validation.core.exception.ValueEvaluationException;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.path.Path;
import de.hipphampel.validation.core.path.PathResolver;
import de.hipphampel.validation.core.path.Resolved;
import de.hipphampel.validation.core.utils.OneOfTwo;
import java.util.Optional;

/**
 * A {@link Value} implementation that accesses a value via a {@link PathResolver}.
 * <p>
 * There are several ways how to use this kind of {@code Value}. The most simple one is to pass a
 * {@link Path} that is evaluated for the object being validated. Optionally, one may specify a
 * {@code referenceObject}; if this is set the {@code Path} is evaluated for this object instead of
 * the object being validated.
 * <p>
 * Normally, the evaluation of this value creates an exception in case that the {@code Path} cannot
 * be resolved. But it is also possible, to specify a {@code defaultValue}. When given, in case of a
 * not resolvable {@code Path} the default value is returned.
 * <p>
 * The optional {@code referenceObject} and {@code defaultValue} are itself {@code Value} instances.
 * The {@code path} parameter is either a {@code Value} as well, which is then parsed into a
 * {@code Path} instance, or directyl a {@code Path}, which is a little bit faster at execution time
 * but less concise at construction time.
 *
 * @param path            The {@code Path} to evaluate
 * @param referenceObject The object the path is resolved for. If empty, the object being validated
 *                        is used
 * @param defaultValue    The default value, optional, which is returned in case the path is not
 *                        resolvable
 * @param <T>             Type of the value
 */
public record PathValue<T>(OneOfTwo<Value<String>, Path> path,
                           Optional<Value<?>> referenceObject,
                           Optional<Value<T>> defaultValue) implements Value<T> {


  @Override
  public T get(ValidationContext context, Object facts) {
    PathResolver resolver = context.getPathResolver();
    Object reference = referenceObject
        .map(ref -> (Object) ref.get(context, facts))
        .orElse(facts);
    Path pathToResolve = path.mapIfFirst(value -> valueToPath(context, facts, value));

    Resolved<T> resolved = resolver.resolve(reference, pathToResolve);
    return resolved.orElseGet(() -> defaultValue
        .map(d -> d.get(context, facts))
        .orElseThrow(() ->
            new ValueEvaluationException("Path '" + path + "' on " + reference + " resolves to nothing")));
  }

  private Path valueToPath(ValidationContext context, Object facts, Value<String> value) {
    String pathStr = value.get(context, facts);
    return context.getPathResolver().parse(pathStr);
  }

  @Override
  public String toString() {
    if (referenceObject.isPresent()) {
      return "@(" + path + ")";
    } else {
      return "@(" + referenceObject + ": " + path + ")";
    }
  }

}
