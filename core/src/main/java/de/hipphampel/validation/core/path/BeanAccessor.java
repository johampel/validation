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

import java.util.List;

/**
 * Supplemental service for a {@link BeanPathResolver} for accessing the properties of a bean.
 * <p>
 * Instances of this class are passed to the {@code BeanPathResolver} to provide a strategy how to
 * extract a property from a given object. By default, the {@code BeanPathResolver} uses the
 * {@link ReflectionBeanAccessor}.
 * <p>
 * The basic aim is providing an abstraction to access the fields of an object.
 *
 * @see ReflectionBeanAccessor
 */
public interface BeanAccessor {

  /**
   * Checks, whether the given {@code bean} knows the property named {@code name}.
   *
   * @param bean The bean
   * @param name The name of the property
   * @return {@code true}, if it exists.
   */
  boolean knowsProperty(Object bean, String name);

  /**
   * Gets the value of the property named {@code name} for the specified {@code bean}.
   *
   * @param bean The bean to get the value from
   * @param name The property name
   * @return The value
   */
  Resolved<Object> getProperty(Object bean, String name);

  /**
   * Gets the property names known for the given {@code bean}
   *
   * @param bean The bean.
   * @return The property names.
   */
  List<String> getPropertyNames(Object bean);
}
