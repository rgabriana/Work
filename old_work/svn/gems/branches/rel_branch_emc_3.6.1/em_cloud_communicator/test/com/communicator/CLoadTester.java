/**
 * 
 */
package com.communicator;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Iterator;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.communicator.callhome.CallHomeThread;
import com.communicator.em.EMInstance;
import com.communicator.em.EMManager;
import com.communicator.sppa.SyncThread;
import com.constants.CloudConstants;

/**
 * @author yogesh
 * 
 */
public class CLoadTester {
	public static final Logger logger = Logger.getLogger(CLoadTester.class
			.getName());

	private EMManager oManager = EMManager.getInstance();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new CLoadTester(args);
	}

	public CLoadTester(String[] args) {
		readEMList(args[0]);
		logger.info("HostServer: " + oManager.getsPPAHostServer()
				+ ", EM List: {" + oManager.toString() + "}");

		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		ObjectName name;
		try {
			name = new ObjectName("com.communicator.em:type=EMManager");
			mbs.registerMBean(oManager, name);
		} catch (MalformedObjectNameException mone) {
			logger.warn(mone);
		} catch (NullPointerException ne) {
			logger.warn(ne);
		} catch (InstanceAlreadyExistsException e) {
			logger.warn(e);
		} catch (MBeanRegistrationException e) {
			logger.warn(e);
		} catch (NotCompliantMBeanException e) {
			logger.warn(e);
		}

		int emCount = oManager.getEMCount();
		// Random rand = new Random();
		int jitter = Math.abs(CloudConstants.INTERVAL / emCount);
		if (jitter == 0) {
			jitter = 100;
		} else if (jitter > 10000) {
			jitter = 100;
		}

		// Start Sync Thread.
		Thread oSyncThread = new SyncThread();
		oSyncThread.start();

		Thread[] oEMThreads = new Thread[emCount];
		Collection<EMInstance> oEMList = oManager.getEMList();
		Iterator<EMInstance> oitr = oEMList.iterator();
		int count = 0;
		while (oitr.hasNext()) {
			try {
				Thread.sleep(jitter);
				EMInstance oInstance = oitr.next();
				oEMThreads[count] = new CallHomeThread(oInstance);
				oEMThreads[count].start();
				count++;
			} catch (InterruptedException e) {
				logger.warn(e);
			}
		}

		// Wait for Call home threads to finish.
		try {
			for (count = 0; count < emCount; count++) {
				Thread oThread = oEMThreads[count];
				if (oThread != null)
					oThread.join();
			}
		} catch (InterruptedException e) {
			logger.warn(e.getMessage());
		}
		// startWalLoadSimulator("68-54-F1-A9-80-F6", "2.2.0 b200",
		// "sppa.enlightedinc.com", "sppa.enlightedinc.com");
	}

	private void readEMList(String sFilePath) {
		File fXmlFile = new File(sFilePath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			NodeList oEList = doc.getElementsByTagName("server");
			if (oEList != null) {
				oManager.setsPPAHostServer(oEList.item(0).getFirstChild()
						.getNodeValue().trim());
			}
			NodeList nList = doc.getElementsByTagName("em");
			for (int count = 0; count < nList.getLength(); count++) {
				Node nNode = nList.item(count);
				EMInstance oInstance = new EMInstance();
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					NodeList nChildrenList = nNode.getChildNodes();
					for (int iChild = 0; iChild < nChildrenList.getLength(); iChild++) {
						Node nCNode = nChildrenList.item(iChild);
						if (nCNode.getFirstChild() == null)
							continue;
						if (nCNode.getNodeName().equalsIgnoreCase("mac")) {
							oInstance.setsMAC(nCNode.getFirstChild()
									.getNodeValue().trim());
						} else if (nCNode.getNodeName().equalsIgnoreCase(
								"version")) {
							oInstance.setsVersion(nCNode.getFirstChild()
									.getNodeValue().trim());
						}
					}
				}
				if (oInstance.getsMAC() != null)
					oManager.addEM(oInstance);
				else
					oInstance = null;
			}
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
