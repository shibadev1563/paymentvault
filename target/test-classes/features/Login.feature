Feature: User Login

  Scenario: Valid user login
    Given I am on the login page
    When I enter valid credentials
    And I click on the login button
    Then I should be redirected to the dashboard

#  Scenario: Invalid user login
#    Given I am on the login page
#    When I enter invalid credentials
#    And I click on the login button
#    Then I should see an error message