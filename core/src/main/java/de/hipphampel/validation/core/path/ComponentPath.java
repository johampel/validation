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

import static de.hipphampel.validation.core.path.ComponentPath.ComponentType.ManyLevels;
import static de.hipphampel.validation.core.path.ComponentPath.ComponentType.NamedLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * {@link Path} consisting of a list of components.
 * <p>
 * This {@code Path} implementation is basically just a sequence of zero or more components, it is
 * intended to resolve hierarchical structures, like nested maps and collections, JSON or XML.
 * <p>
 * Basically, when resolving a path of this kind, it first tries to resolve the reference object
 * passed to the {@link PathResolver} using the first component. On the result of this it tries to
 * resolve the second component and so forth. The special {@code ComponentPaths} having no
 * components resolves to the reference object itself. So all in all, resolving
 * {@code ComponentPaths} means to resolve a hierarchical structure, whereas each component
 * identifies on level in this hierarchical structure and the following component is used to
 * identify the values in the next lower level of the hierarchy. A concrete example could be an file
 * path, whereas each component refers to a corresponding level in the directory structure.
 * <p>
 * Each component is one of the following three:
 * <ol>
 *   <li>A  single level with a concrete name. Such a concrete name refer to exactly one attribute/
 *   property of the reference object. In case of a file system, this corresponds to a concrete file
 *   or directory name</li>
 *   <li>A single level with any name. This is used for path patterns and matches any name for
 *   just one level. In case of a file system, this corresponds to any entry for a given directory
 *   </li>
 *   <li>Zero or more levels with any name. This is used for path patterns as well and represents
 *   zero or more levels in this hierarchical structure. In case of a file system, this corresponds
 *   to the all entries and sub entries for a given directory</li>
 * </ol>
 * <p>
 * Assuming that components are separated by a "/", any single component means is represented by
 * "*" and "**" means mayn levels, then on a file system a path pattern like "**&#47;foo" would
 * select all files in any directory named "foo".
 *
 * @see PathResolver
 * @see Path
 */
public class ComponentPath implements Path {

  /**
   * The different component types
   */
  public enum ComponentType {
    /**
     * A single level with a specific name.
     */
    NamedLevel,

    /**
     * A single level with any name.
     */
    AnyInLevel,

    /**
     * Zero or more levels with any names.
     */
    ManyLevels
  }

  /**
   * Represents a single component of the path.
   *
   * @param type The type of the component
   * @param name The name of the component, if any
   */
  public record Component(ComponentType type, String name) {

    public Component {
      Objects.requireNonNull(type);
      Objects.requireNonNull(name);
    }
  }

  private static ComponentPath EMPTY = new ComponentPath(List.of());

  private final List<Component> components;
  private final boolean pattern;

  /**
   * Constructor.
   *
   * @param components The list of compoonents. Should be immutable.
   */
  public ComponentPath(List<Component> components) {
    this.components = Collections.unmodifiableList(Objects.requireNonNull(components));
    this.pattern = components.stream().anyMatch(component -> component.type != NamedLevel);
  }

  /**
   * Returns an empty instance.
   *
   * @return The empty instance
   */
  public static ComponentPath empty() {
    return EMPTY;
  }

  /**
   * Gets the components of this path.
   *
   * @return The components.
   */
  public List<Component> getComponents() {
    return components;
  }

  @Override
  public Path concat(Path child) {
    return concat((ComponentPath) child);
  }

  /**
   * Generates a path of this concatenated with {@code child}.
   * <p>
   * If this is {@code a/b} and child is {@code c/d}, the result is {@code a/b/c/d}
   *
   * @param child The child to append
   * @return The concatenated path
   */
  public ComponentPath concat(ComponentPath child) {
    if (child.components.isEmpty()) {
      return this;
    }
    return concat(child.components);
  }

