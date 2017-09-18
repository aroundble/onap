package org.openecomp.sdc.ci.tests.execute.sanity;

import java.util.List;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactInfo;
import org.openecomp.sdc.ci.tests.datatypes.CanvasElement;
import org.openecomp.sdc.ci.tests.datatypes.CanvasManager;
import org.openecomp.sdc.ci.tests.datatypes.LifeCycleStateEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.GeneralPageElements;
import org.openecomp.sdc.ci.tests.pages.InformationalArtifactPage;
import org.openecomp.sdc.ci.tests.pages.PropertiesPage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.TesterOperationPage;
import org.openecomp.sdc.ci.tests.pages.ToscaArtifactsPage;
import org.openecomp.sdc.ci.tests.utilities.ArtifactUIUtils;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.ci.tests.verificator.VfVerificator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;

public class PNF extends SetupCDTest {
	
	private String filePath;
	@BeforeClass
	public void beforeClass(){
		filePath = FileHandling.getFilePath("");
	}
	
	@BeforeMethod
	public void beforeTest(){
		System.out.println("File repository is : " + filePath);
		getExtendTest().log(Status.INFO, "File repository is : " + filePath);
	}
		
	@Test
	public void updatePNF() throws Exception {

		ResourceReqDetails pnfMetaData = createPNFWithGenerateName();

		// update Resource
		ResourceReqDetails updatedResource = new ResourceReqDetails();
		updatedResource.setName("ciUpdatedName");
		updatedResource.setDescription("kuku");
		updatedResource.setVendorName("updatedVendor");
		updatedResource.setVendorRelease("updatedRelease");
		updatedResource.setContactId("ab0001");
		updatedResource.setCategories(pnfMetaData.getCategories());
		updatedResource.setVersion("0.1");
		updatedResource.setResourceType(ResourceTypeEnum.VF.getValue());
 		List<String> newTags = pnfMetaData.getTags();
		newTags.remove(pnfMetaData.getName());
		newTags.add(updatedResource.getName());
		updatedResource.setTags(newTags);
		ResourceUIUtils.updateResource(updatedResource, getUser());

		VfVerificator.verifyVFMetadataInUI(updatedResource);
		VfVerificator.verifyVFUpdated(updatedResource, getUser());
	}
	
	@Test
	public void addUpdateDeleteInformationalArtifactPNFTest() throws Exception {
		ResourceReqDetails pnfMetaData = createPNFWithGenerateName();

		ResourceGeneralPage.getLeftMenu().moveToInformationalArtifactScreen();
		
		ArtifactInfo informationalArtifact = new ArtifactInfo(filePath, "asc_heat 0 2.yaml", "kuku", "artifact1", "OTHER");
		InformationalArtifactPage.clickAddNewArtifact();
		ArtifactUIUtils.fillAndAddNewArtifactParameters(informationalArtifact);
		
		AssertJUnit.assertTrue("artifact table does not contain artifacts uploaded", InformationalArtifactPage.checkElementsCountInTable(1));
		
		String newDescription = "new description";
		InformationalArtifactPage.clickEditArtifact(informationalArtifact.getArtifactLabel());
		InformationalArtifactPage.artifactPopup().insertDescription(newDescription);
		InformationalArtifactPage.artifactPopup().clickDoneButton();
		String actualArtifactDescription = InformationalArtifactPage.getArtifactDescription(informationalArtifact.getArtifactLabel());
		AssertJUnit.assertTrue("artifact description is not updated", newDescription.equals(actualArtifactDescription));
		
		InformationalArtifactPage.clickDeleteArtifact(informationalArtifact.getArtifactLabel());
		InformationalArtifactPage.clickOK();
		AssertJUnit.assertTrue("artifact "+ informationalArtifact.getArtifactLabel() + "is not deleted", InformationalArtifactPage.checkElementsCountInTable(0));
	}
	
