package com.enlightedinc.adr.main;

import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sun.misc.BASE64Encoder;

import com.enlightedinc.adr.model.ADRTarget;

/**
 * openADR communication helper
 * 
 * @author kushal
 * 
 */
public class ADRRestWSClient {

    
    ADRTarget adrTargetTemp = null;
    Map<String, Integer> eventMap = new HashMap<String, Integer>();
	public ArrayList<ADRTarget> adrTargetArray = new ArrayList<ADRTarget>();
	int count = 0;
	
    boolean bidevent = false;
    boolean abspriceevent = false;
    boolean relpriceevent = false;


    public ADRRestWSClient() {
    }

	public boolean MakeConnection(String endPoint, String username, String password) {
        try {
            //String confirmEndPoint = "http://cdp.openadr.com/RestClientWS/nossl/restConfirm";

            URLConnection connection = new URL(endPoint).openConnection();

            // String plain = "enlighted.1:Test_1234";
            String plain = username + ":" + password;

            String enocoded = new BASE64Encoder().encode(plain.getBytes());
            connection.setRequestProperty("Authorization", "Basic " + enocoded);

            // create the dom
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            Document document = builder.parse(connection.getInputStream());
            //Document document = builder.parse(new File("/home/enlighted/Desktop/sce2adr.xml"));
            
            // print the xml for debug purposes
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(document);
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            transformer.transform(source, result);
            
            EnlightedADREntryPoint.xmlResponse = sw.toString();
            
            // parse xml and populate global required attributes.
            Element root = document.getDocumentElement();
            parseList(root.getChildNodes(), root.getNodeName());

        } catch (NullPointerException npe) {
        	EnlightedADREntryPoint.logger.info(EnlightedADREntryPoint.xmlResponse);
            EnlightedADREntryPoint.logger.log(Level.SEVERE, npe.toString(), npe);
            return false;
        } catch (Exception e) {
        	EnlightedADREntryPoint.logger.info(EnlightedADREntryPoint.xmlResponse);
        	EnlightedADREntryPoint.logger.log(Level.SEVERE, e.toString(), e);
            return false;
        }
        return true;
    }

    /**
     * Iterate recursivly over each node's nodelist...
     * 
     * @param nList
     * @throws ParseException 
     */
	private void parseList(NodeList nList, String rootNodeName) throws ParseException {

        int listsz = nList.getLength();
        for (int h = 0; h < listsz; h++) // obtain the top level information
        {
            Node listChild = nList.item(h);
            String nodeName = listChild.getNodeName();
            if (nodeName.equalsIgnoreCase("p:eventStates")) {
                NamedNodeMap nnMap = listChild.getAttributes();
                String eventIdentifier = nnMap.getNamedItem("eventIdentifier").getNodeValue();
                if(eventIdentifier != null && !"".equals(eventIdentifier)) {
                	adrTargetTemp = new ADRTarget();
                	adrTargetTemp.setDrIdentifier(eventIdentifier);
                	adrTargetArray.add(adrTargetTemp);
                	eventMap.put(eventIdentifier, count++);
                }
                else {
                	continue;
                }
            }
            if (rootNodeName.equalsIgnoreCase("p:simpleDRModeData")) {
                if (nodeName.equalsIgnoreCase("p:EventStatus")) {
                	adrTargetTemp.setDrStatus(listChild.getFirstChild().getNodeValue());
                } else if (nodeName.equalsIgnoreCase("p:currentTime")) {
                	adrTargetTemp.currentTime = (double) Float.valueOf(listChild.getFirstChild().getNodeValue().trim()).floatValue();
                } else if (nodeName.equalsIgnoreCase("p:OperationModeValue")) {
                	adrTargetTemp.setOperationMode(listChild.getFirstChild().getNodeValue());		
                }
            }

            if (nodeName.equalsIgnoreCase("p:startTime")) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String eventStartTime = listChild.getFirstChild().getNodeValue();
                eventStartTime = eventStartTime.replace('T', ' ');
                eventStartTime = eventStartTime.substring(0, eventStartTime.indexOf('.'));
                adrTargetTemp.setStartTime((Date) sdf.parse(eventStartTime));
            } else if (nodeName.equalsIgnoreCase("p:endTime")) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String eventEndTime = listChild.getFirstChild().getNodeValue();
                eventEndTime = eventEndTime.replace('T', ' ');
                eventEndTime = eventEndTime.substring(0, eventEndTime.indexOf('.'));
                adrTargetTemp.setEndTime((Date) sdf.parse(eventEndTime));
            }
            
