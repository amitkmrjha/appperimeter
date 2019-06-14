package com.amit.hostip.domain

import play.api.libs.json.{Format, Json}

case class AppHostResponse(count: Int,good_ips:Seq[Long],bad_ips: Seq[Long])
object AppHostResponse {
  implicit val appHostResponseFormat: Format[AppHostResponse] = Json.format[AppHostResponse]
}
