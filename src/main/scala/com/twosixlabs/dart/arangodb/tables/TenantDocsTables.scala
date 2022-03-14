package com.twosixlabs.dart.arangodb.tables

import com.arangodb.async.ArangoCollectionAsync
import com.twosixlabs.dart.arangodb.Serialization.ARANGO_CDR_FORMAT
import com.twosixlabs.dart.arangodb.exception.ArangodbException
import com.twosixlabs.dart.arangodb.tables.TenantDocsTables.tenantExistsDocIdValue
import com.twosixlabs.dart.arangodb.{Arango, TenantDocModel}
import com.twosixlabs.dart.utils.AsyncDecorators.DecoratedFuture

import java.util
import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


case class TenantDocument( tenantId : String, docId : String )

class TenantDocsTables( arango : Arango ) {

    val COLLECTION_NAME : String = "tenant_docs"
    val collection : ArangoCollectionAsync = arango.collection( COLLECTION_NAME )

    val TENANT_EXISTS_VALUE : String = "__EXISTS"

    private implicit val executionContext : ExecutionContext = scala.concurrent.ExecutionContext.global

    def exists( tenantDoc : TenantDocModel ) : Future[ Boolean ] = {
        val bindVars : java.util.Map[ String, AnyRef ] = new java.util.HashMap[ String, Object ]()
        bindVars.put( "tenant_id", tenantDoc.tenantId )
        bindVars.put( "doc_id", tenantDoc.docId )
        val query = s"FOR tenant_doc in ${COLLECTION_NAME} FILTER tenant_doc.tenant_id == @tenant_id && tenant_doc.doc_id == @doc_id RETURN tenant_doc"

        collection.db().query( query, bindVars, classOf[ TenantDocModel ] ).toScala transform {
            case Success( cursor ) => {
                val documentCount = cursor.asListRemaining().asScala.toList.size
                if ( documentCount > 1 ) {
                    Failure( ArangodbException( s"tenant Id: ${tenantDoc.tenantId} - Doc Id: ${tenantDoc.docId} - multiple documents found", COLLECTION_NAME,
                                                new IllegalStateException( "Multiple tenant documents found)" ) ) )
                } else {
                    Success( ( documentCount == 1 ) )
                }
            }
            case Failure( e ) => Failure( e )
        }
    }

    def getTenants : Future[ Iterator[ String ] ] = {
        val query = s"FOR tenant_docs IN ${COLLECTION_NAME} FILTER tenant_docs.doc_id == @doc_id RETURN tenant_docs"
        val bindVars : java.util.Map[ String, AnyRef ] = new java.util.HashMap[ String, Object ]()
        bindVars.put( "doc_id", tenantExistsDocIdValue )

        collection.db().query( query, bindVars, classOf[ TenantDocModel ] ).toScala transform {
            case Success( cursor ) => Success( cursor.iterator().asScala.map( document => document.getTenantId ) )
            case Failure( e ) => Failure( ArangodbException( s"Unable to all documents", COLLECTION_NAME, e ) )
        }
    }

    def getTenant( tenantId : String ) : Future[ Option[ String ] ] = {
        val query = s"FOR tenant_doc IN ${COLLECTION_NAME} FILTER tenant_doc.tenant_id == @tenant_id && tenant_doc.doc_id == @tenantExistsDocIdValue return tenant_doc"
        val bindVars : java.util.Map[ String, AnyRef ] = new java.util.HashMap[ String, Object ]()

        bindVars.put( "tenant_id", tenantId )
        bindVars.put( "tenantExistsDocIdValue", tenantExistsDocIdValue )

        collection.db().query( query, bindVars, classOf[ TenantDocModel ] ).toScala transform {
            case Success( cursor ) => {
                val tenantDocs = cursor.asListRemaining.asScala.toList
                if ( tenantDocs.isEmpty ) {
                    Success( None )
                } else if ( tenantDocs.size > 1 ) {
                    Failure( ArangodbException( s"tenant Id: ${tenantId} - multiple tenants found", COLLECTION_NAME,
                                                new IllegalStateException( "Multiple tenant documents found)" ) ) )
                } else {
                    Success( Some( tenantDocs.head.tenantId ) )
                }
            }
            case Failure( e ) => Failure( ArangodbException( s"Unable to get tenant", COLLECTION_NAME, e ) )
        }
    }

