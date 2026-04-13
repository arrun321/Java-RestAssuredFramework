package com.restassured.framework.bdd.steps;

import com.restassured.framework.bdd.context.TestContext;
import com.restassured.framework.config.ConfigManager;
import com.restassured.framework.constants.Endpoints;
import com.restassured.framework.models.CourseItem;
import com.restassured.framework.utils.RestClient;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for Course Management endpoints.
 * Covers: create, delete, search all, search by title, check availability.
 */
public class CourseManagementSteps {

    private static final Logger log = LogManager.getLogger(CourseManagementSteps.class);
    private static final String CE_BASE_URL = ConfigManager.getInstance().getCourseEnrollmentBaseUrl();
    private static final String INSTRUCTOR = ConfigManager.getInstance().getCEInstructorUsername();

    private final TestContext testContext;

    public CourseManagementSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    // ─── When ─────────────────────────────────────────────────────────────────

    @When("the instructor creates a course with a unique code")
    public void theInstructorCreatesACourseWithAUniqueCode() {
        String courseCode = "TC" + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        String startDate = LocalDate.now().plusMonths(1).toString();
        String endDate = LocalDate.now().plusMonths(7).toString();

        log.info("Creating course with code: {}", courseCode);

        Map<String, Object> payload = new HashMap<>();
        payload.put("title", "BDD Automation Course");
        payload.put("instructor", INSTRUCTOR);
        payload.put("courseCode", courseCode);
        payload.put("category", "Test Automation");
        payload.put("totalCapacity", 25);
        payload.put("startDate", startDate);
        payload.put("endDate", endDate);

        Response response = given()
                .spec(RestClient.buildRequestSpecWithAuth(CE_BASE_URL, testContext.getInstructorToken()))
                .body(payload)
                .when()
                .post(Endpoints.CE_COURSES);

        testContext.setResponse(response);

        if (response.statusCode() == 201) {
            testContext.setCreatedCourseCode(courseCode);
            // Retrieve MongoDB _id from the catalog for subsequent deletion
            List<CourseItem> all = given()
                    .spec(RestClient.buildRequestSpec(CE_BASE_URL))
                    .when()
                    .get(Endpoints.CE_COURSES_ALL)
                    .then()
                    .statusCode(200)
                    .extract()
                    .jsonPath()
                    .getList(".", CourseItem.class);

            String courseId = all.stream()
                    .filter(c -> courseCode.equals(c.getCourseCode()))
                    .findFirst()
                    .map(CourseItem::getId)
                    .orElse(null);

            testContext.setCreatedCourseId(courseId);
            log.info("Course created. Code: {}, ID: {}", courseCode, courseId);
        }
    }

    @When("the instructor deletes the created course")
    public void theInstructorDeletesTheCreatedCourse() {
        String courseId = testContext.getCreatedCourseId();
        assertThat(courseId).as("Course ID must be set before deletion").isNotBlank();
        log.info("Deleting course. ID: {}", courseId);

        Response response = given()
                .spec(RestClient.buildRequestSpecWithAuth(CE_BASE_URL, testContext.getInstructorToken()))
                .pathParam("id", courseId)
                .when()
                .delete(Endpoints.CE_COURSE_BY_ID);

        testContext.setResponse(response);
        log.info("Delete response status: {}", response.statusCode());
    }

    @When("I search for courses with title {string}")
    public void iSearchForCoursesWithTitle(String title) {
        testContext.setSearchTitle(title);
        log.info("Searching courses by title: '{}'", title);

        Response response = given()
                .spec(RestClient.buildRequestSpecWithAuth(CE_BASE_URL, testContext.getInstructorToken()))
                .pathParam("title", title)
                .when()
                .get(Endpoints.CE_COURSES_BY_TITLE);

        testContext.setResponse(response);
    }

