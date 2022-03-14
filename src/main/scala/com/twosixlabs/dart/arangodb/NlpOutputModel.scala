package com.twosixlabs.dart.arangodb

import com.arangodb.entity.DocumentField
import com.arangodb.entity.DocumentField.{Type => ArangoFieldType}
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.{JsonInclude, JsonProperty}
import com.twosixlabs.dart.utils.{DatesAndTimes => DAT}

import java.time.OffsetDateTime
import scala.beans.BeanProperty

@JsonInclude( Include.NON_EMPTY )
case class NlpOutputModel( @BeanProperty @JsonProperty( "doc_id" ) docId : String = null,
                      @BeanProperty @JsonProperty( "processor" ) processor : String,
                      @BeanProperty @JsonProperty( "content" ) content : String,
                      @BeanProperty @JsonProperty( "timestamp" ) timestamp : OffsetDateTime = DAT.timeStamp,
                      @BeanProperty @DocumentField( ArangoFieldType.KEY ) key : String )