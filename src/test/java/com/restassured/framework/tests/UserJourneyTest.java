package com.restassured.framework.tests;

import com.restassured.framework.base.BaseCETest;
import com.restassured.framework.config.ConfigManager;
import com.restassured.framework.constants.Endpoints;
import com.restassured.framework.models.CourseItem;
import com.restassured.framework.models.EnrolmentRecord;
import com.restassured.framework.models.LoginResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end user journey tests for the Course Enrollment API.
 *
 * Flow:
 *   1. Instructor logs in
 *   2. Instructor creates a course
 *   3. Student logs in
 *   4. Student enrols in the course
 *   5. Student views active enrolments → course is present
 *   6. Student drops the course
 *   7. Student views enrolment history → dropped record is present
 *   [Cleanup] Instructor deletes the test course
 */
@Epic("Course Enrollment API")
@Feature("User Journey")
public class UserJourneyTest extends BaseCETest {

    private static String instructorToken;
    private static String studentToken;
    private static String journeyCourseId;
    private static final String STUDENT_USERNAME = ConfigManager.getInstance().getCEStudentUsername();
    private static final String JOURNEY_COURSE_CODE =
            "JT" + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    private static final String START_DATE = LocalDate.now().plusMonths(1).toString();
    private static final String END_DATE = LocalDate.now().plusMonths(6).toString();

    // ─── Step 1 ───────────────────────────────────────────────────────────────

    @Test(priority = 1)
    @Story("User Journey")
    @Description("Step 1 — Instructor logs in and receives an auth token")
    @Severity(SeverityLevel.BLOCKER)
    public void step1_instructorLogin_shouldReturn200WithToken() {
        log.info("Journey Step 1: Instructor login");

        Response response = given()
            .spec(requestSpec)
            .body(Map.of(
                "username", config.getCEInstructorUsername(),
                "password", config.getCEInstructorPassword()
            ))
        .when()
            .post(Endpoints.CE_INSTRUCTOR_LOGIN);

        assertThat(response.statusCode()).isEqualTo(200);
        instructorToken = response.as(LoginResponse.class).getToken();
        assertThat(instructorToken).isNotBlank();
        log.info("Instructor token obtained.");
    }

    // ─── Step 2 ───────────────────────────────────────────────────────────────

    @Test(priority = 2, dependsOnMethods = "step1_instructorLogin_shouldReturn200WithToken")
    @Story("User Journey")
    @Description("Step 2 — Instructor creates a course for the student to enrol in")
    @Severity(SeverityLevel.BLOCKER)
    public void step2_instructorCreatesCourse_shouldReturn201() {
        log.info("Journey Step 2: Create course. Code: {}", JOURNEY_COURSE_CODE);

        Map<String, Object> payload = new HashMap<>();
        payload.put("title", "Journey Test Course");
        payload.put("instructor", config.getCEInstructorUsername());
        payload.put("courseCode", JOURNEY_COURSE_CODE);
        payload.put("category", "Automation");
        payload.put("totalCapacity", 30);
        payload.put("startDate", START_DATE);
        payload.put("endDate", END_DATE);

        given()
            .spec(authSpec(instructorToken))
            .body(payload)
        .when()
            .post(Endpoints.CE_COURSES)
        .then()
            .statusCode(201);

        // Get the MongoDB _id for later deletion
        List<CourseItem> allCourses = given()
            .spec(requestSpec)
            .when()
            .get(Endpoints.CE_COURSES_ALL)
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getList(".", CourseItem.class);

        journeyCourseId = allCourses.stream()
                .filter(c -> JOURNEY_COURSE_CODE.equals(c.getCourseCode()))
                .findFirst()
                .map(CourseItem::getId)
                .orElse(null);

        assertThat(journeyCourseId).isNotBlank();
        log.info("Course created. ID: {}", journeyCourseId);
    }

    // ─── Step 3 ───────────────────────────────────────────────────────────────

    @Test(priority = 3, dependsOnMethods = "step2_instructorCreatesCourse_shouldReturn201")
    @Story("User Journey")
    @Description("Step 3 — Student logs in and receives an auth token")
    @Severity(SeverityLevel.BLOCKER)
    public void step3_studentLogin_shouldReturn200WithToken() {
        log.info("Journey Step 3: Student login. Username: {}", STUDENT_USERNAME);

        Response response = given()
            .spec(requestSpec)
            .body(Map.of(
                "username", config.getCEStudentUsername(),
                "password", config.getCEStudentPassword()
            ))
        .when()
            .post(Endpoints.CE_STUDENT_LOGIN);

        assertThat(response.statusCode()).isEqualTo(200);
        studentToken = response.as(LoginResponse.class).getToken();
        assertThat(studentToken).isNotBlank();
        log.info("Student token obtained.");
    }

    // ─── Step 4 ───────────────────────────────────────────────────────────────

