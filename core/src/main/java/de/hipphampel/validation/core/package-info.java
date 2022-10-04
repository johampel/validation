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
 * Validation core library.
 * <p>
 * This package itself contains just the core classes like
 * {@link de.hipphampel.validation.core.Validator Validator} and
 * {@link de.hipphampel.validation.core.ValidatorBuilder ValidatorBuilder} that are intended for
 * clients to run validations.
 * <p>
 * A concrete {@code Validator} normally uses classes and concepts defined in the sub packages,
 * like:
 * <ol>
 *   <li>{@link de.hipphampel.validation.core.rule.Rule Rules} which contain the business logic of
 *   validation rules and {@link de.hipphampel.validation.core.provider.RuleRepository
 *   RuleRepositories} that make these rules available</li>
 *   <li>{@link de.hipphampel.validation.core.report.Reporter Reporters} that are intended to form
 *   the final validation result</li>
 *   <li>More technical driven classes, like
 *   {@link de.hipphampel.validation.core.execution.RuleExecutor RuleExecutors} to control the
 *   rule execution and {@link de.hipphampel.validation.core.path.PathResolver PathResolvers} that
 *   allow to access the different values of the objects being validated</li>
 * </ol>
 *
 * @see de.hipphampel.validation.core.ValidatorBuilder
 * @see de.hipphampel.validation.core.Validator
 */
package de.hipphampel.validation.core;
