@journey @regression
Feature: Student User Journey - End to End
  As a student
  I want to browse, enrol in, and drop courses
  So that I can manage my course schedule

  Background:
    Given the base URL is the course enrollment API

  @e2e @smoke
  Scenario: Complete student enrolment and drop flow
    Given the instructor is authenticated
    And the instructor creates a course with a unique code
    And the student is authenticated
    When the student enrols in the newly created course
    Then the response status code should be 201
    When the student views their active enrolments
    Then the response status code should be 200
    And the created course code appears in the active enrolments
    When the student drops the newly created course
    Then the response status code should be 200
    When the student views their enrolment history
    Then the response status code should be 200
    And the created course code appears in enrolment history with status "dropped"
    And the instructor deletes the created course
