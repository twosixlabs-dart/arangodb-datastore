package com.twosixlabs.dart.arangodb.tables

import annotations.IntegrationTest
import com.arangodb.async.ArangoCollectionAsync
import com.twosixlabs.dart.test.utils.DatastoreIntegrationTest
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

import scala.concurrent.Await
import scala.util.{Failure, Try}

@IntegrationTest
class TenantDocsTablesTest extends DatastoreIntegrationTest {

    private val COLLECTION_NAME : String = "tenant_docs"
    protected val table = new TenantDocsTables( arango )
    protected val collection : ArangoCollectionAsync = arango.collection( COLLECTION_NAME )

    override def beforeAll( ) : Unit = {
        collection.truncate().get()
    }

    override def afterEach( ) : Unit = {
        collection.truncate().get()
    }

    behavior of "TenantsDocsTables"

    it should "add and retrieve tenants" in {
        Await.result( table.getTenants, 5.seconds ) should have length ( 0 )
        Await.result( table.addTenant( "test-tenant-1" ), 5.seconds )
        Await.result( table.getTenants, 5.seconds ).toList shouldBe List( "test-tenant-1" )
        Await.result( table.addTenant( "test-tenant-2" ), 5.seconds )
        Await.result( table.addTenant( "test-tenant-3" ), 5.seconds )
        Await.result( table.addTenant( "test-tenant-4" ), 5.seconds )
        Await.result( table.getTenants, 5.seconds ).toSet shouldBe Set( "test-tenant-1", "test-tenant-2", "test-tenant-3", "test-tenant-4" )
    }

    it should "add and retrieve tenants information only" in {
        Await.result( table.getTenants, 5.seconds ) should have length ( 0 )
        Await.result( table.addTenant( "test-tenant-1" ), 5.seconds )
        Await.result( table.addTenant( "test-tenant-2" ), 5.seconds )
        Await.result( table.addTenant( "test-tenant-3" ), 5.seconds )
        Await.result( table.addTenant( "test-tenant-4" ), 5.seconds )
        Await.result( table.addDocToTenant( "test-tenant-1", "d41d8cd98f00b204e9800998ecf8427e" ), 5.seconds )
        Await.result( table.addDocToTenant( "test-tenant-2", "d41d8cd98f00b204e9800998ecf8427e" ), 5.seconds )
        Await.result( table.getTenants, 5.seconds ).toSet shouldBe Set( "test-tenant-1", "test-tenant-2", "test-tenant-3", "test-tenant-4" )
    }

    it should "verify existence of specific tenant" in {

        Await.result( table.getTenant( "test-tenant-1" ), 5.seconds ) shouldBe None
        Await.result( table.addTenant( "test-tenant-1" ), 5.seconds )
        Await.result( table.addDocToTenant( "test-tenant-1", "d41d8cd98f00b204e9800998ecf8427e" ), 5.seconds )
        Await.result( table.getTenant( "test-tenant-1" ), 5.seconds ) shouldBe Some( "test-tenant-1" )
        Await.result( table.getTenant( "test-tenant-2" ), 5.seconds ) shouldBe None
        Await.result( table.getTenant( "test-tenant-3" ), 5.seconds ) shouldBe None
        Await.result( table.getTenant( "test-tenant-4" ), 5.seconds ) shouldBe None
        Await.result( table.addTenant( "test-tenant-2" ), 5.seconds )
        Await.result( table.addTenant( "test-tenant-3" ), 5.seconds )
        Await.result( table.addTenant( "test-tenant-4" ), 5.seconds )
        Await.result( table.getTenant( "test-tenant-1" ), 5.seconds ) shouldBe Some( "test-tenant-1" )
        Await.result( table.getTenant( "test-tenant-2" ), 5.seconds ) shouldBe Some( "test-tenant-2" )
        Await.result( table.getTenant( "test-tenant-3" ), 5.seconds ) shouldBe Some( "test-tenant-3" )
        Await.result( table.getTenant( "test-tenant-4" ), 5.seconds ) shouldBe Some( "test-tenant-4" )
    }

