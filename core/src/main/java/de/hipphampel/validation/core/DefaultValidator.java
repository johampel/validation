package de.hipphampel.validation.core;

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

import de.hipphampel.validation.core.event.EventPublisher;
import de.hipphampel.validation.core.execution.RuleExecutor;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.path.PathResolver;
import de.hipphampel.validation.core.provider.RuleRepository;
import de.hipphampel.validation.core.report.Reporter;
import java.util.Map;

/**
 * Default implementation of the {@link Validator} interface.
 */
public class DefaultValidator implements Validator {

  private final RuleRepository ruleRepository;
  private final RuleExecutor ruleExecutor;
  private final EventPublisher eventPublisher;
  private final PathResolver pathResolver;

  /**
   * Constructor.
   *
   * @param ruleRepository The {@link RuleRepository} to use.
   * @param ruleExecutor   The {@link RuleExecutor} to use.
   * @param eventPublisher The {@link EventPublisher} to use.
   * @param pathResolver   The {@link PathResolver} to use.
   */
  public DefaultValidator(RuleRepository ruleRepository, RuleExecutor ruleExecutor,
      EventPublisher eventPublisher, PathResolver pathResolver) {
    this.ruleRepository = ruleRepository;
    this.ruleExecutor = ruleExecutor;
    this.eventPublisher = eventPublisher;
    this.pathResolver = pathResolver;
  }

  @Override
  public <T> ValidationContext createValidationContext(Reporter<T> reporter,
      Map<String, Object> parameters) {
    return new ValidationContext(
        reporter,
        parameters,
        ruleExecutor,
        ruleRepository,
        pathResolver,
        eventPublisher
    );
  }
}
