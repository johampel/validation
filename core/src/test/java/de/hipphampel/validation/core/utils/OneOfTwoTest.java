package de.hipphampel.validation.core.utils;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

public class OneOfTwoTest {

  @Test
  public void first() {
    OneOfTwo<String, Integer> oot = OneOfTwo.ofFirst("2");
    assertThat(oot.getFirst()).isEqualTo("2");
    assertThatThrownBy(oot::getSecond).isInstanceOf(NoSuchElementException.class);
    assertThat(oot.hasFirst()).isTrue();
    assertThat(oot.hasSecond()).isFalse();
    assertThat(oot.mapIfFirst(s -> Integer.parseInt(s) + 1)).isEqualTo(3);
    assertThat(oot.mapIfSecond(i -> "" + (i - 1))).isEqualTo("2");
  }

  @Test
  public void second() {
    OneOfTwo<String, Integer> oot = OneOfTwo.ofSecond(2);
    assertThatThrownBy(oot::getFirst).isInstanceOf(NoSuchElementException.class);
    assertThat(oot.getSecond()).isEqualTo(2);
    assertThat(oot.hasFirst()).isFalse();
    assertThat(oot.hasSecond()).isTrue();
    assertThat(oot.mapIfFirst(s -> Integer.parseInt(s) + 1)).isEqualTo(2);
    assertThat(oot.mapIfSecond(i -> "" + (i - 1))).isEqualTo("1");
  }
}
