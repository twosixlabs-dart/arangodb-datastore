package com.twosixlabs.dart.arangodb.tables

import annotations.IntegrationTest
import com.arangodb.async.ArangoCollectionAsync
import com.twosixlabs.cdr4s.annotations.{DocumentGenealogy, FacetScore, OffsetTag}
import com.twosixlabs.cdr4s.core.{CdrAnnotation, CdrDocument, DictionaryAnnotation, DocumentGenealogyAnnotation, FacetAnnotation, OffsetTagAnnotation, TextAnnotation}
import com.twosixlabs.cdr4s.test.base.TestCdrData.DOC_TEMPLATE
import com.twosixlabs.dart.arangodb.Arango
import com.twosixlabs.dart.test.utils.DatastoreIntegrationTest
import org.scalatest.Ignore

import java.util.UUID
import java.util.concurrent.TimeUnit.SECONDS
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

@IntegrationTest
@Ignore
class CanonicalDocsTableTestSuite extends DatastoreIntegrationTest {

    protected val COLLECTION_NAME : String = "canonical_docs"
    protected val collection : ArangoCollectionAsync = arango.collection( COLLECTION_NAME )
    protected val table = new CanonicalDocsTable( arango )

    override def beforeEach( ) : Unit = {
        collection.truncate().get()
    }

    override def afterEach( ) : Unit = {
        collection.truncate().get()
    }

    "Canonical CDR Document Table" should "save and retrieve a document with all Document and Metadata fields" in {
        val docId = UUID.randomUUID().toString
        val doc = DOC_TEMPLATE.copy( documentId = docId, timestamp = null )

        Await.result( table.upsert( doc ), Duration( 2, SECONDS ) )

        val results = Await.result( table.getDocument( docId ), Duration( 2, SECONDS ) )
        results.isDefined shouldBe true
        results.get shouldBe doc
    }

    "Canonical CDR Document Table" should "return an empty result for a document that does not exist" in {
        val docId = "notexist"
        val arango = new Arango( arangoConf )
        val table = new CanonicalDocsTable( arango )

        val results = Await.result( table.getDocument( docId ), Duration( 2, SECONDS ) )
        results.isDefined shouldBe false
    }

    "Canonical CDR Document Table" should "save and retrieve a Document with empty fields" in {
        val docId = UUID.randomUUID().toString
        val doc = DOC_TEMPLATE.copy( documentId = docId, captureSource = null, extractedText = null )

        Await.result( table.upsert( doc ), Duration( 2, SECONDS ) )

        val results = Await.result( table.getDocument( docId ), Duration( 2, SECONDS ) )
        results.isDefined shouldBe true
        results.get shouldBe doc
    }

    "Canonical CDR Document Table" should "save and retrieve a document with missing pages field" in {
        val docId = UUID.randomUUID().toString
        val doc = DOC_TEMPLATE.copy( documentId = docId,
                                     extractedMetadata = DOC_TEMPLATE.extractedMetadata.copy( pages = None ) )

        Await.result( table.upsert( doc ), Duration( 2, SECONDS ) )

        val results = Await.result( table.getDocument( docId ), Duration( 2, SECONDS ) )
        results.isDefined shouldBe true
        results.get shouldBe doc
    }

    "Canonical CDR Document Table" should "save and retrieve a Document with missing Metadata fields" in {
        val docId = UUID.randomUUID().toString

        val doc = DOC_TEMPLATE.copy( documentId = docId, extractedMetadata = DOC_TEMPLATE.extractedMetadata.copy( creationDate = null, publisher = null ) )

        Await.result( table.upsert( doc ), Duration( 2, SECONDS ) )

        val results = Await.result( table.getDocument( docId ), Duration( 2, SECONDS ) )
        results.isDefined shouldBe true
        results.get shouldBe doc
    }

    "Canonical CDR Document Table" should "save and retrieve a document with missing timestamp" in {
        val docId = UUID.randomUUID().toString
        val doc = DOC_TEMPLATE.copy( documentId = docId, timestamp = null )

        Await.result( table.upsert( doc ), Duration( 2, SECONDS ) )

        val results = Await.result( table.getDocument( docId ), Duration( 2, SECONDS ) )
        results.isDefined shouldBe true
        results.get shouldBe doc
    }

    "Canonical CDR Document Table" should "save and retrieve a document with empty labels" in {
        val docId = UUID.randomUUID().toString
        val doc = DOC_TEMPLATE.copy( documentId = docId, labels = Set() )

        Await.result( table.upsert( doc ), Duration( 2, SECONDS ) )

        val results = Await.result( table.getDocument( docId ), Duration( 2, SECONDS ) )
        results.isDefined shouldBe true
        results.get shouldBe doc
    }

