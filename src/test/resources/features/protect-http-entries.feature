Feature: The node turns away HTTP requests that cannot prove who sends them

  Every HTTP entry of a node, except its health, names the provider that identifies the caller and the role the
  caller needs. The node checks both before the entry runs, and tells nobody why a request was turned away.

  Background:
    Given the node has an entry "/spool/example" that requires the role "example-reader"

  @DEV-82
  Scenario: A request without credentials is turned away
    When a request without credentials reaches "/spool/example"
    Then the request is rejected because nobody was identified
    And the entry does not run

  @DEV-82
  Scenario: A caller without the role is turned away
    Given "ana" is identified with the role "example-writer"
    When "ana" calls "/spool/example"
    Then the request is rejected for lack of permission
    And the entry does not run

  @DEV-82
  Scenario: A caller with the role is let in
    Given "ana" is identified with the role "example-reader"
    When "ana" calls "/spool/example"
    Then the entry runs for "ana"

  @DEV-82
  Scenario: The health of the node needs no credentials
    When a request without credentials reaches "/spool/health"
    Then the node answers with its health

  @DEV-82
  Scenario: A rejection is logged without the credentials
    Given the provider rejects the token "t0ken-that-must-not-leak" because "the token has expired"
    When a request with the token "t0ken-that-must-not-leak" reaches "/spool/example"
    Then the request is rejected because nobody was identified
    And the log says "the token has expired"
    And the log does not contain "t0ken-that-must-not-leak"
