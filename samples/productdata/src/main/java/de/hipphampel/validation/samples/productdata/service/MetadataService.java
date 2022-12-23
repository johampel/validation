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
package de.hipphampel.validation.samples.productdata.service;

import de.hipphampel.validation.samples.productdata.model.metadata.AttributeDescriptor;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

/**
 * Service providing metadata about the attributes we are aware of.
 * <p>
 * For each attribute we are interested in, we provide an {@link AttributeDescriptor} that contains some basic information about the
 * attribute
 */
@Service
public class MetadataService {

  public static final AttributeDescriptor AMOUNT = AttributeDescriptor.integer("amount", true, 1, null);
  public static final AttributeDescriptor GTIN = AttributeDescriptor.string("gtin", true);
  public static final AttributeDescriptor NAME = AttributeDescriptor.string("name", true);
  public static final AttributeDescriptor DESCRIPTION = AttributeDescriptor.string("description", false);
  public static final AttributeDescriptor SUPPLIER = AttributeDescriptor.string("supplier", true);
  public static final AttributeDescriptor NET_WEIGHT = AttributeDescriptor.number("netWeightInKg", false, BigDecimal.valueOf(0.001), null);
  public static final AttributeDescriptor GROSS_WEIGHT = AttributeDescriptor.number("grossWeightInKg", false, BigDecimal.valueOf(0.001),
      null);
  public static final AttributeDescriptor CAN_BE_SOLD = AttributeDescriptor.bool("canBeSold", false);
  public static final AttributeDescriptor PRICE = AttributeDescriptor.number("price", false, new BigDecimal("0.01"), null);
  public static final AttributeDescriptor CATEGORY = AttributeDescriptor.string("category", true);

  public static final AttributeDescriptor INGREDIENT_LIST = AttributeDescriptor.list("ingredients", false,
      AttributeDescriptor.composite("ingredient", false,
          AttributeDescriptor.string("name", true),
          AttributeDescriptor.number("percent", false, new BigDecimal("0.1"), new BigDecimal("100.0")),
          AttributeDescriptor.number("amountInMg", false, new BigDecimal("0.1"), null)
      ));
  private static final Set<AttributeDescriptor> PRODUCT_ATTRIBUTES = Set.of(
      GTIN,
      SUPPLIER,
      NAME,
      DESCRIPTION,
      CATEGORY,
      NET_WEIGHT,
      GROSS_WEIGHT,
      CAN_BE_SOLD,
      PRICE,
      INGREDIENT_LIST
  );
  private static final Set<AttributeDescriptor> RELATION_ATTRIBUTES = Set.of(
      AMOUNT
  );

  private final Map<String, AttributeDescriptor> descriptors;

  public MetadataService() {
    this.descriptors = Stream.concat(PRODUCT_ATTRIBUTES.stream(), RELATION_ATTRIBUTES.stream())
        .collect(Collectors.toMap(AttributeDescriptor::name, Function.identity()));
  }

  /**
   * Gets the attributes we expect on a product.
   * @return Attributes
   */
  public Set<AttributeDescriptor> getProductAttributes() {
    return PRODUCT_ATTRIBUTES;
  }

  /**
   * Gets the attributes we expect on a relation.
   * @return Attributes
   */
  public Set<AttributeDescriptor> getRelationAttributes() {
    return RELATION_ATTRIBUTES;
  }

  /**
   * Gets the descriptor by name
   * @param name The name
   * @return The {@link AttributeDescriptor}
   */
  public Optional<AttributeDescriptor> getAttributeDescriptor(String name) {
    return Optional.ofNullable(descriptors.get(name));
  }


}
