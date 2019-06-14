package com.amit.hostip.impl

import amit.myapp.hostip.grpc.IpEvent
import com.amit.hostip.domain.{AppHostIp, HostIp}
import com.datastax.driver.core.utils.UUIDs

object EventDomainUtil {
  implicit class HostIpOps(hostIp: HostIp) {
    def toIpEvent = IpEvent(hostIp.app_sha256,hostIp.ip)
  }

  implicit class IpEventOps(ipEvent: IpEvent) {
    def toHostIp = AppHostIp(UUIDs.timeBased(),ipEvent.appSha256,ipEvent.ip)
  }
}
