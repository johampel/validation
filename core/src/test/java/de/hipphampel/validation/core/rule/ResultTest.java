package de.hipphampel.validation.core.rule;

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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ResultTest {

  @ParameterizedTest
  @CsvSource({
      "OK,      true,  false, false, false",
      "SKIPPED, false, true,  true,  false",
      "FAILED,  false, true, false,  true"
  })
  public void statusMethods(ResultCode code, boolean ok, boolean notOk, boolean skipped,
      boolean failed) {
    Result result = new Result(code, null);
    assertThat(result.isOk()).isEqualTo(ok);
    assertThat(result.isNotOk()).isEqualTo(notOk);
    assertThat(result.isSkipped()).isEqualTo(skipped);
    assertThat(result.isFailed()).isEqualTo(failed);
  }

  @ParameterizedTest
  @CsvSource({
      "OK,      true,  OK",
      "OK,      true,  SKIPPED",
      "OK,      true,  FAILED",
      "SKIPPED, false, OK",
      "FAILED,  false, SKIPPED"
  })
  public void mapIfOk(ResultCode code, boolean actionCalled, ResultCode mappedCode) {
    Result result = new Result(code, null);
    AtomicBoolean called = new AtomicBoolean(false);

    Result actual = result.mapIfOk(ignore -> {
      called.set(true);
      return new Result(mappedCode, null);
    });
    assertThat(called.get()).isEqualTo(actionCalled);
    if (actionCalled) {
      assertThat(actual.code()).isEqualTo(mappedCode);
    }
  }

  @ParameterizedTest
  @CsvSource({
      "OK,      false, FAILED",
      "SKIPPED, true,  SKIPPED",
      "SKIPPED, true,  FAILED",
      "SKIPPED, true,  OK",
      "FAILED,  false, SKIPPED"
  })
  public void mapIfSkipped(ResultCode code, boolean actionCalled, ResultCode mappedCode) {
    Result result = new Result(code, null);
    AtomicBoolean called = new AtomicBoolean(false);

    Result actual = result.mapIfSkipped(ignore -> {
      called.set(true);
      return new Result(mappedCode, null);
    });
    assertThat(called.get()).isEqualTo(actionCalled);
    if (actionCalled) {
      assertThat(actual.code()).isEqualTo(mappedCode);
    }
  }

  @ParameterizedTest
  @CsvSource({
//      "OK,      OK,      true",
//      "OK,      SKIPPED, false",
//      "OK,      FAILED,  false",
//      "SKIPPED, SKIPPED, true",
      "SKIPPED, FAILED,  false",
      "FAILED,  SKIPPED, true"
  })
  public void mergeBySeverity(ResultCode firstCode, ResultCode secondCode, boolean expectFirst) {
    Result first = new Result(firstCode, null);
    Result second = new Result(secondCode, null);
    Result expected = expectFirst? first:second;
     assertThat(Result.mergeBySeverity(first, second)).isSameAs(expected);
  }
}
