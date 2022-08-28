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

import de.hipphampel.validation.core.path.ComponentPath.Component;
import de.hipphampel.validation.core.path.ComponentPath.ComponentType;
import de.hipphampel.validation.core.utils.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;


/**
 * Abstract {@link PathResolver} that deals with {@link ComponentPath}.
 * <p>
 * It provides complete implementations for {@link #parse(String)} and {@link #toString(Path)}
 * methods and stub implementations for {@link #resolve(Object, Path)} and
 * {@link #resolvePattern(Object, Path)} methods.
 * <p>
 * Upon construction, one has to specify the following strings that influcence, how a
 * {@code ComponentPath} is parsed and its string representation is constructed:
 * <ul>
 *   <li>{@link #getSeparator() The separator} (default "/"): This is the substring that is used
 *   to separate the components in the string representation of the {@code ComponentPath}. E.g.
 *   {@code abc/def} represents a {@code ComponentPath} with the two components {@code abc} and
 *   {@code def}</li>
 *   <li>{@link #getAllInLevel() All in level} (default: "*"): This is the special component name
 *   that denotes a single level which any name. It is used in patterns and selects all possible
 *   {@code Paths} in exactly one level</li>
 *   <li>{@link #getManyLevels() Many levels} (default: "**"): This is the special component name
 *   that denotes a zero or more levels with any name. It is used in patterns and selects all
 *   possible {@code Paths} in zero or more levels</li>
 * </ul>
 * <p>
 * Concrete implementations must implement the following two methods:
 * <ol>
 *   <li>{@link #resolveLevel(Object, Component) resolveLevel}, which is called to resolve one
 *   single level of a concrete {@code Path}</li>
 *   <li>{@link #resolvePatternLevel(Object, Component) resolvePatternLevel} which is called to
 *   resolve single level of a pattern {@code Path}</li>
 * </ol>
 *
 * @see ComponentPath
 */
public abstract class AbstractComponentPathResolver implements PathResolver {

  private final String separator;
  private final String allInLevel;
  private final String manyLevels;

  /**
   * Constructor.
   *
   * @param separator  String used to separate the different components.
   * @param allInLevel String representing exactly one levels having any name
   * @param manyLevels String representing zero or more levels having any name
   */
  protected AbstractComponentPathResolver(String separator, String allInLevel, String manyLevels) {
    this.separator = separator;
    this.allInLevel = allInLevel;
    this.manyLevels = manyLevels;
  }

  /**
   * Constructor.
   * <p>
   * Creates an instance with the default settings. This is the same as calling
   * {@code CollectionPathResolver("/", "*", "**")}
   */
  protected AbstractComponentPathResolver() {
    this("/", "*", "**");
  }

  /**
   * Gets the string used to delimit the components in the string representation of the
   * {@code Paths}
   *
   * @return The separator string
   */
  public String getSeparator() {
    return separator;
  }

  /**
   * Gets the string representing exactly one level with any name.
   *
   * @return The string
   */
  public String getAllInLevel() {
    return allInLevel;
  }

  /**
   * Gets the string representing zero or more levels with any names.
   *
   * @return The string
   */
  public String getManyLevels() {
    return manyLevels;
  }

  @Override
  public Path selfPath() {
    return ComponentPath.empty();
  }

  @Override
  public Path parse(String str) {
    if (str.isEmpty()) {
      return ComponentPath.empty();
    }

    List<String> tokens = new ArrayList<>();
    int start = 0;
    int end;
    while ((end = str.indexOf(separator, start)) != -1) {
      tokens.add(str.substring(start, end));
      start = end + separator.length();
    }
    if (start <= str.length()) {
      tokens.add(str.substring(start));
    }

    return new ComponentPath(
        tokens.stream()
            .map(this::toComponent)
            .toList());
  }

  private Component toComponent(String name) {
    return new Component(componentTypeOf(name), name);
  }

