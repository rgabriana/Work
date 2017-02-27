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
	
	private static Communicator communicator = null;
	
	
	private Communicator() {
	}
	
	public static Communicator getInstance() {
		if(communicator == null) {
			communicator = new Communicator();
		}
		return communicator;
	}

	/**
	 * To authenticate communicator with the server
	 * 
	 * @return Login respnose
	 */
	public String authenticate(String url) {
		String loginXML = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><root><request><messageType>1</messageType><body><loginRequest><userName>"
				+ Globals.getUsername()
				+ "</userName><password>"
				+ Globals.getPassword()
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
		
		logger.debug("Login Url::::" + url + " and loginXML::::" + loginXML);
		Communication scObj = new Communication(url);

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
	 * Fire a get request on the web service whose path is given
	 * 
	 * @param url
	 * @return results of the request
	 */
	public synchronized StringBuilder webServiceGetRequest(String url, int timeout, String contentType) {

		// Login and get the response for getting cookies.
		StringBuilder sb = new StringBuilder();

		String str = "";
		if(Globals.sessionId != null && !"".equals(Globals.sessionId)) {
			Communication c = new Communication(url, Globals.sessionId, "GET", timeout, contentType);
			str = c.recvWebServiceData();
			if(str == null) {
				Globals.sessionId = null;
				String loginResponse = authenticate(Globals.HTTPS + Globals.uem_ip + Globals.loginUrl);
				Globals.sessionId = getSessionID(loginResponse);
				if(Globals.sessionId != null && !"".equals(Globals.sessionId)) {
					c = new Communication(url, Globals.sessionId, "GET", timeout, contentType);
					str = c.recvWebServiceData();
				}
				else {
					str = Globals.CONNECTION_FAILURE;
				}
			}
		}
		else {
			Globals.sessionId = null;
			String loginResponse = authenticate(Globals.HTTPS + Globals.uem_ip + Globals.loginUrl);
			Globals.sessionId = getSessionID(loginResponse);
			if(Globals.sessionId != null && !"".equals(Globals.sessionId)) {
				Communication c = new Communication(url, Globals.sessionId, "GET", timeout, contentType);
				str = c.recvWebServiceData();
			}
			else {
				str = Globals.CONNECTION_FAILURE;
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
	public synchronized String webServicePostRequest(String data, String url, int timeout, String contentType) {
		
		String str = null;
		if(Globals.sessionId != null && !"".equals(Globals.sessionId)) {
			Communication c = new Communication(url, Globals.sessionId, "POST", timeout, contentType);
			str = c.sendWebServiceData(data);
			if(str == null) {
				Globals.sessionId = null;
				String loginResponse = authenticate(Globals.HTTPS + Globals.uem_ip + Globals.loginUrl);
				Globals.sessionId = getSessionID(loginResponse);
				if(Globals.sessionId != null && !"".equals(Globals.sessionId)) {
					c = new Communication(url, Globals.sessionId, "POST", timeout, contentType);
					str = c.sendWebServiceData(data);
				}
				else {
					str = Globals.CONNECTION_FAILURE;
				}
			}
		}
		else {
			Globals.sessionId = null;
			String loginResponse = authenticate(Globals.HTTPS + Globals.uem_ip + Globals.loginUrl);
			Globals.sessionId = getSessionID(loginResponse);
			if(Globals.sessionId != null && !"".equals(Globals.sessionId)) {
				Communication c = new Communication(url, Globals.sessionId, "POST", timeout, contentType);
				str = c.sendWebServiceData(data);
			}
			else {
				str = Globals.CONNECTION_FAILURE;
			}
		}
		return str;

	}
	
	
	/**
	 * Fire a get request on the webservice whose path is given
	 * 
	 * @param url
	 * @return results of the request
	 */
	public synchronized StringBuilder emWebServiceGetRequest(String url, int timeout, String contentType) {

		StringBuilder sb = new StringBuilder();
		String loginXML = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><root><request><messageType>1</messageType><body><loginRequest><appIp>"
				+ "EMS COMMUNICATOR"
				+ "</appIp></loginRequest></body></request></root>";
		String str = null;
		if(Globals.emSessionId != null && !"".equals(Globals.emSessionId)) {
			Communication c = new Communication(url, Globals.emSessionId, "GET", timeout, contentType);
			str = c.recvWebServiceData();
			if(str == null) {
				Globals.emSessionId = null;
				String loginResponse = login(loginXML, Globals.emLoginUrl);
				Globals.emSessionId = getSessionID(loginResponse);
				if(Globals.emSessionId != null && !"".equals(Globals.emSessionId)) {
					c = new Communication(url, Globals.emSessionId, "GET", timeout, contentType);
					str = c.recvWebServiceData();
				}
				else {
					str = Globals.CONNECTION_FAILURE;
				}
			}
		}
		else {
			Globals.emSessionId = null;
			String loginResponse = login(loginXML, Globals.emLoginUrl);
			Globals.emSessionId = getSessionID(loginResponse);
			if(Globals.emSessionId != null && !"".equals(Globals.emSessionId)) {
				Communication c = new Communication(url, Globals.emSessionId, "GET", timeout, contentType);
				str = c.recvWebServiceData();
			}
			else {
				str = Globals.CONNECTION_FAILURE;
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
	public synchronized String emWebServicePostRequest(String data, String url, int timeout, String contentType) {

		String str = null;
		String loginXML = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><root><request><messageType>1</messageType><body><loginRequest><appIp>"
				+ "EMS COMMUNICATOR"
				+ "</appIp></loginRequest></body></request></root>";
		if(Globals.emSessionId != null && !"".equals(Globals.emSessionId)) {
			Communication c = new Communication(url, Globals.emSessionId, "POST", timeout, contentType);
			str = c.sendWebServiceData(data);
			if(str == null) {
				Globals.emSessionId = null;
				String loginResponse = login(loginXML, Globals.emLoginUrl);
				Globals.emSessionId= getSessionID(loginResponse);
				if(Globals.emSessionId != null && !"".equals(Globals.emSessionId)) {
					c = new Communication(url, Globals.emSessionId, "POST", timeout, contentType);
					str = c.sendWebServiceData(data);
				}
				else {
					str = Globals.CONNECTION_FAILURE;
				}
			}
		}
		else {
			Globals.emSessionId = null;
			String loginResponse = login(loginXML, Globals.emLoginUrl);
			Globals.emSessionId= getSessionID(loginResponse);
			if(Globals.emSessionId != null && !"".equals(Globals.emSessionId)) {
				Communication c = new Communication(url, Globals.emSessionId, "POST", timeout, contentType);
				str = c.sendWebServiceData(data);
			}
			else {
				str = Globals.CONNECTION_FAILURE;
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
