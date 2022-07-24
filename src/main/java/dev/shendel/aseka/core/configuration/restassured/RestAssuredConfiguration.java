package dev.shendel.aseka.core.configuration.restassured;

import dev.shendel.aseka.core.configuration.AsekaProperties;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.config.SSLConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import lombok.RequiredArgsConstructor;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.SystemDefaultCredentialsProvider;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.springframework.context.annotation.Configuration;

import java.net.ProxySelector;
import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.http.impl.client.DefaultHttpClient.setDefaultHttpParams;

@Configuration
@RequiredArgsConstructor
@SuppressWarnings("deprecation")
public class RestAssuredConfiguration {

    private final AsekaProperties frameworkProperties;
    private final HttpProperties httpProperties;
    private final List<RestAssuredSpecificationConfigurer> customSpecConfigurators;

    public RequestSpecification getSpec() {
        RequestSpecification specification = given()
                .config(buildConfig())
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);

        if (httpProperties.isLogsEnabled()) {
            specification
                    .filter(new RequestLoggingFilter())
                    .filter(new ResponseLoggingFilter());
        }

        if (isNotBlank(httpProperties.getBaseUrl())) {
            specification.baseUri(httpProperties.getBaseUrl());
        }

        for (RestAssuredSpecificationConfigurer customConfigurator : customSpecConfigurators) {
            specification = customConfigurator.configure(specification);
        }

        return specification;
    }

    private RestAssuredConfig buildConfig() {
        HttpClientConfig httpClientConfig = new HttpClientConfig().httpClientFactory(
                () -> {
                    HttpParams params = new SyncBasicHttpParams();
                    setDefaultHttpParams(params);
                    params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, httpProperties.getTimeout());
                    params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, httpProperties.getTimeout());
                    DefaultHttpClient httpClient = new DefaultHttpClient(params);
                    httpClient.setCredentialsProvider(new SystemDefaultCredentialsProvider());
                    httpClient.setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()));
                    return httpClient;
                }
        );

        SSLConfig sslConfig = new SSLConfig();
        String trustStore = frameworkProperties.getTrustStore();
        if (!frameworkProperties.isSslEnabled()) {
            sslConfig = sslConfig.relaxedHTTPSValidation();
        } else if (isNotBlank(trustStore)) {
            sslConfig = sslConfig.trustStore(trustStore, "changeit");
        }

        EncoderConfig encoderConfig = encoderConfig()
                .defaultCharsetForContentType("UTF-8", "application/json");

        return RestAssured.config()
                .httpClient(httpClientConfig)
                .sslConfig(sslConfig)
                .encoderConfig(encoderConfig);
    }

}
