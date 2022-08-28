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

import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.rule.SystemResultReason;
import de.hipphampel.validation.core.rule.SystemResultReason.Code;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link RuleExecutor}.
 * <p>
 * This implementation extends the {@link SimpleRuleExecutor} regarding two central aspects:
 * <ul>
 *   <li>Rule execution is done in an real asyncrhonous fashion; all rule executions are done
 *   by utilizing the associated {@link ForkJoinPool}.</li>
 *   <li>It caches the results opf {@link Rule} executions. The lifetime of the cache is bound to
 *   the {@link ValidationContext}. The {@code ValidationContext} is usually constructed for each
 *   validation of an object and lives until all rules of the objects are executed</li>
 * </ul>
 *
 * @see ValidationContext
 * @see SimpleRuleExecutor
 */
public class DefaultRuleExecutor extends SimpleRuleExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRuleExecutor.class);
  private final ForkJoinPool executor;

  /**
   * Default constructor,
   * <p>
   * Creates a new instance backed by the common {@link ForkJoinPool}
   */
  public DefaultRuleExecutor() {
    this(ForkJoinPool.commonPool());
  }

  /**
   * Constructor.
   * <p>
   * Creates an instance backed by the given {@link Executor}
   *
   * @param executor The {@code Executor}
   */
  public DefaultRuleExecutor(ForkJoinPool executor) {
    this.executor = Objects.requireNonNull(executor);
  }

  @Override
  public Result validate(ValidationContext context, Rule<?> rule, Object facts) {
    try {
      return validateAsync(context, rule, facts).get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOGGER.error("Execution of rule '" + rule.getId() + "' failed", e);
      return Result.failed(
          new SystemResultReason(Code.RuleExecutionThrowsException, e.getMessage()));
    } catch (ExecutionException e) {
      LOGGER.error("Execution of rule '" + rule.getId() + "' failed", e.getCause());
      return Result.failed(
          new SystemResultReason(Code.RuleExecutionThrowsException, e.getCause().getMessage()));
    }
  }

  @Override
  public CompletableFuture<Result> validateAsync(ValidationContext context, Rule<?> rule,
      Object facts) {
    ValidationContext localContext = context.copy();
    RuleResultCache cache = localContext.getOrCreateSharedObject(
        RuleResultCache.class,
        type -> new RuleResultCache());
    return cache.getOrCompute(localContext, rule, facts)
        .thenApply(result -> addRuleResultToReporter(localContext, rule, facts, result));
  }

  private class RuleResultCache {

    private final Object NULL = new Object();
    private final Map<Object, Map<String, CompletableFuture<Result>>> cache = new ConcurrentHashMap<>();

    public CompletableFuture<Result> getOrCompute(ValidationContext context,
        Rule<?> rule,
        Object facts) {
      Map<String, CompletableFuture<Result>> resultMap = cache.computeIfAbsent(
          facts == null ? NULL : facts,
          ignore -> new ConcurrentHashMap<>());
      return resultMap.computeIfAbsent(
          rule.getId(),
          ignore -> CompletableFuture.supplyAsync(() ->
                  DefaultRuleExecutor.super.validateInternal(context, rule, facts),
              executor));
    }
  }
}
