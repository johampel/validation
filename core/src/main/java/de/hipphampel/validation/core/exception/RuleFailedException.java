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
package de.hipphampel.validation.core.exception;

import de.hipphampel.validation.core.execution.RuleExecutor;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.ResultCode;
import de.hipphampel.validation.core.rule.ResultReason;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.rule.StringResultReason;

/**
 * {@link ValidationException} that can be thrown by a {@link Rule} to indicate a failed
 * {@code Result}.
 * <p>
 * An exception of this kind are caught be the {@link RuleExecutor} when the {@code Rule} is
 * executed; the associated {@link ResultReason} is the reason of the failed |@link Result} returned
 * by {@link #toResult()}
 * <p>
 * Use usage of this exception should not be the normal case to signal a failed rule.
 *
 * @see RuleExecutor
 */
public class RuleFailedException extends ValidationException {

  /**
   * The reason.
   */
  private final ResultReason reason;

  /**
   * Constructor.
   *
   * @param reason The {@link ResultReason}
   */
  public RuleFailedException(ResultReason reason) {
    super(String.valueOf(reason));
    this.reason = reason;
  }

  /**
   * Constructor.
   *
   * @param message The message, becomes also the {@link ResultReason}
   */
  public RuleFailedException(String message) {
    super(message);
    this.reason = new StringResultReason(message);
  }

  /**
   * Constructor.
   *
   * @param message The message, becomes also the {@link ResultReason}
   * @param cause   The cause
   */
  public RuleFailedException(String message, Throwable cause) {
    super(message, cause);
    this.reason = new StringResultReason(message);
  }

  /**
   * Constructor.
   *
   * @param reason The {@link ResultReason}
   * @param cause  The cause
   */
  public RuleFailedException(ResultReason reason, Throwable cause) {
    super(String.valueOf(reason), cause);
    this.reason = reason;
  }

  /**
   * Returns the {@link Result}.
   * <p>
   * The {@code Result} has always the code {@link ResultCode#FAILED} and the reason specified at
   * construction time.
   *
   * @return The {@code Result}
   */
  public Result toResult() {
    return Result.failed(reason);
  }
}
