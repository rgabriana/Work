package com.ems.ws;

import java.io.File;
import java.util.Date;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;

import com.ems.filter.GlemRequestValidationFilter;
import com.ems.model.SystemConfiguration;
import com.ems.model.User;
import com.ems.server.util.ServerUtil;
import com.ems.service.SystemConfigurationManager;
import com.ems.service.UserManager;
import com.ems.types.UserStatus;
import com.ems.util.Constants;
import com.ems.utils.CommonUtils;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;

/**
 *
 * @author admin
 *
 */
@Ignore  // This test hangs
public class WSServiceTest extends AbstractEnlightedWSTest {



	@Resource(name = "userManager")
	private UserManager userManager;
	@Resource(name = "systemConfigurationManager")
	private SystemConfigurationManager systemConfigurationManager;

	@Test
	public void testBytes(){
		try {

			int motionGrpChecksum = ServerUtil.checksum(0, ServerUtil.intToByteArray(Integer.parseInt(new Integer("12000001").toString(), 16)));
			byte[] pkt = {0x00, 0x13};
			int chkSum = ServerUtil.extractShortFromByteArray(pkt, 0);

			System.out.println(motionGrpChecksum);
			System.out.println(chkSum);
		} catch (Exception e) {
			logger.error("***FAILED***", e);
			Assert.fail(e.getMessage());
		}
	}
	@Test
	public void testSecretkeyString(){

		try {

			final String key = GlemRequestValidationFilter.getSaltBasedDigest("sharad",
					"1436258793104", String.valueOf("EnlightedAuthKey"), Constants.SHA1_ALGO);
			Assert.assertEquals("0e2ed0dc910059af858fac5b6b464a652ee7aa18", key);

		} catch (Exception e) {
			logger.error("***FAILED***", e);
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void generateHeadersForApiAccess(){
		try {

			final String email = "sharad";
			final long time = (new Date()).getTime();
			final User user = userManager.loadUserByUserName(email);
			final String auth = generateAuthToken(email, time, user.getSecretKey());
			logger.info("AuthenticationToken:"+ auth);
			logger.info("ts:"+ time);
			logger.info("UserId:"+ email);
		} catch (Exception e) {
			logger.error("***FAILED***", e);
			Assert.fail(e.getMessage());
		}
	}

	public static String generateAuthToken(final String userName, final long time, final String key) throws Exception{
		final String authToken = GlemRequestValidationFilter.getSaltBasedDigest(userName, String.valueOf(time), key, Constants.SHA1_ALGO);
		return authToken;
	}
	@Test
	@Transactional
	public void testAPIAccessUsingAPIKeyDiffRole(){
		try {
			final String urlAccess = HOST_ADDR+"/ems/api/org/user/list/admin";
			//Comment out below code if urlAccess received is role based in the ws
//			String email = "sharad";
//			String key = "3b39dab8d725ddc896f7dce842ce494a71874a8d";
//			ClientResponse res = getAPIResponse(urlAccess, email, key);
//			if(res.getStatus() != ClientResponse.Status.OK.getStatusCode()){
//				final String err = res.getEntity(String.class);
//				logger.error(err+" Observed. Status is: "+ res.getStatus());
//				Assert.fail(err);
//			}
//			 email = "sharad_aud";
//			key = "9a5301cc3d73858e766959f9efe5dc4290a15705";
//			res = getAPIResponse(urlAccess, email, key);
//			if(res.getStatus() == ClientResponse.Status.OK.getStatusCode()){
//				final String err = res.getEntity(String.class);
//				logger.error(err+" Observed. Status is: "+ res.getStatus());
//				Assert.fail(err);
//			}
		} catch (Exception e) {
			logger.error("***FAILED***", e);
			Assert.fail(e.getMessage());
		}

	}

	public ClientResponse getAPIResponse(final String urlAccess, String email, String key)
			throws Exception {
		Builder wr = getWebResourceWithoutAuth(urlAccess, MediaType.TEXT_PLAIN);
		final Date dt = new Date();
		final String authToken = GlemRequestValidationFilter.getSaltBasedDigest(email, String.valueOf(dt.getTime()), key, Constants.SHA1_ALGO);

		wr.header("AuthenticationToken", authToken);
		wr.header("ts", String.valueOf(dt.getTime()));
		wr.header("UserId", email);
		ClientResponse res = wr.type(MediaType.TEXT_PLAIN).get(ClientResponse.class);
		return res;
	}


	@Test
	@Transactional
	public void testAPIAccessByUser(){
		try {
			final String urlAccess = HOST_ADDR+"/ems/api/org/user/genapikey";
			final String email = "sharad";
			final User user = userManager.loadUserByUserName(email);
			Assert.assertEquals(email, user.getEmail());
			Builder wr = getWebResourceWithoutAuth(urlAccess+"/"+email, MediaType.TEXT_PLAIN);
			final Date dt = new Date();

			//new api access
			newApiAccess(email, user, wr, dt);

			//Backward compatibility
			backwardCompatibilityAPIAccess(urlAccess, email, dt);

		} catch (Exception e) {
			logger.error("***FAILED***", e);
			Assert.fail(e.getMessage());
		}

	}

	@Test
	public void testReplayAttackOtherService(){
		try{
			final String urlAccess = HOST_ADDR+"/ems/api/org/user/genapikey";
			final String email = "sharad";
			User user = userManager.loadUserByUserName(email);
			Assert.assertEquals(email, user.getEmail());
			Builder wr = getWebResourceWithoutAuth(urlAccess+"/"+email, MediaType.TEXT_PLAIN);
			ClientResponse res = null;
			//Test replay attack different service
			Date dtOther = new Date();
			String authTokenOther = GlemRequestValidationFilter.getSaltBasedDigest(email, String.valueOf(dtOther.getTime()), user.getSecretKey(), Constants.SHA1_ALGO);
			wr.header("AuthenticationToken", authTokenOther);
			wr.header("ts", String.valueOf(dtOther.getTime()));
			wr.header("UserId", email);
			res = wr.type(MediaType.TEXT_PLAIN).get(ClientResponse.class);
			if(res.getStatus() != ClientResponse.Status.OK.getStatusCode()){
				Assert.fail("There should be access to api in this case but code is "+res.getStatus()+" Response is: "+res.getEntity(String.class) );
			}
			res = wr.type(MediaType.TEXT_PLAIN).get(ClientResponse.class);
			if(res.getStatus() == ClientResponse.Status.OK.getStatusCode()){
				Assert.fail("There should be no access to api in this case but code is "+res.getStatus());
			}

			user = userManager.loadUserByUserName(email);
			Assert.assertEquals(email, user.getEmail());
			Builder wrOther = getWebResourceWithoutAuth(HOST_ADDR+"/ems/api/org/user/list/admin", MediaType.TEXT_PLAIN);
			authTokenOther = GlemRequestValidationFilter.getSaltBasedDigest(email, String.valueOf(dtOther.getTime()), user.getSecretKey(), Constants.SHA1_ALGO);
			wrOther.header("AuthenticationToken", authTokenOther);
			wrOther.header("ts", String.valueOf(dtOther.getTime()));
			wrOther.header("UserId", email);
			res = wrOther.type(MediaType.TEXT_PLAIN).get(ClientResponse.class);
			if(res.getStatus() != ClientResponse.Status.OK.getStatusCode()){
				Assert.fail("There should be access to api in this case but code is "+res.getStatus() + " Response is: "+res.getEntity(String.class));
			}
			res = wrOther.type(MediaType.TEXT_PLAIN).get(ClientResponse.class);
			if(res.getStatus() == ClientResponse.Status.OK.getStatusCode()){
				Assert.fail("There should be no access to api in this case but code is "+res.getStatus() + " Response is: "+res.getEntity(String.class));
			}
			Thread.sleep(350);
			dtOther = new Date();
			authTokenOther = GlemRequestValidationFilter.getSaltBasedDigest(email, String.valueOf(dtOther.getTime()), user.getSecretKey(), Constants.SHA1_ALGO);
			wrOther.header("AuthenticationToken", authTokenOther);
			wrOther.header("ts", String.valueOf(dtOther.getTime()));
			wrOther.header("UserId", email);
			res = wrOther.type(MediaType.TEXT_PLAIN).get(ClientResponse.class);
			if(res.getStatus() != ClientResponse.Status.OK.getStatusCode()){
				Assert.fail("There should be no access to api in this case but code is "+res.getStatus() + " Response is: "+res.getEntity(String.class));
			}

		} catch (Exception e) {
			logger.error("***FAILED***", e);
			Assert.fail(e.getMessage());
		}

	}
	@Test
	@Transactional
	public void testAPIAccessByInactiveLockedUser(){
		try {
			final String urlAccess = HOST_ADDR+"/ems/api/org/user/genapikey";
			final String email = "sharad";
			final User user = userManager.loadUserByUserName(email);
			Assert.assertEquals(email, user.getEmail());
			user.setStatus(UserStatus.INACTIVE);
			userManager.save(user);

			Builder wr = getWebResourceWithoutAuth(urlAccess+"/"+email, MediaType.TEXT_PLAIN);
			final Date dt = new Date();

			final String authToken = GlemRequestValidationFilter.getSaltBasedDigest(email, String.valueOf(dt.getTime()), user.getSecretKey(), Constants.SHA1_ALGO);

			wr.header("AuthenticationToken", authToken);
			wr.header("ts", String.valueOf(dt.getTime()));
			wr.header("UserId", email);

			logger.info("AuthenticationToken:"+authToken);
			logger.info("ts:"+String.valueOf(dt.getTime()));
			logger.info("UserId:"+email);

			ClientResponse res = wr.type(MediaType.TEXT_PLAIN).get(ClientResponse.class);
			if(res.getStatus()== ClientResponse.Status.OK.getStatusCode()){
				final String err = res.getEntity(String.class);
				logger.error(err+" Observed. Status is: "+ res.getStatus());
				Assert.fail(err);
			}
			user.setStatus(UserStatus.ACTIVE);
			userManager.save(user);
		} catch (Exception e) {
			logger.error("***FAILED***", e);
			Assert.fail(e.getMessage());
		}finally{

		}

	}

	public void backwardCompatibilityAPIAccess(final String urlAccess,
			final String email, final Date dt) throws Exception {
		Builder wr;
		ClientResponse res;
		//Web-access
		String glemApiKey = getSystemConfigWithDefaultValuesUpdates("glem.apikey","23432ojkg3i42k4bi23b4i23b4").getValue();
		String glemSecretKey = getSystemConfigWithDefaultValuesUpdates("glem.secretkey","64564647564dfsgdfgdfgdf5456546").getValue();
		String authorization = GlemRequestValidationFilter.getSaltBasedDigest(glemApiKey,
				glemSecretKey, String.valueOf(dt.getTime()), Constants.SHA1_ALGO);
		wr = getWebResource(urlAccess+"/"+email, MediaType.TEXT_PLAIN);
		wr.header("Authorization", authorization);
		wr.header("ts", String.valueOf(dt.getTime()));
		wr.header("ApiKey", glemApiKey);
		res = wr.type(MediaType.TEXT_PLAIN).get(ClientResponse.class);
		if(res.getStatus() != ClientResponse.Status.OK.getStatusCode()){
			final String err = res.getEntity(String.class);
			logger.error(err+" Observed. Status is: "+ res.getStatus());
			Assert.fail(err);
		}
		wr.header("Authorization", authorization+"test");
		wr.header("ts", String.valueOf(dt.getTime()));
		wr.header("ApiKey", glemApiKey);
		res = wr.type(MediaType.TEXT_PLAIN).get(ClientResponse.class);
		if(res.getStatus() == ClientResponse.Status.OK.getStatusCode()){
			Assert.fail("Access is not allowed here");
		}
		//Mobile access only.
		glemApiKey = getSystemConfigWithDefaultValuesUpdates("mobile.apikey","343432ojkg435345i42k4bi23b4i23b4").getValue();
		glemSecretKey = getSystemConfigWithDefaultValuesUpdates("mobile.secretkey","45464564647564dfsgdfgdfgdf5456546").getValue();
		authorization = GlemRequestValidationFilter.getSaltBasedDigest(glemApiKey,
				glemSecretKey, String.valueOf(dt.getTime()), Constants.SHA1_ALGO);
		wr = getWebResource(urlAccess+"/"+email, MediaType.TEXT_PLAIN);
		wr.header("Authorization", authorization);
		wr.header("ts", String.valueOf(dt.getTime()));
		wr.header("ApiKey", glemApiKey);
		res = wr.type(MediaType.TEXT_PLAIN).get(ClientResponse.class);
		if(res.getStatus() != ClientResponse.Status.OK.getStatusCode()){
			final String err = res.getEntity(String.class);
			logger.error(err+" Observed. Status is: "+ res.getStatus());
			Assert.fail(err);
		}
		wr.header("Authorization", authorization+"test");
		wr.header("ts", String.valueOf(dt.getTime()));
		wr.header("ApiKey", glemApiKey);
		res = wr.type(MediaType.TEXT_PLAIN).get(ClientResponse.class);
		if(res.getStatus() == ClientResponse.Status.OK.getStatusCode()){
			Assert.fail("Access is not allowed here");
		}
	}

	public void newApiAccess(final String email, final User user, Builder wr,
			final Date dt) throws Exception {
		final String authToken = GlemRequestValidationFilter.getSaltBasedDigest(email, String.valueOf(dt.getTime()), user.getSecretKey(), Constants.SHA1_ALGO);

		wr.header("AuthenticationToken", authToken);
		wr.header("ts", String.valueOf(dt.getTime()));
		wr.header("UserId", email);

		logger.info("AuthenticationToken:"+authToken);
		logger.info("ts:"+String.valueOf(dt.getTime()));
		logger.info("UserId:"+email);

		ClientResponse res = wr.type(MediaType.TEXT_PLAIN).get(ClientResponse.class);
		if(res.getStatus() != ClientResponse.Status.OK.getStatusCode()){
			final String err = res.getEntity(String.class);
			logger.error(err+" Observed. Status is: "+ res.getStatus());
			Assert.fail(err);
		}

		final String secretKey = res.getEntity(String.class);
		logger.info("secretKey:"+secretKey);

		//Test replay attack
		for(int i = 0; i < 3 ;i++){
			res = wr.type(MediaType.TEXT_PLAIN).get(ClientResponse.class);
		}

		if(res.getStatus() == ClientResponse.Status.OK.getStatusCode()){
			Assert.fail("There should be no access to api in this case");
		}

		//Test different token
		wr.header("AuthenticationToken", authToken+"test");
		wr.header("ts", String.valueOf(dt.getTime()));
		wr.header("UserId", email);
		res = wr.type(MediaType.TEXT_PLAIN).get(ClientResponse.class);
		if(res.getStatus() == ClientResponse.Status.OK.getStatusCode()){
			Assert.fail("There should be no access to api in this case");
		}
	}

	private SystemConfiguration getSystemConfigWithDefaultValuesUpdates(final String key, final String newVal){
		SystemConfiguration oConfig = systemConfigurationManager
				.loadConfigByName(key);
		String glemApiKey = newVal;
		if(oConfig == null){
			Assert.fail("No key exists in system_configuration named glem.apikey");
		}
		if (StringUtils.isEmpty(oConfig.getValue()) || StringUtils.isEmpty(oConfig.getValue().trim())){
			//save the new key to db
			oConfig.setValue(glemApiKey);
			systemConfigurationManager.save(oConfig);
		}
		return oConfig;
	}

	@Test
	public void testisParamValueAllowed(){
		try {
			SystemConfiguration validationFlagConfig = systemConfigurationManager.loadConfigByName("flag.ems.apply.validation");
			boolean isApplyValidation = true;
			if (validationFlagConfig != null) {
				final String isApplyValidationStr = validationFlagConfig.getValue();
				isApplyValidation = StringUtils.isEmpty(isApplyValidationStr)?true:isApplyValidationStr.trim().equalsIgnoreCase("true")?true:false;
			}
			checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p1", "v1",Response.Status.OK.getStatusCode());
			checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p1", "v5",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
			checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p1", "V5",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
			checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p1", "v4",Response.Status.OK.getStatusCode());
			checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p2", 3d,Response.Status.OK.getStatusCode()); //3,4.0,1.12,2.23333456
			checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p2", 4.0d,Response.Status.OK.getStatusCode()); //3,4.0,1.12,2.23333456
			checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p2", 1.12d,Response.Status.OK.getStatusCode()); //3,4.0,1.12,2.23333456
			checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p2", 2.23333456d,Response.Status.OK.getStatusCode()); //3,4.0,1.12,2.23333456
			checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p2", 5d,!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
			checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p3", 1,Response.Status.OK.getStatusCode());
			checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p3", 2,Response.Status.OK.getStatusCode());
			checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p3", 3,Response.Status.OK.getStatusCode());
			checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p3", 4,Response.Status.OK.getStatusCode());
			checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p4", "V1",Response.Status.OK.getStatusCode());
			checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p4", "V4",Response.Status.OK.getStatusCode());
			checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p4", "v4",Response.Status.OK.getStatusCode());
			checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p5", "V4",Response.Status.OK.getStatusCode());
			checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p5", "v4",Response.Status.OK.getStatusCode());
			checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6", "",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
			checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfdsfsdfsdfsdfsdfdsf",Response.Status.OK.getStatusCode());
    		checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds@fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","ss@f",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sssf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sssf3333",Response.Status.OK.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds^fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds>fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds<fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds~fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds$fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds%fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds&fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds(fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds)fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds-fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds_fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds+fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds=fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());

	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds|fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds[fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds]fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds:fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds;fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds?fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds/fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds.fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds,fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds'fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds\"fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds}fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds}fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds}fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());

	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds/fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds\\fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p6","sdfsdfds\\fsdfsdfsdfsdfdsf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());

	    	//Date check
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p7","20121205 : 23:34:34",Response.Status.OK.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p7","20141205 : 23:34:34",Response.Status.OK.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p7","20141205 : 99:34:34",Response.Status.OK.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p7","20141205",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());

	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p8","20121205",Response.Status.OK.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p8","20123205",Response.Status.OK.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p8","2012we05",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p8","50121105",Response.Status.OK.getStatusCode());

	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p9","20121105",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
	    	checkAssertFalseWithArgException(testattachedMessageSource, systemConfigurationManager, "p9","sdfsdf",!isApplyValidation?Response.Status.OK.getStatusCode():Response.Status.NOT_ACCEPTABLE.getStatusCode());
			//yyyyMMdd : HH:mm:ss

		} catch (Exception e) {
			logger.error("***FAILED***", e);
			Assert.fail(e.getMessage());
		}

	}

	private void checkAssertFalseWithArgException(final MessageSource messageSource, SystemConfigurationManager systemConfigurationManager,final String paramName, final Object paramNameVal, final int statusCode){
		try{
			com.ems.ws.util.Response res = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, paramName, paramNameVal);
			Assert.assertEquals(res.getStatus(), statusCode);
		} catch (Exception e) {
			logger.error("***FAILED***", e);
			Assert.fail(e.getMessage());
		}
	}

	private void checkAssertFalseWithArgException(final MessageSource messageSource, SystemConfigurationManager systemConfigurationManager,final String paramName, final Object paramNameVal){
		checkAssertFalseWithArgException(messageSource, systemConfigurationManager, paramName, paramNameVal, Response.Status.OK.getStatusCode());
	}

	@Test
	public void testEmailSend(){
		try {

			sendMail();
		} catch (Exception e) {
			logger.error("***FAILED***", e);
			Assert.fail(e.getMessage());
		}
	}

	/**
	 *  Need to configure like below on respective EM
		email.smtp.host=smtp.office365.com
		email.smtp.port=587
		email.smtp.user=dhanesh.rote@enlightedinc.com
		email.smtp.pass=ddada
		email.transport.protocol=smtp
		email.smtp.auth=true
		email.smtp.starttls.enable=true

	 * @throws Exception
	 */
	private void sendMail() throws Exception {
		String URL_DATA =EMAIL_SEND_WS_PATH;
		Builder wr = getWebResource(URL_DATA, MediaType.MULTIPART_FORM_DATA);
		FormDataMultiPart fdmp = new FormDataMultiPart();
		fdmp.bodyPart(new FileDataBodyPart("file", new File("D:\\enlighted\\em_public.key"),MediaType.MULTIPART_FORM_DATA_TYPE));
		//fdmp.bodyPart(new FileDataBodyPart("file", new File("D:\\Expenses.xls"),MediaType.MULTIPART_FORM_DATA_TYPE));
		//fdmp.bodyPart(new FileDataBodyPart("file", new File("D:\\enlighted\\FloorPlans\\fp1.jpg"),MediaType.MULTIPART_FORM_DATA_TYPE));
		fdmp.field("subject", "Test Subj"+ (new Date()).toString());
		fdmp.field("message", "Test Me Message");
		fdmp.field("recipient", "dhanesh.rote@enlightedinc.com");
		//wr.type(MediaType.MULTIPART_FORM_DATA_TYPE).post(fdmp);
		String res = wr.type(MediaType.MULTIPART_FORM_DATA_TYPE).post(String.class,fdmp);
		if(res == null || !res.equals("SUCCESS")){
			Assert.fail("Issues in sending email. Server Response is: "+ res);
		}else{
		}
		System.out.println("Successfuly send the mail");
		//Response wrp = wr.type(MediaType.MULTIPART_FORM_DATA_TYPE).post(Response.class,fdmp);
//		Response wrp = wr.type(MediaType.MULTIPART_FORM_DATA_TYPE).post(Response.class,fdmp);
//		if(wrp.getStatus() != Response.Status.OK.getStatusCode()){
//			throw new Exception("Uploading Not Successful");
//		}
	}
}
