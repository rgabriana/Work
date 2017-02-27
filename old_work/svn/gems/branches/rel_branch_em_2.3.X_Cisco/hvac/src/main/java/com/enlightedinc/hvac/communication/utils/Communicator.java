package com.enlightedinc.hvac.communication.utils;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.enlightedinc.hvac.utils.Globals;

public class Communicator {

	/**
	 * To authenticate communicator with the server
	 * 
	 * @return Login respnose
	 */
	public String authenticate(String url) {

		String loginXML = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><root><request><messageType>1</messageType><body><loginRequest><userName>"
				+ Globals.em_username
				+ "</userName><password>"
				+ Globals.em_password
				+ "</password></loginRequest></body></request></root>";
		return login(loginXML, url);
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
	 * Fire a get request on the web service whose path is given
	 * 
	 * @param url
	 * @return results of the request
	 */
	public synchronized StringBuilder webServiceGetRequest(String url, int timeout, String contentType) {

		// Login and get the response for getting cookies.
		StringBuilder sb = new StringBuilder();

		String str = null;
		if(Globals.sessionId != null && !"".equals(Globals.sessionId)) {
			Communication c = new Communication(url, Globals.sessionId, "GET", timeout, contentType);
			str = c.recvWebServiceData();
			if(str == null) {
				String loginResponse = authenticate(Globals.loginUrl);
				Globals.sessionId = getSessionID(loginResponse);
				c = new Communication(url, Globals.sessionId, "GET", timeout, contentType);
				str = c.recvWebServiceData();
			}
		}
		else {
			String loginResponse = authenticate(Globals.loginUrl);
			Globals.sessionId = getSessionID(loginResponse);
			Communication c = new Communication(url, Globals.sessionId, "GET", timeout, contentType);
			str = c.recvWebServiceData();
		}
		
		sb.append(str);
		return sb;
	}

	/**
	 * Fire a get request on the webservice whose path is given
	 * 
	 * @param url
	 * @return results of the request
	 */
	public synchronized String webServicePostRequest(String data, String url, int timeout, String contentType) {
		
		String str = null;
		if(Globals.sessionId != null && !"".equals(Globals.sessionId)) {
			Communication c = new Communication(url, Globals.sessionId, "POST", timeout, contentType);
			str = c.sendWebServiceData(data);
			if(str == null) {
				String loginResponse = authenticate(Globals.loginUrl);
				Globals.sessionId = getSessionID(loginResponse);
				c = new Communication(url, Globals.sessionId, "POST", timeout, contentType);
				str = c.sendWebServiceData(data);
			}
		}
		else {
			String loginResponse = authenticate(Globals.loginUrl);
			Globals.sessionId = getSessionID(loginResponse);
			Communication c = new Communication(url, Globals.sessionId, "POST", timeout, contentType);
			str = c.sendWebServiceData(data);
		}
		return str;

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

	private String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
				.getChildNodes();

		Node nValue = (Node) nlList.item(0);

		return nValue.getNodeValue();
	}

}
