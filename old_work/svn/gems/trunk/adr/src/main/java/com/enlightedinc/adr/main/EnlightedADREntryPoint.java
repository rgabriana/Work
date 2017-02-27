package com.enlightedinc.adr.main;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
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
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.adrcom.main.ADRCommunication;
import com.adrcom.model.DrEvent;
import com.adrcom.model.DrEventSignal;
import com.adrcom.model.DrEventSignalInterval;
import com.adrcom.service.DrEventManager;
import com.adrcom.util.DurationToMilliSec;
import com.communication.template.SecureCloudConnectionTemplate;
import com.enlightedinc.adr.model.ADRTarget;
import com.enlightedinc.adr.service.ADRTargetManager;

public class EnlightedADREntryPoint {
	
    private static long adrInterval = 60*1000;
    private static ADRTargetManager adrTargetManager  = null;
    
    static final Logger logger = Logger.getLogger(EnlightedADREntryPoint.class.getName());
    static String xmlResponse = "";
    static String sVTNHost;
	private static String username;
	private static String password;
	private static String oadrVersion;
	private static long fileModified;
	private static String locationPrefix = System.getenv("ENL_APP_HOME")+"/Enlighted/";
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
	
	private static Map<Integer, String> levelmap = new HashMap<Integer, String>();
    static {
        try {
            logger.setLevel(Level.INFO); 
            LogManager lm = LogManager.getLogManager();
            lm.addLogger(logger);
        }
        catch (Throwable e) {
        	logger.log(Level.SEVERE, e.toString(), e);
        }
        
        levelmap.put(0, "LOW");
        levelmap.put(1, "MODERATE");
        levelmap.put(2, "HIGH");
        levelmap.put(3, "SPECIAL");
        
    }

