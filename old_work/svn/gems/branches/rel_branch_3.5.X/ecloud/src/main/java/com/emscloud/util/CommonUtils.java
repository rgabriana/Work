package com.emscloud.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.emscloud.model.SystemConfiguration;
import com.emscloud.service.SystemConfigurationManager;

public class CommonUtils {

	public static Logger logger = Logger.getLogger(CommonUtils.class.getName());

	public static boolean isNull(final Object argumentValue) {
		return (argumentValue == null);
	}

	public static boolean isNullOrEmpty(final String argumentValue) {
		return (isNull(argumentValue) || argumentValue.length() ==0);
	}

	@SuppressWarnings("unchecked")
	public static boolean isNullOrEmpty(Collection argumentValue) {
		return (isNull(argumentValue) || argumentValue.size() == 0);
	}

	public static void checkNull(final String argumentName, final Object argumentValue) {
		if (isNull(argumentValue))
			throw new IllegalArgumentException("The supplied property " + argumentName + " was null");
	}

	public static void checkNullOrEmpty(final String argumentName, final String argumentValue) {
		if (isNullOrEmpty(argumentValue))
			throw new IllegalArgumentException("The supplied String property " + argumentName + " was null or empty");
	}
	
	public static int getRandomPort() {
		int port = 0;
		try {
			ServerSocket server = new ServerSocket(0);
			port = server.getLocalPort();
			server.close();
		} catch (Exception e) {
			logger.info("Error while getting ssh port" + e.getMessage() , e);
			port = 0;
		}

		return port;
	}

	public static String getHostName() {
		String hostName = null;
		Runtime run = Runtime.getRuntime();
		BufferedReader buf = null;
		try {
			Process pr = run.exec("hostname");
			buf = new BufferedReader(new InputStreamReader(
					pr.getInputStream()));
			String line = "";
			while ((line = buf.readLine()) != null) {
				hostName = line;
			}
			pr.waitFor();
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		finally {
			IOUtils.closeQuietly(buf);
		}

		return hostName;
	}

	public static void reloadApache() {
		Runtime run = Runtime.getRuntime();
		BufferedReader buf =  null;
		try {
			Process pr = run.exec("sudo /etc/init.d/apache2 reload");
			buf = new BufferedReader(new InputStreamReader(
					pr.getInputStream()));
			String line = "";
			while ((line = buf.readLine()) != null) {
				System.out.println(line);
				logger.info(line);
			}
			pr.waitFor();
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		finally {
			IOUtils.closeQuietly(buf);
		}

	}

	/**
	 * Convenience method to get the application's URL based on request
	 * variables.
	 */
	public static String getAppURL(HttpServletRequest request) {
		StringBuffer url = new StringBuffer();
		int port = request.getServerPort();
		if (port < 0) {
			port = 80; // Work around java.net.URL bug
		}
		String scheme = request.getScheme();
		url.append(scheme);
		url.append("://");
		url.append(request.getServerName());
		if ((scheme.equals("http") && (port != 80))
				|| (scheme.equals("https") && (port != 443))) {
			url.append(':');
			url.append(port);
		}
		url.append(request.getContextPath());
		return url.toString();
	}
	 /**
     * Unmarshal XML to Wrapper and return List value.
     */
	@SuppressWarnings("unchecked")
	public static <T> List<T> unmarshal(Unmarshaller unmarshaller,
			Class<T> clazz, String xml) throws JAXBException {
		 StreamSource xmlSource = new StreamSource(new StringReader (xml));
		Wrapper<T> wrapper = (Wrapper<T>) unmarshaller.unmarshal(xmlSource,Wrapper.class).getValue();
		return wrapper.getItems();
	}
	 /**
     * Wrap List in Wrapper, then leverage JAXBElement to supply root element
     * information.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static String marshal(Marshaller marshaller, List<?> list, String name)
            throws JAXBException {
    	StringWriter sw = new StringWriter();
        QName qName = new QName(name);
        Wrapper wrapper = new Wrapper(list);
        JAXBElement<Wrapper> jaxbElement = new JAXBElement<Wrapper>(qName,
                Wrapper.class, wrapper);
        marshaller.marshal(jaxbElement, sw);
        return sw.toString() ;
    }
    
	public static String getSaltBasedDigest(String key, String secret,
			String salt, String algo) throws Exception  {
		if (isNullOrEmpty(key)
				|| isNullOrEmpty(secret)
				|| isNullOrEmpty(salt)
				|| isNullOrEmpty(algo)) {
			throw new NullPointerException(
					"All parameters required to create digest.");
		}
		MessageDigest md =null;
		try{
		 md = MessageDigest.getInstance(algo);
		}catch(NoSuchAlgorithmException e){
			logger.warn("No Such algorithm exception. falling over to default algo for creating digest which is SHA-1", e);
			 md = MessageDigest.getInstance("SHA-1");
		}
		md.reset();
		byte[] keyByte = (key+secret+salt).getBytes(Charset.forName("utf-8")); 
		md.update(keyByte);
		byte[] digest = md.digest();
		return getByteString(digest);
	}
	
	private static String getByteString(byte[] bytes) {
		StringBuffer oBuffer = new StringBuffer();
		int noOfBytes = bytes.length;
		for (int i = 0; i < noOfBytes; i++) {
			 oBuffer.append(String.format("%02x", bytes[i]));
		}
		return oBuffer.toString();
	}

	public static String getMetaDataServer(SystemConfigurationManager systemConfigurationManager) {
		
		String ip = "localhost";
		try {
			if (systemConfigurationManager == null) {
				return ip;
			}
			SystemConfiguration metaDataServer = systemConfigurationManager.loadConfigByName("MetaDataServer.IP");
			if (metaDataServer == null || metaDataServer.getValue() == null || metaDataServer.getValue().isEmpty()) {
				return ip;
			}
			ip = metaDataServer.getValue();					
		} catch (Exception e) {			
		}
		return ip;
		
	} //end of method getMetaDataServer
	
	public static void sleep(int sec) {
	    try {
	      Thread.sleep(sec * 1000);
	    }
	    catch(Exception ex) {
	    	logger.error("Error:Sleeping the thread", ex);
	    }
	} //end of method sleep
	
	 public static void readStreamOfProcess(final Process pr){
		 readStreamInThread(pr.getInputStream(),false, pr);
		 readStreamInThread(pr.getErrorStream(),true, pr);
	 }
	 private static boolean isProcessRunning(Process process) {
		    try {
		        process.exitValue();
		        return false;
		    } catch (Exception e) {
		        return true;
		    }
		}
	 private static void readStreamInThread(final InputStream stream, final boolean isErrorStream, final Process pr) {

	        new Thread() {
	            public void run() {
	                BufferedReader br = null;
	                try {
	                    sleep(1);
	                    br = new BufferedReader(new InputStreamReader(stream));
	                    String line = "";
	                    StringTokenizer st = null;
	                    while (true) {
	                        line = br.readLine();
	                        if (line == null) {
	                            break;
	                        }else{
	                        	if(isErrorStream){
	                        		logger.error("WARNING: Error Observed in the Process Error Stream: "+ line);
	                        	}else{
	                        		//logger.error("INFO: INFO: "+ line);
	                        	}
	                        }
	                    }
	                } catch (Exception e) {
	                	logger.error("ERROR: Reading inputstream",e);
	                } finally {
	                    if (br != null) {
	                        try {
	                            br.close();
	                        } catch (Exception e) {
	                        	logger.error("ERROR: During closing BufferedReader:",e);
	                        }
	                    }
	                }
	            }
	        }.start();

	    } // end of method readErrorStream

}
