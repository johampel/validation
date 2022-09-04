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
import de.hipphampel.validation.core.event.SubscribableEventPublisher;
import de.hipphampel.validation.core.execution.DefaultRuleExecutor;
import de.hipphampel.validation.core.execution.RuleExecutor;
import de.hipphampel.validation.core.path.BeanPathResolver;
import de.hipphampel.validation.core.path.PathResolver;
import de.hipphampel.validation.core.provider.InMemoryRuleRepository;
import de.hipphampel.validation.core.provider.RuleRepository;
import de.hipphampel.validation.core.report.Report;
import de.hipphampel.validation.core.report.ReportReporter;
import de.hipphampel.validation.core.report.Reporter;
import de.hipphampel.validation.core.report.ReporterFactory;


/**
 * Builder for a {@link Validator}.
 * <p>
 * This class is used as follows (in the most somple case):
 * <pre><tt>
 *   Validato&lt;Report> validator = ValidatorBuilder.newBuilder().build();
 * </tt></pre>
 * which basically constructs a {@link DefaultValidator} with the standard settings. Using the
 * different {@code with*} methods, one may customize the generated {@code Validator}.
 * <p>
 * Teh default settings of the generated {@code Validator} are as follows:
 * <ul>
 *   <li>The {@link ReporterFactory} is a factory for a {@link ReportReporter}</li>
 *   <li>The {@link RuleRepository} is an empty {@link InMemoryRuleRepository}</li>
 *   <li>The {@link PathResolver} is a {@link BeanPathResolver}</li>
 *   <li>The {@link RuleExecutor} is a {@link DefaultRuleExecutor}</li>
 *   <li>The {@link EventPublisher} is a {@link SubscribableEventPublisher}</li>
 * </ul>
 *
 * @param <T> Type of teh report to return
 */
public class ValidatorBuilder<T> {

  private ReporterFactory<T> reporterFactory;
  private RuleRepository ruleRepository;
  private PathResolver pathResolver;
  private RuleExecutor ruleExecutor;
  private EventPublisher eventPublisher;

  private ValidatorBuilder() {

  }

  /**
   * Creates a new {@link ValidatorBuilder}.
   * <p>
   * Using the different {@code with*} methods the validator can be customized; its production is
   * finalized by calling {@link #build()}
   *
   * @return The {@code ValidatorBuilder}
   */
  public static ValidatorBuilder<Report> newBuilder() {
    return new ValidatorBuilder<>();
  }


  /**
   * Finalizes the production of a {@link Validator}.
   *
   * @return The final {@code Validator}
   */
  public Validator<T> build() {
    return new DefaultValidator<>(
        ruleRepository == null ? new InMemoryRuleRepository() : ruleRepository,
        reporterFactory == null ? defaultReporterFactory() : reporterFactory,
        ruleExecutor == null ? new DefaultRuleExecutor() : ruleExecutor,
        eventPublisher == null ? new SubscribableEventPublisher() : eventPublisher,
        pathResolver == null ? new BeanPathResolver() : pathResolver
    );
  }

  /**
   * Specifies the {@link ReporterFactory} to use.
   *
   * @param reporterFactory The {@code ReporterFactory}
   * @param <S>             The type of the {@code Reporter}
   * @return This instance
   */
  public <S> ValidatorBuilder<S> withReporter(ReporterFactory<S> reporterFactory) {
    this.<S>self().reporterFactory = reporterFactory;
    return self();
  }

  /**
   * Specifies the {@link RuleExecutor} to use.
   *
   * @param ruleExecutor The {@code RuleExecutor}
   * @return This instance
   */
  public ValidatorBuilder<T> withRuleExecutor(RuleExecutor ruleExecutor) {
    this.ruleExecutor = ruleExecutor;
    return self();
  }

  /**
   * Specifies the {@link RuleRepository} to use.
   *
   * @param ruleRepository The {@code RuleRepository}
   * @return This instance
   */
  public ValidatorBuilder<T> withRuleRepository(RuleRepository ruleRepository) {
    this.ruleRepository = ruleRepository;
    return self();
  }

  /**
   * Specifies the {@link PathResolver} to use.
   *
   * @param pathResolver The {@code PathResolver}
   * @return This instance
   */
  public ValidatorBuilder<T> withPathResolver(PathResolver pathResolver) {
    this.pathResolver = pathResolver;
    return self();
  }

  /**
   * Specifies the {@link EventPublisher} to use.
   *
   * @param eventPublisher The {@code EventPublisher}
   * @return This instance
   */
  public ValidatorBuilder<T> withEventPublisher(EventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
    return self();
  }

  @SuppressWarnings("unchecked")
  private <S> ValidatorBuilder<S> self() {
    return (ValidatorBuilder<S>) this;
  }

  @SuppressWarnings("unchecked")
  private ReporterFactory<T> defaultReporterFactory() {
    return () -> (Reporter<T>) new ReportReporter();
  }
}
