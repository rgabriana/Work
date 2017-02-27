package com.communication.template;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.communication.utils.ArgumentUtils;
import com.communication.utils.CloudHttpResponse;
import com.communication.utils.TrustEverythingSSLSocketFactory;

@Component("cloudConnectionTemplate")
public class CloudConnectionTemplate {	

	static final Logger logger = Logger.getLogger(CloudConnectionTemplate.class
			.getName());
	static long configLastModified;
	static Integer connectionTimeout;
	static Integer socketTimeout;
	
	static String configFile = "/opt/enLighted/communicator/config.properties";
	
	static {
		File drUserFile = new File(configFile);
		configLastModified = drUserFile.lastModified();
		connectionTimeout = Integer.parseInt(ArgumentUtils.getPropertyWithName("cloudConnectionTimeout", configFile));
		socketTimeout = Integer.parseInt(ArgumentUtils.getPropertyWithName("cloudSocketTimeout", configFile));
	}

	@SuppressWarnings("finally")
	public CloudHttpResponse executeGet(String service, String ip) {

		CloudHttpResponse cloudResponse = null;
		DefaultHttpClient httpclient = this.getHttpClient();
		try {
			String requestLink = this.getRequestUrl(service, ip);
			HttpGet httpget = new HttpGet(requestLink);
			HttpResponse httpResponse = httpclient.execute(httpget);
			cloudResponse = getCloudResponse(httpResponse);
		} catch (Exception e) {
			logger.error(e.toString());
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
			return cloudResponse;
		}
	}

	@SuppressWarnings("finally")
	public CloudHttpResponse executePost(String service, MultipartEntity parts,
			String ip) {
		CloudHttpResponse cloudResponse = null;
		DefaultHttpClient httpclient = getHttpClient();
		try {

			String requestLink = this.getRequestUrl(service, ip);
			HttpPost httpPost = new HttpPost(requestLink);

			httpPost.setEntity(parts);
			HttpResponse response = httpclient.execute(httpPost);
			cloudResponse = getCloudResponse(response);

		} catch (FileNotFoundException e) {
			logger.error(e.toString());
		} catch (ClientProtocolException e) {
			logger.error(e.toString());
		} catch (IOException e) {
			logger.error(e.toString());
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
			return cloudResponse;
		}
	}

	@SuppressWarnings("finally")
	public CloudHttpResponse executePost(String service, String stringPost,
			String ip, String type) {
		CloudHttpResponse cloudResponse = null;
		DefaultHttpClient httpclient = getHttpClient();
		try {
			String requestLink = this.getRequestUrl(service, ip);
			HttpPost httpPost = new HttpPost(requestLink);
			StringEntity requestBodies = new StringEntity(stringPost);
			requestBodies.setContentType(type);
			httpPost.setEntity(requestBodies);
			HttpResponse response = httpclient.execute(httpPost);
			cloudResponse = getCloudResponse(response);

		} catch (FileNotFoundException e) {
			logger.error(e.toString());
		} catch (ClientProtocolException e) {
			logger.error(e.toString());
		} catch (IOException e) {
			logger.error(e.toString());
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
			return cloudResponse;
		}
	}
	
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
			throw e;
		} catch (ClientProtocolException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
			return cloudResponse;
		}
	}


	@SuppressWarnings("finally")
	public CloudHttpResponse executePost(String service,
			Map<String, String> nameValues, String ip, String type) {
		CloudHttpResponse cloudResponse = null;
		DefaultHttpClient httpclient = getHttpClient();
		try {
			String requestLink = this.getRequestUrl(service, ip);
			HttpPost httpPost = new HttpPost(requestLink);

			BasicNameValuePair[] params = new BasicNameValuePair[nameValues
					.keySet().size()];
			int cnt = 0;
			for (String key : nameValues.keySet()) {
				params[cnt++] = new BasicNameValuePair(key, nameValues.get(key));
			}

			UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(
					Arrays.asList(params));
			urlEncodedFormEntity.setContentType(type);
			httpPost.setEntity(urlEncodedFormEntity);

			HttpResponse response = httpclient.execute(httpPost);
			cloudResponse = getCloudResponse(response);

		} catch (FileNotFoundException e) {
			logger.error(e.toString());
		} catch (ClientProtocolException e) {
			logger.error(e.toString());
		} catch (IOException e) {
			logger.error(e.toString());
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
			return cloudResponse;
		}
	}

	@SuppressWarnings("finally")
	public CloudHttpResponse downloadAndDump(String service, String stringPost,
			String ip, String destination) {
		CloudHttpResponse cloudResponse = null;
		DefaultHttpClient httpclient = getHttpClient();
		try {
			String requestLink = this.getRequestUrl(service, ip);
			HttpPost httpPost = new HttpPost(requestLink);
			StringEntity requestBodies = new StringEntity(stringPost);
			requestBodies.setContentType(MediaType.TEXT_PLAIN);
			httpPost.setEntity(requestBodies);
			HttpResponse response = httpclient.execute(httpPost);
			try {
				// Read the content
				HttpEntity entity = response.getEntity();
				byte[] bytes = EntityUtils.toByteArray(entity);
				File file = new File(destination);
				if (!file.exists()) {
					file.createNewFile();
				}
				FileOutputStream fileOuputStream = new FileOutputStream(file,
						false);
				fileOuputStream.write(bytes);
				fileOuputStream.close();
			} catch (Exception e) {
				logger.error(e.toString());
			} finally {
				try {
					Runtime rt = Runtime.getRuntime();
					Process proc = rt.exec(new String[] { "chmod", "666",
							destination });
					proc.waitFor();
				} catch (IOException ioe) {
					logger.error(ioe.toString());
				}
			}

		} catch (FileNotFoundException e) {
			logger.error(e.toString());
		} catch (ClientProtocolException e) {
			logger.error(e.toString());
		} catch (IOException e) {
			logger.error(e.toString());
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
			logger.error(e.toString());
		}
		cloudResponse.setResponse(responseString);
		return cloudResponse;
	}

	protected DefaultHttpClient getHttpClient() {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		File drUserFile = new File(configFile);
		if(drUserFile.lastModified() != configLastModified) {
			connectionTimeout = Integer.parseInt(ArgumentUtils.getPropertyWithName("cloudConnectionTimeout", configFile));
			socketTimeout = Integer.parseInt(ArgumentUtils.getPropertyWithName("cloudSocketTimeout", configFile));
		}
		HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), connectionTimeout);
		HttpConnectionParams.setSoTimeout(httpclient.getParams(), socketTimeout);
		try {

			SSLSocketFactory socketFactory = TrustEverythingSSLSocketFactory
					.getSocketFactory();
			Scheme sch = new Scheme("https", 443, socketFactory);

			httpclient.getConnectionManager().getSchemeRegistry().register(sch);

		} catch (Exception e) {
			logger.warn("Error obtaining Trusted connection: ", e);
		} finally {
		}
		return httpclient;
	}
	
	protected String getRequestUrl(String service,String ip){
		return "https://" + ip + service;
	}
}
