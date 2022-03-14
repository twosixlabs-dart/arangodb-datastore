import Dependencies._
import sbt._

organization in ThisBuild := "com.twosixlabs.dart"
name := "dart-arangodb-datastore"

scalaVersion in ThisBuild := "2.12.7"

resolvers in ThisBuild ++= Seq( "Spray IO Repository" at "https://repo.spray.io/",
                                "Maven Central" at "https://repo1.maven.org/maven2/",
                                "JCenter" at "https://jcenter.bintray.com",
                                "Local Ivy Repository" at s"file://${System.getProperty( "user.home" )}/.ivy2/local/default" )

publishMavenStyle := true
test in publish := {}

lazy val root = ( project in file( "." ) ).settings( libraryDependencies ++= arangodb
                                                                             ++ cdr4s
                                                                             ++ dartCommons
                                                                             ++ betterFiles
                                                                             ++ java8Compat
                                                                             ++ logging
                                                                             ++ diffx,
                                                     dependencyOverrides ++= Seq( "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.10.5",
                                                                                  "com.arangodb" %% "velocypack-module-scala" % "1.2.0",
                                                                                  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.10.5",
                                                                                  "com.fasterxml.jackson.core" % "jackson-databind" % "2.10.5" ),
                                                     excludeDependencies ++= Seq( ExclusionRule( "org.slf4j", "slf4j-log4j12" ),
                                                                                  ExclusionRule( "org.slf4j", "log4j-over-slf4j" ),
                                                                                  ExclusionRule( "log4j", "log4j" ),
                                                                                  ExclusionRule( "org.apache.logging.log4j", "log4j-core" ) ) )

parallelExecution in Test := false

javacOptions in ThisBuild ++= Seq( "-source", "8", "-target", "8" )
scalacOptions in ThisBuild += "-target:jvm-1.8"
