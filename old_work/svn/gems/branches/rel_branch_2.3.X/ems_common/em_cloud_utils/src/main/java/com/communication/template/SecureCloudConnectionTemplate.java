package com.communication.template;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.stereotype.Component;

import com.communication.utils.TrustEverythingSSLSocketFactory;


@Component("secureCloudConnectionTemplate")
public class SecureCloudConnectionTemplate extends CloudConnectionTemplate{
	
	private String sClientCertificatePass = "";
	public static final String PKCS_STORE_TYPE = "pkcs12";
	public static final String JKS_STORE_TYPE = "JKS";
	private KeyStore oKSClient = null;
	private KeyStore oKSServer = null;
	
	/**
	 * Any communication that requires trusted handshake communication via X509
	 * certifiate need to create a new instance of this class and call up this
	 * method before executing any requests.
	 * 
	 * @param sServerTrustStore
	 *            Server Trust certificate, installed via debian in a known
	 *            location on EM
	 * @param sServerTrustStorePasswd
	 *            Server Trust certificate password
	 * @param sClientCertificate
	 *            Client certificate issued at the time of sPPA enabling
	 * @param sClientCertificatePasswd
	 *            Client certificate password.
	 */
	public boolean setUpCertificateDetails(String sTSStoreType, String sServerTrustStore,
			String sServerTrustStorePasswd, String sClientStoreType, String sClientCertificate,
			String sClientCertificatePasswd) {
		boolean bStatus = false;
		this.sClientCertificatePass = sClientCertificatePasswd;
		InputStream keystoreInput = null;
		InputStream truststoreInput = null;
		try {
			oKSClient = KeyStore.getInstance(sClientStoreType);
			keystoreInput = new FileInputStream(sClientCertificate);
			oKSClient.load(keystoreInput,
					sClientCertificatePasswd.toCharArray());

			oKSServer = KeyStore.getInstance(sTSStoreType);
			truststoreInput = new FileInputStream(sServerTrustStore);
			oKSServer.load(truststoreInput,
					sServerTrustStorePasswd.toCharArray());
			bStatus = true;
		} catch (FileNotFoundException e) {
			logger.warn("File not found: ", e);
		} catch (NoSuchAlgorithmException e) {
			logger.warn("No such algorithm: ", e);
		} catch (CertificateException e) {
			logger.warn("Certificate error: ", e);
		} catch (IOException e) {
			logger.warn("IO error: ", e);
		} catch (KeyStoreException e) {
			logger.warn("Keystore error: ", e);
		} finally {
			try {
				if (keystoreInput != null) {
					keystoreInput.close();
				}
				if (truststoreInput != null) {
					truststoreInput.close();
				}
				keystoreInput = null;
				truststoreInput = null;
			} catch (IOException e) {
				logger.warn("finalizing IO error: ", e);
			}
		}
		return bStatus;
	}

	protected DefaultHttpClient getHttpClient() {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		try {
			if (oKSClient != null && oKSServer != null) {
				SSLSocketFactory socketFactory = new SSLSocketFactory(
						oKSClient, this.sClientCertificatePass, oKSServer);
				Scheme sch = new Scheme("https", 443, socketFactory);
				httpclient.getConnectionManager().getSchemeRegistry()
						.register(sch);
			} else {
				SSLSocketFactory socketFactory = TrustEverythingSSLSocketFactory
						.getSocketFactory();
				Scheme sch = new Scheme("https", 443, socketFactory);

				httpclient.getConnectionManager().getSchemeRegistry()
						.register(sch);
			}
		} catch (Exception e) {
			logger.warn("Error obtaining Trusted connection: ", e);
		} finally {
		}
		return httpclient;
	}

}
