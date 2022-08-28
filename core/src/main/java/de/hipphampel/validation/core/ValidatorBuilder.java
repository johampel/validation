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

public class ValidatorBuilder<T> {

  private ReporterFactory<T> reporterFactory;
  private RuleRepository ruleRepository;
  private PathResolver pathResolver;
  private RuleExecutor ruleExecutor;
  private EventPublisher eventPublisher;

  private ValidatorBuilder() {

  }

  public static ValidatorBuilder<Report> newBuilder() {
    return new ValidatorBuilder<>();
  }

  public Validator<T> build() {
    return new GenericValidator<>(
        ruleRepository == null ? new InMemoryRuleRepository() : ruleRepository,
        reporterFactory == null ? defaultReporterFactory() : reporterFactory,
        ruleExecutor == null ? new DefaultRuleExecutor() : ruleExecutor,
        eventPublisher == null ? new SubscribableEventPublisher() : eventPublisher,
        pathResolver == null ? new BeanPathResolver() : pathResolver
    );
  }

  public <S> ValidatorBuilder<S> withReporter(ReporterFactory<S> reporter) {
    this.<S>self().reporterFactory = reporter;
    return self();
  }

  public ValidatorBuilder<T> withRuleExecutor(RuleExecutor ruleExecutor) {
    this.ruleExecutor = ruleExecutor;
    return self();
  }

  public ValidatorBuilder<T> withRuleRepository(RuleRepository ruleRepository) {
    this.ruleRepository = ruleRepository;
    return self();
  }

  public ValidatorBuilder<T> withPathResolver(PathResolver pathResolver) {
    this.pathResolver = pathResolver;
    return self();
  }

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
