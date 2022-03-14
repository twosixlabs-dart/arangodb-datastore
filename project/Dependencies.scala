import sbt._

object Dependencies {

    val slf4jVersion = "1.7.20"
    val logbackVersion = "1.2.9"
    val scalaTestVersion = "3.0.5"
    val betterFilesVersion = "3.8.0"
    val java8CompatVersion = "1.0.0"

    val cdr4sVersion = "3.0.270"
    val dartCommonsVersion = "3.0.307"

    val arangodbVersion = "6.12.3"
    val arangoJacksonVersion = "2.0.0"

    val diffxVersion = "0.3.30"

    val arangodb = Seq( "com.arangodb" % "arangodb-java-driver" % arangodbVersion,
                        "com.arangodb" % "jackson-dataformat-velocypack" % arangoJacksonVersion,
                        "com.arangodb" %% "velocypack-module-scala" % "1.2.1" )

    val java8Compat = Seq( "org.scala-lang.modules" %% "scala-java8-compat" % java8CompatVersion )

    val cdr4s = Seq( "com.twosixlabs.cdr4s" %% "cdr4s-core" % cdr4sVersion,
                     "com.twosixlabs.cdr4s" %% "cdr4s-dart-json" % cdr4sVersion,
                     "com.twosixlabs.cdr4s" %% "cdr4s-test-base" % cdr4sVersion % Test )

    val dartCommons = Seq( "com.twosixlabs.dart" %% "dart-utils" % dartCommonsVersion,
                           "com.twosixlabs.dart" %% "dart-json" % dartCommonsVersion,
                           "com.twosixlabs.dart" %% "dart-test-base" % dartCommonsVersion % Test )

    val betterFiles = Seq( "com.github.pathikrit" %% "better-files" % betterFilesVersion % Test )

    val logging = Seq( "org.slf4j" % "slf4j-api" % slf4jVersion,
                       "ch.qos.logback" % "logback-classic" % logbackVersion )

    val diffx = Seq( "com.softwaremill.diffx" %% "diffx-core" % diffxVersion % Test,
                     "com.softwaremill.diffx" %% "diffx-scalatest" % diffxVersion % Test )

}
