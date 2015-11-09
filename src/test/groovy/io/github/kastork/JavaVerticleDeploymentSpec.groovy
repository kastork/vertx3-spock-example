package io.github.kastork

import io.github.kastork.example.SomeVerticle
import io.vertx.core.Vertx
import spock.lang.Specification
import spock.lang.Stepwise
import spock.util.concurrent.AsyncConditions

@Stepwise
class JavaVerticleDeploymentSpec extends Specification
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

  def "Deploys and undeploys a java verticle"()
  {

    setup:

    conditions = new AsyncConditions(2);

    expect:

    vertx.deployVerticle(SomeVerticle.class.name) {deployResponse ->

      conditions.evaluate {
        assert deployResponse.succeeded()
      }

      vertx.undeploy(deployResponse.result) {undeployResponse ->
        conditions.evaluate {
          assert undeployResponse.succeeded()
        }
      }
    }

    conditions.await()
  }

}