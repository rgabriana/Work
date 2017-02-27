/**
 * LoginController acts as a bridge between GEMS 2.0 and GEMS 3.0 for mobile applications
 */
package com.ems.mvc.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ems.model.User;
import com.ems.security.EmsAuthenticatedUser;
import com.ems.security.EmsAuthenticationContext;
import com.ems.service.EnergyConsumptionManager;
import com.ems.service.UserManager;
import com.ems.types.RoleType;
import com.ems.types.UserStatus;
import com.ems.utils.VersionUtil;


/**
 * @author SAMEER SURJIKAR
 * 
 */
@Controller
public class LoginController {
	@Resource(name = "authenticationManager")
	private AuthenticationManager authenticationManager;
	@Resource
	UserManager userManager;
	@Resource(name = "energyConsumptionManager")
	private EnergyConsumptionManager energyConsumptionManager;

	/**
	 * 
	 * @param body
	 *            request
	 *            "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>"
	 *            "<root><request><messageType>1</messageType><body><loginRequest>"
	 *            "<userName>admin</userName><password>admin</password></loginRequest></body>"
	 *            "</request></root>"
	 * @param request
	 *            context
	 * @param response
	 * @return XML response
	 */
	@RequestMapping("/wsaction.action")
	@ResponseBody
	public String doLogin(@RequestBody String body, HttpServletRequest request,
			HttpServletResponse response) {
		StringBuilder loginResponse = new StringBuilder();
		Boolean byApiKeyAuthentication = true;
		if (body != null) {
			String sUsername = "";
			String sPassword = "";
			String appIp = "";
			Pattern userRegex = Pattern.compile(
					"<.*userName>(.*)</.*userName>", Pattern.CASE_INSENSITIVE);
			Matcher userMatcher = userRegex.matcher(body);
			if (userMatcher.find()) {
				sUsername = userMatcher.group(1);
				sUsername = sUsername.trim();
				byApiKeyAuthentication = false;
			}
			Pattern passRegex = Pattern.compile(
					"<.*password>(.*)</.*password>", Pattern.CASE_INSENSITIVE);
			Matcher passMatcher = passRegex.matcher(body);
			while (passMatcher.find()) {
				sPassword = passMatcher.group(1);
				sPassword = sPassword.trim();
			}

			Pattern apiRegex = Pattern.compile("<.*appIp>(.*)</.*appIp>",
					Pattern.CASE_INSENSITIVE);
			Matcher apiMatcher = apiRegex.matcher(body);
			if (apiMatcher.find()) {
				appIp = apiMatcher.group(1);
				appIp = appIp.trim();
				byApiKeyAuthentication = true;
			}

			// For Username/Password Key based authentication
			if (!byApiKeyAuthentication) {
				final UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
						sUsername, sPassword);
				// generate session if one doesn't exist
				request.getSession();

				String sMessage = "success";
				int iResult = 1; // success, 0: failure
				authRequest.setDetails(new WebAuthenticationDetails(request));
				try {

					Authentication authentication = authenticationManager
							.authenticate(authRequest);
					SecurityContextHolder.getContext().setAuthentication(
							authentication);
				} catch (IllegalArgumentException lae) {
					sMessage = "failed";
					iResult = 0;
				} catch (AuthenticationException ae) {
					sMessage = "failed";
					iResult = 0;
				} catch (Exception ae) {
					sMessage = "failed";
					iResult = 0;
				}
				EmsAuthenticationContext eAC = new EmsAuthenticationContext();
				loginResponse
						.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
						.append("<root xmlns:enl=\"http://www.enlightedinc.com/schema\">")
						.append("<enl:response>")
						.append("<enl:version>")
						.append(VersionUtil.getAppVersion(request.getSession()
								.getServletContext())).append("</enl:version>")
						.append("<enl:messageType>1</enl:messageType>")
						.append("<enl:body>").append("<enl:loginResponse>")
						.append("<enl:result>").append(iResult)
						.append("</enl:result>").append("<enl:sessionId>")
						.append(request.getSession().getId())
						.append("</enl:sessionId>").append("<enl:userId>")
						.append(eAC.getUserId()).append("</enl:userId>")
						.append("<enl:message>").append(sMessage)
						.append("</enl:message>")
						.append("</enl:loginResponse>").append("</enl:body>")
						.append("</enl:response>").append("</root>");
			}
			// For Api Key based authentication
			else {

				// generate session if one doesn't exist
				request.getSession();

				String sMessage = "success";
				int iResult = 1; // success, 0: failure

				try {
					if (!evaluateIsLocalHost(request)) {
						throw new Exception();
					}
					Authentication authenticated;
					User user = new User();
					user.setEmail("Application From" + appIp);
					user.setRole(userManager.getRole(RoleType.FacilitiesAdmin));
					user.setStatus(UserStatus.ACTIVE);
					EmsAuthenticatedUser authenticatedUser = new EmsAuthenticatedUser(
							user);
					authenticated = new UsernamePasswordAuthenticationToken(
							authenticatedUser, null,
							authenticatedUser.getAuthorities());
					SecurityContextHolder.getContext().setAuthentication(
							authenticated);
				} catch (IllegalArgumentException lae) {
					sMessage = "failed";
					iResult = 0;
				} catch (AuthenticationException ae) {
					sMessage = "failed";
					iResult = 0;
				} catch (Exception ae) {
					sMessage = "failed";
					iResult = 0;
				}
				EmsAuthenticationContext eAC = new EmsAuthenticationContext();
				String ip ="" ;
				try {
					InetAddress thisIp =InetAddress.getLocalHost();
					ip = thisIp.getHostAddress() ;
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// Used for communication and sync up with master gems
				String timeStamp = null ;
				Date lastCommunicationDate = energyConsumptionManager.loadFirstRecordDate();
				if(lastCommunicationDate==null)
				{
					lastCommunicationDate=Calendar.getInstance().getTime();
					timeStamp =  lastCommunicationDate.toString() ;
				}
				else
				timeStamp =  lastCommunicationDate.toString() ;
				
				loginResponse
						.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
						.append("<root xmlns:enl=\"http://www.enlightedinc.com/schema\">")
						.append("<enl:response>")
						.append("<enl:version>")
						.append(VersionUtil.getAppVersion(request.getSession()
								.getServletContext())).append("</enl:version>")
						.append("<enl:messageType>1</enl:messageType>")
						.append("<enl:body>").append("<enl:loginResponse>")
						.append("<enl:result>").append(iResult)
						.append("</enl:result>").append("<enl:sessionId>")
						.append(request.getSession().getId())
						.append("</enl:sessionId>").append("<enl:lastCommunicationTimeStamp>")
						.append(timeStamp)
						.append("</enl:lastCommunicationTimeStamp>").append("<enl:message>")
						.append(sMessage).append("</enl:message>").append("<enl:gemIp>")
						.append(ip)
						.append("</enl:gemIp>")
						.append("</enl:loginResponse>").append("</enl:body>")
						.append("</enl:response>").append("</root>");
			}
		}

		return loginResponse.toString();

	}

	private Boolean evaluateIsLocalHost(HttpServletRequest request) {
		if (request.getRemoteAddr().equals("127.0.0.1"))
			return true;
		else
			return false;
	}
}