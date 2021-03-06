/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation;

import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;


public class ResourceTranslationNeutronNetImpl extends ResourceTranslationBase {

  @Override
  public void translate(TranslateTo translateTo) {
    NodeTemplate nodeTemplate = new NodeTemplate();
    nodeTemplate.setType(ToscaNodeType.NEUTRON_NET);
    nodeTemplate.setProperties(TranslatorHeatToToscaPropertyConverter
        .getToscaPropertiesSimpleConversion(translateTo.getServiceTemplate(),translateTo.
                getResourceId(),translateTo.getResource().getProperties(),
            nodeTemplate.getProperties(), translateTo.getHeatFileName(),
            translateTo.getHeatOrchestrationTemplate(), translateTo.getResource().getType(),
            nodeTemplate, translateTo.getContext()));

    HeatToToscaUtil.mapBoolean(nodeTemplate, HeatToToscaUtil
        .getToscaPropertyName(translateTo, HeatConstants.PORT_SECURITY_ENABLED_PROPERTY_NAME));
    HeatToToscaUtil.mapBoolean(nodeTemplate, HeatToToscaUtil
        .getToscaPropertyName(translateTo, HeatConstants.SHARED_PROPERTY_NAME));
    HeatToToscaUtil.mapBoolean(nodeTemplate, HeatToToscaUtil
        .getToscaPropertyName(translateTo, HeatConstants.ADMIN_STATE_UP_PROPERTY_NAME));

    DataModelUtil.addNodeTemplate(translateTo.getServiceTemplate(), translateTo.getTranslatedId(),
        nodeTemplate);
  }


}
