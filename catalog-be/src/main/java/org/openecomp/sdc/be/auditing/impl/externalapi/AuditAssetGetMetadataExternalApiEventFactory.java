package org.openecomp.sdc.be.auditing.impl.externalapi;

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData;

public class AuditAssetGetMetadataExternalApiEventFactory extends AuditAssetExternalApiEventFactory {

    public AuditAssetGetMetadataExternalApiEventFactory(CommonAuditData commonFields, String resourceType, String resourceName,
                                                        String consumerId, String resourceUrl, ResourceAuditData prevParams, ResourceAuditData currParams,
                                                        String invariantUuid, User modifier, String artifactData) {
        super(AuditingActionEnum.GET_ASSET_METADATA, commonFields, resourceType, resourceName, consumerId, resourceUrl, prevParams, currParams,
                invariantUuid, modifier, artifactData);
    }
}
