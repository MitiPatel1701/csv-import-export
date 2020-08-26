package com.user.csv.export.portlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.PortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.user.csv.export.constants.UserCSVExportPortletKeys;

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
		"javax.portlet.name=" + UserCSVExportPortletKeys.UserCSVExport,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user"
	},
	service = Portlet.class
)
public class UserCSVExportPortlet extends MVCPortlet {
	
	private static Log log = LogFactoryUtil.getLog(UserCSVExportPortlet.class);
	
	public static String[] columnNames = { "UserId","EmailAddress", "FirstName", "LastName", "Birth Date" };
	public static final String COMMA = ",";
	public static final String FILENAME = "EmployeeData.csv";
	public static final String DD_MM_YYYY = "dd-MM-yyyy";

	

	@Override
	public void render(RenderRequest renderRequest, RenderResponse renderResponse)
			throws IOException, PortletException {
		log.info("******** In Render **********");
		List<User> usersList = UserLocalServiceUtil.getUsers(0,UserLocalServiceUtil.getUsersCount());
		renderRequest.setAttribute("usersList", usersList);
		super.render(renderRequest, renderResponse);
	}

	@Override
	public void serveResource(ResourceRequest resourceRequest,ResourceResponse resourceResponse) {

		log.info(" **** In Serve Resource Method *****");
		String url = ParamUtil.getString(resourceRequest, "export");

		try {
			if (url.equals("exportCSV")) {
				
				StringBundler sb = new StringBundler();
				for (String columnName : columnNames) {
					sb.append(getCSVFormattedValue(String.valueOf(columnName)));
					sb.append(COMMA);
				}
				sb.setIndex(sb.index() - 1);
				sb.append(CharPool.NEW_LINE);
				List<User> usersList = UserLocalServiceUtil.getUsers(0,UserLocalServiceUtil.getUsersCount());
				
				usersList.forEach(user -> {
					String stringDate;
					try {
					stringDate = getDateToString(user);
					sb.append(getCSVFormattedValue(String.valueOf(user.getUserId())));
					sb.append(COMMA);
					sb.append(getCSVFormattedValue(String.valueOf(user.getEmailAddress())));
					sb.append(COMMA);
					sb.append(getCSVFormattedValue(String.valueOf(user.getFirstName())));
					sb.append(COMMA);
					sb.append(getCSVFormattedValue(String.valueOf(user.getLastName())));
					sb.append(COMMA);
					sb.append(getCSVFormattedValue(String.valueOf(stringDate)));
					sb.append(COMMA);
					sb.setIndex(sb.index() - 1);
					sb.append(CharPool.NEW_LINE);
					} catch (PortalException e) {
						log.error("Error while export data : " + e);;
					}
				});

				byte[] bytes = sb.toString().getBytes();
				String contentType = ContentTypes.APPLICATION_TEXT;
				PortletResponseUtil.sendFile(resourceRequest, resourceResponse,FILENAME, bytes, contentType);
			}
		} catch (Exception e) {
			log.error("Exception While export CSV file : ", e);
		}
	}

	private String getDateToString(User user) throws PortalException {
		Date birthDate = user.getBirthday();
		SimpleDateFormat formatter = new SimpleDateFormat(DD_MM_YYYY);
		String stringDate= formatter.format(birthDate);
		return stringDate;
	}
		
	private String getCSVFormattedValue(String value) {
		StringBundler sb = new StringBundler(3);
		sb.append(CharPool.QUOTE);
		sb.append(StringUtil.replace(value, CharPool.QUOTE,StringPool.DOUBLE_QUOTE));
		sb.append(CharPool.QUOTE);
		return sb.toString();
	}
}