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
package de.hipphampel.validation.core.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ReflectionUtilsTest {


  @Test
  public void getParameterIndex() throws NoSuchMethodException {
    Method method = ReflectionUtilsTest.class.getMethod("testMethod", String.class, String.class);

    assertThat(ReflectionUtils.getParameterIndex(mock(Parameter.class))).isEqualTo(-1);
    assertThat(ReflectionUtils.getParameterIndex(method.getParameters()[0])).isEqualTo(0);
    assertThat(ReflectionUtils.getParameterIndex(method.getParameters()[1])).isEqualTo(1);
  }

  @ParameterizedTest
  @CsvSource({
      "testField1, true,  false, 1",
      "testField1, false, false, 1",
      "testField2, true,  true,",
      "testField2, false, false, 2",
      "testField3, true,  true,",
      "testField3, false, true,",
      "testField4, true,  true,",
      "testField4, false, true,",
  })
  public void getMandatoryFieldValue(String fieldName, boolean staticAccess, boolean shouldFail, String expectedValue)
      throws NoSuchFieldException {
    Field field = ReflectionUtilsTest.class.getField(fieldName);
    if (shouldFail) {
      assertThatThrownBy(() -> ReflectionUtils.getMandatoryFieldValue(staticAccess ? null : this, field))
          .isInstanceOf(RuntimeException.class);
    } else {
      assertThat((String)ReflectionUtils.getMandatoryFieldValue(staticAccess ? null : this, field)).isEqualTo(expectedValue);
    }
  }

  public void testMethod(String a, String b) {
  }

  public static final String testField1 = "1";
  public final String testField2 = "2";
  public static final String testField3 = null;
  public final String testField4 = null;
}
