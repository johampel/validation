package de.hipphampel.validation.core.path;

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

import java.util.stream.Stream;

/**
 * Allows to resolve {@link Path Paths}.
 * <p>
 * The basic idea of a {@code PathResolver} is that using a {@link Path} one may point to one or
 * more values within an object. This is best explained by an example:
 * <p>
 * Consider, the object is a map, whereas each value in the map is either itself a map (of maps or
 * simple values) or values. Then a {@code Path} could be - from functional point of view - a
 * sequence of keys, whereas the first key is used to look up the entry in the root map, the second
 * key to look up the entry in the map looked up by the first key (if present), and so forth.
 * <p>
 * The concept of a {@code Path} and {@code  PathResolved} is the generalization of this example,
 * without the restriction that the objects examined by the resolver need to be maps.
 * <p>
 * One important feature is that a {@code Path} might  be a "pattern", meaning that it effectively
 * might contain wildcards or similar things that allow to address a group of values within an
 * object.
 * <p>
 * This interface makes only the assumption, that a {@code Path} has a parsable string
 * representation, which depends on the concrete implementation of the {@code PathResolver}
 * interface.
 * <p>
 * A good example for a concrete implementation of this interface is the
 * {@link CollectionPathResolver}
 *
 * @see Path
 * @see CollectionPathResolver
 */
public interface PathResolver {

  /**
   * Returns a {@link Path} representing a self reference.
   * <p>
   * The path returned by this method must, if used in the {@link #resolve(Resolvable) resolve}
   * method, retuurn the reference object.
   *
   * @return The self {@code Path} pr {@code null} if no such {@code Path} exists.
   */
  Path selfPath();

  /**
   * Parses the given {@code str} into a {@link Path},
   * <p>
   * which {@code Path} implementation iw returned and how it behaves, totally depends on the
   * implementation.
   *
   * @param str String representation of the {@code Path}
   * @return A {@code Path}
   * @throws IllegalArgumentException If the path is not valid
   */
  Path parse(String str);

  /**
   * Returns a parsable string representation of {@code path}.
   * <p>
   * Implementations must ensure that the resulting string can be passed to {@link #parse(String)}
   * to create an equal path.
   *
   * @param path The path
   * @return The string
   * @throws IllegalArgumentException If the path cannot be stringified
   */
  String toString(Path path);

  /**
   * Resolves the given {@code path} based on the {@code ref}.
   * <p>
   * Resolving means that the resolver tries to determine the value the {@code path} points to.
   * Depending on the {@code path} and {@code ref}, the path might be resolvable or not. This is the
   * reason, why this method does not return directly a value, but just a wrapper, which is quite
   * similar to an {@code Optional}
   *
   * @param ref  The reference object. This is the object the {@code path} is applied on.
   * @param path The {@code Path} to evaluate.
   * @param <T>  Type of the resolved object
   * @return The {@link Resolved} object, which might be defined or not.
   */
  <T> Resolved<T> resolve(Object ref, Path path);

  /**
   * Resolves the given {@code resolvable}.
   * <p>
   * This calls {@link #resolve(Object, Path) resolve}.
   *
   * @param resolvable The {@link Resolvable}
   * @param <T>        Type of the resolved object
   * @return The {@link Resolved} object, which might be defined or not.
   */
  default <T> Resolved<T> resolve(Resolvable resolvable) {
    return resolve(resolvable.reference(), resolvable.path());
  }

  /**
   * Resolves the pattern using the given {@code pattern} based on {@code ref}.
   * <p>
   * The method determines all concrete {@code Paths} that match the given {@code pattern} when
   * being resolved based on {@code ref}. In case that {@code pattern} is not a
   * {@linkplain Path#isPattern() pattern}, it simply returns a stream containing only one element
   * ({@code pattern}), in case the path exists or an empty stream if not exists.
   * <p>
   * If {@code pattern} is a pattern, it generates a stream containing all the those {@code Path}
   * instances, which return {@code true} when calling {@link Path#isMatchedBy(Path) isMatchedBy}
   * with {@code pattern} as argument and which really exist for the given {@code ref} object.
   *
   * @param ref     The reference object
   * @param pattern The {@link Path}
   * @return The stream of matching {@code Paths}.
   */
  Stream<? extends Path> resolvePattern(Object ref, Path pattern);

  /**
   * Resolves the pattern using the given {@code resolvable}.
   * <p>
   * This calls {@link #resolvePattern(Object, Path) resolcePattern}
   *
   * @param resolvable The {@link Resolvable}
   * @return The stream of matching {@code Paths}.
   */
  default Stream<? extends Path> resolvePattern(Resolvable resolvable) {
    return resolvePattern(resolvable.reference(), resolvable.path());
  }

  /**
   * Checks, whether the given {@code path} exists based on {@code ref}.
   * <p>
   * If {@code path} is concrete, this method returns {@code true}, if
   * {@link #resolve(Object, Path)} resolves to an existing value. If {@code path} is a pattern, it
   * returns {@code true}, if {@link #resolvePattern(Object, Path)} returns at least one path.
   *
   * @param ref  The reference object
   * @param path The {@code path}
   * @return {@code true}, if exists
   */
  default boolean exists(Object ref, Path path) {
    if (path.isConcrete()) {
      return resolve(ref, path).isPresent();
    } else {
      return resolvePattern(ref, path).findAny().isPresent();
    }
  }

  /**
   * Checks, whether the given {@code resolvable}.
   * <p>
   * Calls {@link #exists(Object, Path) exists}.
   *
   * @param resolvable The {@link Resolvable}
   * @return {@code true}, if exists
   */
  default boolean exists(Resolvable resolvable) {
    return exists(resolvable.reference(), resolvable.path());
  }

}