  /**
   * Generates a path of this concatenated with {@code children}.
   * <p>
   * If this is {@code a/b} and children is {@code c/d}, the result is {@code a/b/c/d}
   *
   * @param children The children to append
   * @return The concatenated path
   */
  public ComponentPath concat(List<Component> children) {
    if (children.size() == 0) {
      return this;
    }
    List<Component> newComponents = new ArrayList<>(this.components);
    newComponents.addAll(children);
    return new ComponentPath(newComponents);
  }

  /**
   * Generates a path of this concatenated with {@code children}.
   * <p>
   * If this is {@code a/b} and children is {@code c/d}, the result is {@code a/b/c/d}
   *
   * @param children The children to append
   * @return The concatenated path
   */
  public ComponentPath concat(Component... children) {
    return concat(Arrays.asList(children));
  }

  /**
   * Generates a new {@code Path} without the first {@code level} components.
   * <p>
   * So, if this is {@code a/b/c/d/e} and level is {@code 2}, the result is {@code c/d/e}
   *
   * @param level The number of levels to remove
   * @return The stripped path
   */
  public ComponentPath subPath(int level) {
    if (level > components.size()) {
      throw new IllegalArgumentException(
          "Invalid level " + level + "; must be at most" + components.size());
    }
    return new ComponentPath(components.subList(level, components.size()));
  }

  @Override
  public boolean isPattern() {
    return pattern;
  }

  @Override
  public boolean isMatchedBy(Path pattern) {
    if (!(pattern instanceof ComponentPath cp) || isPattern()) {
      return false;
    }

    if (this.components.isEmpty()) {
      return cp.components.stream().allMatch(c -> c.type == ComponentType.ManyLevels);
    }

    return isMatchedBy(0, cp, 0);
  }

  private boolean isMatchedBy(int si, ComponentPath cp, int pi) {
    while (si < components.size() && pi < cp.components.size()) {
      Component sc = components.get(si);
      Component pc = cp.components.get(pi);
      if (!isMatchedBy(sc, pc)) {
        return false;
      }
      pi++;
      if (pc.type == ManyLevels && isMatchedByManyComponents(si, cp, pi)) {
        return true;
      } else {
        si++;
      }
    }
    return si == components.size() && pi == cp.components.size();
  }

  private boolean isMatchedByManyComponents(int si, ComponentPath cp, int pi) {
    if (pi == cp.components.size()) {
      return true;
    }
    for (int i = si; i < components.size(); i++) {
      if (isMatchedBy(i, cp, pi)) {
        return true;
      }
    }
    return false;
  }

  private boolean isMatchedBy(Component component, Component pattern) {
    return switch (pattern.type()) {
      case NamedLevel -> Objects.equals(component.name, pattern.name);
      case AnyInLevel -> true;
      case ManyLevels -> true;
    };
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ComponentPath that = (ComponentPath) o;
    return pattern == that.pattern && Objects.equals(components, that.components);
  }

  @Override
  public int hashCode() {
    return Objects.hash(components, pattern);
  }

  /**
   * Returns a string representation that is parsable by a {@link AbstractComponentPathResolver}
   * having the given settings.
   *
   * @param separator  String used to separate the different components.
   * @param anyInLevel String representing a component of type {@link ComponentType#AnyInLevel}
   * @param manyLevels String representing a component of type {@link ComponentType#ManyLevels}
   * @return The string representation
   */
  public String toString(String separator, String anyInLevel, String manyLevels) {
    StringBuilder buffer = new StringBuilder();
    for (int level = 0; level < components.size(); level++) {
      if (level > 0) {
        buffer.append(separator);
      }
      String name = switch (components.get(level).type) {
        case NamedLevel -> components.get(level).name;
        case AnyInLevel -> anyInLevel;
        case ManyLevels -> manyLevels;
      };
      buffer.append(name);
    }
    return buffer.toString();
  }

  @Override
  public String toString() {
    return toString("/", "*", "**");
  }
}
