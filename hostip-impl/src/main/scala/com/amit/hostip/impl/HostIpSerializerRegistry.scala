package com.amit.hostip.impl

import com.amit.hostip.domain.{AppHostIp, AppHostResponse}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

import scala.collection.immutable

object HostIpSerializerRegistry  extends JsonSerializerRegistry {
  override def serializers: immutable.Seq[JsonSerializer[_]] = List(
    JsonSerializer[AppHostIp],
  JsonSerializer[AppHostResponse])

}
