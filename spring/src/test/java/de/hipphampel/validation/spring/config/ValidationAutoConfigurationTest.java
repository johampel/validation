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

import static org.assertj.core.api.Assertions.assertThat;

import de.hipphampel.validation.core.Validator;
import de.hipphampel.validation.core.event.DefaultSubscribableEventPublisher;
import de.hipphampel.validation.core.event.SubscribableEventPublisher;
import de.hipphampel.validation.core.execution.DefaultRuleExecutor;
import de.hipphampel.validation.core.execution.RuleExecutor;
import de.hipphampel.validation.core.path.BeanAccessor;
import de.hipphampel.validation.core.path.BeanPathResolver;
import de.hipphampel.validation.core.path.PathResolver;
import de.hipphampel.validation.core.path.ReflectionBeanAccessor;
import de.hipphampel.validation.core.provider.InMemoryRuleRepository;
import de.hipphampel.validation.core.provider.RuleRepository;
import de.hipphampel.validation.core.report.BooleanReporter;
import de.hipphampel.validation.core.report.ReporterFactory;
import de.hipphampel.validation.core.rule.OkRule;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.spring.provider.DefaultRuleRepositoryProvider;
import de.hipphampel.validation.spring.provider.RuleRepositoryProvider;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(classes = ValidationAutoConfigurationTest.Context.class)
@ActiveProfiles("test-strange-values")
public class ValidationAutoConfigurationTest {

  @Autowired
  private ApplicationContext context;

  @Test
  public void beanAccessor() {
    BeanAccessor beanAccessor = context.getBean(BeanAccessor.class);
    assertThat(beanAccessor).isInstanceOf(ReflectionBeanAccessor.class);
    ReflectionBeanAccessor reflectionBeanAccessor = (ReflectionBeanAccessor) beanAccessor;
    assertThat(reflectionBeanAccessor.getWhiteList().stream().map(Pattern::toString).toList())
        .containsExactly("abc.*", "def.*");
  }

  @Test
  public void pathResolver() {
    PathResolver pathResolver = context.getBean(PathResolver.class);
    assertThat(pathResolver).isInstanceOf(BeanPathResolver.class);
    BeanPathResolver beanPathResolver = (BeanPathResolver) pathResolver;
    assertThat(beanPathResolver.getSeparator()).isEqualTo(".");
    assertThat(beanPathResolver.getAllInLevel()).isEqualTo("?");
    assertThat(beanPathResolver.getManyLevels()).isEqualTo("??");
  }

  @Test
  public void ruleExecutor() {
    RuleExecutor ruleExecutor = context.getBean(RuleExecutor.class);
    assertThat(ruleExecutor).isInstanceOf(DefaultRuleExecutor.class);
  }

  @Test
  public void subscribableEventPublisher() {
    SubscribableEventPublisher subscribableEventPublisher = context.getBean(
        SubscribableEventPublisher.class);
    assertThat(subscribableEventPublisher).isInstanceOf(DefaultSubscribableEventPublisher.class);
  }

  @Test
  public void ruleRepositoryProvider() {
    RuleRepositoryProvider ruleRepositoryProvider = context.getBean(RuleRepositoryProvider.class);
    assertThat(ruleRepositoryProvider).isInstanceOf(DefaultRuleRepositoryProvider.class);
    RuleRepository ruleRepository = ruleRepositoryProvider.getRuleRepository();
    assertThat(ruleRepository.getRuleIds()).containsExactlyInAnyOrder(
        "aRule1", "aRule2", "aRule3", "aRule4", "aRule5", "aRule6"
    );
  }

  @Test
  public void validator() {
    Validator validator = context.getBean(Validator.class);
    assertThat(validator).isNotNull();
  }

  @ContextConfiguration
  @ComponentScan(basePackages = "de.hipphampel.validation.spring.config")
  @EnableAutoConfiguration
  static class Context {

    @Bean
    public Rule<?> aRule1() {
      return new OkRule<>("aRule1");
    }

    @Bean
    public Rule<?> aRule2() {
      return new OkRule<>("aRule2");
    }

    @Bean
    public RuleRepository aRuleRepository1() {
      return new InMemoryRuleRepository(new OkRule<>("aRule3"));
    }

    @Bean
    public RuleRepository aRuleRepository2() {
      return new InMemoryRuleRepository(new OkRule<>("aRule4"));
    }

    @Bean
    public ReporterFactory<Boolean> reporterFactory() {
      return BooleanReporter::new;
    }
  }

}

