package com.enlightedinc.adr.main;

import java.io.StringReader;

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
	public synchronized StringBuilder webServiceGetRequest(String url, int timeout) {

		StringBuilder sb = new StringBuilder();
		// Login and get the response for getting cookies.
		String loginXML = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><root><request><messageType>1</messageType><body><loginRequest><appIp>"
				+ "EMS COMMUNICATOR"
				+ "</appIp></loginRequest></body></request></root>";
		String loginResponse = login(loginXML, Globals.loginUrl);
		String sessionId = getSessionID(loginResponse);
		Communication c = new Communication(url, sessionId, "GET", timeout);
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
	public synchronized String webServicePostRequest(String data, String url, int timeout) {
		// Login and get the response for getting cookies.
		String loginXML = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><root><request><messageType>1</messageType><body><loginRequest><appIp>"
				+ "EMS COMMUNICATOR"
				+ "</appIp></loginRequest></body></request></root>";
		String loginResponse = login(loginXML, Globals.loginUrl);

		String sessionId = getSessionID(loginResponse);
		Communication c = new Communication(url, sessionId, "POST", timeout);

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

	private String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
				.getChildNodes();

		Node nValue = (Node) nlList.item(0);

		return nValue.getNodeValue();
	}

}
