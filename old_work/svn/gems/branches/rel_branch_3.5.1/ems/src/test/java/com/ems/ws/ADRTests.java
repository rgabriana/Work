package com.ems.ws;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class ADRTests  extends AbstractEnlightedWSTest {



		@Test
		public void testRoleWiseAccessToUpdateADRTargets(){
			try{
				final String loginXML = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><root><request><messageType>1</messageType><body><loginRequest><appIp>"
						+ "EMS COMMUNICATOR"
						+ "</appIp></loginRequest></body></request></root>";
				final String loginUrl = HOST_ADDR+"/ems/wsaction.action";


				Client client = getClientWithoutAuth();

				WebResource webResource = client.resource(loginUrl);
				Builder wr = null;
				webResource.accept(MediaType.APPLICATION_XML);
				wr = webResource.type(MediaType.APPLICATION_XML);

				ClientResponse res = wr.type(MediaType.APPLICATION_XML).post(ClientResponse.class,loginXML);

				if(res == null || (res.getStatus() != ClientResponse.Status.OK.getStatusCode())){
					Assert.fail("Not able to login code is "+res.getStatus() + " Response is: "+res.getEntity(String.class));
				}


				final String updateADRTargetsURL = HOST_ADDR+"/ems/services/org/dr/updateADRTargets/";
				final String email = "Application FromEMS COMMUNICATOR";
				httpAuth = new HTTPBasicAuthFilter(email,"");
				client.addFilter(httpAuth);

				webResource = client.resource(updateADRTargetsURL);
				webResource.accept(MediaType.APPLICATION_XML);
				wr = webResource.type(MediaType.APPLICATION_XML);

				res = wr.type(MediaType.APPLICATION_XML).post(ClientResponse.class,"<dRTargets><drTarget><id></id></drTarget></dRTargets>");
				if(res != null && ((res.getStatus() == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) || (
							(res.getStatus() == ClientResponse.Status.BAD_REQUEST.getStatusCode())
						))  ){
					Assert.fail("The target should be accessible but code is "+res.getStatus() + " Response is: "+res.getEntity(String.class));
				}

				if((res.getStatus() == ClientResponse.Status.FORBIDDEN.getStatusCode())){
					Assert.fail("The target should be accessible but code is "+res.getStatus() + " Response is: "+res.getEntity(String.class));
				}

			} catch (Exception e) {
				logger.error("***FAILED***", e);
				Assert.fail(e.getMessage());
			}
		}
}
