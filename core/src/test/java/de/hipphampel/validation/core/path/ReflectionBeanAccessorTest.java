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
package de.hipphampel.validation.core.path;

import static org.assertj.core.api.Assertions.assertThat;

import de.hipphampel.validation.core.example.Point;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ReflectionBeanAccessorTest {

  @Test
  public void getPropertyNames_forClass() {
    BeanAccessor accessor = new ReflectionBeanAccessor();
    assertThat(accessor.getPropertyNames("str"))
        .containsExactlyInAnyOrder("blank", "bytes", "empty");
  }

  @Test
  public void getPropertyNames_forRecord() {
    BeanAccessor accessor = new ReflectionBeanAccessor();
    assertThat(accessor.getPropertyNames(new Point(1., 2.)))
        .containsExactlyInAnyOrder("x", "y");
  }

  @Test
  public void getPropertyNames_withWhiteList() {
    BeanAccessor accessor = new ReflectionBeanAccessor(List.of("de.hipphampel.*"));
    assertThat(accessor.getPropertyNames(new Point(1., 2.)))
        .containsExactlyInAnyOrder("x", "y");
    assertThat(accessor.getPropertyNames("str"))
        .containsExactlyInAnyOrder(); // Filtered out
  }

  @Test
  public void getProperty_notFound() {
    BeanAccessor accessor = new ReflectionBeanAccessor();
    assertThat(accessor.getProperty("str", "x"))
        .isEqualTo(Resolved.empty());
  }

  @Test
  public void getProperty_found_forClass() {
    BeanAccessor accessor = new ReflectionBeanAccessor();
    assertThat(accessor.getProperty("str", "empty"))
        .isEqualTo(Resolved.of(false));
  }

  @Test
  public void getProperty_found_forRecord() {
    BeanAccessor accessor = new ReflectionBeanAccessor();
    assertThat(accessor.getProperty(new Point(1., 2.), "x"))
        .isEqualTo(Resolved.of(1.));
  }
}
