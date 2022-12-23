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
package de.hipphampel.validation.core.annotations;

import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.rule.Rule;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a rule method parameter to a metadata entry of the {@link Rule} being executed.
 * <p>
 * Annotations of this type are used for parameters of methods annotated with the {@link RuleDef} annotation and ensure that, when the
 * method is invoked as a validation rule, the parameter is filled with the metadata entry of the current {@code Rule} having the
 * name/key specified in the {@link #value() value}.
 * <p>
 * The core library makes not implicit type conversion, so that the method parameter type must match the context parameter type. The default
 * spring implementation tries such a conversion
 *
 * @see BindContext
 * @see BindContextParameter
 * @see BindFacts
 * @see BindPath
 * @see ValidationContext
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface BindMetadata {

  /**
   * The name of the {@link Rule} metadata entry name to bind.
   *
   * @return Metadata name.
   */
  String value();
}
