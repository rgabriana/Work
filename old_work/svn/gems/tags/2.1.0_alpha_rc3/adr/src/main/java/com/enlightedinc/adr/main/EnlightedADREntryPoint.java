package com.enlightedinc.adr.main;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.enlightedinc.adr.model.ADRTarget;
import com.enlightedinc.adr.service.ADRTargetManager;

public class EnlightedADREntryPoint {
	
    private static Timer adrTimer = new Timer("ADR Timer", true);
    private static int adrInterval = 60*1000; // 1 minute
    private static ADRTargetManager adrTargetManager  = null;

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
						//TODO check for change in other information and notify GEMS?
						//TODO if status is changed to ACTIVE or NEAR, notify GEMS 
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
						String groupECEndPoint = "https://localhost/ems/services/org/dr/group/ec/" + 
													sdf.format(adrTargetTemp.getStartTime()) + "/" 
													+ sdf.format(adrTargetTemp.getEndTime());
						String groupSensitivityEndPoint = "https://localhost/ems/services/org/dr/group/sensitivity";
						String currentPricingEndPoint = "https://localhost/ems/services/org/dr/pricing/current";
			            
						try {
							DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
							Document document = null;

				            Element root = null;
				            NodeList nodelist = null;
				            double[] weightArray = new double[17];
				        	double[] baseLineArray = new double[17];
				        	double[] percentReductionArray = null;
				        	double normalPricing = 1;
				        	double normalLoad = 0;
				        	double loadReductionReqd = 0.25;
				        	double kscale = 0;
				        	long durationInMins = (adrTargetTemp.getEndTime().getTime() - adrTargetTemp.getStartTime().getTime()) / (60 * 1000);
				        	
							
				            document = builder.parse(new ByteArrayInputStream(communicator.webServiceGetRequest(groupECEndPoint).toString().getBytes("UTF-8")));

				            root = document.getDocumentElement();
				            nodelist = root.getChildNodes();
				            int size = nodelist.getLength();
				            
				            for (int h = 0; h < size; h++) {
				            	NodeList nodeInfo = nodelist.item(h).getChildNodes();
				            	int id = h;
				            	for(int k = 0; k < nodeInfo.getLength(); k++) {
				            		if("id".equals(nodeInfo.item(k).getNodeName())) {
				            			id = Integer.parseInt(nodeInfo.item(k).getFirstChild().getNodeValue());
				            		}
				            		if("basepowerused".equals(nodeInfo.item(k).getNodeName())) {
				            			baseLineArray[id] = Double.parseDouble(nodeInfo.item(k).getFirstChild().getNodeValue()) * durationInMins / 5;
				            		}
				            		if(("totalfixtures").equals(nodeInfo.item(k).getNodeName())) {
				            			baseLineArray[id] = baseLineArray[id] * Integer.parseInt(nodeInfo.item(k).getFirstChild().getNodeValue());
				            		}
				            	}
				            }
				            
				            document = builder.parse(new ByteArrayInputStream(communicator.webServiceGetRequest(groupSensitivityEndPoint).toString().getBytes("UTF-8")));
				            
				            root = document.getDocumentElement();
				            nodelist = root.getChildNodes();
				            size = nodelist.getLength();
				            
				            for (int h = 0; h < size; h++) {
				            	NodeList nodeInfo = nodelist.item(h).getChildNodes();
				            	int id = h;
				            	for(int k = 0; k < nodeInfo.getLength(); k++) {
				            		if("id".equals(nodeInfo.item(k).getNodeName())) {
				            			id = Integer.parseInt(nodeInfo.item(k).getFirstChild().getNodeValue());
				            		}
				            		if("drsensitivity".equals(nodeInfo.item(k).getNodeName())) {
				            			weightArray[id] = Double.parseDouble(nodeInfo.item(k).getFirstChild().getNodeValue());
				            		}
				            	}
				            }
				            
				            for (int i = 1; i < baseLineArray.length; i++) {
				            	normalLoad += baseLineArray[i];
				            	//System.out.println("Profile: " + i + " [ base power = " + baseLineArray[i] + ", dr sensitivity = " +  weightArray[i] + " ]");
				            }
				            
				            document = builder.parse(new ByteArrayInputStream(communicator.webServiceGetRequest(currentPricingEndPoint).toString().getBytes("UTF-8")));

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
									loadReductionReqd = adrTargetTemp.getLoadAmount() * 1000;
								}
								else if (adrTargetTemp.getPriceAbsolute() != 0) {
									loadReductionReqd = normalLoad
											- (normalPricing / adrTargetTemp.getPriceAbsolute())
											* normalLoad;
								}
								else if (adrTargetTemp.getPriceRelative() != 0) {
									loadReductionReqd = normalLoad
											- (normalLoad/adrTargetTemp.getPriceRelative());
								}
								else if (adrTargetTemp.getOperationMode().equals("HIGH")) {
									loadReductionReqd = 0.5*normalLoad;
								}
								else if (adrTargetTemp.getOperationMode().equals("MODERATE")) {
									loadReductionReqd = 0.25*normalLoad;
								}
								else if (adrTargetTemp.getOperationMode().equals("NORMAL")) {
									loadReductionReqd = 0.1*normalLoad;
								}
							}
							
							//System.out.println("load Reduction required = " + loadReductionReqd);
				
							double tempSum = 0.0;
							percentReductionArray = new double[baseLineArray.length];
							String dimFixturesEndPoint = "https://localhost/ems/services/org/dr/op/dim/group/";
							for (int i = 0; i < baseLineArray.length; i++) {
								tempSum += weightArray[i] * baseLineArray[i];
							}
							
							if (tempSum != 0.0) {
								kscale = loadReductionReqd / tempSum;
							}
							//System.out.println("Scale = " + kscale + ", Power*DR Sum = " + tempSum );
							for (int i = 0; i < baseLineArray.length; i++) {
								if (weightArray[i] != 0) {
									percentReductionArray[i] = kscale * weightArray[i] * 100;
									//System.out.println(dimFixturesEndPoint + i + "/" + (int)percentReductionArray[i] + "/" + (int)durationInMins);
									String result = communicator.webServicePostRequest("", dimFixturesEndPoint + i + "/" + (int)percentReductionArray[i] + "/" + (int)durationInMins);
									System.out.println(result);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							return;
						}
						adrTarget.setDrStatus(adrTargetTemp.getDrStatus());
						adrTargetManager.saveOrUpdateADRTarget(adrTarget);
						
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
        	//System.out.println("=============================== Start = " + new Date() + "=================================");
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
    					//System.out.println(serviceUrl + "@" + username + "::" + password);
    				}
    	        	ADRRestWSClient r2 = new ADRRestWSClient();
    	    		if (r2.MakeConnection(serviceUrl, username, password)) {
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
            	System.out.println("Open ADR service url access information does not exist");
            }
    		//System.out.println("=============================== End = " + new Date() + "=================================");	
        }
        
        public void adsf() {
        	
        }
    }

}
