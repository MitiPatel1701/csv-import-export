package com.user.csv.importer.portlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;

import org.osgi.service.component.annotations.Component;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.user.csv.importer.constants.UserCSVImportPortletKeys;
import com.user.csv.importer.util.UserCSVImportUtil;

/**
 * @author miti.patel
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.display-category=category.sample",
		"com.liferay.portlet.instanceable=true",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=" + UserCSVImportPortletKeys.UserCSVImport,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user"
	},
	service = Portlet.class
)
public class UserCSVImportPortlet extends MVCPortlet {
	
private Log log = LogFactoryUtil.getLog(UserCSVImportPortlet.class);
	
	public void userCSVDataUpload(ActionRequest actionRequest, ActionResponse actionResponse)
			throws IOException, PortletException {
		
		log.info("******************** User CSV Data Upload ***************************");
		
		String filePath = "D:\\CSV\\Import\\User.csv";
		try (FileOutputStream fOut = new FileOutputStream(filePath);) {
			UploadPortletRequest uploadRequest = PortalUtil.getUploadPortletRequest(actionRequest);
			InputStream is = uploadRequest.getFileAsStream("csvDataFile");
			int i;
			while ((i = is.read()) != -1) {
				fOut.write(i);
			}
			
			File csvFile = new File(filePath);
			log.info("CSV File ===> " + csvFile);
			
			if (Validator.isNotNull(csvFile)) {
				if (csvFile.getName().contains(".csv")) {
					JSONArray csvDataArray = UserCSVImportUtil.readCSVFile(csvFile);
					UserCSVImportUtil.addUserToDatabase(csvDataArray, actionRequest);
					
				} else {
					log.error("Uploaded File is not CSV file.Your file name is ----> " + csvFile.getName());
				}

			}			
		} catch (Exception e) {
			log.error("Exception in CSV File Reading Process :: ", e);
		}
	}
}