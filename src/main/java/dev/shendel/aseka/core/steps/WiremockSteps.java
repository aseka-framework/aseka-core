package dev.shendel.aseka.core.steps;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import dev.shendel.aseka.core.extension.wiremock.WiremockExtension;
import dev.shendel.aseka.core.service.FileManagerImpl;
import dev.shendel.aseka.core.service.StringInterpolator;
import dev.shendel.aseka.core.util.Validator;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.Header;
import io.restassured.http.Method;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;

@Slf4j
@RequiredArgsConstructor
public class WiremockSteps {

    private final FileManagerImpl fileLoader;
    private final StringInterpolator stringInterpolator;
    private final WiremockExtension wiremock;

    @Getter
    @Setter
    private RequestPatternBuilder requestPattern;

    @When("set mocks {file_path}")
    public void registerStubMappingsByPattern(String filesLocationPattern) {
        List<String> mappingsContent = fileLoader.readFilesAsString(filesLocationPattern);
        mappingsContent.forEach(wiremock::register);
    }

    @When("set mocks:")
    public void registerStubMappingFromString(String mapping) {
        wiremock.register(stringInterpolator.interpolate(mapping));
    }

    @Then("check that mock received request {http_method}:{interpolated_string}")
    public void verifyRequest(Method method, String endpoint) {
        requestPattern = newRequestPattern(
                new RequestMethod(method.name()),
                WireMock.urlMatching(endpoint)
        );
        verify(requestPattern);
    }

    @Then("check that mock received request with headers:")
    public void verifyRequestHeaders(List<Header> headers) {
        Validator.checkThatNotNull(requestPattern, "This step must be after verifyRequest()");

        headers.forEach(
                header -> requestPattern.withHeader(
                        header.getName(),
                        matching(header.getValue())
                )
        );
        verify(requestPattern);
    }

    @Then("check that mock received request with body:")
    public void checkRequestBody(String body) {
        Validator.checkThatNotNull(requestPattern, "This step must be after verifyRequest()");

        //TODO add common context comparator
        requestPattern.withRequestBody(containing(body));
        verify(requestPattern);
    }

    @When("clean mocks request journal")
    public void cleanRequestsJournal() {
        wiremock.cleanRequestsJournal();
    }

    @When("reset all mocks")
    public void resetMappings() {
        wiremock.clean();
    }

}
