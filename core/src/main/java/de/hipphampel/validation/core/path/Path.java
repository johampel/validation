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
package de.hipphampel.validation.core.path;

/**
 * Path to a value or component of an object.
 * <p>
 * A {@code Path} can be seen as an address of a value. For example, having a file system, a full
 * qualified filename might be seen as a {@code Path}, since such a fully qualified filename allows
 * to address a file in the file system.
 * <p>
 * In terms of this interface, a {@code Path} is a quite abstract concept, since it does not make
 * any assumptions, how a {@code Path} must kook like. The basic idea is that a {@code Path} can be
 * used in conjunction with a {@link PathResolver} to construct and resolve it.
 * <p>
 * A {@code Path} might be a {@linkplain #isPattern() pattern} or a
 * {@linkplain #isConcrete() concrete}: A concrete {@code Path} is the address for exactly one value
 * (e.g. in case of a file system: the exact path to a single file system entry). A concrete
 * {@code Path} is either {@link PathResolver#resolve(Object, Path) resolvable} or not (so it exists
 * or not).
 * <p>
 * In contrast, a pattern {@code Path} represents not an exact value address, but a group of
 * concrete {@code Paths} that follow some common schema. For example, in a file system, an example
 * for a pattern might be the string {@code *.sh}, which selects a file entries having the suffix
 * {@code .sh}. This kind of {@code Path} cannot be resolved to a specific value directly, but it is
 * possible to {@linkplain PathResolver#resolvePattern(Object, Path) resolve the pattern}, so that
 * one get a stream of concrete {@code Paths} matching the pattern.
 *
 * @see PathResolver
 */
public interface Path {

  /**
   * Indicates, whether this is a pattern.
   * <p>
   * If this is a pattern, one may call {@link PathResolver#resolvePattern(Object, Path)} to obtain
   * a stream of all concrete {@code Paths} that match this pattern.
   *
   * @return {@code true}, if it is a pattern
   * @see #isConcrete()
   * @see PathResolver#resolvePattern(Object, Path)
   */
  boolean isPattern();

  /**
   * Indicates, whether this is a concrete {@code Path}.
   * <p>
   * If this is a concrete path, one may call {@link PathResolver#resolve(Object, Path)} to get the
   * value the path points to.
   *
   * @return {@code true}, if this is a concrete path
   * @see #isPattern()
   * @see PathResolver#resolve(Object, Path)
   */
  default boolean isConcrete() {
    return !isPattern();
  }

  /**
   * Concatenates this and the given {@code child} to a new {@link Path}.
   * <p>
   * It depends on the concrete implementation, how the concatenated path looks like.
   *
   * @param child The child {@code Path}
   * @return The concatenated {@code Path}
   */
  Path concat(Path child);
}
