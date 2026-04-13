@status @smoke
Feature: Server Status
  As an API consumer
  I want to verify the Course Enrollment API is healthy
  So that I know the service is available before running tests

  Background:
    Given the base URL is the course enrollment API

  Scenario: Health check confirms server is running
    When I send a GET request to "/status"
    Then the response status code should be 200
    And the response confirms the "Server is running"
