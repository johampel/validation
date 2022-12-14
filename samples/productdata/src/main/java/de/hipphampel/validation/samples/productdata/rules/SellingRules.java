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
import de.hipphampel.validation.core.annotations.RuleDef;
import de.hipphampel.validation.core.annotations.RuleRef;
import de.hipphampel.validation.core.condition.Conditions;
import de.hipphampel.validation.core.path.Path;
import de.hipphampel.validation.core.path.PathResolver;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.rule.RuleBuilder;
import de.hipphampel.validation.samples.productdata.model.product.Product;
import de.hipphampel.validation.spring.annotation.RuleContainer;
import java.math.BigDecimal;
import java.util.stream.Stream;
import org.springframework.core.convert.ConversionService;

/**
 * Selling related rules.
 */
@RuleContainer
public class SellingRules {


  private final ConversionService validationConversionService;
  private final PathResolver pathResolver;

  public SellingRules(ConversionService validationConversionService, PathResolver pathResolver) {
    this.validationConversionService = validationConversionService;
    this.pathResolver = pathResolver;
  }

  @RuleRef
  public final Rule<Product> sellingRules = RuleBuilder.dispatchingRule("sellingRules", Product.class)
      .withMetadata("master", true) // Rule considered as master dispatching rule
      .withPrecondition(Conditions.rule("basicRules"))
      .forPaths("").validateWith("selling:hierarchy:.*")
      .forPaths("relations/*/product").validateWith("selling:product:.*")
      .build();

  /**
   * Rules that checks that products that can be sold have a price,
   *
   * @param price     The price
   * @param canBeSold Flag indicating that the product can be sold
   * @return {@code true} if value
   */
  @RuleDef(
      id = "selling:product:sellableProductsNeedAPrice",
      message = "A price is mandatory, if the product can be sold")
  public boolean sellableProductsNeedAPriceRule(
      @BindPath("attributes/price") BigDecimal price,
      @BindPath("attributes/canBeSold") Boolean canBeSold) {
    return price != null || !Boolean.TRUE.equals(canBeSold);
  }

  /**
   * Checks whether at least one product in the hierarchy ca be sold.
   * <p>
   * Either the product itself or at least one of the products in the hierarchy need to be.
   *
   * @param product The product to validate.
   * @return {@code true}, if a product in the hierarch can be sold.
   */
  @RuleDef(
      id = "selling:hierarchy:atLeastOneProductCanBeSold",
      message = "None of the products in the hierarchy can be sold")
  public boolean atLeastOneProductCanBeSoldRule(@BindFacts Product product) {
    Path nestedFlagPaths = pathResolver.parse("**/product/attributes/canBeSold");
    Path selfFlagPaths = pathResolver.parse("attributes/canBeSold");

    return Stream.of(nestedFlagPaths, selfFlagPaths) // Examine paths attributes/canBeSold and **/product/attributes/canBeSold
        .flatMap(path -> pathResolver.resolvePattern(product, path)) // Get the concrete paths
        .map(path -> pathResolver.resolve(product, path)) // get the values
        .map(resolved -> resolved.orElse(false)) // If not set, assume false
        .map(value -> validationConversionService.convert(value, Boolean.class)) // Convert value to boolean
        .anyMatch(Boolean.TRUE::equals); // Hope at least one is true
  }
}
