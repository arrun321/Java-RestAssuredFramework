@users @regression
Feature: Users API
  As an API consumer
  I want to manage users via the REST API
  So that I can perform CRUD operations on user resources

  Background:
    Given the REST API is available

  @smoke @get
  Scenario: Get all users returns a non-empty list
    When I send a GET request to "/users"
    Then the response status code should be 200
    And the response should contain a list of users
    And every user in the list should have an id, name and email

  @smoke @get
  Scenario: Get a specific user by id
    When I send a GET request to "/users/1"
    Then the response status code should be 200
    And the response user id should be 1
    And the response user name should not be empty
    And the response user email should not be empty

  @get
  Scenario Outline: Get users by different valid IDs
    When I send a GET request to "/users/<userId>"
    Then the response status code should be 200
    And the response user id should be <userId>

    Examples:
      | userId |
      | 1      |
      | 3      |
      | 7      |
      | 10     |

  @negative @get
  Scenario: Get a user that does not exist returns 404
    When I send a GET request to "/users/9999"
    Then the response status code should be 404

  @post @smoke
  Scenario: Create a new user
    Given I have a new user payload with name "John Doe" and email "john.doe@example.com"
    When I send a POST request to "/users" with the payload
    Then the response status code should be 201
    And the created resource should have a generated id
    And the response user name should be "John Doe"
    And the response user email should be "john.doe@example.com"

  @post
  Scenario: Create a user with random data
    Given I have a randomly generated user payload
    When I send a POST request to "/users" with the payload
    Then the response status code should be 201
    And the created resource should have a generated id

  @put
  Scenario: Update an existing user
    Given I have a user payload with name "Jane Updated" and email "jane@updated.com"
    When I send a PUT request to "/users/1" with the payload
    Then the response status code should be 200
    And the response user id should be 1
    And the response user name should be "Jane Updated"

  @patch
  Scenario: Partially update a user's email
    Given I have a patch payload with field "email" and value "patched@example.com"
    When I send a PATCH request to "/users/1" with the payload
    Then the response status code should be 200
    And the response field "email" should be "patched@example.com"

  @delete
  Scenario: Delete a user
    When I send a DELETE request to "/users/1"
    Then the response status code should be 200
