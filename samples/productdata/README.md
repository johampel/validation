# Product data example

This example as a little more complex than the [triangle example](../triangle/README.md) and is intended to demonstrate some techniques
to write validation rules.

Technically, this example uses the - beside the [core](../../core) module - the [spring](../../spring) module. So before explaining the
use case, first a short introduction, some words about the Spring integration:

## Project setup with Spring

The  [spring](../../spring) module is basically a autoconfiguration module for Spring. When imported into the application, it craetes a
ready to go default set up of the validation:
```java
@Autowired
private Validator validator;

    ...
Report report = validator.validate(someObject, RuleSelector.of(".*"));
```

The autoconfiguration provides definitions for `Validator` itself and the beans the `Validator` requires, *unless you define your own*. 
The most important beans - beside the `Validator` are:

- The `RuleExecutor` is a `DefaultRuleExecutor`, which basically allows asynchronous rule execution.
- The `PathResolver` is a `BeanPathResolver`. Note that this can be configured via the application properties of the Spring application.
  This example makes use of such a configuration as well.
- The `RuleRepository` is a special implementation that is described more in detail below and allows a seemless integration with the Spring
  framework.

In order to validate an object, the application needs to provide some rules. For the Spring integration there is a bean having the type
`RuleRepositoryProvider`, which is indented to provide the `Validator` with the `RuleRepository` to use. The default implementation of this
is the `DefaultRuleRepositoryProvider` class, which basically collects the rule definitions from the following beans and forms a unified 
`RuleRepository` for them:

- All Spring beans implementing the `Rule` interface. 
- All Spring beans implementing the `RuleRepository` interface.
- Finally, all spring beans annotated with the `@RuleContainer` annotation. These beans are processed by an `AnnotationRepository` in order 
  to build those rules that are defined via `@RuleDef` or `@RuleRef` annotations.


## Use case and business model of this example

This example deals with product data: We are a retailer that receives product data from some suppliers. Our example application validates 
the data that comes from the suppliers and checks the data for completeness, consistency and special legal restricttions.

In real world, the corresponding rule set would contain several hundreds or thousands of rules; also we would have a fine-grained type model
for the product data. But since we want to have only a not to complex example, we have only round about a dozen of rules and a relative 
simple data model.


### The data model

Product data might be large, there might be several thousand attributes we want to store along with a product, and each kind of product
might have a different set of attributes. Therefore, a `Product` has as unstructured map of `attributes`; certain keys in this map are 
well known, e.g. the `name` of the products; others not. 

Also, a product we receive from the supplier might have nested products. For example consider a brewery, which sends us information about
a palette of boxes of beer bottles. So we as a retailer might decide to sell the beer bottle by bottle, or box wise (most presumably not 
palette wise), but obviously if the supplier sends us data about a palette of boxes, it contains three different products that are related
somehow to each other.

The resulting data model looks like:
```java
record Product(Map<String, Object> attributes, List<Relation> relations) {}
record Relation(Map<String, Object> attributes, Product product) {}
```
Having that, a brewery might send the following data:
```java
new Product(
    Map.of("name", "A Box with 20 Bottles of Beer", 
    List.of(
        new Relation(
            Map.of("amount", 20), 
            new Product(Map.of("A Bottle Of beer"), List.of()))
    )));
```


### What we validate

We validate several aspects of the product, depending on the nature of the validation we require different techniques to implement them. 
Here is an overview (details will follow):
- There is a subset of product attributes and relation attributes, we rely on; even if the supplier is free to send as many data as he 
  wishes, some need to be present and/or have a specific type. So a very fundamental rule is to check, whether mandatory attributes are 
  present and whether they have the correct type. Some attributes require additional validations:
  - The GTIN of the product must be 14 chaeracters long and have the correct checksum.
  - If an ingredient is given, not only the name, but also its amount needs to be present
- Each product might have logistic information. In this example we focus on the gross and net weight of the product: 
  - Obviously, the net weight needs to be less than pr eual to the gross weight
  - If the product has sub products (such as a box that contains bottles), the net or gross weight needs to be greater than or equal to the
    sum of the sub products.
- At least one of the products in the product hierarchy (so either the box, or the bottle) must be sellable to some customer.
- If a product is sellable, the product must have a price
- Products must belong to a category:
  - If the product belongs to the category 'ALCOHOL', it must provide information, how many percent alcohol it contains
  - If the product belongs to the category 'FOOD' and it contains more than 10g sugar, then the term "sugar" must appear in the
    product description.

This is a more or less random selection of validation rules, but they are fine to discuss them in detail to describe several techniques to
organize and write rules.


## The example program

The main program reads in a JSON formatted file containing the product data and starts the validation. The relevant parts for validation 
stripped down to the minimum are:

