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

import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.path.Path;
import de.hipphampel.validation.core.path.PathResolver;
import de.hipphampel.validation.core.rule.AbstractRule;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.samples.productdata.model.product.Product;
import de.hipphampel.validation.samples.productdata.model.product.Relation;
import java.math.BigDecimal;
import java.util.stream.Stream;
import org.springframework.core.convert.ConversionService;

public class HierarchyWeightRule extends AbstractRule<Product> {

  private final ConversionService validationConversionService;
  private final PathResolver pathResolver;
  private final String attribute;

  public HierarchyWeightRule(String ruleId, String attribute, ConversionService validationConversionService, PathResolver pathResolver) {
    super(ruleId);
    this.validationConversionService = validationConversionService;
    this.pathResolver = pathResolver;
    this.attribute = attribute;
  }

  @Override
  public Result validate(ValidationContext context, Product facts) {
    BigDecimal productWeight = getValueAsBigDecimal(facts, "attributes/" + attribute, null);
    if (productWeight == null) {
      return Result.ok(); // No weight given
    }

    BigDecimal relationWeight = (facts.relations() == null ? Stream.<Relation>empty() : facts.relations().stream())
        .map(this::getRelationWeight)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (relationWeight.compareTo(productWeight) > 0) {
      return Result.failed(String.format("The %s = %s is less than the sum of the corresponding weights from the relations = %S",
          attribute,
          productWeight,
          relationWeight));
    }
    return Result.ok();
  }

  private BigDecimal getRelationWeight(Relation relation) {
    BigDecimal amount = getValueAsBigDecimal(relation, "attributes/amount", BigDecimal.ZERO);
    BigDecimal weight = getValueAsBigDecimal(relation.product(), "attributes/" + attribute, BigDecimal.ZERO);
    return amount.multiply(weight);
  }

  private BigDecimal getValueAsBigDecimal(Object reference, String pathStr, BigDecimal defaultValue) {
    Path path = pathResolver.parse(pathStr);
    BigDecimal value = pathResolver.resolve(reference, path)
        .map(obj -> validationConversionService.convert(obj, BigDecimal.class))
        .orElse(null);
    return value == null ? defaultValue : value;
  }
}
