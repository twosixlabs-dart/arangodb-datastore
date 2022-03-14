package com.twosixlabs.dart.arangodb.exception

object ArangodbException {
    def apply( message : String, table : String, cause : Throwable ) : ArangodbException = {
        new ArangodbException( message, table, cause )
    }
}

class ArangodbException( val message : String, val table : String, val cause : Throwable ) extends Exception( message, cause )
