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
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.path.Path;
import de.hipphampel.validation.core.path.PathResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * {@link Rule} implementation that runs some validation for several {@link Path Paths}.
 * <p>
 * In opposite to the {@link DispatchingRule}, which delegates the validation for several {@code Paths} to different {@code Rules}, this is
 * a lightweight alternative when you want to execute the validation logic for several {@code Paths}. Instead of delegating the validation
 * of a specific {@code Path} to a further {@link Rule}, this implementation simply resolves all {@code Paths} and calls the method
 * {@link #validatePath(ValidationContext, Object, Object, Path) validatePath} method of this class instead, so there is no need to
 * implement some additional rule.
 * <p>
 * The {@code validate} method first calls {@link #getPaths(ValidationContext, Object) getPaths} and then directly calls for each
 * {@code Path} that exists the {@code validatePath} method. It finally calls {@link #aggregateResult(List) aggregateResult} to form the
 * final result of this validation rule.
 * <p>
 * There are some things to keep in mind when using the class, since it makes some shortcuts due to simplicity and performance
 * considerations:
 * <ul>
 *   <li>When one of the {@code Paths} returned by {@code getPaths} does not exist, the {@code validatePath} method is not called,
 *   so this type of rule is not intended to check for path existence, you simply get nothing if the path does not exist </li>
 *   <li>All {@code Paths} are validated one after another, there is no parallelism in place. In case that your rule logic contains
 *   blocking operations, such as service calls, it might be better to use the {@link DispatchingRule} approach instead with the
 *   option to parallelize the work</li>
 *   <li>The aggregation of the rule result filters out all sub results having no reason. The final result is has the highest
 *   severity of the resuls produces by {@code validatePath}. This behaviour can be modified by overriding {@code aggregateResult}.</li>
 *   <li>There is no further interaction with the {@link ValidationContext}, esp. the methods {@code enterPath} and {@code leavePath}
 *   are NOT called.</li>
 * </ul>
 *
 * @param <T> The type of the object being validated
 * @param <P> The type the of the object the paths resolve to.
 */
public abstract class PathBasedRule<T, P> extends AbstractRule<T> {

  /**
   * Constructor
   *
   * @param id            The unique id of the rule
   * @param factsType     The {@link Class} for the facts type
   * @param metadata      Metadata of the rule
   * @param preconditions List of precondition
   */
  public PathBasedRule(String id, Class<? super T> factsType, Map<String, Object> metadata, List<Condition> preconditions) {
    super(id, factsType, metadata, preconditions);
  }

  /**
   * Constructor
   *
   * @param id            The unique id of the rule
   * @param metadata      Metadata of the rule
   * @param preconditions List of precondition
   */
  public PathBasedRule(String id, Map<String, Object> metadata, List<Condition> preconditions) {
    super(id, metadata, preconditions);
  }

  /**
   * Constructor
   *
   * @param id The unique id of the rule
   */
  public PathBasedRule(String id) {
    super(id);
  }

  @Override
  public Result validate(ValidationContext context, T facts) {
    PathResolver pathResolver = context.getPathResolver();
    List<Result> results = getPaths(context, facts)
        .flatMap(path -> pathResolver.resolve(facts, path).stream()
            .map(resolved -> validatePath(context, facts, (P) resolved, path)))
        .toList();
    return aggregateResult(results);
  }

  /**
   * Aggregates the {@code results} to one {@code Result}.
   * <p>
   * The method generates a final {@code Result}  representing the result of this rule.
   *
   * @param results The {@code Results} gathered by the {@link #validatePath(ValidationContext, Object, Object, Path) validatePath} calls.
   * @return The final {@code Result}
   */
  protected Result aggregateResult(List<Result> results) {
    ResultCode code = ResultCode.OK;
    List<ResultReason> reasons = new ArrayList<>();
    for (Result result : results) {
      code = ResultCode.getHighestSeverityOf(code, result.code());
      if (result.reason() != null) {
        result.reason().flatten().forEach(reasons::add);
      }
    }
    return new Result(code, new ListResultReason(reasons));
  }

  /**
   * Gets the {@link Path Paths} the rule should validate for.
   *
   * @param context The {@link ValidationContext}
   * @param facts   The object being validated
   * @return The {@code Paths}
   */
  protected abstract Stream<? extends Path> getPaths(ValidationContext context, T facts);

  /**
   * Runs the validation for a single {@code Path}
   *
   * @param context   The {@link ValidationContext}
   * @param facts     The object being validated
   * @param pathFacts The object the {@code path} resolves to
   * @param path      The {@link Path}
   * @return The {@link Result}
   */
  protected abstract Result validatePath(ValidationContext context, T facts, P pathFacts, Path path);
}
