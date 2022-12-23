# Release notes `validation` library

## 0.0.2 

### Core module

- When validating via `Validator`, for each validation of an object the following `Events` are sent:
  - With payload `ValidationStartedPayload`, immediately before the first rule for the object is executed.
  - With payload `ValidationFinishedPayload`, immediately after the last rule for the object is executed.
- Added `ReflectionRule` that allows to bind any kind of method with at least one parameter to be used as
  validation rule. 
- Extended the `AnnotationRuleRepository` to handle also custom annotations, made the annotation handling of
  the well known annotations `@RuleDef` and `@RuleRef` customizable as well.
- `@RuleRef` annotation can be applied to fields of type / methods returning a collection of rules. The basic
  idea is to provide a simple way to define a group similar structured rules.
- Added `PathCondition` that implements a `Condition` that checks for the existence of a `Path`
- Added `SelectorRule` (which delegates its work to the rules being selected by the selector).
- Added several `RuleSelector` implementations:
  - `CategorizingRuleSelector`, which selects rules based on some discriminator
  - `PredicateBasedRuleSelector`, which selects riles based on one or more `Predicates`


### Spring module

- Added special handling of `@RuleDef` annotations: when applied to methods, they create a `SpringRelectionRule`
  instead of a plain `ReflectionRule`. A `SpringReflectionRule` automatically converts the arguments passed to 
  the method to the correct type 

### Samples module

- Added product data example


## 0.0.1

Initial version