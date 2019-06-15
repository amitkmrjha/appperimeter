package com.amit.hostip.impl.daos

import com.datastax.driver.core.querybuilder.{Delete, Insert}
import java.util

import akka.Done
import com.amit.hostip.domain.{AppHostIp, HostIp}
import com.datastax.driver.core.{BoundStatement, PreparedStatement}
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import play.api.Logger

import scala.collection.JavaConverters._
import com.datastax.driver.core.querybuilder.QueryBuilder

import scala.concurrent.{ExecutionContext, Future, Promise}


trait HostIpTable[T <: HostIp] {

  private val logger = Logger(this.getClass)

  protected val insertPromise = Promise[PreparedStatement]

  protected val deletePromise = Promise[PreparedStatement]

  protected def tableName: String

  protected def primaryKey: String

  protected def tableScript: String

  protected def fields: Seq[String]

  protected def prepareDelete : Delete.Where

  protected def getDeleteBindValues(entity: T): Seq[AnyRef]

  protected def cL: util.List[String]

  protected def vL: util.List[AnyRef]

  protected def prepareInsert : Insert

  protected def  getInsertBindValues(entity: T): Seq[AnyRef]

  protected def getAllQueryString: String

  def insert(t:T)
            (implicit session: CassandraSession, ec: ExecutionContext):Future[Option[BoundStatement]] =  {
    val bindV = getInsertBindValues(t)
    bindPrepare(insertPromise,bindV).map(x => Some(x))
  }

  def insert(ts:Seq[T])
            (implicit session: CassandraSession, ec: ExecutionContext):Future[Seq[BoundStatement]] =  {
    val seqF = ts.map{t=>
      val bindV = getInsertBindValues(t)
      bindPrepare(insertPromise,bindV)
    }
    Future.sequence(seqF)
  }

  def delete(t:T)
            (implicit session: CassandraSession, ec: ExecutionContext):Future[Option[BoundStatement]] ={
    val bindV = getDeleteBindValues(t)
    bindPrepare(deletePromise,bindV).map(x => Some(x))
  }

  def createTable()
                 (implicit session: CassandraSession, ec: ExecutionContext):Future[Done] = {
    for {
      _ <- sessionExecuteCreateTable(tableScript)
    }yield Done
  }

  protected def sessionExecuteCreateTable(tableScript: String)
                                         (implicit session: CassandraSession, ec: ExecutionContext) : Future[Done] = {
    session.executeCreateTable(tableScript).recover {
      case ex: Exception =>
        logger.error(s"Store MS CreateTable ${tableScript} execute error => ${ex.getMessage}", ex)
        throw ex
    }
  }

  def prepareStatement()
                      (implicit session: CassandraSession, ec: ExecutionContext):Future[Done] = {
    val insertRepositoryFuture = sessionPrepare(prepareInsert.toString)
    insertPromise.completeWith(insertRepositoryFuture)
    val deleteRepositoryFuture = sessionPrepare(prepareDelete.toString)
    deletePromise.completeWith(deleteRepositoryFuture)
    for {
      _ <- insertRepositoryFuture
      _ <- deleteRepositoryFuture
    }yield Done
  }

  protected def sessionPrepare(stmt: String)
                              (implicit session: CassandraSession, ec: ExecutionContext):Future[PreparedStatement] = {
    session.prepare(stmt).recover{
      case ex:Exception =>
        logger.error(s"Statement ${stmt} prepare error => ${ex.getMessage}",ex)
        throw ex
    }
  }

  protected def bindPrepare(ps:Promise[PreparedStatement],bindV:Seq[AnyRef])(implicit session: CassandraSession, ec: ExecutionContext):Future[BoundStatement] = {
    ps.future.map(x =>
      try {
        x.bind(bindV: _*)
      }catch{
        case ex:Exception =>
          logger.error(s"bindPrepare ${x.getQueryString} => ${ex.getMessage}", ex)
          throw ex
      }
    )
  }

}

object AppHostIpTable extends HostIpTable[AppHostIp]{

  override protected def tableScript: String =
    s"""
     CREATE TABLE IF NOT EXISTS ${tableName} (
         ${Columns.Id} timeuuid,
         ${Columns.AppSha256} text,
         ${Columns.Ip}  bigint,
         PRIMARY KEY (${primaryKey})
         )
      """.stripMargin

  override protected def fields: Seq[String]  = Seq(
    Columns.Id,
    Columns.AppSha256,
    Columns.Ip,
  )

  override protected def cL: util.List[String] = fields.toList.asJava

  override protected def vL: util.List[AnyRef] = fields.map(_ =>
    QueryBuilder.bindMarker().asInstanceOf[AnyRef]).toList.asJava

  override protected def prepareInsert: Insert  = QueryBuilder.insertInto(tableName).values(cL, vL)

  override protected def prepareDelete: Delete.Where = QueryBuilder.delete().from(tableName)
    .where(QueryBuilder.eq(Columns.AppSha256,QueryBuilder.bindMarker()))
    .and(QueryBuilder.eq(Columns.Ip,QueryBuilder.bindMarker()))

  override protected def getDeleteBindValues(entity: AppHostIp): Seq[AnyRef] = {
    val bindValues: Seq[AnyRef] = Seq(entity.app_sha256,entity.ip.asInstanceOf[java.lang.Long])
    bindValues
  }
  override protected def getInsertBindValues(entity: AppHostIp): Seq[AnyRef] = {
    val bindValues: Seq[AnyRef] = fields.map(x => x match {
      case Columns.Id => entity.id
      case Columns.AppSha256 => entity.app_sha256
      case Columns.Ip => entity.ip.asInstanceOf[java.lang.Long]
    })
    bindValues
  }

  override  def getAllQueryString: String =  {
    val select = QueryBuilder.select().from(tableName)
    select.toString
  }

  def byAppId (app_sha256: String): String = {
    val select = QueryBuilder.select().from(tableName)
      .where(QueryBuilder.eq(Columns.AppSha256, app_sha256))
    select.toString
  }

  def deleteByAppId (app_sha256: String): String = {
    val select = QueryBuilder.delete().from(tableName)
      .where(QueryBuilder.eq(Columns.AppSha256, app_sha256))
    select.toString
  }

  override protected def tableName: String = ColumnFamilies.AppHostIp

  override protected def primaryKey: String = s"${Columns.AppSha256},${Columns.Ip}"
}
