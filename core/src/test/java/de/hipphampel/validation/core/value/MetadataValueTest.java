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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.hipphampel.validation.core.exception.ValueEvaluationException;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.rule.OkRule;
import de.hipphampel.validation.core.rule.Rule;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MetadataValueTest {

  private ValidationContext context;

  @BeforeEach
  public void beforeEach() {
    this.context = new ValidationContext();
  }

  @Test
  public void get_withConstant() {
    Rule<?> rule = new OkRule<>("test", Map.of("meta", "data"));
    context.enterRule(rule, "foo");
    Value<?> value = Values.metadata("meta");

    assertThat(value.get(context, "ignore")).isEqualTo("data");
  }

  @Test
  public void get_withValue() {
    Rule<?> rule = new OkRule<>("test", Map.of("meta", "lookup", "lookup", "data"));
    context.enterRule(rule, "foo");
    Value<?> value = Values.metadata(Values.metadata("meta"));

    assertThat(value.get(context, "ignore")).isEqualTo("data");
  }

  @Test
  public void get_noActiveRule_throwsException() {
    Value<?> value = Values.metadata("meta");

    assertThatThrownBy(() -> value.get(context, "ignore"))
        .isInstanceOf(ValueEvaluationException.class)
        .hasMessage("No active rule");
  }

  @Test
  public void get_keyNotPresent_returnsNull() {
    Rule<?> rule = new OkRule<>("test", Map.of());
    context.enterRule(rule, "foo");
    Value<?> value = Values.metadata("meta");

    assertThat(value.get(context, "ignore")).isNull();
  }
}
