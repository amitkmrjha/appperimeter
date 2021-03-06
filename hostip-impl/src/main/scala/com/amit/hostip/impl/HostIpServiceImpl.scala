package com.amit.hostip.impl

import akka.NotUsed
import com.amit.hostip.api.HostIpService
import com.amit.hostip.domain.AppHostResponse
import com.amit.hostip.impl.daos.AppHostIpDao
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import play.api.Logger
import play.utils.Reflect.SubClassOf

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
    appHostIpDao.getByAppId(app_sha256).map { x =>
      val gb = Subnet.getGoodBad(x.map(ip => ip.ip))
      AppHostResponse(
        count = x.size,
        good_ips = gb._1,
        bad_ips = gb._2
      )
    }.recover {
      case ex: Exception => throw BadRequest(s"${ex.getMessage}")
    }
  }

  override def deleteAppHost(app_sha256: String): ServiceCall[NotUsed, String] = ServiceCall { _ =>
    appHostIpDao.deleteByAppId(app_sha256).map(x => s"App id ${app_sha256} deleted from store"
    ).recover {
      case ex: Exception => throw BadRequest(s"${ex.getMessage}")
    }
  }
}
