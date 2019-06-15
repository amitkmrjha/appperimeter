package com.amit.hostip.impl.daos

import java.util.UUID

import akka.Done
import com.amit.hostip.domain.{AppHostIp, HostIp}
import com.datastax.driver.core.Row
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import scala.concurrent.{ExecutionContext, Future}

trait ReadDao[T <:HostIp] {

  def getByAppId(app_sha256: String):Future[Seq[T]]

  def deleteByAppId(app_sha256: String):Future[Done]

  protected def convert(r: Row): T

  protected def sessionSelectAll(queryString: String): Future[Seq[T]]

  protected def sessionSelectOne(queryString: String): Future[Option[T]]

  protected def id(r: Row):UUID = r.getUUID(Columns.Id)

  protected def app_sha256(r: Row):String = r.getString(Columns.AppSha256)

  protected def ip(r: Row):Long = r.getLong(Columns.Ip)
}

abstract class AbstractHostIpDao[T <:HostIp](session: CassandraSession)(implicit ec: ExecutionContext) extends ReadDao[T]{

  override protected def sessionSelectAll(queryString: String): Future[Seq[T]] = {
    session.selectAll(queryString).map(_.map(convert))
  }

  override protected def sessionSelectOne(queryString: String): Future[Option[T]] = {
    session.selectOne(queryString).map(_.map(convert))
  }
}

class AppHostIpDao(session: CassandraSession)(implicit ec: ExecutionContext) extends AbstractHostIpDao[HostIp](session){

  override def getByAppId(app_sha256: String): Future[Seq[HostIp]] = {
    sessionSelectAll(AppHostIpTable.byAppId(app_sha256))
  }

  override protected def convert(r: Row): HostIp = {
    AppHostIp(id(r),app_sha256(r),ip(r))
  }

  override def deleteByAppId(app_sha256: String): Future[Done] = {
    session.executeWrite(AppHostIpTable.deleteByAppId(app_sha256))
  }
}

