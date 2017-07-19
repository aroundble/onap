package org.openecomp.sdc.asdctool.impl.validator.tasks.moduleJson;

import fj.data.Either;
import org.openecomp.sdc.asdctool.impl.validator.tasks.ServiceValidationTask;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapGroupsDataDefinition;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.jsontitan.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsontitan.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by chaya on 7/18/2017.
 */
public class ModuleJsonTask extends ServiceValidationTask {

    @Autowired
    private TopologyTemplateOperation topologyTemplateOperation;

    @Override
    public boolean validate(GraphVertex vertex) {
        ComponentParametersView paramView = new ComponentParametersView();
        paramView.disableAll();
        paramView.setIgnoreArtifacts(false);
        paramView.setIgnoreGroups(false);
        paramView.setIgnoreComponentInstances(false);
        Either<ToscaElement, StorageOperationStatus> toscaElementEither = topologyTemplateOperation.getToscaElement(vertex.getUniqueId(), paramView);
        if (toscaElementEither.isRight()) {
            return false;
        }
        TopologyTemplate element = (TopologyTemplate) toscaElementEither.left().value();
        if (!isAfterSubmitForTesting(element)) {
            return false;
        }
        Map<String, MapGroupsDataDefinition> instGroups = element.getInstGroups();
        Map<String, MapArtifactDataDefinition> instDeploymentArtifacts = element.getInstDeploymentArtifacts();

        for (Map.Entry<String, MapGroupsDataDefinition> pair : instGroups.entrySet()) {
            String groupKey = pair.getKey();
            MapGroupsDataDefinition groups = pair.getValue();
            if (groups != null && !groups.getMapToscaDataDefinition().isEmpty()) {
                MapArtifactDataDefinition deploymentsArtifacts = instDeploymentArtifacts.get(groupKey);
                if (deploymentsArtifacts != null && !deploymentsArtifacts.getMapToscaDataDefinition().isEmpty()) {
                    List<ArtifactDataDefinition> moduleJsonArtifacts = deploymentsArtifacts.getMapToscaDataDefinition().values().stream().filter(artifact -> {
                        String artifactName = artifact.getArtifactName();
                        if (artifactName.startsWith(groupKey) && artifactName.endsWith("module.json")) {
                            return true;
                        }
                        return false;
                    }).collect(Collectors.toList());
                    if (moduleJsonArtifacts.size() > 0) {
                        return true;
                    }
                }
            }
        }
        return true;
    }

    private boolean isAfterSubmitForTesting(TopologyTemplate element){
        List allowedStates = new ArrayList<>(Arrays.asList(LifecycleStateEnum.READY_FOR_CERTIFICATION,
                LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, LifecycleStateEnum.CERTIFIED));
        return allowedStates.contains(element.getLifecycleState());
    }
}
