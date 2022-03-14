package com.twosixlabs.dart.arangodb

import com.twosixlabs.dart.json.JsonFormat

import java.time.OffsetDateTime
import com.twosixlabs.dart.utils.{DatesAndTimes => DAT}

case class NlpOutput( docId : String,
                      processor : String,
                      content : String,
                      timestamp : OffsetDateTime = DAT.timeStamp )

class ArangoNlpOutputFormat extends JsonFormat {

    def fromArango( nlpOutputModel : NlpOutputModel ) : NlpOutput = {
        NlpOutput( nlpOutputModel.docId, nlpOutputModel.processor, nlpOutputModel.content, nlpOutputModel.timestamp )
    }

//    def toArango( docId : String, processor : String, content : String, timestamp : OffsetDateTime ) : TenantDocModel = {
//        NlpOutputModel( docId, processor, content, timestamp)
//    }
}

