package com.ems;

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

public class Communication {
	private URL urlToServer;
	private HttpURLConnection http;
	private int timeout; // timeout is given in miliseconds

	public Communication(String url) {
		//timeout = 60 * 1000;
		//infinite = 0
		timeout = 0;

		try {
			urlToServer = new URL(url);
		} catch (MalformedURLException e) {
			Globals.state = Globals.state_comm_url_failed;

			return;
		}

		System.setProperty("http.keepAlive", "false");

		// init http or https communication params

		if (urlToServer.getProtocol().toLowerCase().equals("https")) { // HTTPS
																		// communication
																		// to be
																		// done

			if (false == trustAllHosts()) {
				Globals.state = Globals.state_comm_init_failed;
				return;
			}

			HttpsURLConnection https;
			try {
				https = (HttpsURLConnection) urlToServer.openConnection();

			} catch (IOException e) {
				Globals.state = Globals.state_comm_connect_failed;

				return;
			}

			https.setHostnameVerifier(DO_NOT_VERIFY);
			http = https;

		} else {// HTTP communication to be done
			try {
				http = (HttpURLConnection) urlToServer.openConnection();

			} catch (IOException e) {
				Globals.state = Globals.state_comm_connect_failed;

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
			Globals.state = Globals.state_comm_init_failed;

			return;
		}

		// http.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
		http.setRequestProperty("Content-Type",
				"application/xml; charset=utf-8");

		try {
			http.connect();
		} catch (IOException e) {
			Globals.state = Globals.state_comm_connect_failed;

			return;
		}

		Globals.state = Globals.state_comm_success;

	}

	public Communication(String url, String sessionId, String type) {
		

		try {
			urlToServer = new URL(url);
		} catch (MalformedURLException e) {
			Globals.state = Globals.state_comm_url_failed;

			return;
		}

		System.setProperty("http.keepAlive", "true");

		// init http or https communication params

		if (urlToServer.getProtocol().toLowerCase().equals("https")) { // HTTPS
																		// communication
																		// to be
																		// done

			if (false == trustAllHosts()) {
				Globals.state = Globals.state_comm_init_failed;
				return;
			}

			HttpsURLConnection https;
			try {
				https = (HttpsURLConnection) urlToServer.openConnection();
			} catch (IOException e) {
				Globals.state = Globals.state_comm_connect_failed;

				return;
			}

			https.setHostnameVerifier(DO_NOT_VERIFY);
			http = https;

		} else {// HTTP communication to be done
			try {
				http = (HttpURLConnection) urlToServer.openConnection();
			} catch (IOException e) {
				Globals.state = Globals.state_comm_connect_failed;

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
			Globals.state = Globals.state_comm_init_failed;

			return;
		}catch (Exception e) {
			Globals.log.fine("Exception" + e.getMessage());
			return ;
		}
		http.addRequestProperty("Accept", "application/xml");

		http.setRequestProperty("Content-Type",
				"application/xml; charset=utf-8");

		try {
			http.connect();

		} catch (IOException e) {
			Globals.state = Globals.state_comm_connect_failed;

			return;
		}catch (Exception e) {
			Globals.log.fine("Exception" + e.getMessage());
			return ;
		}

		Globals.state = Globals.state_comm_success;

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
			Globals.log.fine("Exception" + e.getMessage());
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

			Globals.state = Globals.state_comm_send_failed;
			return false;
		}catch (Exception e) {
        	
			Globals.log.fine("Exception" + e.getMessage());
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
			Globals.buffer = response;

			if (response.equalsIgnoreCase("")) {
				return false;
			} else {
				return true;
			}

		} catch (IOException e) {

			Globals.state = Globals.state_comm_read_failed;
			return false;
		}catch (Exception e) {
        	
			Globals.log.fine("Exception" + e.getMessage());
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
			Globals.log.fine("Exception " + e.getMessage() );
			return null;
		} catch (Exception e) {
			Globals.log.fine("Exception " + e.getMessage() );
			return null;
		}
	}

	public String sendWebServiceData(String data) {

		OutputStreamWriter wr = null;

		try {
			wr = new OutputStreamWriter(http.getOutputStream(), "UTF-8");
			wr.write(data);
			wr.flush();
			wr.close();

		} catch (IOException e) {
			Globals.log.fine("Exception " + e.getMessage());
			Globals.state = Globals.state_comm_send_failed;
			return String.valueOf(Globals.state);
		} catch (Exception e) {
			Globals.log.fine("Exception" + e.getMessage() );
			return e.getMessage();
		}
		String response = recvWebServiceData();
		return response;
	}

}