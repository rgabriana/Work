package com.enlightedinc.dashboard.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Path("org/ec")
public class EnergyDataDetails{
	static final Logger logger = Logger.getLogger(EnergyDataDetails.class.getName());
	
	@Context
	ServletContext context;
	
	/**
	 * Return Total no of fixtures
	 * @param property
	 *            (company|campus|building|floor|area|fixture)
	 * @param pid
	 *            property unique identifier
	 * @param fixedPeriod
	 *            (CURRENT|DAY|WEEK|MONTH|YEAR)
	 * @param fdate
	 *            starting date back to the specified period.
	 * @param ip
	 *            IP of gem server
	 * @return Count of total no of fixtures         
	 */
	
	@Path("/count/fcp/{property}/{pid}/{fixedperiod}/{fdate}/{ip}")
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public String getNumberOfFixtures(@PathParam("property") String property,@PathParam("pid") String pid,@PathParam("fixedperiod") String fixedperiod,@PathParam("fdate") String fdate,@PathParam("ip") String ip) {
		String url=null;
		url="https://"+ip+"/ems/services/org/ec/count/fcp/"+property+"/"+pid+"/"+fixedperiod+"/"+fdate;
        String out="";
        Globals oCommStatus = new Globals();
        Communication cm = new Communication(url,oCommStatus);
        if(oCommStatus.state == 100)
        {
        	cm.recvData() ;
        	logger.debug("FCP Commucation Successful => URL: " + url + "\n" + oCommStatus.buffer);
        	out= oCommStatus.buffer;
        }else
        {
        	logger.debug("***Commucation Over HTTP is unsuccessful***");
        	return "<status>-1</status>";
        }
        return out;
    }

	/**
	 * Return Meter data with range value in KWH
	 * 
	 * @param property
	 *            (company|campus|building|floor|area|fixture)
	 * @param pid
	 *            property unique identifier
	 * @param fdate
	 *            starting date back to the specified period.
	 * @param tdate
	 *            ending date in specified period.
	 * @param ip
	 *            IP of gem server
	 */
	
	@Path("/em/{property}/{pid}/{fdate}/{tdate}/{ip}")
    @GET
    @Produces({MediaType.APPLICATION_XML})
    public String getMasterEnergyMeter(@PathParam("property") String property,@PathParam("pid") String pid,@PathParam("fdate") String fdate,@PathParam("tdate") String tdate,@PathParam("ip") String ip) {

        String out = "getMasterEnergyMeter";
        
        return out;
    }
	
	/**
	 * Return Meter data with range in percentage
	 * 
	 * @param property
	 *            (company|campus|building|floor|area|fixture)
	 * @param pid
	 *            property unique identifier
	 * @param fdate
	 *            starting date back to the specified period.
	 * @param tdate
	 *            ending date in specified period.
	 * @param ip
	 *            IP of gem server
	 */
	@Path("/md/{property}/{pid}/{fdate}/{tdate}/{ip}")
    @GET
    @Produces({MediaType.APPLICATION_XML})
    public String loadMeterDataWithDateRange( @PathParam("property") String property,@PathParam("pid") String pid,@PathParam("fdate") String fdate,@PathParam("tdate") String tdate,@PathParam("ip") String ip) {
		String url=null;
		url="https://"+ip+"/ems/services/org/ec/md/"+property+"/"+pid+"/"+fdate+"/"+tdate;
        String out="";
        Globals oCommStatus = new Globals();
        Communication cm = new Communication(url,oCommStatus);
        if(oCommStatus.state == 100)
        {
        	cm.recvData() ;
        	logger.debug("MD Commucation Successful => URL: " + url + "\n" + oCommStatus.buffer);
        	out= oCommStatus.buffer;
        }else
        {
        	logger.debug("***Commucation Over HTTP is unsuccessful***");
        	return "<status>1</status>";
        }
        return out;
    }
	
	
	/**
	 * Return energy consumption
	 * @param durationType
	 * @param ip IP of gem server
	 */
	@Path("/load/areareport/piechart/{duration}/{gemIP}")
    @GET
    @Produces({MediaType.APPLICATION_XML})
    public String loadAreaReportPieChartData(@PathParam("duration") String durationType,@PathParam("gemIP") String gemIPAddress) {

        String out = "loadAreaReportPieChartData";
        
        return out;
    }
	
