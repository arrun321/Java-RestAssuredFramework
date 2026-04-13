package com.restassured.framework.tests;

import com.restassured.framework.base.BaseCETest;
import com.restassured.framework.constants.Endpoints;
import com.restassured.framework.models.CourseItem;
import com.restassured.framework.models.LoginResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * TestNG tests covering Course Management endpoints.
 * Covers: instructor login, get all, search by title, check availability, create, delete.
 */
@Epic("Course Enrollment API")
@Feature("Course Management")
public class CourseManagementTests extends BaseCETest {

    private static String instructorToken;
    private static String createdCourseId;
    private static final String COURSE_CODE = "TC" + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    private static final String COURSE_TITLE = "BDD Test Automation";
    private static final String START_DATE = LocalDate.now().plusMonths(1).toString();
    private static final String END_DATE = LocalDate.now().plusMonths(7).toString();

    // ─── Auth ─────────────────────────────────────────────────────────────────

    @Test(priority = 1)
    @Story("Instructor Login")
    @Description("Verify instructor login returns 200 with a bearer token")
    @Severity(SeverityLevel.BLOCKER)
    public void instructorLogin_shouldReturn200WithToken() {
        log.info("Test: Instructor login. Username: {}", config.getCEInstructorUsername());

        Response response = given()
            .spec(requestSpec)
            .body(Map.of(
                "username", config.getCEInstructorUsername(),
                "password", config.getCEInstructorPassword()
            ))
        .when()
            .post(Endpoints.CE_INSTRUCTOR_LOGIN);

        assertThat(response.statusCode()).as("Instructor login status").isEqualTo(200);

        instructorToken = response.as(LoginResponse.class).getToken();
        assertThat(instructorToken).as("Instructor token should not be null").isNotBlank();
        log.info("Instructor token obtained.");
    }

    // ─── Browse Courses ────────────────────────────────────────────────────────

    @Test(priority = 2)
    @Story("Get all courses")
    @Description("Verify GET /courses/all returns a list of courses with status 200")
    @Severity(SeverityLevel.CRITICAL)
    public void getAllCourses_shouldReturn200WithCourseList() {
        log.info("Test: Get all courses");

        List<CourseItem> courses = given()
            .spec(requestSpec)
        .when()
            .get(Endpoints.CE_COURSES_ALL)
        .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getList(".", CourseItem.class);

        assertThat(courses).as("Courses list should not be null").isNotNull();
        log.info("Total courses in catalog: {}", courses.size());
    }

    @Test(priority = 3, dependsOnMethods = "instructorLogin_shouldReturn200WithToken")
    @Story("Search courses by title")
    @Description("Verify searching courses by title 'Java' returns matching courses")
    @Severity(SeverityLevel.NORMAL)
    public void searchCoursesByTitle_shouldReturn200WithMatchingCourses() {
        String searchTitle = "Java";
        log.info("Test: Search courses by title '{}'", searchTitle);

        Response response = given()
            .spec(authSpec(instructorToken))
            .pathParam("title", searchTitle)
        .when()
            .get(Endpoints.CE_COURSES_BY_TITLE);

        assertThat(response.statusCode()).isEqualTo(200);

        List<CourseItem> courses = response.jsonPath().getList(".", CourseItem.class);
        assertThat(courses).as("Search results should not be null").isNotNull();

        if (!courses.isEmpty()) {
            courses.forEach(c ->
                assertThat(c.getTitle().toLowerCase()).contains(searchTitle.toLowerCase())
            );
        }
        log.info("Courses matching '{}': {}", searchTitle, courses.size());
    }

