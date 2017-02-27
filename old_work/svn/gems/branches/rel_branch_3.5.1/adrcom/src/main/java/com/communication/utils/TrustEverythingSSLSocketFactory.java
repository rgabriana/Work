package com.communication.utils;

import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;


public class TrustEverythingSSLSocketFactory {
	private static SSLSocketFactory sslSocketFactory;

	public static SSLSocketFactory getSocketFactory() {
		if (sslSocketFactory == null) {
			try {
				TrustManager[] tm = new TrustManager[] { new TrustEverythingTrustManager() };
				SSLContext context = SSLContext.getInstance("SSL");
				context.init(null, tm, new SecureRandom());

				sslSocketFactory = new SSLSocketFactory(context , SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return sslSocketFactory;
	}
}
