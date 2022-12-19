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
package de.hipphampel.validation.spring.rule;

import de.hipphampel.validation.core.condition.Condition;
import de.hipphampel.validation.core.exception.RuleFailedException;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.rule.ReflectionRule;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.utils.ReflectionUtils;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

/**
 * Conversion enabled extension of the {@link ReflectionRule}.
 * <p>
 * This is basically a plain {@link ReflectionRule} which allows in addition an implicit conversion of the arguments. For this, it uses the
 * injected {@link ConversionService} to convert any parameter to the type required by the method
 *
 * @param <T> Type of the object being validate.
 * @see ConversionService
 */
public class SpringReflectionRule<T> extends ReflectionRule<T> {

  private final ConversionService validationConversionService;

  /**
   * Creates an instance using the given parameters.
   *
   * @param id                          The id of the {@link Rule}
   * @param factsType                   {@link Class} of the objects being validated by this rule
   * @param metadata                    The metadata
   * @param preconditions               The list of {@linkplain Condition preconditions}
   * @param boundInstance               The object that is bound to the method. In case of static methods it must be  {@code null}, in case
   *                                    of instance method not {@code null}
   * @param ruleMethod                  The {@code Method} to execute
   * @param bindings                    The {@linkplain ParameterBinding bindings} for the method parameters
   * @param validationConversionService The {@link ConversionService} to use.
   */
  public SpringReflectionRule(String id, Class<? super T> factsType, Map<String, Object> metadata, List<Condition> preconditions,
      Object boundInstance, Method ruleMethod, List<ParameterBinding> bindings, ConversionService validationConversionService) {
    super(id, factsType, metadata, preconditions, boundInstance, ruleMethod, bindings);
    this.validationConversionService = validationConversionService;
  }

  /**
   * Creates an instance using the given parameters.
   *
   * @param id                          The id of the {@link Rule}
   * @param factsType                   {@link Class} of the objects being validated by this rule
   * @param metadata                    The metadata
   * @param preconditions               The list of {@linkplain Condition preconditions}
   * @param boundInstance               The object that is bound to the method. In case of static methods it must be  {@code null}, in case
   *                                    of instance method not {@code null}
   * @param ruleMethod                  The {@code Method} to execute
   * @param bindings                    The {@linkplain ParameterBinding bindings} for the method parameters
   * @param resultMapper                The {@link ResultMapper} to map the methods return type to a {@link Result}
   * @param validationConversionService The {@link ConversionService} to use.
   */
  public SpringReflectionRule(String id, Class<? super T> factsType, Map<String, Object> metadata, List<Condition> preconditions,
      Object boundInstance, Method ruleMethod, List<ParameterBinding> bindings, ResultMapper resultMapper,
      ConversionService validationConversionService) {
    super(id, factsType, metadata, preconditions, boundInstance, ruleMethod, bindings, resultMapper);
    this.validationConversionService = validationConversionService;
  }

  @Override
  protected Object convertArgumentIfRequired(ValidationContext context, T facts, Parameter parameter, Object value) {
    TypeDescriptor sourceType = new TypeDescriptor(ResolvableType.forInstance(value), null, null);
    TypeDescriptor targetType = new TypeDescriptor(new MethodParameter(
        (Method) parameter.getDeclaringExecutable(),
        ReflectionUtils.getParameterIndex(parameter))
    );
    try {
      return validationConversionService.convert(value, sourceType, targetType);
    } catch (ConversionException ce) {
      throw new RuleFailedException(String.format("Expected a %s for parameter '%s', but got a %s",
          parameter.getType().getName(),
          parameter.getName(),
          value == null ? "'null'" : value.getClass().getName()));
    }
  }
}
