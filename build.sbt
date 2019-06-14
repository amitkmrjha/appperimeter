import akka.grpc.gen.scaladsl.play.{ PlayScalaClientCodeGenerator, PlayScalaServerCodeGenerator }

organization in ThisBuild := "com.amit"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.8"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test

val lagomGrpcTestkit = "com.lightbend.play" %% "lagom-scaladsl-grpc-testkit" % "0.6.0"

lagomServiceEnableSsl in ThisBuild := true
val `hostip-impl-HTTPS-port` = 11000


// ALL SETTINGS HERE ARE TEMPORARY WORKAROUNDS FOR KNOWN ISSUES OR WIP
def workaroundSettings: Seq[sbt.Setting[_]] = Seq(
  // Lagom still can't register a service under the gRPC name so we hard-code t
  // he port and the use the value to add the entry on the Service Registry
  lagomServiceHttpsPort := `hostip-impl-HTTPS-port`
)

lazy val `app-perimeter` = (project in file("."))
  .aggregate(`common`,`hostip-api`, `hostip-impl`)

lazy val `common` = (project in file("common"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      scalaTest
    )
  )

lazy val `hostip-api` = (project in file("hostip-api"))
  .settings(
    libraryDependencies += lagomScaladslApi
  ).dependsOn(`common` )

lazy val `hostip-impl` = (project in file("hostip-impl"))
  .enablePlugins(LagomScala)
  .enablePlugins(AkkaGrpcPlugin) // enables source generation for gRPC
  .enablePlugins(PlayAkkaHttp2Support) // enables serving HTTP/2 and gRPC
  .settings(
    akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala),
    akkaGrpcGeneratedSources :=
      Seq(
        AkkaGrpc.Server,
        AkkaGrpc.Client // the client is only used in tests. See https://github.com/akka/akka-grpc/issues/410
      ),
    akkaGrpcExtraGenerators in Compile += PlayScalaServerCodeGenerator,
  ).settings(
    workaroundSettings:_*
  ).settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      macwire,
      scalaTest,
      lagomGrpcTestkit
    )
  ).settings(lagomForkedTestSettings: _*)
  .settings(
    lagomDevSettings := Seq("akka.discovery.method" -> "lagom-dev-mode")
  )
  .dependsOn(`common`,`hostip-api`)



// This sample application doesn't need either Kafka or Cassandra so we disable them
// to make the devMode startup faster.
lagomCassandraEnabled in ThisBuild := false
lagomKafkaEnabled in ThisBuild := false


// This adds an entry on the LagomDevMode Service Registry. With this information on
// the Service Registry a client using Service Discovery to Lookup("helloworld.GreeterService")
// will get "https://localhost:11000" and then be able to send a request.
// See declaration and usages of `hello-impl-HTTPS-port`.
lagomUnmanagedServices in ThisBuild := Map("hostip.HostIpGrpcService" -> s"https://localhost:${`hostip-impl-HTTPS-port`}",
  "cas_native" -> "http://localhost:9042"
)


ThisBuild / scalacOptions ++= List("-encoding", "utf8", "-deprecation", "-feature", "-unchecked")


