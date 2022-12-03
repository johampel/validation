package de.hipphampel.validation.core.provider;

import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.rule.OkRule;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.value.Values;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class CategorizingRuleSelectorTest {

  private final Rule<Object> rule1 = new OkRule<>("rule1");
  private final Rule<Object> rule2 = new OkRule<>("rule2");
  private final Rule<Object> rule3 = new OkRule<>("rule3");
  private final Rule<Object> rule4 = new OkRule<>("rule4");

  private final InMemoryRuleRepository provider = new InMemoryRuleRepository(rule1, rule2, rule3, rule4);

  @ParameterizedTest
  @CsvSource({

      " , 'rule4'",
      "a, 'rule1,rule2'",
      "b, 'rule3'",
      "c, 'rule4'",
  })
  public void selectRules(String category, String expected) {
    CategorizingRuleSelector<String> selector = RuleSelectorBuilder.withCategorizer(Values.val(category))
        .forCategory("a").use(RuleSelector.of("rule1", "rule2"))
        .forCategory("b").use(RuleSelector.of("rule3"))
        .elseUse(RuleSelector.of("rule4"))
        .build();

    String ruleIds = selector.selectRules(provider, mock(ValidationContext.class), mock(Object.class))
        .stream().map(Rule::getId).collect(Collectors.joining(","));

    assertThat(ruleIds).isEqualTo(expected);

  }
}