  private ComponentType componentTypeOf(String name) {
    if (Objects.equals(manyLevels, name)) {
      return ComponentType.ManyLevels;
    } else if (Objects.equals(allInLevel, name)) {
      return ComponentType.AnyInLevel;
    }
    return ComponentType.NamedLevel;
  }

  @Override
  public String toString(Path path) {
    if (!(path instanceof ComponentPath cp)) {
      throw new IllegalArgumentException("Not a ComponentPath: " + path);
    }
    return cp.toString(separator, allInLevel, manyLevels);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Resolved<T> resolve(Object ref, Path path) {
    if (!(path instanceof ComponentPath cp) || path.isPattern()) {
      return Resolved.empty();
    }

    Object current = ref;
    int depth = cp.getComponents().size();
    for (int level = 0; level < depth; level++) {
      Component component = cp.getComponents().get(level);
      Resolved<Object> resolvedLevel = resolveLevel(current, component);
      if (resolvedLevel.isEmpty()) {
        return Resolved.empty();
      }
      current = resolvedLevel.get();
    }
    return (Resolved<T>) Resolved.of(current);
  }

  /**
   * Resolves one level of a concrete {@code ComponentPath}.
   * <p>
   * This is called by the {@link #resolve(Object, Path) resolve} method. It is intended to resolve
   * exactly one level of a the {@code Path}. It is guaranteed that the type of the
   * {@code component} is {@link ComponentType#NamedLevel}.
   *
   * @param ref       The object where the {@code component} is applied to
   * @param component The component of the {@code Path} to resolve
   * @return a {@link Resolved} indicating the result
   */
  protected abstract Resolved<Object> resolveLevel(Object ref, Component component);

  @Override
  public Stream<? extends Path> resolvePattern(Object ref, Path pattern) {
    if (!(pattern instanceof ComponentPath cp) || pattern.isConcrete()) {
      return exists(ref, pattern) ? Stream.of(pattern) : Stream.empty();
    }

    return patternStream(ref, cp, 0, ComponentPath.empty())
        .filter(p -> p.isMatchedBy(pattern));
  }

  private Stream<ComponentPath> patternStream(Object ref, ComponentPath pattern, int level,
      ComponentPath concreteParentPath) {
    if (level == pattern.getComponents().size()) {
      return Stream.of(concreteParentPath);
    }
    if (level > pattern.getComponents().size()) {
      return Stream.empty();
    }

    Component component = pattern.getComponents().get(level);
    return switch (component.type()) {
      case NamedLevel -> resolveLevel(ref, component).stream()
          .flatMap(value -> patternStream(
              value,
              pattern,
              level + 1,
              concreteParentPath.concat(component)));
      case AnyInLevel -> resolvePatternLevel(ref, component)
          .flatMap(p -> patternStream(
              p.second(),
              pattern,
              level + 1,
              concreteParentPath.concat(p.first())));
      case ManyLevels -> Stream.concat(
          Stream.of(concreteParentPath),
          resolvePatternLevel(ref, component)
              .flatMap(p -> patternStream(
                  p.second(),
                  pattern,
                  level,
                  concreteParentPath.concat(p.first()))));
    };
  }

  /**
   * Resolves one level of a pattern {@code ComponentPath}.
   * <p>
   * This is called by the {@link #resolvePattern(Object, Path) resolvePattern} method. It is
   * intended to resolve exactly one level of a the {@code Path}. It is guaranteed that the type of
   * the {@code component} is not {@link ComponentType#NamedLevel}.
   *
   * @param ref       The object where the {@code component} is applied to
   * @param component The component of the {@code Path} to resolve
   * @return A Stream containing pairs with concrete {@link Component Components} having the type
   * {@link ComponentType#NamedLevel} plus the corresponding resolved object.
   */
  protected abstract Stream<Pair<Component, Object>> resolvePatternLevel(Object ref,
      Component component);

}
