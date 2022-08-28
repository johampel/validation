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

import de.hipphampel.validation.core.event.SubscribableEventPublisher;
import de.hipphampel.validation.core.execution.SimpleRuleExecutor;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.path.BeanPathResolver;
import de.hipphampel.validation.core.provider.InMemoryRuleRepository;
import de.hipphampel.validation.core.report.BooleanReporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ContextValueTest {
  private ValidationContext context;

  @BeforeEach
  public void beforeEach() {
    this.context = new ValidationContext(
        new BooleanReporter(),
        Map.of("param1", "a", "param2", 2),
        new SimpleRuleExecutor(),
        new InMemoryRuleRepository(),
        new BeanPathResolver(),
        new SubscribableEventPublisher()
    );
  }

  @Test
  public void get_found() {
    Value<String> value = Values.context("param1");
    assertThat(value.get(context, "ignore")).isEqualTo("a");
  }

  @Test
  public void get_notFound() {
    Value<String> value = Values.context("notFound");
    assertThat(value.get(context, "ignore")).isNull();
  }
}
