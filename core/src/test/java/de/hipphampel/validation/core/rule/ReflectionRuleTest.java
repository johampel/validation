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
package de.hipphampel.validation.core.rule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.hipphampel.validation.core.exception.RuleFailedException;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.path.BeanPathResolver;
import de.hipphampel.validation.core.path.Resolved;
import de.hipphampel.validation.core.rule.ReflectionRule.ContextBinding;
import de.hipphampel.validation.core.rule.ReflectionRule.ContextParameterBinding;
import de.hipphampel.validation.core.rule.ReflectionRule.DefaultResultMapper;
import de.hipphampel.validation.core.rule.ReflectionRule.FactsBinding;
import de.hipphampel.validation.core.rule.ReflectionRule.MetadataBinding;
import de.hipphampel.validation.core.rule.ReflectionRule.ParameterBinding;
import de.hipphampel.validation.core.rule.ReflectionRule.ParentFactsBinding;
import de.hipphampel.validation.core.rule.ReflectionRule.PathBinding;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import de.hipphampel.validation.core.rule.ReflectionRule.RootFactsBinding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ReflectionRuleTest {

  private ValidationContext context;


  @BeforeEach
  public void beforeEach() {
    context = mock(ValidationContext.class);
    when(context.getPathResolver()).thenReturn(new BeanPathResolver());
  }

  @ParameterizedTest
  @CsvSource({
      "1, 2, true",
      "3, 2, false"
  })
  public void validate(Integer a, Integer b, boolean expected) throws NoSuchMethodException {
    Method method = ReflectionRuleTest.class.getMethod("staticRuleMethod", int.class, int.class);
    ReflectionRule<Object> rule = new ReflectionRule<>("id", Object.class, Map.of(), List.of(), null, method,
        List.of(new PathBinding("a"), new PathBinding("b")));
    Map<String, Integer> facts = Map.of("a", a, "b", b);

    assertThat(rule.validate(context, facts).isOk()).isEqualTo(expected);
  }

  @Test
  public void getArgument() throws NoSuchMethodException {
    Method method = ReflectionRuleTest.class.getMethod("staticRuleMethod", int.class, int.class);
    ReflectionRule<Integer> rule = new ReflectionRule<>("id", Object.class, Map.of(), List.of(), null, method,
        List.of(new FactsBinding(), mock(ParameterBinding.class)));

    assertThat(rule.getArgument(context, 4711, method.getParameters()[0], new FactsBinding())).isEqualTo(4711);
  }

  @Test
  public void convertArgumentIfRequired_ok() throws NoSuchMethodException {
    Method method = ReflectionRuleTest.class.getMethod("staticRuleMethod", int.class, int.class);
    ReflectionRule<String> rule = new ReflectionRule<>("id", Object.class, Map.of(), List.of(), null, method,
        List.of(mock(ParameterBinding.class), mock(ParameterBinding.class)));

    assertThat(rule.convertArgumentIfRequired(context, "ignore", method.getParameters()[0], 4711)).isEqualTo(4711);
  }

  @Test
  public void convertArgumentIfRequired_wrongType() throws NoSuchMethodException {
    Method method = ReflectionRuleTest.class.getMethod("instanceRuleMethod", ValidationContext.class);
    ReflectionRule<String> rule = new ReflectionRule<>("id", Object.class, Map.of(), List.of(), this, method,
        List.of(mock(ParameterBinding.class)));

    assertThatThrownBy(() -> rule.convertArgumentIfRequired(context, "ignore", method.getParameters()[0], "4711"))
        .isInstanceOf(RuleFailedException.class);
  }

  @Test
  public void onUnresolvableArgument() throws NoSuchMethodException {
    Parameter parameter = mock(Parameter.class);
    when(parameter.getName()).thenReturn("aName");
    Method method = ReflectionRuleTest.class.getMethod("instanceRuleMethod", ValidationContext.class);
    ReflectionRule<?> rule = new ReflectionRule<>("id", Object.class, Map.of(), List.of(), this, method,
        List.of(mock(ParameterBinding.class)));

    assertThat(rule.onUnresolvableArgument(context, null, parameter, mock(ParameterBinding.class))).isNull();
  }

  @Test
  public void invokeInstanceMethod_ok() throws NoSuchMethodException {
    Method method = ReflectionRuleTest.class.getMethod("instanceRuleMethod", ValidationContext.class);
    ReflectionRule<?> rule = new ReflectionRule<>("id", Object.class, Map.of(), List.of(), this, method,
        List.of(mock(ParameterBinding.class)));

    assertThat(rule.invokeMethod(new Object[]{context})).isEqualTo(true);
    assertThat(rule.invokeMethod(new Object[]{null})).isEqualTo(false);
  }

  @Test
  public void invokeInstanceMethod_failed() throws NoSuchMethodException {
    Method method = ReflectionRuleTest.class.getMethod("instanceRuleMethod", ValidationContext.class);
    ReflectionRule<?> rule = new ReflectionRule<>("id", Object.class, Map.of(), List.of(), this, method,
        List.of(mock(ParameterBinding.class)));

    assertThatThrownBy(() -> rule.invokeMethod(new Object[]{})).isInstanceOf(RuleFailedException.class);
  }

  @Test
  public void staticInstanceMethod_ok() throws NoSuchMethodException {
    Method method = ReflectionRuleTest.class.getMethod("staticRuleMethod", int.class, int.class);
    ReflectionRule<?> rule = new ReflectionRule<>("id", Object.class, Map.of(), List.of(), null, method,
        List.of(mock(ParameterBinding.class), mock(ParameterBinding.class)));

    assertThat(rule.invokeMethod(new Object[]{1, 2})).isEqualTo(true);
    assertThat(rule.invokeMethod(new Object[]{2, 2})).isEqualTo(false);
  }

  @Test
  public void staticInstanceMethod_failed() throws NoSuchMethodException {
    Method method = ReflectionRuleTest.class.getMethod("staticRuleMethod", int.class, int.class);
    ReflectionRule<?> rule = new ReflectionRule<>("id", Object.class, Map.of(), List.of(), null, method,
        List.of(mock(ParameterBinding.class), mock(ParameterBinding.class)));

    assertThatThrownBy(() -> rule.invokeMethod(new Object[]{})).isInstanceOf(RuleFailedException.class);
  }

  @Test
  public void DefaultResultMapper_other() {
    assertThat(DefaultResultMapper.INSTANCE.apply("other")).isEqualTo(Result.failed("Unexpected result: other"));
  }

  @Test
  public void DefaultResultMapper_result() {
    Result result = mock(Result.class);
    assertThat(DefaultResultMapper.INSTANCE.apply(result)).isSameAs(result);
  }

  @Test
  public void DefaultResultMapper_boolean() {
    assertThat(DefaultResultMapper.INSTANCE.apply(true)).isEqualTo(Result.ok());
    assertThat(DefaultResultMapper.INSTANCE.apply(false)).isEqualTo(Result.failed());
  }

  @Test
  public void factsBinding() {
    // Arrange
    Object facts = mock(Object.class);
    FactsBinding binding = new FactsBinding();

    assertThat(binding.apply(context, facts)).isEqualTo(Resolved.of(facts));
  }

  @Test
  public void parentFactsBinding() {
    // Arrange
    Object facts = mock(Object.class);
    Object parentFacts = mock(Object.class);
    when(context.getParentFacts()).thenReturn(parentFacts);

    ParentFactsBinding binding = new ParentFactsBinding();

    assertThat(binding.apply(context, facts)).isEqualTo(Resolved.of(parentFacts));
  }

  @Test
  public void rootFactsBinding() {
    // Arrange
    Object facts = mock(Object.class);
    Object rootFacts = mock(Object.class);
    when(context.getRootFacts()).thenReturn(rootFacts);

    RootFactsBinding binding = new RootFactsBinding();

    assertThat(binding.apply(context, facts)).isEqualTo(Resolved.of(rootFacts));
  }

  @Test
  public void pathBinding_unknown() {
    // Arrange
    Map<String, Object> facts = Map.of();
    ParameterBinding binding = new PathBinding("key");

    assertThat(binding.apply(context, facts)).isEqualTo(Resolved.empty());
  }

  @Test
  public void pathBinding_known() {
    // Arrange
    Map<String, Object> facts = Map.of("key", "value");
    ParameterBinding binding = new PathBinding("key");

    assertThat(binding.apply(context, facts)).isEqualTo(Resolved.of("value"));
  }

  @Test
  public void contextBinding() {
    // Arrange
    ParameterBinding binding = new ContextBinding();

    assertThat(binding.apply(context, "ignore")).isEqualTo(Resolved.of(context));
  }

  @Test
  public void contextParameterBinding_known() {
    // Arrange
    Map<String, Object> parameters = Map.of("key", "value");
    when(context.getParameters()).thenReturn(parameters);
    ParameterBinding binding = new ContextParameterBinding("key");

    assertThat(binding.apply(context, "ignore")).isEqualTo(Resolved.of("value"));
  }

  @Test
  public void contextParameterBinding_unknown() {
    // Arrange
    Map<String, Object> parameters = Map.of();
    when(context.getParameters()).thenReturn(parameters);
    ParameterBinding binding = new ContextParameterBinding("key");

    assertThat(binding.apply(context, "ignore")).isEqualTo(Resolved.of(null));
  }

  @Test
  public void metadataBinding_known() {
    // Arrange
    Map<String, Object> metadata = Map.of("key", "value");
    Rule<?> rule = mock(Rule.class);
    when(rule.getMetadata()).thenReturn(metadata);
    doAnswer(i -> rule).when(context).getCurrentRule();
    ParameterBinding binding = new MetadataBinding("key");

    assertThat(binding.apply(context, "ignore")).isEqualTo(Resolved.of("value"));
  }

  @Test
  public void metadataBinding_unknown() {
    // Arrange
    Map<String, Object> metadata = Map.of();
    Rule<?> rule = mock(Rule.class);
    when(rule.getMetadata()).thenReturn(metadata);
    doAnswer(i -> rule).when(context).getCurrentRule();
    ParameterBinding binding = new MetadataBinding("key");

    assertThat(binding.apply(context, "ignore")).isEqualTo(Resolved.of(null));
  }


  public static boolean staticRuleMethod(int a, int b) {
    return a < b;
  }

  public boolean instanceRuleMethod(ValidationContext otherContext) {
    return this.context == otherContext;
  }
}
