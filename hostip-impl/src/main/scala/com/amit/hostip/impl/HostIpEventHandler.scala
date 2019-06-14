package com.amit.hostip.impl

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink}
import amit.myapp.hostip.grpc.IpEvent
import com.amit.hostip.impl.daos.AppHostIpTable
import com.datastax.driver.core.{BatchStatement, BoundStatement}
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import play.api.Configuration
import com.amit.hostip.impl.EventDomainUtil._

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}


class HostIpEventHandler(session:CassandraSession,
                         system: ActorSystem)(implicit ec: ExecutionContext) {

  AppHostIpTable.prepareStatement()(session,ec)


  def handle: Flow[IpEvent,Done, NotUsed] = {

    def executeStatements(statements: Seq[BoundStatement]): Future[Done] = {
      val batch = new BatchStatement
      // statements is never empty, there is at least the store offset statement
      // for simplicity we just use batch api (even if there is only one)
      batch.addAll(statements.asJava)
      session.executeWriteBatch(batch)
    }
    Flow[IpEvent].mapAsync(parallelism = 1) {elem =>
      bind(elem).flatMap{x =>
        executeStatements(x)
      }
    }
  }

  private def bind(t: IpEvent): Future[Seq[BoundStatement]] = {
    for {
      bs <- AppHostIpTable.insert(t.toHostIp)(session, ec)
    } yield {
      bs match {
        case Some(bs) => Seq(bs)
        case None => Seq.empty[BoundStatement]
      }
    }
  }

}
