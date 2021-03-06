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

package org.openecomp.sdc.tosca.datatypes;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.tosca.datatypes.model.ArtifactType;
import org.openecomp.sdc.tosca.datatypes.model.AttributeDefinition;
import org.openecomp.sdc.tosca.datatypes.model.CapabilityAssignment;
import org.openecomp.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.openecomp.sdc.tosca.datatypes.model.Constraint;
import org.openecomp.sdc.tosca.datatypes.model.Directive;
import org.openecomp.sdc.tosca.datatypes.model.Implementation;
import org.openecomp.sdc.tosca.datatypes.model.Import;
import org.openecomp.sdc.tosca.datatypes.model.InterfaceDefinition;
import org.openecomp.sdc.tosca.datatypes.model.InterfaceDefinitionTemplate;
import org.openecomp.sdc.tosca.datatypes.model.InterfaceDefinitionType;
import org.openecomp.sdc.tosca.datatypes.model.InterfaceType;
import org.openecomp.sdc.tosca.datatypes.model.NodeFilter;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.OperationDefinition;
import org.openecomp.sdc.tosca.datatypes.model.OperationDefinitionTemplate;
import org.openecomp.sdc.tosca.datatypes.model.OperationDefinitionType;
import org.openecomp.sdc.tosca.datatypes.model.ParameterDefinition;
import org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition;
import org.openecomp.sdc.tosca.datatypes.model.PropertyType;
import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.tosca.datatypes.model.RequirementDefinition;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.datatypes.model.SubstitutionMapping;
import org.openecomp.sdc.tosca.datatypes.model.TopologyTemplate;
import org.openecomp.sdc.tosca.datatypes.model.heatextend.ParameterDefinitionExt;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.openecomp.sdc.tosca.services.YamlUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class ToscaModelTest {
  private YamlUtil yamlUtil = new YamlUtil();
  private static final String INTERFACE_ID = "inter_1";
  private static final String NODE_TEMPLATE_ID = "firstNodeTemplate";
  private static final String NODE_TYPE_ID = "compute_node_type";
  private static final String BASE_DIR = "/mock/model";
  private static final String ST = "/serviceTemplate.yaml";
  private static final String ST_WITH_INTERFACE = "/serviceTemplateWithInterface.yaml";
  private static final String ST_WITH_OPERATIONS = "/serviceTemplateWithInterfaceAndOperation.yaml";
  private static final String ST_WITH_INTERFACE_DEF =
      "/serviceTemplateWithNodeTemplateInterface.yaml";
  private static final String ST_WITH_NODE_INTERFACE_DEF =
      "/serviceTemplateWithNodeTypeInterface.yaml";
  private static final String INTERFACE_TYPE_VALUE = "tosca.interfaces.node.lifecycle.Standard";
  private static final String OPERATION_START = "start";
  private static final String OPERATION_DESC = "start operation";
  private static final String IMPLEMENTATION_NAME = "startWorkFlow.json";
  private static final String PRIMARY_IMPL = "myImpl.yaml";
  private static final String DEPENDENCY_NAME = "script1.sh";
  private static final String STRING_TYPE = "string";

  @Test
  public void testServiceTemplateJavaToYaml() {
    ServiceTemplate serviceTemplate = new ServiceTemplate();
    Map<String, String> metadata = new HashMap<>();
    metadata.put("Template_author", "OPENECOMP");
    metadata.put(ToscaConstants.ST_METADATA_TEMPLATE_NAME, "Test");
    metadata.put("Template_version", "1.0.0");
    serviceTemplate.setTosca_definitions_version("tosca_simple_yaml_1_0_0");
    serviceTemplate.setDescription("testing desc tosca service template");

    Import fileImport1 = new Import();
    fileImport1.setFile("path1/path2/file1.yaml");
    Import fileImport2 = new Import();
    fileImport2.setFile("path1/path2/file2.yaml");
    List<Map<String, Import>> imports = new ArrayList<>();
    Map<String, Import> importsMap = new HashMap<>();
    importsMap.put("myfile1", fileImport1);
    imports.add(importsMap);
    importsMap = new HashMap<>();
    importsMap.put("myfile2", fileImport2);
    imports.add(importsMap);
    serviceTemplate.setImports(imports);

    OperationDefinition operationDefinition = new OperationDefinition();
    operationDefinition.setDescription("test operation");
    InterfaceType interfaceType = new InterfaceType();
    interfaceType.setDerived_from("derived_from");
    interfaceType.setDescription("desc");
    interfaceType.addOperation("test", operationDefinition);
    serviceTemplate.addInterfaceType("test_interface", interfaceType);

    ArtifactType artifact = new ArtifactType();
    artifact.setMime_type("application/java-archive");
    ArrayList<String> ext = new ArrayList<>();
    ext.add("yaml");
    ext.add("xml");
    artifact.setFile_ext(ext);
    Map<String, ArtifactType> artifactTypes = new HashMap<>();
    artifactTypes.put("one_artifact", artifact);
    serviceTemplate.setArtifact_types(artifactTypes);

    NodeType nodeType = new NodeType();
    nodeType.setDerived_from("tosca.nodes.Root");
    nodeType.setVersion("1.0.0");
    nodeType.setDescription("tosca compute test");

    PropertyDefinition propDef1 = new PropertyDefinition();
    propDef1.setType("integer");
    propDef1.setDescription("Number of CPUs requested for a software node instance");
    propDef1.setRequired(true);
    propDef1.set_default(1);

    Constraint graterOrEqual = new Constraint();
    graterOrEqual.setGreater_or_equal((float) 5.0);
    Constraint constraintEqual = new Constraint();
    constraintEqual.setEqual(5);
    Constraint greater_than = new Constraint();
    greater_than.setGreater_than(6.02);
    Constraint inRange = new Constraint();
    inRange.setIn_range(new Object[2]);
    inRange.getIn_range()[0] = 0;
    inRange.getIn_range()[1] = ToscaConstants.UNBOUNDED;

    List<Constraint> constraints = new ArrayList<>();
    constraints.add(graterOrEqual);
    constraints.add(constraintEqual);
    constraints.add(greater_than);
    constraints.add(inRange);
    propDef1.setConstraints(constraints);

    Map<String, PropertyDefinition> properties = new HashMap<>();
    properties.put("cpu_num", propDef1);
    nodeType.setProperties(properties);

    Map<String, AttributeDefinition> attributesDef = new HashMap<>();
    AttributeDefinition attDef = new AttributeDefinition();
    attDef.setType(PropertyType.STRING.getDisplayName());
    attDef.set_default("hi");
    attributesDef.put("attDef1", attDef);
    nodeType.setAttributes(attributesDef);

    Map<String, RequirementDefinition> reqsDef = new HashMap<>();
    RequirementDefinition reqDef = new RequirementDefinition();
    reqDef.setCapability("tosca.cap1");
    reqDef.getOccurrences()[0] = 5;
    reqsDef.put("re1", reqDef);
    List<Map<String, RequirementDefinition>> reqList = new ArrayList<>();
    reqList.add(reqsDef);
    nodeType.setRequirements(reqList);


    Map<String, CapabilityDefinition> capsDef = new HashMap<>();
    CapabilityDefinition capdef = new CapabilityDefinition();
    capdef.setType("tosca.cap");
    List<String> vvSource = new ArrayList<>();
    vvSource.add("node1");
    vvSource.add("node2");
    capdef.setValid_source_types(vvSource);
    capsDef.put("cap1", capdef);
    nodeType.setCapabilities(capsDef);

    Map<String, NodeType> nodeTypes = new HashMap<>();
    nodeTypes.put("compute_node_type", nodeType);
    serviceTemplate.setNode_types(nodeTypes);

    TopologyTemplate topologyTemplate = new TopologyTemplate();
    topologyTemplate.setDescription("topologi template descroption");
    Map<String, ParameterDefinition> inputs = new HashMap<>();
    ParameterDefinition paramDef = new ParameterDefinition();
    paramDef.setType(PropertyType.STRING.getDisplayName());
    paramDef.setDescription("desc");
    paramDef.set_default("my default val");
    paramDef.setRequired(false);
    paramDef.setEntry_schema(DataModelUtil.createEntrySchema("tosca.myType", null, null));
    List<Constraint> paramConstraint = new ArrayList<>();
    Constraint paramConst1 = new Constraint();
    paramConst1.setGreater_than(6);
    Constraint paramConst2 = new Constraint();
    paramConst2.setGreater_or_equal(9);
    paramConstraint.add(paramConst1);
    paramConstraint.add(paramConst2);
    paramDef.setConstraints(paramConstraint);
    inputs.put("inParam1", paramDef);
    topologyTemplate.setInputs(inputs);

    Map<String, NodeTemplate> nodeTemplates = new HashMap<>();
    NodeTemplate nodeTemplate = new NodeTemplate();
    nodeTemplate.setType("nodeTypeRef");
    List<String> directives = new ArrayList<>();
    directives.add(Directive.SELECTABLE.getDisplayName());
    directives.add(Directive.SUBSTITUTABLE.getDisplayName());
    nodeTemplate.setDirectives(directives);
    Map<String, Object> nodeTemplateProperties = new HashMap<>();
    nodeTemplateProperties.put("prop1", "abcd");
    nodeTemplateProperties.put("prop2", "{ get_input: my_mysql_rootpw }");
    nodeTemplate.setProperties(nodeTemplateProperties);
    Map<String, Object> nodeTemplateAtts = new HashMap<>();
    nodeTemplateAtts.put("att1", "att1Val");
    nodeTemplateAtts.put("att2", "{ get_input: my_mysql_rootpw }");
    nodeTemplate.setAttributes(nodeTemplateAtts);


    RequirementAssignment reqAssignment1 = new RequirementAssignment();
    reqAssignment1.setNode("nodeA");
    reqAssignment1.setCapability("capA");
    reqAssignment1.setRelationship("relationB");
    Object[] reqAssOccurrences = new Object[2];
    reqAssOccurrences[0] = 1;
    reqAssOccurrences[1] = 2;
    reqAssignment1.setOccurrences(reqAssOccurrences);
    NodeFilter reqNodeFilter = new NodeFilter();
    List<Constraint> propConstrain1 = new ArrayList<>();
    Constraint propConst1 = new Constraint();
    propConst1.setGreater_or_equal(9);
    propConstrain1.add(propConst1);
    List<Constraint> propConstrain2 = new ArrayList<>();
    Constraint propConst2 = new Constraint();
    propConst2.setMin_length(1);
    propConstrain2.add(propConst2);
    Constraint propConst3 = new Constraint();
    propConst3.setMax_length(2);
    propConstrain2.add(propConst3);
    Map<String, List<Constraint>> nodeFilterProp = new HashMap<>();
    nodeFilterProp.put("propName1", propConstrain1);
    nodeFilterProp.put("propName2", propConstrain2);
    reqNodeFilter.setProperties(nodeFilterProp);
    reqAssignment1.setNode_filter(reqNodeFilter);

    RequirementAssignment reqAssignment2 = new RequirementAssignment();
    reqAssignment2.setNode("nodeA");
    reqAssignment2.setCapability("capA");
    reqAssignment2.setRelationship("relationB");
    Map<String, RequirementAssignment> nodeTemplateRequirement1 = new HashMap<>();
    Map<String, RequirementAssignment> nodeTemplateRequirement2 = new HashMap<>();
    nodeTemplateRequirement1.put("req1", reqAssignment1);
    nodeTemplateRequirement2.put("req2", reqAssignment2);
    nodeTemplate.setRequirements(new ArrayList<>());
    nodeTemplate.getRequirements().add(nodeTemplateRequirement1);
    nodeTemplate.getRequirements().add(nodeTemplateRequirement2);

    Map<String, CapabilityAssignment> nodeTemplateCapability = new HashMap<>();
    CapabilityAssignment capAss = new CapabilityAssignment();
    Map<String, Object> capProps = new HashMap<>();
    capProps.put("num_cpus", "{ get_input: cpus }");
    capAss.setProperties(capProps);
    Map<String, Object> capAtts = new HashMap<>();
    capAtts.put("num_cpus", "66");
    capAss.setAttributes(capAtts);
    nodeTemplateCapability.put("cap1", capAss);
    nodeTemplate.setCapabilities(nodeTemplateCapability);

    NodeFilter nodeTemplateNodeFilter = new NodeFilter();
    Map<String, List<Constraint>> ntProp = new HashMap<>();
    Constraint c1 = new Constraint();
    c1.setEqual("1 MB");
    List<Constraint> consList = new ArrayList<>();
    consList.add(c1);
    ntProp.put("test1", consList);
    nodeTemplateNodeFilter.setProperties(ntProp);
    nodeTemplate.setNode_filter(nodeTemplateNodeFilter);
    nodeTemplates.put("firatNodeTemplate", nodeTemplate);
    topologyTemplate.setNode_templates(nodeTemplates);

    SubstitutionMapping subMap = new SubstitutionMapping();
    subMap.setNode_type("myNodeType.node");
    Map<String, List<String>> mapCapabilities = new HashMap<>();
    List<String> NodeCap = new ArrayList<>();
    NodeCap.add("database");
    NodeCap.add("database_endpoint");
    mapCapabilities.put("database_endpoint", NodeCap);
    subMap.setCapabilities(mapCapabilities);
    topologyTemplate.setSubstitution_mappings(subMap);
    serviceTemplate.setTopology_template(topologyTemplate);

    String yaml = new YamlUtil().objectToYaml(serviceTemplate);
    ServiceTemplate serviceTemplateFromYaml =
        new YamlUtil().yamlToObject(yaml, ServiceTemplate.class);
    Assert.assertNotNull(serviceTemplateFromYaml);
  }


  @Test
  public void testYamlToServiceTemplateObj() throws IOException {
    ServiceTemplate serviceTemplateFromYaml =
        getServiceTemplate(BASE_DIR + ST);
    Assert.assertNotNull(serviceTemplateFromYaml);
  }

  @Test
  public void testYamlWithInterfaceToServiceTemplateObj() throws IOException {
    ServiceTemplate serviceTemplateWithOperation =
        getServiceTemplate(BASE_DIR + ST_WITH_OPERATIONS);
    Assert.assertNotNull(serviceTemplateWithOperation);

    InterfaceType expectedInterfaceType = createInterfaceType();

    Map<String, InterfaceType> interfaceTypes =
        DataModelUtil.getInterfaceTypes(serviceTemplateWithOperation);
    Assert.assertEquals(1, interfaceTypes.size());
    InterfaceType actualInterfaceType = interfaceTypes.get(INTERFACE_ID);
    Assert.assertEquals(expectedInterfaceType, actualInterfaceType);
  }

  @Test
  public void testAddOperationToInterface() throws IOException {
    YamlUtil yamlUtil = new YamlUtil();
    ServiceTemplate serviceTemplateWithInterface =
        getServiceTemplate(BASE_DIR + ST_WITH_INTERFACE);
    ServiceTemplate serviceTemplateWithOperation =
        getServiceTemplate(BASE_DIR + ST_WITH_OPERATIONS);

    OperationDefinition operationDefinition = createOperationDefinition();

    DataModelUtil
        .addInterfaceOperation(serviceTemplateWithInterface, INTERFACE_ID, OPERATION_START,
            operationDefinition);
    String expectedServiceTemplate = yamlUtil.objectToYaml(serviceTemplateWithOperation);
    String actualServiceTemplate = yamlUtil.objectToYaml(serviceTemplateWithInterface);
    Assert.assertEquals(expectedServiceTemplate, actualServiceTemplate);
  }

  @Test
  public void testInterfaceTypeToObjConversion() throws IOException {
    ServiceTemplate serviceTemplateWithInterface =
        getServiceTemplate(BASE_DIR + ST_WITH_INTERFACE);
    ServiceTemplate serviceTemplateWithOperation =
        getServiceTemplate(BASE_DIR + ST_WITH_OPERATIONS);
    InterfaceType interfaceType = createInterfaceType();

    Optional<Object> interfaceAsObj = DataModelUtil.convertInterfaceTypeToObj(interfaceType);
    Assert.assertTrue(interfaceAsObj.isPresent());

    Map<String, Object> interfaceTypes = new HashMap<>();
    interfaceTypes.put(INTERFACE_ID, interfaceAsObj.get());
    serviceTemplateWithInterface.setInterface_types(interfaceTypes);

    String expectedServiceTemplate = yamlUtil.objectToYaml(serviceTemplateWithOperation);
    String actualServiceTemplate = yamlUtil.objectToYaml(serviceTemplateWithInterface);
    Assert.assertEquals(expectedServiceTemplate, actualServiceTemplate);
  }

  @Test
  public void testObjToInterfaceTypeConversion() throws IOException, ReflectiveOperationException {
    ServiceTemplate serviceTemplateWithOperation =
        getServiceTemplate(BASE_DIR + ST_WITH_OPERATIONS);
    Map<String, Object> interfaceTypes = serviceTemplateWithOperation.getInterface_types();
    Object interfaceObj = interfaceTypes.get(INTERFACE_ID);
    Optional<InterfaceType> actualInterfaceType =
        DataModelUtil.convertObjToInterfaceType(INTERFACE_ID, interfaceObj);

    Assert.assertTrue(actualInterfaceType.isPresent());
    InterfaceType expectedInterfaceType = createInterfaceType();
    Assert.assertEquals(expectedInterfaceType, actualInterfaceType.get());
  }

  @Test
  public void testObjToInterfaceDefinitionTypeConversion()
      throws IOException, ReflectiveOperationException {
    ServiceTemplate serviceTemplateWithInterfaceDef =
        getServiceTemplate(BASE_DIR + ST_WITH_NODE_INTERFACE_DEF);
    NodeType nodeTypeWithInterface =
        DataModelUtil.getNodeType(serviceTemplateWithInterfaceDef, NODE_TYPE_ID);
    Map<String, Object> interfaces = nodeTypeWithInterface.getInterfaces();
    Object interfaceObj = interfaces.get(INTERFACE_ID);

    Optional<? extends InterfaceDefinition> actualInterfaceDefinition =
        DataModelUtil.convertObjToInterfaceDefinition(INTERFACE_ID, interfaceObj,
            InterfaceDefinitionType.class);

    Assert.assertTrue(actualInterfaceDefinition.isPresent());

    InterfaceDefinitionType expectedInterfaceDefinitionType = createInterfaceDefinitionType();
    Assert.assertEquals(expectedInterfaceDefinitionType, actualInterfaceDefinition.get());
  }

  @Test
  public void testObjToInterfaceDefinitionTemplateConversion()
      throws IOException, ReflectiveOperationException {
    ServiceTemplate serviceTemplateWithInterfaceDef =
        getServiceTemplate(BASE_DIR + ST_WITH_INTERFACE_DEF);
    NodeTemplate nodeTemplateWithInterface =
        DataModelUtil.getNodeTemplate(serviceTemplateWithInterfaceDef, NODE_TEMPLATE_ID);
    Map<String, Object> interfaces = nodeTemplateWithInterface.getInterfaces();
    Object interfaceObj = interfaces.get(INTERFACE_ID);

    Optional<? extends InterfaceDefinition> actualInterfaceDefinition =
        DataModelUtil.convertObjToInterfaceDefinition(INTERFACE_ID, interfaceObj,
            InterfaceDefinitionTemplate.class);

    Assert.assertTrue(actualInterfaceDefinition.isPresent());
    InterfaceDefinitionTemplate expectedInterfaceDefinitionTemplate = createInterfaceDefinitionTemplate();
    Assert.assertEquals(expectedInterfaceDefinitionTemplate, actualInterfaceDefinition.get());
  }

  @Test
  public void testYamlToServiceTemplateIncludingHeatExtend() throws IOException {
    ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
    try (InputStream yamlFile =
             toscaExtensionYamlUtil.loadYamlFileIs(BASE_DIR + "/serviceTemplateHeatExtend.yaml")) {
      ServiceTemplate serviceTemplateFromYaml =
          toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);
      ParameterDefinitionExt parameterDefinitionExt =
          (ParameterDefinitionExt) serviceTemplateFromYaml.getTopology_template().getInputs()
              .get("inParam1");
      Assert.assertNotNull(parameterDefinitionExt.getLabel());
      String backToYamlString = toscaExtensionYamlUtil.objectToYaml(serviceTemplateFromYaml);
      Assert.assertNotNull(backToYamlString);
    }
  }

  private ServiceTemplate getServiceTemplate(String inputPath) throws IOException {
    try (InputStream yamlFile = yamlUtil.loadYamlFileIs(inputPath)) {
      return yamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);
    }
  }

  private InterfaceType createInterfaceType() {
    OperationDefinition operationDefinition = createOperationDefinition();
    InterfaceType interfaceType = new InterfaceType();
    interfaceType.setDescription("test interface");
    interfaceType.addOperation(OPERATION_START, operationDefinition);
    return interfaceType;
  }

  private OperationDefinition createOperationDefinition() {
    OperationDefinition operationDefinition = new OperationDefinition();
    operationDefinition.setDescription(OPERATION_DESC);
    return operationDefinition;
  }

  private InterfaceDefinitionType createInterfaceDefinitionType() {
    OperationDefinitionType operationDefinitionType = createOperationDefinitionType();
    InterfaceDefinitionType interfaceDefinitionType = new InterfaceDefinitionType();
    interfaceDefinitionType.setType(INTERFACE_TYPE_VALUE);
    interfaceDefinitionType.addOperation(OPERATION_START, operationDefinitionType);
    return interfaceDefinitionType;
  }

  private InterfaceDefinitionTemplate createInterfaceDefinitionTemplate() {
    OperationDefinitionTemplate operationDefinitionTemplate = createOperationDefinitionTemplate();
    InterfaceDefinitionTemplate interfaceDefinitionTemplate = new InterfaceDefinitionTemplate();
    interfaceDefinitionTemplate.addOperation(OPERATION_START, operationDefinitionTemplate);
    return interfaceDefinitionTemplate;
  }

  private OperationDefinitionTemplate createOperationDefinitionTemplate() {
    OperationDefinitionTemplate operationDefinitionTemplate = new OperationDefinitionTemplate();
    operationDefinitionTemplate.setDescription(OPERATION_DESC);
    Implementation implementation = new Implementation();
    implementation.setPrimary(PRIMARY_IMPL);
    List<String> dependencies = new ArrayList<>();
    dependencies.add(DEPENDENCY_NAME);
    implementation.setDependencies(dependencies);
    operationDefinitionTemplate.setImplementation(implementation);
    return operationDefinitionTemplate;
  }

  private OperationDefinitionType createOperationDefinitionType() {
    OperationDefinitionType operationDefinitionType = new OperationDefinitionType();
    operationDefinitionType.setDescription(OPERATION_DESC);
    operationDefinitionType.setImplementation(IMPLEMENTATION_NAME);
    PropertyDefinition propertyDefinition = new PropertyDefinition();
    propertyDefinition.setType(STRING_TYPE);
    return operationDefinitionType;
  }
}






