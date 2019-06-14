package com.amit.hostip.impl

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, RunnableGraph, Sink, Source}
import amit.myapp.hostip.grpc.{AbstractHostIpGrpcServiceRouter, HelloReply, HelloRequest, IpEvent}
import play.api.Logger

import scala.concurrent.Future

class HostIpGrpcServiceImpl(handler:HostIpEventHandler,mat: Materializer, system: ActorSystem)
  extends AbstractHostIpGrpcServiceRouter(mat, system) {

  val logger = Logger(this.getClass)


  implicit val ec = mat.executionContext
  implicit  val materializer = mat

  val consumer = Sink.ignore
  val runnableGraph: RunnableGraph[Sink[IpEvent, NotUsed]] =
    MergeHub.source[IpEvent](perProducerBufferSize = 16).to(consumer)

  val toConsumer: Sink[IpEvent, NotUsed] = runnableGraph.run()


  val (inboundHub: Sink[IpEvent, NotUsed], outboundHub: Source[HelloReply, NotUsed]) =
    MergeHub.source[IpEvent]
      .map(request => HelloReply(s"Hello, ${request.appSha256}"))
      .toMat(BroadcastHub.sink[HelloReply])(Keep.both)
      .run()

  override def sayHello(in: HelloRequest): Future[HelloReply] =
    Future.successful(HelloReply(s"Hi ${in.name}! (gRPC)"))

  override def ipEventClientStream(in: Source[IpEvent, NotUsed]): Future[HelloReply] = {
    logger.debug(s"sayHello to in stream...")
    in. via(handler.handle).runWith(Sink.ignore).map(_ => HelloReply("Finished processing ......"))
   // in.runWith(Sink.seq).map(elements => HelloReply(s"Hello, ${elements.map(_.appSha256).mkString(", ")}"))
    //in.runWith(toConsumer)

/*   logger.debug(s"ipEventClientStream invoked ......")

    MergeHub.source[IpEvent](perProducerBufferSize = 16).via(handler.handle).runWith(Sink.ignore)
      .map(_ => HelloReply("Finished processing ......") )*/
  }

  override def ipEventClientServerStream(in: Source[IpEvent, NotUsed]): Source[HelloReply, NotUsed] = {
    in.runWith(inboundHub)
    outboundHub
  }
}
