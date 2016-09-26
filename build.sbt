name := "VRChallenge"

version := "0.1"

organization := "com.vr"

scalaVersion := "2.11.5"

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Spray Repository"    at "http://repo.spray.io")

libraryDependencies ++= {
  val akkaVersion       = "2.4.4"
  val sprayVersion      = "1.3.2"
  val json4sV           = "3.2.11"
  val scalaTestV = "2.2.4"
  Seq(
    "com.typesafe.akka"     %% "akka-actor"      % akkaVersion,
    "io.spray"              %% "spray-can"       % sprayVersion,
    "io.spray"              %% "spray-routing"   % sprayVersion,
    "io.spray"              %% "spray-json"      % "1.3.1",
    "org.scalatest"         %% "scalatest"       % scalaTestV   % "test",
    "org.json4s"            %% "json4s-jackson"  % json4sV,
    "org.json4s"            %% "json4s-ext"      % json4sV,
    "com.typesafe.akka"     %% "akka-slf4j"      % akkaVersion,
    "ch.qos.logback"        %  "logback-classic" % "1.1.2",
    "com.typesafe.akka"     %% "akka-testkit"    % akkaVersion  % "test",
    "io.spray"              %% "spray-testkit"   % sprayVersion % "test",
    "org.specs2"            %% "specs2"          % "2.3.13"     % "test"
  )
}

mainClass in Global := Some("com.vr.challenge.Boot")