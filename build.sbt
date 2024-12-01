ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.15"

lazy val root = (project in file("."))
  .settings(
    name := "Homework3",

    // Library dependencies
    libraryDependencies ++= Seq(
      // gRPC and Protobuf dependencies
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "io.grpc" % "grpc-netty" % "1.65.1",

      // Akka HTTP dependencies
      "com.typesafe.akka" %% "akka-http" % "10.5.3",
      "com.typesafe.akka" %% "akka-stream" % "2.8.6",
      "com.typesafe.akka" %% "akka-slf4j" % "2.8.6",

      // JSON parsing (Spray JSON)
      "io.spray" %% "spray-json" % "1.3.6",

      // Logging (optional)

      "ch.qos.logback" % "logback-classic" % "1.5.6",

      "io.grpc" % "grpc-stub" % "1.64.0",

      // Akka HTTP TestKit for testing HTTP routes
      "com.typesafe.akka" %% "akka-http-testkit" % "10.5.3" % Test,
      // ScalaTest for writing tests
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      // Akka Actor TestKit (for system testing)
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % "2.8.6" % Test,
      // ScalaMock (optional, for mocking services)
      "org.scalamock" %% "scalamock" % "6.0.0" % Test
    ),

    // Protobuf configuration
    Compile / PB.targets := Seq(
      scalapb.gen() -> (Compile / sourceManaged).value
    ),

    // Add proto source directory
    Compile / PB.protoSources += baseDirectory.value / "src/main/protobuf"
  )
