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
 * @author anand
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

	public boolean MakeConnection(String username, String password) {
        try {
            String endPoint = "http://cdp.openadr.com/RestClientWS/nossl/rest2";
            String confirmEndPoint = "http://cdp.openadr.com/RestClientWS/nossl/restConfirm";

            URLConnection connection = new URL(endPoint).openConnection();

            // String plain = "enlighted.1:Test_1234";
            String plain = username + ":" + password;

            String enocoded = new BASE64Encoder().encode(plain.getBytes());
            connection.setRequestProperty("Authorization", "Basic " + enocoded);

            // create the dom
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            Document document = builder.parse(connection.getInputStream());
            
            // print the xml for debug purposes
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(document);
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            transformer.transform(source, result);

            //System.out.println(new Date());
            //System.out.println(sw.toString());
            
            // parse xml and populate global required attributes.
            Element root = document.getDocumentElement();
            parseList(root.getChildNodes(), root.getNodeName());

            //TODO why?
            // send the EventStateConfirmation
            // the data here is dummied - it should really be copied out of the
            // EventState
            connection = new URL(confirmEndPoint).openConnection();
            connection.setRequestProperty("Authorization", "Basic " + enocoded);
            connection.setDoOutput(true);
            StringBuffer sb = new StringBuffer();
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            sb.append("<p:eventStateConfirmation currentTime=\"31.0\" drasClientID=\"DRC-245\"");
            sb.append("eventIdentifier=\"35648\" eventModNumber=\"0\" eventStateID=\"843451309\"");
            sb.append("operationModeValue=\"MODERATE\" optInStatus=\"true\" programName=\"CPP\"");
            sb.append("schemaVersion=\"\" xmlns:p=\"http://www.openadr.org/DRAS/EventState\"");
            sb.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
            sb.append("xsi:schemaLocation=\"http://openadr.lbl.gov/src/1/EventState.xsd\">");
            sb.append("</p:eventStateConfirmation>");
            connection.getOutputStream().write(sb.toString().getBytes());
            //String rv = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
            //System.out.println("Confirmation returned (" + new Date() + ") = "  + rv);

        } catch (NullPointerException npe) {
            npe.printStackTrace();
            return false;
        } catch (Exception e) {
        	e.printStackTrace();
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
                	//TODO Check if it is required
                	adrTargetTemp.setOperationMode(listChild.getFirstChild().getNodeValue());		
                }
            }

            if (nodeName.equalsIgnoreCase("p:startTime")) {
                // sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss zzz");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String eventStartTime = listChild.getFirstChild().getNodeValue();
                eventStartTime = eventStartTime.replace('T', ' ');
                eventStartTime = eventStartTime.substring(0, eventStartTime.indexOf('.'));
                adrTargetTemp.setStartTime((Date) sdf.parse(eventStartTime));
                /*Calendar fromCal = Calendar.getInstance();
                fromCal.setTime(adrTargetTemp.getStartTime());*/
            } else if (nodeName.equalsIgnoreCase("p:endTime")) {
                // sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss zzz");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String eventEndTime = listChild.getFirstChild().getNodeValue();
                eventEndTime = eventEndTime.replace('T', ' ');
                eventEndTime = eventEndTime.substring(0, eventEndTime.indexOf('.'));
                adrTargetTemp.setEndTime((Date) sdf.parse(eventEndTime));
                /*Calendar toCal = Calendar.getInstance();
                toCal.setTime(adrTargetTemp.getEndTime());*/
            }
            
            if (rootNodeName.equalsIgnoreCase("p:modeSlot")) {
                // look for operation modes
                if (nodeName.equalsIgnoreCase("p:OperationModeValue")) {
                    adrTargetTemp.setOperationMode(listChild.getFirstChild().getNodeValue());
                } else if (nodeName.equalsIgnoreCase("p:modeTimeSlot")) {
                	//TODO Check the use case
                    adrTargetTemp.modeTimeSlot = listChild.getFirstChild().getNodeValue();
                }
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
                } else if (nodeName.equalsIgnoreCase("p:timeOffset")) {
                    if (!listChild.getFirstChild().getNodeValue().equalsIgnoreCase(adrTargetTemp.modeTimeSlot)) {
                    	//TODO why?
                    	adrTargetTemp.setPriceAbsolute(null);
                    }
                }
            }

            // relative pricing event
            if (nodeName.equalsIgnoreCase("p:eventInfoTypeID")
                    && listChild.getFirstChild().getNodeValue().equalsIgnoreCase("PRICE_RELATIVE")) {
                relpriceevent = true;
                parseList(nList.item(2).getChildNodes(), "p:eventInfoValues");
                relpriceevent = false;
                continue;
            }

            if (relpriceevent && rootNodeName.equalsIgnoreCase("p:eventInfoValues")) {
                if (nodeName.equalsIgnoreCase("p:value")) {
                    adrTargetTemp.setPriceRelative(Double.parseDouble(listChild.getFirstChild().getNodeValue()));

                } else if (nodeName.equalsIgnoreCase("p:timeOffset")) {

                    if (!listChild.getFirstChild().getNodeValue().equalsIgnoreCase(adrTargetTemp.modeTimeSlot)) {
                    	//TODO why?
                    	adrTargetTemp.setPriceRelative(null);
                    }
                }
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

                } else if (nodeName.equalsIgnoreCase("p:timeOffset")) {

                    if (!listChild.getFirstChild().getNodeValue().equalsIgnoreCase(adrTargetTemp.modeTimeSlot)) {
                    	//TODO why?
                    	adrTargetTemp.setLoadAmount(null);
                    }
                }
            }

            parseList(listChild.getChildNodes(), nodeName);

        }

    }
}
