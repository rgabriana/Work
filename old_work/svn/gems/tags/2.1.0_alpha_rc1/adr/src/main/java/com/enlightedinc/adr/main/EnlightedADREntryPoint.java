package com.enlightedinc.adr.main;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
				            double[] weightArray = null;
				        	double[] baseLineArray = null;
				        	double[] percentReductionArray = null;
				        	double normalPricing = 1;
				        	double normalLoad = 10;
				        	double loadReductionReqd = 0.25;
				        	double kscale = 1.0;
							
				            document = builder.parse(new ByteArrayInputStream(communicator.webServiceGetRequest(groupECEndPoint).toString().getBytes("UTF-8")));

				            root = document.getDocumentElement();
				            nodelist = root.getChildNodes();
				            int size = nodelist.getLength();
				            
				            weightArray = new double[size];
				            baseLineArray = new double[size];
				            
				            for (int h = 0; h < size; h++) {
				            	NodeList nodeInfo = nodelist.item(h).getChildNodes();
				            	int id = h;
				            	for(int k = 0; k < nodeInfo.getLength(); k++) {
				            		if("id".equals(nodeInfo.item(k).getNodeName())) {
				            			id = Integer.parseInt(nodeInfo.item(k).getFirstChild().getNodeValue());
				            		}
				            		if("basepowerused".equals(nodeInfo.item(k).getNodeName())) {
				            			baseLineArray[id] = Double.parseDouble(nodeInfo.item(k).getFirstChild().getNodeValue());
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
				            
				            /*for (int i = 0; i < baseLineArray.length; i++) {
				            	System.out.println("Profile: " + i + " [ base power = " + baseLineArray[i] + ", dr sensitivity = " +  weightArray[i] + " ]");
				            }*/
				            
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
									loadReductionReqd = adrTargetTemp.getLoadAmount();
								}
								else if (adrTargetTemp.getPriceAbsolute() != 0) {
									loadReductionReqd = normalLoad
											- (normalPricing / adrTargetTemp.getPriceAbsolute())
											* normalLoad;
								}
								else if (adrTargetTemp.getPriceRelative() != 0) {
									loadReductionReqd = normalLoad
											- ((normalPricing + adrTargetTemp.getPriceRelative()) / adrTargetTemp.getPriceAbsolute())
											* normalLoad;
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
									percentReductionArray[i] = (1 - (kscale * weightArray[i])) * 100;
									long durationInMins = (adrTargetTemp.getEndTime().getTime() - adrTargetTemp.getStartTime().getTime()) / (60 * 1000);
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
        	String username = "enlighted.pune";
        	String password = "enLighted@pune7";
        	ADRRestWSClient r2 = new ADRRestWSClient();
    		if (r2.MakeConnection(username, password)) {
    			processEvent(r2);
    		}
    		//System.out.println("=============================== End = " + new Date() + "=================================");	
        }
    }

}
