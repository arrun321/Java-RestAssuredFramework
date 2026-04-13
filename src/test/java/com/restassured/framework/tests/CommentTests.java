package com.restassured.framework.tests;

import com.restassured.framework.base.BaseTest;
import com.restassured.framework.constants.Endpoints;
import com.restassured.framework.models.Comment;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

@Epic("Post Management")
@Feature("Comments API")
public class CommentTests extends BaseTest {

    @Test
    @Story("Get all comments")
    @Description("Verify that GET /comments returns a list with status 200")
    @Severity(SeverityLevel.CRITICAL)
    public void getAllComments_shouldReturn200WithCommentList() {
        log.info("Test: Get all comments");

        given()
            .spec(requestSpec)
        .when()
            .get(Endpoints.COMMENTS)
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .body("[0].id", notNullValue())
            .body("[0].email", notNullValue());
    }

    @Test
    @Story("Get comment by ID")
    @Description("Verify that GET /comments/{id} returns the correct comment")
    @Severity(SeverityLevel.NORMAL)
    public void getCommentById_shouldReturn200() {
        int commentId = 1;
        log.info("Test: Get comment by ID = {}", commentId);

        given()
            .spec(requestSpec)
            .pathParam("id", commentId)
        .when()
            .get(Endpoints.COMMENT_BY_ID)
        .then()
            .statusCode(200)
            .body("id", equalTo(commentId))
            .body("postId", notNullValue())
            .body("name", notNullValue())
            .body("email", notNullValue())
            .body("body", notNullValue());
    }

    @Test
    @Story("Get comments by post")
    @Description("Verify comments can be filtered by postId query parameter")
    @Severity(SeverityLevel.NORMAL)
    public void getCommentsByPostId_shouldReturnCommentsForThatPost() {
        int postId = 1;
        log.info("Test: Get comments for postId={}", postId);

        List<Comment> comments = given()
            .spec(requestSpec)
            .queryParam("postId", postId)
        .when()
            .get(Endpoints.COMMENTS)
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .extract()
            .jsonPath()
            .getList(".", Comment.class);

        assertThat(comments).isNotEmpty();
        assertThat(comments).allSatisfy(comment -> {
            assertThat(comment.getPostId()).isEqualTo(postId);
            assertThat(comment.getEmail()).isNotBlank().contains("@");
        });
    }

    @Test
    @Story("Get comment by ID")
    @Description("Verify that GET /comments/{id} with non-existent ID returns 404")
    @Severity(SeverityLevel.MINOR)
    public void getCommentById_withInvalidId_shouldReturn404() {
        int nonExistentId = 9999;
        log.info("Test: Get comment with non-existent id={}", nonExistentId);

        given()
            .spec(requestSpec)
            .pathParam("id", nonExistentId)
        .when()
            .get(Endpoints.COMMENT_BY_ID)
        .then()
            .statusCode(404);
    }
}
