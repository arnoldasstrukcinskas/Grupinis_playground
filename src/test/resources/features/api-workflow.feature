Feature: API workflow

  Scenario: User completes the main hotel recommendation flow
    Given the real API is running under the test profile with external integrations stubbed
    When I register user "bdd-user" with password "Test1234!" and email "bdd-user@example.com"
    Then the response status should be 200
    And the response body should contain "User registered: bdd-user"
    When I login with username "bdd-user" and password "Test1234!"
    Then the response status should be 200
    And the response body should be a JWT token
    When I search locations for "Barcelona"
    Then the response status should be 200
    And the response body should contain "\"dest_id\":-372490"
    When I request hotels for destination -372490
    Then the response status should be 200
    And the response body should contain "\"hotelName\":\"Catalonia Sagrada Familia\""
    When I request an analysis for "Plan a comfortable city break with good food and museums." and hobbies "museums, architecture, cafes"
    Then the response status should be 200
    And the response body should contain "City break analysis"
    When I save the generated analysis
    Then the response status should be 200
    And the response body should contain "Analysis with id:"
    When I fetch all saved analyses
    Then the response status should be 200
    And the response body should contain "\"analysis\":\"City break analysis\""
    When I fetch the saved analysis by id
    Then the response status should be 200
    And the response body should contain "\"analysis\":\"City break analysis\""
    When I fetch hotels for the saved analysis
    Then the response status should be 200
    And the response body should contain a link to the saved hotel
    When I fetch one hotel from the saved analysis
    Then the response status should be 200
    And the response body should contain "\"hotelName\":\"Catalonia Sagrada Familia\""
    When I clear the in-memory analysis
    Then the response status should be 200
    And the response body should contain "clear"
    When I clear the in-memory hotels
    Then the response status should be 200
    And the response body should contain "Hotels service: Hotels cleared"
    When I logout with the current token
    Then the response status should be 200
    And the response body should contain "User with username: bdd-user, logged out."

  @known-bug
  Scenario: User deletes a saved analysis
    Given the real API is running under the test profile with external integrations stubbed
    When I register user "bdd-delete-user" with password "Test1234!" and email "bdd-delete-user@example.com"
    Then the response status should be 200
    When I login with username "bdd-delete-user" and password "Test1234!"
    Then the response status should be 200
    And the response body should be a JWT token
    When I search locations for "Barcelona"
    Then the response status should be 200
    When I request hotels for destination -372490
    Then the response status should be 200
    When I request an analysis for "Plan a comfortable city break with good food and museums." and hobbies "museums, architecture, cafes"
    Then the response status should be 200
    When I save the generated analysis
    Then the response status should be 200
    When I delete the saved analysis
    Then the response status should be 200
    And the response body should equal the saved analysis id
