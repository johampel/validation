package de.hipphampel.validation.samples.triangle.rules;

/*-
 * #%L
 * validation-samples-triangle
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

import de.hipphampel.validation.core.annotations.RuleDef;
import de.hipphampel.validation.core.annotations.RuleRef;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.rule.RuleBuilder;
import de.hipphampel.validation.core.utils.CollectionUtils;
import de.hipphampel.validation.core.value.Value;
import de.hipphampel.validation.core.value.Values;
import de.hipphampel.validation.samples.triangle.model.Point;
import de.hipphampel.validation.samples.triangle.model.Polygon;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class TriangleRules {

  @RuleRef
  public static final Rule<Polygon> polygonAllRules =
      RuleBuilder.dispatchingRule("polygon:allRules", Polygon.class)
          .forPaths("", "*", "points/*", "points/*/*").validateWith("object:notNull")
          .forPaths("points").validateWith("points:.*")
          .forPaths("points")
          .validateWith(Values.fallback(Values.context("additionalValidations"), Values.val(
              Set.of())))
          .build();

  @RuleDef(id = "additional:isRectangular", message = "Triangle is not rectangular",
      preconditions = {"points:hasThreePoints"})
  public static final Predicate<List<Point>> additionalIsRectangular = points -> {
    double l1 = distanceOf(points.get(0), points.get(1));
    double l2 = distanceOf(points.get(0), points.get(2));
    double l3 = distanceOf(points.get(1), points.get(2));
    return l1 * l1 == l2 * l2 + l3 * l3 ||
        l2 * l2 == l1 * l1 + l3 * l3 ||
        l3 * l3 == l1 * l1 + l2 * l2;
  };

  @RuleDef(id = "additional:isSameSided", message = "Triangle is not same sided",
      preconditions = {"points:hasThreePoints"})
  public static final Predicate<List<Point>> additionalIsSameSided = points -> {
    double l1 = distanceOf(points.get(0), points.get(1));
    double l2 = distanceOf(points.get(0), points.get(2));
    double l3 = distanceOf(points.get(1), points.get(2));
    return l1==l2 && l1==l3 && l2==l3;
  };

  @RuleDef(id = "additional:isIsosceles", message = "Triangle is not isoscelesed",
      preconditions = {"points:hasThreePoints"})
  public static final Predicate<List<Point>> additionalIsIsIsosceles = points -> {
    double l1 = distanceOf(points.get(0), points.get(1));
    double l2 = distanceOf(points.get(0), points.get(2));
    double l3 = distanceOf(points.get(1), points.get(2));
    return l1==l2 && l1==l3 && l2==l3;
  };

  @RuleDef(id = "points:pointsNotInOneLine", message = "The point in the polygon are on the same line",
      preconditions = {"points:hasThreePoints"})
  public static final Predicate<List<Point>> pointsPointsNotInOneLine =
      f -> CollectionUtils.streamOfPossiblePairs(f)
          .map(l -> Optional.ofNullable(slopeOf(l.first(), l.second())))
          .distinct()
          .count() == f.size();

  @RuleDef(id = "points:pointsAreUnique", message = "The point in the polygon are not unique",
      preconditions = {"object:notNull"})
  public static final Predicate<List<?>> pointsPointsAreUnique =
      f -> f.size() == f.stream().distinct().count();

  @RuleDef(id = "points:hasThreePoints", message = "Polygon has not exactly three points",
      preconditions = {"object:notNull"})
  public static final Predicate<List<?>> pointsHasThreePoints =
      f -> f.size() == 3;

  @RuleDef(id = "object:notNull", message = "Object must not be null")
  public static final Predicate<Object> objectNotNull =
      Objects::nonNull;

  private static Double slopeOf(Point p1, Point p2) {
    double dx = p1.x() - p2.x();
    double dy = p1.y() - p2.y();
    return dx == 0.0 ? null : dy / dx;
  }

  private static Double distanceOf(Point p1, Point p2) {
    double dx = p1.x() - p2.x();
    double dy = p1.y() - p2.y();
    return Math.sqrt(dx * dx + dy * dy);
  }
}