```java
@Autowired private Validator validator;

private void validateAndPrintReport(Product product) {
    RuleSelector selector = RuleSelectorBuilder.withPredicate(rule -> Boolean.TRUE.equals(rule.getMetadata().get("master"))).build();
    Report report = validator.validate(product, selector);

    ReportFormatter formatter = new ReportFormatter.Simple();
    formatter.format(report, System.out);
}
```
So `validateAndPrintReport` performs two steps: it performs the validation using `validator.validate(product, selector)` and passes the 
resulting `Report` ond passes this to a `ReportFormatter` to print it out. 

Beside the `product` the validator expects a `RuleSelector` that selects the rules to be used for validation. When starting the application,
the `Validator` is bound to a `RuleRepository` that contains really all rules defined in the system. But some rules might make no sense for
a product, or we simply don't want to execute them all. Therefor we have the `RuleSelector` that selects exactly those rules we want to 
execute. In our case we select all rules that have the metadata property `master` set to `true`.

As we can see in the following, there are four rules having the `master` metadata property set to `true`. These rules trigger further rules, 
but step by step.


## Basic rule set

The basic rule set is the first of the four rule sets, you'll find the source [here](src/main/java/de/hipphampel/validation/samples/productdata/rules/BasicRules.java)

The "master" rule is the following:

### `@RuleRef` for a field returning a rule

```java
@RuleRef
public final Rule<Product> basicRules = RuleBuilder.dispatchingRule("basicRules", Product.class)
    .withMetadata("master", true) 
    .withPrecondition(Conditions.rule("object:notNull")) 
    .forPaths("").validateWith("basicRules:.*:attributes") 
    .forPaths("relations/*/product").validateWith("basicRules") 
    .build();
```

The `@RuleRef` annotation makes this rule definition generally visible for the `RuleRepository`; without such an annotation the rule would 
not be found by the repository. The rule is constructed by a `RuleBuilder`, which finally produces a `DispatchingRule`. A `DispatchingRule`
is a forwarding rule, meaning that is does not validate objects on its own, but dispatches the work to subordinated rules.

The dispatching is done using `Paths`. A `Path` is a string describing the address of a single value or a group of values. For each path/group 
of paths we can select the rules to execute. Details follow:

- The `RuleBuilder.dispatchingRule("basicRules", Product.class)` starts the construction of the rule, it is named `basicRules` and the rule 
  is intended to validate `Products`.
- `.withMetadata("master", true)` adds to the rule metadata the field `master` having the value `true` (which makes )
- `.withPrecondition(Conditions.rule("object:notNull"))` indicates that this rule should be only executed, if the given precondition is given,
  in this case the rule `object:notNull` needs to be `true`.
- Lines like `.forPaths(...).validateWith(...)` means that for the paths specified with `forPath(...)` the rules specified with `validateWith`
  are executed. A path like `relations/*/products` selects all products within the relations, the rules are selected here via regular 
  expressions applied on the rule ids (but you may use any kind of `RuleSelectors`). 

  Especially the line `.forPaths("relations/*/product").validateWith("basicRules")` instructs the `basicRules` rule to applied recursively to
  all products in the product graph, _so this kind of dispatching allows an easy way to validate recusrive data structures_.
- finally `.build()` finalizes the production the rule.

So what the rule basically does:
- It calls all the rules having an id matching the regular expression `basicRules:.*:attributes` for the product itself.
  There are effectively the following rules that match this pattern:
  - `basicRules:product:attributes` that validate the attributes of the product
  - `basicRules:relation:attributes` that validate the attributes of the relations
- It executes for all products in the relation `basicRules` rule recursively again, so that we are sure that all attributes of the products 
  and the relations.in the entire graph.


### `@RuleRef` for a method returning a rule

The `basicRules:product:attributes` and `basicRules:relation:attributes` are also `DispatchingRules`, but in this case they the rule is
not a field, but a method, that returns a rule. For example, the `basicRules:product:attributes`:
```java
@RuleRef
public Rule<Product> basicProductAttributesRule() {
    Set<String> paths = metadataService.getProductAttributes().stream()
      .map(descriptor -> "attributes/" + descriptor.name())
      .collect(Collectors.toSet());
    return RuleBuilder.dispatchingRule("basicRules:product:attributes", Product.class)
      .forPaths(paths).validateWith("attribute:attributeDescriptor")
      .forPaths("attributes/gtin").validateWith("attribute:validGTIN")
      .forPaths("attributes/ingredients/*").validateWith("attribute:validIngredient")
      .build();
}
```
This is a method and not a field, because the `paths` in the first `forPaths` statement are dynamically obtained via some service call.
The involved `MetadataService` is explained in the next sections; the logic of the `DispatchingRule` is the already explained above. 

Lets take a closer look at the rules referenced from here:


### `@Component` for complex rule implementations

