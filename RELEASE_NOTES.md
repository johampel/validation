# Release notes `validation` library


## Upcoming

### Core module

- Performance improvements
  - The usage of shared and local objects in the `ValidationContext` is deprecated and internally not used anymore
    On the one hand, it always costs some cpu cycles to retrieve an object via a map instead of a plain getter and
    it is also not good style to do so, since it makes things too implicit. The recommended way is to derive from
    the `ValidationContext`, instantiate that in the `Validator` and use explicit getter/setters for the extensions
    you might need in the context.
  - Sending of events and in that row the usage of the `EventPublisher` during the validation has been made optional,
    also saves some cpu cycles for projects not relying on events.

## 23.4.1

### Core module

- `PathResolver` and `EventPublisher` now can be provided via a `Supplier` to the `ValidationContext` so that
   each validation run might use its own instances
-  `EventPublisher.publish()` returns `void` instead of an event.


## 23.3.1

### Core module

- Introduced a `WeakEventListener` that automatically unsubscribes in case the listener is not weakly reachable anymore
- Introduced `ValidationContext.getRootFacts` to navigate through the object graph more easily.

## 23.1.2

### Core module

- Bugfix for issue `#1`: Changed signature of `Reporter.add` method so that the `ValidationContext` is available. 
- Extended the {@code Stacked} class to provide a {@code toList} method.


## 23.1.1

### Core module

- Switched to calendar versioning
- Bugfix: `Result.ok(ResultReason)` returns no `SKIPPED` result anymore

### Spring module

- Migrated to Spring Boot 3.0.x, fixed bug in the auto configuration


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