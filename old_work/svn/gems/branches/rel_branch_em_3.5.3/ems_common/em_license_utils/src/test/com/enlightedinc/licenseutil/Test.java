package com.enlightedinc.licenseutil;

public class Test {
	
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String uuid = "eee50793-d751-4f60-ac1a-9f3a2160964f";
		int noOfEmDevices = 1000;
		Boolean bacnetEnabled = false;
		int noOfBacnetDevices = 0;
		Boolean zoneSensorsEnabled = false;
		int noOfZoneSensors = 0;
		Boolean hvacEnabled = false;
		String emProductId = "EM-SW-1000";
		String bacnetProductId = "NB-LG-01";
		String zoneSensorsProductId = "EM-ZNS-01";
		String encryptedJsonLicenseString =  null;
		Boolean occupancySensorEnabled = false;
		//String jsonLicenseString = "{\"enabled\":true,\"mac\":\"6c:62:6d:66:24:a3\",\"em\":{\"noofdevices\":1000},\"bacnet\":{\"enabled\":true},\"hvac\":{\"enabled\":true}}";
		encryptedJsonLicenseString = LicenseUtil.getEncryptedJsonLicenseString(uuid,noOfEmDevices,bacnetEnabled,noOfBacnetDevices,zoneSensorsEnabled,noOfZoneSensors,hvacEnabled,emProductId,bacnetProductId,zoneSensorsProductId,occupancySensorEnabled);
		
		System.out.print(encryptedJsonLicenseString);
	}

}
