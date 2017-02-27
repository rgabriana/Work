package com.communication.template;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import com.adrcom.main.ADRCommunication;
import com.communication.utils.CloudHttpResponse;
import com.communication.utils.TrustEverythingSSLSocketFactory;

@Component("cloudConnectionTemplate")
public class CloudConnectionTemplate {	

	static final Logger logger = Logger.getLogger(CloudConnectionTemplate.class
			.getName());
	
	@SuppressWarnings("finally")
	public CloudHttpResponse executePost(String service, String stringPost,
			String ip, String type,String userName,String password) {
		CloudHttpResponse cloudResponse = null;
		DefaultHttpClient httpclient = getHttpClient();		
		try {			
			httpclient.getCredentialsProvider().setCredentials(new AuthScope(ip, AuthScope.ANY_PORT), new UsernamePasswordCredentials(userName,password));
			String requestLink = this.getRequestUrl(service, ip);
			HttpPost httpPost = new HttpPost(requestLink);
			StringEntity requestBodies = new StringEntity(stringPost);
			requestBodies.setContentType(type);
			httpPost.setEntity(requestBodies);
			HttpResponse response = httpclient.execute(httpPost);			
			cloudResponse = getCloudResponse(response);

		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		} catch (ClientProtocolException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
			return cloudResponse;
		}
	}

	private CloudHttpResponse getCloudResponse(HttpResponse httpResponse) {
		CloudHttpResponse cloudResponse = new CloudHttpResponse();

		cloudResponse.setStatus(httpResponse.getStatusLine().getStatusCode());
		String responseString = null;

		try {
			// Read the content
			HttpEntity entity = httpResponse.getEntity();
			if (entity != null) {
				responseString = EntityUtils.toString(entity);
			} else {
				responseString = null;
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
		cloudResponse.setResponse(responseString);
		return cloudResponse;
	}

	protected DefaultHttpClient getHttpClient() {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), ADRCommunication.connectionTimeout);
		HttpConnectionParams.setSoTimeout(httpclient.getParams(), ADRCommunication.socketTimeout);
		try {

			SSLSocketFactory socketFactory = TrustEverythingSSLSocketFactory
					.getSocketFactory();
			Scheme sch = new Scheme("https", 443, socketFactory);

			httpclient.getConnectionManager().getSchemeRegistry().register(sch);

		} catch (Exception e) {
			logger.log(Level.SEVERE, e.toString(), e);
		} finally {
		}
		return httpclient;
	}
	
	protected String getRequestUrl(String service,String ip){
		return "https://" + ip + service;
	}
}
