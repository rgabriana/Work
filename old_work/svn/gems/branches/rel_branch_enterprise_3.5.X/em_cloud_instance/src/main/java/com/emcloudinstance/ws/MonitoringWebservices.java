package com.emcloudinstance.ws;

import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.communication.template.CloudConnectionTemplate;
import com.emcloudinstance.service.CloudConfigManager;
import com.emcloudinstance.service.DatabaseManager;
import com.emcloudinstance.service.MonitoringManager;
import com.emcloudinstance.util.CommonUtils;
import com.emcloudinstance.util.DatabaseUtil;
import com.emcloudinstance.util.Wrapper;
import com.emcloudinstance.vo.FixtureHealthDataVO;
import com.emcloudinstance.vo.GatewayHealthDataVO;



@Component
@Path("/org/monitor")
public class MonitoringWebservices {


	static final Logger logger = Logger.getLogger("EmCloudInstance");

	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	@Resource
	CloudConfigManager cloudConfigManager;
	@Resource
	DatabaseManager databaseManager;
	@Resource
	DatabaseUtil databaseUtil;
	@Resource
	CommonUtils commonUtils;
	@Resource
	CloudConnectionTemplate cloudConnectionTemplate ;
	
	@Resource
	MonitoringManager monitoringManager;
	
		@Path("list/gateway/{mac}")
	    @GET
	    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    public String getGatewayList(@PathParam("mac") String mac) {
			try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Wrapper.class ,GatewayHealthDataVO.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	    	List<GatewayHealthDataVO> gateways = monitoringManager.getGatewayStats(mac);
	    	if(gateways!=null || !gateways.isEmpty())
	    	{
	    		
	    		
					return CommonUtils.marshal(jaxbMarshaller, gateways, "gatewayHealthDataVOs") ;
				
	    	}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	return null ;
	    }
		@Path("list/fixture/{mac}")
	    @GET
	    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    public String getFixtureList(@PathParam("mac") String mac) {
			try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Wrapper.class ,FixtureHealthDataVO.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	    	List<FixtureHealthDataVO> fixtures = monitoringManager.getFixtureStats(mac);
	    	if(fixtures!=null || !fixtures.isEmpty())
	    	{
	    		
	    		
					return CommonUtils.marshal(jaxbMarshaller, fixtures, "fixtureHealthDataVOs") ;
				
	    	}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	return null ;
	    }
}