The `attribute:attributeDescriptor` rule is implemented in an own class, since is it a quite complex one. Rules that require an own class
need to be a `@Component` and implement the `Rule` interface, the recommended way is to subclass `AbstractRule` for that:
```java
@Component
public class AttributeDescriptorRule extends AbstractRule<Object> {
  ...

    @Override
    public Result validate(ValidationContext context, Object facts) {
      ...

    }
}
```
This special rule implementation checks a given attribute value against some `AttrbuteDescriptor`. The descriptor defines the type the value
must have, whether it is a mandatory or optional attribute and optionally, allowed minimum and maximum values. For details regarding the 
`AttributeDefinition` please refer to the corresponding class. 

There are two aspects that are interesting regarding this rule:
- The rule is called with an attribute value (the `facts` parameter of the the `validate` method). How does it find the corresponding 
  `AttributeDescriptor` for validation?
- As outlined above, there might be several reasons, potentially more than one, why this rule fails. How to transport more than one rule 
  result?

The `AttributeDescriptorRule` is called from a dispatching rule for some concrete `Paths`. For example if it is called for the path 
`attributes/gtin` the `facts` parameter has the value of the GTIN attribute; if it is called with the path `attributes/name`, thge `facts`
parameter contains the value of the name attribute. Using the `ValidationContext.getCurrentPath()` method it is possible to get the current
path, the rule is invoked for. The last component of the path corresponds to the name of the `AttributeDescriptor`. So the following code in
the `validate` method find the `AttributeDescriptor` for the value (`facts`) based on the context information, for which path the rule 
was invoked (if no attribute descriptor can be found, the attribute is not interesting for us):

```java
public Result validate(ValidationContext context, Object facts) {
    Path path = context.getCurrentPath();
    if (!(path instanceof ComponentPath componentPath)) {
      return Result.failed("Rule executed with a wrong PathResolver - no predictable results!");
    }
    String attributeName = componentPath.getLastComponent().map(ComponentPath.Component::name).orElse("");
    AttributeDescriptor descriptor = metadataService.getAttributeDescriptor(attributeName).orElse(null);
    if (descriptor == null) {
      return Result.ok();
    }
    ...
```

A `Rule` returns a `Result` when validating, the `Result` has a `code` (which is `OK`, `SKIPPED`, or  `FAILED`) and a `reason`. The reason is 
optional and something that need to implement the interface `ResultReason`. It is intended to describe, why a rule validation has been failed
or skipped.  In order to report more than one reason, you may use the special `ListResultReason`, which can - as the name suggests - transport
more than one reason.


### `@RuleDef` for a field for most simple rules.

While the `AttributeDescriptorRule` is a quite complex rule, the `object:notNull` is a quite simple one:

```java
  @RuleDef(
      id = "object:notNull",
      message = "The object must not be null")
  public final Predicate<Object> objectNotNullRule = Objects::nonNull;
```

This validation rule just checks, whether the object to be validated is `null`. To avoid to write complete classes for such simple rules, 
there is the `@RuleDef` annotation, which automatically generates a rule based on a field being a `Predicate` or a `Condition`. Since neither
the `Predicate` nor the `Condition` is a rule, we need to provide additional information, such as the id of the rule, via the `@RuleDef` 
annotation. In opposite to the `@RuleRef`, which makes an existing rule visible to the validator, the `@RuleDef` creates a new rule based on
the field, or, as we see now, based on a method:

### `@RuleDef' for a method to define a new rule.

The rules `attribute:validIngredient` and `attribute:validGTIN` are examples for rules, that are too simple to have an own class for them, 
otherwise they are too complex to have just a `Predicate`. Let's focus on the `attribute:validIngredient` rule, which checks, if for an 
ingredient like sugar the amount is given, either as `percent` or `amountInMg`:  

```java
@RuleDef(
    id = "attribute:validIngredient")
public Result validIngredientRule(
      @BindPath("name") String name,
      @BindPath("percent") Object percent,
      @BindPath("amountInMg") Object amountInMg) {
    return percent != null || amountInMg != null ? Result.ok()
    : Result.failed(String.format("Missing 'percent' or 'amountInMg' attribute for ingredient '%s'", name));
    }
```

Actually, you can use any public method with at least one parameter as a validation rule; but you have to specify, how to populate the 
parameters and how to map the result of the method.

Mapping the result is pretty simple:
- If the return type is `Result`, it is used as it is as the return value of the rule
- If the return type is a `boolean` or `Boolean`, the return value of the rule is `OK`, if the method returns `true`, otherwise `FAILED`.
- In all other cases the result of the rule is `FAILED`.

Mapping of the parameters is done via the `@Bind*` annotations. With `@BindPath`, which is used here, the method parameter is bound to the 
value the path resolves to, when applied to the object validated. For example, of the rule above is called for a map like
```
{ "name": "Sugar", "percent": "10" }
```
Then the `name` is actually `Sugar`, `percent` is `10`, and `amountInMg` is `null` then.


Beside the `@BindPath` there is also a `@BindFacts`, `@BindContext`,`@BindContextParameter`, and `@BindMetadata` annotation, see the Javadoc
for details

## Logistic rules

## Selling rules

## Category rules




