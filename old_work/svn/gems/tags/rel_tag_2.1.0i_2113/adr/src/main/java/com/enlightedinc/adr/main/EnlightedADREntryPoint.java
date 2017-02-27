package com.enlightedinc.adr.main;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Formatter;
import java.util.logging.Handler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.enlightedinc.adr.model.ADRTarget;
import com.enlightedinc.adr.service.ADRTargetManager;

public class EnlightedADREntryPoint {
	
    private static Timer adrTimer = new Timer("ADR Timer", true);
    private static int adrInterval = 60*1000; // 1 minute
    private static ADRTargetManager adrTargetManager  = null;
    
    static final Logger logger = Logger.getLogger(EnlightedADREntryPoint.class.getName());
    static {
        try {
            logger.setLevel(Level.INFO);

            Formatter formatter = new Formatter() {

                @Override
                public String format(LogRecord arg0) {
                    StringBuilder b = new StringBuilder();
                    b.append(new Date());
                    b.append("::");
                    b.append(arg0.getSourceClassName());
                    b.append("::");
                    b.append(arg0.getSourceMethodName());
                    b.append("::");
                    b.append(arg0.getLevel());
                    b.append("::");
                    b.append(arg0.getMessage());
                    b.append(System.getProperty("line.separator"));
                    return b.toString();
                }

            };

            Handler fh = new FileHandler("/home/enlighted/ADR.log");
            fh.setFormatter(formatter);
            logger.addHandler(fh);

            LogManager lm = LogManager.getLogManager();
            lm.addLogger(logger);
        }
        catch (Throwable e) {
            e.printStackTrace();
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
		PollADR adrPoll = new PollADR();
        adrTimer.scheduleAtFixedRate(adrPoll, 0, adrInterval);
        while(true) {}
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
		Communicator communicator = new Communicator();
		if(queuedEvents != null && queuedEvents.size() > 0) {
			for(ADRTarget adrTarget: queuedEvents) {
				Integer index = r2.eventMap.get(adrTarget.getDrIdentifier());
				if(index != null) {
					ADRTarget adrTargetTemp = r2.adrTargetArray.get(index);
					r2.eventMap.remove(adrTarget.getDrIdentifier());
					String oldStatus = adrTarget.getDrStatus();
					if(("FAR".equals(oldStatus) || "NEAR".equals(oldStatus)) && "ACTIVE".equals(adrTargetTemp.getDrStatus())) {
						
						adrTarget.setDrStatus(adrTargetTemp.getDrStatus());
						adrTargetManager.saveOrUpdateADRTarget(adrTarget);
						//TODO check for change in other information and notify GEMS?
						//TODO if status is changed to ACTIVE or NEAR, notify GEMS 
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
						String fixtureRecordsEndPoint = "https://localhost/ems/services/org/dr/fixture/record/" + 
													sdf.format(adrTargetTemp.getStartTime()) + "/" 
													+ sdf.format(adrTargetTemp.getEndTime());
						String currentPricingEndPoint = "https://localhost/ems/services/org/dr/pricing/current";
						
						logger.info(fixtureRecordsEndPoint);
			            
						try {
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
				        	double loadReductionReqd = 0;
				        	double kscale = 0;
				        	long durationInMins = (adrTargetTemp.getEndTime().getTime() - adrTargetTemp.getStartTime().getTime()) / (60 * 1000);
				        	
							
				            document = builder.parse(new ByteArrayInputStream(communicator.webServiceGetRequest(fixtureRecordsEndPoint, 120000).toString().getBytes("UTF-8")));

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

							if (adrTargetTemp.currentTime >= 0) {
								if (adrTargetTemp.getLoadAmount() != 0) {
									loadReductionReqd += adrTargetTemp.getLoadAmount() * 1000;
								}
								else if (adrTargetTemp.getPriceAbsolute() != 0) {
									loadReductionReqd += normalLoad
											- (normalPricing / adrTargetTemp.getPriceAbsolute())
											* normalLoad;
								}
								else if (adrTargetTemp.getPriceRelative() != 0) {
									loadReductionReqd += normalLoad
											- (normalLoad/adrTargetTemp.getPriceRelative());
								}
								else if (adrTargetTemp.getOperationMode().equals("HIGH")) {
									loadReductionReqd += 0.5*normalLoad;
								}
								else if (adrTargetTemp.getOperationMode().equals("MODERATE")) {
									loadReductionReqd += 0.25*normalLoad;
								}
								else if (adrTargetTemp.getOperationMode().equals("NORMAL")) {
									loadReductionReqd += 0.1*normalLoad;
								}
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
								String result = communicator.webServicePostRequest(postData.toString(), dimFixturesEndPoint + -1*a + "/" + (int)durationInMins, 60000);
								logger.info(result);
							}
						} catch (Exception e) {
							e.printStackTrace();
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
		for(String eventKey: eventKeys) {
			adrTargetManager.saveOrUpdateADRTarget(r2.adrTargetArray.get(r2.eventMap.get(eventKey)));
		}
	}
	
    public static class PollADR extends TimerTask {

        public void run() {
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
    	        	ADRRestWSClient r2 = new ADRRestWSClient();
    	    		if (r2.MakeConnection(serviceUrl, username, password)) {
    	    			//logger.info("Processing Event.");
    	    			processEvent(r2);
    	    		}
    			
    			} catch (SAXException e) {
    				e.printStackTrace();
    			} catch (IOException e) {
    				e.printStackTrace();
    			} catch (ParserConfigurationException e) {
    				e.printStackTrace();
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
            	
            }
            else {
            	logger.info("Open ADR service url access information does not exist");
            }
            //logger.info("Polling End.");
        }
    }

}
