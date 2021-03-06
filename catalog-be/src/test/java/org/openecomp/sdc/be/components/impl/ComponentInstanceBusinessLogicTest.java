package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathElementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.ForwardingPathOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * The test suite designed for test functionality of ComponentInstanceBusinessLogic class
 */
public class ComponentInstanceBusinessLogicTest {

    private final static String USER_ID = "jh0003";
    private final static String COMPONENT_ID = "componentId";
    private final static String TO_INSTANCE_ID = "toInstanceId";
    private final static String FROM_INSTANCE_ID = "fromInstanceId";
    private final static String RELATION_ID = "relationId";
    private final static String CAPABILITY_OWNER_ID = "capabilityOwnerId";
    private final static String CAPABILITY_UID = "capabilityUid";
    private final static String CAPABILITY_NAME = "capabilityName";
    private final static String REQUIREMENT_OWNER_ID = "requirementOwnerId";
    private final static String REQUIREMENT_UID = "requirementUid";
    private final static String REQUIREMENT_NAME = "requirementName";
    private final static String RELATIONSHIP_TYPE = "relationshipType";

    private static ComponentsUtils componentsUtils;
    private static ServletUtils servletUtils;
    private static ResponseFormat responseFormat;
    private static ToscaOperationFacade toscaOperationFacade;
    private static UserBusinessLogic userAdmin;

    private static ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    private static ForwardingPathOperation forwardingPathOperation;
    private static User user;
    private static UserValidations userValidations;
    private static Component service;
    private static Component resource;
    private static ComponentInstance toInstance;
    private static ComponentInstance fromInstance;
    private static CapabilityDataDefinition capability;
    private static RequirementDataDefinition requirement;
    private static RequirementCapabilityRelDef relation;

