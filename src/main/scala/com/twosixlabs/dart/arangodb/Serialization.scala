package com.twosixlabs.dart.arangodb

import com.twosixlabs.cdr4s.core.CdrFormat
import com.twosixlabs.cdr4s.json.dart.DartJsonFormat

object Serialization {

    val CDR_JSON_FORMAT : CdrFormat = new DartJsonFormat

    val ARANGO_CDR_FORMAT : ArangoCdrFormat = new ArangoCdrFormat
    val ARANGO_NLP_OUTPUT_FORMAT : ArangoNlpOutputFormat = new ArangoNlpOutputFormat

}
