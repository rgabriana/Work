package com.emscloud.communication;

import java.util.Calendar;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;



import com.communication.utils.ArgumentUtils;
import com.emscloud.model.EmInstance;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

@Component("connectionTemlpate")
public class ConnectionTemlpate<T> {

	private static final String HOST="Host";
	private static final String HOST_VALUE="enLighted";
	private static final String EM_MAC="em_mac";
	private static final String EM_TIME_ZONE="em_time_zone";
	static final Logger logger = Logger.getLogger(ConnectionTemlpate.class
			.getName());

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
			actualUrl = "https://" + em.getReplicaServer().getInternalIp() + encodeURL(url);
			Client client = ClientHelper.createClient();
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
	
	public Builder executePost(EmInstance em, String url, String inputMediaType , String outpuMediaType) {
		WebResource webR = null;
		String actualUrl = null;
		Builder rb = null;
		try {
			actualUrl = "https://" + em.getReplicaServer().getInternalIp() + encodeURL(url);
			Client client = ClientHelper.createClient();
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
			actualUrl = "https://" + em.getReplicaServer().getInternalIp() + encodeURL(url);
			Client client = ClientHelper.createClient();
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
	}

	private String encodeURL(String url) {
		return url.replaceAll(" ", "%20");
	}
}
