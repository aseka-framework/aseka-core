package dev.shendel.aseka.core.steps;

import dev.shendel.aseka.core.configuration.restassured.RestAssuredConfiguration;
import dev.shendel.aseka.core.context.ContextVariables;
import dev.shendel.aseka.core.cucumber.type.HttpBodyValidator;
import dev.shendel.aseka.core.cucumber.type.InterpolatedString;
import dev.shendel.aseka.core.cucumber.type.Pair;
import dev.shendel.aseka.core.exception.AsekaException;
import dev.shendel.aseka.core.matcher.global.GlobalMatcherFactory;
import dev.shendel.aseka.core.service.FileManagerImpl;
import dev.shendel.aseka.core.util.Asserts;
import dev.shendel.aseka.core.util.Validator;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Allure;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import static io.restassured.matcher.RestAssuredMatchers.matchesXsd;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

@Slf4j
@RequiredArgsConstructor
public class HttpSteps {

    private static final String TEMP_HTTP_FOLDER = "temp/http";

    private final ContextVariables contextVariables;
    private final FileManagerImpl fileLoader;
    private final RestAssuredConfiguration restAssuredConfig;
    private final GlobalMatcherFactory globalMatcherFactory;

    @Getter @Setter
    private RequestSpecification specification;
    @Getter @Setter
    private Response response;
    @Getter @Setter
    private ValidatableResponse validatableResponse;

    @PostConstruct
    public void initBeforeEveryCase() {
        specification = restAssuredConfig.getSpec();
    }

    @When("set url {interpolated_string}")
    public void setUrl(String url) {
        specification = restAssuredConfig.getSpec()
                .baseUri(url);
    }

    @When("set base path {interpolated_string}")
    public void setBasePath(String endpoint) {
        specification.basePath(endpoint);
    }

    @When("set header's:")
    public void setHeaders(List<Header> headers) {
        specification.headers(new Headers(headers));
    }

    @When("set query parameters:")
    public void setQueryParameters(List<Pair> parameters) {
        for (Pair parameter : parameters) {
            specification.queryParam(parameter.getFirst(), parameter.getSecond());
        }
    }

    @When("set form parameters:")
    public void setFormParameters(List<Pair> parameters) {
        for (Pair parameter : parameters) {
            specification.formParam(parameter.getFirst(), parameter.getSecond());
        }
    }

    @When("set cookies:")
    public void setCookie(List<Pair> cookies) {
        for (Pair cookie : cookies) {
            specification.cookie(cookie.getFirst(), cookie.getSecond());
        }
    }

    @When("set Basic auth: {interpolated_string} {interpolated_string}")
    public void setBasicAuth(String login, String password) {
        specification.auth().preemptive().basic(login, password);
    }

    @When("set request body {file_path}")
    public void setBodyByPath(String path) {
        String body = fileLoader.readFileAsString(path);
        specification.and().body(body);
    }

    @When("set request body:")
    public void setBody(InterpolatedString body) {
        specification.and().body(body.get());
    }

    @When("send request {http_method}:{interpolated_string} with body {file_path}")
    public void sendRequestWithBodyByPath(Method method, String endpoint, String bodyPath) {
        String body = fileLoader.readFileAsString(bodyPath);
        sendRequest(method, endpoint, body);
    }

    @When("send request {http_method}:{interpolated_string} with body:")
    public void sendRequestWithBody(Method method, String endpoint, InterpolatedString body) {
        sendRequest(method, endpoint, body.get());
    }

    @When("send request {http_method}:{interpolated_string}")
    public void sendRequest(Method method, String endpoint) {
        sendRequest(method, endpoint, null);
    }

    private void sendRequest(Method method, String endpoint, String body) {
        if (body != null) {
            specification.body(body);
        }

        response = specification.when().request(method, endpoint);
        validatableResponse = response.then();
    }

    @Then("get variables from response:")
    public void putVariablesFromResponse(List<Pair> rows) {
        for (Pair row : rows) {
            String variableName = row.getFirst();
            String gpath = row.getSecond();

            Object value = response.path(gpath);
            contextVariables.set(variableName, value);
        }
    }

    @Then("save response body as file {file_path}")
    public void saveFileFromResponse(String path) {
        Validator.checkDownloadFolder(path, TEMP_HTTP_FOLDER);

        try (InputStream inputStream = response.getBody().asInputStream()) {
            File file = fileLoader.createFile(path, inputStream);
        } catch (Exception e) {
            throw new AsekaException("Can't download file", e);
        }
    }

    @Then("check that response code == {int}")
    public void checkResponseStatus(int status) {
        validatableResponse.statusCode(status);
    }

    @Then("check that response time <= {int} ms")
    public void checkThatResponseTimeIs(int responseTime) {
        validatableResponse.time(lessThanOrEqualTo((long) responseTime));
    }

    @Then("check that response is not empty")
    public void checkThatResponseNotEmpty() {
        int responseLength = response.asByteArray().length;
        Asserts.assertThat(responseLength, greaterThan(0), "Response is empty");
    }

    @Then("check that response is empty")
    public void checkThatResponseIsEmpty() {
        int responseLength = response.asByteArray().length;
        Asserts.assertThat(responseLength, equalTo(0), "Response is not empty");
    }

    @Then("check response body by GPath:")
    public void checkResponseBodyWithValidator(List<HttpBodyValidator> validators) {
        validators.forEach(
                validator -> validatableResponse.assertThat()
                        .body(validator.getGpath(), validator.getMatcher())
        );
    }

    @Then("check response body by JSON Schema {file_path}")
    public void checkResponseByJsonSchema(String jsonFilePath) {
        String jsonSchema = fileLoader.readFileAsString(jsonFilePath);
        Allure.addAttachment("jsonSchema", jsonSchema);
        validatableResponse.assertThat().body(matchesJsonSchema(jsonSchema));
    }

    @Then("check response body by XSD {file_path}")
    public void checkResponseByXsdSchema(String filePath) {
        String xsdSchema = fileLoader.readFileAsString(filePath);
        Allure.addAttachment("xsdSchema", xsdSchema);
        validatableResponse.assertThat().body(matchesXsd(xsdSchema));
    }

    @Then("check that response body is {file_path}")
    public void checkBodyWithGlobalMatcher(String filePath) {
        String expected = fileLoader.readFileAsString(filePath);
        Allure.addAttachment("expected", expected);
        validatableResponse.assertThat().body(globalMatcherFactory.create(expected));
    }

    @Then("check that response body is:")
    public void checkBodyWithGlobalMatcher(InterpolatedString expected) {
        validatableResponse.assertThat().body(globalMatcherFactory.create(expected.get()));
    }

}
