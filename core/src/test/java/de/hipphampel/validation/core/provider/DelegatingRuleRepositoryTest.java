package de.hipphampel.validation.core.provider;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.hipphampel.validation.core.exception.RuleNotFoundException;
import de.hipphampel.validation.core.rule.Rule;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DelegatingRuleRepositoryTest {

  private RuleRepository delegate;
  private DelegatingRuleRepository provider;


  @BeforeEach
  public void beforeEach() {
    delegate = mock(RuleRepository.class);
    provider = new DelegatingRuleRepository(delegate);
  }

  @Test
  public void getDelegate() {
    assertThat(provider.getDelegate()).isSameAs(delegate);
  }

  @Test
  public void knowsRuleId() {
    when(delegate.knowsRuleId("known")).thenReturn(true);
    when(delegate.knowsRuleId("unknown")).thenReturn(false);

    assertThat(provider.knowsRuleId("known")).isTrue();
    assertThat(provider.knowsRuleId("unknown")).isFalse();
  }

  @Test
  public void getRule() {
    Rule<Object> rule = mock(Rule.class);
    when(delegate.getRule("known")).thenReturn(rule);
    when(delegate.getRule("unknown")).thenThrow(new RuleNotFoundException("bad"));

    assertThat(provider.getRule("known")).isSameAs(rule);
    assertThatThrownBy(() -> provider.getRule("unknown")).isInstanceOf(RuleNotFoundException.class);
  }

  @Test
  public void getRuleIds() {
    when(delegate.getRuleIds()).thenReturn(Set.of("a", "b", "c"));
    assertThat(delegate.getRuleIds()).isEqualTo(Set.of("a", "b", "c"));
  }
}
