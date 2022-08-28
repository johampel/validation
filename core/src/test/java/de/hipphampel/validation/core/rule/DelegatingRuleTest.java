package de.hipphampel.validation.core.rule;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.hipphampel.validation.core.condition.Conditions;
import de.hipphampel.validation.core.execution.ValidationContext;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DelegatingRuleTest {


  private Rule<String> delegate;
  private DelegatingRule<String> rule;


  @BeforeEach
  public void beforeEach() {
    delegate = mock(Rule.class);
    rule = new DelegatingRule<>(delegate);
  }

  @Test
  public void getId() {
    when(delegate.getId()).thenReturn("theId");
    assertThat(rule.getId()).isEqualTo(delegate.getId());
  }

  @Test
  public void getMetadata() {
    when(delegate.getMetadata()).thenReturn(Map.of("meta", "data"));
    assertThat(rule.getMetadata()).isEqualTo(delegate.getMetadata());
  }

  @Test
  public void getPreconditions() {
    when(delegate.getPreconditions()).thenReturn(
        List.of(Conditions.alwaysTrue(), Conditions.alwaysFalse()));
    assertThat(rule.getPreconditions()).isEqualTo(delegate.getPreconditions());
  }

  @Test
  public void validate() {
    String facts = "facts";
    ValidationContext context = mock(ValidationContext.class);
    when(delegate.validate(eq(context), eq(facts))).thenReturn(Result.ok());
    assertThat(rule.validate(context, facts)).isEqualTo(Result.ok());
  }

}
