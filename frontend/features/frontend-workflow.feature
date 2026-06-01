@frontend
Feature: Frontend workflow

  Scenario: Visitor is redirected to login and sees login validation
    When I open the chat page without a token
    Then I should be on the login page
    When I submit the login form without a username
    Then I should see "Username is required."

  Scenario: User logs in and reaches chat
    Given the frontend backend API is mocked
    When I open the login page
    And I login through the frontend as "frontend-user" with password "Test1234!"
    Then I should be on the chat page
    And I should see "Find your perfect hotel"

  Scenario: Authenticated user completes a mocked hotel recommendation flow
    Given the frontend backend API is mocked
    And I am authenticated in the frontend
    When I open the chat page
    And I choose destination "Barcelona"
    And I enter stay dates "2026-07-01" to "2026-07-04"
    And I search hotels from the frontend
    Then I should see "Hotels loaded"
    When I ask the frontend for recommendations "Plan a comfortable city break with good food and museums."
    Then I should see "Catalonia Sagrada Familia"
    When I save the frontend analysis
    Then I should see "Analysis saved"

  Scenario: Authenticated user views saved analysis history
    Given the frontend backend API is mocked
    And I am authenticated in the frontend
    When I open the history page
    Then I should see "Analysis #42"
    And I should see "1 saved"
