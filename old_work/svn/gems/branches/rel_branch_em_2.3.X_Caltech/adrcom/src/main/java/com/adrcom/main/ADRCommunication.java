package com.adrcom.main;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.FactoryConfigurationError;

import org.oasis_open.docs.ns.energyinterop._201110.EiActivePeriodType;
import org.oasis_open.docs.ns.energyinterop._201110.EiEventSignalType;
import org.oasis_open.docs.ns.energyinterop._201110.EiEventType;
import org.oasis_open.docs.ns.energyinterop._201110.EiResponse;
import org.oasis_open.docs.ns.energyinterop._201110.EventDescriptorType;
import org.oasis_open.docs.ns.energyinterop._201110.EventResponses;
import org.oasis_open.docs.ns.energyinterop._201110.EventStatusEnumeratedType;
import org.oasis_open.docs.ns.energyinterop._201110.IntervalType;
import org.oasis_open.docs.ns.energyinterop._201110.OptTypeType;
import org.oasis_open.docs.ns.energyinterop._201110.QualifiedEventIDType;
import org.oasis_open.docs.ns.energyinterop._201110.SignalPayloadType;
import org.oasis_open.docs.ns.energyinterop._201110.payloads.EiCreatedEvent;
import org.oasis_open.docs.ns.energyinterop._201110.payloads.EiRequestEvent;
import org.openadr.oadr_2_0a._2012._07.OadrCreatedEvent;
import org.openadr.oadr_2_0a._2012._07.OadrDistributeEvent;
import org.openadr.oadr_2_0a._2012._07.OadrDistributeEvent.OadrEvent;
import org.openadr.oadr_2_0a._2012._07.OadrRequestEvent;
import org.openadr.oadr_2_0a._2012._07.ResponseRequiredType;

import com.adrcom.model.DrEvent;
import com.adrcom.model.DrEventSignal;
import com.adrcom.model.DrEventSignalInterval;
import com.adrcom.service.DrEventManager;
import com.adrcom.util.XMLGregorianCalendarConversionUtil;
import com.communication.template.SecureCloudConnectionTemplate;
import com.communication.utils.CloudHttpResponse;

public class ADRCommunication {
	
    public static String VEN_ID;
    
    public static SecureCloudConnectionTemplate cloudConnectionTemplate = null;
    public static DrEventManager drEventManager = null;
    
    static final Logger logger = Logger.getLogger(ADRCommunication.class.getName());
    public static String xmlResponse = "";
    public static String sVTNHost;
    
    public static HashSet<String> validMarketContexts = new HashSet<String>();
    public static Set<String> vtnIds = new HashSet<String>();   
	
    public static String username;
    public static String password;
    public static String oadrVersion;
	
	
	public static String keystoreFileName;
	public static String truststoreFileName;
	public static String keystorePassword;
	public static String truststorePassword;
	
	public static String servicePath = "/OpenADR2/Simple/EiEvent";
    
