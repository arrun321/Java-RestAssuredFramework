package com.restassured.framework.utils;

import com.restassured.framework.config.ConfigManager;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Centralised REST client that provides pre-configured request and response specifications.
 * All API calls should be made through this class.
 */
public class RestClient {

    private static final Logger log = LogManager.getLogger(RestClient.class);
    private static final ConfigManager config = ConfigManager.getInstance();

    private RestClient() {}

    /**
     * Builds a base RequestSpecification with common headers and base URI.
     */
    public static RequestSpecification buildRequestSpec() {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(config.getBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .setRelaxedHTTPSValidation();

        if (config.isLogRequestResponse()) {
            builder.addFilter(new RequestLoggingFilter(LogDetail.ALL))
                   .addFilter(new ResponseLoggingFilter(LogDetail.ALL));
        }

        return builder.build();
    }

    /**
     * Builds a RequestSpecification with custom headers.
     */
    public static RequestSpecification buildRequestSpec(Map<String, String> headers) {
        return RestAssured.given()
                .spec(buildRequestSpec())
                .headers(headers);
    }

    /**
     * Builds a RequestSpecification with Bearer token authentication.
     */
    public static RequestSpecification buildRequestSpecWithAuth(String token) {
        return RestAssured.given()
                .spec(buildRequestSpec())
                .header("Authorization", "Bearer " + token);
    }

    /**
     * Builds a RequestSpecification pointing to a custom base URI.
     */
    public static RequestSpecification buildRequestSpec(String baseUrl) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .setRelaxedHTTPSValidation();

        if (config.isLogRequestResponse()) {
            builder.addFilter(new RequestLoggingFilter(LogDetail.ALL))
                   .addFilter(new ResponseLoggingFilter(LogDetail.ALL));
        }

        return builder.build();
    }

    /**
     * Builds an authenticated RequestSpecification pointing to a custom base URI.
     */
    public static RequestSpecification buildRequestSpecWithAuth(String baseUrl, String token) {
        return RestAssured.given()
                .spec(buildRequestSpec(baseUrl))
                .header("Authorization", "Bearer " + token);
    }

    /**
     * Builds a base ResponseSpecification that validates 200 status and JSON content type.
     */
    public static ResponseSpecification buildResponseSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType(ContentType.JSON)
                .build();
    }

    /**
     * Builds a ResponseSpecification with a specific expected status code.
     */
    public static ResponseSpecification buildResponseSpec(int statusCode) {
        return new ResponseSpecBuilder()
                .expectStatusCode(statusCode)
                .expectContentType(ContentType.JSON)
                .build();
    }
}
