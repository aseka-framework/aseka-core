package dev.shendel.aseka.core.configuration.restassured;

import io.restassured.specification.RequestSpecification;

public interface RestAssuredSpecificationConfigurer {

    RequestSpecification configure(RequestSpecification specification);

}
