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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.hipphampel.validation.core.annotations.Metadata;
import de.hipphampel.validation.core.annotations.Precondition;
import de.hipphampel.validation.core.annotations.RuleDef;
import de.hipphampel.validation.core.condition.Condition;
import de.hipphampel.validation.core.condition.Conditions;
import de.hipphampel.validation.core.condition.RuleCondition;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.rule.ConditionRule;
import de.hipphampel.validation.core.rule.ReflectionRule;
import de.hipphampel.validation.core.rule.ReflectionRule.ContextBinding;
import de.hipphampel.validation.core.rule.ReflectionRule.DefaultResultMapper;
import de.hipphampel.validation.core.rule.ReflectionRule.FactsBinding;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.ResultReason;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.rule.StringResultReason;
import de.hipphampel.validation.core.value.Value;
import de.hipphampel.validation.core.value.Values;
import java.lang.reflect.Member;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class RuleDefHandlerTest {

  private final RuleDefHandler handler = new RuleDefHandler();

  @Test
  public void getPreconditions() {
    Precondition precondition1 = mock(Precondition.class);
    when(precondition1.rules()).thenReturn(new String[]{"rule1", "rule2"});
    when(precondition1.paths()).thenReturn(new String[]{"path1", "path2"});
    Precondition precondition2 = mock(Precondition.class);
    when(precondition2.rules()).thenReturn(new String[]{"rule3", "rule4"});
    when(precondition2.paths()).thenReturn(new String[]{"path3", "path4"});
    RuleDef ruleDef = mock(RuleDef.class);
    when(ruleDef.preconditions()).thenReturn(new Precondition[]{precondition1, precondition2});

    List<Condition> conditions = handler.getPreconditions(null, ruleDef);

    assertThat(conditions).containsExactly(
        new RuleCondition(Values.val(RuleSelector.of("rule1", "rule2")), Values.val(Set.of("path1", "path2"))),
        new RuleCondition(Values.val(RuleSelector.of("rule3", "rule4")), Values.val(Set.of("path3", "path4")))
    );
  }

  @Test
  public void getMetadata() {
    Metadata metadata1 = mock(Metadata.class);
    when(metadata1.key()).thenReturn("key1");
    when(metadata1.value()).thenReturn("value1");
    Metadata metadata2 = mock(Metadata.class);
    when(metadata2.key()).thenReturn("key2");
    when(metadata2.value()).thenReturn("value2");
    RuleDef annotation = mock(RuleDef.class);
    when(annotation.metadata()).thenReturn(new Metadata[]{metadata1, metadata2});

    assertThat(handler.getMetadata(mock(Member.class), annotation)).isEqualTo(Map.of(
        "key1", "value1",
        "key2", "value2"
    ));
  }

  @ParameterizedTest
  @CsvSource({
      "'',               'memberName'",
      "'annotationName', 'annotationName'"
  })
  public void getRuleId(String annotationName, String expected) {
    Member member = mock(Member.class);
    when(member.getName()).thenReturn("memberName");
    RuleDef annotation = mock(RuleDef.class);
    when(annotation.id()).thenReturn(annotationName);

    assertThat(handler.getRuleId(member, annotation)).isEqualTo(expected);
  }

  @Test
  public void newConditionBasedRule() {
    String ruleId = "ruleId";
    Class<String> factsType = String.class;
    Map<String, Object> metadata = Map.of("a", 1);
    List<Condition> preconditions = List.of(Conditions.alwaysTrue());
    Condition condition = Conditions.alwaysFalse();
    Value<ResultReason> failReason = Values.val(new StringResultReason("error!"));

    Rule<String> rule = handler.newConditionBasedRule(ruleId, factsType, metadata, preconditions, condition, failReason);

    assertThat(rule).isInstanceOf(ConditionRule.class);
    assertThat(rule.getId()).isEqualTo(ruleId);
    assertThat(rule.getFactsType()).isEqualTo(factsType);
    assertThat(rule.getMetadata()).isEqualTo(metadata);
    assertThat(rule.getPreconditions()).isEqualTo(preconditions);
    assertThat(rule.validate(mock(ValidationContext.class), "ignore"))
        .isEqualTo(Result.failed("error!"));
  }

  @Test
  public void newReflectionBasedRule() throws NoSuchMethodException {
    String ruleId = "ruleId";
    Class<String> factsType = String.class;
    Map<String, Object> metadata = Map.of("a", 1);
    List<Condition> preconditions = List.of(Conditions.alwaysTrue());

    Rule<String> rule = handler.newReflectionBasedRule(ruleId, factsType, metadata, preconditions,
        this, RuleDefHandlerTest.class.getMethod("instanceRule", ValidationContext.class, String.class),
        List.of(new ContextBinding(), new FactsBinding()), new DefaultResultMapper());

    assertThat(rule).isInstanceOf(ReflectionRule.class);
    assertThat(rule.getId()).isEqualTo(ruleId);
    assertThat(rule.getFactsType()).isEqualTo(factsType);
    assertThat(rule.getMetadata()).isEqualTo(metadata);
    assertThat(rule.getPreconditions()).isEqualTo(preconditions);
    assertThat(rule.validate(mock(ValidationContext.class), "facts"))
        .isEqualTo(Result.ok());
  }

  public boolean instanceRule(ValidationContext context, String facts) {
    return "facts".equals(facts);
  }
}
