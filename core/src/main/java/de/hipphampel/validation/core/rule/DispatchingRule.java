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
package de.hipphampel.validation.core.rule;

import de.hipphampel.validation.core.condition.Condition;
import de.hipphampel.validation.core.execution.RuleExecutor;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.path.Path;
import de.hipphampel.validation.core.path.PathResolver;
import de.hipphampel.validation.core.provider.RuleSelector;
import de.hipphampel.validation.core.value.Value;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * {@link Rule} implementatiuon that dispatches the validation to subordinates {@code Rules}
 * <p>
 * The basic idea of this implementation is to call other {@code Rules} for certain {@link Path Paths}. So a {@code DispatchingRule} hold
 * as list of {@link DispatchEntry DispatchEntries}, whereas each entry consits of {@code Path} patterns and a corresponding
 * {@link RuleSelector}. Upon validation, this implementation iterates through all these entries and calls the {@code Rules} of the
 * {@code RuleSelector} for each matching {@code Path}.
 * <p>
 * Note that the {@link PathBasedRule} is related and more suitable in certain use cases.
 *
 * @param <T> Type of the objecz to validate
 * @see RuleSelector
 * @see Path
 * @see PathBasedRule
 */
public class DispatchingRule<T> extends AbstractRule<T> implements ForwardingRule<T> {

  private final List<DispatchEntry> dispatchList;

  /**
   * Constructor.
   *
   * @param id            The id of the {@link Rule}
   * @param factsType     {@link Class} of the objects being validated by this rule
   * @param metadata      The metadata
   * @param preconditions The list of {@linkplain Condition preconditions}
   * @param dispatchList  The lsit of {@link DispatchEntry DispatchEntries}
   */
  public DispatchingRule(String id, Class<? super T> factsType, Map<String, Object> metadata,
      List<Condition> preconditions, List<DispatchEntry> dispatchList) {
    super(id, factsType, metadata, preconditions);
    this.dispatchList = dispatchList;
  }

  @Override
  public Result validate(ValidationContext context, T facts) {
    List<CompletableFuture<Result>> futures = dispatchList.stream()
        .map(e -> e.validateAsync(this, context, facts))
        .toList();
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]))
        .thenApply(v -> futures.stream()
            .map(CompletableFuture::join)
            .reduce(noRuleResult(), this::mergeResults))
        .join();
  }

  /**
   * An entry describing, for which {@code paths} which |@code rules} have to be called
   *
   * @param paths The {@link Path} patterns
   * @param rules The {@link RuleSelector}
   */
  public record DispatchEntry(Value<Set<String>> paths, RuleSelector rules) {

    private CompletableFuture<Result> validateAsync(DispatchingRule<?> rule,
        ValidationContext context, Object facts) {
      RuleExecutor executor = context.getRuleExecutor();
      Stream<? extends Path> paths = getPaths(context, facts);
      return executor.validateForPathsAsync(context, rules, facts, paths)
          .thenApply(results -> results.stream().reduce(
              rule.noRuleResult(),
              rule::mergeResults
          ));
    }

    private Stream<? extends Path> getPaths(ValidationContext context, Object facts) {
      PathResolver resolver = context.getPathResolver();
      return paths.get(context, facts).stream()
          .map(resolver::parse)
          .flatMap(p -> resolver.resolvePattern(facts, p));
    }

  }

}