    @Test(priority = 4)
    @Story("Check course availability")
    @Description("Verify availability endpoint returns courseCode and slot information")
    @Severity(SeverityLevel.NORMAL)
    public void checkCourseAvailability_shouldReturn200WithSlotInfo() {
        // Use the first course from the catalog to check availability
        List<CourseItem> courses = given()
            .spec(requestSpec)
            .when()
            .get(Endpoints.CE_COURSES_ALL)
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getList(".", CourseItem.class);

        assertThat(courses).as("Need at least one course in catalog for availability check").isNotEmpty();

        String targetCode = courses.get(0).getCourseCode();
        log.info("Test: Check availability for course code '{}'", targetCode);

        given()
            .spec(requestSpec)
            .pathParam("courseCode", targetCode)
        .when()
            .get(Endpoints.CE_COURSES_AVAILABILITY)
        .then()
            .statusCode(200)
            .body("courseCode", notNullValue())
            .body("title", notNullValue())
            .body("availableSlots", notNullValue());
    }

    // ─── Create & Delete ───────────────────────────────────────────────────────

    @Test(priority = 5, dependsOnMethods = "instructorLogin_shouldReturn200WithToken")
    @Story("Create course")
    @Description("Verify instructor can create a new course returning 201")
    @Severity(SeverityLevel.CRITICAL)
    public void createCourse_shouldReturn201() {
        log.info("Test: Create course. Code: {}", COURSE_CODE);

        Map<String, Object> payload = new HashMap<>();
        payload.put("title", COURSE_TITLE);
        payload.put("instructor", config.getCEInstructorUsername());
        payload.put("courseCode", COURSE_CODE);
        payload.put("category", "Test Automation");
        payload.put("totalCapacity", 20);
        payload.put("startDate", START_DATE);
        payload.put("endDate", END_DATE);

        given()
            .spec(authSpec(instructorToken))
            .body(payload)
        .when()
            .post(Endpoints.CE_COURSES)
        .then()
            .statusCode(201);

        // Retrieve the MongoDB _id from GET /courses/all (needed for deletion)
        List<CourseItem> allCourses = given()
            .spec(requestSpec)
            .when()
            .get(Endpoints.CE_COURSES_ALL)
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getList(".", CourseItem.class);

        createdCourseId = allCourses.stream()
                .filter(c -> COURSE_CODE.equals(c.getCourseCode()))
                .findFirst()
                .map(CourseItem::getId)
                .orElse(null);

        assertThat(createdCourseId).as("Created course ID should be retrievable").isNotBlank();
        log.info("Course created. ID: {}, Code: {}", createdCourseId, COURSE_CODE);
    }

    @Test(priority = 6, dependsOnMethods = "createCourse_shouldReturn201")
    @Story("Delete course")
    @Description("Verify instructor can delete a course by MongoDB ID returning 200")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteCourse_shouldReturn200() {
        log.info("Test: Delete course. ID: {}", createdCourseId);

        given()
            .spec(authSpec(instructorToken))
            .pathParam("id", createdCourseId)
        .when()
            .delete(Endpoints.CE_COURSE_BY_ID)
        .then()
            .statusCode(200);

        log.info("Course deleted successfully. Code: {}", COURSE_CODE);
    }

    // ─── Negative Cases ───────────────────────────────────────────────────────

    @Test(priority = 7)
    @Story("Create course")
    @Description("Verify creating a course without a token returns 401")
    @Severity(SeverityLevel.NORMAL)
    public void createCourse_withoutToken_shouldReturn401() {
        log.info("Test: Create course without auth token");

        Map<String, Object> payload = Map.of(
            "title", "Unauthorized Course",
            "instructor", "instructor01",
            "courseCode", "UNAUTH999",
            "category", "Test",
            "totalCapacity", 10,
            "startDate", START_DATE,
            "endDate", END_DATE
        );

        given()
            .spec(requestSpec)
            .body(payload)
        .when()
            .post(Endpoints.CE_COURSES)
        .then()
            .statusCode(401);
    }

    @Test(priority = 8)
    @Story("Check course availability")
    @Description("Verify checking availability for a non-existent course returns 404")
    @Severity(SeverityLevel.MINOR)
    public void checkAvailability_nonExistentCourse_shouldReturn404() {
        log.info("Test: Check availability for non-existent course");

        given()
            .spec(requestSpec)
            .pathParam("courseCode", "NONEXISTENT99999")
        .when()
            .get(Endpoints.CE_COURSES_AVAILABILITY)
        .then()
            .statusCode(404);
    }
}