    public static String processEvents2_0_part1() {
    	String requestId = null;
    	try {
	    	JAXBContext jaxbContext = JAXBContext.newInstance("ietf.params.xml.ns.icalendar_2:ietf.params.xml.ns.icalendar_2_0.stream:org.oasis_open.docs.ns.emix._2011._06:org.oasis_open.docs.ns.energyinterop._201110:org.oasis_open.docs.ns.energyinterop._201110.payloads:org.openadr.oadr_2_0a._2012._07");
	    	Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
	    	
	    	OadrRequestEvent oadrRequestEvent = new OadrRequestEvent();
	    	EiRequestEvent eiRequestEvent = new EiRequestEvent();
	    	eiRequestEvent.setVenID(VEN_ID); 
	    	eiRequestEvent.setRequestID("getEvents");
	    	oadrRequestEvent.setEiRequestEvent(eiRequestEvent);
	    	
	    	if(username == null || password == null)
	    	{
	    		username = "";
	    		password = "";
	    	}
	    	CloudHttpResponse resp = cloudConnectionTemplate.executePost(servicePath, asString(jaxbContext, oadrRequestEvent) , sVTNHost, MediaType.APPLICATION_XML,username,password);
	    	
	    	if(resp != null) {
		    	logger.info("=======================================");
		    	logger.info(resp.getResponse());
		    	logger.info("=======================================");
	    	}
	    	else {
	    		logger.severe("Could not connect to ADR server: " + sVTNHost);
	    		return null;
	    	}
    		
	    	if (resp.getStatus() != 200) {
	    		logger.severe("Could not receive successful response from ADR server: " + sVTNHost);
	    		return null;
	    	}
     
    		StringReader sr = new StringReader(resp.getResponse());
	    	OadrDistributeEvent distributeEvent = (OadrDistributeEvent) unmarshaller.unmarshal(sr);
	    	requestId = distributeEvent.getRequestID();
	    	
    		Boolean feedback = Boolean.FALSE;
	    	
	    	List<OadrEvent> drEvents =  distributeEvent.getOadrEvent();
	    	OadrCreatedEvent createdEvent = new OadrCreatedEvent();
	    	createdEvent.setEiCreatedEvent(new EiCreatedEvent());
	    	createdEvent.getEiCreatedEvent().setVenID(VEN_ID);
	    	
	    	EiResponse distEventResponse = new EiResponse();
	    	
	    	if(distributeEvent.getVtnID() == null || "".equals(distributeEvent.getVtnID()) || vtnIds.contains(distributeEvent.getVtnID())) {
	    		distEventResponse.setRequestID("");
		    	distEventResponse.setResponseCode("200");
		    	distEventResponse.setResponseDescription("");
	    	}
	    	else {
	    		feedback = Boolean.TRUE;
	    		distEventResponse.setRequestID(distributeEvent.getRequestID());
		    	distEventResponse.setResponseCode("401");	
		    	distEventResponse.setResponseDescription("Unknown Vtn Id");
	    	}
	    	
	    	createdEvent.getEiCreatedEvent().setEiResponse(distEventResponse);
	    	
	    	createdEvent.getEiCreatedEvent().setEventResponses(new EventResponses());
	    	if(!feedback) {
		    	List<EventResponses.EventResponse> eventResponses = createdEvent.getEiCreatedEvent().getEventResponses().getEventResponse();
		    	
		    	List<DrEvent> queuedEvents = drEventManager.getAllQueuedDREvents();
		    	Map<String, String> eventsMap = new HashMap<String, String>();
		    	for (DrEvent event: queuedEvents) {
		    		eventsMap.put(event.getEventId(), event.getEventStatus());
		    	}
		    	if(drEvents != null && drEvents.size() > 0) {
		    		String eventId = "";
		    		EventStatusEnumeratedType eventStatus = EventStatusEnumeratedType.NONE;
		    		
		    		for(OadrEvent event: drEvents) {
		    			
		    			OptTypeType opt = OptTypeType.OPT_IN;
		    			String responseCode = "200";
		    			
		    			
		    			EiEventType eiEvent = event.getEiEvent();
		    			eventId = eiEvent.getEventDescriptor().getEventID();
		    			eventStatus = eiEvent.getEventDescriptor().getEventStatus();
		    			
		    			DrEvent drEvent = drEventManager.loadDrEventByEventId(eventId);
		    			if(drEvent == null) {
		    				drEvent = new DrEvent();
		    			}
		    			if(drEvent.getEventId() != null) {
		    				eventsMap.remove(eventId);
		    				if(OptTypeType.OPT_OUT.value().equals(drEvent.getOptType()) && drEvent.getEventStatus().equals(EventStatusEnumeratedType.CANCELLED.value())) {
		    					opt = OptTypeType.OPT_OUT;
		    				}
		    				else {
			    				if(drEvent.getModificationNumber().compareTo(eiEvent.getEventDescriptor().getModificationNumber()) < 0) {
			    					transformEvent(drEvent, eiEvent, "update");
				    			}
			    				else if(drEvent.getModificationNumber().compareTo(eiEvent.getEventDescriptor().getModificationNumber()) > 0) {
			    					responseCode = "401";
			    				}
			    				else {
			    					drEvent.setEventStatus(eventStatus.value());
			    					drEvent.setCreationDateTime(XMLGregorianCalendarConversionUtil.asDate(eiEvent.getEventDescriptor().getCreatedDateTime()));
			    					drEvent = drEventManager.saveOrUpdateEvent(drEvent);
			    					if(eiEvent.getEiEventSignals().getEiEventSignal() != null) {
			    			    		for(EiEventSignalType eiSignal: eiEvent.getEiEventSignals().getEiEventSignal()) {
		    			    				DrEventSignal drEventSignal = drEventManager.loadDrEventSignalByEventIdAndSignalId(drEvent.getId(), eiSignal.getSignalID());
		    			    				drEventSignal.setCurrentPayloadValue(eiSignal.getCurrentValue().getPayloadFloat().getValue());
		    			    				drEventManager.saveOrUpdateEventSignal(drEventSignal);
			    			    		}
			    					}
			    				}
		    				}
		    			}
		    			else {
		    				for (EiEventSignalType signal: eiEvent.getEiEventSignals().getEiEventSignal()) {
		    					if (signal.getSignalName() == null || !"simple".equals(signal.getSignalName())) {
		    						opt = OptTypeType.OPT_OUT;
			    					responseCode = "401";
		    					}
		    				}
		    				
		    				if(!opt.value().equals(OptTypeType.OPT_OUT.value())) {
			    				if(eiEvent.getEiTarget().getVenID() != null && eiEvent.getEiTarget().getVenID().size() != 0 && !eiEvent.getEiTarget().getVenID().contains(VEN_ID)) {
			    					opt = OptTypeType.OPT_OUT;
			    					responseCode = "401";
			    				}
			    				else if (eiEvent.getEventDescriptor().getEiMarketContext().getMarketContext() == null || !validMarketContexts.contains(eiEvent.getEventDescriptor().getEiMarketContext().getMarketContext())) {
			    					opt = OptTypeType.OPT_OUT;
			    					responseCode = "401";
			    				}
			    				else {
				    				drEvent.setVtnId(distributeEvent.getVtnID());
				    				drEvent.setRequestId(distributeEvent.getRequestID());
				    				transformEvent(drEvent, eiEvent, "new");
			    				}
		    				}
		    				
		    			}
		    			
		    			if(event.getOadrResponseRequired().compareTo(ResponseRequiredType.ALWAYS) == 0) {
		    				feedback = Boolean.TRUE;
		    				EventResponses.EventResponse response = new EventResponses.EventResponse();
		    				response.setRequestID(distributeEvent.getRequestID());
		    				response.setResponseCode(responseCode);
		    				response.setOptType(opt);
		    				QualifiedEventIDType qualifiedEvent = new QualifiedEventIDType();
		    				qualifiedEvent.setEventID(eiEvent.getEventDescriptor().getEventID());
		    				qualifiedEvent.setModificationNumber(eiEvent.getEventDescriptor().getModificationNumber());
		    				response.setQualifiedEventID(qualifiedEvent);
		    				eventResponses.add(response);
		    			}
		    		}
		    		
		    	}
		    	
	    		for(String key: eventsMap.keySet()) {
	    			DrEvent drEvent = drEventManager.loadDrEventByEventId(key);
	    			drEvent.setEventStatus(EventStatusEnumeratedType.CANCELLED.value());
	    			drEventManager.saveOrUpdateEvent(drEvent);
	    		}
	    	}
	    	
    		if(feedback) {
    			resp = cloudConnectionTemplate.executePost(servicePath, asString(jaxbContext, createdEvent) , sVTNHost, MediaType.APPLICATION_XML);
    			logger.info("=======================================");
    			logger.info(resp.getResponse());
    			logger.info("=======================================");
    		}
            
    	}
    	catch (JAXBException e) {
    		logger.log(Level.SEVERE, e.toString(), e);
    		return null;
		} catch (FactoryConfigurationError e) {
			logger.log(Level.SEVERE, e.toString(), e);
			return null;
		}
    	catch (Exception e) {
 			logger.log(Level.SEVERE, e.toString(), e);
 			return null;
 		}
    	return requestId;
    }
    
