package com.restassured.framework.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Response model for instructor/student login endpoints.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {
    private String token;
    private String message;
}
