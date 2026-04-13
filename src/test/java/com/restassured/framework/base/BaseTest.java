package com.restassured.framework.base;

import com.restassured.framework.config.ConfigManager;
import com.restassured.framework.utils.RestClient;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * Base test class that all test classes must extend.
 * Sets up RestAssured configurations and shared specifications.
 */
public class BaseTest {

    protected static final Logger log = LogManager.getLogger(BaseTest.class);
    protected static final ConfigManager config = ConfigManager.getInstance();

    protected static RequestSpecification requestSpec;
    protected static ResponseSpecification responseSpec;

    @BeforeSuite(alwaysRun = true)
    public void setUpSuite() {
        log.info("=== Test Suite Starting | Environment: {} | Base URL: {} ===",
                config.getEnvironment(), config.getBaseUrl());

        RestAssured.baseURI = config.getBaseUrl();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        requestSpec = RestClient.buildRequestSpec();
        responseSpec = RestClient.buildResponseSpec();

        log.info("RestAssured configured successfully.");
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownSuite() {
        log.info("=== Test Suite Completed ===");
        RestAssured.reset();
    }
}
