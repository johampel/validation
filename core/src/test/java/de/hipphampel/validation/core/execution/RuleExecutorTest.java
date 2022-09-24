package de.hipphampel.validation.core.execution;

/*-
 * #%L
 * validation-core
 * %%
 * Copyright (C) 2022 Johannes Hampel
 * %%
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
 * #L%
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.hipphampel.validation.core.condition.Conditions;
import de.hipphampel.validation.core.event.DefaultSubscribableEventPublisher;
import de.hipphampel.validation.core.path.BeanPathResolver;
import de.hipphampel.validation.core.path.Path;
import de.hipphampel.validation.core.provider.InMemoryRuleRepository;
import de.hipphampel.validation.core.provider.RuleRepository;
import de.hipphampel.validation.core.provider.RuleSelector;
import de.hipphampel.validation.core.provider.SimpleRuleSelector;
import de.hipphampel.validation.core.report.ReportReporter;
import de.hipphampel.validation.core.rule.DelegatingRule;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.rule.RuleBuilder;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RuleExecutorTest {

  private RuleExecutor underTest;

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

  @BeforeEach
  public void beforeEach() {
    underTest = spy(new RuleExecutor() {
      @Override
      public Result validate(ValidationContext context, Rule<?> rule, Object facts) {
        return Result.ok();
      }
    });
    context = spy(new ValidationContext(
        new ReportReporter(), Map.of(), underTest, ruleRepository, new BeanPathResolver(),
        new DefaultSubscribableEventPublisher()));

  }

  @Test
  public void validateForPath_forRule_callsValidate_withParentFactCallbacksOnContext() {
    Map<String, Integer> parentFacts = Map.of("a", 4711);
    // Not found case
    Path notFound = context.getPathResolver().parse("b");
    assertThat(underTest.validateForPath(context, rule1, parentFacts, notFound)).isEmpty();
    verify(context, times(0)).enterPath(any(), any());
    verify(context, times(0)).leavePath();
    verify(underTest, times(0)).validate(any(), (Rule<?>) any(), any());

    // Found case
    Path found = context.getPathResolver().parse("a");
    assertThat(underTest.validateForPath(context, rule1, parentFacts, found))
        .contains(Result.ok());
    verify(context, times(1)).enterPath(eq(parentFacts), eq(found));
    verify(context, times(1)).leavePath();
    verify(underTest, times(1)).validate(eq(context), eq(rule1), eq(4711));
  }

  @Test
  public void validateForPaths_forRule_callsValidate_withParentFactCallbacksOnContext() {
    Map<String, Integer> parentFacts = Map.of("a", 4711, "b", 4712);
    Stream<? extends Path> paths = context.getPathResolver().resolvePattern(
        parentFacts,
        context.getPathResolver().parse("*"));

    // Found case
    assertThat(underTest.validateForPaths(context, rule1, parentFacts, paths)).contains(
        Result.ok());
    verify(context, times(2)).enterPath(eq(parentFacts), any());
    verify(context, times(2)).leavePath();
    verify(underTest, times(1)).validate(eq(context), eq(rule1), eq(4711));
    verify(underTest, times(1)).validate(eq(context), eq(rule1), eq(4712));
  }

  @Test
  public void validate_forRuleSelector_callsValidate_withoutInteractingWithContext() {
    assertThat(underTest.validate(context, SimpleRuleSelector.all(), 4711))
        .containsExactly(
            Result.ok(),
            Result.ok());
    verify(context, times(0)).enterPath(any(), any());
    verify(context, times(0)).leavePath();
    verify(underTest, times(1)).validate(eq(context), eq(rule1), eq(4711));
    verify(underTest, times(1)).validate(eq(context), eq(rule2), eq(4711));
  }

  @Test
  public void validateForPath_forRuleSelector_callsValidate_withParentFactCallbacksOnContext() {
    Map<String, Integer> parentFacts = Map.of("a", 4711);
    // Not found case
    Path notFound = context.getPathResolver().parse("b");
    assertThat(
        underTest.validateForPath(context, SimpleRuleSelector.all(), parentFacts,
            notFound)).isEmpty();
    verify(context, times(0)).enterPath(any(), any());
    verify(context, times(0)).leavePath();
    verify(underTest, times(0)).validate(any(), (RuleSelector) any(), any());

    // Found case
    Path found = context.getPathResolver().parse("a");
    assertThat(underTest.validateForPath(context, SimpleRuleSelector.all(), parentFacts, found))
        .contains(List.of(Result.ok(), Result.ok()));
    verify(context, times(1)).enterPath(eq(parentFacts), eq(found));
    verify(context, times(1)).leavePath();
    verify(underTest, times(1)).validate(eq(context), eq(rule1), eq(4711));
    verify(underTest, times(1)).validate(eq(context), eq(rule2), eq(4711));
  }

  @Test
  public void validateForPaths_forRuleSelector_callsValidate_withParentFactCallbacksOnContext() {
    Map<String, Integer> parentFacts = Map.of("a", 4711, "b", 4712);
    Stream<? extends Path> paths = context.getPathResolver().resolvePattern(
        parentFacts,
        context.getPathResolver().parse("*"));

    assertThat(underTest.validateForPaths(context, SimpleRuleSelector.all(), parentFacts, paths))
        .containsExactly(Result.ok(), Result.ok(), Result.ok(), Result.ok());
    verify(context, times(2)).enterPath(eq(parentFacts), any());
    verify(context, times(2)).leavePath();
    verify(underTest, times(1)).validate(eq(context), eq(rule1), eq(4711));
    verify(underTest, times(1)).validate(eq(context), eq(rule2), eq(4711));
    verify(underTest, times(1)).validate(eq(context), eq(rule1), eq(4712));
    verify(underTest, times(1)).validate(eq(context), eq(rule2), eq(4712));
  }

  @Test
  public void validateAsync_forRule_callsValidate_withoutInteractingWithContext() {
    assertThat(underTest.validateAsync(context, rule1, 4711).join())
        .isEqualTo(Result.ok());
    verify(context, times(0)).enterPath(any(), any());
    verify(context, times(0)).leavePath();
    verify(underTest, times(1)).validate(eq(context), eq(rule1), eq(4711));
  }

  @Test
  public void validateForPathAsync_forRule_callsValidate_withParentFactCallbacksOnContext() {
    Map<String, Integer> parentFacts = Map.of("a", 4711);
    // Not found case
    Path notFound = context.getPathResolver().parse("b");
    assertThat(underTest.validateForPathAsync(context, rule1, parentFacts, notFound)).isEmpty();
    verify(context, times(0)).enterPath(any(), any());
    verify(context, times(0)).leavePath();
    verify(underTest, times(0)).validate(any(), (Rule<?>) any(), any());

    // Found case
    Path found = context.getPathResolver().parse("a");
    assertThat(underTest.validateForPathAsync(context, rule1, parentFacts, found).get().join())
        .isEqualTo(Result.ok());
    verify(context, times(1)).enterPath(eq(parentFacts), eq(found));
    verify(context, times(1)).leavePath();
    verify(underTest, times(1)).validate(eq(context), eq(rule1), eq(4711));
  }

  @Test
  public void validateForPathsAsync_forRule_callsValidate_withParentFactCallbacksOnContext() {
    Map<String, Integer> parentFacts = Map.of("a", 4711, "b", 4712);
    Stream<? extends Path> paths = context.getPathResolver().resolvePattern(
        parentFacts,
        context.getPathResolver().parse("*"));

    assertThat(underTest.validateForPathsAsync(context, rule1, parentFacts, paths).join())
        .containsExactly(Result.ok(), Result.ok());
    verify(context, times(2)).enterPath(eq(parentFacts), any());
    verify(context, times(2)).leavePath();
    verify(underTest, times(1)).validate(eq(context), eq(rule1), eq(4711));
    verify(underTest, times(1)).validate(eq(context), eq(rule1), eq(4712));
  }

  @Test
  public void validateAsync_forRuleSelector_callsValidate_withoutInteractingWithContext() {
    assertThat(underTest.validateAsync(context, SimpleRuleSelector.all(), 4711).join())
        .containsExactly(Result.ok(), Result.ok());
    verify(context, times(0)).enterPath(any(), any());
    verify(context, times(0)).leavePath();
    verify(underTest, times(1)).validate(eq(context), eq(rule1), eq(4711));
    verify(underTest, times(1)).validate(eq(context), eq(rule2), eq(4711));
  }

  @Test
  public void validateForPathAsync_forRuleSelector_callsValidate_withParentFactCallbacksOnContext() {
    Map<String, Integer> parentFacts = Map.of("a", 4711);
    // Not found case
    Path notFound = context.getPathResolver().parse("b");
    assertThat(underTest.validateForPathAsync(context, SimpleRuleSelector.all(), parentFacts,
        notFound)).isEmpty();
    verify(context, times(0)).enterPath(any(), any());
    verify(context, times(0)).leavePath();
    verify(underTest, times(0)).validate(any(), (Rule<?>) any(), any());

    // Found case
    Path found = context.getPathResolver().parse("a");
    assertThat(
        underTest.validateForPathAsync(context, SimpleRuleSelector.all(), parentFacts, found).get()
            .join())
        .isEqualTo(List.of(Result.ok(), Result.ok()));
    verify(context, times(1)).enterPath(eq(parentFacts), eq(found));
    verify(context, times(1)).leavePath();
    verify(underTest, times(1)).validate(eq(context), eq(rule1), eq(4711));
    verify(underTest, times(1)).validate(eq(context), eq(rule2), eq(4711));
  }

  @Test
  public void validateForPathsAsync_forRuleSelector_callsValidate_withParentFactCallbacksOnContext() {
    Map<String, Integer> parentFacts = Map.of("a", 4711, "b", 4712);
    Stream<? extends Path> paths = context.getPathResolver().resolvePattern(
        parentFacts,
        context.getPathResolver().parse("*"));

    assertThat(
        underTest.validateForPathsAsync(context, SimpleRuleSelector.all(), parentFacts, paths)
            .join())
        .isEqualTo(List.of(Result.ok(), Result.ok(), Result.ok(), Result.ok()));
    verify(context, times(2)).enterPath(eq(parentFacts), any());
    verify(context, times(2)).leavePath();
    verify(underTest, times(1)).validate(eq(context), eq(rule1), eq(4711));
    verify(underTest, times(1)).validate(eq(context), eq(rule2), eq(4711));
    verify(underTest, times(1)).validate(eq(context), eq(rule1), eq(4712));
    verify(underTest, times(1)).validate(eq(context), eq(rule2), eq(4712));
  }
}
