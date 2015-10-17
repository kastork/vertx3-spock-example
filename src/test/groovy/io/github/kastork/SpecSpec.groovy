package io.github.kastork

import io.vertx.core.Vertx
import spock.lang.Specification
import spock.util.concurrent.AsyncConditions

class SpecSpec extends Specification
{
  def conds = new AsyncConditions(2)

  def "Is fascinating"()
  {
    given:

    def vertx = Vertx.vertx()
    def deployAssertion = {response ->
      conds.evaluate {
        assert response.succeeded()
        println "deploy assertion"
      }
    }

    when:

    def server = vertx
        .createHttpServer()
        .requestHandler {req -> req.response.end("foo")}
        .listen(8080, deployAssertion)

    def client = vertx.createHttpClient()
    def responseAssertion = {result ->
      conds.evaluate {
        assert result.toString().equals("foo")
        println "response assertion"

      }
    }

    client.getNow(
        8080, "localhost", "/"
    ) {resp ->
      resp.bodyHandler(responseAssertion)
      client.close();
    }
    
    then:

    conds.await()

    cleanup:
    
    vertx.close();
  }
}
