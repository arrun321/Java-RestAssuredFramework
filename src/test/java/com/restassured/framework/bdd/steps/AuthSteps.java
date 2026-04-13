package com.restassured.framework.bdd.steps;

import com.restassured.framework.bdd.context.TestContext;
import com.restassured.framework.config.ConfigManager;
import com.restassured.framework.constants.Endpoints;
import com.restassured.framework.models.LoginResponse;
import com.restassured.framework.utils.RestClient;
import io.cucumber.java.en.Given;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for API setup and authentication.
 * Handles base URL configuration and instructor/student login.
 */
public class AuthSteps {

    private static final Logger log = LogManager.getLogger(AuthSteps.class);
    private static final String CE_BASE_URL = ConfigManager.getInstance().getCourseEnrollmentBaseUrl();

    private final TestContext testContext;

    public AuthSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    @Given("the base URL is the course enrollment API")
    public void theBaseUrlIsTheCourseEnrollmentApi() {
        testContext.setBaseUrl(CE_BASE_URL);
        log.info("Base URL set to: {}", CE_BASE_URL);
    }

    @Given("the instructor is authenticated")
    public void theInstructorIsAuthenticated() {
        instructorHasLoggedIn(
                ConfigManager.getInstance().getCEInstructorUsername(),
                ConfigManager.getInstance().getCEInstructorPassword()
        );
    }

    @Given("the student is authenticated")
    public void theStudentIsAuthenticated() {
        studentHasLoggedIn(
                ConfigManager.getInstance().getCEStudentUsername(),
                ConfigManager.getInstance().getCEStudentPassword()
        );
    }

    @Given("instructor {string} has logged in with password {string}")
    public void instructorHasLoggedIn(String username, String password) {
        log.info("Logging in instructor: {}", username);

        Response response = given()
                .spec(RestClient.buildRequestSpec(CE_BASE_URL))
                .body(java.util.Map.of("username", username, "password", password))
                .when()
                .post(Endpoints.CE_INSTRUCTOR_LOGIN);

        assertThat(response.statusCode())
                .as("Instructor login should return 200 but got %d", response.statusCode())
                .isEqualTo(200);

        String token = response.as(LoginResponse.class).getToken();
        assertThat(token).as("Instructor token should not be null").isNotBlank();

        testContext.setInstructorToken(token);
        log.info("Instructor '{}' logged in successfully.", username);
    }

    @Given("student {string} has logged in with password {string}")
    public void studentHasLoggedIn(String username, String password) {
        log.info("Logging in student: {}", username);

        Response response = given()
                .spec(RestClient.buildRequestSpec(CE_BASE_URL))
                .body(java.util.Map.of("username", username, "password", password))
                .when()
                .post(Endpoints.CE_STUDENT_LOGIN);

        assertThat(response.statusCode())
                .as("Student login should return 200 but got %d", response.statusCode())
                .isEqualTo(200);

        String token = response.as(LoginResponse.class).getToken();
        assertThat(token).as("Student token should not be null").isNotBlank();

        testContext.setStudentToken(token);
        testContext.setStudentUsername(username);
        log.info("Student '{}' logged in successfully.", username);
    }
}
