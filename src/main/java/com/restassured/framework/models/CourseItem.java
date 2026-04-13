package com.restassured.framework.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request/Response model for Course Enrollment API course resources.
 * The _id field is MongoDB's auto-generated ID returned in responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseItem {

    @JsonProperty("_id")
    private String id;

    private String title;
    private String instructor;
    private String courseCode;
    private String category;
    private Integer totalCapacity;
    private Integer availableSlots;
    private String startDate;
    private String endDate;
}
