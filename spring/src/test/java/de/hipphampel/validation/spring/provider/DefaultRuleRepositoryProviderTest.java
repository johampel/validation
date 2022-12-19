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
package de.hipphampel.validation.spring.provider;

import de.hipphampel.validation.core.annotations.RuleDef;
import de.hipphampel.validation.core.provider.InMemoryRuleRepository;
import de.hipphampel.validation.core.provider.RuleRepository;
import de.hipphampel.validation.core.rule.OkRule;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.spring.annotation.RuleContainer;
import de.hipphampel.validation.spring.config.ValidationConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = DefaultRuleRepositoryProviderTest.Context.class)
@ActiveProfiles("test")
public class DefaultRuleRepositoryProviderTest {

  @Autowired
  private ApplicationContext context;

  @Test
  public void getRuleRepository() {
    RuleRepositoryProvider provider =  context.getBean(RuleRepositoryProvider.class);
    assertThat(provider).isInstanceOf(DefaultRuleRepositoryProvider.class);

    RuleRepository repository = provider.getRuleRepository();
    assertThat(repository).isNotNull();
    assertThat(provider.getRuleRepository()).isSameAs(repository);

    assertThat(repository.getRuleIds()).containsExactlyInAnyOrder(
        "aRule1", "aRule2", "aRule3", "aRule4", "aRule5", "aRule6"
    );
  }

  @ContextConfiguration
  @ComponentScan(basePackages = "de.hipphampel.validation.spring.provider")
  @Import(ValidationConfiguration.class)
  @EnableAutoConfiguration
  public static class Context {

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
    @RuleContainer
    public Object aRuleContainer1() {
      return new Object() {

        @RuleDef(id = "aRule5")
        public boolean aRule5(Object obj) {
          return true;
        }

      };
    }


    @Bean
    @RuleContainer
    public Object aRuleContainer2() {
      return new Object() {

        @RuleDef(id = "aRule6")
        public boolean aRule6(Object obj) {
          return true;
        }

      };
    }
  }
}
