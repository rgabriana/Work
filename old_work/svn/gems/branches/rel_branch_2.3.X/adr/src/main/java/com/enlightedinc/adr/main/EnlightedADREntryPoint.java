package com.enlightedinc.adr.main;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.oasis_open.docs.ns.energyinterop._201110.EiActivePeriodType;
import org.oasis_open.docs.ns.energyinterop._201110.EiEventSignalType;
import org.oasis_open.docs.ns.energyinterop._201110.EiEventType;
import org.oasis_open.docs.ns.energyinterop._201110.EiResponse;
import org.oasis_open.docs.ns.energyinterop._201110.EventDescriptorType;
import org.oasis_open.docs.ns.energyinterop._201110.EventResponses;
import org.oasis_open.docs.ns.energyinterop._201110.IntervalType;
import org.oasis_open.docs.ns.energyinterop._201110.OptTypeType;
import org.oasis_open.docs.ns.energyinterop._201110.QualifiedEventIDType;
import org.oasis_open.docs.ns.energyinterop._201110.SignalPayloadType;
import org.oasis_open.docs.ns.energyinterop._201110.payloads.EiCreatedEvent;
import org.openadr.oadr_2_0a._2012._07.OadrCreatedEvent;
import org.openadr.oadr_2_0a._2012._07.OadrDistributeEvent;
import org.openadr.oadr_2_0a._2012._07.OadrDistributeEvent.OadrEvent;
import org.openadr.oadr_2_0a._2012._07.ResponseRequiredType;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.enlightedinc.adr.model.ADRTarget;
import com.enlightedinc.adr.model.DrEvent;
import com.enlightedinc.adr.model.DrEventSignal;
import com.enlightedinc.adr.model.DrEventSignalInterval;
import com.enlightedinc.adr.service.ADRTargetManager;
import com.enlightedinc.adr.service.DrEventManager;
import com.enlightedinc.adr.util.DurationToMilliSec;
import com.enlightedinc.adr.util.XMLGregorianCalendarConversionUtil;

public class EnlightedADREntryPoint {
	
    private static int adrInterval = 60*1000; // 1 minute
    private static ADRTargetManager adrTargetManager  = null;
    private static DrEventManager drEventManager  = null;
    
