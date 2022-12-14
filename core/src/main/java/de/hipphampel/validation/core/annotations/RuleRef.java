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

import de.hipphampel.validation.core.provider.AnnotationRuleRepository;
import de.hipphampel.validation.core.provider.RuleRefHandler;
import de.hipphampel.validation.core.rule.Rule;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a class member providing a {@link Rule}.
 * <p>
 * Annotations of this type are evaluated by the {@link RuleRefHandler} which is normally called by the {@link AnnotationRuleRepository}.
 * <p>
 * This annotation can be applied to final, not-null fields of type {@code Rule} or to parameterless methods returning a {@code Rule}. In
 * both cases the outcome is a ready to use {@code Rule}. An example is:
 *
 * <pre>
 *   &#64;RuleRef
 *   Rule&lt;SomeType> rule = RuleBuilder.functionRule("ruleId", Object.class)
 *      .validateWith((context, facts) -> facts != null)
 *      .build();
 * </pre>
 *
 * <p>
 * Also, it is possible to apply this annotation to a field or a parameterless method providing collection of {@code Rules}. This option is
 * intended to produce rules the have a common shape, e.g.:
 *
 * <pre>
 *   &#64;RuleRef
 *   List&lt;? extends Rule&lt;?>> rules = Stream.of("Attr1", "Attr2", Attr3")
 *      .map(attr -> RuleBuilder.conditionRule("ruleFor" + attr, Object.class)
 *                              .validateWith(Conditions.notNull(Values.path(attr)))
 *                              .build())
 *      .toList();
 * </pre>
 * <p>
 * Note that in opposite to the {@link RuleDef} annotation, this annotation does not construct a new {@code Rule}, but simply refers to an
 * existing one.
 *
 * @see RuleDef
 * @see AnnotationRuleRepository
 * @see RuleRefHandler
 * @see Rule
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface RuleRef {

}
