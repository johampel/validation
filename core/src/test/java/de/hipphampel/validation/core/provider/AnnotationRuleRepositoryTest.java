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
import static de.hipphampel.validation.core.value.Values.var;
import static org.assertj.core.api.Assertions.assertThat;

import de.hipphampel.validation.core.annotations.Precondition;
import de.hipphampel.validation.core.annotations.RuleDef;
import de.hipphampel.validation.core.annotations.RuleRef;
import de.hipphampel.validation.core.condition.Condition;
import de.hipphampel.validation.core.condition.Conditions;
import de.hipphampel.validation.core.example.Point;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.rule.RuleBuilder;
import java.util.Objects;
import org.junit.jupiter.api.Test;

public class AnnotationRuleRepositoryTest {

  @Test
  public void staticRules_getRuleIds() {
    RuleRepository repository = AnnotationRuleRepository.ofClass(StaticContainer.class);
    assertThat(repository.getRuleIds()).containsExactlyInAnyOrder(
        "staticFieldRef",
        "staticFieldDef",
        "staticMethodRef",
        "staticPredicate",
        "staticMethod",
        "StaticContainer:r4"
    );
  }

  @Test
  public void staticRules_getAndUse() {
    RuleRepository repository = AnnotationRuleRepository.ofClass(StaticContainer.class);
    ValidationContext context = new ValidationContext();

    Rule<String> r1 = repository.getRule("staticFieldRef");
    assertThat(r1.validate(context, "r1")).isEqualTo(Result.ok());
    assertThat(r1.validate(context, "r2")).isEqualTo(Result.failed());

    Rule<Integer> r2 = repository.getRule("staticFieldDef");
    assertThat(r2.validate(context, 1)).isEqualTo(Result.ok());
    assertThat(r2.validate(context, 2)).isEqualTo(Result.failed("value not 1"));

    Rule<String> r3 = repository.getRule("staticMethodRef");
    assertThat(r3.validate(context, "r3")).isEqualTo(Result.ok());
    assertThat(r3.validate(context, "r2")).isEqualTo(Result.failed("failed rule"));

    Rule<Long> r4 = repository.getRule("StaticContainer:r4");
    assertThat(r4.validate(context, 4L)).isEqualTo(Result.ok());
    assertThat(r4.validate(context, 5L)).isEqualTo(Result.failed());

    Rule<Long> r5 = repository.getRule("staticPredicate");
    context.getOrCreateSharedObject(Long.class, ignore -> 4L);
    assertThat(r5.validate(context, 4L)).isEqualTo(Result.ok());
    assertThat(r5.validate(context, 5L)).isEqualTo(Result.failed("Bad"));

    Rule<Point> r6 = repository.getRule("staticMethod");
    assertThat(r6.validate(context, new Point(1., 1.))).isEqualTo(Result.ok());
    assertThat(r6.validate(context, new Point(0., 1.))).isEqualTo(Result.failed("x!=y"));
  }

  @Test
  public void instanceRules_getRuleIds() {
    InstanceContainer instance = new InstanceContainer();
    RuleRepository repository = AnnotationRuleRepository.ofInstance(instance);
    assertThat(repository.getRuleIds()).containsExactlyInAnyOrder(
        "fieldRef",
        "fieldDef",
        "methodRef",
        "predicate",
        "method",
        "InstanceContainer:r4"
    );
  }

