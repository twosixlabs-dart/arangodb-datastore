package com.twosixlabs.dart.arangodb

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.{JsonInclude, JsonProperty}

import scala.beans.BeanProperty

@JsonInclude( Include.NON_EMPTY )
case class TenantDocModel( @BeanProperty @JsonProperty( "tenant_id" ) tenantId : String = null,
                           @BeanProperty @JsonProperty( "doc_id" ) docId : String = null )
