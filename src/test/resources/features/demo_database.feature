#language:en

@Skip
@Demo
@DemoDatabase
@AlphaRelease
Feature: Database examples

  Background:
    # setting scenario database (only if we don't want use default one)
    Given set current db 'oracleDefault'
    And execute ANY SQL script 'data/demo/sql/prepare_database.sql'

  Example: Checking response records count
    When execute SELECT SQL script:
    """
    SELECT * FROM TEST_USER
    """
    Then check that response records count > 1
    Then check that response records count < 3
    Then check that response records count == 2

  Example: Check response records
    When execute SELECT SQL script:
    """
    SELECT * FROM TEST_USER WHERE ID=1
    """
    # check columns like a string or with a matcher
    And check response records:
      | ID | FULL_NAME  | AGE           | URL            |
      | 1  | Steve Jobs | matcher:>::50 | matcher:isNull |

  Example: Get variables from response records
    When execute SELECT SQL script:
    """
    SELECT * FROM TEST_USER WHERE ID=12
    """
    Then get variables from 1 row in response:
      | nameFromDatabase | FULL_NAME |
    And check variables:
      | nameFromDatabase | == | Elon Mask |