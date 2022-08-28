#language:en

@Skip
@Demo
@DemoKafka
@AlphaRelease
Feature: Kafka examples

  Background:
    Given set variables:
      | randomId | ${{randomLong:5}} |

  @DemoKafka
  Example: Send and check message to kafka (JSON)
    Given reset offset to end for topic 'test.topic'
    When send to topic 'test.topic' message:
    """
    {"test":"${{randomId}}"}
    """
    Then check that message in topic 'test.topic' is:
    """
    {"test":"${{randomId}}"}
    """

  Example: Send and check message to kafka (XML)
    Given reset offset to end for topic 'test.topic'
    When send to topic 'test.topic' message:
    """
    <?xml version="1.0" encoding="UTF-8"?>
    <note>
      <body>Don't forget me this weekend!</body>
    </note>
    """
    Then check that message in topic 'test.topic' is:
    """
    <?xml version="1.0" encoding="UTF-8"?>
    <note>
      <body>${{matcher:contains::weekend}}</body>
    </note>
    """