[![](https://jitpack.io/v/dev.shendel/aseka-core.svg)](https://jitpack.io/#dev.shendel/aseka-core)
# ASEKA FRAMEWORK [Work in progress]

Framework for integration testing with Cucumber

[//]: # (TODO add description)

### Examples

```gherkin
Feature: Example feature

  Example: example scenario
    Given set variables:
      | orderId  | 15                 |
      | randomId | ${{randomLong:19}} |
    And execute SQL script 'data/demo/sql/prepare_database.sql'
    When send request GET:'/orders/${{orderId}}'
    Then check that response code == 200
    And check that response body is:
    """
    {
      "id": ${{orderId}}
      "status": "${{matcher:anyString}}"
    }
    """
    When send to topic 'test.topic' message:
    """
    {"test":"${{randomId}}"}
    """
    Then check that message in topic 'test.topic2' is:
    """
    {"test":"${{randomId}}"}
    """
```

## Getting Started
example project structure
```
.
+- itests
   +- src
      +- test
         +- java
            +-- org.company
                +- IntegrationTests.java
            +- resources
               +- features
                  +- example1.feature
                  +- example2.feature
               +- application.yml
               +- application-ci.yml
   +- build.gradle

     
```

build.gradle
```groovy
//add repo
repositories {
    maven { url 'https://jitpack.io' }
}

//add dependency
dependencies {
    implementation 'dev.shendel:aseka-core:0.0.4'
}
```

IntegrationTests.java
```java
@CucumberOptions(
        features = DEFAULT_FEATURES,
        tags = DEFAULT_TAGS,
        glue = DEFAULT_GLUE,
        plugin = {"pretty", Constants.ALLURE_PLUGIN}
)
@RunWith(Cucumber.class)
public class IntegrationTests {
}
```

### placeholders

### http

### database

### AMQP

### kafka

### wiremock
