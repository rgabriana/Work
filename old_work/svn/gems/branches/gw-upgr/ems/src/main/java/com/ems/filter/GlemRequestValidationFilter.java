package com.ems.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
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
public class GlemRequestValidationFilter implements Filter {
	private static final String DIGEST_ALGO = "SHA-1";
	private static final Logger logger = Logger.getLogger("WSLogger");
	private static final String GLEM_AUTHORIZATION = "Authorization";
	private static final String GLEM_API_KEY="ApiKey";
	private static final String TIME_STAMP="ts";  // timestamp used as salt to digest key.
	private SystemConfigurationManager systemConfigurationManager = null;

	private class ReplayDTO{
		private String path="";
		private Long lastApiCalledTime=0l;
	}
	private static final ConcurrentHashMap<String, ReplayDTO> replayCheckMap = new ConcurrentHashMap<String, ReplayDTO>(); 
	
	private static final String AUTH_TOKEN = "AuthenticationToken";
	private static final String USER_ID = "UserId";
	
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
		boolean isUserIdExistsInReq = false;
		try {
			final String userId = request.getHeader(USER_ID);
			final String ts = request.getHeader(TIME_STAMP);
			final String authToken = request.getHeader(AUTH_TOKEN);
			final UserManager userManager = (UserManager) SpringContext.getBean("userManager");
			if (!StringUtils.isEmpty(userId) && !StringUtils.isEmpty(ts) && !StringUtils.isEmpty(authToken)){
				//New API logic access
				isUserIdExistsInReq = true;
				handleRequest(response, userId, ts,
						authToken, userManager, filterChain, request);
				return;
			}//Backward portability
			else if (request.getHeader(GLEM_AUTHORIZATION) != null
					&& !request.getHeader(GLEM_AUTHORIZATION).isEmpty()) {
				if (validated(request.getHeader(GLEM_AUTHORIZATION),
						request.getHeader(GLEM_API_KEY),
						request.getHeader(TIME_STAMP))) {
					logger.info("REQ "
							+ request.getPathInfo()
							+ ((request.getQueryString() != null) ? ("?" + request
									.getQueryString()) : ""));
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
			if (!isUserIdExistsInReq && status != 0) {
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

	/**
	 * Checks the authentication token using the secretKey specified against the user from db. Also rejects the request if it is replay attack
	 * If the response is OK then filterChain the request further.
	 * @param response
	 * @param userId
	 * @param ts
	 * @param authToken
	 * @param userManager
	 * @throws IOException
	 */
	public void handleRequest( HttpServletResponse response,
			final String userId, final String ts, final String authToken,
			final UserManager userManager, FilterChain filterChain, HttpServletRequest request) throws IOException {
		String sResponse;
		int res = HttpServletResponse.SC_UNAUTHORIZED;
		sResponse = "Invalid Credentials";
		try {
			final User user = userManager.loadUserByUserName(userId);
			if(user != null){
				String path = request.getRequestURI();
				if(!user.isActive() || user.isUserLocked()){
					sResponse = "User either inactive or locked";
				}else{
					final SystemConfiguration conf = systemConfigurationManager.loadConfigByName("default.replayAttackTimeInmillis");
					final long REPLAY_CHECK_TIME_LIMIT = Long.parseLong(conf.getValue());
					final String secretKey = user.getSecretKey();
					if(!StringUtils.isEmpty(secretKey)){
						final String genAuthToken = getSaltBasedDigest(userId, ts, secretKey, DIGEST_ALGO);
						if(!StringUtils.isEmpty(genAuthToken) && genAuthToken.equals(authToken)){
							//Retrieve the api called name
					        final String context = request.getContextPath();
					        path = path.replace(context, "");
							//Check api call time is allowed within limits
					        ReplayDTO dto = replayCheckMap.get(userId);
					        if(dto == null){
					        	dto = new ReplayDTO();
					        	dto.path = path;
					        	replayCheckMap.put(userId, dto);
					        }
					        
							final Long lastCallTime = dto.lastApiCalledTime;
							if(lastCallTime == null){
								res = HttpServletResponse.SC_OK;
							}else{
								try {
									final long currTime = Long.parseLong(ts);
									if(path.equals(dto.path) && (currTime - lastCallTime < REPLAY_CHECK_TIME_LIMIT) ){
										res = HttpServletResponse.SC_REQUEST_TIMEOUT;
										sResponse = "Replay Attack Error";
									}else{
										res = HttpServletResponse.SC_OK;
									}
								} catch (Exception e) {
									sResponse = "Error processing request.";
								}
							}
						}
					}
					//Update user last call time
					if(res == HttpServletResponse.SC_OK){
						replayCheckMap.get(userId).lastApiCalledTime =  System.currentTimeMillis();
						replayCheckMap.get(userId).path =  path;
						userManager.save(user);
						//Give access to this user of the api
						EmsAuthenticatedUser authenticatedUser = new EmsAuthenticatedUser(
								user);
						UsernamePasswordAuthenticationToken authenticated = new UsernamePasswordAuthenticationToken(
								authenticatedUser, null,
								authenticatedUser.getAuthorities());
						SecurityContextHolder.getContext().setAuthentication(
								authenticated);
						final HttpSession session = request.getSession();
						if (session != null) {
							session.setAttribute(
									HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
									SecurityContextHolder.getContext());
						}
						filterChain.doFilter(request, response);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error occured in authenticating the request:", e);
			sResponse = "Error processing request.";
		}finally{
			if (res != HttpServletResponse.SC_OK) {
				response.setStatus(res);
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				sResponse = getMessage((short)1, sResponse);
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
			String MOBILE_HSK_KEY = "admin";
			String MOBILE_HSK_SECERET_KEY = "admin";
			if (systemConfigurationManager != null) {
				SystemConfiguration oConfig = systemConfigurationManager
						.loadConfigByName("glem.apikey");
				if (oConfig != null && oConfig.getValue() != null
						&& !oConfig.getValue().equals("")) {
					UEM_HSK_KEY = oConfig.getValue();
				}
				if (apiKey != null && !apiKey.isEmpty() && apiKey.equals(UEM_HSK_KEY)) {
					SystemConfiguration oConfigSec = systemConfigurationManager
							.loadConfigByName("glem.secretkey");
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
				}
				
				if(!valid) {
					oConfig = systemConfigurationManager
							.loadConfigByName("mobile.apikey");
					if (oConfig != null && oConfig.getValue() != null
							&& !oConfig.getValue().equals("")) {
						MOBILE_HSK_KEY = oConfig.getValue();
					}
					if (apiKey != null && !apiKey.isEmpty() && apiKey.equals(MOBILE_HSK_KEY)) {
						SystemConfiguration oConfigSec = systemConfigurationManager
								.loadConfigByName("mobile.secretkey");
						if (oConfigSec != null && oConfigSec.getValue() != null
								&& !oConfigSec.getValue().equals("")) {
							MOBILE_HSK_SECERET_KEY = oConfigSec.getValue();
						}

						// create digest and compare it with already present key in
						String emKey = getSaltBasedDigest(MOBILE_HSK_KEY,
								MOBILE_HSK_SECERET_KEY, timeStamp, DIGEST_ALGO);
						if (key != null && !key.isEmpty() && key.equals(emKey)) {
							valid = true;
						}
					}
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
	
	/**
	 * IN unix you can generate this using following command
	 * echo -n key+secret+salt | sha1sum | awk '{print $1}'
	 * @param key
	 * @param secret
	 * @param salt
	 * @param algo
	 * @return
	 * @throws Exception
	 */
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
			logger.error("No Such algorithm exception. falling over to default algo for creating digest which is SHA-1", e);
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