	@Test
	public void addPropertiesToVfcInstanceInPNFTest() throws Exception {
		
		String fileName = "CP.yml";
		ResourceReqDetails atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.CP, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		
		try{
			ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
			ResourceGeneralPage.clickCheckinButton(atomicResourceMetaData.getName());
	
			ResourceReqDetails pnfMetaData = createPNFWithGenerateName();
	
			ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
			CanvasManager vfCanvasManager = CanvasManager.getCanvasManager();
			CompositionPage.searchForElement(atomicResourceMetaData.getName());
			CanvasElement cpElement = vfCanvasManager.createElementOnCanvas(atomicResourceMetaData.getName());
	
			vfCanvasManager.clickOnCanvaElement(cpElement);
			CompositionPage.showPropertiesAndAttributesTab();
			List<WebElement> properties = CompositionPage.getProperties();
			String propertyValue = "abc123";
			for (int i = 0; i < 2; i++) {
				WebElement findElement = properties.get(i).findElement(By.className("i-sdc-designer-sidebar-section-content-item-property-and-attribute-label"));
				findElement.click();
				PropertiesPage.getPropertyPopup().insertPropertyDefaultValue(propertyValue);
				PropertiesPage.getPropertyPopup().clickSave();				
				
				findElement = properties.get(i).findElement(By.className("i-sdc-designer-sidebar-section-content-item-property-value"));
				AssertJUnit.assertTrue(findElement.getText().equals(propertyValue));
			}
		}
		finally{
			ResourceRestUtils.deleteResourceByNameAndVersion(atomicResourceMetaData.getName(), "0.1");
		}
	}
	
	@Test
	public void changeInstanceVersionPNFTest() throws Exception{
		
		ResourceReqDetails atomicResourceMetaData = null;
		ResourceReqDetails pnfMetaData = null;
		CanvasManager vfCanvasManager;
		CanvasElement cpElement = null;
		String fileName = "CP.yml";
		try{
			atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.CP, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
			ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
			ResourceGeneralPage.clickSubmitForTestingButton(atomicResourceMetaData.getName());
			
			pnfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.PNF, getUser());
			ResourceUIUtils.createPNF(pnfMetaData, getUser());
			ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
			vfCanvasManager = CanvasManager.getCanvasManager();
			CompositionPage.searchForElement(atomicResourceMetaData.getName());
			cpElement = vfCanvasManager.createElementOnCanvas(atomicResourceMetaData.getName());
			
		
			CompositionPage.clickSubmitForTestingButton(pnfMetaData.getName());
			assert(false);
		}
		catch(Exception e){ 
			String errorMessage = GeneralUIUtils.getWebElementByClassName("w-sdc-modal-caption").getText();
			String checkUIResponseOnError = ErrorValidationUtils.checkUIResponseOnError(ActionStatus.VALIDATED_RESOURCE_NOT_FOUND.name());
			AssertJUnit.assertTrue(errorMessage.contains(checkUIResponseOnError));
			
			
			reloginWithNewRole(UserRoleEnum.TESTER);
			GeneralUIUtils.findComponentAndClick(atomicResourceMetaData.getName());
			TesterOperationPage.certifyComponent(atomicResourceMetaData.getName());
			
			reloginWithNewRole(UserRoleEnum.DESIGNER);
			GeneralUIUtils.findComponentAndClick(pnfMetaData.getName());
			ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
			vfCanvasManager = CanvasManager.getCanvasManager();
			CompositionPage.changeComponentVersion(vfCanvasManager, cpElement, "1.0");
			
			//verfication
			VfVerificator.verifyInstanceVersion(pnfMetaData, getUser(), atomicResourceMetaData.getName(), "1.0");
		}
			
