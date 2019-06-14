package com.amit.hostip.api

import akka.NotUsed
import com.amit.hostip.domain.AppHostResponse
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}

/**
  * The Hello service interface.
  * <p>
  * This describes everything that Lagom needs to know about how to serve and
  * consume the HelloService.
  */
trait HostIpService extends Service {

  /**
    * Example: curl http://localhost:9000/api/hello/Alice
    */
  def hello(id: String): ServiceCall[NotUsed, String]

  def getAppHost(app_sha256: String): ServiceCall[NotUsed, AppHostResponse]

  override final def descriptor = {
    import Service._
    named("hostip-srvc")
      .withCalls(
        pathCall("/api/hello/:id", hello _),
        restCall(Method.GET, "/event/:app_sha256", getAppHost _),
      )
      .withAutoAcl(true)
  }
}

