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
package de.hipphampel.validation.core.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.hipphampel.validation.core.TestUtils;
import de.hipphampel.validation.core.condition.Conditions;
import de.hipphampel.validation.core.event.DefaultSubscribableEventPublisher;
import de.hipphampel.validation.core.event.Event;
import de.hipphampel.validation.core.event.payloads.RuleFinishedPayload;
import de.hipphampel.validation.core.event.payloads.RuleStartedPayload;
import de.hipphampel.validation.core.path.BeanPathResolver;
import de.hipphampel.validation.core.path.ComponentPath;
import de.hipphampel.validation.core.path.Path;
import de.hipphampel.validation.core.path.Resolvable;
import de.hipphampel.validation.core.provider.InMemoryRuleRepository;
import de.hipphampel.validation.core.provider.RuleRepository;
import de.hipphampel.validation.core.report.Report;
import de.hipphampel.validation.core.report.ReportEntry;
import de.hipphampel.validation.core.report.ReportReporter;
import de.hipphampel.validation.core.rule.DelegatingRule;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.rule.RuleBuilder;
import de.hipphampel.validation.core.rule.SystemResultReason;
import de.hipphampel.validation.core.rule.SystemResultReason.Code;
import de.hipphampel.validation.core.utils.Stacked;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SimpleRuleExecutorTest {

  private final SimpleRuleExecutor executor = new SimpleRuleExecutor();

  private final Rule<Integer> rule1 = new DelegatingRule<>(
      RuleBuilder.conditionRule("test1", Integer.class)
          .withPrecondition(Conditions.alwaysTrue())
          .validateWith(Conditions.alwaysTrue())
          .build());

  private final Rule<Integer> rule2 = new DelegatingRule<>(
      RuleBuilder.conditionRule("test2", Integer.class)
          .withPrecondition(Conditions.alwaysTrue())
          .validateWith(Conditions.alwaysTrue())
          .build());

  private final RuleRepository ruleRepository = new InMemoryRuleRepository(rule1, rule2);

  private ValidationContext context;
  private List<Event<?>> events;

  @BeforeEach
  public void beforeEach() {
    context = spy(new ValidationContext(
        new ReportReporter(null), Map.of(), executor, ruleRepository, new BeanPathResolver(),
        new DefaultSubscribableEventPublisher()));
    events = new ArrayList<>();
    TestUtils.collectEventsInto(context, events);
  }

  @Test
  public void validate_forSingleRule_ok() {
    assertThat(executor.validate(context, rule1, 4711))
        .isEqualTo(Result.ok());
    assertThat(events).containsExactly(
        new Event<>(new RuleStartedPayload(rule1, Stacked.empty(), 4711),
            TestUtils.FIXED_DATE, executor),
        new Event<>(new RuleFinishedPayload(rule1, Stacked.empty(), 4711, Result.ok(),
            TestUtils.FIXED_DURATION),
            TestUtils.FIXED_DATE, executor)
    );
  }

  @Test
  public void validate_forSingleRule_factTypeDoesNotMatchRuleType() {
    assertThat(executor.validate(context, rule1, "4711"))
        .isEqualTo(
            Result.skipped(new SystemResultReason(Code.FactTypeDoesNotMatchRuleType,
                "Expected type java.lang.Integer, but got java.lang.String")));
    assertThat(events).containsExactly(
        new Event<>(new RuleStartedPayload(rule1, Stacked.empty(), "4711"),
            TestUtils.FIXED_DATE, executor),
        new Event<>(new RuleFinishedPayload(rule1, Stacked.empty(), "4711",
            Result.skipped(new SystemResultReason(Code.FactTypeDoesNotMatchRuleType,
                "Expected type java.lang.Integer, but got java.lang.String")),
            TestUtils.FIXED_DURATION),
            TestUtils.FIXED_DATE, executor)
    );
  }

  @Test
  public void validate_forSingleRule_preconditionNotMet() {
    Rule<Integer> rule = new DelegatingRule<>(
        RuleBuilder.conditionRule("test", Integer.class)
            .withPrecondition(Conditions.alwaysFalse())
            .validateWith(Conditions.alwaysTrue())
            .build());

    assertThat(executor.validate(context, rule, 4711))
        .isEqualTo(Result.skipped(
            new SystemResultReason(Code.PreconditionNotMet, "Condition 'false' not met")));
    assertThat(events).containsExactly(
        new Event<>(new RuleStartedPayload(rule, Stacked.empty(), 4711),
            TestUtils.FIXED_DATE, executor),
        new Event<>(new RuleFinishedPayload(rule, Stacked.empty(), 4711,
            Result.skipped(
                new SystemResultReason(Code.PreconditionNotMet, "Condition 'false' not met")),
            TestUtils.FIXED_DURATION),
            TestUtils.FIXED_DATE, executor)
    );
  }

  @Test
  public void validate_forSingleRule_ruleExecutionThrowsException() {
    Rule<Integer> rule = new DelegatingRule<>(
        RuleBuilder.functionRule("test", Integer.class)
            .withPrecondition(Conditions.alwaysTrue())
            .validateWith(
                (ctxt, facts) -> {
                  throw new IllegalArgumentException("Bad thing happened");
                })
            .build());

    assertThat(executor.validate(context, rule, 4711))
        .isEqualTo(
            Result.failed(new SystemResultReason(Code.RuleExecutionThrowsException,
                "Bad thing happened")));
    assertThat(events).containsExactly(
        new Event<>(new RuleStartedPayload(rule, Stacked.empty(), 4711),
            TestUtils.FIXED_DATE, executor),
        new Event<>(new RuleFinishedPayload(rule, Stacked.empty(), 4711,
            Result.failed(new SystemResultReason(Code.RuleExecutionThrowsException,
                "Bad thing happened")),
            TestUtils.FIXED_DURATION),
            TestUtils.FIXED_DATE, executor)
    );
  }

  @Test
  public void validate_forSingleRule_addsResultToReporter() {
    assertThat(executor.validate(context, rule1, 4711)).isEqualTo(Result.ok());

    Report report = (Report) context.getReporter().getReport();
    assertThat(report).isNotNull();
    assertThat(report.entries()).containsExactly(
        new ReportEntry(ComponentPath.empty(), rule1, Result.ok())
    );
  }

  @Test
  public void validate_forSingleRule_setsCurrentRuleInValidationContext() {
    assertThat(context.getCurrentRule()).isNull();
    Rule<Integer> rule = new DelegatingRule<>(
        RuleBuilder.functionRule("test", Integer.class)
            .withPrecondition(Conditions.alwaysTrue())
            .validateWith(
                (ctxt, facts) -> ctxt.getCurrentRule() != null
                    && ctxt.getCurrentRule().getId().equals("test") ? Result.ok()
                    : Result.failed())
            .build());

    assertThat(executor.validate(context, rule, 4711)).isEqualTo(Result.ok());
    assertThat(context.getCurrentRule()).isNull();
  }

  @Test
  public void validate_forParentWithPath_sendsEventsWithParentAndPath() {
    SimpleRuleExecutor underTest = spy(executor);
    Map<String, Integer> parentFacts = Map.of("a", 4711);
    Path path = context.getPathResolver().parse("a");
    Stacked<Resolvable> pathStack = Stacked.<Resolvable>empty()
        .push(new Resolvable(parentFacts, path));

    Optional<Result> results = underTest.validateForPath(context, rule1, parentFacts, path);
    assertThat(results).contains(Result.ok());
    verify(underTest, times(1)).validate(eq(context), eq(rule1), eq(4711));
    assertThat(events).containsExactlyInAnyOrder(
        new Event<>(new RuleStartedPayload(rule1, pathStack, 4711),
            TestUtils.FIXED_DATE, underTest),
        new Event<>(
            new RuleFinishedPayload(rule1, pathStack, 4711, Result.ok(), TestUtils.FIXED_DURATION),
            TestUtils.FIXED_DATE, underTest));
  }
}
