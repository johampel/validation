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
package de.hipphampel.validation.core.provider;

import static de.hipphampel.validation.core.utils.ReflectionUtils.ensurePublicFinalField;
import static de.hipphampel.validation.core.utils.ReflectionUtils.ensurePublicMethod;
import static de.hipphampel.validation.core.utils.ReflectionUtils.getMandatoryFieldValue;

import de.hipphampel.validation.core.annotations.RuleRef;
import de.hipphampel.validation.core.provider.AnnotationRuleRepository.Handler;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.utils.TypeInfo;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * {@link Handler} implementation for the {@link RuleRef} annotation.
 * <p>
 * The {@code RuleRef} annotation is intended to expose existing {@link Rule Rules} to a {@link AnnotationRuleRepository}. The annotation
 * can be applied to:
 * <ol>
 *   <li>Static or instance fields, which are {@code final} and return either a single {@code Rule} or a {@code Collection} of
 *       {@code Rules}</li>
 *   <li>Static or instance methods, which have no parameters and return either a single {@code Rule} or a {@code Collection} of
 *       {@code Rules}</li>
 * </ol>
 * In any case, the field or method needs to be public.
 *
 * @see RuleRef
 */
public class RuleRefHandler implements Handler<RuleRef> {

  @Override
  public Class<RuleRef> getAnnotation() {
    return RuleRef.class;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Rule<?>> handleAnnotationForField(Object repositoryInstance, Field field, RuleRef annotation) {
    ensurePublicFinalField(repositoryInstance, field);
    return switch (getMemberType(field.getGenericType())) {
      case ONE_RULE -> List.of((Rule<?>) getMandatoryFieldValue(repositoryInstance, field));
      case COLLECTION_OF_RULES -> new ArrayList<>((Collection<Rule<?>>) getMandatoryFieldValue(repositoryInstance, field));
      default -> List.of();
    };
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Rule<?>> handleAnnotationForMethod(Object repositoryInstance, Method method, RuleRef annotation) {
    checkMethodSignature(repositoryInstance, method);
    return switch (getMemberType(method.getGenericReturnType())) {
      case ONE_RULE -> List.of((Rule<?>) getMethodValue(repositoryInstance, method));
      case COLLECTION_OF_RULES -> new ArrayList<>((Collection<Rule<?>>) getMethodValue(repositoryInstance, method));
      default -> List.of();
    };
  }

  private void checkMethodSignature(Object repositoryInstance, Method method) {
    ensurePublicMethod(repositoryInstance, method);
    if (method.getParameterCount() > 0) {
      throw new IllegalStateException("Method '" + method.getName() + "' must have no parameters");
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T getMethodValue(Object repositoryInstance, Method method) {
    try {
      T value = (T) method.invoke(repositoryInstance);
      if (value == null) {
        throw new IllegalStateException("Method '" + method.getName() + "' returns null");
      }
      return value;
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e.getCause());
    }
  }

  private static MemberType getMemberType(Type type) {
    TypeInfo typeInfo = TypeInfo.forType(type);
    if (Rule.class.isAssignableFrom(typeInfo.resolve())) {
      return MemberType.ONE_RULE;
    }

    if (Collection.class.isAssignableFrom(typeInfo.resolve()) && Rule.class.isAssignableFrom(typeInfo.getGeneric(0).resolve())) {
      return MemberType.COLLECTION_OF_RULES;

    }

    return MemberType.NO_RULE;
  }

  private enum MemberType {
    NO_RULE,
    ONE_RULE,
    COLLECTION_OF_RULES
  }
}