    @BeforeClass
    public static void setup() {
        createMocks();
        setMocks();
        stubMethods();
        createComponents();

    }

    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
    }
    @Test
    public void testGetRelationByIdSuccess(){
        getServiceRelationByIdSuccess(service);
        getServiceRelationByIdSuccess(resource);
    }

    @Test
    public void testGetRelationByIdUserValidationFailure(){
        getServiceRelationByIdUserValidationFailure(service);
        getServiceRelationByIdUserValidationFailure(resource);
    }

    @Test
    public void testGetRelationByIdComponentNotFoundFailure(){
        getRelationByIdComponentNotFoundFailure(service);
        getRelationByIdComponentNotFoundFailure(resource);
    }




    @Test
    public void testForwardingPathOnVersionChange(){
        getforwardingPathOnVersionChange();
    }

    private void getforwardingPathOnVersionChange(){
        String containerComponentParam="services";
        String containerComponentID="121-cont";
        String componentInstanceID="121-cont-1-comp";
        Service component=new Service();
        Map<String, ForwardingPathDataDefinition> forwardingPaths = generateForwardingPath(componentInstanceID);

        //Add existing componentInstance to component
        List<ComponentInstance> componentInstanceList=new ArrayList<>();
        ComponentInstance oldComponentInstance=new ComponentInstance();
        oldComponentInstance.setName("OLD_COMP_INSTANCE");
        oldComponentInstance.setUniqueId(componentInstanceID);
        oldComponentInstance.setToscaPresentationValue(JsonPresentationFields.CI_COMPONENT_UID,"1-comp");
        componentInstanceList.add(oldComponentInstance);
        component.setComponentInstances(componentInstanceList);
        component.setForwardingPaths(forwardingPaths);

        List<ComponentInstance> componentInstanceListNew=new ArrayList<>();
        ComponentInstance newComponentInstance=new ComponentInstance();
        String new_Comp_UID="2-comp";
        newComponentInstance.setToscaPresentationValue(JsonPresentationFields.CI_COMPONENT_UID,new_Comp_UID);
        newComponentInstance.setUniqueId(new_Comp_UID);
        componentInstanceListNew.add(newComponentInstance);
        Component component2=new Service();
        component2.setComponentInstances(componentInstanceListNew);

        //Mock for getting component
        when(toscaOperationFacade.getToscaElement(eq(containerComponentID),any(ComponentParametersView.class))).thenReturn(Either.left(component));
        when(toscaOperationFacade.validateComponentExists(any(String.class))).thenReturn(Either.left(Boolean.TRUE));
        //Mock for getting component for componentInstance
        when(toscaOperationFacade.getToscaFullElement(eq("1-comp"))).thenReturn(Either.left(component));
        when(toscaOperationFacade.getToscaFullElement(eq(new_Comp_UID))).thenReturn(Either.left(component2));

        Either<Set<String>, ResponseFormat> resultOp = componentInstanceBusinessLogic.forwardingPathOnVersionChange
            (containerComponentParam,containerComponentID,componentInstanceID,newComponentInstance);
        Assert.assertEquals(1,resultOp.left().value().size());
        Assert.assertEquals("FP-ID-1",resultOp.left().value().iterator().next());

    }


    @Test
    public void testDeleteForwardingPathsWhenComponentinstanceDeleted(){

        ComponentTypeEnum containerComponentType = ComponentTypeEnum.findByParamName("services");
        String containerComponentID = "Service-comp";
        String componentInstanceID = "NodeA1";
        Service component = new Service();

        component.addForwardingPath(createPath("path1", "NodeA1", "NodeB1",  "1"));
        component.addForwardingPath(createPath("Path2", "NodeA2","NodeB2", "2"));
        when(toscaOperationFacade.getToscaElement(eq(containerComponentID),any(ComponentParametersView.class))).thenReturn(Either.left(component));
        when(toscaOperationFacade.getToscaElement(eq(containerComponentID))).thenReturn(Either.left(component));
        when(forwardingPathOperation.deleteForwardingPath(any(Service.class), anySet())).thenReturn(Either.left(new HashSet<>()));
        Either<ComponentInstance, ResponseFormat> responseFormatEither = componentInstanceBusinessLogic.deleteForwardingPathsRelatedTobeDeletedComponentInstance(
                containerComponentID, componentInstanceID, containerComponentType, Either.left(new ComponentInstance()));
        Assert.assertTrue(responseFormatEither.isLeft());

    }

    @Test
    public void testDeleteForwardingPathsWhenErrorInComponentinstanceDelete(){

        ComponentTypeEnum containerComponentType = ComponentTypeEnum.findByParamName("services");
        String containerComponentID = "Service-comp";
        String componentInstanceID = "NodeA1";
        Service component = new Service();

        component.addForwardingPath(createPath("path1", "NodeA1", "NodeB1",  "1"));
        component.addForwardingPath(createPath("Path2", "NodeA2","NodeB2", "2"));
        when(toscaOperationFacade.getToscaElement(eq(containerComponentID),any(ComponentParametersView.class))).thenReturn(Either.left(component));
        when(toscaOperationFacade.getToscaElement(eq(containerComponentID))).thenReturn(Either.left(component));
        when(forwardingPathOperation.deleteForwardingPath(any(Service.class), anySet())).thenReturn(Either.left(new HashSet<>()));
        Either<ComponentInstance, ResponseFormat> responseFormatEither = componentInstanceBusinessLogic.deleteForwardingPathsRelatedTobeDeletedComponentInstance(
                containerComponentID, componentInstanceID, containerComponentType, Either.right(new ResponseFormat()));
        Assert.assertTrue(responseFormatEither.isRight());

    }


    private ForwardingPathDataDefinition createPath(String pathName, String fromNode, String toNode , String uniqueId){
        ForwardingPathDataDefinition forwardingPath = new ForwardingPathDataDefinition(pathName);
        forwardingPath.setProtocol("protocol");
        forwardingPath.setDestinationPortNumber("port");
        forwardingPath.setUniqueId(uniqueId);
        ListDataDefinition<ForwardingPathElementDataDefinition> forwardingPathElementListDataDefinition = new ListDataDefinition<>();
        forwardingPathElementListDataDefinition.add(new ForwardingPathElementDataDefinition(fromNode, toNode,
                "nodeAcpType", "nodeBcpType", "nodeDcpName", "nodeBcpName"));
        forwardingPath.setPathElements(forwardingPathElementListDataDefinition);

        return forwardingPath;
    }



    private Map<String, ForwardingPathDataDefinition> generateForwardingPath(String componentInstanceID) {
        ForwardingPathDataDefinition forwardingPath = new ForwardingPathDataDefinition("fpName");
        String protocol = "protocol";
        forwardingPath.setProtocol(protocol);
        forwardingPath.setDestinationPortNumber("DestinationPortNumber");
        forwardingPath.setUniqueId("FP-ID-1");
        ListDataDefinition<ForwardingPathElementDataDefinition> forwardingPathElementListDataDefinition =
            new ListDataDefinition<>();
        forwardingPathElementListDataDefinition.add(
            new ForwardingPathElementDataDefinition(componentInstanceID, "nodeB", "nodeA_FORWARDER_CAPABILITY",
                "nodeBcpType" , "nodeDcpName",
                "nodeBcpName"));
        forwardingPath.setPathElements(forwardingPathElementListDataDefinition);
        Map<String, ForwardingPathDataDefinition> forwardingPaths = new HashMap<>();
        forwardingPaths.put("1122", forwardingPath);
        return forwardingPaths;
    }

  @SuppressWarnings("unchecked")
    private void getServiceRelationByIdSuccess(Component component){
        Either<User, ActionStatus> eitherCreator = Either.left(user);
        when(userAdmin.getUser(eq(USER_ID), eq(false))).thenReturn(eitherCreator);
        Either<Component, StorageOperationStatus> getComponentRes = Either.left(component);
        when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(getComponentRes);

        Either<RequirementDataDefinition, StorageOperationStatus> getfulfilledRequirementRes = Either.left(requirement);
        when(toscaOperationFacade.getFulfilledRequirementByRelation(eq(COMPONENT_ID), eq(FROM_INSTANCE_ID), eq(relation), any(BiPredicate.class))).thenReturn(getfulfilledRequirementRes);

        Either<CapabilityDataDefinition, StorageOperationStatus> getfulfilledCapabilityRes = Either.left(capability);
        when(toscaOperationFacade.getFulfilledCapabilityByRelation(eq(COMPONENT_ID), eq(FROM_INSTANCE_ID), eq(relation), any(BiPredicate.class))).thenReturn(getfulfilledCapabilityRes);

        Either<RequirementCapabilityRelDef, ResponseFormat> response = componentInstanceBusinessLogic.getRelationById(COMPONENT_ID, RELATION_ID, USER_ID, component.getComponentType());
        assertTrue(response.isLeft());
    }

    private void getServiceRelationByIdUserValidationFailure(Component component){
        //Either<User, ActionStatus> eitherCreator = Either.right(ActionStatus.USER_NOT_FOUND);
        //when(userAdmin.getUser(eq(USER_ID), eq(false))).thenReturn(eitherCreator);
        when(userValidations.validateUserExists(eq(USER_ID), eq("get relation by Id"), eq(false))).thenReturn(Either.right(new ResponseFormat(404)));
        Either<RequirementCapabilityRelDef, ResponseFormat> response = componentInstanceBusinessLogic.getRelationById(COMPONENT_ID, RELATION_ID, USER_ID, component.getComponentType());
        assertTrue(response.isRight());
    }

    private void getRelationByIdComponentNotFoundFailure(Component component){
        Either<User, ActionStatus> eitherCreator = Either.left(user);
        when(userAdmin.getUser(eq(USER_ID), eq(false))).thenReturn(eitherCreator);
        Either<Component, StorageOperationStatus> getComponentRes = Either.right(StorageOperationStatus.NOT_FOUND);
        when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(getComponentRes);

        Either<RequirementCapabilityRelDef, ResponseFormat> response = componentInstanceBusinessLogic.getRelationById(COMPONENT_ID, RELATION_ID, USER_ID, component.getComponentType());
        assertTrue(response.isRight());
    }

    private static void createMocks() {
        componentsUtils = Mockito.mock(ComponentsUtils.class);
        servletUtils = Mockito.mock(ServletUtils.class);
        responseFormat = Mockito.mock(ResponseFormat.class);
        toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);
        userAdmin =  Mockito.mock(UserBusinessLogic.class);
        user = Mockito.mock(User.class);
        userValidations = Mockito.mock(UserValidations.class);
        forwardingPathOperation = Mockito.mock(ForwardingPathOperation.class);
    }

    private static void setMocks() {
        componentInstanceBusinessLogic = new ComponentInstanceBusinessLogic();
        componentInstanceBusinessLogic.setToscaOperationFacade(toscaOperationFacade);
        componentInstanceBusinessLogic.setUserAdmin(userAdmin);
        componentInstanceBusinessLogic.setComponentsUtils(componentsUtils);
        componentInstanceBusinessLogic.setUserValidations(userValidations);
        componentInstanceBusinessLogic.setForwardingPathOperation(forwardingPathOperation);
    }

    private static void stubMethods() {
        when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
        when(userValidations.validateUserExists(eq(USER_ID), eq("get relation by Id"), eq(false))).thenReturn(Either.left(user));
        when(componentsUtils.getResponseFormat(eq(ActionStatus.RELATION_NOT_FOUND), eq(RELATION_ID), eq(COMPONENT_ID))).thenReturn(responseFormat);
    }

    private static void createComponents() {
        createRelation();
        createInstances();
        createService();
        createResource();
    }

    private static void createResource() {
        resource = new Resource();
        resource.setUniqueId(COMPONENT_ID);
        resource.setComponentInstancesRelations(Lists.newArrayList(relation));
        resource.setComponentInstances(Lists.newArrayList(toInstance,fromInstance));
        resource.setCapabilities(toInstance.getCapabilities());
        resource.setRequirements(fromInstance.getRequirements());
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
    }


    private static void createService() {
        service = new Service();
        service.setUniqueId(COMPONENT_ID);
        service.setComponentInstancesRelations(Lists.newArrayList(relation));
        service.setComponentInstances(Lists.newArrayList(toInstance,fromInstance));
        service.setCapabilities(toInstance.getCapabilities());
        service.setRequirements(fromInstance.getRequirements());
        service.setComponentType(ComponentTypeEnum.SERVICE);
    }


    private static void createInstances() {
        toInstance = new ComponentInstance();
        toInstance.setUniqueId(TO_INSTANCE_ID);

        fromInstance = new ComponentInstance();
        fromInstance.setUniqueId(FROM_INSTANCE_ID);

        capability = new CapabilityDataDefinition();
        capability.setOwnerId(CAPABILITY_OWNER_ID);
        capability.setUniqueId(CAPABILITY_UID);
        capability.setName(CAPABILITY_NAME);

        Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        capabilities.put(capability.getName(), Lists.newArrayList(new CapabilityDefinition(capability)));

        requirement = new RequirementDataDefinition();
        requirement.setOwnerId(REQUIREMENT_OWNER_ID);
        requirement.setUniqueId(REQUIREMENT_UID);
        requirement.setName(REQUIREMENT_NAME);
        requirement.setRelationship(RELATIONSHIP_TYPE);


        Map<String, List<RequirementDefinition>> requirements = new HashMap<>();
        requirements.put(requirement.getCapability(), Lists.newArrayList(new RequirementDefinition(requirement)));

        toInstance.setCapabilities(capabilities);
        fromInstance.setRequirements(requirements);
    }


    private static void createRelation() {

        relation = new RequirementCapabilityRelDef();
        CapabilityRequirementRelationship relationship = new CapabilityRequirementRelationship();
        RelationshipInfo relationInfo = new RelationshipInfo();
        relationInfo.setId(RELATION_ID);
        relationship.setRelation(relationInfo);

        relation.setRelationships(Lists.newArrayList(relationship));
        relation.setToNode(TO_INSTANCE_ID);
        relation.setFromNode(FROM_INSTANCE_ID);

        relationInfo.setCapabilityOwnerId(CAPABILITY_OWNER_ID);
        relationInfo.setCapabilityUid(CAPABILITY_UID);
        relationInfo.setCapability(CAPABILITY_NAME);
        relationInfo.setRequirementOwnerId(REQUIREMENT_OWNER_ID);
        relationInfo.setRequirementUid(REQUIREMENT_UID);
        relationInfo.setRequirement(REQUIREMENT_NAME);
        RelationshipImpl relationshipImpl  = new RelationshipImpl();
        relationshipImpl.setType(RELATIONSHIP_TYPE);
        relationInfo.setRelationships(relationshipImpl);
    }
}
