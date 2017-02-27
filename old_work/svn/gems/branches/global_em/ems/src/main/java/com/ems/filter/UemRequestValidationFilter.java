package com.ems.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import com.ems.action.SpringContext;
import com.ems.model.SystemConfiguration;
import com.ems.model.User;
import com.ems.security.EmsAuthenticatedUser;
import com.ems.service.SystemConfigurationManager;
import com.ems.service.UserManager;
import com.ems.types.RoleType;
import com.ems.types.UserStatus;
import com.ems.utils.ArgumentUtils;


/**
 * @author SAMEER SURJIKAR
 * 
 */
public class UemRequestValidationFilter implements Filter {
	private static final String DIGEST_ALGO = "SHA-1";
	private static final Logger logger = Logger.getLogger("WSLogger");
	private static final String UEM_AUTHORIZATION = "Authorization";
	private static final String UEM_API_KEY="ApiKey";
	private static final String TIME_STAMP="ts";  // timestamp used as salt to digest key.
	private SystemConfigurationManager systemConfigurationManager = null;

	public void init(FilterConfig filterConfig) throws ServletException {
		systemConfigurationManager = (SystemConfigurationManager) SpringContext
				.getBean("systemConfigurationManager");
	}

	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		String sResponse = "";
		short status = 0;
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		// generate session if one doesn't exist and invalidate after
		// prossessing is done.
		HttpSession session = request.getSession();
		try {
			if (request.getHeader(UEM_AUTHORIZATION) != null
					&& !request.getHeader(UEM_AUTHORIZATION).isEmpty()) {
				if (validated(request.getHeader(UEM_AUTHORIZATION),
						request.getHeader(UEM_API_KEY),
						request.getHeader(TIME_STAMP))) {
					logger.info("REQ "
							+ request.getPathInfo()
							+ ((request.getQueryString() != null) ? ("?" + request
									.getQueryString()) : ""));

					UserManager userManager = (UserManager) SpringContext
							.getBean("userManager");
					User user = new User();
					user.setId(1l);
					user.setEmail("admin");
					user.setRole(userManager
							.getRoleFromRoleList(RoleType.Admin));
					user.setStatus(UserStatus.ACTIVE);
					EmsAuthenticatedUser authenticatedUser = new EmsAuthenticatedUser(
							user);
					UsernamePasswordAuthenticationToken authenticated = new UsernamePasswordAuthenticationToken(
							authenticatedUser, null,
							authenticatedUser.getAuthorities());
					SecurityContextHolder.getContext().setAuthentication(
							authenticated);
					if (session != null) {
						session.setAttribute(
								HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
								SecurityContextHolder.getContext());
					}
					filterChain.doFilter(request, response);
				} else {
					status++;
					sResponse = "Invalid Credentials";
				}
			} else {
				status++;
				sResponse = "Missing Authorization Token";
			}
		} catch (IllegalArgumentException lae) {
			// lae.printStackTrace();
			status++;
			sResponse = "Illegal Arguments";
			logger.error(sResponse, lae);
		} catch (AuthenticationException ae) {
			// ae.printStackTrace();
			status++;
			sResponse = "Invalid Credentials.";
			logger.error(sResponse, ae);
		} catch (ArrayIndexOutOfBoundsException e) {
			// e.printStackTrace();
			status++;
			sResponse = "Error processing request";
			logger.error(sResponse, e);
		} catch (Exception ae) {
			// ae.printStackTrace();
			status++;
			sResponse = "Error processing request.";
			logger.error(sResponse, ae);
		} finally {
			if (session != null) {
				// This will go away with having multiple http configuration,
				// one dedicated for API calls which are stateless (starting
				// SS-3.1)
				// For now just invalidate this session
				session.invalidate();
			}
			if (status != 0) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				sResponse = getMessage(status++, sResponse);
				logger.debug("Response: " + sResponse);
				response.setContentLength(sResponse.length());
				PrintWriter pw = response.getWriter();
				pw.print(sResponse);
			}
		}
	}

	public boolean validated(String key, String apiKey, String timeStamp) {
		boolean valid = false;
		try {

			String UEM_HSK_KEY = "admin";
			String UEM_HSK_SECERET_KEY = "admin";
			if (systemConfigurationManager != null) {
				SystemConfiguration oConfig = systemConfigurationManager
						.loadConfigByName("uem.apikey");
				if (oConfig != null && oConfig.getValue() != null
						&& !oConfig.getValue().equals("")) {
					UEM_HSK_KEY = oConfig.getValue();
				}
				if (apiKey != null && !apiKey.isEmpty()
						&& apiKey.equals(UEM_HSK_KEY)) {
					SystemConfiguration oConfigSec = systemConfigurationManager
							.loadConfigByName("uem.secretkey");
					if (oConfigSec != null && oConfigSec.getValue() != null
							&& !oConfigSec.getValue().equals("")) {
						UEM_HSK_SECERET_KEY = oConfigSec.getValue();
					}

					// create digest and compare it with already present key in
					String emKey = getSaltBasedDigest(UEM_HSK_KEY,
							UEM_HSK_SECERET_KEY, timeStamp, DIGEST_ALGO);
					if (key != null && !key.isEmpty() && key.equals(emKey)) {
						valid = true;
					}
				}else
				{
					valid=false;
				}
			}
		} catch (Exception e) {
			logger.fatal("Fatal error while validaing key. Contact Admin.", e);
			valid = false;
		}
		return valid;
	}

	@Override
	public void destroy() {

	}

	private String getMessage(Short status, String sMsg) {
		String sResponse = "";
		try {
			JSONObject resObj = new JSONObject();
			resObj.put("status", status);
			resObj.put("msg", sMsg);
			sResponse = resObj.toString();
		} catch (JSONException e) {
		}
		return sResponse;
	}
	
	public static String getSaltBasedDigest(String key, String secret,
			String salt, String algo) throws Exception  {
		if (ArgumentUtils.isNullOrEmpty(key)
				|| ArgumentUtils.isNullOrEmpty(secret)
				|| ArgumentUtils.isNullOrEmpty(salt)
				|| ArgumentUtils.isNullOrEmpty(algo)) {
			throw new NullPointerException(
					"All parameters required to create digest.");
		}
		MessageDigest md =null;
		try{
		 md = MessageDigest.getInstance(algo);
		}catch(NoSuchAlgorithmException e){
			logger.warn("No Such algorithm exception. falling over to default algo for creating digest which is SHA-1", e);
			 md = MessageDigest.getInstance(DIGEST_ALGO);
		}
		md.reset();
		byte[] keyByte = (key+secret+salt).getBytes(Charset.forName("utf-8")); 
		md.update(keyByte);
		byte[] digest = md.digest();
		return getByteString(digest);
	}
	private static String getByteString(byte[] bytes) {
		StringBuffer oBuffer = new StringBuffer();
		int noOfBytes = bytes.length;
		for (int i = 0; i < noOfBytes; i++) {
			 oBuffer.append(String.format("%02x", bytes[i]));
		}
		return oBuffer.toString();
	}

}
