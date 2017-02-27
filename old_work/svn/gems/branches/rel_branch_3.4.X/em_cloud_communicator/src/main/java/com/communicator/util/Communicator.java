package com.communicator.util;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Communicator {
	
	public static final Logger logger = Logger.getLogger(Communicator.class.getName());
	
	public static String emSessionId = "";
	
	
	public Communicator() {
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
		
		logger.debug("Login Url::::" + url + " and loginXML::::" + loginXML);
		Communication scObj = new Communication(url, "application/xml; charset=utf-8");

		if (scObj.state == 100) {
			logger.debug("send loginXML");
			if (scObj.sendData(loginXML)) {
				if (scObj.recvData()) {
					loginResponceXML = scObj.buffer;

				} else {
					loginResponceXML = Integer.toString(scObj.state);
				}
			} else {
				loginResponceXML = Integer.toString(scObj.state);
			}

		} else {
			loginResponceXML = Integer.toString(scObj.state);
		}
		logger.debug(loginResponceXML);
		return loginResponceXML;
	}
	

	/**
	 * Fire a get request on the webservice whose path is given
	 * 
	 * @param url
	 * @return results of the request
	 */
	public synchronized StringBuilder emWebServiceGetRequest(String url, int timeout, String contentType, String acceptType) {

		StringBuilder sb = new StringBuilder();
		String loginXML = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><root><request><messageType>1</messageType><body><loginRequest><appIp>"
				+ "EMS COMMUNICATOR"
				+ "</appIp></loginRequest></body></request></root>";
		String str = null;
		if(emSessionId != null && !"".equals(emSessionId)) {
			Communication c = new Communication(url, emSessionId, "GET", timeout, contentType, acceptType);
			str = c.recvWebServiceData();
			if(str == null) {
				emSessionId = null;
				String loginResponse = login(loginXML, CommunicatorConstant.emLoginUrl);
				emSessionId = getSessionID(loginResponse);
				if(emSessionId != null && !"".equals(emSessionId)) {
					c = new Communication(url, emSessionId, "GET", timeout, contentType, acceptType);
					str = c.recvWebServiceData();
				}
				else {
					str = CommunicatorConstant.CONNECTION_FAILURE;
				}
			}
		}
		else {
			emSessionId = null;
			String loginResponse = login(loginXML, CommunicatorConstant.emLoginUrl);
			emSessionId = getSessionID(loginResponse);
			if(emSessionId != null && !"".equals(emSessionId)) {
				Communication c = new Communication(url, emSessionId, "GET", timeout, contentType, acceptType);
				str = c.recvWebServiceData();
			}
			else {
				str = CommunicatorConstant.CONNECTION_FAILURE;
			}
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
	public synchronized String emWebServicePostRequest(String data, String url, int timeout, String contentType, String acceptType) {

		String str = null;
		String loginXML = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><root><request><messageType>1</messageType><body><loginRequest><appIp>"
				+ "EMS COMMUNICATOR"
				+ "</appIp></loginRequest></body></request></root>";
		if(emSessionId != null && !"".equals(emSessionId)) {
			Communication c = new Communication(url, emSessionId, "POST", timeout, contentType, acceptType);
			str = c.sendWebServiceData(data);
			if(str == null) {
				emSessionId = null;
				String loginResponse = login(loginXML, CommunicatorConstant.emLoginUrl);
				emSessionId= getSessionID(loginResponse);
				if(emSessionId != null && !"".equals(emSessionId)) {
					c = new Communication(url, emSessionId, "POST", timeout, contentType, acceptType);
					str = c.sendWebServiceData(data);
				}
				else {
					str = CommunicatorConstant.CONNECTION_FAILURE;
				}
			}
		}
		else {
			emSessionId = null;
			String loginResponse = login(loginXML, CommunicatorConstant.emLoginUrl);
			emSessionId= getSessionID(loginResponse);
			if(emSessionId != null && !"".equals(emSessionId)) {
				Communication c = new Communication(url, emSessionId, "POST", timeout, contentType, acceptType);
				str = c.sendWebServiceData(data);
			}
			else {
				str = CommunicatorConstant.CONNECTION_FAILURE;
			}
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
			logger.error(e);
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
