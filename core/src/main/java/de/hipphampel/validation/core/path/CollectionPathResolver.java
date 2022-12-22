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

import de.hipphampel.validation.core.path.ComponentPath.Component;
import de.hipphampel.validation.core.path.ComponentPath.ComponentType;
import de.hipphampel.validation.core.utils.Pair;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * {@link PathResolver} implementation for collection based objects.
 * <p>
 * This implementation can resolve paths on any kind of objects that implement the {@link Collection} interface and on {@link Map Maps} that
 * use a {@code String} as a key. This makes it suitable for all objects being JSON serializable, since in JSON any object can be seen as a
 * composition of atomic values, lists and maps. The only restriction regarding {@code Collections} is that when iterating through them, the
 * order is stable, if iterating it twice or more times.
 * <p>
 * This implementation uses the {@link ComponentPath} as representation for a  {@link Path}. The components represent either keys in a map
 * or the string representation of collection indices, so the path "2" being applied to a collection would resolve to the third element in
 * the collection. Note that the implementation makes no distinction between maps and collections: a component named "2" could be also seen
 * as key for a map.
 * <p>
 * Apart from that this implementation follows the same semantics as described for its base class, the
 * {@link AbstractComponentPathResolver}
 *
 * @see AbstractComponentPathResolver
 * @see ComponentPath
 */
public class CollectionPathResolver extends AbstractComponentPathResolver {

  private final boolean mapUnresolvableToNull;

  /**
   * Constructor.
   *
   * @param separator             String used to separate the different components.
   * @param allInLevel            String representing exactly one levels having any name
   * @param manyLevels            String representing zero or more levels having any name
   * @param mapUnresolvableToNull If {@code true}, then not existing concrete {@link Path Paths} resolve to a {@link Resolved} with value
   *                              {@code null}. If {@code false} it resolves to an {@link Resolved#empty() empty} {@code Resolved}
   */
  public CollectionPathResolver(String separator, String allInLevel, String manyLevels, boolean mapUnresolvableToNull) {
    super(separator, allInLevel, manyLevels);
    this.mapUnresolvableToNull = mapUnresolvableToNull;
  }

  /**
   * Constructor.
   * <p>
   * Creates an instance with the default settings. This is the same as calling {@code CollectionPathResolver("/", "*", "**", false)}
   *
   * @param separator  String used to separate the different components.
   * @param allInLevel String representing exactly one levels having any name
   * @param manyLevels String representing zero or more levels having any name
   */
  public CollectionPathResolver(String separator, String allInLevel, String manyLevels) {
    this(separator, allInLevel, manyLevels, false);
  }

  /**
   * Constructor.
   * <p>
   * Creates an instance with the default settings. This is the same as calling {@code CollectionPathResolver("/", "*", "**", false)}
   */
  public CollectionPathResolver() {
    this("/", "*", "**", false);
  }

  /**
   * Gets the {@code mapUnresolvableToNull}
   *
   * @return If {@code true}, then not existing concrete {@link Path Paths} resolve to a {@link Resolved} with value {@code null}. If
   * {@code false} it resolves to an {@link Resolved#empty() empty} {@code Resolved}
   */
  public boolean isMapUnresolvableToNull() {
    return mapUnresolvableToNull;
  }

  @Override
  protected Resolved<Object> resolveLevel(Object ref, Component component) {
    if (ref instanceof Map<?, ?> map) {
      if (map.containsKey(component.name())) {
        return Resolved.of(map.get(component.name()));
      } else if (mapUnresolvableToNull) {
        return Resolved.of(null);
      } else {
        return Resolved.empty();
      }
    } else if (ref instanceof List<?> list) {
      int index = stringToIndex(component.name());
      if (index >= 0 && index < list.size()) {
        return Resolved.of(list.get(index));
      } else {
        return Resolved.empty();
      }
    } else if (ref instanceof Collection<?> collection) {
      int index = stringToIndex(component.name());
      if (index >= 0 && index < collection.size()) {
        return Resolved.of(collection.stream().skip(index).findFirst().orElseThrow());
      } else {
        return Resolved.empty();
      }
    }
    return resolveLevelForNonCollection(ref, component);
  }

  /**
   * Called when attempting to resolve a concrete level for a non map or collection.
   * <p>
   * Can be used by derived classes to resolve a {@code Path} that points to a different object than covered by this implementation. This
   * implemnentation returns always {@link Resolved#empty()}
   *
   * @param ref       The object where the {@code component} is applied to
   * @param component The component of the {@code Path} to resolve
   * @return a {@link Resolved} indicating the result
   */
  protected Resolved<Object> resolveLevelForNonCollection(Object ref, Component component) {
    return mapUnresolvableToNull ? Resolved.of(null) : Resolved.empty();
  }

  private int stringToIndex(String str) {
    try {
      return Integer.parseInt(str);
    } catch (NumberFormatException nfe) {
      return -1;
    }
  }

  protected Stream<Pair<Component, Object>> resolvePatternLevel(Object ref, Component component) {

    if (ref instanceof Map map) {
      return ((Map<?, ?>) map).entrySet().stream()
          .map(e -> Pair.of(
              new Component(ComponentType.NamedLevel, String.valueOf(e.getKey())),
              e.getValue()));
    } else if (ref instanceof List list) {
      return IntStream.range(0, list.size()).boxed()
          .map(i -> Pair.of(
              new Component(ComponentType.NamedLevel, "" + i),
              list.get(i)));
    } else if (ref instanceof Collection collection) {
      AtomicInteger i = new AtomicInteger();
      return ((Collection<?>) collection).stream()
          .map(e -> Pair.of(
              new Component(ComponentType.NamedLevel, "" + i.getAndIncrement()),
              e));
    }
    return resolvePatternLevelForNonCollection(ref, component);
  }

  /**
   * Called when attempting to resolve a patter level for a non map or collection.
   * <p>
   * Can be used by derived classes to resolve a {@code Path} that points to a different object than covered by this implementation. This
   * implementation always returns {@link Stream#empty()}.
   *
   * @param ref       The object where the {@code component} is applied to
   * @param component The component of the {@code Path} to resolve
   * @return A Stream containing pairs with concrete {@link Component Components} having the type {@link ComponentType#NamedLevel} plus the
   * corresponding resolved object.
   */
  protected Stream<Pair<Component, Object>> resolvePatternLevelForNonCollection(Object ref,
      Component component) {
    return Stream.empty();
  }
}