    def addTenant( tenantId : String ) : Future[ Unit ] = {

        if ( tenantId.equals( tenantExistsDocIdValue ) )
            return Future.failed( ArangodbException( s"${tenantId} - is invalid", COLLECTION_NAME, new IllegalStateException( s"${tenantId} - is invalid" ) ) )

        val tenantDoc = ARANGO_CDR_FORMAT.toArango( tenantId, tenantExistsDocIdValue )
        exists( tenantDoc ) synchronously match { // we need to come back to this later and make it async, like a Future[Future[Unit] flatmapped to Future[Unit]
            case Success( docExists ) => {
                if ( !docExists ) {
                    collection.insertDocument( tenantDoc ).toScala transform {
                        case Success( _ ) => Success()
                        case Failure( e ) =>
                            Failure( ArangodbException( s"${tenantId} - insert document failed", COLLECTION_NAME, e ) )
                    }
                }
                else {
                    Future.failed( ArangodbException( s"${tenantId} - does not exist", COLLECTION_NAME, new IllegalStateException( s"${tenantId} - does not exist" ) ) )
                }
            }
            case Failure( e ) => Future.failed( e )
        }
    }

    def removeTenant( tenantId : String ) : Future[ Unit ] = {
        val tenantDoc = ARANGO_CDR_FORMAT.toArango( tenantId, tenantExistsDocIdValue )
        exists( tenantDoc ) synchronously match {
            case Success( docExists ) => {
                if ( docExists ) {
                    val query = s"FOR tenant_doc IN ${COLLECTION_NAME} FILTER tenant_doc.tenant_id == @tenant_id REMOVE {_key: tenant_doc._key} IN ${COLLECTION_NAME}"
                    val bindVars : java.util.Map[ String, AnyRef ] = new java.util.HashMap[ String, Object ]()

                    bindVars.put( "tenant_id", tenantId )
                    collection.db().query( query, bindVars, classOf[ TenantDocModel ] ).toScala transform {
                        case Success( _ ) => Success()
                        case Failure( e ) => Failure( ArangodbException( s"tenant Id: ${tenantId} - delete failed", COLLECTION_NAME, e ) )
                    }
                } else {
                    Future.failed( ArangodbException( s"tenant Id: ${tenantId} - does not exist", COLLECTION_NAME, new IllegalStateException( "Tenant does not exist" ) ) )
                }
            }
            case Failure( e ) => Future.failed( e )
        }

    }

    def tenantDocExists( tenantId : String, docId : String ) : Future[ Boolean ] = {
        val tenantDoc = ARANGO_CDR_FORMAT.toArango( tenantId, docId )
        exists( tenantDoc )
    }

    def getDocsByTenant( tenantId : String ) : Future[ Iterator[ String ] ] = {
        val query = s"FOR tenant_doc IN ${COLLECTION_NAME} FILTER tenant_doc.tenant_id == @tenant_id && tenant_doc.doc_id != @tenantExistsDocIdValue return tenant_doc"
        val bindVars : java.util.Map[ String, AnyRef ] = new util.HashMap[ String, Object ]()
        bindVars.put( "tenant_id", tenantId )
        bindVars.put( "tenantExistsDocIdValue", tenantExistsDocIdValue )

        collection.db().query( query, bindVars, classOf[ TenantDocModel ] ).toScala transform {
            case Success( cursor ) => Success( cursor.iterator().asScala.map( document => document.getDocId ) )
            case Failure( e ) => Failure( ArangodbException( s"tenant Id: ${tenantId} failed to retrieve documents", COLLECTION_NAME, e ) )
        }
    }

    def getTenantsByDoc( docId : String ) : Future[ Iterator[ String ] ] = {
        val query = s"FOR tenant_doc IN ${COLLECTION_NAME} FILTER tenant_doc.doc_id == @doc_id return tenant_doc"
        val bindVars : java.util.Map[ String, AnyRef ] = new util.HashMap[ String, Object ]()
        bindVars.put( "doc_id", docId )

        collection.db().query( query, bindVars, classOf[ TenantDocModel ] ).toScala transform {
            case Success( cursor ) => Success( cursor.iterator().asScala.map( document => document.getTenantId ) )
            case Failure( e ) => Failure( ArangodbException( s"tenant Id: ${docId} failed to retrieve tenants", COLLECTION_NAME, e ) )
        }
    }

