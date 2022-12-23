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
import de.hipphampel.validation.core.provider.RuleSelector;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@link Rule} that executes all {@code Rules} provided by the given {@link RuleSelector}.
 * <p>
 * This is a {@link ForwardingRule}, so the {@code Rule} itself has no business logic except fowarding the validation to all {@code Rules}
 * of the associated {@code RuleSelector}
 *
 * @param <T> Type of the object being validated.
 * @see RuleSelector
 */
public class SelectorRule<T> extends AbstractRule<T> implements ForwardingRule<T> {

  private final RuleSelector selector;

  /**
   * Creates an instance using the given parameters.
   *
   * @param id            The id of the {@link Rule}
   * @param factsType     {@link Class} of the objects being validated by this rule
   * @param metadata      The metadata
   * @param preconditions The list of {@linkplain Condition preconditions}
   * @param selector      The {@link RuleSelector} to use
   */
  public SelectorRule(String id, Class<? super T> factsType, Map<String, Object> metadata, List<Condition> preconditions,
      RuleSelector selector) {
    super(id, factsType, metadata, preconditions);
    this.selector = Objects.requireNonNull(selector);
  }

  /**
   * Creates an instance using the given parameters.
   *
   * @param id            The id of the {@link Rule}
   * @param metadata      The metadata
   * @param preconditions The list of {@linkplain Condition preconditions}
   * @param selector      The {@link RuleSelector} to use
   */
  public SelectorRule(String id, Map<String, Object> metadata, List<Condition> preconditions, RuleSelector selector) {
    super(id, metadata, preconditions);
    this.selector = Objects.requireNonNull(selector);
  }

  /**
   * Creates an instance using the given parameters.
   *
   * @param id            The id of the {@link Rule}
   * @param selector      The {@link RuleSelector} to use
   */
  public SelectorRule(String id, RuleSelector selector) {
    super(id);
    this.selector = Objects.requireNonNull(selector);
  }

  @Override
  public Result validate(ValidationContext context, T facts) {
    return context.getRuleExecutor().validateAsync(context, selector, facts)
        .thenApply(list -> list.stream().reduce(noRuleResult(), this::mergeResults))
        .join();
  }
}