    "Canonical CDR Document Table" should "save and retrieve a document with annotations" in {
        val docId = UUID.randomUUID().toString

        val allAnnotationTypes : List[ CdrAnnotation[ _ ] ] = {

            val genealogy : CdrAnnotation[ _ ] = {
                val similarDocuments : Map[ String, BigDecimal ] = Map( "abc" -> BigDecimal( 0.5 ), "def" -> BigDecimal( 0.2 ) )
                val similarityMatrix : Array[ Array[ BigDecimal ] ] = Array( Array( BigDecimal( 0.1 ), BigDecimal( 0.2 ), BigDecimal( 0.3 ) ),
                                                                             Array( BigDecimal( 0.4 ), BigDecimal( 0.5 ), BigDecimal( 0.6 ) ),
                                                                             Array( BigDecimal( 0.7 ), BigDecimal( 0.8 ), BigDecimal( 0.9 ) ) )
                DocumentGenealogy( similarDocuments, similarityMatrix )
                DocumentGenealogyAnnotation( "genealogy", "1", DocumentGenealogy( similarDocuments, similarityMatrix ) )
            }

            val textAnnotation : CdrAnnotation[ _ ] = TextAnnotation( "text", "1.0", "text" )
            val dict : CdrAnnotation[ _ ] = DictionaryAnnotation( "dict", "1.0", Map( "test" -> "1" ) )
            val tag : CdrAnnotation[ _ ] = OffsetTagAnnotation( "tags", "1.0", List( OffsetTag( 0, 1, None, "N", None ), OffsetTag( 2, 3, None, "V", None ) ) )
            val categories : CdrAnnotation[ _ ] = FacetAnnotation( "facets", "1", List( FacetScore( "x", None ), FacetScore( "y", Some( 2.0 ) ) ) )

            List( textAnnotation, dict, tag, genealogy, categories )
        }

        val doc = DOC_TEMPLATE.copy( documentId = docId, annotations = allAnnotationTypes )

        Await.result( table.upsert( doc ), Duration( 2, SECONDS ) )

        val results = Await.result( table.getDocument( docId ), Duration( 2, SECONDS ) )
        results.isDefined shouldBe true
        results.get shouldBe doc
    }

    "Canonical CDR Document Table" should "add an annotation to an existing document with no annotations" in {
        val docId = UUID.randomUUID().toString

        val doc = DOC_TEMPLATE.copy( documentId = docId, annotations = List() )

        Await.result( table.upsert( doc ), Duration( 2, SECONDS ) )


        val tag : CdrAnnotation[ _ ] = OffsetTagAnnotation( "tags", "1.0", List( OffsetTag( 0, 1, None, "N", None ), OffsetTag( 2, 3, None, "V", None ) ) )

        Await.result( table.upsertAnnotation( docId, tag ), Duration( 2, SECONDS ) )

        val results = Await.result( table.getDocument( docId ), Duration( 2, SECONDS ) )
        results.isDefined shouldBe true
        results.get.annotations.size shouldBe 1
        results.get.annotations.map( _.label ).toSet shouldBe Set( "tags" )
    }

    "Canonical CDR Document Table" should "add an annotation to an existing document with existing annotations" in {
        val docId = UUID.randomUUID().toString

        val existingAnnotations : List[ CdrAnnotation[ _ ] ] = {
            val tag : CdrAnnotation[ _ ] = OffsetTagAnnotation( "tags", "1.0", List( OffsetTag( 0, 1, None, "N", None ), OffsetTag( 2, 3, None, "V", None ) ) )
            val categories : CdrAnnotation[ _ ] = FacetAnnotation( "facets", "1", List( FacetScore( "x", None ), FacetScore( "y", Some( 2.0 ) ) ) )

            List( tag, categories )
        }

        val doc = DOC_TEMPLATE.copy( documentId = docId, annotations = existingAnnotations )

        Await.result( table.upsert( doc ), Duration( 2, SECONDS ) )

        val existing = Await.result( table.getDocument( docId ), Duration( 2, SECONDS ) )
        existing.isDefined shouldBe true
        existing.get.annotations.size shouldBe 2

        val newAnnotation = OffsetTagAnnotation( "tags-2", "1.0", List( OffsetTag( 0, 1, None, "N", None ), OffsetTag( 2, 3, None, "V", None ) ) )
        Await.result( table.upsertAnnotation( docId, newAnnotation ), Duration( 2, SECONDS ) )

        val updated = Await.result( table.getDocument( docId ), Duration( 2, SECONDS ) )
        updated.isDefined shouldBe true
        updated.get.annotations.size shouldBe 3
        updated.get.annotations.map( _.label ).toSet shouldBe Set( "tags", "tags-2", "facets" )
    }


    "Canonical CDR Document Table" should "return true if a document exists" in {
        val docId = UUID.randomUUID().toString

        val doc = DOC_TEMPLATE.copy( documentId = docId )

        Await.result( table.upsert( doc ), Duration( 2, SECONDS ) )

        val result = Await.result( table.exists( docId ), Duration( 2, SECONDS ) )

        result shouldBe true
    }

    "Canonical CDR Document Table" should "return false if a document does not exist" in {
        val docId = UUID.randomUUID().toString

        val result = Await.result( table.exists( docId ), Duration( 2, SECONDS ) )

        result shouldBe false
    }

    "Canonical CDR Document Table" should "get all existing documents" in {
        val docs : Seq[ CdrDocument ] = Seq( DOC_TEMPLATE.copy( documentId = "1a" ), DOC_TEMPLATE.copy( documentId = "2a" ), DOC_TEMPLATE.copy( documentId = "3a" ) )

        Await.result( Future.sequence( docs.map( doc => table.upsert( doc ) ) ), Duration( 2, SECONDS ) )

        val results = Await.result( table.getAllDocuments(), Duration( 2, SECONDS ) ).toList

        results.size shouldBe docs.size
        results.toSet shouldBe docs.toSet
    }

    "Canonical CDR Document Table" should "return an empty collection if there are no documents" in {

        val results = Await.result( table.getAllDocuments(), Duration( 2, SECONDS ) )

        results.isEmpty shouldBe true
    }
}
