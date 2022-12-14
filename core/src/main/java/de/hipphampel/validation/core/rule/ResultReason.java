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
package de.hipphampel.validation.core.rule;

import java.util.stream.Stream;

/**
 * Interface for the reason part of {@link Result RuleResults}.
 */
public interface ResultReason {

  /**
   * Flattens the reason to a {@link Stream}.
   * <p>
   * In case that the {@code ResultReason} is actually a list of reasons, this decomposes this instance to a list of single
   * {@code ResultReasons}; this works also recursively.
   * <p>
   * The basic idea behind this is that if single {@link Rule} wants to signal multiple errors, it could use a {@link ListResultReason} to
   * transport more than one reason and the reporter can later on decompose the single list of reasons into one {@code ResultReason} for
   * each error.
   * <p>
   * This default implementation just returns a stream containing this instance.
   *
   * @return The stream of subordinated reasons.
   * @see de.hipphampel.validation.core.report.AbstractReportBasedReporter
   */
  default Stream<ResultReason> flatten() {
    return Stream.of(this);
  }
}