	/**
	 * Return energy consumption for mini gems
	 * 
	 * @param property
	 *            (company|campus|building|floor|area|fixture)
	 * @param pid
	 *            property unique identifier
	 * @param fixedperiod
	 *            (CURRENT|DAY|WEEK|MONTH|YEAR)
	 * @param fdate
	 *            starting date back to the specified period.
	 * @param ip
	 *            IP of gem server
	 */
	@Path("/cp/{property}/{pid}/{fixedperiod}/{fdate:.*}/{ip}")
    @GET
    @Produces({MediaType.APPLICATION_XML})
	
    public String loadEnergyConsumption(@PathParam("property") String property,@PathParam("pid") String pid,@PathParam("fixedperiod") String fixedperiod,@PathParam("fdate") String fdate,@PathParam("ip") String ip) {
		
		String url=null;
		url="https://"+ip+"/ems/services/org/ec/cp/"+property+"/"+pid+"/"+fixedperiod+"/"+fdate;
        String out="";
        logger.debug("url "+ url);
        Globals oCommStatus = new Globals();
        Communication cm = new Communication(url, oCommStatus);
        if(oCommStatus.state == 100)
        {
        	cm.recvData() ;
        	logger.debug("CP Commucation Successful => URL: " + url + "\n" + oCommStatus.buffer);
        	out= oCommStatus.buffer;
        }else
        {
        	logger.debug("***Commucation Over HTTP is unsuccessful***");
        	return "<status>1</status>";
        }
        return out;
    }
	/**
	 * Method will return XML String of the Serverdata.xml
	 * @Return XML String to UI - Which will be displayed in the form of Tree.
	 * 
	 */
	@Path("/load/data")
    @GET
    @Produces({MediaType.APPLICATION_XML})
	public String loadServerDataXML()
	{
		String xmlString="";
		
		try{
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            String path = context.getRealPath(".."+File.separatorChar+".."+File.separatorChar+"Enlighted");
            String op = path + File.separatorChar + "Serverdata.xml";
            //String op ="D:\\Workspace\\lems\\ems_dashboard\\dashboard\\src\\main\\webapp\\modules\\Serverdata.xml";
            InputStream inputStream = new FileInputStream(new File(op));
            //logger.info("op --------> "+ op);
            org.w3c.dom.Document doc = documentBuilderFactory.newDocumentBuilder().parse(inputStream);
            StringWriter stw = new StringWriter();
            Transformer serializer = TransformerFactory.newInstance().newTransformer();
            serializer.transform(new DOMSource(doc), new StreamResult(stw));
            xmlString = stw.toString();
          }
          catch (Exception e) {
            logger.debug(e.getMessage());
            return "<status>1</status>";
          } 
		
		return xmlString;
		
	}
	
	/**
	 * Method will save xml string coming from UI to serverdata.xml
	 * Return Serverdata.xml to UI - Which will be displayed in the form of Tree.
	 * @param XML String
	 * @return String value: 1- Success, 0- Fail
	 */
	
