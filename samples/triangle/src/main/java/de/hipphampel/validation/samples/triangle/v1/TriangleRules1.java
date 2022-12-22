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
package de.hipphampel.validation.samples.triangle.v1;

import de.hipphampel.validation.core.annotations.BindPath;
import de.hipphampel.validation.core.annotations.Precondition;
import de.hipphampel.validation.core.annotations.RuleDef;
import de.hipphampel.validation.core.annotations.RuleRef;
import de.hipphampel.validation.core.condition.Condition;
import de.hipphampel.validation.core.condition.Conditions;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.rule.RuleBuilder;
import de.hipphampel.validation.core.utils.TypeReference;
import de.hipphampel.validation.core.value.Values;
import de.hipphampel.validation.samples.triangle.model.ModelUtils;
import de.hipphampel.validation.samples.triangle.model.Point;
import de.hipphampel.validation.samples.triangle.model.Polygon;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class TriangleRules1 {

  @RuleRef
  public static final Rule<Polygon> allRules =
      RuleBuilder.dispatchingRule("polygon:allRules", Polygon.class)
          .forPaths("", "*").validateWith("object:notNullRule")
          .forPaths("points").validateWith("points:.*")
          .build();

  @RuleDef(id = "points:pointsNotInOneLine",
      message = "The points in the polygon are on the same line",
      preconditions = {
          @Precondition(rules = "points:hasThreePoints")
      })
  public static boolean pointLinearIndependentRule(@BindPath("0") Point a, @BindPath("1") Point b, @BindPath("2") Point c) {
    Double ab = ModelUtils.slopeOf(a, b);
    Double ac = ModelUtils.slopeOf(a, c);
    Double bc = ModelUtils.slopeOf(b, c);
    return !Objects.equals(ab, ac) || !Objects.equals(ac, bc);
  }

  @RuleDef(id = "points:pointsAreUnique",
      message = "The points in the polygon are not unique",
      preconditions = {
          @Precondition(rules = "points:notNull")
      })
  public static final Predicate<List<?>> uniquePointsRule =
      f -> f.size() == f.stream().distinct().count();

  @RuleDef(id = "points:hasThreePoints",
      message = "Polygon has not exactly three points",
      preconditions = {
          @Precondition(rules = "points:notNull")
      })
  public static final Predicate<List<?>> threePointsRule =
      f -> f.size() == 3;

  @RuleRef
  public static final Rule<List<Point>> pointsNotNull =
      RuleBuilder.dispatchingRule("points:notNull", new TypeReference<List<Point>>() {
          })
          .forPaths("", "*", "*/*").validateWith("object:notNullRule")
          .build();

  @RuleDef(id = "object:notNullRule", message = "Object must not be null")
  public static final Condition objectNotNullRule = Conditions.isNotNull(Values.facts());
}
