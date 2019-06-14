package com.amit.hostip.impl

import akka.stream.Materializer
import amit.myapp.hostip.grpc.{HelloRequest, HostIpGrpcServiceClient}
import com.amit.hostip.api._
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.lightbend.lagom.scaladsl.testkit.grpc.AkkaGrpcClientHelpers
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class HostIpServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server: ServiceTest.TestServer[HelloApplication with LocalServiceLocator] = ServiceTest.startServer(
    ServiceTest.defaultSetup.withSsl(true)
  ) { ctx =>
    new HelloApplication(ctx) with LocalServiceLocator
  }

  val client: HostIpService = server.serviceClient.implement[HostIpService]
  val grpcClient: HostIpGrpcServiceClient = AkkaGrpcClientHelpers.grpcClient(
    server,
    HostIpGrpcServiceClient.apply,
  )

  implicit val mat: Materializer = server.materializer

  override protected def afterAll(): Unit = {
    grpcClient.close()
    server.stop()
  }

  "Hello service" should {

    "say hello over HTTP" in {
      client.hello("Alice").invoke().map { answer =>
        answer should ===("Hi Alice!")
      }
    }

    "say hello over gRPC" in {
      grpcClient
        .sayHello(HelloRequest("Alice"))
        .map{
          _.message should be ("Hi Alice! (gRPC)")
        }
    }

  }
}