    it should "remove only specified tenants" in {
        Await.result( table.addTenant( "test-tenant-0" ), 5.seconds )
        Await.result( table.addTenant( "test-tenant-1" ), 5.seconds )
        Await.result( table.getTenant( "test-tenant-1" ), 5.seconds ) shouldBe Some( "test-tenant-1" )
        Await.result( table.removeTenant( "test-tenant-1" ), 5.seconds )
        Await.result( table.getTenant( "test-tenant-1" ), 5.seconds ) shouldBe None
        Await.result( table.addTenant( "test-tenant-2" ), 5.seconds )
        Await.result( table.addTenant( "test-tenant-3" ), 5.seconds )
        Await.result( table.addTenant( "test-tenant-4" ), 5.seconds )
        Await.result( table.getTenant( "test-tenant-2" ), 5.seconds ) shouldBe Some( "test-tenant-2" )
        Await.result( table.getTenant( "test-tenant-3" ), 5.seconds ) shouldBe Some( "test-tenant-3" )
        Await.result( table.getTenant( "test-tenant-4" ), 5.seconds ) shouldBe Some( "test-tenant-4" )
        Await.result( table.removeTenant( "test-tenant-2" ), 5.seconds )
        Await.result( table.removeTenant( "test-tenant-3" ), 5.seconds )
        Await.result( table.removeTenant( "test-tenant-4" ), 5.seconds )
        Await.result( table.getTenant( "test-tenant-2" ), 5.seconds ) shouldBe None
        Await.result( table.getTenant( "test-tenant-3" ), 5.seconds ) shouldBe None
        Await.result( table.getTenant( "test-tenant-4" ), 5.seconds ) shouldBe None
        Await.result( table.getTenant( "test-tenant-0" ), 5.seconds ) shouldBe Some( "test-tenant-0" )

    }

    it should "add documents to tenant and retrieve documents from tenant" in {
        Await.result( table.getDocsByTenant( "test-tenant-1" ), 5.seconds ).toSeq should have length ( 0 )
        Await.result( table.addTenant( "test-tenant-1" ), 5.seconds )
        Await.result( table.getDocsByTenant( "test-tenant-1" ), 5.seconds ).toSeq should have length ( 0 )
        Await.result( table.addDocToTenant( "test-tenant-1", "test-doc-1" ), 5.seconds )
        Await.result( table.getDocsByTenant( "test-tenant-1" ), 5.seconds ).toList shouldBe List( "test-doc-1" )
        Await.result( table.addDocToTenant( "test-tenant-1", "test-doc-2" ), 5.seconds )
        Await.result( table.addDocToTenant( "test-tenant-1", "test-doc-3" ), 5.seconds )
        Await.result( table.addDocToTenant( "test-tenant-1", "test-doc-4" ), 5.seconds )
        Await.result( table.getDocsByTenant( "test-tenant-1" ), 5.seconds ).toSet shouldBe Set( "test-doc-1", "test-doc-2", "test-doc-3", "test-doc-4" )
    }
    //
    it should "verify that specific documents are in tenant" in {
        Await.result( table.tenantDocExists( "test-tenant-1", "test-doc-1" ), 5.seconds ) shouldBe false
        Await.result( table.addTenant( "test-tenant-1" ), 5.seconds )
        Await.result( table.tenantDocExists( "test-tenant-1", "test-doc-1" ), 5.seconds ) shouldBe false
        Await.result( table.addDocToTenant( "test-tenant-1", "test-doc-1" ), 5.seconds )
        Await.result( table.tenantDocExists( "test-tenant-1", "test-doc-1" ), 5.seconds ) shouldBe true
        Await.result( table.tenantDocExists( "test-tenant-1", "test-doc-2" ), 5.seconds ) shouldBe false
        Await.result( table.tenantDocExists( "test-tenant-1", "test-doc-3" ), 5.seconds ) shouldBe false
        Await.result( table.tenantDocExists( "test-tenant-1", "test-doc-4" ), 5.seconds ) shouldBe false
        Await.result( table.addDocToTenant( "test-tenant-1", "test-doc-2" ), 5.seconds )
        Await.result( table.addDocToTenant( "test-tenant-1", "test-doc-3" ), 5.seconds )
        Await.result( table.addDocToTenant( "test-tenant-1", "test-doc-4" ), 5.seconds )
        Await.result( table.tenantDocExists( "test-tenant-1", "test-doc-1" ), 5.seconds ) shouldBe true
        Await.result( table.tenantDocExists( "test-tenant-1", "test-doc-2" ), 5.seconds ) shouldBe true
        Await.result( table.tenantDocExists( "test-tenant-1", "test-doc-3" ), 5.seconds ) shouldBe true
        Await.result( table.tenantDocExists( "test-tenant-1", "test-doc-4" ), 5.seconds ) shouldBe true
    }

