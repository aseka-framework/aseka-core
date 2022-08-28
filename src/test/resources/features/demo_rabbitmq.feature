#language:en

@Skip
@Demo
@DemoRabbitMq
@AlphaRelease
Feature: RabbitMq examples

  Example: Send and check message to Rabbit MQ (JSON)
    Given purge queue 'message.update'
    When send to queue 'message.update' message:
    """
    {"test":"12"}
    """
    Then check that message in queue 'message.update' is:
    """
    {"test":"12"}
    """

  Example: Send and check message to Rabbit MQ (XML)
    Given purge queue 'message.update'
    When send to queue 'message.update' message:
    """
    <?xml version="1.0" encoding="UTF-8"?>
    <note>
      <body>Don't forget me this weekend!</body>
    </note>
    """
    Then check that message in queue 'message.update' is:
    """
    <?xml version="1.0" encoding="UTF-8"?>
    <note>
      <body>${{matcher:contains::weekend}}</body>
    </note>
    """