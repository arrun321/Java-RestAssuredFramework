package com.restassured.framework.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton configuration manager that loads properties from config.properties.
 * Supports environment-specific overrides via system properties.
 */
public class ConfigManager {

    private static final Logger log = LogManager.getLogger(ConfigManager.class);
    private static final String DEFAULT_CONFIG = "config.properties";

    private static ConfigManager instance;
    private final Properties properties = new Properties();

    private ConfigManager() {
        loadProperties(DEFAULT_CONFIG);
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void loadProperties(String fileName) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new RuntimeException("Configuration file not found: " + fileName);
            }
            properties.load(inputStream);
            log.info("Configuration loaded from: {}", fileName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration file: " + fileName, e);
        }
    }

    /**
     * Gets a property value. System property takes precedence over config file.
     */
    public String get(String key) {
        String systemProp = System.getProperty(key);
        if (systemProp != null && !systemProp.isEmpty()) {
            return systemProp;
        }
        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Property not found: " + key);
        }
        return value;
    }

    /**
     * Gets a property value with a default fallback.
     */
    public String get(String key, String defaultValue) {
        try {
            return get(key);
        } catch (RuntimeException e) {
            return defaultValue;
        }
    }

    public String getBaseUrl() {
        return get("base.url");
    }

    public int getConnectionTimeout() {
        return Integer.parseInt(get("connection.timeout", "10000"));
    }

    public int getReadTimeout() {
        return Integer.parseInt(get("read.timeout", "30000"));
    }

    public String getEnvironment() {
        return get("environment", "dev");
    }

    public boolean isLogRequestResponse() {
        return Boolean.parseBoolean(get("log.request.response", "true"));
    }

    // ─── Course Enrollment API ──────────────────────────────────────────────────

    public String getCourseEnrollmentBaseUrl() {
        return get("course.enrollment.base.url");
    }

    public String getCEInstructorUsername() {
        return get("course.enrollment.instructor.username");
    }

    public String getCEInstructorPassword() {
        return get("course.enrollment.instructor.password");
    }

    public String getCEStudentUsername() {
        return get("course.enrollment.student.username");
    }

    public String getCEStudentPassword() {
        return get("course.enrollment.student.password");
    }
}
