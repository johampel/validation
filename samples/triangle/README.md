# Triangle example

This example is intended to become familar with the basic usage of the validation library.
As an example for introduction, we validate polygons and check, whether a given polygon is
a triangle or not.

## The business model

In our case, the business model is pretty simple: we have a `Polygon` class, that consists of
`Points`. In order to easyly distingush the polygons, a `Polygon` also has a name:

```java
public record Polygon(String name, List<Point> points) { }

public record Point(Double x, Double y) { }
```

## The rules

In terms of this example, a `Polygon` is a triangle, if the following rules apply:

1. Neither the `Polygon` nor any of its components (`name` or `points`) is `null`. The same applies
   to each element of `points`, and to the `Point` objects itself.
2. All elements of `points` need to be different.
3. There are exactly three `points`.
4. The three `points`are not on the same line.

## Implementation without the validation library

[TriangleApp0](src/main/java/de/hipphampel/validation/samples/triangle/v0/TriangleApp0.java) contains
a quite simple implementation of the rules mentioned above, **without** the usage of the validation
library. The program accepts a single JSON file with an array of polygon definitions, like those
from [the examples](src/main/resources/test.json). If you run it with this file, the output will 
look like:
```
Polygon 1 is valid
Polygon 2 is invalid:
 - points/1/x: is null
 - points/2/y: is null
Polygon 3 is invalid:
 - points: polygon points are in one line
Polygon 4 is invalid:
 - points: polygon has not unique points
 - points: polygon has not exactly three points
Polygon 5 is valid
...
```
So it prints for each polygon some information, whether it is valid in terms of the rules defined 
above or not. If it is not valid, it also shows the reasons why. Let's see how it works:

In the `TriangleApp0` class you'll find five methods, directly related to the validation: all
returning a `boolean` indicating whether a given polygon is valid, and expecting two parameters:
the first is the `Polygon` instance to validate, the second a list of string to be filled with the
problems, in case that valiation fails.

While the methods named `*Rule` correspond to the rules described in the section before, the
`validatePolygon` method orchestrates the rule methods:

```java
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
```

_Orchestration_ means, that it makes not much sense to call `threePointsRule` or other, in case
the entire polygon is `null`. Also, according to the implementation of `linearIndependentRule`
it makes no sense to call this rule in case the polygon has not three points.

While most of the rules are quite trivial, especially the `notNullRule` is more complex:

```java
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
```
The reason is that - in opposite to the other rules, where we have just one condition to check - 
this rule is basically a collection of a lot of conditions: for each field we have to check, whether
it is `null` or not.

Both, the required orchestration and the clumsy implementation of the null-check-rule are quite bad
if it comes to larger rule sets or more complex objects.

Let's move on and take a look what is different when using the validation library:



## Implementation with the validation library

[TriangleApp1](src/main/java/de/hipphampel/validation/samples/triangle/v1/TriangleApp1.java) does the
same like `TriangleApp0`, but it uses the validation library. The output when running it with the same
arguments like above should look like:
```
Polygon 1: Result: OK
Polygon 2: Result: FAILED
- at root: polygon:allRules - FAILED
- at path 'points': points:notNullRule - FAILED
- at path 'points/1/x': object:notNullRule - FAILED (Object must not be null)
- at path 'points/2/y': object:notNullRule - FAILED (Object must not be null)
Polygon 3: Result: FAILED
- at root: polygon:allRules - FAILED
- at path 'points': points:pointsNotInOneLine - FAILED (The point in the polygon are on the same line)
Polygon 4: Result: FAILED
- at root: polygon:allRules - FAILED
- at path 'points': points:hasThreePoints - FAILED (Polygon has not exactly three points)
- at path 'points': points:pointsAreUnique - FAILED (The point in the polygon are not unique)
Polygon 5: Result: OK
...
```
So it basically prints nearly the same information, but a little more than above. Let's go through 
the source. First the main class with the so called `Validator`:


### `Validator` 

```java
public class TriangleApp1 {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final Validator validator = ValidatorBuilder.newBuilder()
      .withRuleRepository(AnnotationRuleRepository.ofClass(TriangleRules1.class))
      .build();
  private static final ReportFormatter reportFormatter = new ReportFormatter.Simple();

  public static void main(String[] args) {
    try {
      List<Polygon> polygons = objectMapper.readValue(new File(args[0]), new TypeReference<>() {
      });
      for (Polygon polygon : polygons) {
        Report report = validator.validate(polygon, RuleSelector.of("polygon:.*"));
        System.out.print(polygon == null ? null : polygon.name() + ": ");
        reportFormatter.format(report.filter(ResultCode.FAILED), System.out);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
  }
}
```
Compared with `TriangleApp0` we have one new important fields: the `Validator` that contains the 
complete configuration for the validation. The `Validator` is constructed via a `ValidatorBuilder`,
there are several methods to customize the generated `Validator`, but finally it is built using
the `build()` methoed. The only thing that really needs to be specified is where the validation 
rules come from, which is done via the `.withRuleRepository(...)` call to the `ValidationBuilder`.

### `RuleRepository`

A `RuleRepository` is an object that provides the validation rules so that the `Validator` can 
use them. The `AnnotationRuleRepository` is on implementation of the `RuleRepository` interface,
which finds or defines validation rules based on annotations attached to members of a class. So
the code from above:
```java
withRuleRepository(AnnotationRuleRepository.ofClass(TriangleRules1.class))
```
creates a `RuleRepository` that knows the rules defined in the class `TriangleRule1`.


