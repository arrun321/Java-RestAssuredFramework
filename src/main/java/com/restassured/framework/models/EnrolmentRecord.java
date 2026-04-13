package com.restassured.framework.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Response model for Course Enrollment API enrolment records.
 * Status can be: active, dropped, completed.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnrolmentRecord {

    @JsonProperty("_id")
    private String id;

    private String username;
    private String courseCode;
    private String title;
    private String status;
    private String enrolledAt;
    private String completedAt;
}
