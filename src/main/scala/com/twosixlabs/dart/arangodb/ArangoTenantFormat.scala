package com.twosixlabs.dart.arangodb

import com.twosixlabs.dart.json.JsonFormat


class ArangoTenantFormat extends JsonFormat {

    def toArango( tenantId : String, docId : String ) : TenantDocModel = {
        TenantDocModel( tenantId, docId )
    }
}

