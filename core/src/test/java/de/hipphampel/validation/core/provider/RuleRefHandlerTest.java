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
package de.hipphampel.validation.core.provider;

import static de.hipphampel.validation.core.value.Values.facts;
import static de.hipphampel.validation.core.value.Values.val;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import de.hipphampel.validation.core.annotations.RuleRef;
import de.hipphampel.validation.core.condition.Conditions;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.rule.RuleBuilder;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

public class RuleRefHandlerTest {

  private final RuleRefHandler handler = new RuleRefHandler();
  private final InstanceContainer container = new InstanceContainer();

  @Test
  public void handleAnnotationForField_instance_singleRule() throws NoSuchFieldException {
    List<Rule<?>> rules = handler.handleAnnotationForField(
        container,
        getField(InstanceContainer.class, "r1"),
        mock(RuleRef.class));
    assertThat(rules).isEqualTo(List.of(container.r1));
  }

  @Test
  public void handleAnnotationForMethod_instance_singleRule() throws NoSuchMethodException {
    List<Rule<?>> rules = handler.handleAnnotationForMethod(
        container,
        getMethod(InstanceContainer.class, "r2"),
        mock(RuleRef.class));
    assertThat(rules).isEqualTo(List.of(container.r2()));
  }

  @Test
  public void handleAnnotationForField_instance_listRule() throws NoSuchFieldException {
    List<Rule<?>> rules = handler.handleAnnotationForField(
        container,
        getField(InstanceContainer.class, "r3"),
        mock(RuleRef.class));
    assertThat(rules).isEqualTo(container.r3);
  }

  @Test
  public void handleAnnotationForMethod_instance_listRule() throws NoSuchMethodException {
    List<Rule<?>> rules = handler.handleAnnotationForMethod(
        container,
        getMethod(InstanceContainer.class, "r4"),
        mock(RuleRef.class));
    assertThat(rules).isEqualTo(container.r4());
  }

  @Test
  public void handleAnnotationForField_instance_inaccessible() throws NoSuchFieldException {
    assertThatThrownBy(() -> handler.handleAnnotationForField(
        container,
        getField(InstanceContainer.class, "inaccessible"),
        mock(RuleRef.class)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Field 'inaccessible' must be public");
  }

  @Test
  public void handleAnnotationForField_instance_modifiable() throws NoSuchFieldException {
    assertThatThrownBy(() -> handler.handleAnnotationForField(
        container,
        getField(InstanceContainer.class, "modifiable"),
        mock(RuleRef.class)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Field 'modifiable' must be final");
  }

  @Test
  public void handleAnnotationForField_static_singleRule() throws NoSuchFieldException {
    List<Rule<?>> rules = handler.handleAnnotationForField(
        null,
        getField(StaticContainer.class, "r1"),
        mock(RuleRef.class));
    assertThat(rules).isEqualTo(List.of(StaticContainer.r1));
  }

  @Test
  public void handleAnnotationForMethod_static_singleRule() throws NoSuchMethodException {
    List<Rule<?>> rules = handler.handleAnnotationForMethod(
        null,
        getMethod(StaticContainer.class, "r2"),
        mock(RuleRef.class));
    assertThat(rules).isEqualTo(List.of(StaticContainer.r2()));
  }

  @Test
  public void handleAnnotationForField_static_listRule() throws NoSuchFieldException {
    List<Rule<?>> rules = handler.handleAnnotationForField(
        null,
        getField(StaticContainer.class, "r3"),
        mock(RuleRef.class));
    assertThat(rules).isEqualTo(StaticContainer.r3);
  }

  @Test
  public void handleAnnotationForMethod_static_listRule() throws NoSuchMethodException {
    List<Rule<?>> rules = handler.handleAnnotationForMethod(
        null,
        getMethod(StaticContainer.class, "r4"),
        mock(RuleRef.class));
    assertThat(rules).isEqualTo(StaticContainer.r4());
  }

  @Test
  public void handleAnnotationForField_static_inaccessible() throws NoSuchFieldException {
    assertThatThrownBy(() -> handler.handleAnnotationForField(
        null,
        getField(StaticContainer.class, "inaccessible"),
        mock(RuleRef.class)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Field 'inaccessible' must be public");
  }

  @Test
  public void handleAnnotationForField_static_modifiable() throws NoSuchFieldException {
    assertThatThrownBy(() -> handler.handleAnnotationForField(
        null,
        getField(StaticContainer.class, "modifiable"),
        mock(RuleRef.class)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Field 'modifiable' must be final");
  }

  private Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
    return clazz.getDeclaredField(name);
  }

  private Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
    return clazz.getMethod(name, parameterTypes);
  }

  public static class InstanceContainer {

    private final Rule<String> inaccessible = null;
    public Rule<String> modifiable = null;

    @RuleRef
    public final Rule<String> r1 =
        RuleBuilder.conditionRule("r1", String.class)
            .validateWith(Conditions.eq(facts(), val("r1")))
            .build();

    @RuleRef
    public Rule<?> r2() {
      return RuleBuilder.conditionRule("r2", String.class)
          .validateWith(Conditions.eq(facts(), val("r2")))
          .withFailReason("failed rule")
          .build();
    }

    @RuleRef
    public final List<? extends Rule<?>> r3 = IntStream.range(0, 2).boxed()
        .map(i -> RuleBuilder.conditionRule("r3_" + i, Object.class).validateWith(Conditions.alwaysTrue()).build())
        .toList();

    @RuleRef
    public List<? extends Rule<?>> r4() {
      return IntStream.range(0, 2).boxed()
          .map(i -> RuleBuilder.conditionRule("r4_" + i, Object.class).validateWith(Conditions.alwaysTrue()).build())
          .toList();
    }
  }

  public static class StaticContainer {

    private static final Rule<String> inaccessible = null;
    public static Rule<String> modifiable = null;

    @RuleRef
    public static final Rule<String> r1 =
        RuleBuilder.conditionRule("r1", String.class)
            .validateWith(Conditions.eq(facts(), val("r1")))
            .build();

    @RuleRef
    public static Rule<?> r2() {
      return RuleBuilder.conditionRule("r2", String.class)
          .validateWith(Conditions.eq(facts(), val("r2")))
          .withFailReason("failed rule")
          .build();
    }

    @RuleRef
    public static final List<? extends Rule<?>> r3 = IntStream.range(0, 2).boxed()
        .map(i -> RuleBuilder.conditionRule("r3_" + i, Object.class).validateWith(Conditions.alwaysTrue()).build())
        .toList();

    @RuleRef
    public static List<? extends Rule<?>> r4() {
      return IntStream.range(0, 2).boxed()
          .map(i -> RuleBuilder.conditionRule("r4_" + i, Object.class).validateWith(Conditions.alwaysTrue()).build())
          .toList();
    }
  }

}