    @When("I check availability for the first available course")
    public void iCheckAvailabilityForTheFirstAvailableCourse() {
        List<CourseItem> courses = given()
                .spec(RestClient.buildRequestSpec(CE_BASE_URL))
                .when()
                .get(Endpoints.CE_COURSES_ALL)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", CourseItem.class);

        assertThat(courses).as("At least one course must exist in the catalog").isNotEmpty();

        String courseCode = courses.get(0).getCourseCode();
        log.info("Checking availability for course code: {}", courseCode);

        Response response = given()
                .spec(RestClient.buildRequestSpec(CE_BASE_URL))
                .pathParam("courseCode", courseCode)
                .when()
                .get(Endpoints.CE_COURSES_AVAILABILITY);

        testContext.setResponse(response);
    }

    @When("I attempt to create a course without a token")
    public void iAttemptToCreateACourseWithoutAToken() {
        log.info("Attempting to create course without auth token");

        Map<String, Object> payload = Map.of(
                "title", "Unauthorized Course",
                "instructor", INSTRUCTOR,
                "courseCode", "UNAUTH" + UUID.randomUUID().toString().substring(0, 4).toUpperCase(),
                "category", "Test",
                "totalCapacity", 10,
                "startDate", LocalDate.now().plusMonths(1).toString(),
                "endDate", LocalDate.now().plusMonths(6).toString());

        Response response = given()
                .spec(RestClient.buildRequestSpec(CE_BASE_URL))
                .body(payload)
                .when()
                .post(Endpoints.CE_COURSES);

        testContext.setResponse(response);
    }

    // ─── Then / And ───────────────────────────────────────────────────────────

    @Then("the response is a non-empty list of courses")
    public void theResponseIsANonEmptyListOfCourses() {
        List<CourseItem> courses = testContext.getResponse().jsonPath().getList(".", CourseItem.class);
        assertThat(courses).as("Courses list should not be null").isNotNull();
        log.info("Courses returned: {}", courses.size());

        assertThat(courses.get(0).getId())
                .as("Course ID should exist")
                .isNotNull();
    }

    @Then("the response is a list of courses")
    public void theResponseIsAListOfCourses() {
               // List<?> courses = testContext.getResponse().jsonPath().getList("$");
        // assertThat(courses).as("Response should be a JSON array").isNotNull();
        // log.info("Courses in response: {}", courses.size());

        List<CourseItem> courses = testContext.getResponse().jsonPath().getList(".", CourseItem.class);
        assertThat(courses).as("Courses list should not be null").isNotNull();
        assertThat(courses)
                .extracting(CourseItem::getTitle)
                .allMatch(title ->
                        title.toLowerCase().contains(testContext.getSearchTitle().toLowerCase()));

        log.info("Courses returned: {}", courses.size());
    }

    @And("the availability response contains a courseCode and availableSlots")
    public void theAvailabilityResponseContainsCoursecodeAndAvailableSlots() {
        String courseCode = testContext.getResponse().jsonPath().getString("courseCode");
        Object slots = testContext.getResponse().jsonPath().get("availableSlots");

        assertThat(courseCode).as("courseCode field should not be null").isNotBlank();
        assertThat(slots).as("availableSlots field should not be null").isNotNull();
        log.info("Availability: courseCode={}, availableSlots={}", courseCode, slots);
    }

    @And("the response confirms the server is running")
    public void theResponseConfirmsTheServerIsRunning() {
        String status = testContext.getResponse().jsonPath().getString("status");
        assertThat(status).as("status field should not be null").isNotBlank();
        log.info("Server status: {}", status);
    }

    @And("the response confirms the {string}")
    public void theResponseConfirmsTheStatus(String expectedStatus) {
        String status = testContext.getResponse().jsonPath().getString("status");
        assertThat(status).as("status field should match expected value").isEqualTo(expectedStatus);
        log.info("Server status: {}", status);
    }

        @When("I search for course with course-code {string}")
    public void iSearchForCourseWithCourseCode(String courseCode) {
        testContext.setExistingCourseId(courseCode);
        log.info("Searching courses by Course ID: '{}'", courseCode);

        Response response = given()
                .spec(RestClient.buildRequestSpecWithAuth(CE_BASE_URL, testContext.getInstructorToken()))
                .pathParam("courseCode", courseCode)
                .when()
                .get(Endpoints.CE_COURSES_AVAILABILITY);

        testContext.setResponse(response);
    }
}