		finally{
			ResourceRestUtils.deleteResourceByNameAndVersion(atomicResourceMetaData.getName(), "1.0");
		}
		
	}
	
	@Test
	public void verifyToscaArtifactsExistPNFTest() throws Exception{
		ResourceReqDetails pnfMetaData = createPNFWithGenerateName();
		
		final int numOfToscaArtifacts = 2;
		ResourceGeneralPage.getLeftMenu().moveToToscaArtifactsScreen();
		AssertJUnit.assertTrue(ToscaArtifactsPage.checkElementsCountInTable(numOfToscaArtifacts));
		
		for(int i = 0; i < numOfToscaArtifacts; i++){
			String typeFromScreen = ToscaArtifactsPage.getArtifactType(i);
			AssertJUnit.assertTrue(typeFromScreen.equals(ArtifactTypeEnum.TOSCA_CSAR.getType()) || typeFromScreen.equals(ArtifactTypeEnum.TOSCA_TEMPLATE.getType()));
		}
		
		ToscaArtifactsPage.clickSubmitForTestingButton(pnfMetaData.getName());
		VfVerificator.verifyToscaArtifactsInfo(pnfMetaData, getUser());
	}	
	
	@Test
	public void pnfCertificationTest() throws Exception{
		ResourceReqDetails pnfMetaData = createPNFWithGenerateName();
		
		String vfName = pnfMetaData.getName();
		
		ResourceGeneralPage.clickCheckinButton(vfName);
		GeneralUIUtils.findComponentAndClick(vfName);
		ResourceGeneralPage.clickSubmitForTestingButton(vfName);
		
		reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(vfName);
		TesterOperationPage.certifyComponent(vfName);
		
		pnfMetaData.setVersion("1.0");
		VfVerificator.verifyVFLifecycle(pnfMetaData, getUser(), LifecycleStateEnum.CERTIFIED);
		
		reloginWithNewRole(UserRoleEnum.DESIGNER);
		GeneralUIUtils.findComponentAndClick(vfName);
		VfVerificator.verifyVfLifecycleInUI(LifeCycleStateEnum.CERTIFIED);
	}
	
	@Test
	public void deletePNFCheckedoutTest() throws Exception{
		ResourceReqDetails pnfMetaData = createPNFWithGenerateName();
		
		GeneralPageElements.clickTrashButtonAndConfirm();
		
		pnfMetaData.setVersion("0.1");
		VfVerificator.verifyVfDeleted(pnfMetaData, getUser());
	}
	
	@Test
	public void revertPNFMetadataTest() throws Exception{
		ResourceReqDetails pnfMetaData = createPNFWithGenerateName();
		
		ResourceReqDetails pvfRevertDetails = new ResourceReqDetails();
		pvfRevertDetails.setName("ciUpdatedName");
		pvfRevertDetails.setDescription("kuku");
		pvfRevertDetails.setCategories(pnfMetaData.getCategories());
		pvfRevertDetails.setVendorName("updatedVendor");
		pvfRevertDetails.setVendorRelease("updatedRelease");
		ResourceUIUtils.fillResourceGeneralInformationPage(pvfRevertDetails, getUser(), false);
		
		GeneralPageElements.clickRevertButton();
		
		VfVerificator.verifyVFMetadataInUI(pnfMetaData);		
	}
	
	@Test
	public void checkoutVfTest() throws Exception{
		ResourceReqDetails pnfMetaData = createPNFWithGenerateName();
		
		ResourceGeneralPage.clickCheckinButton(pnfMetaData.getName());
		GeneralUIUtils.findComponentAndClick(pnfMetaData.getName());
		GeneralPageElements.clickCheckoutButton();
		
		pnfMetaData.setVersion("0.2");
		VfVerificator.verifyVFLifecycle(pnfMetaData, getUser(), LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		VfVerificator.verifyVfLifecycleInUI(LifeCycleStateEnum.CHECKOUT);
		
		ResourceGeneralPage.clickSubmitForTestingButton(pnfMetaData.getName());
		
		reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(pnfMetaData.getName());
		TesterOperationPage.certifyComponent(pnfMetaData.getName());
		
		reloginWithNewRole(UserRoleEnum.DESIGNER);
		GeneralUIUtils.findComponentAndClick(pnfMetaData.getName());
		ResourceGeneralPage.clickCheckoutButton();
		
		pnfMetaData.setVersion("1.1");
		pnfMetaData.setUniqueId(null);
		VfVerificator.verifyVFLifecycle(pnfMetaData, getUser(), LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		VfVerificator.verifyVfLifecycleInUI(LifeCycleStateEnum.CHECKOUT);
	}
	
	public ResourceReqDetails createPNFWithGenerateName() throws Exception {
		ResourceReqDetails pnfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.PNF, getUser());
		ResourceUIUtils.createPNF(pnfMetaData, getUser());
		return pnfMetaData;
	}
	
	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}