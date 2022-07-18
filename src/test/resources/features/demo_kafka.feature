#language:en

@Skip
@Demo
@DemoKafka
@AlphaRelease
Feature: Kafka examples

  Example: Send and check message to kafka (JSON)
    Given reset offset to end for topic 'test.topic'
    When send to topic 'test.topic' message:
    """
    {"test":"51351"}
    """
    Then check that message in topic 'test.topic' is:
    """
    {"test":"51351"}
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
    And set global matcher XML
    Then check that message in topic 'test.topic' is:
    """
    <?xml version="1.0" encoding="UTF-8"?>
    <note>
      <body>${{matcher:contains::weekend}}</body>
    </note>
    """