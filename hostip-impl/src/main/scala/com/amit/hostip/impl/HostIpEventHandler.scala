package com.amit.hostip.impl

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Keep, Sink}
import amit.myapp.hostip.grpc.IpEvent
import com.amit.hostip.impl.daos.AppHostIpTable
import com.datastax.driver.core.{BatchStatement, BoundStatement}
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import play.api.{Configuration, Logger}
import com.amit.hostip.impl.EventDomainUtil._

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}


class HostIpEventHandler(session:CassandraSession,
                         system: ActorSystem)(implicit ec: ExecutionContext) {
  val logger = Logger(this.getClass)

  AppHostIpTable.prepareStatement()(session,ec)
  def handle: Sink[Seq[IpEvent], NotUsed] = {

    def executeStatements(statements: Seq[BoundStatement]): Future[Done] = {
      val batch = new BatchStatement
      // statements is never empty, there is at least the store offset statement
      // for simplicity we just use batch api (even if there is only one)
      batch.addAll(statements.asJava)
      session.executeWriteBatch(batch)
    }
    Flow[Seq[IpEvent]]
      .mapAsync(parallelism = 1) {bind}
      .to(Sink.foreach(executeStatements))

  }

  private def bind(t: Seq[IpEvent]): Future[Seq[BoundStatement]] = {
     AppHostIpTable.insert(t.map(e => e.toHostIp))(session, ec)
  }

}
