@courses @regression
Feature: Course Management
  As an instructor
  I want to manage the course catalog
  So that students can browse and enrol in courses

  Background:
    Given the base URL is the course enrollment API
    And the instructor is authenticated

  @smoke @get
  Scenario: Get all courses from the catalog
    When I send a GET request to "/courses/all"
    Then the response status code should be 200
    And the response is a non-empty list of courses

  @get
  Scenario: Search courses by title returns matching results
    When I search for courses with title "hacking"
    Then the response status code should be 200
    And the response is a list of courses

  @get1
  Scenario: Check availability for an existing course and it shows slot information
    When I check availability for the first available course
    Then the response status code should be 200
    And the availability response contains a courseCode and availableSlots

  @get1
  Scenario: Check availability for an existing course with a particular course code and shows slot information
    When I search for course with course-code "TC20260407184805_911"
    Then the response status code should be 200
    And the availability response contains a courseCode and availableSlots

  @post @delete @smoke
  Scenario: Create a new course and then delete it
    When the instructor creates a course with a unique code
    Then the response status code should be 201
    When the instructor deletes the created course
    Then the response status code should be 200

  @negative
  Scenario: Creating a course without authentication returns 401
    When I attempt to create a course without a token
    Then the response status code should be 401
