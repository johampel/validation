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

import de.hipphampel.validation.core.provider.AggregatingRuleRepository;
import de.hipphampel.validation.core.provider.AnnotationRuleRepository;
import de.hipphampel.validation.core.provider.AnnotationRuleRepository.Handler;
import de.hipphampel.validation.core.provider.InMemoryRuleRepository;
import de.hipphampel.validation.core.provider.RuleRepository;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.spring.annotation.RuleContainer;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Default implementation of the {@code RuleRepositoryProvider}.
 * <p>
 * This implementation creates a {@link RuleRepository} that aggregates all available {@link Rule Rules} via an
 * {@link AggregatingRuleRepository}. Possible sources of these {@code Rules} are:
 * <ol>
 *   <li>Beans implementing the {@code Rule} interface. For these an {@link InMemoryRuleRepository}
 *   is created which is then part of the resulting {@code AggregatingRuleRepository}.</li>
 *   <li>Beans implementing the {@link RuleRepository} interface. These repositories are also part
 *   of the resulting {@code AggregatingRuleRepository}.</li>
 *   <li>Beans annotated with {@link RuleContainer}, which are wrapped by a {@link
 *   AnnotationRuleRepository}. These repositories are also part of the resulting
 *   {@code AggregatingRuleRepository}.</li>
 * </ol>
 *
 * @see AnnotationRuleRepository
 * @see AggregatingRuleRepository
 * @see RuleContainer
 */
public class DefaultRuleRepositoryProvider implements RuleRepositoryProvider {

  private final RuleRepository repository;

  /**
   * Constructor.
   *
   * @param ruleBeans          Beans implementing the {@link Rule} interface.
   * @param repositoryBeans    Beans implementing the {@link RuleRepository} interface.
   * @param ruleContainerBeans Beans annotated with {@link RuleContainer}
   */
  public DefaultRuleRepositoryProvider(
      List<? extends Rule<?>> ruleBeans,
      List<? extends RuleRepository> repositoryBeans,
      List<?> ruleContainerBeans) {
    this.repository = new AggregatingRuleRepository(
        Stream.of(
                List.of(new InMemoryRuleRepository(ruleBeans)),
                repositoryBeans,
                ruleContainerBeans.stream().map(AnnotationRuleRepository::ofInstance).toList())
            .flatMap(Collection::stream)
            .toList()
    );
  }

  /**
   * Constructor.
   *
   * @param annotationHandlers The {@link Handler annotation handlers} to use
   * @param ruleBeans          Beans implementing the {@link Rule} interface.
   * @param repositoryBeans    Beans implementing the {@link RuleRepository} interface.
   * @param ruleContainerBeans Beans annotated with {@link RuleContainer}
   */
  public DefaultRuleRepositoryProvider(
      List<? extends Handler<?>> annotationHandlers,
      List<? extends Rule<?>> ruleBeans,
      List<? extends RuleRepository> repositoryBeans,
      List<?> ruleContainerBeans) {
    this.repository = new AggregatingRuleRepository(
        Stream.of(
                List.of(new InMemoryRuleRepository(ruleBeans)),
                repositoryBeans,
                ruleContainerBeans.stream()
                    .map(bean -> AnnotationRuleRepository.ofInstance(bean, annotationHandlers))
                    .toList())
            .flatMap(Collection::stream)
            .toList()
    );
  }

  @Override
  public RuleRepository getRuleRepository() {
    return repository;
  }
}
