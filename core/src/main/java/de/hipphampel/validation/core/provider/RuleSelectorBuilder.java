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
package de.hipphampel.validation.core.provider;

import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.value.Value;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;


/**
 * Builders to create {@link RuleSelector RuleSelectors}
 */
public class RuleSelectorBuilder {

  /**
   * Starts the production of a {@link PredicateBasedRuleSelector}.
   *
   * @param predicate The first {@link Predicate}
   * @return The builder
   */
  public static PredicateBasedRuleSelectorBuilder withPredicate(Predicate<Rule<?>> predicate) {
    return new PredicateBasedRuleSelectorBuilder(predicate);
  }

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
   * Builder for a {@link PredicateBasedRuleSelector}.
   * <p>
   * The builder is normally created using the {@link #withPredicate(Predicate) withPredicate} method. For example:
   *
   * <pre>
   *   PredicateBasedRuleSelector selector =  RuleSelectorBuilder.withPredicate(rule -> rule.getId().startsWith("ABC"))
   *      .and(rule -> rule.getMetadata().containsKey("ImportantKey")
   *      .build();
   * </pre>
   */
  public static class PredicateBasedRuleSelectorBuilder extends RuleSelectorBuilder {

    private final List<Predicate<Rule<?>>> predicates;

    private PredicateBasedRuleSelectorBuilder(Predicate<Rule<?>> predicate) {
      this.predicates = new ArrayList<>();
      this.predicates.add(predicate);
    }

    /**
     * Adds a further {@link Predicate} that need to be {@code true} in order to select a {@link Rule}.
     * <p>
     * All predicates are and combined.
     *
     * @param predicate The {@code Predicate} to add
     * @return This builder
     */
    public PredicateBasedRuleSelectorBuilder and(Predicate<Rule<?>> predicate) {
      this.predicates.add(predicate);
      return this;
    }

    /**
     * Finalizes the production of the {@link PredicateBasedRuleSelector}.
     *
     * @return The {@code PredicateBasedRuleSelector}
     */
    public PredicateBasedRuleSelector build() {
      return new PredicateBasedRuleSelector(Collections.unmodifiableList(new ArrayList<>(predicates)));
    }
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