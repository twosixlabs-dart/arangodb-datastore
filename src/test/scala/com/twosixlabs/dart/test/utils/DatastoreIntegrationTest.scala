package com.twosixlabs.dart.test.utils

import com.softwaremill.diffx.scalatest.DiffMatcher
import com.twosixlabs.dart.arangodb.{Arango, ArangoConf}
import org.scalatest.BeforeAndAfterAll
import org.slf4j.{Logger, LoggerFactory}
import com.twosixlabs.dart.json.JsonFormat
import com.twosixlabs.dart.test.base.StandardTestBase3x
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContextExecutor

trait DatastoreIntegrationTest extends StandardTestBase3x with DiffMatcher with JsonFormat with BeforeAndAfterEach with BeforeAndAfterAll {

    implicit val ec : ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
    private val LOG : Logger = LoggerFactory.getLogger( getClass )
    private val arangoDBHost = Option( System.getenv( "ARANGODB_HOST" ) ).getOrElse( "localhost" )
    private val arangoDBPort = Option( System.getenv( "ARANGODB_PORT" ) ).getOrElse( "8529" ).toInt

    protected val arangoConf : ArangoConf = ArangoConf( host = arangoDBHost, port = arangoDBPort, database = "dart" )
    protected val arango = new Arango( arangoConf )
}
