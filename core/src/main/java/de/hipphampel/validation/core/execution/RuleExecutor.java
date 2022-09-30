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

import de.hipphampel.validation.core.path.Path;
import de.hipphampel.validation.core.path.PathResolver;
import de.hipphampel.validation.core.provider.RuleSelector;
import de.hipphampel.validation.core.report.Reporter;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.ResultCode;
import de.hipphampel.validation.core.rule.Rule;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Controls the execution of  {@link Rule Rules}.
 * <p>
 * This class provides several methods to execute one or more {@code Rules}. Basically, all
 * validation methods come in two forms: a synchronous version and an asynchronous one.
 * <p>
 * The general flow of executing a single {@code Rule} is as follows: First, it is checked, whether
 * all {@link Rule#getPreconditions() preconditions} of the {@code Rule} are fulfilled, if this is
 * not the case, a {@link Result} with code {@link ResultCode#SKIPPED} or {@link ResultCode#FAILED}
 * is returned. Otherwise the {@link Rule#validate(ValidationContext, Object) validate} method is
 * called and its result returned. Concrete implementations might extend this basic flow, but in
 * general, the contract outline above must be fulfilled.
 * <p>
 * Any result of a rule has to be reported to the {@link Reporter} that is accessible via the
 * {@link ValidationContext}.
 * <p>
 * Based on this single rule execution there are several further methods, that allow to execute
 * multiple {@code Rules} or execute the validation on some sub objects of a given one.
 * <p>
 * This interface has a default implementation for several methods, concrete implementation must
 * implement {@link #validate(ValidationContext, Rule, Object)} and should implement
 * {@link #validateAsync(ValidationContext, Rule, Object)}
 *
 * @see Rule
 * @see Result
 */
public interface RuleExecutor {

  /**
   * Validates {@code facts} for the {@link Rule}.
   * <p>
   * A concrete implementation must follow at least the general workflow outlined in the
   * {@link RuleExecutor class description}. The execution is done synchronously, meaning that the
   * method blocks, until the result is computed; but there is no guarantee that the execution is
   * done in the same thread.
   *
   * @param context The {@code ValidationContext}
   * @param rule    The {@code Rule} to execute
   * @param facts   The object to be validated
   * @return The {@link Result} indicating the result
   * @see #validateAsync(ValidationContext, Rule, Object)
   */
  Result validate(ValidationContext context, Rule<?> rule, Object facts);

  /**
   * Resolves {@code path} for {@code parentFacts} and validates the result for the {@code rule}.
   * <p>
   * So technically, this is basically the combination of the following two steps:
   * <ol>
   *   <li>Use the {@link PathResolver} to resolve {@code path} on {@code parentFacts}</li>
   *   <li>Call {@link #validate(ValidationContext, Rule, Object) validate} for the object being
   *   found in the first step</li>
   * </ol>
   *
   * <b>Important note:</b> {@code path} needs to be a concrete path; the method cannot deal with
   * patterns
   *
   * @param context     The {@code ValidationContext}
   * @param rule        The {@code Rule} to execute
   * @param parentFacts The object containing the object to validate
   * @param path        The {@link Path} to resolve the object to be resolved.
   * @return {@code Optional}, which is empty if the {@code path} does not exist or otherwise
   * contains the validation result
   * @see #validate(ValidationContext, Rule, Object)
   */
  default Optional<Result> validateForPath(ValidationContext context, Rule<?> rule,
      Object parentFacts, Path path) {
    return validateForPath(context, parentFacts, path, facts -> validate(context, rule, facts));
  }

  /**
   * Calls {@link #validateForPath(ValidationContext, Rule, Object, Path) validateForPath} for each
   * element of {@code paths}.
   *
   * <b>Important note:</b> {@code paths} needs to be a concrete paths; the method cannot deal with
   * patterns
   *
   * @param context     The {@code ValidationContext}
   * @param rule        The {@code Rule} to execute
   * @param parentFacts The object containing the object to validate
   * @param paths       The {@code Paths} to validate for
   * @return The list of {@link Result Results} (only results for existing paths are reported)
   * @see #validateForPath(ValidationContext, Rule, Object, Path)
   */
  default List<Result> validateForPaths(ValidationContext context, Rule<?> rule,
      Object parentFacts, Stream<? extends Path> paths) {
    return paths
        .map(path -> validateForPath(context, rule, parentFacts, path))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  /**
   * Validates {@code facts} for all the {@link Rule rules} selected by {@code selector}.
   * <p>
   * This implementation is effectively the same like calling
   * {@link #validate(ValidationContext, Rule, Object) validate} for each {@code Rule} the
   * {@code selector} selects. The execution is done synchronously, meaning that the method blocks,
   * until the result is computed; but there is no guarantee that the execution is done in the same
   * thread.
   *
   * @param context  The {@code ValidationContext}
   * @param selector The {@link RuleSelector} to use
   * @param facts    The object to be validated
   * @return The {@link Result  RuleResults} indicating the result
   * @see #validate(ValidationContext, Rule, Object)
   */
  default List<Result> validate(ValidationContext context, RuleSelector selector,
      Object facts) {
    List<Rule<?>> rules = selector.selectRules(context.getRuleProvider(), context, facts);
    return rules.stream()
        .map(rule -> validate(context, rule, facts))
        .collect(Collectors.toList());
  }

  /**
   * Resolves {@code path} for {@code parentFacts} and validates the result for the
   * {@code selector}.
   * <p>
   * So technically, this is basically the combination of the following two steps:
   * <ol>
   *   <li>Use the {@link PathResolver} to resolve {@code path} on {@code parentFacts}</li>
   *   <li>Call {@link #validate(ValidationContext, RuleSelector, Object) validate} for the object being
   *   found in the first step</li>
   * </ol>
   *
   *
   * <b>Important note:</b> {@code path} needs to be a concrete path; the method cannot deal with
   * patterns
   *
   * @param context     The {@code ValidationContext}
   * @param selector    The {@link RuleSelector} to use
   * @param parentFacts The object containing the object to validate
   * @param path        The {@link Path} to resolve the object to be resolved.
   * @return {@code Optional}, which is empty if the {@code path} does not exist or otherwise
   * contains the validation result
   * @see #validate(ValidationContext, RuleSelector, Object)
   */
  default Optional<List<Result>> validateForPath(ValidationContext context, RuleSelector selector,
      Object parentFacts, Path path) {
    return validateForPath(context, parentFacts, path, facts -> validate(context, selector, facts));
  }

  /**
   * Calls {@link #validateForPath(ValidationContext, RuleSelector, Object, Path) validateForPath}
   * for each element of {@code paths}.
   *
   * <b>Important note:</b> {@code path} needs to be a concrete path; the method cannot deal with
   * patterns
   *
   * @param context     The {@code ValidationContext}
   * @param selector    The {@link RuleSelector} to use
   * @param parentFacts The object containing the object to validate
   * @param paths       The {@code Paths} to validate for
   * @return The list of {@link Result Results} (only results for existing paths are reported)
   * @see #validateForPath(ValidationContext, RuleSelector, Object, Path)
   */
  default List<Result> validateForPaths(ValidationContext context, RuleSelector selector,
      Object parentFacts, Stream<? extends Path> paths) {
    return paths
        .map(path -> validateForPath(context, selector, parentFacts, path))
        .filter(Optional::isPresent)
        .flatMap(listOpt -> listOpt.get().stream())
        .toList();
  }

  /**
   * Asynchronously validates {@code facts} for the {@link Rule}.
   * <p>
   * This default implementation just returns a completed {@link CompletableFuture} with the result
   * of {@link #validate(ValidationContext, Rule, Object)}; concrete implementation might perform a
   * real asynchronous validation.
   *
   * @param context The {@code ValidationContext}
   * @param rule    The {@code Rule} to execute
   * @param facts   The object to be validated
   * @return The {@link Result} indicating the result
   * @see #validate(ValidationContext, Rule, Object)
   */
  default CompletableFuture<Result> validateAsync(ValidationContext context, Rule<?> rule,
      Object facts) {
    return CompletableFuture.completedFuture(validate(context, rule, facts));
  }

  /**
   * Resolves {@code path} for {@code parentFacts} and asynchronously validates the result for the
   * {@code rule}.
   * <p>
   * So technically, this is basically the combination of the following two steps:
   * <ol>
   *   <li>Use the {@link PathResolver} to resolve {@code path} on {@code parentFacts}</li>
   *   <li>Call {@link #validateAsync(ValidationContext, Rule, Object) validateAsync} for the object being
   *   found in the first step</li>
   * </ol>
   *
   * <b>Important note:</b> {@code path} needs to be a concrete path; the method cannot deal with
   * patterns
   *
   * @param context     The {@code ValidationContext}
   * @param rule        The {@link Rule} to use
   * @param parentFacts The object containing the object to validate
   * @param path        The {@link Path} to resolve the object to be resolved.
   * @return {@code Optional}, which is empty if the {@code path} does not exist or otherwise
   * contains the validation result
   * @see #validateAsync(ValidationContext, Rule, Object)
   */
  default Optional<CompletableFuture<Result>> validateForPathAsync(ValidationContext context,
      Rule<?> rule, Object parentFacts, Path path) {
    return validateForPath(context, parentFacts, path,
        facts -> validateAsync(context, rule, facts));
  }

  /**
   * Calls {@link #validateForPathAsync(ValidationContext, Rule, Object, Path) validateForPathAsync}
   * for each element of {@code paths}.
   *
   * <b>Important note:</b> {@code path} needs to be a concrete path; the method cannot deal with
   * patterns
   *
   * @param context     The {@code ValidationContext}
   * @param rule        The {@link Rule} to use
   * @param parentFacts The object containing the object to validate
   * @param paths       The {@code Paths} to validate for
   * @return The list of {@link Result Results} (only results for existing paths are reported)
   * @see #validateForPathAsync(ValidationContext, Rule, Object, Path)
   */
  default CompletableFuture<List<Result>> validateForPathsAsync(ValidationContext context,
      Rule<?> rule, Object parentFacts, Stream<? extends Path> paths) {
    List<CompletableFuture<Result>> futures = paths
        .map(path -> validateForPathAsync(context, rule, parentFacts, path))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]))
        .thenApply(v -> futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList()));
  }

  /**
   * Asynchronously validates {@code facts} for all the {@link Rule rules} selected by
   * {@code selector}.
   * <p>
   * This implementation is effectively the same like calling
   * {@link #validate(ValidationContext, RuleSelector, Object) validate} for each {@code Rule} the
   * {@code selector} selects. The execution of each {@code Rule} is done asynchronously, by calling
   * {@linkplain #validateAsync(ValidationContext, Rule, Object) validateAsync} for each of them.
   *
   * @param context  The {@code ValidationContext}
   * @param selector The {@link RuleSelector} to use
   * @param facts    The object to be validated
   * @return The {@link Result RuleResults} indicating the result
   * @see #validate(ValidationContext, Rule, Object)
   */
  default CompletableFuture<List<Result>> validateAsync(ValidationContext context,
      RuleSelector selector, Object facts) {
    List<Rule<?>> rules = selector.selectRules(context.getRuleProvider(), context, facts);
    List<CompletableFuture<Result>> futures = rules.stream()
        .map(rule -> validateAsync(context, rule, facts))
        .toList();
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]))
        .thenApply(v -> futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList()));
  }

  /**
   * Resolves {@code path} for {@code parentFacts} and asynchronously validates the result for the
   * {@code selector}.
   * <p>
   * So technically, this is basically the combination of the following two steps:
   * <ol>
   *   <li>Use the {@link PathResolver} to resolve {@code path} on {@code parentFacts}</li>
   *   <li>Call {@link #validateAsync(ValidationContext, RuleSelector, Object) validateAsync} for
   *   the object being found in the first step</li>
   * </ol>
   *
   * <b>Important note:</b> {@code path} needs to be a concrete path; the method cannot deal with
   * patterns
   *
   * @param context     The {@code ValidationContext}
   * @param selector    The {@link RuleSelector} to use
   * @param parentFacts The object containing the object to validate
   * @param path        The {@link Path} to resolve the object to be resolved.
   * @return {@code Optional}, which is empty if the {@code path} does not exist or otherwise
   * contains the validation result
   * @see #validateAsync(ValidationContext, RuleSelector, Object)
   */
  default Optional<CompletableFuture<List<Result>>> validateForPathAsync(ValidationContext context,
      RuleSelector selector, Object parentFacts, Path path) {
    return validateForPath(context, parentFacts, path,
        facts -> validateAsync(context, selector, facts));
  }

  /**
   * Calls
   * {@link #validateForPathAsync(ValidationContext, RuleSelector, Object, Path)
   * validateForPathAsync} for each element of {@code paths}.
   *
   * <b>Important note:</b> {@code paths} needs to be concrete paths; the method cannot deal with
   * patterns
   *
   * @param context     The {@code ValidationContext}
   * @param selector    The {@link RuleSelector} to use
   * @param parentFacts The object containing the object to validate
   * @param paths       The {@code Paths} to validate for
   * @return The list of {@link Result Results} (only results for existing paths are reported)
   * @see #validateForPathAsync(ValidationContext, RuleSelector, Object, Path)
   */
  default CompletableFuture<List<Result>> validateForPathsAsync(ValidationContext context,
      RuleSelector selector, Object parentFacts, Stream<? extends Path> paths) {
    List<CompletableFuture<List<Result>>> futures = paths
        .map(path -> validateForPathAsync(context, selector, parentFacts, path))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]))
        .thenApply(v -> futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .collect(Collectors.toList()));
  }

  private <T> Optional<T> validateForPath(ValidationContext context, Object parentFacts,
      Path path, Function<Object, T> validations) {
    return context.getPathResolver().resolve(parentFacts, path)
        .map(facts -> {
          try {
            context.enterPath(parentFacts, path);
            return Optional.of(validations.apply(facts));
          } finally {
            context.leavePath();
          }
        })
        .orElse(Optional.empty());
  }
}
