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
import de.hipphampel.validation.core.path.Path;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.utils.OneOfTwo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Collection for factory methods for {@link Value Values}.
 */
public class Values {

  /**
   * Factory method to create a {@link ConstantValue} containing the object being validated.
   *
   * @param <T> The type of the value
   * @return The {@link Value} instance
   */
  @SuppressWarnings("unchecked")
  public static <T> Value<T> facts() {
    return FactsValue.instance();
  }

  /**
   * Factory method to create a {@link ConstantValue}
   *
   * @param value The value
   * @param <T>   The type of the value
   * @return The {@link Value} instance
   */
  public static <T> Value<T> val(T value) {
    return new ConstantValue<>(value);
  }

  /**
   * Factory method to create a {@link VariableValue}
   *
   * @param valueSupplier The supplier providing the value
   * @param <T>           The type of the value
   * @return The {@link Value} instance
   */
  public static <T> Value<T> var(Supplier<T> valueSupplier) {
    return var(ignore -> valueSupplier.get());
  }

  /**
   * Factory method to create a {@link VariableValue}
   *
   * @param valueSupplier The supplier providing the value
   * @param <T>           The type of the value
   * @return The {@link Value} instance
   */
  public static <T> Value<T> var(Function<ValidationContext, T> valueSupplier) {
    return new VariableValue<>(valueSupplier);
  }

  /**
   * Factory method to create a {@link MetadataValue}.
   * <p>
   * Values of this type allow to access the {@linkplain Rule#getMetadata() metadata} of the current {@link Rule}
   *
   * @param key The key for the metadata
   * @param <T> The type of the value
   * @return The {@link Value}
   */
  public static <T> Value<T> metadata(String key) {
    return metadata(val(key));
  }

  /**
   * Factory method to create a {@link MetadataValue}.
   * <p>
   * Values of this type allow to access the {@linkplain Rule#getMetadata() metadata} of the current {@link Rule}
   *
   * @param key The key for the metadata
   * @param <T> The type of the value
   * @return The {@link Value}
   */
  public static <T> Value<T> metadata(Value<String> key) {
    return new MetadataValue<>(key);
  }

  /**
   * Factory method to create a {@link PathValue} to be evaluated for the {@code referenceObject}.
   * <p>
   * The returned {@code Value} will evaluate {@code defaultValue} in case the path is not resolvable.
   *
   * @param referenceObject The {@code Value} with the reference object the path is evaulated fr.
   * @param path            The string representation of the {@link Path}
   * @param defaultValue    The default value
   * @param <T>             The type of the value
   * @return The {@code Value}
   */
  public static <T> Value<T> pathForObject(Value<?> referenceObject, String path, Value<T> defaultValue) {
    return new PathValue<>(
        OneOfTwo.ofFirst(val(path)),
        Optional.of(referenceObject),
        Optional.of(defaultValue)
    );
  }

  /**
   * Factory method to create a {@link PathValue} to be evaluated for the {@code referenceObject}.
   * <p>
   * The returned {@code Value} will evaluate {@code defaultValue} in case the path is not resolvable.
   *
   * @param referenceObject The {@code Value} with the reference object the path is evaulated fr.
   * @param path            A {@link Value} with the string representation of the {@link Path}
   * @param defaultValue    The default value
   * @param <T>             The type of the value
   * @return The {@code Value}
   */
  public static <T> Value<T> pathForObject(Value<?> referenceObject, Value<String> path, Value<T> defaultValue) {
    return new PathValue<>(
        OneOfTwo.ofFirst(path),
        Optional.of(referenceObject),
        Optional.of(defaultValue)
    );
  }

  /**
   * Factory method to create a {@link PathValue} to be evaluated for the {@code referenceObject}.
   * <p>
   * The returned {@code Value} will evaluate {@code defaultValue} in case the path is not resolvable.
   *
   * @param referenceObject The {@code Value} with the reference object the path is evaulated fr.
   * @param path            The {@link Path}
   * @param defaultValue    The default value
   * @param <T>             The type of the value
   * @return The {@code Value}
   */
  public static <T> Value<T> pathForObject(Value<?> referenceObject, Path path, Value<T> defaultValue) {
    return new PathValue<>(
        OneOfTwo.ofSecond(path),
        Optional.of(referenceObject),
        Optional.of(defaultValue)
    );
  }

