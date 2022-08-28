package de.hipphampel.validation.core.value;

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

import static de.hipphampel.validation.core.value.Values.path;
import static de.hipphampel.validation.core.value.Values.pathForObject;
import static de.hipphampel.validation.core.value.Values.val;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.hipphampel.validation.core.example.Worker;
import de.hipphampel.validation.core.exception.ValueEvaluationException;
import de.hipphampel.validation.core.execution.ValidationContext;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

public class PathValueTest {

  private final ValidationContext context = new ValidationContext();
  private final Object facts = new Worker("Donald", "Duck", 0.0, new TreeMap<>());
  private final Object object = new Worker("Klaas", "Klever", 1000.0, new TreeMap<>());

  @Test
  public void path_string_withoutDefault_throwsExceptionIfNotExists() {
    Value<?> value = path("notFound");
    assertThatThrownBy(() -> value.get(context, facts))
        .isInstanceOf(ValueEvaluationException.class)
        .hasMessage(
            "Path '(String)notFound' on Worker[firstName=Donald, lastName=Duck, salary=0.0, roles={}] resolves to nothing");
  }

  @Test
  public void path_path_withoutDefault_throwsExceptionIfNotExists() {
    Value<?> value = path(context.getPathResolver().parse("notFound"));
    assertThatThrownBy(() -> value.get(context, facts))
        .isInstanceOf(ValueEvaluationException.class)
        .hasMessage(
            "Path 'notFound' on Worker[firstName=Donald, lastName=Duck, salary=0.0, roles={}] resolves to nothing");
  }

  @Test
  public void path_value_withoutDefault_throwsExceptionIfNotExists() {
    Value<?> value = path(val("notFound"));
    assertThatThrownBy(() -> value.get(context, facts))
        .isInstanceOf(ValueEvaluationException.class)
        .hasMessage(
            "Path '(String)notFound' on Worker[firstName=Donald, lastName=Duck, salary=0.0, roles={}] resolves to nothing");
  }

  @Test
  public void path_string_withoutDefault_evaluatesToValueExists() {
    Value<?> value = path("lastName");
    assertThat(value.get(context, facts)).isEqualTo("Duck");
  }

  @Test
  public void path_path_withoutDefault_evaluatesToValueExists() {
    Value<?> value = path(context.getPathResolver().parse("lastName"));
    assertThat(value.get(context, facts)).isEqualTo("Duck");
  }

  @Test
  public void path_value_withoutDefault_evaluatesToValueExists() {
    Value<?> value = path(val("lastName"));
    assertThat(value.get(context, facts)).isEqualTo("Duck");
  }

  @Test
  public void path_string_withDefault_evaluatesToValueIfExists() {
    Value<?> value = path("lastName", val("default"));
    assertThat(value.get(context, facts)).isEqualTo("Duck");
  }

  @Test
  public void path_string_withDefault_evaluatesToDefaulaIfNotExists() {
    Value<?> value = path("notFound", val("default"));
    assertThat(value.get(context, facts)).isEqualTo("default");
  }

  @Test
  public void path_value_withDefault_evaluatesToValueIfExists() {
    Value<?> value = path(val("lastName"), val("default"));
    assertThat(value.get(context, facts)).isEqualTo("Duck");
  }

  @Test
  public void path_value_withDefault_evaluatesToDefaulaIfNotExists() {
    Value<?> value = path(val("notFound"), val("default"));
    assertThat(value.get(context, facts)).isEqualTo("default");
  }

  @Test
  public void path_path_withDefault_evaluatesToValueIfExists() {
    Value<?> value = path(context.getPathResolver().parse("lastName"),
        val("default"));
    assertThat(value.get(context, facts)).isEqualTo("Duck");
  }

  @Test
  public void path_path_withDefault_evaluatesToDefaulaIfNotExists() {
    Value<?> value = path(context.getPathResolver().parse("notFound"),
        val("default"));
    assertThat(value.get(context, facts)).isEqualTo("default");
  }

  @Test
  public void pathForObject_string_withoutDefault_throwsExceptionIfNotExists() {
    Value<?> value = pathForObject(val(object), "notFound");
    assertThatThrownBy(() -> value.get(context, facts))
        .isInstanceOf(ValueEvaluationException.class)
        .hasMessage(
            "Path '(String)notFound' on Worker[firstName=Klaas, lastName=Klever, salary=1000.0, roles={}] resolves to nothing");
  }


  @Test
  public void pathForObject_path_withoutDefault_throwsExceptionIfNotExists() {
    Value<?> value = pathForObject(val(object), context.getPathResolver().parse("notFound"));
    assertThatThrownBy(() -> value.get(context, facts))
        .isInstanceOf(ValueEvaluationException.class)
        .hasMessage(
            "Path 'notFound' on Worker[firstName=Klaas, lastName=Klever, salary=1000.0, roles={}] resolves to nothing");
  }

  @Test
  public void pathForObject_value_withoutDefault_throwsExceptionIfNotExists() {
    Value<?> value = pathForObject(val(object), val("notFound"));
    assertThatThrownBy(() -> value.get(context, facts))
        .isInstanceOf(ValueEvaluationException.class)
        .hasMessage(
            "Path '(String)notFound' on Worker[firstName=Klaas, lastName=Klever, salary=1000.0, roles={}] resolves to nothing");
  }

  @Test
  public void pathForObject_string_withoutDefault_evaluatesToValueExists() {
    Value<?> value = pathForObject(val(object), "lastName");
    assertThat(value.get(context, facts)).isEqualTo("Klever");
  }

  @Test
  public void pathForObject_path_withoutDefault_evaluatesToValueExists() {
    Value<?> value = pathForObject(val(object), context.getPathResolver().parse("lastName"));
    assertThat(value.get(context, facts)).isEqualTo("Klever");
  }

  @Test
  public void pathForObject_value_withoutDefault_evaluatesToValueExists() {
    Value<?> value = pathForObject(val(object), val("lastName"));
    assertThat(value.get(context, facts)).isEqualTo("Klever");
  }

  @Test
  public void pathForObject_string_withDefault_evaluatesToValueIfExists() {
    Value<?> value = pathForObject(val(object), "lastName", val("default"));
    assertThat(value.get(context, facts)).isEqualTo("Klever");
  }

  @Test
  public void pathForObject_string_withDefault_evaluatesToDefaulaIfNotExists() {
    Value<?> value = pathForObject(val(object), "notFound", val("default"));
    assertThat(value.get(context, facts)).isEqualTo("default");
  }

  @Test
  public void pathForObject_value_withDefault_evaluatesToValueIfExists() {
    Value<?> value = pathForObject(val(object), val("lastName"), val("default"));
    assertThat(value.get(context, facts)).isEqualTo("Klever");
  }

  @Test
  public void pathForObject_value_withDefault_evaluatesToDefaulaIfNotExists() {
    Value<?> value = pathForObject(val(object), val("notFound"), val("default"));
    assertThat(value.get(context, facts)).isEqualTo("default");
  }

  @Test
  public void pathForObject_path_withDefault_evaluatesToValueIfExists() {
    Value<?> value = pathForObject(val(object), context.getPathResolver().parse("lastName"),
        val("default"));
    assertThat(value.get(context, facts)).isEqualTo("Klever");
  }

  @Test
  public void pathForObject_path_withDefault_evaluatesToDefaulaIfNotExists() {
    Value<?> value = pathForObject(val(object), context.getPathResolver().parse("notFound"),
        val("default"));
    assertThat(value.get(context, facts)).isEqualTo("default");
  }
}
