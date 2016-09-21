name := """activator-akka-spray"""

version := "1.0"

scalaVersion := "2.11.6"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "spray nightlies" at "http://nightlies.spray.io"

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  "spray repo"                       at "http://repo.spray.io",
  "Typesafe repository snapshots"    at "http://repo.typesafe.com/typesafe/snapshots/",
  "Typesafe repository releases"     at "http://repo.typesafe.com/typesafe/releases/",
  "Eclipse Repository"               at "https://repo.eclipse.org/content/repositories/paho-releases/",
  "Java.net Maven2 Repository"       at "http://download.java.net/maven/2/",
  "Twitter Repository"               at "http://maven.twttr.com",
  "Kamon Releases"                   at "http://repo.kamon.io",
  "Maven Central"                    at "http://central.maven.org/maven2"
)

val sprayV        = "1.3.2"
val akkaV         = "2.4.10"
val logbackV      = "1.1.7"
val json4sV       = "3.2.11"
val junitV        = "0.11"
val scalaTestV    = "2.2.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka"  %% "akka-actor"                % akkaV,
  "com.typesafe.akka"  %% "akka-slf4j"                % akkaV,
  "ch.qos.logback"      % "logback-classic"           % logbackV,
  "io.spray"           %% "spray-can"                 % sprayV,
  "io.spray"           %% "spray-routing-shapeless2"  % sprayV,
  "io.spray"           %% "spray-json"                % sprayV,
  "org.json4s"         %%  "json4s-jackson"           % json4sV,
  "org.json4s"         %%  "json4s-ext"               % json4sV,
  "org.scalatest"      %%  "scalatest"                % scalaTestV      % "test",
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

//testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")

fork in run := true