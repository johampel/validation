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

import org.junit.jupiter.api.Test;

public class StackedTest {

  @Test
  public void isEmpty() {
    assertThat(Stacked.empty().isEmpty()).isTrue();
    assertThat(Stacked.empty().push(null).isEmpty()).isFalse();
    assertThat(Stacked.empty().push("a").isEmpty()).isFalse();
  }

  @Test
  public void exists() {
    Stacked<String> l0 = Stacked.empty();
    Stacked<String> l1 = l0.push("a");
    Stacked<String> l2 = l1.push("b");

    assertThat(l2.exists("a"::equals)).isTrue();
    assertThat(l2.exists("b"::equals)).isTrue();
    assertThat(l2.exists("c"::equals)).isFalse();
  }

  @Test
  public void toList() {
    Stacked<String> l0 = Stacked.empty();
    Stacked<String> l1 = l0.push("a");
    Stacked<String> l2 = l1.push("b");
    Stacked<String> l3 = l2.push("c");

    assertThat(l0.toList()).isEmpty();
    assertThat(l1.toList()).containsExactly("a");
    assertThat(l2.toList()).containsExactly("a", "b");
    assertThat(l3.toList()).containsExactly("a", "b", "c");
  }
}
