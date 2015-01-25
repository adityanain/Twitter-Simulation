name := "tweetR"

version := "1.0"

scalaVersion := "2.11.4"

resolvers += "spray repo" at "http://repo.spray.io"

unmanagedBase := baseDirectory.value / "lib"

libraryDependencies ++= {
  val akkaV = "2.3.7"
  val sprayV = "1.3.2"
  Seq(
    "com.typesafe.akka"   %% "akka-actor"                 % akkaV,
    "com.typesafe.akka"   %% "akka-http-experimental"     % "0.7",
    "io.spray"            %%  "spray-routing"             % sprayV,
    "io.spray"            %%  "spray-client"              % sprayV,
    "io.spray"            %%  "spray-can"                 % sprayV,
    "io.spray" %%  "spray-json" % "1.3.1",
    "io.spray"            %%  "spray-testkit"             % sprayV          % "test",
    "org.json4s"          %% "json4s-native"              % "3.2.11",
    "org.json4s"          %% "json4s-jackson"             % "3.2.11",
    "org.json4s" %% "json4s-jackson" % "3.2.11"
  )
}