    def addDocToTenant( tenantId : String, docId : String ) : Future[ Unit ] = {
        val tenantDoc = ARANGO_CDR_FORMAT.toArango( tenantId, docId )
        exists( tenantDoc ) synchronously match { // we need to come back to this later and make it async, like a Future[Future[Unit] flatmapped to Future[Unit]
            case Success( docExists ) => {
                if ( !docExists ) {
                    collection.insertDocument( tenantDoc ).toScala transform {
                        case Success( _ ) => Success()
                        case Failure( e ) =>
                            Failure( ArangodbException( s"${tenantId} - insert document failed", COLLECTION_NAME, e ) )
                    }
                }
                else {
                    Future.failed( ArangodbException( s"${tenantId} - Tenant already exists", COLLECTION_NAME, new IllegalStateException( "Tenant already exists" ) ) )
                }
            }
            case Failure( e ) => Future.failed( e )
        }
    }

    def removeDocFromTenant( tenantId : String, docId : String ) : Future[ Unit ] = {
        val tenantDoc = ARANGO_CDR_FORMAT.toArango( tenantId, docId )
        exists( tenantDoc ) synchronously match {
            case Success( docExists ) => {
                if ( docExists ) {
                    val query = s"FOR tenant_doc IN ${COLLECTION_NAME} FILTER tenant_doc.tenant_id == @tenant_id && tenant_doc.doc_id == @doc_id REMOVE {_key: tenant_doc._key} IN ${COLLECTION_NAME}"
                    val bindVars : java.util.Map[ String, AnyRef ] = new java.util.HashMap[ String, Object ]()

                    bindVars.put( "tenant_id", tenantId )
                    bindVars.put( "doc_id", docId )

                    collection.db().query( query, bindVars, classOf[ TenantDocModel ] ).toScala transform {
                        case Success( _ ) => Success()
                        case Failure( e ) => Failure( ArangodbException( s"tenant Id: ${tenantId} - unable to remove doc from tenant", COLLECTION_NAME, e ) )
                    }
                } else {
                    Future.failed( ArangodbException( s"${docId} does not exists in ${tenantId}", COLLECTION_NAME, new IllegalStateException( s"${docId} does not exists in ${tenantId}" ) ) )
                }
            }
            case Failure( e ) => Future.failed( e )
        }
    }

    def removeTenantFromAllDocs( tenantId : String ) : Future[ Unit ] = {
        val query = s"FOR tenant_doc IN ${COLLECTION_NAME} FILTER tenant_doc.tenant_id == @tenant_id && tenant_doc.doc_id != @tenantExistsDocIdValue REMOVE {_key: tenant_doc._key} IN ${COLLECTION_NAME}"
        val bindVars : java.util.Map[ String, AnyRef ] = new java.util.HashMap[ String, Object ]()

        bindVars.put( "tenant_id", tenantId )
        bindVars.put( "tenantExistsDocIdValue", tenantExistsDocIdValue )

        collection.db().query( query, bindVars, classOf[ TenantDocModel ] ).toScala transform {
            case Success( _ ) => Success()
            case Failure( e ) => Failure( ArangodbException( s"tenant Id: ${tenantId} - unable to remove tenant from all docs", COLLECTION_NAME, e ) )
        }
    }

    def removeDocFromAllTenants( docId : String ) : Future[ Unit ] = {
        val query = s"FOR tenant_doc IN ${COLLECTION_NAME} FILTER tenant_doc.doc_id == @doc_id REMOVE {_key: tenant_doc._key} IN ${COLLECTION_NAME}"
        val bindVars : java.util.Map[ String, AnyRef ] = new java.util.HashMap[ String, Object ]()

        bindVars.put( "doc_id", docId )

        collection.db().query( query, bindVars, classOf[ TenantDocModel ] ).toScala transform {
            case Success( _ ) => Success()
            case Failure( e ) => Failure( ArangodbException( s"doc Id: ${docId} - unable to remove doc from all tenants", COLLECTION_NAME, e ) )
        }
    }
}

object TenantDocsTables {
    val TENANT_DOCS_TABLE : String = "tenant_docs"

    val tenantIdField : String = "tenant_id"
    val docIdField : String = "doc_id"

    val tenantExistsDocIdValue : String = "__EXISTS"
}