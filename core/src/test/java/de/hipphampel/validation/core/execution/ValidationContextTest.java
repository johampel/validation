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
package de.hipphampel.validation.core.execution;

import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.utils.Pair;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ValidationContextTest {

  @Test
  public void sharedObject_isAvailableInCopies() {
    ValidationContext context = new ValidationContext();
    Pair<String,String> pair = new Pair<>("a","b");
    context.getOrCreateSharedExtension(Pair.class, ignore -> pair);

    assertThat(context.getSharedExtension(Pair.class)).isSameAs(pair);
    assertThat(context.copy().getSharedExtension(Pair.class)).isSameAs(pair);
  }
  @Test
  public void localObject_isNotAvailableInCopies() {
    ValidationContext context = new ValidationContext();
    Pair<String,String> pair = new Pair<>("a","b");
    context.getOrCreateLocalExtension(Pair.class, ignore -> pair);

    assertThat(context.getLocalExtension(Pair.class)).isSameAs(pair);
    assertThat(context.copy().knowsLocalExtension(Pair.class)).isFalse();
  }

  @Test
  public void facts() {
    ValidationContext context = new ValidationContext();
    assertThat(context.getCurrentFacts()).isNull();
    assertThat(context.getParentFacts()).isNull();
    assertThat(context.getRootFacts()).isNull();

    context.enterRule(mock(Rule.class), "foo");
    assertThat(context.getCurrentFacts()).isEqualTo("foo");
    assertThat(context.getParentFacts()).isNull();
    assertThat(context.getRootFacts()).isEqualTo("foo");

    context.enterRule(mock(Rule.class), "bar");
    assertThat(context.getCurrentFacts()).isEqualTo("bar");
    assertThat(context.getParentFacts()).isEqualTo("foo");
    assertThat(context.getRootFacts()).isEqualTo("foo");

    context.leaveRule();
    assertThat(context.getCurrentFacts()).isEqualTo("foo");
    assertThat(context.getParentFacts()).isNull();
    assertThat(context.getRootFacts()).isEqualTo("foo");

    context.leaveRule();
    assertThat(context.getCurrentFacts()).isNull();
    assertThat(context.getParentFacts()).isNull();
    assertThat(context.getRootFacts()).isNull();
  }
}
