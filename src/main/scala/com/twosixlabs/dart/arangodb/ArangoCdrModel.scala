package com.twosixlabs.dart.arangodb

import com.arangodb.entity.DocumentField
import com.arangodb.entity.DocumentField.{Type => ArangoFieldType}
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.{JsonFormat, JsonInclude, JsonProperty, JsonSubTypes, JsonTypeInfo, JsonTypeName}

import java.time.{LocalDate, OffsetDateTime}
import scala.beans.BeanProperty

@JsonInclude( Include.NON_EMPTY )
case class ArangoCdrDocument( @BeanProperty @JsonProperty( "capture_source" ) captureSource : String = null,
                              @BeanProperty @JsonProperty( "extracted_metadata" ) extractedMetadata : ArangoCdrMetadata = null,
                              @BeanProperty @JsonProperty( "content_type" ) contentType : String = null,
                              @BeanProperty @JsonProperty( "extracted_numeric" ) extractedNumeric : Map[ String, String ] = Map.empty,
                              @BeanProperty @JsonProperty( "document_id" ) documentId : String = null,
                              @BeanProperty @JsonProperty( "extracted_text" ) extractedText : String = null,
                              @BeanProperty @JsonProperty( "uri" ) uri : String = null,
                              @BeanProperty @JsonProperty( "source_uri" ) sourceUri : String = null,
                              @BeanProperty @JsonProperty( "extracted_ntriples" ) extractedNtriples : String = null,
                              @BeanProperty @JsonProperty( "timestamp" ) @JsonFormat( pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC" ) timestamp : OffsetDateTime,
                              @BeanProperty @JsonProperty( "annotations" ) @JsonInclude( Include.ALWAYS ) annotations : List[ ArangoCdrAnnotation[ Any ] ] = List.empty,
                              @BeanProperty @JsonProperty( "labels" ) labels : Set[ String ] = Set.empty,
                              @BeanProperty @DocumentField( ArangoFieldType.KEY ) key : String )

@JsonInclude( Include.NON_EMPTY )
case class ArangoCdrMetadata( @BeanProperty @JsonProperty( "CreationDate" ) @JsonFormat( pattern = "yyyy-MM-dd" ) creationDate : LocalDate = null,
                              @BeanProperty @JsonProperty( "ModDate" ) @JsonFormat( pattern = "yyyy-MM-dd" ) modificationDate : LocalDate = null,
                              @BeanProperty @JsonProperty( "Author" ) author : String = null,
                              @BeanProperty @JsonProperty( "Type" ) docType : String = null,
                              @BeanProperty @JsonProperty( "Description" ) description : String = null,
                              @BeanProperty @JsonProperty( "OriginalLanguage" ) originalLanguage : String = null,
                              @BeanProperty @JsonProperty( "Classification" ) classification : String = null,
                              @BeanProperty @JsonProperty( "Title" ) title : String = null,
                              @BeanProperty @JsonProperty( "Publisher" ) publisher : String = null,
                              @BeanProperty @JsonProperty( "URL" ) url : String = null,
                              @BeanProperty @JsonProperty( "Pages" ) pages : Integer = null,
                              @BeanProperty @JsonProperty( "Subject" ) subject : String = null,
                              @BeanProperty @JsonProperty( "Creator" ) creator : String = null,
                              @BeanProperty @JsonProperty( "Producer" ) producer : String = null,
                              @BeanProperty @JsonProperty( "StatedGenre" ) statedGenre : String = null,
                              @BeanProperty @JsonProperty( "PredictedGenre" ) predictedGenre : String = null )

//@formatter:off
@JsonTypeInfo (
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
    )
@JsonSubTypes( Array(
    new Type( value = classOf[ ArangoGenealogyAnnotation ], name = "genealogy" ),
    new Type( value = classOf[ ArangoTextAnnotation ], name = "text" ),
    new Type( value = classOf[ ArangoDictionaryAnnotation ], name = "dictionary" ),
    new Type( value = classOf[ ArangoTagsAnnotation ], name = "tags" ),
    new Type( value = classOf[ ArangoFacetsAnnotation ], name = "facets" ),
    new Type( value = classOf[ ArangoTranslationAnnotation ], name = "translation" )
    ) )
@JsonInclude( Include.NON_NULL )
abstract class ArangoCdrAnnotation[ +T ]( @BeanProperty @JsonProperty( "label" ) val label : String,
                                          @BeanProperty @JsonProperty( "version" ) val version : String,
                                          @BeanProperty @JsonProperty( "content" ) val content : T,
                                          @BeanProperty @JsonProperty( "class" ) val classification : String )

//@formatter:on

case class ArangoTextAnnotation( override val label : String,
                                 override val version : String,
                                 override val content : String,
                                 @BeanProperty @JsonProperty( "class" ) override val classification : String ) extends ArangoCdrAnnotation[ String ]( label, version, content, classification )

case class ArangoDictionaryAnnotation( override val label : String,
                                       override val version : String,
                                       override val content : Map[ String, String ],
                                       @BeanProperty @JsonProperty( "class" ) override val classification : String ) extends ArangoCdrAnnotation[ Map[ String, String ] ]( label, version, content, classification )

@JsonTypeName( "tags" )
case class ArangoTagsAnnotation( override val label : String,
                                 override val version : String,
                                 override val content : List[ ArangoOffsetTag ],
                                 @BeanProperty @JsonProperty( "class" ) override val classification : String ) extends ArangoCdrAnnotation[ List[ ArangoOffsetTag ] ]( label, version, content, classification )

@JsonTypeName( "facets" )
case class ArangoFacetsAnnotation( override val label : String,
                                   override val version : String,
                                   override val content : List[ ArangoFacetScore ],
                                   @BeanProperty @JsonProperty( "class" ) override val classification : String ) extends ArangoCdrAnnotation[ List[ ArangoFacetScore ] ]( label, version, content, classification )

@JsonTypeName( "genealogy" )
case class ArangoGenealogyAnnotation( override val label : String,
                                      override val version : String,
                                      override val content : ArangoDocGenealogy,
                                      @BeanProperty @JsonProperty( "class" ) override val classification : String ) extends ArangoCdrAnnotation[ ArangoDocGenealogy ]( label, version, content, classification )

@JsonTypeName( "translation" )
case class ArangoTranslationAnnotation( override val label : String,
                                        override val version : String,
                                        override val content : ArangoTranslatedFields,
                                        @BeanProperty @JsonProperty( "class" ) override val classification : String ) extends ArangoCdrAnnotation[ ArangoTranslatedFields ]( label, version, content, classification )

@JsonInclude( Include.NON_EMPTY )
case class ArangoOffsetTag( @BeanProperty @JsonProperty( "offset_start" ) offsetStart : Int,
                            @BeanProperty @JsonProperty( "offset_end" ) offsetEnd : Int,
                            @BeanProperty @JsonProperty( "value" ) value : String,
                            @BeanProperty @JsonProperty( "tag" ) tag : String,
                            @BeanProperty @JsonProperty( "score" ) score : BigDecimal = null )

@JsonInclude( Include.NON_EMPTY )
case class ArangoFacetScore( @BeanProperty @JsonProperty( "value" ) value : String,
                             @BeanProperty @JsonProperty( "score" ) score : BigDecimal = null )

@JsonInclude( Include.NON_EMPTY )
case class ArangoDocGenealogy( @BeanProperty @JsonProperty( "similar_documents" ) similarDocuments : Map[ String, BigDecimal ],
                               @BeanProperty @JsonProperty( "similarity_matrix" ) similarityMatrix : Array[ Array[ BigDecimal ] ] )

@JsonInclude( Include.NON_EMPTY )
case class ArangoTranslatedFields( @BeanProperty @JsonProperty( "language" ) language : String,
                                   @BeanProperty @JsonProperty( "fields" ) fields : Map[ String, String ] )
