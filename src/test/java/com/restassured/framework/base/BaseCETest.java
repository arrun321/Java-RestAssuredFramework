package com.restassured.framework.base;

import com.restassured.framework.config.ConfigManager;
import com.restassured.framework.utils.RestClient;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;

/**
 * Base class for all Course Enrollment API TestNG tests.
 * Sets up a request spec targeting the CE API base URL.
 * Extend this instead of BaseTest when testing the CE API.
 */
public class BaseCETest {

    protected static final Logger log = LogManager.getLogger(BaseCETest.class);
    protected static final ConfigManager config = ConfigManager.getInstance();
    protected static final String CE_BASE_URL = ConfigManager.getInstance().getCourseEnrollmentBaseUrl();

    protected RequestSpecification requestSpec;

    @BeforeClass(alwaysRun = true)
    public void setUpCEBase() {
        requestSpec = RestClient.buildRequestSpec(CE_BASE_URL);
        log.info("=== CE Test Class Setup | Base URL: {} ===", CE_BASE_URL);
    }

    /**
     * Convenience method to build an auth spec after obtaining a token.
     */
    protected RequestSpecification authSpec(String token) {
        return RestClient.buildRequestSpecWithAuth(CE_BASE_URL, token);
    }
}
