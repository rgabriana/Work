package com.enlightedportal.ws.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.enlightedportal.model.Globals;
/**
 * Class is used to make secure HTTPS connection to the provided URL and data returned from the URL is sent back to UI.
 * 
 */
public class Communication {
	private URL					urlToServer ;
	private HttpURLConnection	http ;
	private int timeout ; // timeout is given in miliseconds
	static final Logger logger = Logger.getLogger("EMS_DASHBOARD");
	private Globals m_oGlobalStatus;
	
	public Communication(String url, Globals oGlobalStatus)
	{
		timeout = 30*1000;
		m_oGlobalStatus = oGlobalStatus;
		
		try {
			urlToServer = new URL (url);
		} catch (MalformedURLException e) {
			m_oGlobalStatus.state = m_oGlobalStatus.state_comm_url_failed;
			logger.debug("EnlightedInc1 "+e.getMessage());
			logger.debug("EnlightedInc State1"+ Integer.toString(m_oGlobalStatus.state));
			return ;
		}
		
		System.setProperty("http.keepAlive", "false");

		//init http or https communication params
		
	    if (urlToServer.getProtocol().toLowerCase().equals("https")) { //HTTPS communication to be done
	    	
	        if (false == trustAllHosts()) {
	        	m_oGlobalStatus.state	= m_oGlobalStatus.state_comm_init_failed;
	        	return;
			} 
	        	
	        HttpsURLConnection https;
			try {
				https = (HttpsURLConnection) urlToServer.openConnection();
			} catch (IOException e) {
				m_oGlobalStatus.state = m_oGlobalStatus.state_comm_connect_failed;
				logger.debug("EnlightedInc2 "+e.getMessage());
				logger.debug("EnlightedInc State2"+ Integer.toString(m_oGlobalStatus.state));
				return;
			}
				
	        https.setHostnameVerifier(DO_NOT_VERIFY);
	        http = https;
	            
	    } else {//HTTP communication to be done
	    		try	{ 
	    			http = (HttpURLConnection) urlToServer.openConnection();
				} catch (IOException e) {
					m_oGlobalStatus.state = m_oGlobalStatus.state_comm_connect_failed;
					logger.debug("EnlightedInc3 "+e.getMessage());
		        	return;
				}
	    }
	    
	    http.setConnectTimeout(timeout);
	    http.setReadTimeout(timeout);
	    	    
        http.setDoOutput(true);
        http.setDoInput(true);
        try {
			http.setRequestMethod("GET");
		} catch (ProtocolException e1) {
			m_oGlobalStatus.state = m_oGlobalStatus.state_comm_init_failed;
			logger.debug("EnlightedInc4 "+e1.getMessage());
			return;
		}
		
		http.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        
		try {
			http.connect();
		} catch (IOException e) {
			m_oGlobalStatus.state = m_oGlobalStatus.state_comm_connect_failed;
			logger.debug("EnlightedInc5 "+e.getMessage());
			return;
		}
		
		m_oGlobalStatus.state = m_oGlobalStatus.state_comm_success;

	}

	/**
	 * Trust every server - Don't check for any certificate
	 */
	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
	        public boolean verify(String hostname, SSLSession session) {
	                return true;
	        }
	};

	/**
	 * Trust every server - Don't check for any certificate
	 */
	private static boolean trustAllHosts() {
	        // Create a trust manager that does not validate certificate chains
	        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
	                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                        return new java.security.cert.X509Certificate[] {};
	                }

	                public void checkClientTrusted(X509Certificate[] chain,
	                                String authType) throws CertificateException {
	                }

	                public void checkServerTrusted(X509Certificate[] chain,
	                                String authType) throws CertificateException {
	                }
	        } };

	        // Install the all-trusting trust manager
	        try {
	            SSLContext sc = SSLContext.getInstance("TLS");
	            sc.init(null, trustAllCerts, new java.security.SecureRandom());
	            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	        } catch (Exception e) {
	        	logger.debug("EnlightedInc6 "+e.getMessage());
	        	return false;
	        }
	        
	        return true;
	}
	
	/**
	 * If we need to send data to Server, Use this method
	 * @param dataToSend String to send over stream
	 * @return boolean value if data send over the stream is successful.
	 */
	public boolean sendData(String dataToSend)
	{
        OutputStreamWriter wr = null;
		try {
			wr = new OutputStreamWriter(http.getOutputStream(), "UTF-8");
			//wr.write(dataToSend);
			logger.info("senddata "+dataToSend);
	        wr.flush();
	        wr.close();
	        
		} catch (IOException e) {
			logger.debug("EnlightedInc7 "+e.getMessage());
			m_oGlobalStatus.state = m_oGlobalStatus.state_comm_send_failed;
			return false;
		}
		return true;
	}
	
	/**
	 * Data is received from the Server over the HTTP Input Stream and returned back to UI for displaying.
	 *
	 * @return boolean value if data received over the stream is successful.
	 */
	public boolean recvData()
	{
        try {
        	// Get the Input Stream
        	InputStream is = http.getInputStream();
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        	DocumentBuilder builder = factory.newDocumentBuilder();
        	Document doc = builder.parse(is);
            m_oGlobalStatus.buffer = getXMLString(doc);
           // logger.info(m_oGlobalStatus.buffer);
            return true;

		} catch (IOException e) {
			logger.debug("EnlightedInc8 "+e.getMessage());
			//e.printStackTrace();
			m_oGlobalStatus.state = m_oGlobalStatus.state_comm_read_failed;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			logger.debug("EnlightedInc9 "+e.getMessage());
		} catch (SAXException e) {
			e.printStackTrace();
			logger.debug("EnlightedInc10 "+e.getMessage());
		}
        m_oGlobalStatus.buffer="<status>1</status>";
        return false;
	}
	
	/**
	 * Data is received from the Server over the HTTP Input Stream and returned back to UI for displaying.
	 * @return String of XML; 
	 */
	private String getXMLString(Document doc)
	{
	    try
	    {
	       DOMSource domSource = new DOMSource(doc);
	       StringWriter writer = new StringWriter();
	       StreamResult result = new StreamResult(writer);
	       TransformerFactory tf = TransformerFactory.newInstance();
	       Transformer transformer = tf.newTransformer();
	       transformer.transform(domSource, result);
	       return writer.toString();
	    }
	    catch(TransformerException e)
	    {
	       e.printStackTrace();
	       logger.debug("EnlightedInc11 "+e.getMessage());
	       return null;
	    }
	} 
}
