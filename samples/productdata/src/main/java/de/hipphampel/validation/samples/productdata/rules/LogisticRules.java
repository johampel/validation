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

import de.hipphampel.validation.core.annotations.BindPath;
import de.hipphampel.validation.core.annotations.RuleDef;
import de.hipphampel.validation.core.annotations.RuleRef;
import de.hipphampel.validation.core.condition.Conditions;
import de.hipphampel.validation.core.path.PathResolver;
import de.hipphampel.validation.core.rule.DispatchingRule;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.rule.RuleBuilder;
import de.hipphampel.validation.samples.productdata.model.product.Product;
import de.hipphampel.validation.spring.annotation.RuleContainer;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.core.convert.ConversionService;

/**
 * Optional logistics rule set.
 * <p>
 * If present, logistics information are validated.
 */
@RuleContainer
public class LogisticRules {

  private final ConversionService validationConversionService;
  private final PathResolver pathResolver;

  public LogisticRules(ConversionService validationConversionService, PathResolver pathResolver) {
    this.validationConversionService = validationConversionService;
    this.pathResolver = pathResolver;
  }

  /**
   * {@link DispatchingRule} that orchestrates all logistic related rules.
   * <p>
   * One thing to outline here is that the rule recursively invokes itself, since products might be nested.
   */
  @RuleRef
  public final Rule<Product> logisticRules = RuleBuilder.dispatchingRule("logisticRules", Product.class)
      .withMetadata("master", true) // Rule considered as master dispatching rule
      .withPrecondition(Conditions.rule("basicRules"))
      .forPaths("").validateWith("logistic:.*")
      .forPaths("relations/*/product").validateWith("logisticRules")
      .build();

  /**
   * Checks the weight of a product.
   * <p>
   * If given, the net weight must be less or equal the gross weight; weights need to be bigger than 0.
   *
   * @param netWeight   The net weight
   * @param grossWeight The gross weight
   * @return The validation result
   */
  @RuleDef(id = "logistic:product:checkProductWeight")
  public Result checkProductWeightRule(
      @BindPath("attributes/netWeightInKg") BigDecimal netWeight,
      @BindPath("attributes/grossWeightInKg") BigDecimal grossWeight) {
    if (netWeight != null && grossWeight != null && netWeight.compareTo(grossWeight) > 0) {
      return Result.failed(String.format("netWeight=%s is bigger than grossWeight=%s", netWeight, grossWeight));
    }
    return Result.ok();
  }

  /**
   * Rule factory that produces {@link Rule Rules} to check, whether the weights are in the product hierarchy are consistent.
   * <p>
   * A weight of a product is consistent, if it is equal or larger than the sum of the weights of the components.
   * <p>
   * In this case, two rules are produced: One for the net weight and the other for the gross weight
   *
   * @return The list of {@code Rules}
   */
  @RuleRef
  public List<? extends Rule<Product>> hierarchicalWeightRuleFactory() {
    return Stream.of("netWeightInKg", "grossWeightInKg")
        .map(attribute -> new HierarchyWeightRule(
            "logistic:product:" + attribute + "HierarchyCheck",
            attribute,
            validationConversionService,
            pathResolver))
        .toList();
  }
}