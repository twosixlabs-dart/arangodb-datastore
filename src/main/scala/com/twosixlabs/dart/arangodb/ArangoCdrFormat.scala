package com.twosixlabs.dart.arangodb

import com.twosixlabs.cdr4s.annotations.{DocumentGenealogy, FacetScore, OffsetTag, TranslatedFields}
import com.twosixlabs.cdr4s.core.{CdrAnnotation, CdrDocument, CdrMetadata, DictionaryAnnotation, DocumentGenealogyAnnotation, FacetAnnotation, OffsetTagAnnotation, TextAnnotation, TranslationAnnotation}
import com.twosixlabs.dart.json.JsonFormat


class ArangoCdrFormat extends JsonFormat {

    def toArango( doc : CdrDocument ) : ArangoCdrDocument = {
        ArangoCdrDocument( captureSource = doc.captureSource,
                           extractedMetadata = metadataToDto( doc.extractedMetadata ),
                           contentType = doc.contentType,
                           extractedNumeric = doc.extractedNumeric,
                           documentId = doc.documentId,
                           extractedText = doc.extractedText,
                           uri = doc.uri,
                           sourceUri = doc.sourceUri,
                           extractedNtriples = doc.extractedNtriples,
                           timestamp = doc.timestamp,
                           annotations = convertList[ CdrAnnotation[ Any ], ArangoCdrAnnotation[ Any ] ]( doc.annotations, annotationToDto ),
                           labels = doc.labels,
                           key = doc.documentId )
    }

    def toArango( tenantId : String, docId : String ) : TenantDocModel = {
        TenantDocModel( tenantId, docId )
    }

    def fromArango( dto : ArangoCdrDocument ) : CdrDocument = {
        CdrDocument( dto.captureSource,
                     dtoToMetadata( dto.extractedMetadata ),
                     dto.contentType,
                     if ( dto.extractedNumeric == null ) Map.empty else dto.extractedNumeric,
                     dto.documentId,
                     dto.extractedText,
                     dto.uri,
                     dto.sourceUri,
                     dto.extractedNtriples,
                     dto.timestamp,
                     convertList[ ArangoCdrAnnotation[ Any ], CdrAnnotation[ Any ] ]( dto.annotations, dtoToAnnotation ),
                     if ( dto.labels == null ) Set.empty else dto.labels )

    }

    private def metadataToDto( metadata : CdrMetadata ) : ArangoCdrMetadata = {
        ArangoCdrMetadata( creationDate = metadata.creationDate,
                           modificationDate = metadata.modificationDate,
                           author = metadata.author,
                           docType = metadata.docType,
                           description = metadata.description,
                           originalLanguage = metadata.originalLanguage,
                           classification = metadata.classification,
                           title = metadata.title,
                           publisher = metadata.publisher,
                           url = metadata.url,
                           pages = {
                               if ( metadata.pages.isDefined ) metadata.pages.get
                               else null
                           },
                           subject = metadata.subject,
                           creator = metadata.creator,
                           producer = metadata.producer,
                           statedGenre = metadata.statedGenre,
                           predictedGenre = metadata.predictedGenre )
    }

    private def dtoToMetadata( dto : ArangoCdrMetadata ) : CdrMetadata = {
        CdrMetadata( creationDate = dto.creationDate,
                     modificationDate = dto.modificationDate,
                     author = dto.author,
                     docType = dto.docType,
                     description = dto.description,
                     originalLanguage = dto.originalLanguage,
                     classification = dto.classification,
                     title = dto.title,
                     publisher = dto.publisher,
                     url = dto.url,
                     pages = Option( dto.pages ),
                     subject = dto.subject,
                     creator = dto.creator,
                     producer = dto.producer,
                     statedGenre = dto.statedGenre,
                     predictedGenre = dto.predictedGenre )
    }

    private def dtoToOffsetTag( tag : ArangoOffsetTag ) : OffsetTag = OffsetTag( tag.offsetStart, tag.offsetEnd, Option( tag.value ), tag.tag, Option( tag.score ) )

    private def offsetTagToDto( tag : OffsetTag ) : ArangoOffsetTag = ArangoOffsetTag( tag.offsetStart, tag.offsetEnd, tag.value.orNull, tag.tag,
                                                                                       if ( tag.score.isEmpty ) null else tag.score.get )

