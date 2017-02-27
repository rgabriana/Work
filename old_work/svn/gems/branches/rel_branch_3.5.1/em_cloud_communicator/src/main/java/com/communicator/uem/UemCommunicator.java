package com.communicator.uem;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.communicator.util.CommunicatorConstant;

public class UemCommunicator {
	
	public static final Logger logger = Logger.getLogger(UemCommunicator.class.getName());
	
	private static UemCommunicator communicator = null;
	
	
	private UemCommunicator() {
	}
	
	public static UemCommunicator getInstance() {
		if(communicator == null) {
			communicator = new UemCommunicator();
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
				+ CommunicatorConstant.getUsername()
				+ "</userName><password>"
				+ CommunicatorConstant.getPassword()
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
		UemCommunication scObj = new UemCommunication(url);

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
	
	
	public synchronized String postWithoutLogin(String data, String url, int timeout, String contentType, String acceptType) {
		UemCommunication c = new UemCommunication(url, null, "POST", timeout, contentType, acceptType);
		String str = c.sendWebServiceData(data);
		return str;
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
		if(CommunicatorConstant.sessionId != null && !"".equals(CommunicatorConstant.sessionId)) {
			UemCommunication c = new UemCommunication(url, CommunicatorConstant.sessionId, "GET", timeout, contentType, null);
			str = c.recvWebServiceData();
			if(str == null) {
				CommunicatorConstant.sessionId = null;
				String loginResponse = authenticate(CommunicatorConstant.HTTPS + CommunicatorConstant.uem_ip + CommunicatorConstant.loginUrl);
				CommunicatorConstant.sessionId = getSessionID(loginResponse);
				if(CommunicatorConstant.sessionId != null && !"".equals(CommunicatorConstant.sessionId)) {
					c = new UemCommunication(url, CommunicatorConstant.sessionId, "GET", timeout, contentType, null);
					str = c.recvWebServiceData();
				}
				else {
					str = CommunicatorConstant.CONNECTION_FAILURE;
				}
			}
		}
		else {
			CommunicatorConstant.sessionId = null;
			String loginResponse = authenticate(CommunicatorConstant.HTTPS + CommunicatorConstant.uem_ip + CommunicatorConstant.loginUrl);
			CommunicatorConstant.sessionId = getSessionID(loginResponse);
			if(CommunicatorConstant.sessionId != null && !"".equals(CommunicatorConstant.sessionId)) {
				UemCommunication c = new UemCommunication(url, CommunicatorConstant.sessionId, "GET", timeout, contentType, null);
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
	public synchronized String webServicePostRequest(String data, String url, int timeout, String contentType, String acceptType) {
		
		String str = null;
		if(CommunicatorConstant.sessionId != null && !"".equals(CommunicatorConstant.sessionId)) {
			UemCommunication c = new UemCommunication(url, CommunicatorConstant.sessionId, "POST", timeout, contentType, acceptType);
			str = c.sendWebServiceData(data);
			if(str == null) {
				CommunicatorConstant.sessionId = null;
				String loginResponse = authenticate(CommunicatorConstant.HTTPS + CommunicatorConstant.uem_ip + CommunicatorConstant.loginUrl);
				CommunicatorConstant.sessionId = getSessionID(loginResponse);
				if(CommunicatorConstant.sessionId != null && !"".equals(CommunicatorConstant.sessionId)) {
					c = new UemCommunication(url, CommunicatorConstant.sessionId, "POST", timeout, contentType, acceptType);
					str = c.sendWebServiceData(data);
				}
				else {
					str = CommunicatorConstant.CONNECTION_FAILURE;
				}
			}
		}
		else {
			CommunicatorConstant.sessionId = null;
			String loginResponse = authenticate(CommunicatorConstant.HTTPS + CommunicatorConstant.uem_ip + CommunicatorConstant.loginUrl);
			CommunicatorConstant.sessionId = getSessionID(loginResponse);
			if(CommunicatorConstant.sessionId != null && !"".equals(CommunicatorConstant.sessionId)) {
				UemCommunication c = new UemCommunication(url, CommunicatorConstant.sessionId, "POST", timeout, contentType, acceptType);
				str = c.sendWebServiceData(data);
			}
			else {
				str = CommunicatorConstant.CONNECTION_FAILURE;
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
		if(CommunicatorConstant.emSessionId != null && !"".equals(CommunicatorConstant.emSessionId)) {
			UemCommunication c = new UemCommunication(url, CommunicatorConstant.emSessionId, "GET", timeout, contentType, null);
			str = c.recvWebServiceData();
			if(str == null) {
				CommunicatorConstant.emSessionId = null;
				String loginResponse = login(loginXML, CommunicatorConstant.emLoginUrl);
				CommunicatorConstant.emSessionId = getSessionID(loginResponse);
				if(CommunicatorConstant.emSessionId != null && !"".equals(CommunicatorConstant.emSessionId)) {
					c = new UemCommunication(url, CommunicatorConstant.emSessionId, "GET", timeout, contentType, null);
					str = c.recvWebServiceData();
				}
				else {
					str = CommunicatorConstant.CONNECTION_FAILURE;
				}
			}
		}
		else {
			CommunicatorConstant.emSessionId = null;
			String loginResponse = login(loginXML, CommunicatorConstant.emLoginUrl);
			CommunicatorConstant.emSessionId = getSessionID(loginResponse);
			if(CommunicatorConstant.emSessionId != null && !"".equals(CommunicatorConstant.emSessionId)) {
				UemCommunication c = new UemCommunication(url, CommunicatorConstant.emSessionId, "GET", timeout, contentType, null);
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
	public synchronized String emWebServicePostRequest(String data, String url, int timeout, String contentType) {

		String str = null;
		String loginXML = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><root><request><messageType>1</messageType><body><loginRequest><appIp>"
				+ "EMS COMMUNICATOR"
				+ "</appIp></loginRequest></body></request></root>";
		if(CommunicatorConstant.emSessionId != null && !"".equals(CommunicatorConstant.emSessionId)) {
			UemCommunication c = new UemCommunication(url, CommunicatorConstant.emSessionId, "POST", timeout, contentType, null);
			str = c.sendWebServiceData(data);
			if(str == null) {
				CommunicatorConstant.emSessionId = null;
				String loginResponse = login(loginXML, CommunicatorConstant.emLoginUrl);
				CommunicatorConstant.emSessionId= getSessionID(loginResponse);
				if(CommunicatorConstant.emSessionId != null && !"".equals(CommunicatorConstant.emSessionId)) {
					c = new UemCommunication(url, CommunicatorConstant.emSessionId, "POST", timeout, contentType, null);
					str = c.sendWebServiceData(data);
				}
				else {
					str = CommunicatorConstant.CONNECTION_FAILURE;
				}
			}
		}
		else {
			CommunicatorConstant.emSessionId = null;
			String loginResponse = login(loginXML, CommunicatorConstant.emLoginUrl);
			CommunicatorConstant.emSessionId= getSessionID(loginResponse);
			if(CommunicatorConstant.emSessionId != null && !"".equals(CommunicatorConstant.emSessionId)) {
				UemCommunication c = new UemCommunication(url, CommunicatorConstant.emSessionId, "POST", timeout, contentType, null);
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
			logger.error(e, e);
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
