package de.hipphampel.validation.core.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import de.hipphampel.validation.core.condition.Conditions;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.rule.RuleBuilder;
import java.util.List;
import org.junit.jupiter.api.Test;

public class PredicateBasedRuleSelectorTest {

  private final Rule<?> rule1 = RuleBuilder.conditionRule("arule1", Object.class)
      .withMetadata("foo", "bar")
      .validateWith(Conditions.alwaysTrue())
      .build();
  private final Rule<?> rule2 = RuleBuilder.conditionRule("arule2", Object.class)
      .validateWith(Conditions.alwaysTrue())
      .build();
  private final Rule<?> rule3 = RuleBuilder.conditionRule("brule1", Object.class)
      .withMetadata("foo", "bar")
      .validateWith(Conditions.alwaysTrue())
      .build();
  private final RuleRepository repository = new InMemoryRuleRepository(rule1, rule2, rule3);

  @Test
  public void onePredicate() {
    PredicateBasedRuleSelector selector = RuleSelectorBuilder.withPredicate(rule -> rule.getId().startsWith("a"))
        .build();

    assertThat((List<Rule<?>>) selector.selectRules(repository, mock(ValidationContext.class), mock(Object.class)))
        .containsExactlyInAnyOrderElementsOf(List.of(rule1, rule2));
  }

  @Test
  public void twoPredicates() {
    PredicateBasedRuleSelector selector = RuleSelectorBuilder.withPredicate(rule -> rule.getId().startsWith("a"))
        .and(rule -> rule.getMetadata().containsKey("foo"))
        .build();

    assertThat(selector.selectRules(repository, mock(ValidationContext.class), mock(Object.class)))
        .isEqualTo(List.of(rule1));
  }
}
