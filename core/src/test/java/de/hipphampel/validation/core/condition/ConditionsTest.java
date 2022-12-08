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
package de.hipphampel.validation.core.condition;

import static org.assertj.core.api.Assertions.assertThat;

import de.hipphampel.validation.core.TestUtils;
import de.hipphampel.validation.core.event.Event;
import de.hipphampel.validation.core.event.payloads.RuleFinishedPayload;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.path.Resolvable;
import de.hipphampel.validation.core.provider.InMemoryRuleRepository;
import de.hipphampel.validation.core.provider.RuleSelector;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.rule.RuleBuilder;
import de.hipphampel.validation.core.utils.Stacked;
import de.hipphampel.validation.core.value.Values;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ConditionsTest {

  private ValidationContext context;

  @BeforeEach
  public void beforeEach() {
    context = new ValidationContext();
  }

  @ParameterizedTest
  @CsvSource({
      "true",
      "false",
  })
  public void always(boolean expected) {
    Condition condition = Conditions.always(expected);
    assertThat(condition.evaluate(context, "ignore")).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
      "true,  true,  true,  true",
      "false, false, false, false",
      "true,  false, false, false",
      "true,  true,  false, false",
  })
  public void and(boolean a, boolean b, boolean c, boolean expected) {
    Condition condition = Conditions.and(
        Conditions.always(a), Conditions.always(b), Conditions.always(c)
    );
    assertThat(condition.evaluate(context, "ignore")).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
      "true,  true,  true,  false",
      "false, false, false, true",
      "true,  false, false, false",
      "true,  true,  false, false",
  })
  public void not(boolean a, boolean b, boolean c, boolean expected) {
    Condition condition = Conditions.not(
        Conditions.always(a), Conditions.always(b), Conditions.always(c)
    );
    assertThat(condition.evaluate(context, "ignore")).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
      "true,  true,  true,  true",
      "false, false, false, false",
      "true,  false, false, true",
      "true,  true,  false, true",
  })
  public void or(boolean a, boolean b, boolean c, boolean expected) {
    Condition condition = Conditions.or(
        Conditions.always(a), Conditions.always(b), Conditions.always(c)
    );
    assertThat(condition.evaluate(context, "ignore")).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
      "true,  true,  true,  false",
      "false, false, false, false",
      "true,  false, false, true",
      "true,  true,  false, false",
  })
  public void xor(boolean a, boolean b, boolean c, boolean expected) {
    Condition condition = Conditions.xor(
        Conditions.always(a), Conditions.always(b), Conditions.always(c)
    );
    assertThat(condition.evaluate(context, "ignore")).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
      "   , true",
      "str, false",
  })
  public void isNull(String arg, boolean expected) {
    Condition condition = Conditions.isNull(Values.val(arg));
    assertThat(condition.evaluate(context, "ignore")).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
      "   , false",
      "str, true",
  })
  public void isNotNull(String arg, boolean expected) {
    Condition condition = Conditions.isNotNull(Values.val(arg));
    assertThat(condition.evaluate(context, "ignore")).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
      "   ,    , true",
      "   , abc, false",
      "abc, abc, true",
      "abc, def, false",
  })
  public void eq(String left, String right, boolean expected) {
    Condition condition = Conditions.eq(Values.val(left), Values.val(right));
    assertThat(condition.evaluate(context, "ignore")).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
      "   ,    , False",
      "   , abc, true",
      "abc, abc, false",
      "abc, def, true",
  })
  public void ne(String left, String right, boolean expected) {
    Condition condition = Conditions.ne(Values.val(left), Values.val(right));
    assertThat(condition.evaluate(context, "ignore")).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
      "   ,    , False",
      "   , abc, false",
      "abc, abb, false",
      "abc, abc, true",
      "abc, abd, true",
  })
  public void le(String left, String right, boolean expected) {
    Condition condition = Conditions.le(Values.val(left), Values.val(right));
    assertThat(condition.evaluate(context, "ignore")).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
      "   ,    , False",
      "   , abc, false",
      "abc, abb, false",
      "abc, abc, false",
      "abc, abd, true",
  })
  public void lt(String left, String right, boolean expected) {
    Condition condition = Conditions.lt(Values.val(left), Values.val(right));
    assertThat(condition.evaluate(context, "ignore")).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
      "   ,    , False",
      "   , abc, false",
      "abc, abb, true",
      "abc, abc, false",
      "abc, abd, false",
  })
  public void gt(String left, String right, boolean expected) {
    Condition condition = Conditions.gt(Values.val(left), Values.val(right));
    assertThat(condition.evaluate(context, "ignore")).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
      "   ,    , False",
      "   , abc, false",
      "abc, abb, true",
      "abc, abc, true",
      "abc, abd, false",
  })
  public void ge(String left, String right, boolean expected) {
    Condition condition = Conditions.ge(Values.val(left), Values.val(right));
    assertThat(condition.evaluate(context, "ignore")).isEqualTo(expected);
  }

  @Test
  public void rule_noPath() {
    Rule<String> okRule = RuleBuilder.conditionRule("ok", String.class)
        .validateWith(Conditions.alwaysTrue())
        .build();
    Rule<String> failRule = RuleBuilder.conditionRule("fail", String.class)
        .validateWith(Conditions.alwaysFalse())
        .build();
    context.getSharedExtension(InMemoryRuleRepository.class).addRules(okRule, failRule);
    List<Event<?>> events = new ArrayList<>();
    TestUtils.collectEventsInto(context, events);
    String facts = "Test";

    RuleSelector okSelector = RuleSelector.of("ok");
    RuleSelector failSelector = RuleSelector.of("fail");
    Condition conditionOk = Conditions.rule(Values.val(okSelector));
    Condition conditionFail = Conditions.rule(Values.val(failSelector));

    assertThat(conditionOk.evaluate(context, facts)).isTrue();
    assertThat(events).contains(new Event<>(
        new RuleFinishedPayload(okRule, Stacked.empty(), facts, Result.ok(),
            TestUtils.FIXED_DURATION),
        TestUtils.FIXED_DATE, context.getRuleExecutor()));

    assertThat(conditionFail.evaluate(context, facts)).isFalse();
    assertThat(events).contains(new Event<>(
        new RuleFinishedPayload(failRule, Stacked.empty(), facts, Result.failed(),
            TestUtils.FIXED_DURATION),
        TestUtils.FIXED_DATE, context.getRuleExecutor()));
  }

  @Test
  public void rule_withPath() {
    Rule<String> isDonaldRule = RuleBuilder.conditionRule("isDonald", String.class)
        .validateWith(Conditions.eq(Values.facts(), Values.val("Donald")))
        .build();
    context.getSharedExtension(InMemoryRuleRepository.class).addRules(isDonaldRule);
    List<Event<?>> events = new ArrayList<>();
    TestUtils.collectEventsInto(context, events);
    Map<String, String> facts = Map.of(
        "firstName", "Donald",
        "lastName", "Duck"
    );

    RuleSelector donaldSelector = RuleSelector.of("isDonald");
    RuleSelector failSelector = RuleSelector.of("fail");
    Condition conditionOk = Conditions.rule(Values.val(donaldSelector),
        Values.val(Set.of("firstName")));
    Condition conditionFail = Conditions.rule(Values.val(donaldSelector),
        Values.val(Set.of("lastName")));

    assertThat(conditionOk.evaluate(context, facts)).isTrue();
    assertThat(events).contains(new Event<>(
        new RuleFinishedPayload(
            isDonaldRule,
            Stacked.<Resolvable>empty()
                .push(new Resolvable(facts, context.getPathResolver().parse("firstName"))),
            "Donald",
            Result.ok(),
            TestUtils.FIXED_DURATION),
        TestUtils.FIXED_DATE, context.getRuleExecutor()));

    assertThat(conditionFail.evaluate(context, facts)).isFalse();
    assertThat(events).contains(new Event<>(
        new RuleFinishedPayload(
            isDonaldRule,
            Stacked.<Resolvable>empty()
                .push(new Resolvable(facts, context.getPathResolver().parse("lastName"))),
            "Duck",
            Result.failed(),
            TestUtils.FIXED_DURATION),
        TestUtils.FIXED_DATE, context.getRuleExecutor()));
  }

  @ParameterizedTest
  @CsvSource({
      "exists,    true",
      "notexists, false"
  })
  public void existsPath(String path, boolean expected) {
    Condition condition = Conditions.existsPath(path);
    assertThat(condition.evaluate(context, Map.of("exists", true))).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
      "exists,    true",
      "notexists, false"
  })
  public void existsPathForObject(String path, boolean expected) {
    Condition condition = Conditions.existsPathForObject(Values.val(Map.of("exists", true)), path);
    assertThat(condition.evaluate(context, "ignore")).isEqualTo(expected);
  }
}
