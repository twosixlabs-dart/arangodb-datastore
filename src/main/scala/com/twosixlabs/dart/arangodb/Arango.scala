package com.twosixlabs.dart.arangodb

import com.arangodb.async.{ArangoCollectionAsync, ArangoDBAsync, ArangoDatabaseAsync}
import com.arangodb.entity.CollectionEntity
import com.arangodb.mapping.ArangoJack
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import java.util.concurrent.CompletableFuture

case class ArangoConf( host : String, port : Int, database : String, connectionPoolSize : Int = 25 )

object Arango {
    def formatKey( input : String ) : String = {
        input.replaceAll( "[^A-Za-z0-9 ]", "" ).replace( " ", "_" )
    }
}

class Arango( conf : ArangoConf ) {

    private val arangodb : ArangoDBAsync = {
        val jackson : ArangoJack = {
            val bigDecimalConverterModule = new SimpleModule
            bigDecimalConverterModule.addSerializer( classOf[ BigDecimal ], new ToStringSerializer )
            val serializer = new ArangoJack()
            serializer.configure( mapper => {
                mapper.registerModule( DefaultScalaModule )
                mapper.registerModule( new JavaTimeModule )
                mapper.registerModule( bigDecimalConverterModule )
            } )
            serializer
        }

        new ArangoDBAsync.Builder()
          .host( conf.host, conf.port )
          .maxConnections( conf.connectionPoolSize )
          .serializer( jackson )
          .build()
    }

    private val database : ArangoDatabaseAsync = arangodb.db( conf.database )

    def collection( name : String ) : ArangoCollectionAsync = {
        database.collection( name )
    }

    def createCollection( name : String ) : CompletableFuture[ CollectionEntity ] = {
        database.createCollection( name )
    }

}
