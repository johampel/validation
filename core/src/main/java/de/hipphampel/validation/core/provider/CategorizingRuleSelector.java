package de.hipphampel.validation.core.provider;

import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.value.Value;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * {@link RuleSelector} that selects the {@link Rule Rules} based  on the category of the object to validate.
 * <p>
 * In order to determine the {@code Rules}, this selector first calls the {@code disscriminator} function, which returns a category of type
 * {@code D}. It then returns those {@code Rules} that are mapped in the {@code categories} map to this category. If no matching entry is
 * found in the {@code categories} map, the rules from the {@code defaultCategory} are used.
 *
 * @param discriminator   The discriminator function. It accepts the {@link ValidationContext} and the object being validated and returns
 *                        the category.
 * @param categories      A Map of {@code RuleSelectors}, whereas the keys are the different category values.
 * @param defaultCategory The {@code RuleSelector} to be used in case the {@code discriminator} returns an unknown category.
 * @param <D>             The type of the category
 */
public record CategorizingRuleSelector<D>(
    Value<D> discriminator,
    Map<D, RuleSelector> categories,
    RuleSelector defaultCategory) implements RuleSelector {

  public CategorizingRuleSelector {
    Objects.requireNonNull(discriminator);
    Objects.requireNonNull(categories);
    Objects.requireNonNull(defaultCategory);
  }

  @Override
  public List<Rule<?>> selectRules(RuleRepository provider, ValidationContext context, Object facts) {
    return Optional.ofNullable(discriminator.get(context, facts))
        .flatMap(category -> Optional.ofNullable(categories.get(category)))
        .orElse(defaultCategory)
        .selectRules(provider, context, facts);
  }
}
