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

import de.hipphampel.validation.core.provider.RuleRepository;
import de.hipphampel.validation.core.rule.Rule;

/**
 * {@link ValidationException} indicating that a {@link Rule} cannot be found.
 * <p>
 * Exceptions of this kind are typically thrown by a {@link RuleRepository}, in case that a request
 * {@code Rule} not exists.
 *
 * @see RuleRepository
 */
public class RuleNotFoundException extends ValidationException {

  /**
   * Constructor.
   *
   * @param message The message, typically the id of the {@link Rule}
   */
  public RuleNotFoundException(String message) {
    super(message);
  }

  /**
   * Constructor.
   *
   * @param message The message, typically the id of the {@link Rule}
   * @param cause   The cause
   */
  public RuleNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
