package com.restassured.framework.bdd.steps;

import com.restassured.framework.bdd.context.TestContext;
import com.restassured.framework.config.ConfigManager;
import com.restassured.framework.constants.Endpoints;
import com.restassured.framework.models.EnrolmentRecord;
import com.restassured.framework.utils.RestClient;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for Enrolment endpoints.
 * Covers: enrol, drop, view active enrolments, view history.
 */
public class EnrolmentSteps {

    private static final Logger log = LogManager.getLogger(EnrolmentSteps.class);
    private static final String CE_BASE_URL = ConfigManager.getInstance().getCourseEnrollmentBaseUrl();

    private final TestContext testContext;

    public EnrolmentSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    // ─── When ─────────────────────────────────────────────────────────────────

    @When("the student enrols in the newly created course")
    public void theStudentEnrolsInTheNewlyCreatedCourse() {
        String username = testContext.getStudentUsername();
        String courseCode = testContext.getCreatedCourseCode();

        assertThat(username).as("Student username must be set before enrolment").isNotBlank();
        assertThat(courseCode).as("Course code must be set before enrolment").isNotBlank();

        log.info("Student '{}' enrolling in course '{}'", username, courseCode);

        Response response = given()
                .spec(RestClient.buildRequestSpecWithAuth(CE_BASE_URL, testContext.getStudentToken()))
                .body(Map.of("username", username, "courseCode", courseCode))
                .when()
                .post(Endpoints.CE_ENROL);

        testContext.setResponse(response);
        log.info("Enrol response status: {}", response.statusCode());
    }

    @When("the student views their active enrolments")
    public void theStudentViewsTheirActiveEnrolments() {
        String username = testContext.getStudentUsername();
        log.info("Fetching active enrolments for '{}'", username);

        Response response = given()
                .spec(RestClient.buildRequestSpecWithAuth(CE_BASE_URL, testContext.getStudentToken()))
                .body(Map.of("username", username))
                .when()
                .post(Endpoints.CE_ACTIVE_ENROLMENTS);

        testContext.setResponse(response);
    }

    @When("the student drops the newly created course")
    public void theStudentDropsTheNewlyCreatedCourse() {
        String username = testContext.getStudentUsername();
        String courseCode = testContext.getCreatedCourseCode();

        log.info("Student '{}' dropping course '{}'", username, courseCode);

        Response response = given()
                .spec(RestClient.buildRequestSpecWithAuth(CE_BASE_URL, testContext.getStudentToken()))
                .body(Map.of("username", username, "courseCode", courseCode))
                .when()
                .post(Endpoints.CE_DROP);

        testContext.setResponse(response);
        log.info("Drop response status: {}", response.statusCode());
    }

    @When("the student views their enrolment history")
    public void theStudentViewsTheirEnrolmentHistory() {
        String username = testContext.getStudentUsername();
        log.info("Fetching enrolment history for '{}'", username);

        Response response = given()
                .spec(RestClient.buildRequestSpecWithAuth(CE_BASE_URL, testContext.getStudentToken()))
                .body(Map.of("username", username))
                .when()
                .post(Endpoints.CE_HISTORY);

        testContext.setResponse(response);
    }

    // ─── Then / And ───────────────────────────────────────────────────────────

    @And("the created course code appears in the active enrolments")
    public void theCreatedCourseCodeAppearsInActiveEnrolments() {
        String courseCode = testContext.getCreatedCourseCode();
        List<EnrolmentRecord> active = testContext.getResponse().jsonPath().getList(".", EnrolmentRecord.class);

        assertThat(active).as("Active enrolments list should not be empty").isNotEmpty();

        boolean found = active.stream().anyMatch(e -> courseCode.equals(e.getCourseCode()));
        assertThat(found)
                .as("Course '%s' should appear in active enrolments", courseCode)
                .isTrue();

        log.info("Course '{}' found in active enrolments. Total active: {}", courseCode, active.size());
    }

    @And("the created course code appears in enrolment history with status {string}")
    public void theCreatedCourseCodeAppearsInEnrolmentHistoryWithStatus(String expectedStatus) {
        String courseCode = testContext.getCreatedCourseCode();
        List<EnrolmentRecord> history = testContext.getResponse().jsonPath().getList(".", EnrolmentRecord.class);

        assertThat(history).as("Enrolment history should not be empty").isNotEmpty();

        boolean found = history.stream()
                .anyMatch(e -> courseCode.equals(e.getCourseCode())
                        && expectedStatus.equalsIgnoreCase(e.getStatus()));

        assertThat(found)
                .as("A '%s' record for course '%s' should appear in history", expectedStatus, courseCode)
                .isTrue();

        log.info("Course '{}' found in history with status '{}'. Total records: {}",
                courseCode, expectedStatus, history.size());
    }
}
