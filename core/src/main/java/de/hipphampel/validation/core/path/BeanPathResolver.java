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
import java.util.stream.Stream;

/**
 * Simple {@link PathResolver} for any kind of beans.
 * <p>
 * This is based on a {@link CollectionPathResolver}. In addition to the capabilities inherited from there (allow adressing elements in
 * {@code Collections} and {@code Maps} with strings as keys) it allows to address properties of PoJos/Beans.
 * <p>
 * The syntax of the {@link ComponentPath ComponentPaths} is the same like for {@code CollectionPathResolver}, but in addition a single
 * component might be also interpreted as the name of a bean property. So having a Java record like
 * {@code record Person(String name, Collection<Person> friends)} allows path like "**&#47;friends/*&#47;name" to select the names of all
 * friends that appears somewhere in the object graph.
 * <p>
 * The strategy to find the properties is outsourced in the {@link BeanAccessor}, which is by default an implementation that finds all the
 * getters for normal Java Beans as well for Java records.
 *
 * @see CollectionPathResolver
 * @see ComponentPath
 * @see BeanAccessor
 */
public class BeanPathResolver extends CollectionPathResolver {

  private final BeanAccessor beanAccessor;

  /**
   * Default constructor.
   * <p>
   * Creates an instance with the standard settings from the {@link CollectionPathResolver} and a {@link BeanAccessor} that recognizes
   * properties of Java Beans and Java records.
   */
  public BeanPathResolver() {
    this(new ReflectionBeanAccessor());
  }

  /**
   * Constructor.
   * <p>
   * Creates an instance with the standard settings from the {@link CollectionPathResolver} and the given {@code beanAccessor}
   *
   * @param beanAccessor {@code BeanAccessor} to use
   */
  public BeanPathResolver(BeanAccessor beanAccessor) {
    this.beanAccessor = beanAccessor;
  }

  /**
   * Constructor.
   * <p>
   * Missing keys in maps resolve to {@link Resolved#isEmpty() empty}
   *
   * @param separator    String used to separate the different components.
   * @param allInLevel   String representing exactly one levels having any name
   * @param manyLevels   String representing zero or more levels having any name
   * @param beanAccessor The {@code BeanAccessor} to use
   */
  public BeanPathResolver(String separator, String allInLevel, String manyLevels, BeanAccessor beanAccessor) {
    this(separator, allInLevel, manyLevels, beanAccessor, false);
  }

  /**
   * Constructor.
   *
   * @param separator             String used to separate the different components.
   * @param allInLevel            String representing exactly one levels having any name
   * @param manyLevels            String representing zero or more levels having any name
   * @param beanAccessor          The {@code BeanAccessor} to use
   * @param mapUnresolvableToNull If {@code true}, then not existing concrete {@link Path Paths} resolve to a {@link Resolved} with value
   *                              {@code null}. If {@code false} it resolves to an {@link Resolved#empty() empty} {@code Resolved}
   */
  public BeanPathResolver(String separator, String allInLevel, String manyLevels, BeanAccessor beanAccessor,
      boolean mapUnresolvableToNull) {
    super(separator, allInLevel, manyLevels, mapUnresolvableToNull);
    this.beanAccessor = beanAccessor;
  }

  @Override
  protected Resolved<Object> resolveLevelForNonCollection(Object ref, Component component) {
    if (beanAccessor.knowsProperty(ref, component.name())) {
      return beanAccessor.getProperty(ref, component.name());
    }
    return super.resolveLevelForNonCollection(ref, component);
  }

  @Override
  protected Stream<Pair<Component, Object>> resolvePatternLevelForNonCollection(Object ref,
      Component component) {
    return beanAccessor.getPropertyNames(ref).stream()
        .map(name -> Pair.of(
            new Component(ComponentType.NamedLevel, name),
            beanAccessor.getProperty(ref, name).get()));
  }
}
