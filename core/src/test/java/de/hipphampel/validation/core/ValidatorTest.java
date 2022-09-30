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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.hipphampel.validation.core.execution.RuleExecutor;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.provider.RuleSelector;
import de.hipphampel.validation.core.report.BooleanReporter;
import de.hipphampel.validation.core.report.Reporter;
import de.hipphampel.validation.core.report.ReporterFactory;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ValidatorTest {

  private static final Object FACTS = new Object();
  private static final RuleSelector SELECTOR = mock(RuleSelector.class);
  private static final Map<String, Object> PARAMS = Map.of("a", "b");
  private static final ReporterFactory<Boolean> REPORTER_FACTORY = BooleanReporter::new;

  private ValidationContext context;
  private RuleExecutor ruleExecutor;
  private Validator validator;

  @BeforeEach
  public void beforeEach() {
    ruleExecutor = mock(RuleExecutor.class);
    context = mock(ValidationContext.class);
    when(context.getRuleExecutor()).thenReturn(ruleExecutor);
    validator = spy(new TestValidator());
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

  private class TestValidator implements Validator {

    @Override
    public <T> ValidationContext createValidationContext(Reporter<T> reporter,
        Map<String, Object> parameters) {
      return context;
    }
  }
}
