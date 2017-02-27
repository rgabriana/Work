package com.ems.XMLParser;



import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
//import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.util.*;

public class XMLParser {

	DocumentBuilderFactory dbFactory;
	DocumentBuilder dBuilder;
	Document doc;
	//String[] macs;
	//String[] iolists;

	public XMLParser(String Filename)
	{
		
		try{
		File inputFile = new File(Filename);
		dbFactory = DocumentBuilderFactory.newInstance();
        dBuilder = dbFactory.newDocumentBuilder();
        doc = dBuilder.parse(inputFile);
        //System.out.println("File Opened Successfully");
        doc.getDocumentElement().normalize();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public int getElementSize(String tagName)
	{
		System.out.println(doc.getTextContent());
		NodeList nl = doc.getElementsByTagName(tagName);
		int size = nl.getLength();
		return size;
	}
	
	public Map<String,String>  returnMacAndIOList()
	{
		Element rootElement = doc.getDocumentElement();
		Map<String,String> mactoIOListMapping = new HashMap<String,String>();
		NodeList nl = rootElement.getChildNodes();
		String[] macs = new String[10];
		String[] iolists = new String[10];
		int counter = 0;
		//int device = 0;
		String mac = "";
		String io = "";
		int p=0; 
		int r=0;
		if(nl != null)
		{
			for(int i=0; i<nl.getLength(); i++)
			{
				NodeList devices = nl.item(i).getChildNodes();
				for(int j=0; j<devices.getLength(); j++)
				{
					NodeList q = devices.item(j).getChildNodes();
					for(int k=0; k<q.getLength(); k++)
					{
						if(counter%2==0){
						   // System.out.println("Mac "+q.item(k).getNodeValue());
							mac = q.item(k).getNodeValue();
							macs[p] = mac;p++;
						}
						else{
							//System.out.println("IO "+q.item(k).getNodeValue());
							io = q.item(k).getNodeValue();
							iolists[r] = io;r++;
						}
						counter++;
						mactoIOListMapping.put(mac, io);
					}
				}
			}
		}
		
		//for(Map.Entry<String,String> s: mactoIOListMapping.entrySet())
		//{
			//System.out.println("Entries in the hash");
			//System.out.println(s.getKey());
			//System.out.println(s.getValue());
		//}
		return mactoIOListMapping;
	}
	
	public String[] getMacs()
	{
		Element rootElement = doc.getDocumentElement();
		//Map<String,String> mactoIOListMapping = new HashMap<String,String>();
		NodeList nl = rootElement.getChildNodes();
		String[] macs = new String[10];
		//String[] iolists = new String[10];
		int counter = 0;
		//int device = 0;
		String mac = "";
		//String io = "";
		int p=0; 
		//int r=0;
		if(nl != null)
		{
			for(int i=0; i<nl.getLength(); i++)
			{
				NodeList devices = nl.item(i).getChildNodes();
				for(int j=0; j<devices.getLength(); j++)
				{
					NodeList q = devices.item(j).getChildNodes();
					for(int k=0; k<q.getLength(); k++)
					{
						if(counter%2==0){
						   // System.out.println("Mac "+q.item(k).getNodeValue());
							mac = q.item(k).getNodeValue();
							macs[p] = mac;p++;
						}
						counter++;
					}
				}
			}
		}
		return macs;
	}
	
	public String[] getIoLists()
	{
		Element rootElement = doc.getDocumentElement();
		//Map<String,String> mactoIOListMapping = new HashMap<String,String>();
		NodeList nl = rootElement.getChildNodes();
		//String[] macs = new String[10];
		String[] iolists = new String[10];
		int counter = 0;
		//int device = 0;
		//String mac = "";
		String io = "";
		//int p=0; 
		int r=0;
		if(nl != null)
		{
			for(int i=0; i<nl.getLength(); i++)
			{
				NodeList devices = nl.item(i).getChildNodes();
				for(int j=0; j<devices.getLength(); j++)
				{
					NodeList q = devices.item(j).getChildNodes();
					for(int k=0; k<q.getLength(); k++)
					{
						if(counter%2!=0){
						     //System.out.println("IO "+q.item(k).getNodeValue());
							io = q.item(k).getNodeValue();
							iolists[r] = io;r++;
						}
						counter++;
					}
				}
			}
		}
		return iolists;
	}
	
}
