package com.restassured.framework.bdd.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Cucumber TestNG runner.
 * Discovers feature files under src/test/resources/features and runs them.
 *
 * Tags can be overridden at runtime:
 *   mvn test -Dcucumber.filter.tags="@smoke"
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {
                "com.restassured.framework.bdd.steps",
                "com.restassured.framework.bdd.hooks"
        },
        plugin = {
                "pretty",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "json:target/cucumber-reports/cucumber.json",
                "html:target/cucumber-reports/cucumber.html",
                "junit:target/cucumber-reports/cucumber.xml"
        },
        monochrome = true,
        publish = false
)
public class CucumberRunner extends AbstractTestNGCucumberTests {

    /**
     * Run each scenario sequentially (set parallel = true to run in parallel).
     */
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
