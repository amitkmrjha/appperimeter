package com.amit.hostip.domain

import java.util.UUID

import play.api.libs.json.{Format, Json}

trait HostIp {

  def id : UUID

  def app_sha256: String

  def ip: Long
}

object HostIp {

}

case class AppHostIp(id:UUID,app_sha256 : String,ip:Long) extends HostIp
object AppHostIp {
  implicit val appHostIpFormat: Format[AppHostIp] = Json.format[AppHostIp]
}

