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

import static de.hipphampel.validation.core.rule.ResultCode.FAILED;
import static de.hipphampel.validation.core.rule.ResultCode.OK;
import static de.hipphampel.validation.core.rule.ResultCode.SKIPPED;

import de.hipphampel.validation.core.execution.ValidationContext;
import java.util.Objects;
import java.util.function.Function;

/**
 * Represents the result of a {@link Rule#validate(ValidationContext, Object) Rule execution}.
 * <p>
 * It consists basically of two parts: The most important is the {@link ResultCode}, which indicates
 * the general outcome of the validation, the secondary is the {@link ResultReason}, which is
 * typically set only, if the result is not ok and might provide additional information.
 *
 * @param code   The main {@code ResultCode}
 * @param reason The reason, might be {@code null}
 * @see Rule
 * @see ResultCode
 * @see ResultReason
 */
public record Result(ResultCode code, ResultReason reason) {

  private static final Result OK_RESULT = new Result(OK, null);

  /**
   * Constructor.
   *
   * @param code   The main {@code ResultCode}
   * @param reason The reason, might be {@code null}
   */
  public Result {
    Objects.requireNonNull(code);
  }

  /**
   * Returns, whether this has a higher severity than {@code other}.
   *
   * @param other The other instance
   * @return {@code true}, if having a higher severity
   */
  public boolean hasHigherSeverityThan(Result other) {
    return code.hasHigherSeverityThan(other.code);
  }

  /**
   * Returns, whether this has a lower severity than {@code other}.
   *
   * @param other The other instance
   * @return {@code true}, if having a lower severity
   */
  public boolean hasLowerSeverityThan(Result other) {
    return code.hasLowerSeverityThan(other.code);
  }

  /**
   * Returns, whether the result is ok.
   * <p>
   * This is the case, if the {@link #code() code} is {@link ResultCode#OK}.
   *
   * @return {@code true}, if ok.
   */
  public boolean isOk() {
    return code == OK;
  }

  /**
   * Returns, whether the result is not ok.
   * <p>
   * This is the case, if the {@link #code() code} is not {@link ResultCode#OK}.
   *
   * @return {@code true}, if not ok.
   */
  public boolean isNotOk() {
    return code != OK;
  }

  /**
   * Returns, whether the result is  skipped.
   * <p>
   * This is the case, if the {@link #code() code} is {@link ResultCode#SKIPPED}.
   *
   * @return {@code true}, if skipped.
   */
  public boolean isSkipped() {
    return code == SKIPPED;
  }

  /**
   * Returns, whether the result is failed.
   * <p>
   * This is the case, if the {@link #code() code} is {@link ResultCode#FAILED}.
   *
   * @return {@code true}, if failed.
   */
  public boolean isFailed() {
    return code == FAILED;
  }

  /**
   * Executes {@code mapping}, if the result is ok.
   * <p>
   * If the result is ok, the result of the {@code mapping} is returned, otherwise this instance.
   *
   * @param mapping Mapping to execute, if the result is ok
   * @return The final result.
   */
  public Result mapIfOk(Function<Result, Result> mapping) {
    if (isOk()) {
      return mapping.apply(this);
    } else {
      return this;
    }
  }

  /**
   * Executes {@code mapping}, if the result is skipped.
   * <p>
   * If the result is skipped, the result of the {@code mapping} is returned, otherwise this
   * instance.
   *
   * @param mapping Mapping to execute, if the result is skippef
   * @return The final result.
   */
  public Result mapIfSkipped(Function<Result, Result> mapping) {
    if (isSkipped()) {
      return mapping.apply(this);
    } else {
      return this;
    }
  }

  /**
   * Factory method to create an ok result without a reason.
   *
   * @return The {@code Result}
   */
  public static Result ok() {
    return OK_RESULT;
  }

  /**
   * Factory method to create a skipped result.
   *
   * @param reason The reason, might be {@code null}
   * @return The {@code Result}
   */
  public static Result skipped(ResultReason reason) {
    return new Result(SKIPPED, reason);
  }

  /**
   * Factory method to create a skipped result.
   *
   * @param reason The reason, might be {@code null}
   * @return The {@code Result}
   */
  public static Result skipped(String reason) {
    return new Result(SKIPPED, reason == null ? null : new StringResultReason(reason));
  }

  /**
   * Factory method to create a skipped result without a reason.
   *
   * @return The {@code Result}
   */
  public static Result skipped() {
    return new Result(SKIPPED, null);
  }

  /**
   * Factory method to create a failed result.
   *
   * @param reason The reason, might be {@code null}
   * @return The {@code Result}
   */
  public static Result failed(ResultReason reason) {
    return new Result(FAILED, reason);
  }

  /**
   * Factory method to create a failed result.
   *
   * @param reason The reason, might be {@code null}
   * @return The {@code Result}
   */
  public static Result failed(String reason) {
    return new Result(FAILED, reason == null ? null : new StringResultReason(reason));
  }

  /**
   * Factory method to create a failed result without a reason.
   *
   * @return The {@code Result}
   */
  public static Result failed() {
    return new Result(FAILED, null);
  }

  /**
   * Helper method that returns the {@link Result} with the hight severity.
   * <p>
   * The severity is measured by the {@link ResultCode}, whereas {@code FAILED} has a higher
   * severity than {@code SKIPPED}, which has a hight severity than {@code OK}.
   * <p>
   * If both results have the same severity, the first on is returned.
   *
   * @param first  The first {@code Result}
   * @param second The second {@code Result}
   * @return The  {@code Result} with the higher priority
   */
  public static Result mergeBySeverity(Result first, Result second) {
    return second.hasLowerSeverityThan(first) ? first : second;
  }
}
