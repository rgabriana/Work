/**
 * LoginController acts as a bridge between GEMS 2.0 and GEMS 3.0 for mobile applications
 */
package com.emsdashboard.mvc.controller;

import java.util.Calendar;
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

import com.emsdashboard.model.GemsServer;
import com.emsdashboard.model.User;
import com.emsdashboard.security.EmsAuthenticationContext;
import com.emsdashboard.security.EmsDashBoardAuthenticatedUser;
import com.emsdashboard.security.EmsDashboardauthenticationProvider;
import com.emsdashboard.service.GemsManager;
import com.emsdashboard.types.Status;

/**
 * @author yogesh
 * 
 */
@Controller
public class LoginController {
	@Resource(name = "authenticationManager")
	private AuthenticationManager authenticationManager;
	@Resource(name = "gemsManager")
	private GemsManager gemsManager;
	private Authentication authenticate;

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
			String apiKey = "";
			String gemsIp = "";
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

			Pattern apiRegex = Pattern.compile("<.*apiKey>(.*)</.*apiKey>",
					Pattern.CASE_INSENSITIVE);
			Matcher apiMatcher = apiRegex.matcher(body);
			if (apiMatcher.find()) {
				apiKey = apiMatcher.group(1);
				apiKey = apiKey.trim();
				byApiKeyAuthentication = true;
			}
			Pattern ipRegex = Pattern.compile("<.*gemsIp>(.*)</.*gemsIp>",
					Pattern.CASE_INSENSITIVE);
			Matcher ipMatcher = ipRegex.matcher(body);
			while (ipMatcher.find()) {
				gemsIp = ipMatcher.group(1);
				gemsIp = gemsIp.trim();
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
						.append("<enl:messageType>1</enl:messageType>")
						.append("<enl:body>").append("<enl:loginResponse>")
						.append("<enl:result>").append(iResult)
						.append("</enl:result>").append("<enl:sessionId>")
						.append(request.getSession().getId())
						.append("</enl:sessionId>").append("<enl:timeStamp>")
						.append(Calendar.getInstance())
						.append("</enl:timeStamp>").append("<enl:message>")
						.append(sMessage).append("</enl:message>")
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
					if (!evaluateApiKey(apiKey, gemsIp)) {
						throw new Exception();
					}
					Authentication authenticated;
					User user = new User();
					user.setEmail("APPlication From" + gemsIp);
					user.setStatus(Status.A);
					EmsDashBoardAuthenticatedUser authenticatedUser = new EmsDashBoardAuthenticatedUser(
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
				loginResponse
						.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
						.append("<root xmlns:enl=\"http://www.enlightedinc.com/schema\">")
						.append("<enl:response>")
						.append("<enl:messageType>1</enl:messageType>")
						.append("<enl:body>").append("<enl:loginResponse>")
						.append("<enl:result>").append(iResult)
						.append("</enl:result>").append("<enl:sessionId>")
						.append(request.getSession().getId())
						.append("</enl:sessionId>").append("<enl:timeStamp>")
						.append(Calendar.getInstance().getTime())
						.append("</enl:timeStamp>").append("<enl:message>")
						.append(sMessage).append("</enl:message>")
						.append("</enl:loginResponse>").append("</enl:body>")
						.append("</enl:response>").append("</root>");
			}
		}

		return loginResponse.toString();

	}

	private Boolean evaluateApiKey(String key, String gemIp) {
		GemsServer serverData = gemsManager.loadGEMSByGemsIp(gemIp);
		if(serverData!=null)
		{
			return serverData.getApiKey().equals(key) ;
		}
		else   // Auto discovery of gems server 
		{
			GemsServer newGemsServer = new GemsServer() ;
			newGemsServer.setApiKey(key);
			newGemsServer.setGemsIpAddress(gemIp) ;
			newGemsServer.setName("Gems" + gemIp) ;
			newGemsServer.setPort(443l);
			try{
			gemsManager.saveGEMSDataWithApiKey(newGemsServer);}
			catch(Exception e)
			{
				return false ;
			}
			return true ;
		}
		
	}
}
