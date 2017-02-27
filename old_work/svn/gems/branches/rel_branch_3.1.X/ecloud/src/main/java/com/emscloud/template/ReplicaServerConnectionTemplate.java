package com.emscloud.template;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;*/
import org.springframework.stereotype.Component;

@Component("replicaServerConnectionTemplate")
public class ReplicaServerConnectionTemplate {
	
	static final Logger logger = Logger.getLogger(ReplicaServerConnectionTemplate.class
			.getName());


	
	public byte[] executeGet(String service,String replicaServer){
		
		byte[] responseBody = null;
		/*Protocol easyhttps = new Protocol("https",
				new EasySSLProtocolSocketFactory(), 443);
		GetMethod get = new GetMethod(service);

		HostConfiguration hc = new HostConfiguration();
		hc.setHost(replicaServer, 443, easyhttps);
		HttpClient client = new HttpClient();

		try {
			client.executeMethod(hc, get);
			responseBody = get.getResponseBody();
		} catch (HttpException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}*/
		return responseBody;
	}
	

}