  /**
   * Factory method to create a {@link PathValue} to be evaluated for the {@code referenceObject}.
   * <p>
   * The returned {@code Value} will throw an exception upon evaluation in case the path is not resolvable.
   *
   * @param referenceObject The {@code Value} with the reference object the path is evaulated for.
   * @param path            The string representation of the {@link Path}
   * @param <T>             The type of the value
   * @return The {@code Value}
   */
  public static <T> Value<T> pathForObject(Value<?> referenceObject, String path) {
    return new PathValue<>(
        OneOfTwo.ofFirst(val(path)),
        Optional.of(referenceObject),
        Optional.empty()
    );
  }

  /**
   * Factory method to create a {@link PathValue} to be evaluated for the {@code referenceObject}.
   * <p>
   * The returned {@code Value} will throw an exception upon evaluation in case the path is not resolvable.
   *
   * @param referenceObject The {@code Value} with the reference object the path is evaulated for.
   * @param path            A {@link Value} with the string representation of the {@link Path}
   * @param <T>             The type of the value
   * @return The {@code Value}
   */
  public static <T> Value<T> pathForObject(Value<?> referenceObject, Value<String> path) {
    return new PathValue<>(
        OneOfTwo.ofFirst(path),
        Optional.of(referenceObject),
        Optional.empty()
    );
  }

  /**
   * Factory method to create a {@link PathValue} to be evaluated for the {@code referenceObject}.
   * <p>
   * The returned {@code Value} will throw an exception upon evaluation in case the path is not resolvable.
   *
   * @param referenceObject The {@code Value} with the reference object the path is evaulated fr.
   * @param path            The {@link Path}
   * @param <T>             The type of the value
   * @return The {@code Value}
   */
  public static <T> Value<T> pathForObject(Value<?> referenceObject, Path path) {
    return new PathValue<>(
        OneOfTwo.ofSecond(path),
        Optional.of(referenceObject),
        Optional.empty()
    );
  }

  /**
   * Factory method to create a {@link PathValue} to be evaluated against the object being validated.
   * <p>
   * The returned {@code Value} will evaluate {@code defaultValue} in case the path is not resolvable.
   *
   * @param path         The string representation of the {@link Path}
   * @param defaultValue The default value
   * @param <T>          The type of the value
   * @return The {@code Value}
   */
  public static <T> Value<T> path(String path, Value<T> defaultValue) {
    return new PathValue<>(
        OneOfTwo.ofFirst(val(path)),
        Optional.empty(),
        Optional.of(defaultValue)
    );
  }

  /**
   * Factory method to create a {@link PathValue} to be evaluated against the object being validated.
   * <p>
   * The returned {@code Value} will evaluate {@code defaultValue} in case the path is not resolvable.
   *
   * @param path         A {@link Value} with the string representation of the {@link Path}
   * @param defaultValue The default value
   * @param <T>          The type of the value
   * @return The {@code Value}
   */
  public static <T> Value<T> path(Value<String> path, Value<T> defaultValue) {
    return new PathValue<>(
        OneOfTwo.ofFirst(path),
        Optional.empty(),
        Optional.of(defaultValue)
    );
  }

  /**
   * Factory method to create a {@link PathValue} to be evaluated against the object being validated.
   * <p>
   * The returned {@code Value} will evaluate {@code defaultValue} in case the path is not resolvable.
   *
   * @param path         The {@link Path}
   * @param defaultValue The default value
   * @param <T>          The type of the value
   * @return The {@code Value}
   */
  public static <T> Value<T> path(Path path, Value<T> defaultValue) {
    return new PathValue<>(
        OneOfTwo.ofSecond(path),
        Optional.empty(),
        Optional.of(defaultValue)
    );
  }

