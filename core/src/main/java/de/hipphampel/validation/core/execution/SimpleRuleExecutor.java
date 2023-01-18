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

import de.hipphampel.validation.core.condition.Condition;
import de.hipphampel.validation.core.event.EventPublisher;
import de.hipphampel.validation.core.event.payloads.RuleFinishedPayload;
import de.hipphampel.validation.core.event.payloads.RuleStartedPayload;
import de.hipphampel.validation.core.exception.RuleFailedException;
import de.hipphampel.validation.core.report.Reporter;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.rule.SystemResultReason;
import de.hipphampel.validation.core.rule.SystemResultReason.Code;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple implementation of the {@link RuleExecutor} interface.
 * <p>
 * This is a ready to use implementation of the {@code RuleExecutor} interface. In addition to the
 * basic workflow it has the following extensions/detail handling:
 * <ol>
 *   <li>For start and end of the rule execution according events are published via the {@link
 *   EventPublisher}</li>
 *   <li>If a precondition is not met or the type of the facts does not match the type exepected
 *   by the {@code Rule} lead to a {@code Result} with code {@code SKIPPED}. you may customize
 *   this behaviour by overwriting the method {@link #postprocessSkippedResult(ValidationContext,
 *   Rule, Object, Result)}</li>
 *   <li>Exceptions during rule execution are caught and translated to {@code Result} with
 *   code {@code FAILED}</li>
 * </ol>
 * <p>
 * Note that this implementation does not support asynchronous rule execution: the {@code *Async}
 * methods are just wrappers for the synchronous ones, returning an already completed future. Also,
 * it does not provide any optimizations to prevent duplicate rule execution. For a more elaborated
 * implementation see {@link DefaultRuleExecutor}
 *
 * @see DefaultRuleExecutor
 */
public class SimpleRuleExecutor implements RuleExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleRuleExecutor.class);

  @Override
  public Result validate(ValidationContext context, Rule<?> rule, Object facts) {
    ValidationContext localContext = context.copy();
    Result result = doValidate(localContext, rule, facts);
    return addRuleResultToReporter(localContext, rule, facts, result);
  }

  /**
   * Adds the {@code result} to the {@link Reporter}
   *
   * @param context The {@code ValidationContext}
   * @param rule    The {@code Rule} to execute
   * @param facts   The object to be validated
   * @param result  The {@link Result}
   * @return The {@link Result} indicating the result, same as parameter {@code result}
   */
  protected Result addRuleResultToReporter(ValidationContext context, Rule<?> rule, Object facts,
      Result result) {
    Reporter<?> reporter = context.getReporter();
    reporter.add(context, facts, context.getCurrentPath(), rule, result);
    return result;
  }

  /**
   * Internal validation.
   * <p>
   * Performs all operations required to validate {@code facts} for {@code rule}, except adding the
   * result to the {@link Reporter}.
   *
   * @param context The {@code ValidationContext}
   * @param rule    The {@code Rule} to execute
   * @param facts   The object to be validated
   * @return The {@link Result} indicating the result
   */
  protected Result doValidate(ValidationContext context, Rule<?> rule, Object facts) {
    Result result = null;
    long now = System.nanoTime();

    if (!context.enterRule(rule, facts)) {
      return Result.failed(
          new SystemResultReason(Code.CyclicRuleDependency, rule.getId()));
    }
    try {
      publishEvent(context, new RuleStartedPayload(rule, context.getPathStack(), facts));
      result = checkFactsType(rule, facts)
          .mapIfOk(toBeMapped -> checkPreconditions(context, rule, facts))
          .mapIfOk(toBeMapped -> invokeValidation(context, rule, facts))
          .mapIfSkipped(toBeMapped -> postprocessSkippedResult(context, rule, facts, toBeMapped));
    } catch (Exception e) {
      LOGGER.error("Execution of rule '" + rule.getId() + "' failed", e);
      result = Result.failed(
          new SystemResultReason(Code.RuleExecutionThrowsException, e.getMessage()));
    } finally {
      publishEvent(context, new RuleFinishedPayload(rule, context.getPathStack(),
          facts, result, System.nanoTime() - now));
      context.leaveRule();
    }
    return result;
  }

  private Result checkFactsType(Rule<?> rule, Object facts) {
    if (facts == null) {
      return Result.ok();
    }
    Class<?> expectedFactType = rule.getFactsType();
    return expectedFactType.isInstance(facts) ? Result.ok()
        : Result.skipped(new SystemResultReason(Code.FactTypeDoesNotMatchRuleType,
            "Expected type " + expectedFactType.getName() + ", but got " + facts.getClass()
                .getName()));
  }

  /**
   * Checks the preconditions of the {@code rule}.
   * <p>
   * If at least one of the preconditions is not met, a {@link Result} with code {@code SKIPPED} is
   * returned.
   *
   * @param context The {@link ValidationContext}
   * @param rule    The {@link Rule} being executed.
   * @param facts   The object being validated
   * @return The {@code Result}
   * @see #postprocessSkippedResult(ValidationContext, Rule, Object, Result)
   */
  protected Result checkPreconditions(ValidationContext context, Rule<?> rule, Object facts) {
    for (Condition condition : rule.getPreconditions()) {
      if (!checkPrecondition(context, condition, facts)) {
        return Result.skipped(new SystemResultReason(Code.PreconditionNotMet,
            "Condition '" + condition + "' not met"));
      }
    }
    return Result.ok();
  }

  private boolean checkPrecondition(ValidationContext context, Condition condition, Object facts) {
    return condition.evaluate(context, facts);
  }

  /**
   * Calls the {@code validate} method of the {@code Rule}.
   *
   * @param context The {@link ValidationContext}
   * @param rule    The {@link Rule} being executed.
   * @param facts   The object being validated
   * @return The {@code Result}
   */
  @SuppressWarnings("unchecked")
  protected Result invokeValidation(ValidationContext context, Rule<?> rule, Object facts) {
    try {
      return ((Rule<Object>) rule).validate(context, facts);
    } catch (RuleFailedException rfe) {
      return rfe.toResult();
    }
  }

  /**
   * Postprocesses the {@link Result} in case it has the code {@code SKIPPED}.
   * <p>
   * Derived classes might wish to remap this to a ok or failed code. This default implementation
   * leave the original result unchanged,
   *
   * @param context The {@link ValidationContext}
   * @param rule    The {@link Rule} being executed.
   * @param facts   The object being validated
   * @param result  The original {@code Result}
   * @return The mapped result
   */
  protected Result postprocessSkippedResult(ValidationContext context, Rule<?> rule,
      Object facts, Result result) {
    return result;
  }

  /**
   * Publishes the given {@code payload}.
   *
   * @param context The {@link ValidationContext} providing the {@link EventPublisher}
   * @param payload The payload.
   */
  protected void publishEvent(ValidationContext context, Object payload) {
    context.getEventPublisher().publish(this, payload);
  }
}
