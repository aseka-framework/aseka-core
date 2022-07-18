#language:en

@Skip
@Demo
@DemoHttp
#@SaveContext #TODO add description here about saving context
@AlphaRelease
Feature: HTTP examples

  Example: Sending request to google (url was set in properties)
    # aseka.http.base-uri=https://google.com
    # defined in application.properties
    And send request GET:'/'
    Then check that response code == 200
    And check that response time <= 5000 ms

  Example: Send request to public calculator (url was set in feature file)
    # aseka.variables.calculator.url=http://www.dneonline.com/calculator.asmx
    # defined in application.properties
    Given set url '${{calculator.url}}'
    And set variables:
      | variableA         | 15 |
      | variableB         | 16 |
      | expectedSumResult | 31 |
    And set header's:
      | SOAPAction   | http://tempuri.org/Add  |
      | Content-type | text/xml; charset=UTF-8 |
    When send request POST:'' with body 'data/demo/http/sum_request.xml'
    Then check that response code == 200
    And check that response is not empty
    And set global matcher XML
    And check that response body is 'data/demo/http/sum_response.xml'

    # Alternative way comparing body
    Then check response body by GPath:
      | Envelope.Body.AddResponse.AddResult.text() | !=       | "1"                      |
      | Envelope.Body.AddResponse.AddResult.text() | ==       | "${{expectedSumResult}}" |
      | Envelope.Body.AddResponse.AddResult.text() | ==       | "31"                     |
      | Envelope.Body.AddResponse.AddResult.text() | contains | "${{expectedSumResult}}" |

    # One more alternative way comparing body
    Then get variables from response:
      | actualSumResult | Envelope.Body.AddResponse.AddResult.text() |
    And check variables:
      | actualSumResult | ==       | 31 |
      | actualSumResult | contains | 31 |
      | actualSumResult | notNull  |    |

  Example: One more example using GPath
    Given set url 'http://webservices.oorsprong.org/websamples.countryinfo/CountryInfoService.wso'
    And set header's:
      | Content-type | text/xml; charset=UTF-8 |
    When send request POST:'' with body 'data/demo/http/country_list_request.xml'
    And check response body by GPath:
      | '*'.'*'.'*'.'*'.tLanguage.findAll().sName.findAll{it.text().contains('Ac')} | == | ["Achinese", "Acoli"] |

  Example: Downloading files
    Given set url 'https://repo.maven.apache.org'
    When send request GET:'/maven2/HTTPClient/HTTPClient/0.3-3/HTTPClient-0.3-3.jar'
    Then check that response code == 200
    When save response body as file 'temp/http/test.jar'

  Example: Example using matchers
    Given set url 'https://world.openpetfoodfacts.org'
    When send request GET:'/api/v0/product/20106836.json'
    Then check that response code == 200
    And check that response body is:
    """
    {
      "status": "${{matcher:anyNumber}}",
      "status_verbose": "product found"
    }
    """

    And check that response body is:
    """
    {
      "code": "${{matcher:matchesRegex::\\d{8}}}"
    }
    """

    And check that response body is:
    """
    {
      "code": "${{matcher:contains::10683}}"
    }
    """

    And check that response body is:
    """
    {
      "code": "${{matcher:!contains::124}}"
    }
    """

