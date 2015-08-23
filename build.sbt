name := "MyEventSourcedAkkaCQRS"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= {

  val akkaV = "2.4-SNAPSHOT"

  Seq(
    "com.typesafe.akka"          %% "akka-http-experimental"              % "1.0",
    "com.typesafe.akka"          %% "akka-actor"                          % akkaV,
    "com.typesafe.akka"          %% "akka-persistence-experimental"       % akkaV,
    "com.typesafe.akka"          %% "akka-persistence-query-experimental" % akkaV,
    "com.typesafe.akka"          %% "akka-slf4j"                          % akkaV        exclude ("com.typesafe.akka","akka-actor"),
    "joda-time"                  %  "joda-time"                           % "2.8.1",
    "org.joda"                   %  "joda-convert"                        % "1.2",
    "com.github.nscala-time"     %% "nscala-time"                         % "2.0.0",
    "commons-validator"          %  "commons-validator"                   % "1.4.1",
    "ch.qos.logback"             %  "logback-classic"                     % "1.1.3",
    "com.typesafe"               %  "config"                              % "1.3.0",
    "org.iq80.leveldb"           %  "leveldb"                             % "0.7",
    "org.fusesource.leveldbjni"  %  "leveldbjni-all"                      % "1.8",
    "de.heikoseeberger"          %% "akka-http-json4s"                    % "1.0.0",
    "org.json4s"                 %% "json4s-jackson"                      % "3.3.0.RC3",
    "org.json4s"                 %% "json4s-ext"                          % "3.2.11",
    "org.scalacheck"             %% "scalacheck"                          % "1.11.0"     % "test",
    "org.scalatest"              %% "scalatest"                           % "2.2.4"      % "test",
    "org.specs2"                 %% "specs2-core"                         % "2.3.11"     % "test",
    "com.typesafe.akka"          %% "akka-testkit"                        % akkaV        % "test"
  )
}

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

resolvers += "hseeberger at bintray" at "http://dl.bintray.com/hseeberger/maven"
