package com.restassured.framework.bdd.hooks;

import com.restassured.framework.bdd.context.TestContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * Cucumber lifecycle hooks.
 * Runs before/after each scenario to set up context and attach Allure artifacts.
 */
public class Hooks {

    private static final Logger log = LogManager.getLogger(Hooks.class);
    private final TestContext testContext;

    public Hooks(TestContext testContext) {
        this.testContext = testContext;
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        log.info("▶ Starting Scenario: [{}] {}", scenario.getId(), scenario.getName());
        testContext.clearPayload();
        testContext.setBaseUrl(null); // reset to default before each scenario (Background re-sets it if needed)
    }

    @After
    public void afterScenario(Scenario scenario) {
        if (testContext.getResponse() != null) {
            attachResponseToAllure(scenario);
        }

        if (scenario.isFailed()) {
            log.error("✗ Scenario FAILED: {}", scenario.getName());
        } else {
            log.info("✓ Scenario PASSED: {}", scenario.getName());
        }
    }

    private void attachResponseToAllure(Scenario scenario) {
        try {
            String responseBody = testContext.getResponse().getBody().asPrettyString();
            String statusCode = String.valueOf(testContext.getResponse().statusCode());

            Allure.addAttachment(
                    "Response [HTTP " + statusCode + "]",
                    "application/json",
                    new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8)),
                    ".json"
            );
        } catch (Exception e) {
            log.warn("Could not attach response to Allure report: {}", e.getMessage());
        }
    }
}
