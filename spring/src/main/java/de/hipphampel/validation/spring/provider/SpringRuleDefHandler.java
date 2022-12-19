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

import de.hipphampel.validation.core.condition.Condition;
import de.hipphampel.validation.core.provider.RuleDefHandler;
import de.hipphampel.validation.core.rule.ReflectionRule;
import de.hipphampel.validation.core.rule.ReflectionRule.ParameterBinding;
import de.hipphampel.validation.core.rule.ReflectionRule.ResultMapper;
import de.hipphampel.validation.spring.rule.SpringReflectionRule;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.springframework.core.convert.ConversionService;

/**
 * Extension of the {@link RuleDefHandler} that automatically converts the parameters.
 * <p>
 * While the base implementation simply takes the parameter values as they are and tries to map them to the bound parameters, this
 * implementation applies a conversion to match the required parameter type.
 * <p>
 * For example consider the following method:
 *
 * <pre>
 * &#64;RuleDef
 * public boolean aRule(@BindPath("value") double x) {
 *   return x != 0.0;
 * }
 * </pre>
 * <p>
 * In case that the path {@code value} resolves to an {@code int}, the default {@code RuleDefHandler} would fail, since an {@code int} is
 * not a {@code double}, but this implementation would convert it to a {@code double}.
 *
 * @see RuleDefHandler
 */
public class SpringRuleDefHandler extends RuleDefHandler {

  private final ConversionService validationConversionService;

  /**
   * Constructor
   *
   * @param validationConversionService The {@link ConversionService} to use.
   */
  public SpringRuleDefHandler(ConversionService validationConversionService) {
    this.validationConversionService = validationConversionService;
  }

  @Override
  protected <T> ReflectionRule<T> newReflectionBasedRule(String id, Class<? super T> factsType, Map<String, Object> metadata,
      List<Condition> preconditions, Object boundInstance, Method ruleMethod, List<ParameterBinding> bindings, ResultMapper resultMapper) {
    return new SpringReflectionRule<>(id, factsType, metadata, preconditions, boundInstance, ruleMethod, bindings, resultMapper,
        validationConversionService);
  }
}