    private def dtoToKeyword( key : ArangoFacetScore ) : FacetScore = FacetScore( key.value, Option( key.score ) )

    private def keywordToDto( key : FacetScore ) : ArangoFacetScore = ArangoFacetScore( key.value,
                                                                                        if ( key.score.isEmpty ) null else key.score.get )

    private def dtoToDocumentGenealogy( from : ArangoDocGenealogy ) : DocumentGenealogy = {
        DocumentGenealogy( from.similarDocuments, from.similarityMatrix )
    }

    private def documentGenealogyToDto( from : DocumentGenealogy ) : ArangoDocGenealogy = {

        ArangoDocGenealogy( from.similarDocuments, from.similarityMatrix )
    }

    private def dtoToTranslatedFields( from : ArangoTranslatedFields ) : TranslatedFields = {
        TranslatedFields( from.language, from.fields )
    }

    private def translatedFieldsToDto( from : TranslatedFields ) : ArangoTranslatedFields = {
        ArangoTranslatedFields( from.language, from.fields )
    }

    private def dtoToAnnotation( annotation : ArangoCdrAnnotation[ Any ] ) : CdrAnnotation[ Any ] = {
        annotation match {
            case text : ArangoTextAnnotation => {
                TextAnnotation( text.label, text.version, text.content, text.classification ).asInstanceOf[ CdrAnnotation[ Any ] ]
            }
            case dict : ArangoDictionaryAnnotation => {
                DictionaryAnnotation( dict.label, dict.version, dict.content, dict.classification ).asInstanceOf[ CdrAnnotation[ Any ] ]
            }
            case tags : ArangoTagsAnnotation => {
                OffsetTagAnnotation( tags.label, tags.version, convertList( tags.content, dtoToOffsetTag ), tags.classification ).asInstanceOf[ CdrAnnotation[ Any ] ]
            }
            case facets : ArangoFacetsAnnotation => {
                FacetAnnotation( facets.label, facets.version, convertList( facets.content, dtoToKeyword ), facets.classification ).asInstanceOf[ CdrAnnotation[ Any ] ]
            }
            case genealogy : ArangoGenealogyAnnotation => {
                DocumentGenealogyAnnotation( genealogy.label, genealogy.version, dtoToDocumentGenealogy( genealogy.content ), genealogy.classification ).asInstanceOf[ CdrAnnotation[ Any ] ]
            }
            case translation : ArangoTranslationAnnotation => {
                TranslationAnnotation( translation.label, translation.version, dtoToTranslatedFields( translation.content ) )
            }

        }

    }

    def annotationToDto( annotation : CdrAnnotation[ Any ] ) : ArangoCdrAnnotation[ Any ] = {
        annotation match {
            case text : TextAnnotation => {
                ArangoTextAnnotation( text.label, text.version, text.content, text.classification ).asInstanceOf[ ArangoCdrAnnotation[ Any ] ]
            }
            case dict : DictionaryAnnotation => {
                ArangoDictionaryAnnotation( dict.label, dict.version, dict.content, dict.classification ).asInstanceOf[ ArangoCdrAnnotation[ Any ] ]
            }
            case tags : OffsetTagAnnotation => {
                ArangoTagsAnnotation( tags.label, tags.version, convertList( tags.content, offsetTagToDto ), tags.classification ).asInstanceOf[ ArangoCdrAnnotation[ Any ] ]
            }
            case facets : FacetAnnotation => {
                ArangoFacetsAnnotation( facets.label, facets.version, convertList( facets.content, keywordToDto ), facets.classification ).asInstanceOf[ ArangoCdrAnnotation[ Any ] ]
            }
            case genealogy : DocumentGenealogyAnnotation => {
                ArangoGenealogyAnnotation( genealogy.label, genealogy.version, documentGenealogyToDto( genealogy.content ), genealogy.classification )
            }
            case translation : TranslationAnnotation => {
                ArangoTranslationAnnotation( translation.label, translation.version, translatedFieldsToDto( translation.content ), translation.classification )
            }
        }
    }
}

