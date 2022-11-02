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
package de.hipphampel.validation.spring.config;

import de.hipphampel.validation.core.Validator;
import de.hipphampel.validation.core.ValidatorBuilder;
import de.hipphampel.validation.core.event.DefaultSubscribableEventPublisher;
import de.hipphampel.validation.core.event.EventPublisher;
import de.hipphampel.validation.core.event.SubscribableEventPublisher;
import de.hipphampel.validation.core.execution.DefaultRuleExecutor;
import de.hipphampel.validation.core.execution.RuleExecutor;
import de.hipphampel.validation.core.path.BeanAccessor;
import de.hipphampel.validation.core.path.BeanPathResolver;
import de.hipphampel.validation.core.path.PathResolver;
import de.hipphampel.validation.core.path.ReflectionBeanAccessor;
import de.hipphampel.validation.core.provider.RuleRepository;
import de.hipphampel.validation.core.report.Report;
import de.hipphampel.validation.core.report.ReportReporter;
import de.hipphampel.validation.core.report.ReporterFactory;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.spring.annotation.RuleContainer;
import de.hipphampel.validation.spring.provider.DefaultRuleRepositoryProvider;
import de.hipphampel.validation.spring.provider.RuleRepositoryProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Autoconfiguration for the validation library.
 * <p>
 * This is a ready to start configuration of all beans required to create a {@link Validator}.
 * Beside the {@code Validator} itself it provides bean definitions for all subordinated components,
 * such as:
 * <ul>
 *   <li>A {@link PathResolver}, which is a {@link BeanPathResolver}</li>
 *   <li>A {@link EventPublisher}, which is a {@link DefaultSubscribableEventPublisher}</li>
 *   <li>A {@link RuleExecutor}, which is a {@link DefaultRuleExecutor}</li>
 *   <li>A {@link RuleRepository}, which is provided via the {@link DefaultRuleRepositoryProvider}</li>
 * </ul>
 * <p>
 * All these beans are only instantiated, unless the application defines its own beans.
 * <p>
 * Some of those beans can be configured via the {@link ValidationProperties}.
 *
 * @see ValidationProperties
 */
@Configuration
@EnableConfigurationProperties(ValidationProperties.class)
public class ValidationConfiguration {

  private final ValidationProperties properties;

  /**
   * Constructor
   *
   * @param properties The {@link ValidationProperties}
   */
  public ValidationConfiguration(ValidationProperties properties) {
    this.properties = properties;
  }

  /**
   * Optional {@link Validator} bean.
   *
   * @param ruleRepositoryProvider {@link RuleRepositoryProvider} providing the
   *                               {@link RuleRepository}
   * @param ruleExecutor           The {@link RuleExecutor}
   * @param eventPublisher         The {@link EventPublisher}
   * @param pathResolver           The {@link PathResolver}
   * @return The {@code Validator}
   */
  @Bean
  @Lazy
  @ConditionalOnMissingBean(Validator.class)
  public Validator validator(
      RuleRepositoryProvider ruleRepositoryProvider,
      RuleExecutor ruleExecutor,
      EventPublisher eventPublisher,
      PathResolver pathResolver) {
    return ValidatorBuilder.newBuilder()
        .withRuleExecutor(ruleExecutor)
        .withEventPublisher(eventPublisher)
        .withPathResolver(pathResolver)
        .withRuleRepository(ruleRepositoryProvider.getRuleRepository())
        .build();
  }

  /**
   * Optional {@link ReporterFactory} bean.
   * <p>
   * Returns a {@link ReporterFactory} providing a {@link Report}
   *
   * @return The {@link ReporterFactory}
   */
  @Bean
  @Lazy
  @ConditionalOnMissingBean(ReporterFactory.class)
  public ReporterFactory<Report> reportReporterFactory() {
    return ReportReporter::new;
  }

  /**
   * Optional {@link RuleRepositoryProvider} bean.
   * <p>
   * Returns a {@link DefaultRuleRepositoryProvider} unless another {@code RuleRepositoryProvider}
   * is defined in the context.
   *
   * @param ruleBeans           List of beans implementing the {@link Rule} interface.
   * @param ruleRepositoryBeans List of beans implementing the {@link RuleRepository} interface
   * @param context             {@link ApplicationContext} to look up the beans annotate with
   *                            {@link RuleContainer}
   * @return The {@code RuleRepositoryProvider}
   */
  @Bean
  @Lazy
  @ConditionalOnMissingBean(RuleRepositoryProvider.class)
  public RuleRepositoryProvider ruleRepositoryProvider(
      List<? extends Rule<?>> ruleBeans,
      List<? extends RuleRepository> ruleRepositoryBeans,
      ApplicationContext context) {
    List<?> ruleContainerBeans = new ArrayList<>(
        context.getBeansWithAnnotation(RuleContainer.class).values());
    return new DefaultRuleRepositoryProvider(ruleBeans, ruleRepositoryBeans, ruleContainerBeans);
  }

  /**
   * Optional {@link RuleExecutor} bean.
   * <p>
   * Returns a {@link DefaultRuleExecutor} unless another {@code RuleExecutor} is defined in the
   * context.
   *
   * @param executor The {@link Executor} to use
   * @return The {@code RuleExecutor}
   */
  @Bean
  @Lazy
  @ConditionalOnMissingBean(EventPublisher.class)
  public RuleExecutor ruleExecutor(Executor executor) {
    return new DefaultRuleExecutor(executor);
  }

  /**
   * Optional {@link SubscribableEventPublisher} bean.
   * <p>
   * Returns a {@link DefaultSubscribableEventPublisher} unless another
   * {@code SubscribableEventPublisher} is defined in the context.
   *
   * @return The {@code SubscribableEventPublisher}
   */
  @Bean
  @Lazy
  @ConditionalOnMissingBean(EventPublisher.class)
  public SubscribableEventPublisher subscribableEventPublisher() {
    return new DefaultSubscribableEventPublisher();
  }

  /**
   * Optional {@link PathResolver} bean.
   * <p>
   * Returns a {@link BeanPathResolver} unless another {@code PathResolver} is defined in the
   * context.
   * <p>
   * There are configuration parameters for this bean in the {@link ValidationProperties}
   *
   * @param beanAccessor The {@link BeanAccessor}
   * @return The {@code PathResolver}
   */
  @Bean
  @ConditionalOnMissingBean(PathResolver.class)
  public PathResolver pathResolver(BeanAccessor beanAccessor) {
    return new BeanPathResolver(
        properties.getPathResolver().getSeparator(),
        properties.getPathResolver().getAllInLevel(),
        properties.getPathResolver().getManyLevels(),
        beanAccessor);
  }

  /**
   * Optional {@link BeanAccessor} bean.
   * <p>
   * Returns a {@link ReflectionBeanAccessor} unless another {@code BeanAccessor} is defined in the
   * context.
   * <p>
   * There are configuration parameters for this bean in the {@link ValidationProperties}
   *
   * @return The {@code BeanAccessor}
   */
  @Bean
  @Lazy
  @ConditionalOnMissingBean(BeanAccessor.class)
  public BeanAccessor beanAccessor() {
    return new ReflectionBeanAccessor(
        properties.getPathResolver().getWhiteList());
  }
}
