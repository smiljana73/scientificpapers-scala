
name := """Test"""
organization := "ftn"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "com.sksamuel.elastic4s" %% "elastic4s-http" % "6.5.0",
  "com.sksamuel.elastic4s" %% "elastic4s-testkit" % "6.5.0" % Test,
  "com.sksamuel.elastic4s" %% "elastic4s-jackson" % "6.5.0" % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.5.17" % Test,
  "com.typesafe.play" %% "play-json-joda" % "2.6.10",
  guice,
  jodaForms,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.16.0-play26",
  "org.reactivemongo" %% "reactivemongo-akkastream" % "0.16.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
  "org.scalaz" %% "scalaz-core" % "7.2.17",
  "org.sangria-graphql" %% "sangria" % "1.4.1",
  "org.sangria-graphql" %% "sangria-play-json" % "1.0.4",
  ws,
  filters,
  "com.pauldijou" %% "jwt-play-json" % "4.2.0",
  "org.scalacheck" %% "scalacheck" % "1.13.4" % Test,
  "com.typesafe.play" %% "play-mailer" % "6.0.1",
  "com.typesafe.play" %% "play-mailer-guice" % "6.0.1",
  "de.mkammerer" % "argon2-jvm" % "2.6"
)


/*libraryDependencies += ws
libraryDependencies += "com.sksamuel.elastic4s" %% "elastic4s-http" % "6.5.0"
libraryDependencies += "com.sksamuel.elastic4s" %% "elastic4s-testkit" % "6.5.0" % Test
libraryDependencies += "com.sksamuel.elastic4s" %% "elastic4s-jackson" % "6.5.0" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.17" % Test
libraryDependencies += guice
libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % "0.16.0-play26"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "org.sangria-graphql" %% "sangria" % "1.4.1"
libraryDependencies += "org.sangria-graphql" %% "sangria-play-json" % "1.0.4"
libraryDependencies += "com.pauldijou" %% "jwt-play" % "3.0.1"
libraryDependencies += "com.pauldijou" %% "jwt-core" % "3.0.1"*/
/*libraryDependencies += "org.apache.pdfbox" % "pdfbox" % "2.0.17"*/


/*dependencyOverrides ++= Seq(
/*  "com.fasterxml.jackson.core" % "jackson-core" % "2.9.9",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.8",
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.9.8",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.6",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % "2.9.6",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % "2.9.8",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.9.8",
  "com.fasterxml.jackson.module" % "jackson-module-parameter-names" % "2.9.6"*/
)*/

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "ftn.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "ftn.binders._"
