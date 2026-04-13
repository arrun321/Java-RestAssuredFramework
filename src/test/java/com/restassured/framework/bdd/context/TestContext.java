package com.restassured.framework.bdd.context;

import com.restassured.framework.config.ConfigManager;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Shared test context injected via PicoContainer into all step definition classes.
 * Holds state across Given/When/Then steps within the same scenario.
 */
public class TestContext {

    private Response response;
    private Map<String, Object> requestPayload = new HashMap<>();

    // Dynamic base URL — null means use the default from config
    private String baseUrl;

    // Auth tokens set by login steps
    private String instructorToken;
    private String studentToken;
    private String studentUsername;

    // Created course tracking (for create → delete flows)
    private String createdCourseId;
    private String createdCourseCode;

    private String searchTitle;

    private String existingCourseId;

    // ─ Response ─────────────────────────────────────────────────────────────────

    public Response getResponse() { return response; }
    public void setResponse(Response response) { this.response = response; }

    // ─ Payload ────────────────────────────────────────────────────────────────

    public Map<String, Object> getRequestPayload() { return requestPayload; }
    public void setRequestPayload(Map<String, Object> requestPayload) { this.requestPayload = requestPayload; }
    public void putPayloadField(String key, Object value) { this.requestPayload.put(key, value); }

    public void clearPayload() {
        this.requestPayload.clear();
    }

    // ─ Base URL ──────────────────────────────────────────────────────────────

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    /**
     * Returns the explicit base URL if set, otherwise falls back to the default config URL.
     */
    public String getEffectiveBaseUrl() {
        return baseUrl != null ? baseUrl : ConfigManager.getInstance().getBaseUrl();
    }

    // ─ Auth tokens ──────────────────────────────────────────────────────────

    public String getInstructorToken() { return instructorToken; }
    public void setInstructorToken(String instructorToken) { this.instructorToken = instructorToken; }

    public String getStudentToken() { return studentToken; }
    public void setStudentToken(String studentToken) { this.studentToken = studentToken; }

    public String getStudentUsername() { return studentUsername; }
    public void setStudentUsername(String studentUsername) { this.studentUsername = studentUsername; }

    // ─ Created course ───────────────────────────────────────────────────────

    public String getCreatedCourseId() { return createdCourseId; }
    public void setCreatedCourseId(String createdCourseId) { this.createdCourseId = createdCourseId; }

    public String getCreatedCourseCode() { return createdCourseCode; }
    public void setCreatedCourseCode(String createdCourseCode) { this.createdCourseCode = createdCourseCode; }


    public String getSearchTitle() { return searchTitle; }
    public void setSearchTitle(String searchTitle) { this.searchTitle = searchTitle; }

    public String getExistingCourseId() { return existingCourseId; }
    public void setExistingCourseId(String existingCourseId) { this.existingCourseId = existingCourseId; }
}