    static final Logger logger = Logger.getLogger(EnlightedADREntryPoint.class.getName());
    static String xmlResponse = "";
    
    
    static {
        try {
            logger.setLevel(Level.INFO); 
            LogManager lm = LogManager.getLogManager();
            lm.addLogger(logger);
        }
        catch (Throwable e) {
        	logger.log(Level.SEVERE, e.toString(), e);
        }
    }


	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
				"/META-INF/spring/applicationContext-services.xml");
		adrTargetManager = (ADRTargetManager) SpringContext.getBean("adrTargetManager");
		drEventManager = (DrEventManager) SpringContext.getBean("drEventManager");
		
		//processEvents2_0(null, null, null);
		
        while(true) {
        	try {
        		xmlResponse = "";
        		pollServer();
				Thread.sleep(adrInterval);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, e.toString(), e);
			}
        	catch (Exception e) {
        		logger.log(Level.SEVERE, e.toString(), e);
        	}
        }
	}
	
    private static void processEvents2_0(String serviceUrl, String username, String password) {
    	try {
	    	JAXBContext jaxbContext = JAXBContext.newInstance("ietf.params.xml.ns.icalendar_2:ietf.params.xml.ns.icalendar_2_0.stream:org.oasis_open.docs.ns.emix._2011._06:org.oasis_open.docs.ns.energyinterop._201110:org.oasis_open.docs.ns.energyinterop._201110.payloads:org.openadr.oadr_2_0a._2012._07");
	    	Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
	    	
	    	InputStream inputStream = new FileInputStream("/home/enlighted/distribute.xml");
	    	
	    	OadrDistributeEvent distributeEvent = (OadrDistributeEvent) unmarshaller.unmarshal(inputStream);
	    	
	    	List<OadrEvent> drEvents =  distributeEvent.getOadrEvent();
	    	OadrCreatedEvent createdEvent = new OadrCreatedEvent();
	    	createdEvent.setEiCreatedEvent(new EiCreatedEvent());
	    	createdEvent.getEiCreatedEvent().setVenID("get from config table");
	    	EiResponse distEventResponse = new EiResponse();
	    	distEventResponse.setRequestID(distributeEvent.getRequestID());
	    	createdEvent.getEiCreatedEvent().setEiResponse(distEventResponse);
	    	createdEvent.getEiCreatedEvent().setEventResponses(new EventResponses());
	    	List<EventResponses.EventResponse> eventResponses = createdEvent.getEiCreatedEvent().getEventResponses().getEventResponse();
	    	//TODO send createdEvent object back to server afteer the whole processing.
	    	
	    	if(drEvents != null && drEvents.size() > 0) {
	    		for(OadrEvent event: drEvents) {
	    			EiEventType eiEvent = event.getEiEvent();
	    			DrEvent drEvent = drEventManager.loadDrEventByEventId(eiEvent.getEventDescriptor().getEventID());
	    			if(drEvent == null) {
	    				drEvent = new DrEvent();
	    			}
	    			if(drEvent.getEventId() != null) {
	    				if(drEvent.getModificationNumber().compareTo(eiEvent.getEventDescriptor().getModificationNumber()) != 0) {
	    					transformEvent(drEvent, eiEvent, "update");
		    			}
	    				else {
	    					drEvent.setEventStatus(eiEvent.getEventDescriptor().getEventStatus().value());
	    					drEvent.setCreationDateTime(XMLGregorianCalendarConversionUtil.asDate(eiEvent.getEventDescriptor().getCreatedDateTime()));
	    					if(eiEvent.getEiEventSignals().getEiEventSignal() != null) {
	    			    		for(EiEventSignalType eiSignal: eiEvent.getEiEventSignals().getEiEventSignal()) {
    			    				DrEventSignal drEventSignal = drEventManager.loadDrEventSignalByEventIdAndSignalId(drEvent.getId(), eiSignal.getSignalID());
    			    				drEventSignal.setCurrentPayloadValue(eiSignal.getCurrentValue().getPayloadFloat().getValue());
    			    				drEventManager.saveOrUpdateEventSignal(drEventSignal);
	    			    		}
	    					}
	    				}
	    			}
	    			else {
	    				drEvent.setVtnId(distributeEvent.getVtnID());
	    				drEvent.setRequestId(distributeEvent.getRequestID());
	    				transformEvent(drEvent, eiEvent, "new");
	    			}
	    			if(event.getOadrResponseRequired().compareTo(ResponseRequiredType.ALWAYS) == 0) {
	    				EventResponses.EventResponse response = new EventResponses.EventResponse();
	    				response.setRequestID(distributeEvent.getRequestID());
	    				response.setResponseCode("200"); //TODO check response codes
	    				response.setOptType(OptTypeType.OPT_IN); //TODO check opt logic
	    				QualifiedEventIDType qualifiedEvent = new QualifiedEventIDType();
	    				qualifiedEvent.setEventID(eiEvent.getEventDescriptor().getEventID());
	    				qualifiedEvent.setModificationNumber(eiEvent.getEventDescriptor().getModificationNumber());
	    				response.setQualifiedEventID(qualifiedEvent);
	    				
	    				eventResponses.add(response);
	    			}
	    		}
	    	}
    	}
    	catch (JAXBException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }
    
    private static void transformEvent(DrEvent drEvent, EiEventType eiEvent, String opMode) {
    	
    	if("update".equals(opMode)) {
    		drEventManager.deleteSignalsAndIntervals(drEvent.getId());
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
    	
    	String oldStatus = drEvent.getEventStatus();
    	String newStatus = eventDesc.getEventStatus().value();
    	
    	drEvent.setEventStatus(eventDesc.getEventStatus().value());
    	
    	EiActivePeriodType activePeriod = eiEvent.getEiActivePeriod();
    	
    	drEvent.setStartDateTime(XMLGregorianCalendarConversionUtil.asDate(activePeriod.getProperties().getDtstart().getDateTime()));
    	
    	Date startDate = drEvent.getStartDateTime();

    	drEvent.setEventDuration(activePeriod.getProperties().getDuration().getDuration());
    	
    	Date endDate = new Date(startDate.getTime() + DurationToMilliSec.getMilliSecFromDuration(drEvent.getEventDuration()));
    	
    	drEvent.setStartAfter(activePeriod.getProperties().getTolerance().getTolerate().getStartafter());
    	drEvent.setNotificationDuration(activePeriod.getProperties().getXEiNotification().getDuration());
    	drEvent.setRampUpDuration(activePeriod.getProperties().getXEiRampUp().getDuration());
    	drEvent.setRecoveryDuration(activePeriod.getProperties().getXEiRecovery().getDuration());
    	
    	drEvent = drEventManager.saveOrUpdateEvent(drEvent);
    	
    	String signalType = "";
    	Double payloadValue = 0.0;
    	
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
    	
    	signalType = eiEvent.getEiEventSignals().getEiEventSignal().get(0).getSignalType().value();
    	if(signalType != null && !"".equals(signalType)) {
    		if("delta".equals(signalType)) {
    			signalType = "load";
    		}
    		else if("price".equals(signalType)) {
    			signalType = "priceAbsolute";
    		}
    		
    		if("level".equals(signalType)) {
    			Float levelVal = eiEvent.getEiEventSignals().getEiEventSignal().get(0).getCurrentValue().getPayloadFloat().getValue();
    			if(levelVal.compareTo(new Float("0.0")) == 0) {
    				payloadValue = 0.1;
    			} 
    			else if(levelVal.compareTo(new Float("1.0")) == 0) {
    				payloadValue = 0.25;
    			}
    			else {
    				payloadValue = 0.5;
    			}
    		}
    		else {
    			payloadValue = new Double(eiEvent.getEiEventSignals().getEiEventSignal().get(0).getCurrentValue().getPayloadFloat().getValue());
    		}
    		
    	}
    	
    	try {
    		System.out.println(startDate + "  " + endDate + "  "  + signalType + "  " + payloadValue);
    		initDR(startDate, endDate, signalType, payloadValue);
    	}
    	catch (Exception e) {
    		logger.log(Level.SEVERE, e.toString(), e);
    	}
    	
    	
    }
	
	
	/**
	 * @param r2
	 * We expect the load reduction calculation factor in either of the following formats:
	 * 	1) Absolute expected load reduction in kW or
	 * 	2) Absolute price during dr event or
	 * 	3) Relative price during dr event (New Price/Old Price) or
	 * 	4) DR Mode High leads to 50% reduction or
	 * 	5) DR Mode Moderate leads to 25% reduction or
	 * 	6) DR Mode Normal leads to 10% reduction
	 */
	private static void processEvent(ADRRestWSClient r2) {
		List<ADRTarget> queuedEvents = adrTargetManager.getAllQueuedADRTargets();
		if(queuedEvents != null && queuedEvents.size() > 0) {
			for(ADRTarget adrTarget: queuedEvents) {
				Integer index = r2.eventMap.get(adrTarget.getDrIdentifier());
				if(index != null) {
					ADRTarget adrTargetTemp = r2.adrTargetArray.get(index);
					r2.eventMap.remove(adrTarget.getDrIdentifier());
					String oldStatus = adrTarget.getDrStatus();
					if(("FAR".equals(oldStatus) || "NEAR".equals(oldStatus)) && "ACTIVE".equals(adrTargetTemp.getDrStatus())) {
						logger.info(xmlResponse);
						adrTarget.setDrStatus(adrTargetTemp.getDrStatus());
						adrTargetManager.saveOrUpdateADRTarget(adrTarget);
						try {
							
							double payloadValue = 0;
							String signalType = "load";
							
							if (adrTargetTemp.getLoadAmount() != null && adrTargetTemp.getLoadAmount() != 0) {
								signalType = "load";
								payloadValue = adrTargetTemp.getLoadAmount();
							}
							else if (adrTargetTemp.getPriceAbsolute() != null && adrTargetTemp.getPriceAbsolute() != 0) {
								signalType = "priceAbsolute";
								payloadValue = adrTargetTemp.getPriceAbsolute();
							}
							else if (adrTargetTemp.getPriceRelative() != null && adrTargetTemp.getPriceRelative() != 0) {
								signalType = "priceRelative";
								payloadValue = adrTargetTemp.getPriceRelative();
							}
							else if (("HIGH").equals(adrTargetTemp.getOperationMode())) {
								signalType = "level";
								payloadValue = 0.5;
							}
							else if (("MODERATE").equals(adrTargetTemp.getOperationMode())) {
								signalType = "level";
								payloadValue = 0.25;
							}
							else if (("NORMAL").equals(adrTargetTemp.getOperationMode())) {
								signalType = "level";
								payloadValue = 0.1;
							}
							initDR(adrTargetTemp.getStartTime(), adrTargetTemp.getEndTime(), signalType, payloadValue);
						}
						catch (Exception e) {
							logger.log(Level.SEVERE, e.toString(), e);
							adrTarget.setDrStatus(oldStatus);
							adrTargetManager.saveOrUpdateADRTarget(adrTarget);
							return;
						}
						
					}
					else {
						adrTarget.setDrStatus(adrTargetTemp.getDrStatus());
						adrTargetManager.saveOrUpdateADRTarget(adrTarget);
					}
				}
				else {
					//event is canceled or finished.
					//TODO notify GEMS and restore fixtures?
					adrTarget.setDrStatus("CANCEL_OVER");
					adrTargetManager.saveOrUpdateADRTarget(adrTarget);
				}
			}
		}
		Set<String> eventKeys = r2.eventMap.keySet();
		if (!eventKeys.isEmpty()) {
			logger.info(xmlResponse);
		}
		for(String eventKey: eventKeys) {
			adrTargetManager.saveOrUpdateADRTarget(r2.adrTargetArray.get(r2.eventMap.get(eventKey)));
		}
	}
	
	private static void initDR(Date startTime, Date endTime, String signalType, double payloadValue) throws UnsupportedEncodingException, SAXException, IOException, ParserConfigurationException, FactoryConfigurationError {
		
		Communicator communicator = new Communicator();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String fixtureRecordsEndPoint = "https://localhost/ems/services/org/dr/fixture/record/" + 
									sdf.format(startTime) + "/" 
									+ sdf.format(endTime);
		String currentPricingEndPoint = "https://localhost/ems/services/org/dr/pricing/current";
		
		logger.info(fixtureRecordsEndPoint);
        
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = null;

        Element root = null;
        NodeList nodelist = null;
        List<String[]> drRecords = new ArrayList<String[]>();

    	Integer percentReduction = null;
    	double currentLoad = 0;
    	double normalPricing = 1;
    	double normalLoad = 0;
    	double tempSum = 0.0;
    	double kscale = 0;
    	double loadReductionReqd = 0;
    	long durationInMins = (endTime.getTime() - startTime.getTime()) / (60 * 1000);
    	
		
        document = builder.parse(new ByteArrayInputStream(communicator.webServiceGetRequest(fixtureRecordsEndPoint, 1200000).toString().getBytes("UTF-8")));

        root = document.getDocumentElement();
        nodelist = root.getChildNodes();
        int size = nodelist.getLength();
        
        for (int h = 0; h < size; h++) {
        	NodeList nodeInfo = nodelist.item(h).getChildNodes();
        	String[] obj = new String[8];
        	obj[0] = nodeInfo.item(0).getFirstChild().getNodeValue();
        	obj[1] = nodeInfo.item(1).getFirstChild().getNodeValue();
        	obj[2] = nodeInfo.item(2).getFirstChild().getNodeValue();
        	obj[3] = nodeInfo.item(3).getFirstChild().getNodeValue();
        	obj[4] = nodeInfo.item(4).getFirstChild().getNodeValue();
        	obj[5] = nodeInfo.item(5).getFirstChild().getNodeValue();
        	obj[6] = nodeInfo.item(6).getFirstChild().getNodeValue();
        	obj[7] = nodeInfo.item(7).getFirstChild().getNodeValue();
        	normalLoad += Double.parseDouble(obj[6]);
        	currentLoad += Double.parseDouble(obj[7]);
        	tempSum += Double.parseDouble(obj[6]) * Integer.parseInt(obj[3]); 
        	logger.info("Fixture : " + obj[0] + " [ avg power = " + obj[6] + ", dr sensitivity = " + obj[3] + " ]");
        	drRecords.add(obj);
        }
        logger.info("average energy consumption = " + normalLoad);
        logger.info("current total energy consumption = " + currentLoad);
        loadReductionReqd = currentLoad - normalLoad;
        
        /* Object Content
         * 		Fixture Id - 0
         * 		Dimmer Control - 1
         * 		Current State - 2
         * 		DR Reactivity - 3
         * 		Min Level - 4
         * 		On Level - 5
         * 		Avg Power consumption - 6
         * 		Current power consumption - 7
         */
        
        document = builder.parse(new ByteArrayInputStream(communicator.webServiceGetRequest(currentPricingEndPoint, 60000).toString().getBytes("UTF-8")));

        root = document.getDocumentElement();
        nodelist = root.getChildNodes();
        size = nodelist.getLength();
        for (int h = 0; h < size; h++) {
    		if("msg".equals(nodelist.item(h).getNodeName())) {
    			normalPricing = Double.parseDouble(nodelist.item(h).getFirstChild().getNodeValue());
    		}
        }
        
        if ("load".equals(signalType)) {
			loadReductionReqd += payloadValue*1000;
		}
		else if ("priceAbsolute".equals(signalType)) {
			loadReductionReqd += normalLoad
					- (normalPricing / payloadValue)
					* normalLoad;
		}
		else if ("priceRelative".equals(signalType)) {
			loadReductionReqd += normalLoad
					- (normalLoad/payloadValue);
		}
		else if ("level".equals(signalType)) {
			loadReductionReqd += payloadValue*normalLoad;
		}
		
		logger.info("load Reduction required = " + loadReductionReqd);

		String dimFixturesEndPoint = "https://localhost/ems/services/org/fixture/op/dim/rel/";
		
		if (tempSum != 0.0 && loadReductionReqd > 0) {
			kscale = loadReductionReqd / tempSum;
		}
		logger.info("Scale = " + kscale + ", Power*DR Sum = " + tempSum );
		Map<Integer, List<Integer> > dimFixtures = new HashMap<Integer, List<Integer> >(); 
		for (int i = 0; i < drRecords.size(); i++) {
			
			Integer minLevel = Integer.parseInt(drRecords.get(i)[4]);
			Integer drReactivity = Integer.parseInt(drRecords.get(i)[3]);
			Integer lightLevel = Integer.parseInt(drRecords.get(i)[1]);
			if (drReactivity > 0 && lightLevel > minLevel) {
				percentReduction = (int)(kscale * drReactivity * lightLevel);
				logger.info(drRecords.get(i)[0] + " " + minLevel + " " + drReactivity + " " + lightLevel);
				if(percentReduction > 100) {
					percentReduction = 100;
				}
				if(lightLevel - percentReduction < minLevel) {
					percentReduction = (int)(lightLevel - minLevel); 
				}
				if(!dimFixtures.containsKey(percentReduction)) {
					dimFixtures.put(percentReduction, new ArrayList<Integer>());
				}
				dimFixtures.get(percentReduction).add(Integer.parseInt(drRecords.get(i)[0]));

			}
		}
		for(Integer a: dimFixtures.keySet()) {
			logger.info(dimFixturesEndPoint + -1*a + "/" + (int)durationInMins);
			StringBuffer postData = new StringBuffer("<fixtures>");
			for(Integer b: dimFixtures.get(a)) {
				postData.append("<fixture><id>" + b + "</id></fixture>");
			}
			postData.append("</fixtures>");
			logger.info(postData.toString());
			String result = communicator.webServicePostRequest(postData.toString(), dimFixturesEndPoint + -1*a + "/" + (int)durationInMins, 600000);
			logger.info(result);
		}
	}

    public static void pollServer() {
    	//logger.info("Polling Start.");
    	File drUserFile = new File("/var/lib/tomcat6/Enlighted/openADRSeverConfig.xml");
        if(drUserFile.exists()) {
        	try {
        		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            	DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(drUserFile.getAbsoluteFile());
				NodeList users = doc.getElementsByTagName("users");
				String serviceUrl = null;
				String username = null;
				String password = null;
				if(users != null && users.getLength() > 0) {
					NodeList each = users.item(0).getChildNodes();
					serviceUrl = each.item(0).getFirstChild().getNodeValue();
					username = each.item(1).getFirstChild().getNodeValue();
					password = each.item(2).getFirstChild().getNodeValue();
				}
				if(serviceUrl != null  && serviceUrl.startsWith("https")) {
					processEvents2_0(serviceUrl, username, password);
				}
				else {
		        	ADRRestWSClient r2 = new ADRRestWSClient();
		    		if (r2.MakeConnection(serviceUrl, username, password)) {
		    			processEvent(r2);
		    		}
				}
			} catch (SAXException e) {
				logger.log(Level.SEVERE, e.toString(), e);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.toString(), e);
			} catch (ParserConfigurationException e) {
				logger.log(Level.SEVERE, e.toString(), e);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.toString(), e);
			}
        	
        }
        else {
        	logger.info("Open ADR service url access information does not exist");
        }
    }
    

}
