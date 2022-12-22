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
package de.hipphampel.validation.samples.productdata.model.metadata;

import de.hipphampel.validation.samples.productdata.model.product.Product;
import de.hipphampel.validation.samples.productdata.model.product.Relation;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.core.convert.TypeDescriptor;

/**
 * Describes how an attribute of a {@link Product} or {@link Relation} should look like.
 * <p>
 * It contains information about the name of the attribute, whether it is mandatory and what type is has.
 */
public sealed interface AttributeDescriptor permits BoolAttributeDescriptor, CompositeAttributeDescriptor, IntegerAttributeDescriptor,
    ListAttributeDescriptor, NumberAttributeDescriptor, StringAttributeDescriptor {

  /**
   * Gets the name of the attribute.
   *
   * @return The name
   */
  String name();

  /**
   * Indicates, whether the attribute is mandatory.
   *
   * @return {@code true}, if mandatory.
   */
  boolean mandatory();

  /**
   * A {@link TypeDescriptor} describing the type
   *
   * @return The {@code TypeDescriptor}
   */
  TypeDescriptor type();

  static AttributeDescriptor integer(String name, boolean mandatory, Integer minValue, Integer maxValue) {
    return new IntegerAttributeDescriptor(name, mandatory, minValue, maxValue);
  }

  static AttributeDescriptor number(String name, boolean mandatory, BigDecimal minValue, BigDecimal maxValue) {
    return new NumberAttributeDescriptor(name, mandatory, minValue, maxValue);
  }

  static AttributeDescriptor string(String name, boolean mandatory) {
    return new StringAttributeDescriptor(name, mandatory);
  }

  static AttributeDescriptor bool(String name, boolean mandatory) {
    return new BoolAttributeDescriptor(name, mandatory);
  }

  static AttributeDescriptor list(String name, boolean mandatory, AttributeDescriptor elementType) {
    return new ListAttributeDescriptor(name, mandatory, elementType);
  }

  static AttributeDescriptor composite(String name, boolean mandatory, AttributeDescriptor... components) {
    return new CompositeAttributeDescriptor(name, mandatory, List.of(components));
  }
}

