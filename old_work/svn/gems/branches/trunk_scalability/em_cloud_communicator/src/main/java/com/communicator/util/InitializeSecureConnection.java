/**
 * 
 */
package com.communicator.util;

import java.io.File;
import java.io.FilenameFilter;

import com.communication.template.SecureCloudConnectionTemplate;

/**
 * @author yogesh
 * 
 */
public class InitializeSecureConnection {
	private static Boolean isSppaCertificateInitialized = false ;
	// Set up the certificate
	private final static String STR_CERTS_PATH = "/var/lib/tomcat6/Enlighted/certs/";
	private final static String STR_TRUST_STORE = STR_CERTS_PATH
			+ "CA/enlighted.ts";
	private final static String STR_TRUST_STORE_PASS = "enlighted";

	public static boolean init(
			SecureCloudConnectionTemplate secureCloudConnectionTemplate) {
		File f = new File(STR_CERTS_PATH);
		File[] matchingFiles = f.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith("pfx");
			}
		});
		if (matchingFiles != null && matchingFiles.length > 0) {
			String sName = matchingFiles[0].getName();
			String sClientCertPassword = "";
			int pos = sName.lastIndexOf(".");
			if (pos != -1) {
				try{
				sClientCertPassword = sName.substring(0, pos);
				secureCloudConnectionTemplate.setUpCertificateDetails(
						SecureCloudConnectionTemplate.JKS_STORE_TYPE,
						STR_TRUST_STORE, STR_TRUST_STORE_PASS,
						SecureCloudConnectionTemplate.PKCS_STORE_TYPE,
						STR_CERTS_PATH + sName, sClientCertPassword);
					isSppaCertificateInitialized=true ;
				return true;
				} catch(Exception ex)
				{
					isSppaCertificateInitialized = false ;
				}
			}
		}
		return false;
	}

	public static Boolean getIsSppaCertificateInitialized() {
		return isSppaCertificateInitialized;
	}
}
