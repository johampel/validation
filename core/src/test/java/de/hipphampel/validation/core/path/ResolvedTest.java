package de.hipphampel.validation.core.path;

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

public class ResolvedTest {

  @Test
  public void isEmpty() {
    assertThat(Resolved.empty().isEmpty()).isTrue();
    assertThat(Resolved.of(null).isEmpty()).isFalse();
    assertThat(Resolved.of("x").isEmpty()).isFalse();
  }

  @Test
  public void isPresent() {
    assertThat(Resolved.empty().isPresent()).isFalse();
    assertThat(Resolved.of(null).isPresent()).isTrue();
    assertThat(Resolved.of("x").isPresent()).isTrue();
  }

  @Test
  public void get() {
    assertThatThrownBy(() -> Resolved.empty().get()).isInstanceOf(NoSuchElementException.class);
    assertThat(Resolved.of(null).get()).isNull();
    assertThat(Resolved.of("x").get()).isEqualTo("x");
  }

  @Test
  public void map() {
    assertThat(Resolved.<Integer>empty().map((a) -> 2 * a)).isEqualTo(Resolved.empty());
    assertThat(Resolved.of(3).map((a) -> 2 * a)).isEqualTo(Resolved.of(6));
  }

  @Test
  public void orElse() {
    assertThat(Resolved.empty().orElse(2)).isEqualTo(2);
    assertThat(Resolved.of(3).orElse(3)).isEqualTo(3);
  }

  @Test
  public void orElseGet() {
    assertThat(Resolved.empty().orElseGet(() -> 2)).isEqualTo(2);
    assertThat(Resolved.of(3).orElseGet(() -> 3)).isEqualTo(3);
  }

  @Test
  public void orElseThrow() {
    assertThatThrownBy(() -> Resolved.empty().orElseThrow(IllegalStateException::new)).isInstanceOf(
        IllegalStateException.class);
    assertThat(Resolved.of(3).orElseThrow(IllegalStateException::new)).isEqualTo(3);
  }

  @Test
  public void stream() {
    assertThat(Resolved.empty().stream()).isEmpty();
    assertThat(Resolved.of("x").stream()).containsExactly("x");
    assertThat(Resolved.of(null).stream()).containsExactly((Object) null);
  }
}
