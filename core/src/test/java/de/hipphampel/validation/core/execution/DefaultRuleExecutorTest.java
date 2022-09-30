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

import de.hipphampel.validation.core.TestUtils;
import de.hipphampel.validation.core.condition.Conditions;
import de.hipphampel.validation.core.event.Event;
import de.hipphampel.validation.core.event.payloads.RuleFinishedPayload;
import de.hipphampel.validation.core.event.payloads.RuleStartedPayload;
import de.hipphampel.validation.core.rule.AbstractRule;
import de.hipphampel.validation.core.rule.DelegatingRule;
import de.hipphampel.validation.core.rule.OkRule;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.rule.RuleBuilder;
import de.hipphampel.validation.core.utils.Stacked;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultRuleExecutorTest {

  private final DefaultRuleExecutor executor = new DefaultRuleExecutor();
  private ValidationContext context;
  private List<Event<?>> events;

  @BeforeEach
  public void beforeEach() {
    context = new ValidationContext();
    events = new CopyOnWriteArrayList<>();
    TestUtils.collectEventsInto(context, events);
  }

  @Test
  public void validate_cachesResults() {
    Rule<Integer> rule = new DelegatingRule<>(
        RuleBuilder.conditionRule("test", Integer.class)
            .withPrecondition(Conditions.alwaysTrue())
            .validateWith(Conditions.alwaysTrue())
            .build());

    // Do first execution
    Result result = executor.validate(context, rule, 4711);
    assertThat(result).isEqualTo(Result.ok());
    assertThat(events).containsExactly(
        new Event<>(new RuleStartedPayload(rule, Stacked.empty(), 4711),
            TestUtils.FIXED_DATE, executor),
        new Event<>(new RuleFinishedPayload(rule, Stacked.empty(), 4711, Result.ok(),
            TestUtils.FIXED_DURATION),
            TestUtils.FIXED_DATE, executor)
    );

    // Do second execution (no new events)
    assertThat(executor.validate(context, rule, 4711)).isSameAs(result);
    assertThat(events).hasSize(2);
  }

  @Test
  public void validateAsync_cachesResults() {
    Rule<Integer> rule = new DelegatingRule<>(
        RuleBuilder.conditionRule("test", Integer.class)
            .withPrecondition(Conditions.alwaysTrue())
            .validateWith(Conditions.alwaysTrue())
            .build());

    // Do both executions
    CompletableFuture<Result> resultFuture1 = executor.validateAsync(context, rule, 4711);
    CompletableFuture<Result> resultFuture2 = executor.validateAsync(context, rule, 4711);

    Result result1 = resultFuture1.join();
    Result result2 = resultFuture2.join();
    assertThat(result1).isEqualTo(Result.ok());
    assertThat(result2).isSameAs(result1);
    assertThat(events).containsExactly(
        new Event<>(new RuleStartedPayload(rule, Stacked.empty(), 4711),
            TestUtils.FIXED_DATE, executor),
        new Event<>(new RuleFinishedPayload(rule, Stacked.empty(), 4711, Result.ok(),
            TestUtils.FIXED_DURATION),
            TestUtils.FIXED_DATE, executor)
    );

    // Do execution for different object
    CompletableFuture<Result> resultFuture3 = executor.validateAsync(context, rule, 4712);
    Result result3 = resultFuture3.join();
    assertThat(result3).isEqualTo(Result.ok());
    assertThat(events).containsExactly(
        new Event<>(new RuleStartedPayload(rule, Stacked.empty(), 4711),
            TestUtils.FIXED_DATE, executor),
        new Event<>(new RuleFinishedPayload(rule, Stacked.empty(), 4711, Result.ok(),
            TestUtils.FIXED_DURATION),
            TestUtils.FIXED_DATE, executor),
        new Event<>(new RuleStartedPayload(rule, Stacked.empty(), 4712),
            TestUtils.FIXED_DATE, executor),
        new Event<>(new RuleFinishedPayload(rule, Stacked.empty(), 4712, Result.ok(),
            TestUtils.FIXED_DURATION),
            TestUtils.FIXED_DATE, executor)
    );
  }

  @Test
  public void validateAsync_asynchronouslyExecutesTheRules() {
    Rule<Integer> rule3 = new SleepyRule("3", 300);
    Rule<Integer> rule2 = new SleepyRule("2", 200);
    Rule<Integer> rule1 = new SleepyRule("1", 100);

    // Run all rules
    CompletableFuture<Result> resultFuture3 = executor.validateAsync(context, rule3, 4711);
    CompletableFuture<Result> resultFuture2 = executor.validateAsync(context, rule2, 4711);
    CompletableFuture<Result> resultFuture1 = executor.validateAsync(context, rule1, 4711);
    assertThat(resultFuture3.isDone()).isFalse();
    assertThat(resultFuture2.isDone()).isFalse();
    assertThat(resultFuture1.isDone()).isFalse();

    // Wait for them
    resultFuture1.join();
    resultFuture2.join();
    resultFuture3.join();

    // Check the order of completion
    assertThat(events).hasSize(6);
    assertThat(events.subList(3, 6)).containsExactly(
        new Event<>(new RuleFinishedPayload(rule1, Stacked.empty(), 4711, Result.ok(),
            TestUtils.FIXED_DURATION),
            TestUtils.FIXED_DATE, executor),
        new Event<>(new RuleFinishedPayload(rule2, Stacked.empty(), 4711, Result.ok(),
            TestUtils.FIXED_DURATION),
            TestUtils.FIXED_DATE, executor),
        new Event<>(new RuleFinishedPayload(rule3, Stacked.empty(), 4711, Result.ok(),
            TestUtils.FIXED_DURATION),
            TestUtils.FIXED_DATE, executor)
    );
  }

  @Test
  public void validateAsync_createsCopyOfValidationContext() {
    Rule<Integer> rule = new OkRule<>("1") {
      @Override
      public Result validate(ValidationContext context, Integer facts) {
        return context == DefaultRuleExecutorTest.this.context ? Result.failed()
            : Result.ok();
      }
    };

    // Run all rules
    assertThat(executor.validateAsync(context, rule, 4711).join()).isEqualTo(Result.ok());
  }

  private static class SleepyRule extends AbstractRule<Integer> {

    public SleepyRule(String id, long sleep) {
      super(id, Integer.class, Map.of("sleep", sleep), List.of());
    }

    @Override
    public Result validate(ValidationContext context, Integer facts) {
      try {
        Thread.sleep((Long) getMetadata().get("sleep"));
      } catch (InterruptedException ie) {
        throw new RuntimeException(ie);
      }
      return Result.ok();
    }
  }
}
