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

import java.util.Objects;

/**
 * {@link ResultReason} implementation for internal causes
 *
 * @param code    The {@link Code}
 * @param message The message
 */
public record SystemResultReason(Code code, String message) implements ResultReason {

  /**
   * Constructor.
   *
   * @param code    The {@link Code}
   * @param message The message
   */
  public SystemResultReason {
    Objects.requireNonNull(code);
  }

  /**
   * Code describing the reason why a execution failed.
   */
  public enum Code {

    /**
     * Indicates that at least one of the precondotions was not met.
     */
    PreconditionNotMet,

    /**
     * Indicates that the {@link Rule} was called with the wrong type.
     */
    FactTypeDoesNotMatchRuleType,

    /**
     * Indicates that a {@link Rule} threw an exception
     */
    RuleExecutionThrowsException,

    /**
     * Indicates a cyclic dependency between {@link Rule Rules}.
     */
    CyclicRuleDependency
  }
}