	@POST  
    @Path("/save/data")   
    @Consumes("application/xml")   
    @Produces("text/plain")   
    public String saveServerDataXML(String incomingXML) {  
		logger.debug("incomingXML :" + incomingXML); 
		
        DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();      
        DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.debug(e.getMessage());
		}
        // Read serverdata.xml file      
        Document dct = null;
		try {
			String path = context.getRealPath(".."+File.separatorChar+".."+File.separatorChar+"Enlighted");
            String op = path + File.separatorChar + "Serverdata.xml";
            //String op ="D:\\Workspace\\lems\\ems_dashboard\\dashboard\\src\\main\\webapp\\modules\\Serverdata.xml";
          	dct = db.parse(op);
		} catch (SAXException e) {
			logger.debug(e.getMessage());
		} catch (IOException e) {
			logger.debug(e.getMessage());
			dct = db.newDocument();
			Element rootElement = dct.createElement("company");
			dct.appendChild(rootElement);
			
			Attr attr1 = dct.createAttribute("id");
			attr1.setValue("1");
			rootElement.setAttributeNode(attr1);
			
			Attr attr2 = dct.createAttribute("name");
			attr2.setValue("GEMS");
			rootElement.setAttributeNode(attr2);
			
			Attr attr3 = dct.createAttribute("type");
			attr3.setValue("company");
			rootElement.setAttributeNode(attr3);
		}
        Element root = dct.getDocumentElement();      
        Document document = null;
		try {
			document = db.parse( new InputSource(new StringReader(incomingXML)));
		} catch (SAXException e) {
			logger.debug(e.getMessage());
		} catch (IOException e) {
			logger.debug(e.getMessage());
		}
        Element root2=document.getDocumentElement();      
            
        // Copy the existing XML file with newly created element      
        root.appendChild(dct.importNode(root2,true));      
        Transformer transformer = null;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException e) {
			logger.debug(e.getMessage());
		} catch (TransformerFactoryConfigurationError e) {
			logger.debug(e.getMessage());
		}
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");      
        StreamResult result = null;
		try {
			String path = context.getRealPath(".."+File.separatorChar+".."+File.separatorChar+"Enlighted");
			String op = path + File.separatorChar + "Serverdata.xml";
            //String op ="D:\\Workspace\\lems\\ems_dashboard\\dashboard\\src\\main\\webapp\\modules\\Serverdata.xml";
            logger.debug("Path before Saving--------> "+ op);
			result = new StreamResult(new FileWriter(op));
			
		} catch (IOException e) {
			logger.debug(e.getMessage());
		}
        DOMSource source = new DOMSource(dct);      
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			logger.debug(e.getMessage());
		}
		return "1";  
    } 
	
	/**
	 * Method will remove given node from XML file
	 * Return 0(Success)/1(Fail)
	 * @param XML String
	 * @return String value: 1- Success, 0- Fail
	 */
	
	@POST  
    @Path("/remove/node")   
    @Consumes("application/xml")   
    @Produces("text/plain")   
    public String removeItemInXML(String incomingXML) {  
			try{
				  String path = context.getRealPath(".."+File.separatorChar+".."+File.separatorChar+"Enlighted");
				  String op = path + File.separatorChar + "Serverdata.xml";
				  File file = new File(op);
				  if (file.exists()){
					  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					  DocumentBuilder builder = factory.newDocumentBuilder();
					  Document doc = builder.parse(op);
					  Element root = doc.getDocumentElement();
				      parseList(root.getChildNodes(), incomingXML);
				      doc.normalize();
					  TransformerFactory tFactory = TransformerFactory.newInstance();
					  Transformer tFormer = tFactory.newTransformer();
					  StreamResult result = null;
						try {
							result = new StreamResult(new FileWriter(op));
						} catch (IOException e) {
							logger.debug(e.getMessage());
						}
					  Source source = new DOMSource(doc);
					  // Result dest = new StreamResult(System.out);
					  tFormer.transform(source, result);
					  return "1";
				  }
				  else
				  {
					  logger.debug("File not found!");
				  }
			  }
			  catch (Exception e){
				  logger.debug(e);
			  }
			return "0";
    } 
	
	private void parseList(NodeList nList, String rootNodeName) {
        int listsz = nList.getLength();
        for (int h = 0; h < listsz; h++) // obtain the top level information
        {
            Node listChild = nList.item(h);
            String nodeName = listChild.getNodeName();
            if (nodeName.equalsIgnoreCase("gem")) {
	            NamedNodeMap nnm = listChild.getAttributes();
	            if(nnm.getNamedItem("name").getNodeValue().equalsIgnoreCase(rootNodeName)) {
	            	listChild.getParentNode().removeChild(listChild);
	            	return;
	            }
            }
            parseList(listChild.getChildNodes(), nodeName);
        }
	}
}
