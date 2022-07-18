#language:en

@Skip
@Demo
@DemoWiremock
@AlphaRelease
Feature: Mocks examples

  Background:
    # configure http client for sending requests to mocks
    Given set url 'http://localhost:8484'
    And set header's:
      | SOAPAction | test123 |
    And set request body:
    """
    test
    """

  Example: Set default mocks by properties
    # defined in application.properties
    # aseka.wiremock.default-mocks-path=data/demo/wiremock/default_mocks/*.json
    When send request GET:'/wiremock/test'
    Then check that response code == 201

  Example: Set mocks by file
    # one file or multiple by pattern
    Given set mocks 'data/demo/wiremock/mocks/*.json'
    When send request GET:'/wiremock/test'
    Then check that response code == 200

  Example: Set mocks from feature file
    And set mocks:
    """
      {
        "request": {
          "method": "GET",
          "url": "/wiremock/test"
        },
        "response": {
          "status": 200,
          "body": "Hello world!",
          "headers": {
            "Content-Type": "text/plain"
          }
        }
      }
    """
    When send request GET:'/wiremock/test'
    Then check that response code == 200

  Example: Verify that mocks received some request
    Given set mocks 'data/demo/wiremock/mocks/*.json'
    When send request GET:'/wiremock/test'
    Then check that mock received request GET:'/wiremock/test'
    And check that mock received request with headers:
      | SOAPAction | test\d{3} |
    And check that mock received request with body:
    """
    test
    """

  Example: Other useful steps
    Given set mocks 'data/demo/wiremock/mocks/*.json'
    # can be invoked before verification
    And clean mocks request journal
    # reset all mocks to defaults form properties
    And reset all mocks
