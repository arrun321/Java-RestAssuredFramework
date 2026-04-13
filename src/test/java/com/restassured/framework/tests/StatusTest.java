package com.restassured.framework.tests;

import com.restassured.framework.base.BaseCETest;
import com.restassured.framework.constants.Endpoints;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@Epic("Course Enrollment API")
@Feature("Server Status")
public class StatusTest extends BaseCETest {

    @Test
    @Story("Health check")
    @Description("Verify the server health check endpoint returns 200 and a non-empty status message")
    @Severity(SeverityLevel.BLOCKER)
    public void serverHealthCheck_shouldReturn200AndStatusMessage() {
        log.info("Test: Server health check");

        given()
            .spec(requestSpec)
        .when()
            .get(Endpoints.CE_STATUS)
        .then()
            .statusCode(200)
            .body("status", notNullValue());
    }
}
