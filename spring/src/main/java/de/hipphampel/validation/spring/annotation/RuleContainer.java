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
package de.hipphampel.validation.spring.annotation;

import de.hipphampel.validation.core.annotations.RuleDef;
import de.hipphampel.validation.core.annotations.RuleRef;
import de.hipphampel.validation.core.provider.AnnotationRuleRepository;
import de.hipphampel.validation.core.rule.Rule;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

/**
 * Alias for a {@link Component} annotation that makes the bean known as a container with
 * {@link Rule} definitions.
 * <p>
 * This annotation is intended to be used for spring components that contain members with
 * {@link RuleDef} or {@link RuleRef} annotations.
 * <p>
 * Since this annotation is not only a {@code Component} but also a {@link Qualifier}, it can be
 * used in bean postprocessors or injects to identify those beans that are intended to be wrapped by
 * a {@link AnnotationRuleRepository}
 *
 * @see RuleRef
 * @see RuleDef
 * @see Component
 * @see Qualifier
 * @see AnnotationRuleRepository
 */
@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@Qualifier
public @interface RuleContainer {

  /**
   * The name of the bean
   *
   * @return The name of the bean
   */
  @AliasFor(
      annotation = Component.class
  )
  String value() default "";
}
