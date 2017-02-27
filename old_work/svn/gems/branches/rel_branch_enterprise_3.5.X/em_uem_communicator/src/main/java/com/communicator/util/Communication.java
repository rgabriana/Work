package com.communicator.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;

public class Communication {
	private URL urlToServer;
	private HttpURLConnection http;
	
	public int state;
	
	public String buffer = "";
	
	static final Logger logger = Logger.getLogger(Communication.class.getName());

	public Communication(String url) {
		int timeout = 60 * 1000;
		try {
			urlToServer = new URL(url);
		} catch (MalformedURLException e) {
			state = Globals.state_comm_url_failed;
			return;
		}

		System.setProperty("http.keepAlive", "false");

		if (urlToServer.getProtocol().toLowerCase().equals("https")) {

			if (false == trustAllHosts()) {
				state = Globals.state_comm_init_failed;
				return;
			}

			HttpsURLConnection https;
			try {
				https = (HttpsURLConnection) urlToServer.openConnection();

			} catch (IOException e) {
				state = Globals.state_comm_connect_failed;
				return;
			}

			https.setHostnameVerifier(DO_NOT_VERIFY);
			http = https;

		} else {
			try {
				http = (HttpURLConnection) urlToServer.openConnection();

			} catch (IOException e) {
				state = Globals.state_comm_connect_failed;
				return;
			}
		}

		http.setConnectTimeout(timeout);
		http.setReadTimeout(timeout);

		http.setDoOutput(true);
		http.setDoInput(true);
		try {
			http.setRequestMethod("POST");
		} catch (ProtocolException e1) {
			state = Globals.state_comm_init_failed;

			return;
		}

		http.setRequestProperty("Content-Type",
				"application/xml; charset=utf-8");

		try {
			http.connect();
		} catch (IOException e) {
			state = Globals.state_comm_connect_failed;

			return;
		}
		state = Globals.state_comm_success;
	}

	public Communication(String url, String sessionId, String type, int timeout, String contentType) {

		try {
			urlToServer = new URL(url);
		} catch (MalformedURLException e) {
			state = Globals.state_comm_url_failed;

			return;
		}

		System.setProperty("http.keepAlive", "false");

		if (urlToServer.getProtocol().toLowerCase().equals("https")) {

			if (false == trustAllHosts()) {
				state = Globals.state_comm_init_failed;
				return;
			}

			HttpsURLConnection https;
			try {
				https = (HttpsURLConnection) urlToServer.openConnection();
			} catch (IOException e) {
				state = Globals.state_comm_connect_failed;

				return;
			}

			https.setHostnameVerifier(DO_NOT_VERIFY);
			http = https;

		} else {
			try {
				http = (HttpURLConnection) urlToServer.openConnection();
			} catch (IOException e) {
				state = Globals.state_comm_connect_failed;

				return;
			}
		}
		if (sessionId != null)
			http.setRequestProperty("Cookie", "JSESSIONID=" + sessionId);
		http.setConnectTimeout(timeout);
		http.setReadTimeout(timeout);

		http.setDoOutput(true);
		http.setDoInput(true);
		try {
			http.setRequestMethod(type);
		} catch (ProtocolException e1) {
			state = Globals.state_comm_init_failed;

			return;
		}

		http.setRequestProperty("Content-Type",
				contentType);

		try {
			http.connect();

		} catch (IOException e) {
			state = Globals.state_comm_connect_failed;
			e.printStackTrace();
			return;
		}

		state = Globals.state_comm_success;

	}

	// always verify the host - dont check for certificate
	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	/**
	 * Trust every server - dont check for any certificate
	 */
	private static boolean trustAllHosts() {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}

			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}
		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {

			return false;
		}

		return true;
	}

	public boolean sendData(String dataToSend) {
		OutputStreamWriter wr = null;
		try {
			wr = new OutputStreamWriter(http.getOutputStream(), "UTF-8");
			wr.write(dataToSend);

			wr.flush();
			wr.close();

		} catch (IOException e) {
			state = Globals.state_comm_send_failed;
			return false;
		}
		return true;
	}

	public boolean recvData() {
		try {

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					http.getInputStream(), "UTF-8"));
			String line = null;
			String response = "";

			while ((line = rd.readLine()) != null) {
				response += line;
			}
			rd.close();
			buffer = response;

			if (response.equalsIgnoreCase("")) {
				return false;
			} else {
				return true;
			}

		} catch (IOException e) {

			state = Globals.state_comm_read_failed;
			return false;
		}
	}

	public String recvWebServiceData() {
		try {

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					http.getInputStream(), "UTF-8"));
			String line = null;
			String response = "";

			while ((line = rd.readLine()) != null) {

				response += line;
			}
			rd.close();

			if (response.equalsIgnoreCase("")) {
				return null;
			} else {
				return response;
			}

		} catch (IOException e) {

			e.printStackTrace();
			return null;
		}
	}
	
	public String sendWebServiceData(String data) {

		OutputStreamWriter wr = null;

		try {
			// System.out.println("Data before send" + data);
			wr = new OutputStreamWriter(http.getOutputStream(), "UTF-8");
			wr.write(data);

			wr.flush();
			wr.close();

		} catch (IOException e) {
			logger.error(e);
			state = Globals.state_comm_send_failed;
			return String.valueOf(state);
		}

		String response = recvWebServiceData();

		return response;
	}

}
