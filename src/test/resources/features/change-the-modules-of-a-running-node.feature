Feature: A running node takes and drops the modules of a deployment without restarting

  In a cluster, the API of Spool assigns deployments to nodes that are already running (ADR-031). The node adds the
  modules of a deployment as a group, with their HTTP routes and their heartbeats, and drops the group as a whole.
  The modules it already ran keep running untouched.

  Background:
    Given a running node that added the deployment "orders" with the modules "orders-crawler"

  @DEV-94
  Scenario: The modules of a new deployment start in the running node
    When the node adds the deployment "payments" with the modules "payments-crawler" and "payments-ingester"
    Then the modules "payments-crawler" and "payments-ingester" are running
    And the health of the node lists "orders-crawler", "payments-crawler" and "payments-ingester"
    And the module "orders-crawler" was not restarted

  @DEV-94
  Scenario: The routes of an added module answer on the server of the node
    When the node adds the deployment "webhooks" with a module that answers "/spool/webhooks/orders"
    Then a request to "/spool/webhooks/orders" reaches that module

  @DEV-94
  Scenario: Removing a deployment stops its modules, their routes and their heartbeats
    Given the node added the deployment "webhooks" with a module that answers "/spool/webhooks/orders"
    When the node removes the deployment "webhooks"
    Then that module is stopped and sends no more heartbeats
    And a request to "/spool/webhooks/orders" finds no route
    And the health of the node lists only "orders-crawler"
    And the module "orders-crawler" is still running

  @DEV-94
  Scenario: A deployment whose route is taken is not added, and nothing of it is left behind
    Given the node added the deployment "webhooks" with a module that answers "/spool/webhooks/orders"
    When the node adds the deployment "copy" with a module that answers "/spool/webhooks/orders"
    Then adding it fails with an error that names "/spool/webhooks/orders"
    And no module of "copy" is running
    And a request to "/spool/webhooks/orders" reaches the module of "webhooks"

  @DEV-94
  Scenario: A deployment cannot be added twice
    When the node adds the deployment "orders" with the modules "orders-ingester"
    Then adding it fails with an error that says "orders" is already in the node

  @DEV-94
  Scenario: Removing a deployment the node does not have changes nothing
    When the node removes the deployment "unknown"
    Then the module "orders-crawler" is still running

  @DEV-94
  Scenario: Stopping the node stops every deployment it added
    Given the node added the deployment "payments" with the modules "payments-crawler"
    When the node stops
    Then the modules "orders-crawler" and "payments-crawler" are stopped
