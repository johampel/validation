package de.hipphampel.validation.core.provider;

import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.rule.Rule;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * {@link RuleSelector} that uses a list of {@link Predicate Predicates} to select the rules.
 * <p>
 * All given predicates must become {@code true} at the same time in order to select a rule.
 *
 * @param predicates List of {@code Predicates}
 */
public record PredicateBasedRuleSelector(List<Predicate<Rule<?>>> predicates) implements RuleSelector {

  public PredicateBasedRuleSelector {
    Objects.requireNonNull(predicates);
  }

  @Override
  public List<? extends Rule<?>> selectRules(RuleRepository provider, ValidationContext context, Object facts) {
    return provider.getRuleIds().stream()
        .map(provider::getRule)
        .filter(rule -> predicates.stream().allMatch(predicate -> predicate.test(rule)))
        .map(rule -> (Rule<?>) rule)
        .toList();
  }
}
