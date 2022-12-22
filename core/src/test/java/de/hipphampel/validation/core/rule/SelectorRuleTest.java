package de.hipphampel.validation.core.rule;

import static org.assertj.core.api.Assertions.assertThat;

import de.hipphampel.validation.core.TestUtils;
import de.hipphampel.validation.core.condition.Conditions;
import de.hipphampel.validation.core.event.DefaultSubscribableEventPublisher;
import de.hipphampel.validation.core.event.Event;
import de.hipphampel.validation.core.event.payloads.RuleFinishedPayload;
import de.hipphampel.validation.core.event.payloads.RuleStartedPayload;
import de.hipphampel.validation.core.execution.RuleExecutor;
import de.hipphampel.validation.core.execution.SimpleRuleExecutor;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.path.BeanPathResolver;
import de.hipphampel.validation.core.path.PathResolver;
import de.hipphampel.validation.core.path.Resolvable;
import de.hipphampel.validation.core.provider.InMemoryRuleRepository;
import de.hipphampel.validation.core.provider.RuleSelector;
import de.hipphampel.validation.core.report.ReportReporter;
import de.hipphampel.validation.core.utils.Stacked;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class SelectorRuleTest {

  private final Rule<?> rule1 = RuleBuilder.conditionRule("rule1", Object.class)
      .validateWith(Conditions.alwaysTrue())
      .build();
  private final Rule<?> rule2 = RuleBuilder.conditionRule("rule2", Object.class)
      .validateWith(Conditions.alwaysFalse())
      .build();
  private final RuleExecutor executor = new SimpleRuleExecutor();
  private final PathResolver pathResolver = new BeanPathResolver();
  private final ValidationContext context = new ValidationContext(
      new ReportReporter(null), Map.of(), executor, new InMemoryRuleRepository(rule1, rule2), pathResolver,
      new DefaultSubscribableEventPublisher());

  @Test
  public void validate() {
    Rule<Object> rule = RuleBuilder.selectorRule("undertest", Object.class)
        .validateWith(RuleSelector.of(".*"))
        .build();
    String facts = "DoesNotMatter";
    List<Event<?>> events = new ArrayList<>();
    TestUtils.collectEventsInto(context, events);

    Result result = rule.validate(context, facts);
    assertThat(result).isEqualTo(Result.failed());
    assertThat(events).containsExactlyInAnyOrder(
        new Event<>(new RuleStartedPayload(
            rule1,
            Stacked.<Resolvable>empty(), facts),
            TestUtils.FIXED_DATE, executor),
        new Event<>(new RuleFinishedPayload(rule1,
            Stacked.<Resolvable>empty(), facts,
            Result.ok(), TestUtils.FIXED_DURATION),
            TestUtils.FIXED_DATE, executor),
        new Event<>(new RuleStartedPayload(
            rule2,
            Stacked.<Resolvable>empty(), facts),
            TestUtils.FIXED_DATE, executor),
        new Event<>(new RuleFinishedPayload(rule2,
            Stacked.<Resolvable>empty(), facts,
            Result.failed(), TestUtils.FIXED_DURATION),
            TestUtils.FIXED_DATE, executor)
    );
  }

}
