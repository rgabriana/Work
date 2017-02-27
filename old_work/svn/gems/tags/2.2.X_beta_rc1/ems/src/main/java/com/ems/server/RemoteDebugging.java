/**
 * Copyright 2010 - EnlightedInc 
 */
package com.ems.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * RemoteDebugging class is used to declare the Application and sub application
 * categories and also declares messsage types used in sending packets to SU
 * unit.
 * 
 * @author yogesh
 * 
 */
public final class RemoteDebugging {
	private static Logger m_rdbLogger = Logger.getLogger(RemoteDebugging.class
			.getName());
	public static final int START_REMOTE_DEBUG = 0xd3;

	public static final int RDB_PKT_APP_NO = ServerConstants.RES_CMD_PKT_MSG_START_POS;
	public static final int RDB_PKT_SUBAPP_NO = RDB_PKT_APP_NO + 1;
	public static final int RDB_PKT_MSG_START = RDB_PKT_SUBAPP_NO + 1;

	public static final int RDB_PKT_MSG_LENGTH = 64;

	public static final int RDB_PKT_EXPTECTED_TOTAL_MSG_LENGTH = 69;

	/**
	 * Maintains a list of top level Application categories.
	 */
	private static ArrayList<String> RDBRootList = new ArrayList<String>();
	static {
		RDBRootList.add("Power on Self Test");
		RDBRootList.add("State Machine Test");
		RDBRootList.add("Sensors Test");
		RDBRootList.add("Drivers Test");
	}

	/**
	 * Maintains a list of sub level Application categories on which the RDB
	 * will be activated.
	 */
	private static HashMap<String, List<String>> RDBSubSectionList = new HashMap<String, List<String>>();
	static {
		RDBSubSectionList.put("Power on Self Test", Arrays.asList(new String[] {
				"PIR", "I2C", "UART", "CU", "Temprature", "Ambient Light" }));
		RDBSubSectionList.put("State Machine Test", Arrays.asList(new String[] {
				"Read Profile", "Debug Transitions" }));
		RDBSubSectionList.put("Sensors Test", Arrays.asList(new String[] {
				"PIR", "Temprature", "Light" }));
		RDBSubSectionList.put("Drivers Test", Arrays.asList(new String[] {
				"SPI", "I2C", "GPIO", "UART", "FLASH", "UART_CU" }));
	}

	/**
	 * Returns Top level Application categories Note the order is important -
	 * The items in the category are mapped as per the message types defined in
	 * the SU code.
	 * 
	 * @return ArrayList
	 */
	public static ArrayList<String> getRootList() {
		return RDBRootList;
	}

	/**
	 * Returns sub level Application categories based on the root category Note
	 * the order is important - The items in the category are mapped as per the
	 * message types defined in the SU code for the sub categories.
	 * 
	 * @return ArrayList
	 * 
	 * @param rootNode
	 *            String
	 * @return ArrayList
	 */
	public static List<String> getSubSectionList(String rootNode) {
		return RDBSubSectionList.get(rootNode);
	}

	/**
	 * Used for debugging purposes
	 * 
	 * @param iRootindex
	 * @return root application name
	 */
	public static String getApplication(int iRootindex) {
		String rootapplication = "";
		try {
			rootapplication = RDBRootList.get(iRootindex);
		} catch (IndexOutOfBoundsException iobe) {
			m_rdbLogger.warn(iobe.getMessage());
		}
		return rootapplication;
	}

	/**
	 * Used for debugging purposes
	 * 
	 * @param iRootindex
	 * @param iSubindex
	 * @return sub application name
	 */
	public static String getSubApplication(int iRootindex, int iSubindex) {
		String subapplication = "";
		try {
			String rootcategory = RDBRootList.get(iRootindex);
			subapplication = RDBSubSectionList.get(rootcategory).get(iSubindex);
		} catch (IndexOutOfBoundsException iobe) {
			m_rdbLogger.warn(iobe.getMessage());
		}
		return subapplication;
	}
}