            //TODO Check the use case
            if (rootNodeName.equalsIgnoreCase("p:modeSlot")) {
                // look for operation modes
                if (nodeName.equalsIgnoreCase("p:OperationModeValue")) {
                    adrTargetTemp.setOperationMode(listChild.getFirstChild().getNodeValue());
                }
                /*else if (nodeName.equalsIgnoreCase("p:modeTimeSlot")) {
                    adrTargetTemp.modeTimeSlot = listChild.getFirstChild().getNodeValue();
                }*/
            }

            if (nodeName.equalsIgnoreCase("p:eventInfoInstances")) {
                bidevent = false;
                abspriceevent = false;
                relpriceevent = false;
            }

            // absolute pricing event

            if (nodeName.equalsIgnoreCase("p:eventInfoTypeID")
                    && listChild.getFirstChild().getNodeValue().equalsIgnoreCase("PRICE_ABSOLUTE")) {
                abspriceevent = true;
                parseList(nList.item(2).getChildNodes(), "p:eventInfoValues");
                abspriceevent = false;
                continue;
            }

            if (abspriceevent && rootNodeName.equalsIgnoreCase("p:eventInfoValues")) {
                if (nodeName.equalsIgnoreCase("p:value")) {
                    adrTargetTemp.setPriceAbsolute(Double.parseDouble(listChild.getFirstChild().getNodeValue()));
                } 
              //TODO why?
/*                else if (nodeName.equalsIgnoreCase("p:timeOffset")) {
                    if (!listChild.getFirstChild().getNodeValue().equalsIgnoreCase(adrTargetTemp.modeTimeSlot)) {
                    	
                    	adrTargetTemp.setPriceAbsolute(null);
                    }
                }*/
            }

            // relative pricing event
            if (nodeName.equalsIgnoreCase("p:eventInfoTypeID")
                    && listChild.getFirstChild().getNodeValue().equalsIgnoreCase("PRICE_MULTIPLE")) {
                relpriceevent = true;
                parseList(nList.item(2).getChildNodes(), "p:eventInfoValues");
                relpriceevent = false;
                continue;
            }

            if (relpriceevent && rootNodeName.equalsIgnoreCase("p:eventInfoValues")) {
                if (nodeName.equalsIgnoreCase("p:value")) {
                    adrTargetTemp.setPriceRelative(Double.parseDouble(listChild.getFirstChild().getNodeValue()));

                }
              //TODO why?
/*                else if (nodeName.equalsIgnoreCase("p:timeOffset")) {
                    if (!listChild.getFirstChild().getNodeValue().equalsIgnoreCase(adrTargetTemp.modeTimeSlot)) {
                    	adrTargetTemp.setPriceRelative(null);
                    }
                }*/
            }

            // bid event type
            if (nodeName.equalsIgnoreCase("p:eventInfoTypeID")
                    && listChild.getFirstChild().getNodeValue().equalsIgnoreCase("LOAD_AMOUNT")) {
                bidevent = true;
                parseList(nList.item(2).getChildNodes(), "p:eventInfoValues");
                bidevent = false;
                continue;
            }

            if (bidevent && rootNodeName.equalsIgnoreCase("p:eventInfoValues")) {

                if (nodeName.equalsIgnoreCase("p:value")) {
                    adrTargetTemp.setLoadAmount(Double.parseDouble(listChild.getFirstChild().getNodeValue()));

                } 
              //TODO why?
/*                else if (nodeName.equalsIgnoreCase("p:timeOffset")) {

                    if (!listChild.getFirstChild().getNodeValue().equalsIgnoreCase(adrTargetTemp.modeTimeSlot)) {
                    	adrTargetTemp.setLoadAmount(null);
                    }
                }*/
            }

            parseList(listChild.getChildNodes(), nodeName);

        }

    }
}
