package com.restassured.framework.tests;

import com.restassured.framework.base.BaseTest;
import com.restassured.framework.constants.Endpoints;
import com.restassured.framework.models.Post;
import com.restassured.framework.utils.TestDataFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

@Epic("Post Management")
@Feature("Posts API")
public class PostTests extends BaseTest {

    @Test
    @Story("Get all posts")
    @Description("Verify that GET /posts returns 100 posts with status 200")
    @Severity(SeverityLevel.CRITICAL)
    public void getAllPosts_shouldReturn200WithPostList() {
        log.info("Test: Get all posts");

        given()
            .spec(requestSpec)
        .when()
            .get(Endpoints.POSTS)
        .then()
            .statusCode(200)
            .body("size()", equalTo(100))
            .body("[0].id", notNullValue())
            .body("[0].title", notNullValue());
    }

    @Test
    @Story("Get post by ID")
    @Description("Verify that GET /posts/{id} returns the correct post")
    @Severity(SeverityLevel.CRITICAL)
    public void getPostById_shouldReturn200WithCorrectPost() {
        int postId = 1;
        log.info("Test: Get post by ID = {}", postId);

        given()
            .spec(requestSpec)
            .pathParam("id", postId)
        .when()
            .get(Endpoints.POST_BY_ID)
        .then()
            .statusCode(200)
            .body("id", equalTo(postId))
            .body("userId", notNullValue())
            .body("title", notNullValue())
            .body("body", notNullValue());
    }

    @Test
    @Story("Get post by ID")
    @Description("Deserialize post response to POJO and assert all fields")
    @Severity(SeverityLevel.NORMAL)
    public void getPostById_shouldDeserializeToPostPojo() {
        int postId = 5;
        log.info("Test: Deserialize post {} to POJO", postId);

        Response response = given()
            .spec(requestSpec)
            .pathParam("id", postId)
        .when()
            .get(Endpoints.POST_BY_ID);

        assertThat(response.statusCode()).isEqualTo(200);

        Post post = response.as(Post.class);
        assertThat(post.getId()).isEqualTo(postId);
        assertThat(post.getUserId()).isNotNull().isGreaterThan(0);
        assertThat(post.getTitle()).isNotBlank();
        assertThat(post.getBody()).isNotBlank();

        log.info("Post deserialized: id={}, userId={}", post.getId(), post.getUserId());
    }

    @Test
    @Story("Get posts by user")
    @Description("Verify that posts can be filtered by userId query parameter")
    @Severity(SeverityLevel.NORMAL)
    public void getPostsByUserId_shouldReturnPostsForThatUser() {
        int userId = 1;
        log.info("Test: Get posts for userId={}", userId);

        List<Post> posts = given()
            .spec(requestSpec)
            .queryParam("userId", userId)
        .when()
            .get(Endpoints.POSTS)
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .extract()
            .jsonPath()
            .getList(".", Post.class);

        assertThat(posts).isNotEmpty();
        assertThat(posts).allSatisfy(post ->
            assertThat(post.getUserId()).isEqualTo(userId)
        );
    }

    @Test
    @Story("Create post")
    @Description("Verify that POST /posts creates a new post and returns 201")
    @Severity(SeverityLevel.CRITICAL)
    public void createPost_shouldReturn201WithCreatedPost() {
        Map<String, Object> postPayload = TestDataFactory.createPostPayload(1);
        log.info("Test: Create post for userId={}", postPayload.get("userId"));

        Response response = given()
            .spec(requestSpec)
            .body(postPayload)
        .when()
            .post(Endpoints.POSTS);

        assertThat(response.statusCode()).isEqualTo(201);

        Post createdPost = response.as(Post.class);
        assertThat(createdPost.getId()).isNotNull();
        assertThat(createdPost.getTitle()).isEqualTo(postPayload.get("title"));
        assertThat(createdPost.getBody()).isEqualTo(postPayload.get("body"));
        assertThat(createdPost.getUserId()).isEqualTo(postPayload.get("userId"));
    }

    @Test
    @Story("Update post")
    @Description("Verify that PUT /posts/{id} updates an existing post and returns 200")
    @Severity(SeverityLevel.NORMAL)
    public void updatePost_shouldReturn200WithUpdatedPost() {
        int postId = 1;
        Map<String, Object> updatePayload = TestDataFactory.createPostPayload(1);
        log.info("Test: Update post id={}", postId);

        given()
            .spec(requestSpec)
            .pathParam("id", postId)
            .body(updatePayload)
        .when()
            .put(Endpoints.POST_BY_ID)
        .then()
            .statusCode(200)
            .body("id", equalTo(postId))
            .body("title", equalTo(updatePayload.get("title")));
    }

    @Test
    @Story("Partial update post")
    @Description("Verify that PATCH /posts/{id} partially updates a post and returns 200")
    @Severity(SeverityLevel.MINOR)
    public void patchPost_shouldReturn200WithPatchedTitle() {
        int postId = 1;
        String newTitle = TestDataFactory.randomSentence();
        log.info("Test: Patch post id={} with newTitle", postId);

        given()
            .spec(requestSpec)
            .pathParam("id", postId)
            .body(Map.of("title", newTitle))
        .when()
            .patch(Endpoints.POST_BY_ID)
        .then()
            .statusCode(200)
            .body("title", equalTo(newTitle));
    }

    @Test
    @Story("Delete post")
    @Description("Verify that DELETE /posts/{id} returns 200")
    @Severity(SeverityLevel.NORMAL)
    public void deletePost_shouldReturn200() {
        int postId = 1;
        log.info("Test: Delete post id={}", postId);

        given()
            .spec(requestSpec)
            .pathParam("id", postId)
        .when()
            .delete(Endpoints.POST_BY_ID)
        .then()
            .statusCode(200);
    }

    @Test
    @Story("Get post by ID")
    @Description("Verify that GET /posts/{id} with non-existent ID returns 404")
    @Severity(SeverityLevel.NORMAL)
    public void getPostById_withInvalidId_shouldReturn404() {
        int nonExistentId = 9999;
        log.info("Test: Get post with non-existent id={}", nonExistentId);

        given()
            .spec(requestSpec)
            .pathParam("id", nonExistentId)
        .when()
            .get(Endpoints.POST_BY_ID)
        .then()
            .statusCode(404);
    }
}
