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
package de.hipphampel.validation.core.condition;

import de.hipphampel.validation.core.execution.RuleExecutor;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.path.PathResolver;
import de.hipphampel.validation.core.provider.RuleSelector;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.value.Value;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link Condition} that evaluates {@link Rule Rules} for optional given paths.
 * <p>
 * This condition calls the the {@code Rules} selected by the {@link RuleSelector} for the given
 * paths, or - in case that the paths are not set - for the object being validated.
 * <p>
 * The condition evaluates to {@code true}, if all rules evaulates to {@code ok}
 *
 * @param ruleSelector The {@code RuleSelector} to use
 * @param paths        A {@link Set} for the paths to evaluate
 */
public record RuleCondition(Value<RuleSelector> ruleSelector,
                            Value<Set<String>> paths) implements Condition {

  /**
   * Constructor.
   *
   * @param ruleSelector The {@code RuleSelector} to use
   * @param paths        A {@link Set} for the paths to evaluate
   */
  public RuleCondition {
    Objects.requireNonNull(ruleSelector);
    Objects.requireNonNull(paths);
  }

  @Override
  public boolean evaluate(ValidationContext context, Object facts) {
    RuleExecutor executor = context.getRuleExecutor();
    PathResolver resolver = context.getPathResolver();
    RuleSelector rules = this.ruleSelector.get(context, facts);
    return Optional.of(this.paths)
        .map(paths -> paths.get(context, facts))
        .filter(paths -> !paths.isEmpty())
        .map(paths -> paths.stream()
            .map(resolver::parse)
            .flatMap(path -> resolver.resolvePattern(facts, path)))
        .map(paths -> executor.validateForPaths(context, rules, facts, paths))
        .orElseGet(() -> executor.validate(context, rules, facts))
        .stream()
        .allMatch(Result::isOk);
  }
}
