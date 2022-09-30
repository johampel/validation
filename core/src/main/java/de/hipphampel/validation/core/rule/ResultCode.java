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

import java.util.Collection;
import java.util.Optional;

/**
 * Enum describing the different outcomes of a rule execution.
 * <p>
 * This is used in a {@link Result} which might provide - in case of a negative outcome - additional
 * information.
 */
public enum ResultCode {

  /**
   * The {@link Rule} has been executed with successful result.
   */
  OK,

  /**
   * The execution of the {@link Rule} has been skipped, since at least one precondition was not
   * met.
   */
  SKIPPED,

  /**
   * The {@link Rule} has been executed with a failing result.
   */
  FAILED;

  /**
   * Returns, whether this has a higher severity than {@code other}.
   *
   * @param other The other instance
   * @return {@code true}, if having a higher severity
   */
  public boolean hasHigherSeverityThan(ResultCode other) {
    return this.compareTo(other) > 0;
  }

  /**
   * Returns, whether this has a lower severity than {@code other}.
   *
   * @param other The other instance
   * @return {@code true}, if having a lower severity
   */
  public boolean hasLowerSeverityThan(ResultCode other) {
    return this.compareTo(other) < 0;
  }

  /**
   * Gets the {@link ResultCode} with the highest severity.
   *
   * @param first   The first code
   * @param further Further codes
   * @return {@link ResultCode} with the highest severity.
   */
  public static ResultCode getHighestSeverityOf(ResultCode first, ResultCode... further) {
    ResultCode result = first;
    for (int i = 0; i < further.length && result != FAILED; i++) {
      if (result.hasLowerSeverityThan(further[i])) {
        result = further[i];
      }
    }
    return result;
  }

  /**
   * Gets the {@link ResultCode} with the highest severity.
   *
   * @param codes The codes
   * @return {@link ResultCode} with the highest severity.
   */
  public static Optional<ResultCode> getHighestSeverityOf(Collection<ResultCode> codes) {
    ResultCode result = null;
    for (ResultCode current : codes) {
      if (result == null || current.hasHigherSeverityThan(result)) {
        result = current;
      }
      if (result == FAILED) {
        break;
      }
    }
    return Optional.ofNullable(result);
  }

}