    private static void readConfigParameters()
    {
    	try {
			File drUserFile = new File( System.getenv("ENL_APP_HOME")+"/Enlighted/openADRSeverConfig.xml");
			fileModified = drUserFile.lastModified();
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(drUserFile.getAbsoluteFile());
            NodeList users = doc.getElementsByTagName("users");
            if(users != null && users.getLength() > 0) {
                Node each = users.item(0);
                
                sVTNHost = each.getChildNodes().item(0).getChildNodes().getLength() > 0 ? each.getChildNodes().item(0).getChildNodes().item(0).getNodeValue() : null;
                ADRCommunication.sVTNHost = sVTNHost;
                username = each.getChildNodes().item(1).getChildNodes().getLength() > 0 ?  each.getChildNodes().item(1).getChildNodes().item(0).getNodeValue() : null;
                ADRCommunication.username = username;
                password = each.getChildNodes().item(2).getChildNodes().getLength() > 0 ? each.getChildNodes().item(2).getChildNodes().item(0).getNodeValue() : null;
                ADRCommunication.password = password;
                
        		if (each.getChildNodes().getLength() > 3) {
        			if(each.getChildNodes().item(3).hasChildNodes()) {
        				String timeInterval = each.getChildNodes().item(3).getChildNodes().item(0).getNodeValue();
                    	adrInterval = (timeInterval == null || "".equals(timeInterval)) ? 60*1000 : Long.parseLong(timeInterval) * 1000;	
        			}
        			if(each.getChildNodes().item(4).hasChildNodes()) {
        				ADRCommunication.VEN_ID = each.getChildNodes().item(4).getChildNodes().item(0).getNodeValue();
        			}
        			
        			ADRCommunication.validMarketContexts = new HashSet<String>();
                	if(each.getChildNodes().item(5).hasChildNodes()) {
                		ADRCommunication.validMarketContexts.add(each.getChildNodes().item(5).getChildNodes().item(0).getNodeValue());
                	}
                	if(each.getChildNodes().item(6).hasChildNodes()) {
                		ADRCommunication.validMarketContexts.add(each.getChildNodes().item(6).getChildNodes().item(0).getNodeValue());
                	}
                	if (each.getChildNodes().item(7).hasChildNodes()) {
                		ADRCommunication.validMarketContexts.add(each.getChildNodes().item(7).getChildNodes().item(0).getNodeValue());
                	}
                	
                	ADRCommunication.vtnIds = new HashSet<String>();
                	if(each.getChildNodes().item(8).hasChildNodes()) {
                		ADRCommunication.vtnIds.add(each.getChildNodes().item(8).getChildNodes().item(0).getNodeValue());
                	}
                	if(each.getChildNodes().item(9).hasChildNodes()) {
                		ADRCommunication.vtnIds.add(each.getChildNodes().item(9).getChildNodes().item(0).getNodeValue());
                	}
                	if(each.getChildNodes().item(10).hasChildNodes()) {
                		ADRCommunication.vtnIds.add(each.getChildNodes().item(10).getChildNodes().item(0).getNodeValue());
                	}
                	if(each.getChildNodes().item(11).hasChildNodes()) {
        				oadrVersion = each.getChildNodes().item(11).getChildNodes().item(0).getNodeValue();
        			}
                	if(each.getChildNodes().item(12).hasChildNodes()) {
                		ADRCommunication.keystoreFileName = each.getChildNodes().item(12).getChildNodes().item(0).getNodeValue();
            		}
            		if(each.getChildNodes().item(13).hasChildNodes()) {
            			ADRCommunication.keystorePassword = each.getChildNodes().item(13).getChildNodes().item(0).getNodeValue();
            		}
            		if(each.getChildNodes().item(14).hasChildNodes()) {
            			ADRCommunication.truststoreFileName = each.getChildNodes().item(14).getChildNodes().item(0).getNodeValue();
            		}
            		if(each.getChildNodes().item(15).hasChildNodes()) {
            			ADRCommunication.truststorePassword = each.getChildNodes().item(15).getChildNodes().item(0).getNodeValue();
            		}
            		if(each.getChildNodes().getLength() > 16 && each.getChildNodes().item(16).hasChildNodes() && each.getChildNodes().item(17).hasChildNodes()) {
            			ADRCommunication.servicePath = "/" + each.getChildNodes().item(16).getChildNodes().item(0).getNodeValue() + each.getChildNodes().item(17).getChildNodes().item(0).getNodeValue();
            		}
            		if(each.getChildNodes().getLength() > 18 && each.getChildNodes().item(18).hasChildNodes()) {
            			ADRCommunication.connectionTimeout = Integer.parseInt(each.getChildNodes().item(18).getChildNodes().item(0).getNodeValue());
            		}
            		if(each.getChildNodes().getLength() > 19 && each.getChildNodes().item(19).hasChildNodes()) {
            			ADRCommunication.socketTimeout = Integer.parseInt(each.getChildNodes().item(19).getChildNodes().item(0).getNodeValue());
            		}
            		
            		
        	    	if (ADRCommunication.keystorePassword != null && ADRCommunication.truststorePassword != null) {
        	    		
        	    		String keystoreFilePath = locationPrefix+"adr/certs/"+ADRCommunication.keystoreFileName;
            	    	String truststoreFilePath = locationPrefix+"adr/certs/"+ADRCommunication.truststoreFileName;
            	    	ADRCommunication.cloudConnectionTemplate = (SecureCloudConnectionTemplate)SpringContext.getBean("secureCloudConnectionTemplate");
            	    	ADRCommunication.cloudConnectionTemplate.setUpCertificateDetails(
	        					SecureCloudConnectionTemplate.JKS_STORE_TYPE,
	        					truststoreFilePath, ADRCommunication.truststorePassword,
	        					SecureCloudConnectionTemplate.JKS_STORE_TYPE,
	        					keystoreFilePath, ADRCommunication.keystorePassword);
        	    	}
                }
            }
       } catch (SAXException e) {
    	   logger.log(Level.SEVERE, e.toString(), e);
       } catch (IOException e) {
    	   logger.log(Level.SEVERE, e.toString(), e);
       } catch (ParserConfigurationException e) {
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
		readConfigParameters();
		adrTargetManager = (ADRTargetManager) SpringContext.getBean("adrTargetManager");
		ADRCommunication.drEventManager = (DrEventManager) SpringContext.getBean("drEventManager");
		
        while(true) {
        	try {
        		File drUserFile = new File( System.getenv("ENL_APP_HOME")+"/Enlighted/openADRSeverConfig.xml");        		
        		if(drUserFile.lastModified()!=fileModified)
        		{
        			// Read the file again if file modified
        			logger.info("----------------------------------------------------------Reading the file again as Config file has got modified------");
        			readConfigParameters();
        			logger.info("----------------------------------------------------------Read Complete------------------------------");
        		}
        		xmlResponse = "";
        		if ("2.0".equals(oadrVersion)) {
        			String requestId = ADRCommunication.processEvents2_0_part1();
        			
        			if(requestId != null) {
        				StringBuffer postData = new StringBuffer("<dRTargets>");
            	    	List<DrEvent> allDrEvents = ADRCommunication.drEventManager.getAllQueuedDREvents();
            			for (DrEvent eachEvent : allDrEvents) {
            				List<DrEventSignal> signals = ADRCommunication.drEventManager.loadDrEventSignalsByEventId(eachEvent.getId());
            				if(signals != null && signals.size() > 0) {
            					Date startDate = eachEvent.getStartDateTime();
            					Long totalduration = 0L;
            					Long duration = 0L;
            		    		for(DrEventSignal eachSignal: signals) {
            		    			List<DrEventSignalInterval> intervals = ADRCommunication.drEventManager.loadDrEventSignalIntervalsByEventSignalId(eachSignal.getId());
            		    			if(intervals != null && intervals.size() > 0 ) {
            		    				for(DrEventSignalInterval eachInterval : intervals) {
            	    						Date intervalStartDate = new Date(startDate.getTime() + totalduration);
            		    					duration = DurationToMilliSec.getMilliSecFromDuration(eachInterval.getIntervalDuration());
            		    					totalduration = totalduration + duration;
            	    						postData.append("<drTarget><id></id>" +
            			    						"<pricelevel>" + levelmap.get(eachInterval.getPayloadValue().intValue()) +"</pricelevel>" +
            			    						"<pricing></pricing>" +
            			    						"<priority>" + eachEvent.getPriority() + "</priority>" +
            			    						"<duration>" + duration/(1000) + "</duration>" +
            			    						"<starttime>" + sdf.format(intervalStartDate) + "T" + sdf1.format(intervalStartDate) + "</starttime>" +
            			    						"<startafter>" + DurationToMilliSec.getMilliSecFromDuration(eachEvent.getStartAfter())/1000 + "</startafter>" +
            			    						"<dridentifier>" + eachEvent.getEventId() + "</dridentifier>" +
            			    						"<targetReduction></targetReduction>" +
            			    						"<enabled></enabled>" +
            			    						"<drtype></drtype>" +
            			    						"<drstatus>" + eachEvent.getEventStatus() + "</drstatus>" +
            			    						"<optIn></optIn>" +
            			    						"<uid>" + eachInterval.getUid() + "</uid>" + 
            		    						"</drTarget>");
            		    				}
            		    			}
            		    		}
            		    	}
            			}
                		
                		postData.append("</dRTargets>");
                		String updateADREvents = "http://localhost:9090/ems/services/org/dr/updateADRTargets/";
                		Communicator communicator = new Communicator();
                		logger.info(postData.toString());
                		String result = null;
                		
                		for (int i = 0; i < 3; i++ ) {
	            			result = communicator.webServicePostRequest(postData.toString(), updateADREvents , 60000);
	            			if(result.contains("dRTargets")) {
	            				break;
	            			}
	            			else {
	            				Thread.sleep(2000);
	            			}
                		}
                		logger.info(result);

                		if(result.contains("dRTargets")) {
	            			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	                        InputSource is = new InputSource();
	                        is.setCharacterStream(new StringReader(result));
	                        Document document = builder.parse(is);
	                        Transformer transformer = TransformerFactory.newInstance().newTransformer();
	                        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	                        DOMSource source = new DOMSource(document);
	                        StringWriter sw = new StringWriter();
	                        StreamResult streamResult = new StreamResult(sw);
	                        transformer.transform(source, streamResult);
	                        Element root = document.getDocumentElement();
	                        logger.info(root.getNodeName());
	                        
	                        HashMap<String,DrEvent> idEventMap = new HashMap<String, DrEvent>();
	                        
	                        for (int h = 0; h < root.getChildNodes().getLength(); h++) {
	                            for (int i = 0; i < root.getChildNodes().item(h).getChildNodes().getLength(); i++) {
	                            	if("dridentifier".equals(root.getChildNodes().item(h).getChildNodes().item(i).getNodeName())) {
	                            		idEventMap.put(root.getChildNodes().item(h).getChildNodes().item(i).getFirstChild().getNodeValue(),null);
	                            	}
	                            }
	                        }
	                        ADRCommunication.processEvents2_0_part2(idEventMap, requestId);
                		}
        			}
        		}
        		else {
        			pollServer();
        		}
			}
        	catch (Exception e) {
        		logger.log(Level.SEVERE, e.toString(), e);
        	}
        	finally {
        		try {
        			Thread.sleep(adrInterval);
        		}
        		 catch (InterruptedException e) {
     				logger.log(Level.SEVERE, e.toString(), e);
     			}
        	}
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
    	File drUserFile = new File( System.getenv("ENL_APP_HOME")+"/Enlighted/openADRSeverConfig.xml");
        if(drUserFile.exists()) {
        	try {
	        	ADRRestWSClient r2 = new ADRRestWSClient();
	    		if (r2.MakeConnection(sVTNHost, username, password)) {
	    			processEvent(r2);
	    		}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.toString(), e);
			}
        	
        }
        else {
        	logger.info("Open ADR service url access information does not exist");
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
