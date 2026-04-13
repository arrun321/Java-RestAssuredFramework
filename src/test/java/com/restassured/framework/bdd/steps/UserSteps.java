package com.restassured.framework.bdd.steps;

import com.restassured.framework.bdd.context.TestContext;
import com.restassured.framework.models.User;
import com.restassured.framework.utils.TestDataFactory;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions specific to the Users API feature.
 * PicoContainer injects the shared TestContext instance.
 */
public class UserSteps {

    private static final Logger log = LogManager.getLogger(UserSteps.class);
    private final TestContext testContext;

    public UserSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    // ─── Given ────────────────────────────────────────────────────────────────

    @Given("I have a new user payload with name {string} and email {string}")
    public void iHaveANewUserPayload(String name, String email) {
        testContext.clearPayload();
        testContext.putPayloadField("name", name);
        testContext.putPayloadField("email", email);
        testContext.putPayloadField("username", name.toLowerCase().replace(" ", "."));
        log.info("User payload: name={}, email={}", name, email);
    }

    @Given("I have a randomly generated user payload")
    public void iHaveARandomlyGeneratedUserPayload() {
        testContext.clearPayload();
        testContext.getRequestPayload().putAll(TestDataFactory.createUserPayload());
        log.info("Random user payload generated: {}", testContext.getRequestPayload());
    }

    @Given("I have a user payload with name {string} and email {string}")
    public void iHaveAUserPayloadForUpdate(String name, String email) {
        testContext.clearPayload();
        testContext.putPayloadField("name", name);
        testContext.putPayloadField("email", email);
        log.info("Update payload: name={}, email={}", name, email);
    }

    // ─── Then / And ───────────────────────────────────────────────────────────

    @Then("the response should contain a list of users")
    public void theResponseShouldContainAListOfUsers() {
        List<?> users = testContext.getResponse().jsonPath().getList("$");
        assertThat(users).as("Users list should not be empty").isNotEmpty();
        log.info("Users list size: {}", users.size());
    }

    @And("every user in the list should have an id, name and email")
    public void everyUserShouldHaveIdNameAndEmail() {
        List<User> users = testContext.getResponse().jsonPath().getList("$", User.class);
        assertThat(users).allSatisfy(user -> {
            assertThat(user.getId()).isNotNull();
            assertThat(user.getName()).isNotBlank();
            assertThat(user.getEmail()).isNotBlank();
        });
    }

    @And("the response user id should be {int}")
    public void theResponseUserIdShouldBe(int expectedId) {
        Integer actualId = testContext.getResponse().jsonPath().getInt("id");
        assertThat(actualId).isEqualTo(expectedId);
    }

    @And("the response user name should not be empty")
    public void theResponseUserNameShouldNotBeEmpty() {
        String name = testContext.getResponse().jsonPath().getString("name");
        assertThat(name).isNotBlank();
    }

    @And("the response user email should not be empty")
    public void theResponseUserEmailShouldNotBeEmpty() {
        String email = testContext.getResponse().jsonPath().getString("email");
        assertThat(email).isNotBlank().contains("@");
    }

    @And("the response user name should be {string}")
    public void theResponseUserNameShouldBe(String expectedName) {
        String actualName = testContext.getResponse().jsonPath().getString("name");
        assertThat(actualName).isEqualTo(expectedName);
    }

    @And("the response user email should be {string}")
    public void theResponseUserEmailShouldBe(String expectedEmail) {
        String actualEmail = testContext.getResponse().jsonPath().getString("email");
        assertThat(actualEmail).isEqualTo(expectedEmail);
    }
}