    private static void transformEvent(DrEvent drEvent, EiEventType eiEvent, String opMode) {
    	
    	if("update".equals(opMode)) {
    		drEventManager.deleteSignalsAndIntervals(drEvent.getId());
    	}
    	else {
    		drEvent.setOptType(OptTypeType.OPT_IN.value());
    	}
    	
    	EventDescriptorType eventDesc = eiEvent.getEventDescriptor();
    	drEvent.setEventId(eventDesc.getEventID());
    	drEvent.setModificationNumber(eventDesc.getModificationNumber());
    	drEvent.setPriority(eventDesc.getPriority());
    	if(eventDesc.getEiMarketContext() != null) {
    		drEvent.setMarketContext(eventDesc.getEiMarketContext().getMarketContext());
    	}
    	drEvent.setTestEvent(eventDesc.getTestEvent());
    	drEvent.setVtnComment(eventDesc.getEventID());
    	drEvent.setCreationDateTime(XMLGregorianCalendarConversionUtil.asDate(eventDesc.getCreatedDateTime()));
    	
    	//String oldStatus = drEvent.getEventStatus();
    	//String newStatus = eventDesc.getEventStatus().value();
    	
    	drEvent.setEventStatus(eventDesc.getEventStatus().value());
    	
    	EiActivePeriodType activePeriod = eiEvent.getEiActivePeriod();
    	
    	drEvent.setStartDateTime(XMLGregorianCalendarConversionUtil.asDate(activePeriod.getProperties().getDtstart().getDateTime()));

    	drEvent.setEventDuration(activePeriod.getProperties().getDuration().getDuration());
    	
    	if(activePeriod.getProperties().getTolerance() != null && activePeriod.getProperties().getTolerance().getTolerate() != null) {
    		drEvent.setStartAfter(activePeriod.getProperties().getTolerance().getTolerate().getStartafter());
    	}
    	else {
    		drEvent.setStartAfter(null);
    	}
    	if(activePeriod.getProperties().getXEiNotification() != null) {
    		drEvent.setNotificationDuration(activePeriod.getProperties().getXEiNotification().getDuration());
    	}
    	else {
    		drEvent.setNotificationDuration(null);
    	}
    	if(activePeriod.getProperties().getXEiRampUp() != null) {
    		drEvent.setRampUpDuration(activePeriod.getProperties().getXEiRampUp().getDuration());
    	}
    	else {
    		drEvent.setRampUpDuration(null);
    	}
    	if(activePeriod.getProperties().getXEiRecovery() != null) {
    		drEvent.setRecoveryDuration(activePeriod.getProperties().getXEiRecovery().getDuration());
    	}
    	else {
    		drEvent.setRecoveryDuration(null);
    	}
    	
    	drEvent = drEventManager.saveOrUpdateEvent(drEvent);
    	
    	if(eiEvent.getEiEventSignals().getEiEventSignal() != null) {
    		for(EiEventSignalType eiSignal: eiEvent.getEiEventSignals().getEiEventSignal()) {
    			DrEventSignal eventSignal = new DrEventSignal(null, drEvent, eiSignal.getSignalID(), eiSignal.getSignalName(),
    					eiSignal.getSignalType().value(), eiSignal.getCurrentValue().getPayloadFloat().getValue());
    			
    			
    			
    			
    			if(eiSignal.getIntervals().getInterval() != null) {
    				Set<DrEventSignalInterval> intervals = new HashSet<DrEventSignalInterval>();
    				for(IntervalType interval : eiSignal.getIntervals().getInterval()) {
    					DrEventSignalInterval eventSignalInterval = new DrEventSignalInterval(null, eventSignal, interval.getDuration().getDuration(), interval.getUid().getText(), ((SignalPayloadType)interval.getStreamPayloadBase().getValue()).getPayloadFloat().getValue());
    					intervals.add(eventSignalInterval);
    				}
    				eventSignal.setIntervals(intervals);
    			}
    			eventSignal = drEventManager.saveOrUpdateEventSignal(eventSignal);
    		}
    	}
    }
    
    
    public static void processEvents2_0_part2(HashMap<String, DrEvent> idEventMap, String requestId) {
		try {
	    	if(idEventMap.size() > 0) {
			JAXBContext jaxbContext = JAXBContext.newInstance("ietf.params.xml.ns.icalendar_2:ietf.params.xml.ns.icalendar_2_0.stream:org.oasis_open.docs.ns.emix._2011._06:org.oasis_open.docs.ns.energyinterop._201110:org.oasis_open.docs.ns.energyinterop._201110.payloads:org.openadr.oadr_2_0a._2012._07");
	        	OadrCreatedEvent createdEvent = new OadrCreatedEvent();
		    	createdEvent.setEiCreatedEvent(new EiCreatedEvent());
		    	createdEvent.getEiCreatedEvent().setVenID(VEN_ID);
		    	EiResponse distEventResponse = new EiResponse();
		    	distEventResponse.setRequestID("");
		    	distEventResponse.setResponseCode("200");
		    	distEventResponse.setResponseDescription("");
		    	createdEvent.getEiCreatedEvent().setEiResponse(distEventResponse);
		    	createdEvent.getEiCreatedEvent().setEventResponses(new EventResponses());
		    	List<EventResponses.EventResponse> eventResponses = createdEvent.getEiCreatedEvent().getEventResponses().getEventResponse();
		    	
		    	for (String eventId: idEventMap.keySet()) {
		    		DrEvent event = drEventManager.loadDrEventByEventId(eventId);
		    		idEventMap.put(eventId, event);
		    		EventResponses.EventResponse response = new EventResponses.EventResponse();
					response.setRequestID(requestId);
					response.setResponseCode("200");
					response.setOptType(OptTypeType.OPT_OUT);
					QualifiedEventIDType qualifiedEvent = new QualifiedEventIDType();
					qualifiedEvent.setEventID(eventId);
					qualifiedEvent.setModificationNumber(event.getModificationNumber());
					response.setQualifiedEventID(qualifiedEvent);
					eventResponses.add(response);
		    	}
		    	
		    	CloudHttpResponse resp = cloudConnectionTemplate.executePost(servicePath, asString(jaxbContext, createdEvent) , sVTNHost, MediaType.APPLICATION_XML);
		    	
		    	logger.info("=======================================");
		    	logger.info(resp.getResponse());
		    	logger.info("=======================================");
		    	
		    	if(resp.getStatus() == 200) {
		    		for (String eventId: idEventMap.keySet()) {
		    			DrEvent event = idEventMap.get(eventId);
		    			event.setOptType(OptTypeType.OPT_OUT.value());
		    			event.setEventStatus(EventStatusEnumeratedType.CANCELLED.value());
		    			drEventManager.saveOrUpdateEvent(event);
		    		}
		    	}
	        }
		} catch (JAXBException e) {
			e.printStackTrace();
		}
    }
    
    public static String asString(JAXBContext pContext, Object pObject)
			throws JAXBException {

		java.io.StringWriter sw = new StringWriter();

		Marshaller marshaller = pContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.marshal(pObject, sw);

		return sw.toString();
	}
    

}
