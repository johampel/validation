# Library to drive business validations

This library contains the basic infrastructure to define and run business validations.

This project consists of teh following submodules:

- [core](core): is the core library, which contains all you need to start with validations. In order to use it, you
  should add the following dependency to your project:

  _Maven:_
  ```xml
      <dependency>
        <groupId>de.hipphampel.valdation</groupId>
        <artifactId>validation-core</artifactId>
        <version>VERSION/version>
      </dependency>
  ```
  _Gradle:_
  ```groovy
      implementation 'de.hipphampel.validation:validation-core:VERSION'
  ```
- [spring](spring): Provides some support to include the validation library into Spring Boot projects,
  you may add the following dependency:

  _Maven:_
  ```xml
      <dependency>
        <groupId>de.hipphampel.valdation</groupId>
        <artifactId>validation-spring</artifactId>
        <version>VERSION/version>
      </dependency>
  ```
  _Gradle:_
  ```groovy
      implementation 'de.hipphampel.validation:validation-spring:VERSION'
  ```
- [samples](samples): Provides samples explaining the concepts.


## Quick start

Please check the [triangle example](samples//triangle/README.md)

## Release notes

Release notes can be found [here](RELEASE_NOTES.md) 