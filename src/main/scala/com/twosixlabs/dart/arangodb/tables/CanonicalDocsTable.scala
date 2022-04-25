package com.twosixlabs.dart.arangodb.tables

import com.arangodb.async.ArangoCollectionAsync
import com.twosixlabs.cdr4s.core.{ CdrAnnotation, CdrDocument }
import com.twosixlabs.dart.arangodb.Serialization.ARANGO_CDR_FORMAT
import com.twosixlabs.dart.arangodb.exception.ArangodbException
import com.twosixlabs.dart.arangodb.{ Arango, ArangoCdrDocument }
import com.twosixlabs.dart.utils.AsyncDecorators._
import org.slf4j.{ Logger, LoggerFactory }

import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters._
import scala.concurrent.{ ExecutionContext, Future }
import scala.language.postfixOps
import scala.util.{ Failure, Success }

class CanonicalDocsTable( arango : Arango ) {

    private implicit val executionContext : ExecutionContext = ExecutionContext.global
    private val LOG : Logger = LoggerFactory.getLogger( getClass )
    private val COLLECTION_NAME : String = "canonical_docs"
    private val collection : ArangoCollectionAsync = arango.collection( COLLECTION_NAME )

    def exists( docId : String ) : Future[ Boolean ] = {
        collection.documentExists( docId ).toScala transform {
            case Success( result ) => Success( result.booleanValue() )
            case Failure( e ) => Failure( ArangodbException( s"${docId} - exists document check failed", COLLECTION_NAME, e ) )
        }
    }

    def upsertAnnotation( docId : String, annotation : CdrAnnotation[ _ ] ) : Future[ Unit ] = {
        getDocument( docId ) synchronously match {
            case Success( doc : Option[ CdrDocument ] ) => {
                doc match {
                    case None => Future( Failure( ArangodbException( s"unable to upsert annotation for ${docId}", COLLECTION_NAME, new Exception( "Document doesn't exists" ) ) ) )
                    case Some( document ) => {
                        val mergedAnnotations = document.annotations :+ annotation
                        val mergedDocument = document.copy( annotations = mergedAnnotations )
                        val arangoDocument = ARANGO_CDR_FORMAT.toArango( mergedDocument )
                        collection.updateDocument( docId, arangoDocument ).toScala transform {
                            case Success( _ ) => Success()
                            case Failure( e ) =>
                                LOG.error( s"${docId} - upsert annotation failed: ${e.getLocalizedMessage}" )
                                Failure( ArangodbException( s"${docId} - upsert annotation failed", COLLECTION_NAME, e ) )
                        }
                    }
                }
            }
            case Failure( e ) => Future.failed( e )
        }
    }

    def upsert( doc : CdrDocument ) : Future[ Unit ] = {
        exists( doc.documentId ) synchronously match { // we need to come back to this later and make it async, like a Future[Future[Unit] flatmapped to Future[Unit]
            case Success( docExists ) => {
                val arangoDoc = ARANGO_CDR_FORMAT.toArango( doc )
                if ( !docExists ) {
                    collection.insertDocument( arangoDoc ).toScala transform {
                        case Success( _ ) => Success()
                        case Failure( e ) =>
                            Failure( ArangodbException( s"${arangoDoc.documentId} - insert document failed", COLLECTION_NAME, e ) )
                    }
                }
                else {
                    collection.updateDocument( arangoDoc.documentId, arangoDoc ).toScala transform {
                        case Success( _ ) => Success()
                        case Failure( e ) => Failure( ArangodbException( s"${doc.documentId} - upsert document failed", COLLECTION_NAME, e ) )
                    } // do update
                }
            }
            case Failure( e ) => Future.failed( e )
        }
    }

    def getDocument( docId : String ) : Future[ Option[ CdrDocument ] ] = {
        collection.getDocument( docId, classOf[ ArangoCdrDocument ] ).toScala transform {
            case Success( null ) => Success( None )
            case Success( document : ArangoCdrDocument ) => Success( Some( ARANGO_CDR_FORMAT.fromArango( document ) ) )
            case Failure( e ) => Failure( ArangodbException( s"${docId} - get document failed", COLLECTION_NAME, e ) )
        }
    }

    def getAllDocuments( ) : Future[ Iterator[ CdrDocument ] ] = {
        collection.db().query( "FOR document IN canonical_docs RETURN document", classOf[ ArangoCdrDocument ] ).toScala transform {
            case Success( cursor ) => Success( cursor.iterator.asScala.map( document => ARANGO_CDR_FORMAT.fromArango( document ) ) )
            case Failure( e ) => Failure( ArangodbException( s"Unable to retrieve all documents", COLLECTION_NAME, e ) )
        }
    }

    def getAllDocIds( ) : Future[ List[ String ] ] = {
        collection.db().query( "FOR document IN canonical_docs RETURN document.document_id", classOf[ String ] ).toScala transform {
            case Success( cursor ) => Success( cursor.iterator().asScala.toList )
            case Failure( e ) => Failure( ArangodbException( s"Unable to retrieve all document ids", COLLECTION_NAME, e ) )
        }
    }

}
