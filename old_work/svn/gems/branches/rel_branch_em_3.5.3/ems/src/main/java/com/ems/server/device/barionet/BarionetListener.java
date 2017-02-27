/**
 * 
 */
package com.ems.server.device.barionet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.server.EmsShutdownObserver;
import com.ems.server.ServerMain;
import com.ems.service.ContactClosureManager;

/**
 * @author enlighted
 *
 */
public class BarionetListener extends Thread implements EmsShutdownObserver {
	private static final Logger m_Logger = Logger.getLogger("SysLog");
	private static final int BARIONET_TELNET_PORT = 12302;
	private boolean isRunning = true;

	private Socket clientSocket = null;
	private ContactClosureManager contactClosureManager;
	private String sMacAddress;
	private String sIpAddress;
	private PrintWriter out = null;

	public BarionetListener(String sMacAddress, String sIpAddress) {
		ServerMain.getInstance().addShutdownObserver(this);
		contactClosureManager = (ContactClosureManager) SpringContext
				.getBean("contactClosureManager");
		this.sMacAddress = sMacAddress;
		this.sIpAddress = sIpAddress;
	}

	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * statechange,1,0\nstatechange,2,0\nstatechange,3,0\nstatechange,4,0\n
	 * statechange
	 * ,201,0\nstatechange,202,0\nstatechange,203,0\nstatechange,204,0
	 * 
	 * @param contactClosureVo
	 */
	@Override
	public void run() {
		m_Logger.info("BL-" + sMacAddress + "-"
				+ Thread.currentThread().getId() + " " + sIpAddress
				+ " Running...");
		if (sIpAddress.equals("")) {
			m_Logger.debug("CC Device not yet discovered!");
			return;
		}

		BufferedReader inFromServer = null;
	
		try {
			clientSocket = new Socket(sIpAddress, BARIONET_TELNET_PORT);
			inFromServer = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			while (isRunning) {
				try {
					String sLine = "";
					Map<String, Integer> oInputTriggerMap = new HashMap<String, Integer>();
					boolean bProcess = false;
					while ((sLine = inFromServer.readLine()) != null) {

						String[] sData = sLine.split(",");
						m_Logger.debug(sIpAddress + ", received: " + sLine);
						if (sData != null && sData.length == 3
								&& sData[0].equalsIgnoreCase("statechange")) {
							try {
								oInputTriggerMap.put(sData[1],
										Integer.valueOf(sData[2]));
								bProcess = true;
							} catch (NumberFormatException nfe) {
								m_Logger.error(sIpAddress + " => " + sLine);
							}
						}
						if (!inFromServer.ready()) {
							break;
						}
					}
					if (bProcess) {
						contactClosureManager.checkInputTriggerStatus(
								sMacAddress, oInputTriggerMap);
					}
					// wait before reading the next time for input change
					oInputTriggerMap.clear();
					oInputTriggerMap = null;
					
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					m_Logger.error("BL-" + Thread.currentThread().getId()
							+ " interrupted!");
				}
			}
		} catch (IOException e) {
			m_Logger.error(e.getMessage());
		} catch (Exception e) {
			m_Logger.error(e.getMessage());

		} finally {
			if (out != null) {
				out.close();
				out = null;
			}
			
			if (inFromServer != null) {
				try {
					inFromServer.close();
				} catch (IOException e) {
					m_Logger.error(e.getMessage());
				}
			}
			if (clientSocket != null) {
				try {
					clientSocket.close();
				} catch (IOException e) {
					m_Logger.error(e.getMessage());
				}
			}
			inFromServer = null;
			clientSocket = null;
			isRunning = false;
			contactClosureManager.removeCCEntry(sMacAddress);
		}
		m_Logger.info("BL-" + sMacAddress + "-"
				+ Thread.currentThread().getId() + " stopping...");
	}

	@Override
	public void cleanUp() {
		isRunning = false;
		if (clientSocket != null) {
			try {
				clientSocket.close();
			} catch (IOException e) {
				m_Logger.error(e.getMessage());
			}finally {
				clientSocket = null;
			}
		}

	}
	
	public void ping() {
		if (out != null) {
			out.println("version");
			if (out.checkError()) {
				m_Logger.debug(
						"BL-" + Thread.currentThread().getId()
								+ " Remote server has terminated connection!");
				cleanUp();
			}
		}
	}

}
