package com.twosixlabs.dart.arangodb.tables

import com.arangodb.async.ArangoCollectionAsync
import com.twosixlabs.dart.arangodb.exception.ArangodbException
import com.twosixlabs.dart.arangodb.{Arango, NlpOutput, NlpOutputModel}
import com.twosixlabs.dart.utils.AsyncDecorators.DecoratedFuture

import scala.collection.JavaConverters.{asScalaBufferConverter, asScalaIteratorConverter}
import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class NlpOutputsTable( arango : Arango ) {
    private implicit val executionContext : ExecutionContext = ExecutionContext.global

    private val COLLECTION_NAME : String = "nlp_outputs"
    private val collection : ArangoCollectionAsync = arango.collection( COLLECTION_NAME )

    private def getNlpOutput( nlpOutput : NlpOutput ) : Future[ Option[ NlpOutputModel ] ] = {
        val bindVars : java.util.Map[ String, AnyRef ] = new java.util.HashMap[ String, Object ]()
        bindVars.put( "doc_id", nlpOutput.docId )
        bindVars.put( "processor", nlpOutput.processor )
        bindVars.put( "content", nlpOutput.content )
        val query = s"FOR nlp_output in ${COLLECTION_NAME} FILTER nlp_output.doc_id == @doc_id && nlp_output.processor == @processor && nlp_output.content == @content RETURN nlp_output"

        collection.db().query( query, bindVars, classOf[ NlpOutputModel ] ).toScala transform {
            case Success( cursor ) => {
                val result = cursor.asListRemaining().asScala.toList
                if ( result.size > 1 ) {
                    Failure( ArangodbException( s"doc id: ${nlpOutput.docId} - processor: ${nlpOutput.processor} - content: ${nlpOutput.content} - multiple documents found", COLLECTION_NAME,
                                                new IllegalStateException( "Multiple tenant documents found)" ) ) )
                } else {
                    Success( result.headOption )
                }
            }
            case Failure( e ) => Failure( ArangodbException( s"doc id: ${nlpOutput.docId} - processor: ${nlpOutput.processor} - content: ${nlpOutput.content} - unable to get Nlp Output", COLLECTION_NAME, e ) )
        }
    }

    def update( nlpOutput : NlpOutput ) : Future[ NlpOutput ] = {
        getNlpOutput( nlpOutput ) synchronously match { // we need to come back to this later and make it async, like a Future[Future[Unit] flatmapped to Future[Unit]
            case Success( nlpOutputModel : Option[ NlpOutputModel ] ) => {
                if ( nlpOutputModel.isEmpty ) {
                    collection.insertDocument( nlpOutput ).toScala transform {
                        case Success( _ ) => Success( nlpOutput )
                        case Failure( e ) =>
                            Failure( ArangodbException( s"${nlpOutput.docId} - insert failed", COLLECTION_NAME, e ) )
                    }
                } else {
                    val _nlpOutputModel = nlpOutputModel.get
                    collection.updateDocument( _nlpOutputModel.key, _nlpOutputModel ).toScala transform {
                        case Success( _ ) => Success( nlpOutput )
                        case Failure( e ) =>
                            Failure( ArangodbException( s"${nlpOutput.docId} - update failed", COLLECTION_NAME, e ) )
                    }
                }
            }
            case Failure( e ) => Future.failed( e )
        }
    }

    def findNlpOutputFor( docId : String, processor : String ) : Future[ Option[ NlpOutput ] ] = {
        val bindVars : java.util.Map[ String, AnyRef ] = new java.util.HashMap[ String, Object ]()
        bindVars.put( "doc_id", docId )
        bindVars.put( "processor", processor )
        val query = s"FOR nlp_output in ${COLLECTION_NAME} FILTER nlp_output.doc_id == @doc_id && nlp_output.processor == @processor RETURN nlp_output"

        collection.db().query( query, bindVars, classOf[ NlpOutput ] ).toScala transform {
            case Success( result ) => Success( result.asListRemaining().asScala.headOption )
            case Failure( e ) => Failure( ArangodbException( s"Unable to find Nlp Output for doc id: ${docId} - processor: ${processor}", COLLECTION_NAME, e ) )
        }
    }

    def allOutputsFor( processor : String ) : Future[ Iterator[ NlpOutput ] ] = {
        val bindVars : java.util.Map[ String, AnyRef ] = new java.util.HashMap[ String, Object ]()
        bindVars.put( "processor", processor )
        val query = s"FOR nlp_output in ${COLLECTION_NAME} FILTER nlp_output.processor == @processor RETURN nlp_output"

        collection.db().query( query, bindVars, classOf[ NlpOutput ] ).toScala transform {
            case Success( result ) => Success( result.iterator().asScala )
            case Failure( e ) => Failure( ArangodbException( s"Unable to find nlp Output for processor: ${processor}", COLLECTION_NAME, e ) )
        }
    }


}
