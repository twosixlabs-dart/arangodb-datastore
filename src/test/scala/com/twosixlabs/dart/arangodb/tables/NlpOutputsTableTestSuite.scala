package com.twosixlabs.dart.arangodb.tables

import annotations.IntegrationTest
import com.arangodb.async.ArangoCollectionAsync
import com.twosixlabs.dart.arangodb.NlpOutput
import com.twosixlabs.dart.test.utils.DatastoreIntegrationTest

import scala.concurrent.Await
import scala.concurrent.duration.{ Duration, SECONDS }

@IntegrationTest
class NlpOutputsTableTestSuite extends DatastoreIntegrationTest {

    protected val COLLECTION_NAME : String = "nlp_outputs"
    protected val collection : ArangoCollectionAsync = arango.collection( COLLECTION_NAME )
    protected val table = new NlpOutputsTable( arango )


    override def beforeAll( ) : Unit = {
        collection.truncate().get()
    }

    override def afterEach( ) : Unit = {
        collection.truncate().get()
    }

    "Canonical CDR Document Table" should "save an retrieve NLP Processing output for a single document" in {
        val DOC_ID = "a"
        val processor = "test-processor"
        val output = "{}"

        val nlpIn = NlpOutput( DOC_ID, processor, output )
        val nlpOut = Await.result( table.update( nlpIn ), Duration( 2, SECONDS ) )


        val results = Await.result( table.findNlpOutputFor( DOC_ID, processor ), Duration( 2, SECONDS ) )
        results.isDefined shouldBe true
        results.get.docId shouldBe nlpOut.docId
        results.get.processor shouldBe nlpOut.processor
        results.get.content shouldBe nlpOut.content
        results.get.timestamp shouldBe nlpOut.timestamp
    }
}
//
//    "Canonical CDR Document Table" should "not find any output" in {
//        val docId = "a"
//        val processor = "test-processor"
//        val output = "{}"
//        val cassandra = new Cassandra( CassandraConf( keyspace = Some( "dart" ) ) )
//        val table = new NlpOutputsTable( cassandra )
//
//        val nlpIn = NlpOutput( docId, processor, output )
//        Await.result( table.update( nlpIn ), Duration( 2, SECONDS ) )
//
//
//        val results = Await.result( table.findNlpOutputFor( docId, "not-exists" ), Duration( 2, SECONDS ) )
//        results.isDefined shouldBe false
//    }
//
//    "Canonical CDR Document Table" should "get all the NLP outputs for a given processor" in {
//        val processor = "processor-1"
//        val output = "{}"
//        val cassandra = new Cassandra( CassandraConf( keyspace = Some( "dart" ) ) )
//        val table = new NlpOutputsTable( cassandra )
//
//        //@formatter:off
//        val nlpOne = NlpOutput( "ab", processor, output, DatesAndTimes.midnightOf(6,30,1988) )
//        val nlpTwo = NlpOutput( "cd", processor, output, DatesAndTimes.midnightOf(6,30,1988) )
//        val nlpThree = NlpOutput( "cd", "processor-2", output, DatesAndTimes.midnightOf(6,30,1988) ) // use another processor to ensure filtering works
//        Await.result(
//            Future sequence (
//                Seq( table.update( nlpOne ), table.update( nlpTwo ), table.update( nlpThree ) ) ),
//                Duration( 2, SECONDS )
//        )
//        //@formatter:on
//
//
//        val results = Await.result( table.allOutputsFor( processor ), Duration( 2, SECONDS ) )
//
//        results.size shouldBe 2
//    }

//}
