Feature: A module runs once, every interval or on the dates of a cron expression in its time zone

  A module that works in rounds runs once, every interval or on the dates of a Unix cron expression read in a
  time zone (ADR-015). Running once and running every interval start when the module starts; a cron expression
  waits for its first date. After a round, the next execution comes after the round ends, so the dates that
  pass during a round are not caught up.

  @DEV-85
  Scenario: Running once runs when the module starts and never again
    Given a module that runs once
    When the module starts at "2026-11-22T10:00:00Z"
    Then the first execution is at "2026-11-22T10:00:00Z"
    And there is no execution after a round that ends at "2026-11-22T10:03:00Z"

  @DEV-85
  Scenario: Running every interval starts when the module starts and counts from the end of each round
    Given a module that runs every "5m"
    When the module starts at "2026-11-22T10:00:00Z"
    Then the first execution is at "2026-11-22T10:00:00Z"
    And the execution after a round that ends at "2026-11-22T10:01:30Z" is at "2026-11-22T10:06:30Z"

  @DEV-85
  Scenario: A cron expression waits for its first date
    Given a module that runs on "0 8 1 * *" in "Europe/Madrid"
    When the module starts at "2026-11-22T10:00:00Z"
    Then the first execution is at "2026-12-01T07:00:00Z"

  @DEV-85
  Scenario Outline: A cron expression runs at the hour of its zone in winter and in summer time
    Given a module that runs on "0 8 1 * *" in "Europe/Madrid"
    When the module starts at "<start>"
    Then the first execution is at "<execution>"

    Examples:
      | start                | execution            |
      | 2026-02-15T12:00:00Z | 2026-03-01T07:00:00Z |
      | 2026-03-15T12:00:00Z | 2026-04-01T06:00:00Z |

  @DEV-85
  Scenario: The dates that pass during a round are not caught up
    Given a module that runs on "*/15 * * * *" in "UTC"
    When the module starts at "2026-11-22T10:00:00Z"
    Then the execution after a round that ends at "2026-11-22T10:40:00Z" is at "2026-11-22T10:45:00Z"

  @DEV-85
  Scenario Outline: With a day of the month and a day of the week, either of them is enough
    Given a module that runs on "0 0 1 * MON" in "UTC"
    When the module starts at "<start>"
    Then the first execution is at "<execution>"

    Examples:
      | start                | execution            |
      | 2026-11-22T12:00:00Z | 2026-11-23T00:00:00Z |
      | 2026-11-30T12:00:00Z | 2026-12-01T00:00:00Z |

  @DEV-85
  Scenario Outline: Only a Unix cron expression of 5 fields or a supported macro is a cron expression
    When the cron expression "<written>" is read
    Then the cron expression is rejected

    Examples:
      | written       |
      | 0 8 1 *       |
      | 0 0 8 1 * *   |
      | 61 * * * *    |
      | 0 0 L * *     |
      | 0 0 * * MON#1 |
      | @often        |
      | @reboot       |

  @DEV-85
  Scenario Outline: An interval that is not positive is rejected
    When a module is set to run every "<interval>"
    Then the schedule is rejected because the interval must be positive

    Examples:
      | interval |
      | 0s       |
      | -5m      |

  @DEV-85
  Scenario: An interval is required
    When a module is set to run every interval without one
    Then the schedule is rejected because the interval is required

  @DEV-85
  Scenario: A cron expression needs a time zone
    When a module is set to run on "0 8 1 * *" without a time zone
    Then the schedule is rejected because the time zone is required
