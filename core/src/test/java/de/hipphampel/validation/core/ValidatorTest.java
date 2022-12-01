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
package de.hipphampel.validation.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.hipphampel.validation.core.event.DefaultSubscribableEventPublisher;
import de.hipphampel.validation.core.event.Event;
import de.hipphampel.validation.core.event.EventPublisher;
import de.hipphampel.validation.core.event.SubscribableEventPublisher;
import de.hipphampel.validation.core.event.payloads.ValidationFinishedPayload;
import de.hipphampel.validation.core.event.payloads.ValidationStartedPayload;
import de.hipphampel.validation.core.execution.RuleExecutor;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.provider.RuleSelector;
import de.hipphampel.validation.core.report.BooleanReporter;
import de.hipphampel.validation.core.report.Report;
import de.hipphampel.validation.core.report.Reporter;
import de.hipphampel.validation.core.report.ReporterFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class ValidatorTest {

  private static final Object FACTS = new Object();
  private static final RuleSelector SELECTOR = mock(RuleSelector.class);
  private static final Map<String, Object> PARAMS = Map.of("a", "b");
  private static final ReporterFactory<Boolean> REPORTER_FACTORY = obj -> new BooleanReporter(obj);

  private ValidationContext context;
  private RuleExecutor ruleExecutor;
  private SubscribableEventPublisher eventPublisher;
  private Validator validator;

  private List<Event<?>> events;

  @BeforeEach
  public void beforeEach() {
    eventPublisher = new DefaultSubscribableEventPublisher();
    events = new ArrayList<>();
    eventPublisher.subscribe(events::add);
    ruleExecutor = mock(RuleExecutor.class);
    context = mock(ValidationContext.class);
    when(context.getRuleExecutor()).thenReturn(ruleExecutor);
    when(context.getEventPublisher()).thenReturn(eventPublisher);
    validator = spy(new TestValidator());
  }

  @Test
  public void validate_sendEvents() {
    validator.validate(FACTS, SELECTOR);
    assertThat(events).hasSize(2);
    assertThat(events.get(0).source()).isEqualTo(validator);
    assertThat(events.get(0).payload()).isEqualTo(new ValidationStartedPayload(FACTS));
    assertThat(events.get(1).source()).isEqualTo(validator);
    assertThat(events.get(1).payload()).isInstanceOf(ValidationFinishedPayload.class);
    assertThat(((ValidationFinishedPayload<?>)events.get(1).payload()).facts()).isEqualTo(FACTS);
    assertThat(((ValidationFinishedPayload<?>)events.get(1).payload()).error()).isNull();
    assertThat(((ValidationFinishedPayload<?>)events.get(1).payload()).result()).isEqualTo(new Report(Set.of()));
    events.clear();
    Mockito.reset(validator, ruleExecutor);

    validator.validate(REPORTER_FACTORY, FACTS, SELECTOR);
    assertThat(events).hasSize(2);
    assertThat(events.get(0).source()).isEqualTo(validator);
    assertThat(events.get(0).payload()).isEqualTo(new ValidationStartedPayload(FACTS));
    assertThat(events.get(1).source()).isEqualTo(validator);
    assertThat(events.get(1).payload()).isInstanceOf(ValidationFinishedPayload.class);
    assertThat(((ValidationFinishedPayload<?>)events.get(1).payload()).facts()).isEqualTo(FACTS);
    assertThat(((ValidationFinishedPayload<?>)events.get(1).payload()).error()).isNull();
    assertThat(((ValidationFinishedPayload<?>)events.get(1).payload()).result()).isEqualTo(true);
    events.clear();
    Mockito.reset(validator, ruleExecutor);

    validator.validate(FACTS, SELECTOR, PARAMS);
    assertThat(events).hasSize(2);
    assertThat(events.get(0).source()).isEqualTo(validator);
    assertThat(events.get(0).payload()).isEqualTo(new ValidationStartedPayload(FACTS));
    assertThat(events.get(1).source()).isEqualTo(validator);
    assertThat(events.get(1).payload()).isInstanceOf(ValidationFinishedPayload.class);
    assertThat(((ValidationFinishedPayload<?>)events.get(1).payload()).facts()).isEqualTo(FACTS);
    assertThat(((ValidationFinishedPayload<?>)events.get(1).payload()).error()).isNull();
    assertThat(((ValidationFinishedPayload<?>)events.get(1).payload()).result()).isEqualTo(new Report(Set.of()));
    events.clear();
    Mockito.reset(validator, ruleExecutor);

    validator.validate(REPORTER_FACTORY, FACTS, SELECTOR, PARAMS);
    assertThat(events).hasSize(2);
    assertThat(events.get(0).source()).isEqualTo(validator);
    assertThat(events.get(0).payload()).isEqualTo(new ValidationStartedPayload(FACTS));
    assertThat(events.get(1).source()).isEqualTo(validator);
    assertThat(events.get(1).payload()).isInstanceOf(ValidationFinishedPayload.class);
    assertThat(((ValidationFinishedPayload<?>)events.get(1).payload()).facts()).isEqualTo(FACTS);
    assertThat(((ValidationFinishedPayload<?>)events.get(1).payload()).error()).isNull();
    assertThat(((ValidationFinishedPayload<?>)events.get(1).payload()).result()).isEqualTo(true);
    events.clear();
    Mockito.reset(validator, ruleExecutor);
  }

  @Test
  public void validate_dispatchesToCorrectMethod() {
    validator.validate(FACTS, SELECTOR);
    verify(validator, times(1)).validate(any(), same(FACTS), same(SELECTOR), eq(Map.of()));
    verify(validator, times(1)).createValidationContext(any(), eq(Map.of()));
    verify(ruleExecutor, times(1)).validate(any(), same(SELECTOR), same(FACTS));
    Mockito.reset(validator, ruleExecutor);

    validator.validate(REPORTER_FACTORY, FACTS, SELECTOR);
    verify(validator, times(1)).validate(same(REPORTER_FACTORY), same(FACTS), same(SELECTOR), eq(Map.of()));
    verify(validator, times(1)).createValidationContext(any(), eq(Map.of()));
    verify(ruleExecutor, times(1)).validate(any(), same(SELECTOR), same(FACTS));
    Mockito.reset(validator, ruleExecutor);

    validator.validate(FACTS, SELECTOR, PARAMS);
    verify(validator, times(1)).validate(any(), same(FACTS), same(SELECTOR), same(PARAMS));
    verify(validator, times(1)).createValidationContext(any(), same(PARAMS));
    verify(ruleExecutor, times(1)).validate(any(), same(SELECTOR), same(FACTS));
    Mockito.reset(validator, ruleExecutor);

    validator.validate(REPORTER_FACTORY, FACTS, SELECTOR, PARAMS);
    verify(validator, times(1)).validate(same(REPORTER_FACTORY), same(FACTS), same(SELECTOR), same(PARAMS));
    verify(validator, times(1)).createValidationContext(any(), same(PARAMS));
    verify(ruleExecutor, times(1)).validate(any(), same(SELECTOR), same(FACTS));
    Mockito.reset(validator, ruleExecutor);
  }

  @Test
  public void validateAsync_dispatchesToCorrectMethod() {
    when(ruleExecutor.validateAsync(any(), any(RuleSelector.class), any())).thenReturn(
        CompletableFuture.completedFuture(null));
    validator.validateAsync(FACTS, SELECTOR);
    verify(validator, times(1)).validateAsync(any(), same(FACTS), same(SELECTOR), eq(Map.of()));
    verify(validator, times(1)).createValidationContext(any(), eq(Map.of()));
    verify(ruleExecutor, times(1)).validateAsync(any(), same(SELECTOR), same(FACTS));
    Mockito.reset(validator, ruleExecutor);

    when(ruleExecutor.validateAsync(any(), any(RuleSelector.class), any())).thenReturn(
        CompletableFuture.completedFuture(null));
    validator.validateAsync(REPORTER_FACTORY, FACTS, SELECTOR);
    verify(validator, times(1)).validateAsync(same(REPORTER_FACTORY), same(FACTS), same(SELECTOR), eq(Map.of()));
    verify(validator, times(1)).createValidationContext(any(), eq(Map.of()));
    verify(ruleExecutor, times(1)).validateAsync(any(), same(SELECTOR), same(FACTS));
    Mockito.reset(validator, ruleExecutor);

    when(ruleExecutor.validateAsync(any(), any(RuleSelector.class), any())).thenReturn(
        CompletableFuture.completedFuture(null));
    validator.validateAsync(FACTS, SELECTOR, PARAMS);
    verify(validator, times(1)).validateAsync(any(), same(FACTS), same(SELECTOR), same(PARAMS));
    verify(validator, times(1)).createValidationContext(any(), same(PARAMS));
    verify(ruleExecutor, times(1)).validateAsync(any(), same(SELECTOR), same(FACTS));
    Mockito.reset(validator, ruleExecutor);

    when(ruleExecutor.validateAsync(any(), any(RuleSelector.class), any())).thenReturn(
        CompletableFuture.completedFuture(null));
    validator.validateAsync(REPORTER_FACTORY, FACTS, SELECTOR, PARAMS);
    verify(validator, times(1)).validateAsync(same(REPORTER_FACTORY), same(FACTS), same(SELECTOR), same(PARAMS));
    verify(validator, times(1)).createValidationContext(any(), same(PARAMS));
    verify(ruleExecutor, times(1)).validateAsync(any(), same(SELECTOR), same(FACTS));
    Mockito.reset(validator, ruleExecutor);
  }

  @Test
  public void validateAsync_sendEvents() {
    when(ruleExecutor.validateAsync(any(), any(RuleSelector.class), any())).thenReturn(
        CompletableFuture.completedFuture(null));
    validator.validateAsync(FACTS, SELECTOR).join();
    assertThat(events).hasSize(2);
    assertThat(events.get(0).source()).isEqualTo(validator);
    assertThat(events.get(0).payload()).isEqualTo(new ValidationStartedPayload(FACTS));
    assertThat(events.get(1).source()).isEqualTo(validator);
    assertThat(events.get(1).payload()).isInstanceOf(ValidationFinishedPayload.class);
    assertThat(((ValidationFinishedPayload<?>)events.get(1).payload()).facts()).isEqualTo(FACTS);
    assertThat(((ValidationFinishedPayload<?>)events.get(1).payload()).error()).isNull();
    assertThat(((ValidationFinishedPayload<?>)events.get(1).payload()).result()).isEqualTo(new Report(Set.of()));
    events.clear();
    Mockito.reset(validator, ruleExecutor);

    when(ruleExecutor.validateAsync(any(), any(RuleSelector.class), any())).thenReturn(
        CompletableFuture.completedFuture(null));
    validator.validateAsync(REPORTER_FACTORY, FACTS, SELECTOR).join();
    assertThat(events).hasSize(2);
    assertThat(events.get(0).source()).isEqualTo(validator);
    assertThat(events.get(0).payload()).isEqualTo(new ValidationStartedPayload(FACTS));
    assertThat(events.get(1).source()).isEqualTo(validator);
    assertThat(events.get(1).payload()).isInstanceOf(ValidationFinishedPayload.class);
    assertThat(((ValidationFinishedPayload<?>)events.get(1).payload()).facts()).isEqualTo(FACTS);
    assertThat(((ValidationFinishedPayload<?>)events.get(1).payload()).error()).isNull();
    assertThat(((ValidationFinishedPayload<?>)events.get(1).payload()).result()).isEqualTo(true);
    events.clear();
    Mockito.reset(validator, ruleExecutor);

    when(ruleExecutor.validateAsync(any(), any(RuleSelector.class), any())).thenReturn(
        CompletableFuture.completedFuture(null));
    validator.validateAsync(FACTS, SELECTOR, PARAMS).join();
    assertThat(events).hasSize(2);
    assertThat(events.get(0).source()).isEqualTo(validator);
    assertThat(events.get(0).payload()).isEqualTo(new ValidationStartedPayload(FACTS));
    assertThat(events.get(1).source()).isEqualTo(validator);
    assertThat(events.get(1).payload()).isInstanceOf(ValidationFinishedPayload.class);
    assertThat(((ValidationFinishedPayload<?>)events.get(1).payload()).facts()).isEqualTo(FACTS);
    assertThat(((ValidationFinishedPayload<?>)events.get(1).payload()).error()).isNull();
    assertThat(((ValidationFinishedPayload<?>)events.get(1).payload()).result()).isEqualTo(new Report(Set.of()));
    events.clear();
    Mockito.reset(validator, ruleExecutor);

    when(ruleExecutor.validateAsync(any(), any(RuleSelector.class), any())).thenReturn(
        CompletableFuture.completedFuture(null));
    validator.validateAsync(REPORTER_FACTORY, FACTS, SELECTOR, PARAMS).join();
    assertThat(events).hasSize(2);
    assertThat(events.get(0).source()).isEqualTo(validator);
    assertThat(events.get(0).payload()).isEqualTo(new ValidationStartedPayload(FACTS));
    assertThat(events.get(1).source()).isEqualTo(validator);
    assertThat(events.get(1).payload()).isInstanceOf(ValidationFinishedPayload.class);
    assertThat(((ValidationFinishedPayload<?>)events.get(1).payload()).facts()).isEqualTo(FACTS);
    assertThat(((ValidationFinishedPayload<?>)events.get(1).payload()).error()).isNull();
    assertThat(((ValidationFinishedPayload<?>)events.get(1).payload()).result()).isEqualTo(true);
    events.clear();
    Mockito.reset(validator, ruleExecutor);
  }
  private class TestValidator implements Validator {

    @Override
    public <T> ValidationContext createValidationContext(Reporter<T> reporter,
        Map<String, Object> parameters) {
      return context;
    }
  }
}
