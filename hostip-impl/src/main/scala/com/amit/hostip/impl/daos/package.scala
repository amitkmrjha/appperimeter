package com.amit.hostip.impl

package object daos {

  object ColumnFamilies {
    val AppHostIp: String = "app_host_ip"
  }

  object Columns {
    val Id: String = "id"
    val AppSha256: String = "app_sha256"
    val Ip: String = "ip"
  }

}
