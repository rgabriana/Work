package com.emscloud.communication;

import java.util.Calendar;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.emscloud.model.EmInstance;
import com.emscloud.service.GlemManager;
import com.emscloud.types.GlemModeType;
import com.emscloud.util.CommonUtils;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

@Component("connectionTemlpate")
public class ConnectionTemlpate<T> {

	private static final String HOST="Host";
	private static final String HOST_VALUE="enLighted";
	private static final String EM_MAC="em_mac";
	private static final String EM_TIME_ZONE="em_time_zone";
	
	private static final String DIGEST_ALGO = "SHA-1";
	private static final String FORMATTER = "yyyyMMddHHmmss".trim();
	private static final String UEM_AUTHORIZATION = "Authorization";
	private static final String UEM_API_KEY="ApiKey";
	private static final String TIME_STAMP="ts";  // timestamp in miliseconds from epoch used as salt to digest key.
	static final Logger logger = Logger.getLogger(ConnectionTemlpate.class
			.getName());
	@Resource
	private GlemManager glemManager;
	/*
	 * public ClientResponse executeGet(EmInstance em, String url, String
	 * mediaType) { ClientResponse response = null; WebResource webR = null;
	 * String actualUrl = null; try { actualUrl = "https://" + em.getIpAddress()
	 * + url; Client client = ClientHelper.createClient(); webR =
	 * client.resource(actualUrl); response =
	 * webR.accept(mediaType).get(ClientResponse.class); } catch (Exception ex)
	 * { ex.printStackTrace(); logger.error(ex.getMessage(), ex); } return
	 * response; }
	 */

	public Builder executeGet(EmInstance em, String url, String mediaType) {
		WebResource webR = null;
		String actualUrl = null;
		Builder rb = null;
		try {
			String serverIp = glemManager.getGLEMMode() == GlemModeType.ECLOUD.getMode() ? em.getReplicaServer().getInternalIp() : em
					.getIpAddress();
			actualUrl = "https://" + serverIp + encodeURL(url);
			Client client = glemManager.getGLEMMode() == GlemModeType.ECLOUD.getMode() ? ClientHelper.createKSClient() : ClientHelper.createClient();
			webR = client.resource(actualUrl);
			// added to avoid caching and 204 error code.
			webR = webR.queryParam("ts",
					Long.toString(Calendar.getInstance().getTimeInMillis()));
			rb = webR.accept(mediaType);
			rb = addSecurityHeaders(em, rb);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage(), ex);
		}
		return rb;
	}
	
	public Builder executeGet(String ip, String url, String mediaType) {
		
		WebResource webR = null;
		String actualUrl = null;
		Builder rb = null;
		try {
			actualUrl = "https://" + ip + encodeURL(url);
			Client client = ClientHelper.createClient();
			webR = client.resource(actualUrl);
			// added to avoid caching and 204 error code.
			//webR = webR.queryParam("ts",
				//	Long.toString(Calendar.getInstance().getTimeInMillis()));
			rb = webR.accept(mediaType);
			//rb = addSecurityHeaders(em, rb);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage(), ex);
		}
		return rb;
	}
	
	public Builder executePost(EmInstance em, String url, String inputMediaType , String outpuMediaType) {
		WebResource webR = null;
		String actualUrl = null;
		Builder rb = null;
		try {
			String serverIp = glemManager.getGLEMMode() == GlemModeType.ECLOUD.getMode() ? em.getReplicaServer().getInternalIp() : em.getIpAddress();
			actualUrl = "https://" + serverIp + encodeURL(url);
			Client client = glemManager.getGLEMMode() == GlemModeType.ECLOUD.getMode() ? ClientHelper.createKSClient() : ClientHelper.createClient();
			webR = client.resource(actualUrl);
			rb = webR.accept(outpuMediaType).type(inputMediaType);
			rb = addSecurityHeaders(em,rb);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage(), ex);
		}
		return rb;
	}
	
	public Builder executePost(EmInstance em, String url, String outpuMediaType) {
		WebResource webR = null;
		String actualUrl = null;
		Builder rb = null;
		try {
			String serverIp = glemManager.getGLEMMode() == GlemModeType.ECLOUD.getMode() ? em.getReplicaServer().getInternalIp() : em.getIpAddress();
			actualUrl = "https://" + serverIp + encodeURL(url);
			Client client = glemManager.getGLEMMode() == GlemModeType.ECLOUD.getMode() ? ClientHelper.createKSClient() : ClientHelper.createClient();
			webR = client.resource(actualUrl);
			rb = webR.accept(outpuMediaType);
			rb = addSecurityHeaders(em,rb);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage(), ex);
		}
		return rb;
	}

	private Builder addSecurityHeaders(EmInstance em, Builder rb) {
		if (glemManager.getGLEMMode() == GlemModeType.ECLOUD.getMode()) {
			try {
				rb.header(HOST, HOST_VALUE);
				rb.header(EM_MAC, em.getMacId());
				rb.header(EM_TIME_ZONE, em.getTimeZone());
				return rb;
			} catch (Exception e) {
				logger.fatal(
						"Fatal Exception while creating authorization Header parameter"
								+ " for UEM to EM communication. Contact Admin.", e);
			}
			return rb ;
		}else {
			try {
				final String apiKey = em.getApiKey().toString().trim();
				final String secretKey = em.getSecretKey().toString().trim();
				String dateString = Long.toString(Calendar.getInstance().getTimeInMillis());
				final String authKey =CommonUtils.getSaltBasedDigest(
						apiKey, secretKey, dateString, DIGEST_ALGO);
				rb.header(UEM_AUTHORIZATION, authKey);
				rb.header(UEM_API_KEY, apiKey);
				rb.header(TIME_STAMP, dateString);
				rb.header(HOST, HOST_VALUE);
				return rb;
			} catch (Exception e) {
				logger.fatal(
						"Fatal Exception while creating authorization Header parameter"
								+ " for UEM to EM communication. Contact Admin.", e);
			}
			return rb ;
		}
	}

	private String encodeURL(String url) {
		return url.replaceAll(" ", "%20");
	}
}