  /**
   * Factory method to create a {@link PathValue} to be evaluated against the object being validated.
   * <p>
   * The returned {@code Value} will throw an exception upon evaluation in case the path is not resolvable.
   *
   * @param path The string representation of the {@link Path}
   * @param <T>  The type of the value
   * @return The {@code Value}
   */
  public static <T> Value<T> path(String path) {
    return new PathValue<>(
        OneOfTwo.ofFirst(val(path)),
        Optional.empty(),
        Optional.empty()
    );
  }

  /**
   * Factory method to create a {@link PathValue} to be evaluated against the object being validated.
   * <p>
   * The returned {@code Value} will throw an exception upon evaluation in case the path is not resolvable.
   *
   * @param path A {@link Value} with the string representation of the {@link Path}
   * @param <T>  The type of the value
   * @return The {@code Value}
   */
  public static <T> Value<T> path(Value<String> path) {
    return new PathValue<>(
        OneOfTwo.ofFirst(path),
        Optional.empty(),
        Optional.empty()
    );
  }

  /**
   * Factory method to create a {@link PathValue} to be evaluated against the object being validated.
   * <p>
   * The returned {@code Value} will throw an exception upon evaluation in case the path is not resolvable.
   *
   * @param path The {@link Path}
   * @param <T>  The type of the value
   * @return The {@code Value}
   */
  public static <T> Value<T> path(Path path) {
    return new PathValue<>(
        OneOfTwo.ofSecond(path),
        Optional.empty(),
        Optional.empty()
    );
  }

  /**
   * Factory method to combine multiple {@link Value Values} to a {@link List}.
   *
   * @param list A list of {@code Values}
   * @param <T>  The list element type
   * @return A {@code Value} returning a {@code List} of the values that are provided by {@code list}
   */
  @SafeVarargs
  public static <T> Value<List<T>> list(Value<T>... list) {
    return new ListValue<>(Arrays.asList(list));
  }

  /**
   * Factory method to combine multiple {@link Value Values} to a {@link List}.
   *
   * @param list A list of {@code Values}
   * @param <T>  The list element type
   * @return A {@code Value} returning a {@code List} of the values that are provided by {@code list}
   */
  public static <T> Value<List<T>> list(Collection<Value<T>> list) {
    return new ListValue<>(list);
  }

  /**
   * Factory method to combine multiple {@link Value Values} to a {@link Set}.
   *
   * @param set A set of {@code Values}
   * @param <T> The set element type
   * @return A {@code Value} returning a {@code Set} of the values that are provided by {@code set}
   */
  @SafeVarargs
  public static <T> Value<Set<T>> set(Value<T>... set) {
    return new SetValue<>(new HashSet<>(Arrays.asList(set)));
  }

  /**
   * Factory method to combine multiple {@link Value Values} to a {@link Set}.
   *
   * @param set A set of {@code Values}
   * @param <T> The set element type
   * @return A {@code Value} returning a {@code Set} of the values that are provided by {@code set}
   */
  public static <T> Value<Set<T>> set(Collection<Value<T>> set) {
    return new SetValue<>(set);
  }

  /**
   * Factory method to access {@link ValidationContext#getParameters() context parameters}
   *
   * @param key The parameter key
   * @param <T> The type of the value
   * @return A {@code Value} returning the context parameter
   */
  public static <T> Value<T> context(Value<String> key) {
    return new ContextValue<>(key);
  }

  /**
   * Factory method to access {@link ValidationContext#getParameters() context parameters}
   *
   * @param key The parameter key
   * @param <T> The type of the value
   * @return A {@code Value} returning the context parameter
   */
  public static <T> Value<T> context(String key) {
    return context(val(key));
  }

  /**
   * Factory method for a fallback {@link Value}
   * <p>
   * If {@code value} is not null, it is returned, otherwise {@code fallback}
   *
   * @param value    The primary value
   * @param fallback The fallback
   * @param <T>      The type of the value
   * @return The evaluated value
   */
  public static <T> Value<T> fallback(Value<T> value, Value<T> fallback) {
    return new FallbackValue<>(value, fallback);
  }
}
