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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ResultCodeTest {

  @ParameterizedTest
  @CsvSource({
      "OK, OK,      OK,     OK",
      "OK, SKIPPED, OK,     SKIPPED",
      "OK, SKIPPED, FAILED, FAILED",
  })
  public void getHighestSeverityOf_varargs(ResultCode c1, ResultCode c2, ResultCode c3,
      ResultCode expected) {
    assertThat(ResultCode.getHighestSeverityOf(c1, c2, c3))
        .isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
      "OK, OK,      OK,     OK",
      "OK, SKIPPED, OK,     SKIPPED",
      "OK, SKIPPED, FAILED, FAILED",
  })
  public void getHighestSeverityOf_collections(ResultCode c1, ResultCode c2, ResultCode c3,
      ResultCode expected) {
    assertThat(ResultCode.getHighestSeverityOf(List.of(c1, c2, c3)))
        .isEqualTo(Optional.of(expected));
  }
}
