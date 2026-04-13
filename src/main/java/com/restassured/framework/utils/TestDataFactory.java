package com.restassured.framework.utils;

import com.github.javafaker.Faker;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides random test data using Java Faker.
 */
public class TestDataFactory {

    private static final Faker faker = new Faker();

    private TestDataFactory() {}

    public static String randomName() {
        return faker.name().fullName();
    }

    public static String randomFirstName() {
        return faker.name().firstName();
    }

    public static String randomLastName() {
        return faker.name().lastName();
    }

    public static String randomEmail() {
        return faker.internet().emailAddress();
    }

    public static String randomUsername() {
        return faker.name().username();
    }

    public static String randomPhone() {
        return faker.phoneNumber().phoneNumber();
    }

    public static String randomAddress() {
        return faker.address().streetAddress();
    }

    public static String randomCity() {
        return faker.address().city();
    }

    public static String randomCompany() {
        return faker.company().name();
    }

    public static String randomSentence() {
        return faker.lorem().sentence();
    }

    public static String randomParagraph() {
        return faker.lorem().paragraph();
    }

    public static int randomId(int min, int max) {
        return faker.number().numberBetween(min, max);
    }

    /**
     * Creates a sample user payload map for POST/PUT requests.
     */
    public static Map<String, Object> createUserPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", randomName());
        payload.put("username", randomUsername());
        payload.put("email", randomEmail());
        payload.put("phone", randomPhone());
        payload.put("website", faker.internet().domainName());
        return payload;
    }

    /**
     * Creates a sample post payload map for POST/PUT requests.
     */
    public static Map<String, Object> createPostPayload(int userId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("title", faker.lorem().sentence(5));
        payload.put("body", faker.lorem().paragraph());
        return payload;
    }
}