    it should "remove documents from tenant" in {
        Await.result( table.addTenant( "test-tenant-1" ), 5.seconds )
        Await.result( table.tenantDocExists( "test-tenant-1", "test-doc-1" ), 5.seconds ) shouldBe false
        Await.result( table.addDocToTenant( "test-tenant-1", "test-doc-1" ), 5.seconds )
        Await.result( table.tenantDocExists( "test-tenant-1", "test-doc-1" ), 5.seconds ) shouldBe true
        Await.result( table.removeDocFromTenant( "test-tenant-1", "test-doc-1" ), 5.seconds )
        Await.result( table.tenantDocExists( "test-tenant-1", "test-doc-1" ), 5.seconds ) shouldBe false
        Await.result( table.addDocToTenant( "test-tenant-1", "test-doc-2" ), 5.seconds )
        Await.result( table.addDocToTenant( "test-tenant-1", "test-doc-3" ), 5.seconds )
        Await.result( table.addDocToTenant( "test-tenant-1", "test-doc-4" ), 5.seconds )
        Await.result( table.tenantDocExists( "test-tenant-1", "test-doc-2" ), 5.seconds ) shouldBe true
        Await.result( table.tenantDocExists( "test-tenant-1", "test-doc-3" ), 5.seconds ) shouldBe true
        Await.result( table.tenantDocExists( "test-tenant-1", "test-doc-4" ), 5.seconds ) shouldBe true
        Await.result( table.removeDocFromTenant( "test-tenant-1", "test-doc-2" ), 5.seconds )
        Await.result( table.removeDocFromTenant( "test-tenant-1", "test-doc-3" ), 5.seconds )
        Await.result( table.removeDocFromTenant( "test-tenant-1", "test-doc-4" ), 5.seconds )
        Await.result( table.tenantDocExists( "test-tenant-1", "test-doc-1" ), 5.seconds ) shouldBe false
        Await.result( table.tenantDocExists( "test-tenant-1", "test-doc-2" ), 5.seconds ) shouldBe false
        Await.result( table.tenantDocExists( "test-tenant-1", "test-doc-3" ), 5.seconds ) shouldBe false
        Await.result( table.tenantDocExists( "test-tenant-1", "test-doc-4" ), 5.seconds ) shouldBe false
    }

    it should "add docs to multiple tenants and retrieve tenants from specific docs" in {
        Await.result( table.addTenant( "test-tenant-1" ), 5.seconds )
        Await.result( table.addTenant( "test-tenant-2" ), 5.seconds )
        Await.result( table.addTenant( "test-tenant-3" ), 5.seconds )
        Await.result( table.addDocToTenant( "test-tenant-1", "test-doc-1" ), 5.seconds )
        Await.result( table.addDocToTenant( "test-tenant-1", "test-doc-2" ), 5.seconds )
        Await.result( table.addDocToTenant( "test-tenant-1", "test-doc-3" ), 5.seconds )
        Await.result( table.addDocToTenant( "test-tenant-1", "test-doc-4" ), 5.seconds )
        Await.result( table.addDocToTenant( "test-tenant-2", "test-doc-1" ), 5.seconds )
        Await.result( table.addDocToTenant( "test-tenant-2", "test-doc-2" ), 5.seconds )
        Await.result( table.addDocToTenant( "test-tenant-2", "test-doc-3" ), 5.seconds )
        Await.result( table.addDocToTenant( "test-tenant-3", "test-doc-1" ), 5.seconds )
        Await.result( table.getTenantsByDoc( "test-doc-1" ), 5.seconds ).toSet shouldBe Set( "test-tenant-1", "test-tenant-2", "test-tenant-3" )
        Await.result( table.getTenantsByDoc( "test-doc-2" ), 5.seconds ).toSet shouldBe Set( "test-tenant-1", "test-tenant-2" )
        Await.result( table.getTenantsByDoc( "test-doc-3" ), 5.seconds ).toSet shouldBe Set( "test-tenant-1", "test-tenant-2" )
        Await.result( table.getTenantsByDoc( "test-doc-4" ), 5.seconds ).toSet shouldBe Set( "test-tenant-1" )
    }

    it should "return an error when trying to remove non existent tenant" in {
        Await.result( table.addTenant( "test-tenant-1" ), 5.seconds )
        Await.result( table.addTenant( "test-tenant-2" ), 5.seconds )
        Try( Await.result( table.removeTenant( "non-existent-fake-tenant" ), 5.seconds ) ) match {
            case Failure( e ) => e.getMessage should include( "does not exist" )
        }
    }

    it should "return an error when trying to remove non existent document from tenant" in {
        Await.result( table.addTenant( "test-tenant-1" ), 5.seconds )
        Await.result( table.addDocToTenant( "test-tenant-1", "test-doc-1" ), 5.seconds )
        Try( Await.result( table.removeDocFromTenant( "test-tenant-1", "test-doc-2" ), 5.seconds ) ) match {
            case Failure( e ) => e.getMessage should include( "test-doc-2 does not exists in test-tenant-1" )
        }
    }

    it should "return error when creating tenant with __EXISTS tenant id" in {
        Try( Await.result( table.addTenant( "__EXISTS" ), 5.seconds ) ) match {
            case Failure( e ) => e.getMessage should include( "__EXISTS - is invalid" )
        }
    }
}