  @Test
  public void instanceRules_getAndUse() {
    InstanceContainer instance = new InstanceContainer();
    RuleRepository repository = AnnotationRuleRepository.ofInstance(instance);
    ValidationContext context = new ValidationContext();

    Rule<Integer> r1 = repository.getRule("fieldRef");
    instance.intVar = 4711;
    assertThat(r1.validate(context, 4711)).isEqualTo(Result.ok());
    assertThat(r1.validate(context, 4712)).isEqualTo(Result.failed());

    Rule<Integer> r2 = repository.getRule("fieldDef");
    instance.intVar = 4711;
    assertThat(r2.validate(context, 9422)).isEqualTo(Result.ok());
    assertThat(r2.validate(context, 4711)).isEqualTo(Result.failed("value not good"));

    Rule<String> r3 = repository.getRule("methodRef");
    instance.strVar = "r3";
    assertThat(r3.validate(context, "r3")).isEqualTo(Result.ok());
    assertThat(r3.validate(context, "r2")).isEqualTo(Result.failed("failed rule"));

    Rule<Long> r4 = repository.getRule("InstanceContainer:r4");
    assertThat(r4.validate(context, 4L)).isEqualTo(Result.ok());
    assertThat(r4.validate(context, 5L)).isEqualTo(Result.failed());

    Rule<Long> r5 = repository.getRule("predicate");
    context.getOrCreateSharedObject(Long.class, ignore -> 4L);
    assertThat(r5.validate(context, 4L)).isEqualTo(Result.ok());
    assertThat(r5.validate(context, 5L)).isEqualTo(Result.failed("Bad"));

    Rule<Point> r6 = repository.getRule("method");
    assertThat(r6.validate(context, new Point(1., 1.))).isEqualTo(Result.ok());
    assertThat(r6.validate(context, new Point(0., 1.))).isEqualTo(Result.failed("x!=y"));
    assertThat(r6.getPreconditions()).hasSize(2);
  }

  private static class InstanceContainer {

    private int intVar;
    private String strVar;

    @RuleRef
    private final Rule<Integer> r1 =
        RuleBuilder.conditionRule("fieldRef", Integer.class)
            .validateWith(Conditions.eq(facts(), var(() -> intVar)))
            .build();

    @RuleDef(id = "fieldDef", factsType = Integer.class, message = "value not good")
    private final Condition r2 = Conditions.eq(var(() -> intVar * 2), facts());

    @RuleRef
    public Rule<?> r3() {
      return RuleBuilder.conditionRule("methodRef", String.class)
          .validateWith(Conditions.eq(facts(), var(() -> strVar)))
          .withFailReason("failed rule")
          .build();
    }

    @RuleDef
    public boolean r4(Long value) {
      return value == 4L;
    }

    @RuleDef(id = "predicate", message = "Bad")
    public Boolean r5(ValidationContext context, Long value) {
      return Objects.equals(value, context.getSharedObject(Long.class));
    }

    @RuleDef(id = "method", preconditions = {
        @Precondition(rules="a", paths="b"),
        @Precondition(rules="c", paths="d"),
    })
    public static Result r6(Point value) {
      return Objects.equals(value.x(), value.y()) ? Result.ok() : Result.failed("x!=y");
    }
  }

   static class StaticContainer {

    @RuleRef
    private static final Rule<String> r1 =
        RuleBuilder.conditionRule("staticFieldRef", String.class)
            .validateWith(Conditions.eq(facts(), val("r1")))
            .build();

    @RuleDef(id = "staticFieldDef", factsType = Integer.class, message = "value not 1")
    private static final Condition r2 = Conditions.eq(val(1), facts());

    @RuleRef
    public static Rule<?> r3() {
      return RuleBuilder.conditionRule("staticMethodRef", String.class)
          .validateWith(Conditions.eq(facts(), val("r3")))
          .withFailReason("failed rule")
          .build();
    }

    @RuleDef
    public static boolean r4(Long value) {
      return value == 4L;
    }

    @RuleDef(id = "staticPredicate", message = "Bad")
    public static Boolean r5(ValidationContext context, Long value) {
      return Objects.equals(value, context.getSharedObject(Long.class));
    }

    @RuleDef(id = "staticMethod")
    public static Result r6(Point value) {
      return Objects.equals(value.x(), value.y()) ? Result.ok() : Result.failed("x!=y");
    }
  }
}
