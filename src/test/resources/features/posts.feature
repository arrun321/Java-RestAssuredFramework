@posts @regression
Feature: Posts API
  As an API consumer
  I want to manage posts via the REST API
  So that I can perform CRUD operations on post resources

  Background:
    Given the REST API is available

  @smoke @get
  Scenario: Get all posts returns 100 posts
    When I send a GET request to "/posts"
    Then the response status code should be 200
    And the response list size should be 100

  @smoke @get
  Scenario: Get a specific post by id
    When I send a GET request to "/posts/1"
    Then the response status code should be 200
    And the response post id should be 1
    And the response post title should not be empty
    And the response post body should not be empty

  @get
  Scenario Outline: Get posts by different valid IDs
    When I send a GET request to "/posts/<postId>"
    Then the response status code should be 200
    And the response post id should be <postId>

    Examples:
      | postId |
      | 1      |
      | 25     |
      | 50     |
      | 100    |

  @negative @get
  Scenario: Get a post that does not exist returns 404
    When I send a GET request to "/posts/9999"
    Then the response status code should be 404

  @get
  Scenario: Filter posts by userId query parameter
    When I send a GET request to "/posts" with query param "userId" equal to "1"
    Then the response status code should be 200
    And all posts in the response should belong to user 1

  @post @smoke
  Scenario: Create a new post
    Given I have a new post payload with title "My BDD Post" and body "This is the post body" for user 1
    When I send a POST request to "/posts" with the payload
    Then the response status code should be 201
    And the created resource should have a generated id
    And the response post title should be "My BDD Post"

  @put
  Scenario: Update an existing post
    Given I have a post payload with title "Updated Title" and body "Updated body" for user 1
    When I send a PUT request to "/posts/1" with the payload
    Then the response status code should be 200
    And the response post id should be 1
    And the response post title should be "Updated Title"

  @patch
  Scenario: Partially update a post title
    Given I have a patch payload with field "title" and value "Patched Post Title"
    When I send a PATCH request to "/posts/1" with the payload
    Then the response status code should be 200
    And the response field "title" should be "Patched Post Title"

  @delete
  Scenario: Delete a post
    When I send a DELETE request to "/posts/1"
    Then the response status code should be 200
