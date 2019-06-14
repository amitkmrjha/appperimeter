package com.amit.hostip.impl

import com.amit.hostip.api.HostIpService
import com.amit.hostip.impl.daos.{AppHostIpDao, ReadDao}
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

class HostIpLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new HelloApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new HelloApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[HostIpService])
}

abstract class HelloApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents
    with CassandraPersistenceComponents {

  // Bind the service that this server provides
  override lazy val lagomServer =
    serverFor[HostIpService](wire[HostIpServiceImpl])
    .additionalRouter(wire[HostIpGrpcServiceImpl])

  lazy val hostIpDao = wire[AppHostIpDao]

  lazy val hostIpEventHandler:HostIpEventHandler =  wire[HostIpEventHandler]

  override lazy val jsonSerializerRegistry = HostIpSerializerRegistry

}
