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

/**
 * Provides class related to {@link de.hipphampel.validation.core.provider}.
 * <p>
 * In general, it contains some enhancements that allow a smooth integration into the Spring framwork:
 *
 * <ol>
 *   <li>The {@link de.hipphampel.validation.spring.provider.RuleRepositoryProvider} with its default implementation
 *   {@link de.hipphampel.validation.spring.provider.DefaultRuleRepositoryProvider} allow to gather all beans that contain or
 *   are {@link de.hipphampel.validation.core.rule.Rule Rules} and expose them all together via a single
 *   {@link de.hipphampel.validation.core.provider.RuleRepository}</li>
 *   <li>The {@link de.hipphampel.validation.spring.provider.SpringRuleDefHandler} is an extension of the
 *   {@link de.hipphampel.validation.core.provider.RuleDefHandler} and generates {@code Rules} that is able to generate
 *   {@code Rules} that do an automatic parameter conversion</li>
 * </ol>
 */
package de.hipphampel.validation.spring.provider;
