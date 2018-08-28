name := "mvp"
version := "0.1"
scalaVersion := "2.12.6"
organization := "org.encryfoundation"

val akkaVersion = "2.5.13"
val akkaHttpVersion = "10.0.9"

val testingDependencies = Seq(
  "com.typesafe.akka" %% "akka-testkit" % "2.4.+" % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",
  "org.scalatest" %% "scalatest" % "3.0.3" % "test",
  "org.scalacheck" %% "scalacheck" % "1.13.+" % "test"
)

val loggingDependencies = Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.+",
  "ch.qos.logback" % "logback-classic" % "1.+",
  "ch.qos.logback" % "logback-core" % "1.+"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "org.bouncycastle" % "bcprov-jdk15on" % "1.58",
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
  "org.encry" %% "encry-common" % "0.1.2",
  "org.iq80.leveldb" % "leveldb" % "0.7",
  "javax.xml.bind" % "jaxb-api" % "2.+",
  "org.scorexfoundation" %% "scrypto" % "2.1.1",
  "com.iheart" %% "ficus" % "1.4.3",
  "org.influxdb" % "influxdb-java" % "2.10",
  "org.iq80.leveldb" % "leveldb" % "0.7"
) ++ loggingDependencies ++ testingDependencies

resolvers ++= Seq("Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "SonaType" at "https://oss.sonatype.org/content/groups/public",
  "Typesafe maven releases" at "http://repo.typesafe.com/typesafe/maven-releases/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/")

assemblyJarName in assembly := "mvp.jar"

mainClass in assembly := Some("mvp.MVP")

test in assembly := {}

assemblyMergeStrategy in assembly := {
  case "logback.xml" => MergeStrategy.first
  case "module-info.class" => MergeStrategy.discard
  case "META-INF/MANIFEST.MF" => MergeStrategy.discard
  case "META-INF/*.DSA" => MergeStrategy.discard
  case "META-INF/*.RSA" => MergeStrategy.discard
  case "META-INF/*.SF" => MergeStrategy.discard
  case "META-INF/BC1024KE.SF" => MergeStrategy.discard
  case "META-INF/BC2048KE.SF" => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case _ => MergeStrategy.first
}