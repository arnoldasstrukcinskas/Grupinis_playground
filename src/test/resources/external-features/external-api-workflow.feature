@external
Feature: External API workflow

  Scenario: User completes the live hotel recommendation flow
    Given the external API is available
    When I register a unique external user
    Then the external response status should be 200
    When I login as the external user
    Then the external response status should be 200
    And the external response body should contain "User logged in"
    When I search live locations for "Barcelona"
    Then the external response status should be 200
    And the live location response should contain at least one destination
    When I request live hotels for the first destination
    Then the external response status should be 200
    And the live hotel response should contain at least one hotel
    When I request live analysis for "Plan a comfortable city break with good food and museums." and hobbies "museums, architecture, cafes"
    Then the external response status should be 200
    And the live analysis response should contain non-empty analysis text
