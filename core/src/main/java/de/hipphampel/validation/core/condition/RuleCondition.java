package de.hipphampel.validation.core.condition;

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

import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.value.Value;
import java.util.Objects;

/**
 * A {@link Condition} that evaluates a {@link Rule}
 * <p>
 * This condition calls the {@code Rule} with the id specified at construction time and returns
 * {@code true}, if the rule evaluates to ok.
 *
 * @param ruleId The id of the {@code Rule}
 */
public record RuleCondition(Value<String> ruleId) implements Condition {

  public RuleCondition {
    Objects.requireNonNull(ruleId);
  }

  @Override
  public boolean evaluate(ValidationContext context, Object facts) {
    String id = ruleId().get(context, facts);
    Rule<Object> rule = context.getRuleProvider().getRule(id);
    return context.getRuleExecutor().validate(context, rule, facts).isOk();
  }

  @Override
  public String toString() {
    return "rule(" + ruleId + ")";
  }
}