    @Test(priority = 4, dependsOnMethods = "step3_studentLogin_shouldReturn200WithToken")
    @Story("User Journey")
    @Description("Step 4 — Student enrols in the newly created course")
    @Severity(SeverityLevel.CRITICAL)
    public void step4_studentEnrolsInCourse_shouldReturn201() {
        log.info("Journey Step 4: Student enrols in course '{}'", JOURNEY_COURSE_CODE);

        given()
            .spec(authSpec(studentToken))
            .body(Map.of(
                "username", STUDENT_USERNAME,
                "courseCode", JOURNEY_COURSE_CODE
            ))
        .when()
            .post(Endpoints.CE_ENROL)
        .then()
            .statusCode(201);
    }

    // ─── Step 5 ───────────────────────────────────────────────────────────────

    @Test(priority = 5, dependsOnMethods = "step4_studentEnrolsInCourse_shouldReturn201")
    @Story("User Journey")
    @Description("Step 5 — Student views active enrolments; journey course should be present")
    @Severity(SeverityLevel.CRITICAL)
    public void step5_studentViewsActiveEnrolments_shouldContainCourse() {
        log.info("Journey Step 5: View active enrolments for '{}'", STUDENT_USERNAME);

        Response response = given()
            .spec(authSpec(studentToken))
            .body(Map.of("username", STUDENT_USERNAME))
        .when()
            .post(Endpoints.CE_ACTIVE_ENROLMENTS);

        assertThat(response.statusCode()).isEqualTo(200);

        List<EnrolmentRecord> active = response.jsonPath().getList(".", EnrolmentRecord.class);
        assertThat(active).as("Active enrolments should not be empty").isNotEmpty();

        boolean courseFound = active.stream()
                .anyMatch(e -> JOURNEY_COURSE_CODE.equals(e.getCourseCode()));
        assertThat(courseFound)
                .as("Course '%s' should appear in active enrolments", JOURNEY_COURSE_CODE)
                .isTrue();

        log.info("Active enrolments count: {}. Course found: {}", active.size(), courseFound);
    }

    // ─── Step 6 ───────────────────────────────────────────────────────────────

    @Test(priority = 6, dependsOnMethods = "step5_studentViewsActiveEnrolments_shouldContainCourse")
    @Story("User Journey")
    @Description("Step 6 — Student drops the course")
    @Severity(SeverityLevel.CRITICAL)
    public void step6_studentDropsCourse_shouldReturn200() {
        log.info("Journey Step 6: Student drops course '{}'", JOURNEY_COURSE_CODE);

        given()
            .spec(authSpec(studentToken))
            .body(Map.of(
                "username", STUDENT_USERNAME,
                "courseCode", JOURNEY_COURSE_CODE
            ))
        .when()
            .post(Endpoints.CE_DROP)
        .then()
            .statusCode(200);
    }

    // ─── Step 7 ───────────────────────────────────────────────────────────────

    @Test(priority = 7, dependsOnMethods = "step6_studentDropsCourse_shouldReturn200")
    @Story("User Journey")
    @Description("Step 7 — Student views enrolment history; dropped record should be present")
    @Severity(SeverityLevel.CRITICAL)
    public void step7_studentViewsEnrolmentHistory_shouldContainDroppedRecord() {
        log.info("Journey Step 7: View enrolment history for '{}'", STUDENT_USERNAME);

        Response response = given()
            .spec(authSpec(studentToken))
            .body(Map.of("username", STUDENT_USERNAME))
        .when()
            .post(Endpoints.CE_HISTORY);

        assertThat(response.statusCode()).isEqualTo(200);

        List<EnrolmentRecord> history = response.jsonPath().getList(".", EnrolmentRecord.class);
        assertThat(history).as("History should not be empty").isNotEmpty();

        boolean droppedFound = history.stream()
                .anyMatch(e -> JOURNEY_COURSE_CODE.equals(e.getCourseCode())
                        && "dropped".equalsIgnoreCase(e.getStatus()));
        assertThat(droppedFound)
                .as("A dropped record for course '%s' should appear in history", JOURNEY_COURSE_CODE)
                .isTrue();

        log.info("History count: {}. Dropped record found: {}", history.size(), droppedFound);
    }

    // ─── Cleanup ──────────────────────────────────────────────────────────────

    @AfterClass(alwaysRun = true)
    public void cleanUp_deleteCreatedCourse() {
        if (instructorToken != null && journeyCourseId != null) {
            log.info("Cleanup: Deleting journey course. ID: {}", journeyCourseId);
            try {
                given()
                    .spec(authSpec(instructorToken))
                    .pathParam("id", journeyCourseId)
                .when()
                    .delete(Endpoints.CE_COURSE_BY_ID)
                .then()
                    .statusCode(200);
                log.info("Journey course deleted.");
            } catch (Exception e) {
                log.warn("Cleanup failed for course ID {}: {}", journeyCourseId, e.getMessage());
            }
        }
    }

}
