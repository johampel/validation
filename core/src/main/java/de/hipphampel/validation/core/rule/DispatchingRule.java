package de.hipphampel.validation.core.rule;

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
 * The basic idea of this implementatation is to call other {@code Rules} for certain
 * {@link Path Paths}. So a {@code DispatchingRule} hold as list of
 * {@link DispatchEntry DispatchEntries}, whereas each entry consits of {@code Path} patterns and a
 * corresponding {@link RuleSelector}. Upon validation, this implementation iterates through all
 * these entries and calls the {@code Rules} of the {@code RuleSelector} for each matching
 * {@code Path}.
 *
 * @param <T> Type of the objecz to validate
 * @see RuleSelector
 * @see Path
 */
public class DispatchingRule<T> extends AbstractRule<T> {

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
            .reduce(noPathResult(), this::mergeResults))
        .join();
  }

  /**
   * Called when there is not matching path to for a certain validation to valiate
   * <p>
   * By default, this returns {@link Result#ok() ok}
   *
   * @return The {@code Result}
   */
  protected Result noPathResult() {
    return Result.ok();
  }

  /**
   * Called to merge the results of two validations.
   * <p>
   * Since a {@link Rule} returns only one {@link Result} but this executes potentially more than
   * one {@code Rule}, this method is used merge two into one. The merged result has no reason by
   * default
   * <p>
   * This returns a {@code Result} with the highest severity of the two inputs.
   *
   * @param first  First {@code Result}
   * @param second Second {@code Result}
   * @return Merged {@code Result}
   */
  protected Result mergeResults(Result first, Result second) {
    return new Result(ResultCode.getHighestSeverityOf(first.code(), second.code()), null);
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
              rule.noPathResult(),
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
