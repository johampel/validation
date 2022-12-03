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
package de.hipphampel.validation.core.rule;

import de.hipphampel.validation.core.utils.TypeInfo;

/**
 * Collection of {@link Rule} related utility methods.
 */
public class RuleUtils {

  /**
   * Determines the {@link TypeInfo} the facts need to have when calling the given {@code rule}.
   * <p>
   * THe method deduces this information from the generic type information of the {@code rule}
   *
   * @param rule The {@link Rule}
   * @return The type
   */
  public static TypeInfo determineRuleFactsTypeInfo(Rule<?> rule) {
    for (TypeInfo typeInfo = TypeInfo.forInstance(rule);
        typeInfo != TypeInfo.NONE;
        typeInfo = typeInfo.getSuperType()) {
      for (TypeInfo interfaceInfo : typeInfo.getInterfaces()) {
        if (interfaceInfo.getRawClass() == Rule.class) {
          return interfaceInfo.getGeneric(0);
        }
      }
    }
    throw new IllegalStateException("Cannot determine of the rules facts type");
  }

  /**
   * Determines the {@link Class} the facts need to have when calling the given {@code rule}.
   * <p>
   * THe method deduces this information from the generic type information of the {@code rule}
   *
   * @param rule The {@link Rule}
   * @param <T>  Type type
   * @return The type
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T> determineRuleFactsType(Rule<?> rule) {
    Class<?> type = determineRuleFactsTypeInfo(rule).resolve();
    return (Class<T>) (type == null ? Object.class : type);
  }

}
