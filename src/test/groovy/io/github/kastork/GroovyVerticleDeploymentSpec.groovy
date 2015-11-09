package io.github.kastork

import io.github.kastork.example.SomeVerticle
import io.vertx.core.Vertx
import spock.lang.Specification
import spock.lang.Stepwise
import spock.util.concurrent.AsyncConditions

@Stepwise
class GroovyVerticleDeploymentSpec extends Specification
{
  Vertx vertx;
  AsyncConditions conditions

  def setup()
  {
    vertx = Vertx.vertx()
  }

  def cleanup()
  {
    vertx.close()
  }

  def "Deploys and undeploys a groovy verticle"()
  {

    setup:

    conditions = new AsyncConditions(2);

    expect:

    vertx.deployVerticle("SomeGroovyVerticle.groovy") {deployResponse ->

      conditions.evaluate {
        assert deployResponse.succeeded()
      }

      vertx.undeploy(deployResponse.result) {undeployResponse ->
        conditions.evaluate {
          assert undeployResponse.succeeded()
        }
      }
    }

    conditions.await(5d)
  }
}
