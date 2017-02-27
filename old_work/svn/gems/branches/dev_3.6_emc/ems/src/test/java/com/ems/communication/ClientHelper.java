package com.ems.communication;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

public class ClientHelper {

	 private static final String ECLOUD_REPLICA = "ecloud_replica";
	static final Logger logger = Logger.getLogger(ClientHelper.class.getName());
	 private final static String STR_CERTS_PATH =
	 "/etc/enlighted/CA/ssl/pfx/";
	 private static String filePath = STR_CERTS_PATH + ECLOUD_REPLICA +
	 ".pfx";
	 private static char[] sClientCertPassword = ECLOUD_REPLICA.toCharArray();
	 private static Long fileModified = null;
	private static ClientConfig clientConfig = null;

	 private static KeyStore keyStoreKeys;
	 private static KeyManagerFactory keyMgrFactory;

	public static ClientConfig configureKSClient() {
		 File pfxFile = new File(filePath);
		 if (clientConfig != null && fileModified != null &&
		 fileModified.compareTo(pfxFile.lastModified()) == 0) {
		 return clientConfig;
		 }
		 fileModified = pfxFile.lastModified();

		TrustManager[] certs = new TrustManager[] { new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}

			@Override
			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}
		} };
		SSLContext ctx = null;
		FileInputStream fis = null;
		ClientConfig config = new DefaultClientConfig();
		try {
			 keyStoreKeys = KeyStore.getInstance("PKCS12");
			 fis = new FileInputStream(filePath);
			 keyStoreKeys.load(fis, sClientCertPassword);
			 keyMgrFactory = KeyManagerFactory.getInstance("SunX509");
			 keyMgrFactory.init(keyStoreKeys, sClientCertPassword);
			 ctx = SSLContext.getInstance("TLS");
			 ctx.init(keyMgrFactory.getKeyManagers(), certs, new
			 SecureRandom());
			ctx = SSLContext.getInstance("TLS");
			ctx.init(null, certs, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(ctx
					.getSocketFactory());
			config.getProperties().put(
					HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
					new HTTPSProperties(new HostnameVerifier() {
						@Override
						public boolean verify(String hostname,
								SSLSession session) {
							return true;
						}
					}, ctx));
		}  catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(fis);
		}

		return config;
	}

	public static ClientConfig configureClient() {
		TrustManager[] certs = new TrustManager[] { new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}

			@Override
			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}
		} };
		SSLContext ctx = null;
		try {
			ctx = SSLContext.getInstance("TLS");
			ctx.init(null, certs, new SecureRandom());
		} catch (java.security.GeneralSecurityException ex) {
		}
		HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
		ClientConfig config = new DefaultClientConfig();
		try {
			config.getProperties().put(
					HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
					new HTTPSProperties(new HostnameVerifier() {
						@Override
						public boolean verify(String hostname,
								SSLSession session) {
							return true;
						}
					}, ctx));
		} catch (Exception e) {
		}
		return config;
	}

	public static Client createKSClient() {
		System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
		System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
		clientConfig = ClientHelper.configureKSClient();
		Client c = Client.create(clientConfig);
		c.setConnectTimeout(10000);
		c.setReadTimeout(300000);
		return c;
	}

	public static Client createClient() {
		System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
		System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
		clientConfig = ClientHelper.configureClient();
		Client c = Client.create(clientConfig);
		c.setConnectTimeout(10000);
		c.setReadTimeout(300000);
		return c;
	}

}
