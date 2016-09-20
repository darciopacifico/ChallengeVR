name := """activator-akka-spray"""

version := "1.0"

scalaVersion := "2.11.6"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "spray nightlies" at "http://nightlies.spray.io"

val akkaV         = "2.3.11"
val logbackV      = "1.1.7"
val sprayV        = "1.3.2"
val junitV        = "0.11"

libraryDependencies ++= Seq(
  "com.typesafe.akka"  %% "akka-actor"                % akkaV,
  "com.typesafe.akka"  %% "akka-slf4j"                % akkaV,
  "ch.qos.logback"      % "logback-classic"           % logbackV,
  "io.spray"           %% "spray-can"                 % sprayV,
  "io.spray"           %% "spray-routing-shapeless2"  % sprayV,
  "io.spray"           %% "spray-json"                % sprayV,
  "org.specs2"         %% "specs2"                    % akkaV           % "test",
  "io.spray"           %% "spray-testkit"             % sprayV          % "test",
  "com.typesafe.akka"  %% "akka-testkit"              % akkaV           % "test",
  "com.novocode"        % "junit-interface"           % junitV          % "test->default"
)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")

fork in run := true