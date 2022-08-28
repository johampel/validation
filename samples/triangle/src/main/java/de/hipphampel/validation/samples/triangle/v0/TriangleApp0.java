package de.hipphampel.validation.samples.triangle.v0;

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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hipphampel.validation.core.Validator;
import de.hipphampel.validation.core.ValidatorBuilder;
import de.hipphampel.validation.core.provider.AnnotationRuleRepository;
import de.hipphampel.validation.samples.triangle.model.ModelUtils;
import de.hipphampel.validation.samples.triangle.model.Point;
import de.hipphampel.validation.samples.triangle.model.Polygon;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TriangleApp0 {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static void main(String[] args) {
    try {
      List<Polygon> polygons = objectMapper.readValue(new File(args[0]), new TypeReference<>() {
      });

      for (Polygon polygon : polygons) {
        List<String> problems = new ArrayList<>();
        if (validatePolygon(polygon, problems)) {
          System.out.println(polygon.name() + " is valid");
        } else {
          System.out.println((polygon == null ? null : polygon.name()) + " is invalid:");
          for(String problem: problems) {
            System.out.println(" - "+problem);
          }
        }

      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
  }

  public static boolean validatePolygon(Polygon polygon, List<String> problems) {
    boolean ok = notNullRule(polygon, problems);
    if (ok) {
      boolean uniquePoints = uniquePointsRule(polygon, problems);
      boolean threePoints = threePointsRule(polygon, problems);
      if (threePoints) {
        ok = linearIndependentRule(polygon, problems) && uniquePoints;
      } else {
        ok = false;
      }
    }
    return ok;
  }

  public static boolean notNullRule(Polygon polygon, List<String> problems) {
    if (polygon == null) {
      problems.add("is null");
      return false;
    }
    boolean ok = true;
    if (polygon.name() == null) {
      problems.add("name: is null");
      ok = false;
    }
    if (polygon.points() == null) {
      problems.add("points: is null");
      ok = false;
    } else {
      for (int i = 0; i < polygon.points().size(); i++) {
        Point point = polygon.points().get(i);
        if (point == null) {
          problems.add("points/" + i + ": is null");
          ok = false;
        } else {
          if (point.x() == null) {
            problems.add("points/" + i + "/x: is null");
            ok = false;
          }
          if (point.y() == null) {
            problems.add("points/" + i + "/y: is null");
            ok = false;
          }
        }
      }
    }
    return ok;
  }

  public static boolean uniquePointsRule(Polygon polygon, List<String> problems) {
    if (polygon.points().size() != (long) polygon.points().stream().distinct().count()) {
      problems.add("points: polygon has not unique points");
      return false;
    }
    return true;
  }

  public static boolean threePointsRule(Polygon polygon, List<String> problems) {
    if (polygon.points().size() != 3) {
      problems.add("points: polygon has not exactly three points");
      return false;
    }
    return true;
  }

  public static boolean linearIndependentRule(Polygon polygon, List<String> problems) {
    Point a = polygon.points().get(0);
    Point b = polygon.points().get(1);
    Point c = polygon.points().get(2);
    Double slopeAB = ModelUtils.slopeOf(a, b);
    Double slopeAC = ModelUtils.slopeOf(a, c);
    Double slopeBC = ModelUtils.slopeOf(b, c);
    if (Objects.equals(slopeAB, slopeAC) && Objects.equals(slopeAB, slopeBC)) {
      problems.add("points: polygon points are in one line");
      return false;
    }
    return true;
  }
}
