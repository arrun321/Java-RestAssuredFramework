package com.restassured.framework.tests;

import com.restassured.framework.base.BaseTest;
import com.restassured.framework.constants.Endpoints;
import com.restassured.framework.models.User;
import com.restassured.framework.utils.TestDataFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;

@Epic("User Management")
@Feature("Users API")
public class UserTests extends BaseTest {

    @Test
    @Story("Get all users")
    @Description("Verify that GET /users returns a list of users with status 200")
    @Severity(SeverityLevel.CRITICAL)
    public void getAllUsers_shouldReturn200WithUserList() {
        log.info("Test: Get all users");

        given()
            .spec(requestSpec)
        .when()
            .get(Endpoints.USERS)
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .body("[0].id", notNullValue())
            .body("[0].name", notNullValue())
            .body("[0].email", notNullValue());
    }

    @Test
    @Story("Get user by ID")
    @Description("Verify that GET /users/{id} returns the correct user")
    @Severity(SeverityLevel.CRITICAL)
    public void getUserById_shouldReturn200WithCorrectUser() {
        int userId = 1;
        log.info("Test: Get user by ID = {}", userId);

        given()
            .spec(requestSpec)
            .pathParam("id", userId)
        .when()
            .get(Endpoints.USER_BY_ID)
        .then()
            .statusCode(200)
            .body("id", equalTo(userId))
            .body("name", notNullValue())
            .body("email", notNullValue());
    }

    @Test
    @Story("Get user by ID")
    @Description("Deserialize user response to POJO and assert fields")
    @Severity(SeverityLevel.NORMAL)
    public void getUserById_shouldDeserializeToUserPojo() {
        int userId = 1;
        log.info("Test: Deserialize user {} to POJO", userId);

        Response response = given()
            .spec(requestSpec)
            .pathParam("id", userId)
        .when()
            .get(Endpoints.USER_BY_ID);

        assertThat(response.statusCode()).isEqualTo(200);

        User user = response.as(User.class);
        assertThat(user.getId()).isEqualTo(userId);
        assertThat(user.getName()).isNotBlank();
        assertThat(user.getEmail()).isNotBlank().contains("@");
        assertThat(user.getUsername()).isNotBlank();

        log.info("User deserialized: id={}, name={}, email={}", user.getId(), user.getName(), user.getEmail());
    }

    @Test
    @Story("Create user")
    @Description("Verify that POST /users creates a new user and returns 201")
    @Severity(SeverityLevel.CRITICAL)
    public void createUser_shouldReturn201WithCreatedUser() {
        Map<String, Object> userPayload = TestDataFactory.createUserPayload();
        log.info("Test: Create user with name={}", userPayload.get("name"));

        given()
            .spec(requestSpec)
            .body(userPayload)
        .when()
            .post(Endpoints.USERS)
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo(userPayload.get("name")))
            .body("email", equalTo(userPayload.get("email")));
    }

    @Test
    @Story("Update user")
    @Description("Verify that PUT /users/{id} updates an existing user and returns 200")
    @Severity(SeverityLevel.NORMAL)
    public void updateUser_shouldReturn200WithUpdatedUser() {
        int userId = 1;
        Map<String, Object> updatePayload = TestDataFactory.createUserPayload();
        log.info("Test: Update user id={}", userId);

        given()
            .spec(requestSpec)
            .pathParam("id", userId)
            .body(updatePayload)
        .when()
            .put(Endpoints.USER_BY_ID)
        .then()
            .statusCode(200)
            .body("id", equalTo(userId))
            .body("name", equalTo(updatePayload.get("name")));
    }

    @Test
    @Story("Partial update user")
    @Description("Verify that PATCH /users/{id} partially updates a user and returns 200")
    @Severity(SeverityLevel.MINOR)
    public void patchUser_shouldReturn200WithPatchedUser() {
        int userId = 1;
        String newEmail = TestDataFactory.randomEmail();
        log.info("Test: Patch user id={} with email={}", userId, newEmail);

        given()
            .spec(requestSpec)
            .pathParam("id", userId)
            .body(Map.of("email", newEmail))
        .when()
            .patch(Endpoints.USER_BY_ID)
        .then()
            .statusCode(200)
            .body("email", equalTo(newEmail));
    }

    @Test
    @Story("Delete user")
    @Description("Verify that DELETE /users/{id} returns 200")
    @Severity(SeverityLevel.NORMAL)
    public void deleteUser_shouldReturn200() {
        int userId = 1;
        log.info("Test: Delete user id={}", userId);

        given()
            .spec(requestSpec)
            .pathParam("id", userId)
        .when()
            .delete(Endpoints.USER_BY_ID)
        .then()
            .statusCode(200);
    }

    @Test
    @Story("Get user by ID")
    @Description("Verify that GET /users/{id} with non-existent ID returns 404")
    @Severity(SeverityLevel.NORMAL)
    public void getUserById_withInvalidId_shouldReturn404() {
        int nonExistentId = 9999;
        log.info("Test: Get user with non-existent id={}", nonExistentId);

        given()
            .spec(requestSpec)
            .pathParam("id", nonExistentId)
        .when()
            .get(Endpoints.USER_BY_ID)
        .then()
            .statusCode(404);
    }

    @Test
    @Story("Get all users")
    @Description("Verify the count and structure of users list")
    @Severity(SeverityLevel.NORMAL)
    public void getAllUsers_shouldReturnTenUsers() {
        log.info("Test: Verify users list size");

        List<User> users = given()
            .spec(requestSpec)
        .when()
            .get(Endpoints.USERS)
        .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getList(".", User.class);

        assertThat(users).hasSize(10);
        assertThat(users).allSatisfy(user -> {
            assertThat(user.getId()).isNotNull();
            assertThat(user.getName()).isNotBlank();
            assertThat(user.getEmail()).isNotBlank();
        });
    }
}
