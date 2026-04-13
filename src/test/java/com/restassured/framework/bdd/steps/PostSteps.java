package com.restassured.framework.bdd.steps;

import com.restassured.framework.bdd.context.TestContext;
import com.restassured.framework.models.Post;
import com.restassured.framework.utils.TestDataFactory;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions specific to the Posts API feature.
 * PicoContainer injects the shared TestContext instance.
 */
public class PostSteps {

    private static final Logger log = LogManager.getLogger(PostSteps.class);
    private final TestContext testContext;

    public PostSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    // ─── Given ────────────────────────────────────────────────────────────────

    @Given("I have a new post payload with title {string} and body {string} for user {int}")
    public void iHaveANewPostPayload(String title, String body, int userId) {
        testContext.clearPayload();
        testContext.putPayloadField("title", title);
        testContext.putPayloadField("body", body);
        testContext.putPayloadField("userId", userId);
        log.info("Post payload: title={}, userId={}", title, userId);
    }

    @Given("I have a post payload with title {string} and body {string} for user {int}")
    public void iHaveAPostPayloadForUpdate(String title, String body, int userId) {
        testContext.clearPayload();
        testContext.putPayloadField("title", title);
        testContext.putPayloadField("body", body);
        testContext.putPayloadField("userId", userId);
        log.info("Update post payload: title={}, userId={}", title, userId);
    }

    // ─── Then / And ───────────────────────────────────────────────────────────

    @And("the response post id should be {int}")
    public void theResponsePostIdShouldBe(int expectedId) {
        Integer actualId = testContext.getResponse().jsonPath().getInt("id");
        assertThat(actualId).isEqualTo(expectedId);
    }

    @And("the response post title should not be empty")
    public void theResponsePostTitleShouldNotBeEmpty() {
        String title = testContext.getResponse().jsonPath().getString("title");
        assertThat(title).isNotBlank();
    }

    @And("the response post body should not be empty")
    public void theResponsePostBodyShouldNotBeEmpty() {
        String body = testContext.getResponse().jsonPath().getString("body");
        assertThat(body).isNotBlank();
    }

    @And("the response post title should be {string}")
    public void theResponsePostTitleShouldBe(String expectedTitle) {
        String actualTitle = testContext.getResponse().jsonPath().getString("title");
        assertThat(actualTitle).isEqualTo(expectedTitle);
    }

    @And("all posts in the response should belong to user {int}")
    public void allPostsShouldBelongToUser(int expectedUserId) {
        List<Post> posts = testContext.getResponse().jsonPath().getList("$", Post.class);
        assertThat(posts).isNotEmpty();
        assertThat(posts).allSatisfy(post ->
                assertThat(post.getUserId())
                        .as("Post userId should be %d but was %d", expectedUserId, post.getUserId())
                        .isEqualTo(expectedUserId)
        );
        log.info("All {} posts belong to userId={}", posts.size(), expectedUserId);
    }
}
