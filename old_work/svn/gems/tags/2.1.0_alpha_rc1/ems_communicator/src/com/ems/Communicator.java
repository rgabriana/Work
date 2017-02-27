/**
 * 
 */
package com.ems;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author SAMEER SURJIKAR This class help communication between server and
 *         communicator
 * 
 */
public class Communicator {

	private String url;
	private String userName = "admin";
	private String password = "admin";
	private String gemsIp = "";

	/**
	 * To authenticate communicator with the server
	 * 
	 * @return Login respnose
	 */
	public synchronized StringBuilder authenticate(String url) {

		StringBuilder sb = new StringBuilder();
		String loginXML = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><root><request><messageType>1</messageType><body><loginRequest><userName>"
				+ "admin"
				+ "</userName><password>"
				+ "admin"
				+ "</password></loginRequest></body></request></root>";
		return sb.append(login(loginXML, url));
	}

	/**
	 * Helps with login process
	 * 
	 * @param loginXML
	 * @param url
	 * @return Login response
	 */
	public synchronized String login(String loginXML, String url) {

		String loginResponceXML = "";
		Globals.buffer = "";

		Communication scObj = new Communication(url);

		if (Globals.state == 100) {

			if (scObj.sendData(loginXML)) {
				if (scObj.recvData()) {

					loginResponceXML = Globals.buffer;

				} else {
					;

					loginResponceXML = Integer.toString(Globals.state);
				}
			} else {

				loginResponceXML = Integer.toString(Globals.state);

			}

		} else {
			loginResponceXML = Integer.toString(Globals.state);
		}

		return loginResponceXML;
	}

	/**
	 * Fire a get request on the webservice whose path is given
	 * 
	 * @param url
	 * @return results of the request
	 */
	public synchronized StringBuilder webServiceGetRequest(String url) {
		// ------------------------------------------------------------------------------------------------------------------------------
		Properties properties = new Properties();
		try {
			InputStream is = new FileInputStream(Globals.propFile);

			properties.load(is);
		} catch (IOException e) {
		}
		String loginUrl = properties.getProperty("GemsLoginUrl");
		StringBuilder sb = new StringBuilder();

		// Login and get the response for getting cookies.
		String loginXML = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><root><request><messageType>1</messageType><body><loginRequest><appIp>"
				+ "EMS COMMUNICATOR"
				+ "</appIp></loginRequest></body></request></root>";
		String loginResponse = login(loginXML, loginUrl);
		String sessionId = "";
		try {
			sessionId = getSessionID(loginResponse);
			gemsIp = getGemsIP(loginResponse);
		} catch (Exception e) {
			Globals.log.fine("Login error " + loginResponse);
		}
		// ------------------------------------------------------------------------------------------------------------------------------

		Communication c = new Communication(url, sessionId, "GET");
		String str = c.recvWebServiceData();
		sb.append(str);
		return sb;
	}

	/**
	 * Fire a get request on the webservice whose path is given
	 * 
	 * @param url
	 * @return results of the request
	 */
	public synchronized String webServicePostRequest(String data, String url) {
		StringBuilder sb = new StringBuilder();
		Properties properties = new Properties();
		try {

			InputStream is = new FileInputStream(Globals.propFile);

			properties.load(is);
		} catch (IOException e) {
		}
		String loginUrl = properties.getProperty("ServerLoginUrl");
		String apiKey = properties.getProperty("ApiKey");
		// ------------------------------------------------------------------------------------------------------------------------------
		// Login and get the response for getting session

		String ip = properties.getProperty("GemsIp");

		String loginXML = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><root><request><messageType>1</messageType><body><loginRequest><apiKey>"
				+ apiKey
				+ "</apiKey><gemsIp>"
				+ ip
				+ "</gemsIp></loginRequest></body></request></root>";
		String loginResponse = "";
		String sessionId = "";
		try {
			loginResponse = login(loginXML, loginUrl);
			sessionId = getSessionID(loginResponse);
		} catch (Exception e) {
			Globals.log.fine("Login error " + loginResponse);
		}
		// ------------------------------------------------------------------------------------------------------------------------------
		Communication c = new Communication(url, sessionId, "POST");
		return c.sendWebServiceData(data);

	}

	/**
	 * Parse response xml from login webservice to get session id
	 * 
	 * @param loginXml
	 * @return session id
	 */
	public synchronized String getSessionID(String loginXml) {
		String sessionId = null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(loginXml));
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("enl:response");
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					sessionId = getTagValue("enl:sessionId", eElement);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sessionId;
	}

	/**
	 * Parse response xml from login webservice to get session id
	 * 
	 * @param loginXml
	 * @return session id
	 */
	public synchronized String getGemsIP(String loginXml) {
		String gemsIP = null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(loginXml));
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("enl:response");
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					gemsIP = getTagValue("enl:gemIp", eElement);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return gemsIP;
	}

	private String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
				.getChildNodes();

		Node nValue = (Node) nlList.item(0);

		return nValue.getNodeValue();
	}

}
