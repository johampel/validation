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
package de.hipphampel.validation.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;


/**
 * Collection of utility methods related to reflection.
 */
public class ReflectionUtils {

  /**
   * Checks, whether {@code field} is declared {@code public} and {@code final}.
   * <p>
   * If {@code instance} is {@code null}, the field needs to be {@code static} as well. If one of the condition is not fulfilled, an
   * exception is thrown.
   *
   * @param instance The instance
   * @param field    The {@code Field} to check
   * @throws IllegalArgumentException If an exception occurs
   */
  public static void ensurePublicFinalField(Object instance, Field field) {
    int modifier = field.getModifiers();
    if (!Modifier.isFinal(modifier)) {
      throw new IllegalArgumentException("Field '" + field.getName() + "' must be final");
    }
    if (!Modifier.isPublic(modifier)) {
      throw new IllegalArgumentException("Field '" + field.getName() + "' must be public");
    }
    if ((instance == null) != Modifier.isStatic(modifier)) {
      throw new IllegalArgumentException("Field '" + field.getName() + "' must be " +
          (instance == null ? "a static field" : "an instance field"));
    }
  }

  /**
   * Checks, whether {@code method} is declared {@code public}.
   * <p>
   * If {@code instance} is {@code null}, the method needs to be {@code static} as well. If one of the condition is not fulfilled, an
   * exception is thrown.
   *
   * @param instance The instance
   * @param method   The {@code Method} to check
   * @throws IllegalArgumentException If an exception occurs
   */
  public static void ensurePublicMethod(Object instance, Method method) {
    int modifier = method.getModifiers();
    if (!Modifier.isPublic(modifier)) {
      throw new IllegalArgumentException("Method '" + method.getName() + "' must be public");
    }
    if ((instance == null) != Modifier.isStatic(modifier)) {
      throw new IllegalArgumentException("Method '" + method.getName() + "' must be " +
          (instance == null ? "a static method" : "an instance method"));
    }
  }

  /**
   * Gets the value of the given {@code field}.
   * <p>
   * Gets the value of the {@code field} and throws an exception, if the value is {@code null}
   *
   * @param instance The instance
   * @param field    The {@code Field} to check
   * @param <T>      The type of the value
   * @throws IllegalArgumentException If an exception occurs or the value is {@code null}
   */
  @SuppressWarnings("unchecked")
  public static <T> T getMandatoryFieldValue(Object instance, Field field) {
    try {
      T value = (T) field.get(instance);
      if (value == null) {
        throw new IllegalArgumentException("Field '" + field.getName() + "' is null");
      }
      return value;
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException("Cannot access field '" + field.getName() + "'", e);
    }
  }

  /**
   * Gets the positional index of the given {@code parameter}
   *
   * @param parameter The {@link Parameter}
   * @return The index
   */
  public static int getParameterIndex(Parameter parameter) {
    if (!(parameter.getDeclaringExecutable() instanceof Method method)) {
      return -1;
    }
    for (int i = 0; i < method.getParameters().length; i++) {
      if (method.getParameters()[i] == parameter) {
        return i;
      }
    }
    return -1;
  }
}
