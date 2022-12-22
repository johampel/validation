package de.hipphampel.validation.samples.productdata.rules;

import de.hipphampel.validation.core.annotations.BindPath;
import de.hipphampel.validation.core.annotations.RuleDef;
import de.hipphampel.validation.core.annotations.RuleRef;
import de.hipphampel.validation.core.provider.RuleSelector;
import de.hipphampel.validation.core.provider.RuleSelectorBuilder;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.rule.RuleBuilder;
import de.hipphampel.validation.core.value.Values;
import de.hipphampel.validation.samples.productdata.model.product.Product;
import de.hipphampel.validation.spring.annotation.RuleContainer;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;

@RuleContainer
public class CategoryRules {

  private final ConversionService validationConversionService;

  @RuleRef
  public final Rule<Product> categoryRules = RuleBuilder.dispatchingRule("categoryRules", Product.class)
      .withMetadata("master", true) // Rule considered as master dispatching rule
      .forPaths("relations/*/product").validateWith("categoryRules")
      .forPaths("").validateWith(
          RuleSelectorBuilder.withCategorizer(Values.path("attributes/category"))
              .forCategory("ALCOHOL").use("categoryRules:alcohol:.*")
              .forCategory("FOOD").use("categoryRules:food:.*")
              .elseUse(RuleSelector.of(""))
              .build())
      .build();

  public CategoryRules(ConversionService validationConversionService) {
    this.validationConversionService = validationConversionService;
  }

  @RuleDef(
      id = "categoryRules:alcohol:alcoholIngredientMandatory",
      message = "Alcoholic products need an indication about the alcohol in it"
  )
  public boolean alcoholIngredientMandatoryRule(@BindPath("attributes/ingredients") List<Map<String, Object>> ingredients) {
    return ingredients.stream().anyMatch(ingredient -> ingredient.get("name").equals("Alcohol"));
  }

  @RuleDef(
      id = "categoryRules:food:descriptionMustContainSugarHint",
      message = "If products contains more than 10g sugar, the description must contain the word 'sugar'"
  )
  public Result descriptionMustContainSugarHintRule(
      @BindPath("attributes/description") String description,
      @BindPath("attributes/ingredients") List<Map<String, Object>> ingredients) {

    if (description != null && description.toLowerCase().contains("sugar")) {
      return Result.ok();
    }

    Map<String, Object> ingredient = ingredients.stream()
        .filter(i -> i.get("name").equals("Sugar"))
        .findFirst()
        .orElse(null);
    if (ingredient == null || !ingredient.containsKey("amountInMg")) {
      return Result.ok();
    }

    try {
      BigDecimal value = validationConversionService.convert(ingredient.get("amountInMg"), BigDecimal.class);
      if (value.compareTo(new BigDecimal("10000.0")) >= 0) {
        return Result.failed(
            String.format("The product contains %smg sugar (which is more than 10g), so the description must contain the term 'sugar'",
                value));
      }
      return Result.ok();
    } catch (ConversionException ce) {
      // Should be reported by different rule
      return Result.ok();
    }
  }
}
