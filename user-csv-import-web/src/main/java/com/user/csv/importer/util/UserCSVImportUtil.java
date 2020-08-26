package com.user.csv.importer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.portlet.ActionRequest;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.user.csv.importer.bean.UsersBean;

public class UserCSVImportUtil {

private static Log log = LogFactoryUtil.getLog(UserCSVImportUtil.class);
	
	public static JSONArray readCSVFile(File csvfile) throws IOException {

		JSONArray csvData = JSONFactoryUtil.createJSONArray();
		
		try (InputStream targetStream = new FileInputStream(csvfile);
				InputStreamReader isr = new InputStreamReader(targetStream);) {

			CSVFormat csvFormat = CSVFormat.newFormat(',').withIgnoreEmptyLines().withTrim(true);
			
			CSVParser csvParser = csvFormat.parse(isr);
			if (csvParser != null) {
				JSONObject rowObject = null;
				for (CSVRecord record : csvParser) {
					rowObject = JSONFactoryUtil.createJSONObject();
					for (int j = 0; j < record.size(); j++) {
						rowObject.put(String.valueOf(j), record.get(j));
					}
					csvData.put(rowObject);
				}
				log.info("CSV Data : " + csvData.toString());
			}

		} catch (IOException e) {
			log.error("Exception while reading file : ", e);
			throw e;
		}

		return csvData;
	}
	
	public static void addUserToDatabase(JSONArray csvDataArray, ActionRequest actionRequest)
			throws PortalException, java.text.ParseException {
		ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
		String portalUrl = PortalUtil.getPortalURL(themeDisplay);
		
		log.info("******** Inside  addUserToLiferayDB() ********** ");
		log.info("Data Array inside addUserToLiferayDb ===> " + csvDataArray);
		
		long companyId = PortalUtil.getDefaultCompanyId();
		User adminUser = UserLocalServiceUtil.getUserByEmailAddress(PortalUtil.getDefaultCompanyId(), "test@liferay.com");
		SimpleDateFormat dateFormate = new SimpleDateFormat("dd-MM-yyyy");
		
		log.info("Company Id ===> " + companyId);
		log.info("Admin User ===> " + adminUser);
		
		if (Validator.isNotNull(csvDataArray)) {
			log.info("Data Array Length ===> " + csvDataArray.length());
			for (int i = 1; i < csvDataArray.length(); i++) {
				JSONObject jsonObject = csvDataArray.getJSONObject(i);
				log.info("Json Object ===> " + jsonObject);
				
				UsersBean csvToBean = new UsersBean();
				csvToBean.setEmployeeId(jsonObject.getString("0"));
				
				if (Validator.isNotNull(jsonObject.getString("1"))) {
					csvToBean.setEmailId(jsonObject.getString("1").trim());
					log.info("Email Address ===> " + jsonObject.getString("1").trim());
				}
				
				if (jsonObject.getString("2").contains(StringPool.SPACE)) {
					
					String[] firstNameWithSpace = jsonObject.getString("2").split(StringPool.SPACE);
					csvToBean.setFirstName(firstNameWithSpace[0]);
					csvToBean.setMiddleName(firstNameWithSpace[1]);
					log.info("First Name with Space ===>  " + jsonObject.getString("2"));

				} else {
					csvToBean.setFirstName(jsonObject.getString("2"));
					log.info("First Name without Space ===> " + jsonObject.getString("2"));
				}
				
				if (jsonObject.getString("3").contains(StringPool.SPACE)) {
					String[] lastNameWithSpace = jsonObject.getString("3").split(StringPool.SPACE);
					csvToBean.setLastName(lastNameWithSpace[0]);
					csvToBean.setMiddleName(lastNameWithSpace[1]);
					log.info("Last Name with Space ===> " + jsonObject.getString("3"));
				} else {
					csvToBean.setLastName(jsonObject.getString("3"));
					log.info("Last Name without Space ===> " + jsonObject.getString("3"));
				}
				
				Date dob = null;

				if (jsonObject.getString("4").contains(StringPool.FORWARD_SLASH)) {
					dob = dateFormate.parse(removeHyphenFromDate(jsonObject.getString("4")));
					log.info("Date of Birth with forward slash ===> " + dob);
				} else {
					dob = dateFormate.parse(jsonObject.getString("4"));
					log.info("Date of Birth without forward slash ===> " + dob);
				}
				
				csvToBean.setDob(dob);
				csvToBean.setMobileNumber(jsonObject.getString("5"));
				csvToBean.setPortalUrl(portalUrl);
				
				log.info("User Bean" + csvToBean);
	
				validateUser(companyId, adminUser, csvToBean);
				
			}
		}
	}
	
	private static void validateUser(long companyId, User adminUser, UsersBean usersBean)
			throws PortalException {
		User user = null;
		
		boolean userExists = false;
		if (Validator.isNotNull(usersBean.getMobileNumber())) {
			userExists = isUserExists(companyId, usersBean, userExists);
		}

		if (!userExists) {
			user = addCSVUser(companyId, adminUser, usersBean);
		} else {
			log.info("User already exists in system." + usersBean.toString());
		}
	}
	
	private static User addCSVUser(long companyId, User adminUser, UsersBean usersBean) {

		log.info("******* In add User *********");
		User user = null;

		log.info("Mobile Number ===>" + usersBean.getMobileNumber());
		log.info("DOB ===> " + usersBean.getDob());
		
		if (Validator.isNumber(usersBean.getMobileNumber())&& Validator.isNotNull(usersBean.getDob())) {
			try {
				if (Validator.isNull(user)) {
					String password = "test";
					user = UserLocalServiceUtil.addUser(adminUser.getUserId(), companyId, false, password, password,
							false, usersBean.getMobileNumber().trim(), usersBean.getEmailId(), 0L,
							StringPool.BLANK, adminUser.getLocale(), usersBean.getFirstName(),
							usersBean.getMiddleName(), usersBean.getLastName(), 0L, 0L, true,
							usersBean.getDob().getMonth(), usersBean.getDob().getDate(),
							usersBean.getDob().getYear(), StringPool.BLANK, null, null, null, null, false,
							new ServiceContext());

					log.info("User added Successfully in database : " + usersBean.getEmailId());
				}

			} catch (PortalException e) {
				log.error("Error while adding user in database : " + e.getStackTrace());
			}

		} else {
			log.info("Issue while user Import " + usersBean.getFirstName() + "-> Last Name "
					+ usersBean.getLastName() + " -> Email " + usersBean.getEmailId() + " -> Mobile "
					+ usersBean.getMobileNumber());
		}
		return user;
	}
	
	private static boolean isUserExists(long companyId, UsersBean usersBean, boolean userExists) {
	
		log.info("MobileNumber : " + usersBean.getMobileNumber());
		try {
			User userScreenName = UserLocalServiceUtil.getUserByScreenName(companyId,usersBean.getMobileNumber());
			log.info("User Screen Name ===> " + userScreenName);
			if (userScreenName != null) {
				userExists = true;
				log.info(" User already Exists : " + userExists);
			}
		} catch (PortalException noUser) {
			log.info("User not exist with this screen Name : " + usersBean.getMobileNumber());
		}

		return userExists;
	}
	
	public static String removeHyphenFromDate(String date) {
		if (date.contains("-")) {
			return date;
		} else {
			return date.replace("/", "-");
		}

	}
}
