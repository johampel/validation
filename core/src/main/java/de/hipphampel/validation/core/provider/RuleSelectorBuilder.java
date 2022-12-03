package de.hipphampel.validation.core.provider;

import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.value.Value;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Builders to create {@link RuleSelector RuleSelectors}
 */
public class RuleSelectorBuilder {

  /**
   * Starts the production of a {@link CategorizingRuleSelector}.
   *
   * @param discriminator The discriminator to determine the value
   * @param <D>           Type of the category
   * @return The builder
   */
  public static <D> CategorizingRuleSelectorBuilder<D> withCategorizer(Value<D> discriminator) {
    return new CategorizingRuleSelectorBuilder<>(discriminator);
  }

  /**
   * Builder for a {@link CategorizingRuleSelector}.
   * <p>
   * The builder is normally created using the {@link #withCategorizer(Value) withCategorizer} method. For example:
   *
   * <pre>
   *   CotegorizingRuleSelector selector =  RuleSelectorBuilder.withCategorizer(Values.path("typeofObject"))
   *      .forCategory("MEDS").use("MedRules:.*")
   *      .forCategory("FOOD").use("FoodRules:.*")
   *      .elseUse("UncategorizedRules:.*")
   *      .build();
   * </pre>
   *
   * @param <D> Type of the category
   */
  public static class CategorizingRuleSelectorBuilder<D> extends RuleSelectorBuilder {

    private final Value<D> discriminator;
    private final Map<D, RuleSelector> categories = new HashMap<>();
    private RuleSelector defaultCategory;

    private CategorizingRuleSelectorBuilder(Value<D> discriminator) {
      this.discriminator = discriminator;
    }

    /**
     * Creates a subordinated builder to specify the {@link RuleSelector} to be used for the given {@code category}.
     *
     * @param category The category
     * @return A {@link  CategorizingRuleSelectorBuilder}
     */
    public CategorizingRuleSelectorCategoryBuilder forCategory(D category) {
      return new CategorizingRuleSelectorCategoryBuilder(category);
    }

    /**
     * Specifies the {@link RuleSelector} to use, if no category matches.
     *
     * @param ruleSelector The {@code RuleSelector} to use if not category is matching
     * @return This builder
     */
    public CategorizingRuleSelectorBuilder<D> elseUse(RuleSelector ruleSelector) {
      defaultCategory = ruleSelector;
      return this;
    }

    /**
     * Finalizes the production of the {@link CategorizingRuleSelector}.
     *
     * @return The {@code CategorizingRuleSelector}
     */
    public CategorizingRuleSelector<D> build() {
      return new CategorizingRuleSelector<>(discriminator, Collections.unmodifiableMap(categories), defaultCategory);
    }


    /**
     * Subordinated builder of the {@link CategorizingRuleSelectorBuilder} to add a single category to the {@code categories} map.
     * <p>
     * It specifies, for a given category, which rules to use, the category to use is bound at construction time to this builder.
     */
    public class CategorizingRuleSelectorCategoryBuilder {

      private final D category;

      CategorizingRuleSelectorCategoryBuilder(D category) {
        this.category = category;
      }

      /**
       * Assigns the {@code ruleSelector} to the category bound to this builder.
       * <p>
       * It returns the parent {@link CategorizingRuleSelectorBuilder} with the additional category mapping created by this instance.
       *
       * @param ruleSelector The {@link RuleSelector} assigned to the category
       * @return The parent builder.
       */
      public CategorizingRuleSelectorBuilder<D> use(RuleSelector ruleSelector) {
        CategorizingRuleSelectorBuilder.this.categories.put(category, ruleSelector);
        return CategorizingRuleSelectorBuilder.this;
      }

      /**
       * Assigns a {@link RuleSelector} for the given {@code ruleIds} to the category bound to this builder.
       * <p>
       * It returns the parent {@link CategorizingRuleSelectorBuilder} with the additional category mapping created by this instance.
       *
       * @param ruleIds The ids of the {@link Rule Rules} assigned to the category
       * @return The parent builder.
       */
      public CategorizingRuleSelectorBuilder<D> use(String... ruleIds) {
        return use(RuleSelector.of(ruleIds));
      }

      /**
       * Assigns a {@link RuleSelector} for the given {@code ruleIds} to the category bound to this builder.
       * <p>
       * It returns the parent {@link CategorizingRuleSelectorBuilder} with the additional category mapping created by this instance.
       *
       * @param ruleIds The ids of the {@link Rule Rules} assigned to the category
       * @return The parent builder.
       */
      public CategorizingRuleSelectorBuilder<D> use(Collection<String> ruleIds) {
        return use(RuleSelector.of(ruleIds));
      }
    }

  }
}