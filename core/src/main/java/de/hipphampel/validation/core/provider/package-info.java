/**
 * Provides types for objects providing {@link de.hipphampel.validation.core.rule.Rule Rules}.
 * <p>
 * There are basically two groups of providers:
 * <ol>
 *   <li>{@link de.hipphampel.validation.core.provider.RuleRepository RuleRepositories}, which act as
 *   a storage for {@code Rules} in general. {@code Repositories} are typically context free, so
 *   they returns always the same set of {@code Rules}.</li>
 *   <li>{@link de.hipphampel.validation.core.provider.RuleSelector RuleSelectors} that act as a
 *   kind of filter for {@code Repositories} that return only those {@code Rules} that are
 *   suitable for the object to validate</li>
 * </ol>
 *
 * @see de.hipphampel.validation.core.provider.RuleSelector
 * @see de.hipphampel.validation.core.provider.RuleRepository
 */
package de.hipphampel.validation.core.provider;
/*-
 * #%L
 * validation-core
 * %%
 * Copyright (C) 2022 Johannes Hampel
 * %%
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
 * #L%
 */
