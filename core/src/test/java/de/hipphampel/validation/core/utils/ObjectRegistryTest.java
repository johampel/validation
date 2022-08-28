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

import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ObjectRegistryTest {

  private ObjectRegistry registry;
  private final Object a = new A() {
  };
  private final Object b = new B() {
  };
  private final Object c = new C() {
  };
  private final Object d = new Object();


  @BeforeEach
  public void beforeEach() {
    this.registry = new ObjectRegistry();
  }

  @Test
  public void add() {
    assertThat(registry.toMap()).isEqualTo(Map.of());

    assertThat(registry.add(d)).isSameAs(registry);
    assertThat(registry.toMap()).isEqualTo(Map.of(Object.class, d));

    assertThat(registry.add(c, C.class, B.class, A.class)).isSameAs(registry);
    assertThat(registry.toMap()).isEqualTo(
        Map.of(A.class, c, B.class, c, C.class, c, c.getClass(), c, Object.class, d));

    assertThat(registry.add(b, B.class)).isSameAs(registry);
    assertThat(registry.toMap()).isEqualTo(
        Map.of(A.class, c, B.class, b, b.getClass(), b, C.class, c, c.getClass(), c, Object.class,
            d));

    assertThat(registry.add(a, A.class)).isSameAs(registry);
    assertThat(registry.toMap()).isEqualTo(
        Map.of(A.class, a, a.getClass(), a, B.class, b, b.getClass(), b, C.class, c, c.getClass(),
            c, Object.class, d));
  }

  @Test
  public void addAndRemovePrevious() {
    assertThat(registry.toMap()).isEqualTo(Map.of());

    assertThat(registry.addAndRemovePrevious(d)).isSameAs(registry);
    assertThat(registry.toMap()).isEqualTo(Map.of(Object.class, d));

    assertThat(registry.addAndRemovePrevious(c, C.class, B.class, A.class)).isSameAs(registry);
    assertThat(registry.toMap()).isEqualTo(
        Map.of(A.class, c, B.class, c, C.class, c, c.getClass(), c, Object.class, d));

    assertThat(registry.addAndRemovePrevious(b, B.class)).isSameAs(registry);
    assertThat(registry.toMap()).isEqualTo(Map.of(B.class, b, b.getClass(), b, Object.class, d));

    assertThat(registry.addAndRemovePrevious(a, A.class)).isSameAs(registry);
    assertThat(registry.toMap()).isEqualTo(
        Map.of(A.class, a, a.getClass(), a, B.class, b, b.getClass(), b, Object.class, d));
  }

  @Test
  public void removeType() {
    assertThat(registry.add(d)).isSameAs(registry);
    assertThat(registry.add(c, C.class, B.class, A.class)).isSameAs(registry);
    assertThat(registry.add(b, B.class)).isSameAs(registry);
    assertThat(registry.add(a, A.class)).isSameAs(registry);
    assertThat(registry.toMap()).isEqualTo(
        Map.of(A.class, a, a.getClass(), a, B.class, b, b.getClass(), b, C.class, c, c.getClass(),
            c, Object.class, d));

    assertThat(registry.removeType(A.class)).isSameAs(registry);
    assertThat(registry.toMap()).isEqualTo(
        Map.of(a.getClass(), a, B.class, b, b.getClass(), b, C.class, c, c.getClass(), c,
            Object.class, d));
    assertThat(registry.removeType(Object.class)).isSameAs(registry);
    assertThat(registry.toMap()).isEqualTo(
        Map.of(a.getClass(), a, B.class, b, b.getClass(), b, C.class, c, c.getClass(), c));
  }

  @Test
  public void removeObject() {
    assertThat(registry.add(d)).isSameAs(registry);
    assertThat(registry.add(c, C.class, B.class, A.class)).isSameAs(registry);
    assertThat(registry.add(b, B.class)).isSameAs(registry);
    assertThat(registry.add(a, A.class)).isSameAs(registry);
    assertThat(registry.toMap()).isEqualTo(
        Map.of(A.class, a, a.getClass(), a, B.class, b, b.getClass(), b, C.class, c, c.getClass(),
            c, Object.class, d));

    assertThat(registry.removeObject(a)).isSameAs(registry);
    assertThat(registry.toMap()).isEqualTo(
        Map.of(B.class, b, b.getClass(), b, C.class, c, c.getClass(), c, Object.class, d));

    assertThat(registry.removeObject(b)).isSameAs(registry);
    assertThat(registry.toMap()).isEqualTo(Map.of(C.class, c, c.getClass(), c, Object.class, d));
  }

  @Test
  public void knowsObject() {
    assertThat(registry.add(c, C.class)).isSameAs(registry);
    assertThat(registry.add(a, A.class)).isSameAs(registry);

    assertThat(registry.knowsObject(a)).isTrue();
    assertThat(registry.knowsObject(b)).isFalse();
    assertThat(registry.knowsObject(c)).isTrue();
    assertThat(registry.knowsObject(d)).isFalse();
  }

  @Test
  public void knowsType() {
    assertThat(registry.add(c, C.class)).isSameAs(registry);
    assertThat(registry.add(a, A.class)).isSameAs(registry);

    assertThat(registry.knowsType(A.class)).isTrue();
    assertThat(registry.knowsType(B.class)).isFalse();
    assertThat(registry.knowsType(C.class)).isTrue();
    assertThat(registry.knowsType(Object.class)).isFalse();
  }

  @Test
  public void getTypes() {
    assertThat(registry.add(c, C.class, B.class)).isSameAs(registry);
    assertThat(registry.add(a, A.class)).isSameAs(registry);

    assertThat(registry.getTypes()).containsExactlyInAnyOrder(
        c.getClass(), C.class, B.class, a.getClass(), A.class
    );
  }

  @Test
  public void getTypesOf() {
    assertThat(registry.add(c, C.class, B.class)).isSameAs(registry);
    assertThat(registry.add(a, A.class)).isSameAs(registry);

    assertThat(registry.getTypesOf(a)).containsExactlyInAnyOrder(
        a.getClass(), A.class
    );
    assertThat(registry.getTypesOf(b)).containsExactlyInAnyOrder();
    assertThat(registry.getTypesOf(c)).containsExactlyInAnyOrder(
        c.getClass(), C.class, B.class
    );
  }

  @Test
  public void getObjects() {
    assertThat(registry.add(c, C.class, B.class)).isSameAs(registry);
    assertThat(registry.add(a, A.class)).isSameAs(registry);

    assertThat(registry.getObjects()).containsExactlyInAnyOrder(
        a, c
    );
  }

  @Test
  public void get() {
    assertThat(registry.add(c, C.class, B.class)).isSameAs(registry);
    assertThat(registry.add(a, A.class)).isSameAs(registry);

    assertThat(registry.get(A.class)).isSameAs(a);
    assertThat(registry.get(B.class)).isSameAs(c);
    assertThat(registry.get(C.class)).isSameAs(c);
    assertThatThrownBy(() -> registry.get(Object.class)).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  public void getOpt() {
    assertThat(registry.add(c, C.class, B.class)).isSameAs(registry);
    assertThat(registry.add(a, A.class)).isSameAs(registry);

    assertThat(registry.getOpt(A.class)).contains((A) a);
    assertThat(registry.getOpt(B.class)).contains((B) c);
    assertThat(registry.getOpt(C.class)).contains((C) c);
    assertThat(registry.getOpt(Object.class)).isEmpty();
  }

  @Test
  public void getOrRegister() {
    assertThat(registry.knowsType(A.class)).isFalse();
    assertThat(registry.getOrRegister(A.class, ignore -> (A)a)).isSameAs(a);
    assertThat(registry.knowsType(A.class)).isTrue();
    assertThat(registry.getOrRegister(A.class, ignore -> (A)c)).isSameAs(a);

  }

  interface A {

  }

  interface B {

  }

  interface C extends A, B {

  }

}