### Rules

The validaiton rules the a `RuleRepository` provides are basically objects implementing the `Rule`
interface, they are the building blocks of the validation and contain the business logic. Basically,
a `Rule` has the following members:

- an unique `id` that identifies the rule.
- an information, for which type of object the rule is intended.
- a `validate` method that contains the code to execute the actual validation.
- an optional list of preconditions that must be met to execute the rule at all.
- finally, an optional map with some metadata.

Since it would be quite verbose to implement for each concrete `Rule` an own class (but of course,
this could be one valid way to go), the validation library provides annotation to automatically
generate `Rule` instances or easily make them known to the `RuleRepository`. 
Let's see how this work with the rules defined in `TriangleRules1`.


### @RuleRef

Lets start with the following:

```java
  @RuleRef
  public static final Rule<Polygon> allRules =
      RuleBuilder.dispatchingRule("polygon:allRules", Polygon.class)
          .forPaths("", "*").validateWith("object:notNullRule")
          .forPaths("points").validateWith("points:.*")
          .build();
```
This is the first rule definition. We use `@RuleRef` to tag a field having the type `Rule` or a
parameter less method returning a `Rule` object to make it known to the `AnnotationRuleRepository`.
This `allRules` field is a so called `DispatchingRule` constructed via the `RuleBuilder` 
(the `RuleBuilder` provides several further patterns to create rules). The main aim of a 
`DispatchingRule` is to call other rules. But step by step:

`RuleBuilder.dispatchingRule("polygon:allRules", Polygon.class)` tells that rule the builder should
create a rule with the id `polygon:allRules` and it is intended to be used for objects of type 
`Polygon`.

The next lines are all syntactically the same: `.forPaths(...).validateWith(...)` specify, for which
parts of the `Polygon` which validation rules have to be executed. The parts are identified via so
called _paths_, whereas a path is simply spoken a string describing a path in an object graph. 
For example, `points` means the field  `Polygon.points`, `points/0/x` would be the x coordinate of
the first point of the polygon. In case a path is empty, it means the polygon instance itself, and
the special path `*` means any component. Note that there are a lot more features regarding path,
please also refer to the `PathResolver` interface and its subclasses for details.

So `forPaths("", "*").validateWith("object:notNullRule")` just means: for the polgyon itself and
all its direct fields execute the rule `object:notNullRule`. Note that you may specify more than one
rule and also a regular expression to select the rules to execute.

Finally, the `build()` finalizes the rule production. All in all this first rule is more or less the 
orchestrating rule, which triggers the others. The `pointsNotNull` rule is the same kind of rule.

### @RuleDef

While `@RuleRef` is an annotation that makes an existing `Rule` known to the 
`AnnotationRuleRepository`, the `@RuleDef` annotation is intended to construct a `Rule` based on
more lightweight objects like ordinary methods or `Predicates`.

As an example, consider the following definition:

```java
  @RuleDef(id = "points:hasThreePoints",
      message = "Polygon has not exactly three points",
      preconditions = {
          @Precondition(rules = "points:notNull")
      })
  public static final Predicate<List<?>> threePointsRule =
      f -> f.size() == 3;
```

This definition creates a `Rule` named `points:hasThreePoints`. The generated `Rule` will be 
basically a wrapper around the `Predicate<List<?>>` which checks, whether the given list has three 
elements. Since the `Predicate` itself is not a `Rule`, the `@RuleDef` annotation needs to provide some 
additional information, such as the `id` of the `Rule`, the validation `message` and - if required - 
a list of `preconditions`. 

A further - and the most powerful kind of application of `@RuleDef` - is to use an arbitrary method as a rule.
The following example uses a method, which returns a `boolean` and expects three `Point` instances as a rule:

```java
  @RuleDef(id = "points:pointsNotInOneLine",
      message = "The points in the polygon are on the same line",
      preconditions = {
          @Precondition(rules = "points:hasThreePoints")
      })
  public static boolean pointLinearIndependentRule(
          @BindPath("0") Point a, 
          @BindPath("1") Point b, 
          @BindPath("2") Point c) {
    Optional<Double> ab = Optional.ofNullable(ModelUtils.slopeOf(a,b));
    Optional<Double> ac = Optional.ofNullable(ModelUtils.slopeOf(a,c));
    Optional<Double> bc = Optional.ofNullable(ModelUtils.slopeOf(b,c));
    return !ab.equals(ac) || !ac.equals(bc);
  }
```

In order to use the method as a rule, there must be some definition that describes, how to populate the parameters and how
to map the return value of the method.  

Mapping the return value is fairly simple:
<ol>
  <li>If the method returns a `Result` object, the rule returns exactly this object</li>
  <li>If the method returns a boolean value, the rule returns `Result.ok()` for `true` and `Result.failed()` for `false`. 
      The error message can be customized via the `message` parameter of the `RuleDef` annotation.</li>
  <li>Any other return value of the method results in a `Result.failed()` of the rule.</li>
</ol>

The parameters are bound via the `BindPath` annotation: for example,  when invoking the method as a rule with a list of `Points`, 
the parameter `a` is populated with the first `Point` of the point list. Beside the `BindPath` there are many more `Bind*` annotations
that allow to bind other kind of value sources to a parameter, please also refer to the documentation of the `RuleDef` for details.






