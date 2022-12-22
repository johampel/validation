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
package de.hipphampel.validation.samples.productdata.rules;

import de.hipphampel.validation.core.annotations.BindFacts;
import de.hipphampel.validation.core.annotations.BindPath;
import de.hipphampel.validation.core.annotations.Precondition;
import de.hipphampel.validation.core.annotations.RuleDef;
import de.hipphampel.validation.core.annotations.RuleRef;
import de.hipphampel.validation.core.condition.Conditions;
import de.hipphampel.validation.core.rule.DispatchingRule;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.rule.RuleBuilder;
import de.hipphampel.validation.samples.productdata.model.metadata.AttributeDescriptor;
import de.hipphampel.validation.samples.productdata.model.product.Product;
import de.hipphampel.validation.samples.productdata.service.MetadataService;
import de.hipphampel.validation.spring.annotation.RuleContainer;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Basic rule set.
 * <p>
 * The basic rule set contains fundamental rules such as whether mandatory fields are filled and whether they have the correct data type.
 * The rules defined here are need to be valid for all products.
 */
@RuleContainer
public class BasicRules {

  private final MetadataService metadataService;

  public BasicRules(MetadataService metadataService) {
    this.metadataService = metadataService;
  }

  /**
   * {@link DispatchingRule} that orchestrates all other basic rules.
   * <p>
   * It basically triggers the rules defined below for the individual parts of a product.
   * <p>
   * One thing to outline here is that the rule recursively invokes itself, since products might be nested.
   */
  @RuleRef
  public final Rule<Product> basicRules = RuleBuilder.dispatchingRule("basicRules", Product.class)
      .withMetadata("master", true) // Rule considered as master dispatching rule
      .withPrecondition(Conditions.rule("object:notNull")) // Product must not be null
      .forPaths("").validateWith("basicRules:.*:attributes") // Validate all attributes of the product and relations
      .forPaths("relations/*/product").validateWith("basicRules") // Apply validation for all nested products
      .build();

  /**
   * {@link DispatchingRule} that checks each product attribute.
   * <p>
   * This generates a {@code DispatchingRule} that checks each product attribute whether it conforms to the corresponding
   * {@link AttributeDescriptor}. If there is no {@code AttributeDescriptor} we ignore the attribute.
   * <p>
   * In addition, it also has a special check for the {@code gtin} attribute that checks, whether the attribute is really a GTIN.
   *
   * @return The {@link Rule}
   */
  @RuleRef
  public Rule<Product> basicProductAttributesRule() {
    Set<String> paths = metadataService.getProductAttributes().stream()
        .map(descriptor -> "attributes/" + descriptor.name())
        .collect(Collectors.toSet());
    return RuleBuilder.dispatchingRule("basicRules:product:attributes", Product.class)
        .forPaths(paths).validateWith("attribute:attributeDescriptor")
        .forPaths("attributes/gtin").validateWith("attribute:validGTIN")
        .forPaths("attributes/ingredients/*").validateWith("attribute:validIngredient")
        .build();
  }

  /**
   * {@link DispatchingRule} that checks each relation attribute.
   * <p>
   * This generates a {@code DispatchingRule} that checks each relation attribute whether it conforms to the corresponding
   * {@link AttributeDescriptor}. If there is no {@code AttributeDescriptor} we ignore the attribute.
   *
   * @return The {@link Rule}
   */
  @RuleRef
  public Rule<Product> basicRelationAttributesRule() {
    Set<String> paths = metadataService.getRelationAttributes().stream()
        .map(descriptor -> "relations/*/attributes/" + descriptor.name())
        .collect(Collectors.toSet());
    return RuleBuilder.dispatchingRule("basicRules:relation:attributes", Product.class)
        .forPaths(paths).validateWith("attribute:attributeDescriptor")
        .build();
  }

  /**
   * Rule that checks, whether an ingredient is complete.
   * <p>
   * An ingredient is complete, if the name and one of percent otr amountInMg is set.
   *
   * @param name       The ingredient name
   * @param percent    Percent indication
   * @param amountInMg Amount indication
   * @return Validation result
   */
  @RuleDef(
      id = "attribute:validIngredient")
  public Result validIngredientRule(
      @BindPath("name") String name,
      @BindPath("percent") Object percent,
      @BindPath("amountInMg") Object amountInMg) {
    return percent != null || amountInMg != null ? Result.ok()
        : Result.failed(String.format("Missing 'percent' or 'amountInMg' attribute for ingredient '%s'", name));
  }

  /**
   * Special rule that checks, whether a string value represents a valid GTIN.
   *
   * @param str The string to check
   * @return {@code true}, if valid
   */
  @RuleDef(
      id = "attribute:validGTIN",
      preconditions = {@Precondition(rules = "attribute:attributeDescriptor")}, // The basic AttributeDescriptor check should be done before
      message = "Not a valid GTIN (length must be 14 characters and checksum must be ok)")
  public boolean validGTINRule(@BindFacts String str) {
    if (str.length() != 14) {
      return false;
    }
    int s = 0;
    for (int i = 13; i >= 0; i--) {
      int c = str.charAt(i) - '0';
      if (c < 0 || c > 9) {
        return false;
      }
      s += c * (3 - 2 * (i % 2));
    }
    return s % 10 == 0;
  }

  /**
   * Rule that checks whether the given object is not null.
   */
  @RuleDef(
      id = "object:notNull",
      message = "The object must not be null")
  public final Predicate<Object> objectNotNullRule = Objects::nonNull;

}
