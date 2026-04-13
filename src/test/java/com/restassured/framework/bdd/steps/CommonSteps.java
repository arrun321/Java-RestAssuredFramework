package com.restassured.framework.bdd.steps;

import com.restassured.framework.bdd.context.TestContext;
import com.restassured.framework.config.ConfigManager;
import com.restassured.framework.utils.RestClient;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Common/shared step definitions used across multiple feature files.
 * Handles generic HTTP verbs, status code assertions, and field assertions.
 */
public class CommonSteps {

    private static final Logger log = LogManager.getLogger(CommonSteps.class);
    private final TestContext testContext;

    public CommonSteps(TestContext testContext) {
        this.testContext = testContext;
        RestAssured.baseURI = ConfigManager.getInstance().getBaseUrl();
    }

    // ─── Given ────────────────────────────────────────────────────────────────

    @Given("the REST API is available")
    public void theRestApiIsAvailable() {
        log.info("Setting up API base URI: {}", ConfigManager.getInstance().getBaseUrl());
        RestAssured.baseURI = ConfigManager.getInstance().getBaseUrl();
    }

    @Given("I have a patch payload with field {string} and value {string}")
    public void iHaveAPatchPayload(String field, String value) {
        testContext.clearPayload();
        testContext.putPayloadField(field, value);
        log.info("Patch payload: {} = {}", field, value);
    }

    // ─── When ─────────────────────────────────────────────────────────────────

    @When("I send a GET request to {string}")
    public void iSendAGetRequestTo(String endpoint) {
        log.info("GET {}", endpoint);
        Response response = given()
                .spec(RestClient.buildRequestSpec(testContext.getEffectiveBaseUrl()))
                .when()
                .get(endpoint);
        testContext.setResponse(response);
    }

    @When("I send a GET request to {string} with query param {string} equal to {string}")
    public void iSendAGetRequestWithQueryParam(String endpoint, String paramName, String paramValue) {
        log.info("GET {} ?{}={}", endpoint, paramName, paramValue);
        Response response = given()
                .spec(RestClient.buildRequestSpec(testContext.getEffectiveBaseUrl()))
                .queryParam(paramName, paramValue)
                .when()
                .get(endpoint);
        testContext.setResponse(response);
    }

    @When("I send a POST request to {string} with the payload")
    public void iSendAPostRequestTo(String endpoint) {
        log.info("POST {} | payload: {}", endpoint, testContext.getRequestPayload());
        Response response = given()
                .spec(RestClient.buildRequestSpec(testContext.getEffectiveBaseUrl()))
                .body(testContext.getRequestPayload())
                .when()
                .post(endpoint);
        testContext.setResponse(response);
    }

    @When("I send a PUT request to {string} with the payload")
    public void iSendAPutRequestTo(String endpoint) {
        log.info("PUT {} | payload: {}", endpoint, testContext.getRequestPayload());
        Response response = given()
                .spec(RestClient.buildRequestSpec(testContext.getEffectiveBaseUrl()))
                .body(testContext.getRequestPayload())
                .when()
                .put(endpoint);
        testContext.setResponse(response);
    }

    @When("I send a PATCH request to {string} with the payload")
    public void iSendAPatchRequestTo(String endpoint) {
        log.info("PATCH {} | payload: {}", endpoint, testContext.getRequestPayload());
        Response response = given()
                .spec(RestClient.buildRequestSpec(testContext.getEffectiveBaseUrl()))
                .body(testContext.getRequestPayload())
                .when()
                .patch(endpoint);
        testContext.setResponse(response);
    }

    @When("I send a DELETE request to {string}")
    public void iSendADeleteRequestTo(String endpoint) {
        log.info("DELETE {}", endpoint);
        Response response = given()
                .spec(RestClient.buildRequestSpec(testContext.getEffectiveBaseUrl()))
                .when()
                .delete(endpoint);
        testContext.setResponse(response);
    }

    // ─── Then ─────────────────────────────────────────────────────────────────

    @Then("the response status code should be {int}")
    public void theResponseStatusCodeShouldBe(int expectedStatusCode) {
        int actualStatusCode = testContext.getResponse().statusCode();
        log.info("Response status: {} (expected: {})", actualStatusCode, expectedStatusCode);
        assertThat(actualStatusCode)
                .as("Expected status code %d but got %d", expectedStatusCode, actualStatusCode)
                .isEqualTo(expectedStatusCode);
    }

    @Then("the created resource should have a generated id")
    public void theCreatedResourceShouldHaveAGeneratedId() {
        Integer id = testContext.getResponse().jsonPath().getInt("id");
        assertThat(id).as("Created resource should have a non-null id").isNotNull();
        log.info("Created resource id: {}", id);
    }

    @And("the response field {string} should be {string}")
    public void theResponseFieldShouldBe(String field, String expectedValue) {
        String actualValue = testContext.getResponse().jsonPath().getString(field);
        assertThat(actualValue)
                .as("Field '%s' expected '%s' but got '%s'", field, expectedValue, actualValue)
                .isEqualTo(expectedValue);
    }

    @And("the response list size should be {int}")
    public void theResponseListSizeShouldBe(int expectedSize) {
        int actualSize = testContext.getResponse().jsonPath().getList("$").size();
        assertThat(actualSize)
                .as("Expected list size %d but got %d", expectedSize, actualSize)
                .isEqualTo(expectedSize);
    }
}
