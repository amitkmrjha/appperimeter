package com.amit.hostip.impl

import akka.NotUsed
import com.amit.hostip.api.HostIpService
import com.amit.hostip.domain.AppHostResponse
import com.amit.hostip.impl.daos.AppHostIpDao
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

/**
  * Implementation of the HelloService.
  */
class HostIpServiceImpl(appHostIpDao:AppHostIpDao)(implicit ec: ExecutionContext) extends HostIpService {

  val logger = Logger(this.getClass)

  override def hello(id: String) = ServiceCall { _ =>
    Future.successful(s"Hi $id!")
  }

  override def getAppHost(app_sha256: String): ServiceCall[NotUsed, AppHostResponse] = ServiceCall { _ =>
    appHostIpDao.getByAppId(app_sha256).map(x =>
      AppHostResponse(
        count = x.size,
        good_ips= x.map(ip => ip.ip),
        bad_ips =  Seq.empty[Long]
      )
    ).recover {
      case ex: Exception => throw BadRequest(s"${ex.getMessage}")
    }
  }
}
