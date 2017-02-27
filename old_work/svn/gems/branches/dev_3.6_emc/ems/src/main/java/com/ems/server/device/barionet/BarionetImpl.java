package com.ems.server.device.barionet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.server.EmsShutdownObserver;
import com.ems.server.ServerMain;
import com.ems.server.util.ServerUtil;
import com.ems.service.ContactClosureManager;
import com.ems.vo.ContactClosureVo;

/**
 * Purpose of this class is to start the Barionet network listener and provide
 * functions to search for Barionet devices and look for their input trigger
 * status
 * 
 * @author enlighted
 *
 */
public class BarionetImpl extends Thread implements EmsShutdownObserver {
	private static final Logger m_Logger = Logger.getLogger("SysLog");
	private boolean isRunning = true;
	private final static int port = 30718;
	private String host = "255.255.255.255";
	private final static int[] barixID = { 129, 136, 83, 129 };
	private final static int[] messageGet = { 129, 136, 83, 129, 1 };
	private final static int[] messageSet = { 129, 136, 83, 129, 2 };
	private byte byteGetResponse = -127;
	private int intGetResponse = 129;
	private byte byteSetResponse = -126;
	private int intSetResponse = 130;
	private byte byteConfigResponse = -125;
	private int intConfigResponse = 131;
	private int get = 1;
	private int set = 2;
	private int RebootCommand = 3;
	private int ClearCommand = 4;
	private int RebootClearCommand = 7;
	private int incomingBufferSize = 256;
	private byte[] barixIDByte = new byte[barixID.length];
	private DatagramSocket socket;

	private ContactClosureManager contactClosureManager;

	public BarionetImpl() {
		ServerMain.getInstance().addShutdownObserver(this);
		contactClosureManager = (ContactClosureManager) SpringContext
				.getBean("contactClosureManager");
	}
	
	public void stopRunning() {
		isRunning = false;
		if (socket != null) {
			socket.close();
			socket = null;
		}
	}

	public boolean isRunning() {
		return isRunning;
	}
	
	public void run() {
		try {
			m_Logger.info(getId() + ": Starting barionet discovery listener...");
			System.arraycopy(convertIntToByte(barixID), 0, this.barixIDByte, 0,
					this.barixIDByte.length);
			socket = new DatagramSocket(port);
			socket.setReceiveBufferSize(2048);
			while (isRunning) {
				byte[] buffer = new byte[incomingBufferSize];
				DatagramPacket localDatagramPacket = new DatagramPacket(
						buffer, buffer.length);
				try {
					socket.receive(localDatagramPacket);
					byte[] ccdatabuffer = localDatagramPacket.getData();
					if (m_Logger.isDebugEnabled()) {
						m_Logger.debug(localDatagramPacket.getAddress() + ":"
								+ port + " data received..."
								+ ServerUtil.getLogPacket(ccdatabuffer));
					}

					byte[] deviceIDArr = new byte[4];
					System.arraycopy(ccdatabuffer, 0, deviceIDArr, 0,
							deviceIDArr.length);
					if (Arrays.equals(deviceIDArr, barixIDByte)) {
						if ((ccdatabuffer[4] == byteGetResponse)
								|| (ccdatabuffer[4] == byteSetResponse)
								|| (ccdatabuffer[4] == byteConfigResponse)) {
							int[] arrayOfInt1 = convertByteToInt(ccdatabuffer);
							int[] arrayOfInt2 = new int[arrayOfInt1.length];
							System.arraycopy(arrayOfInt1, 0, arrayOfInt2, 0,
									arrayOfInt1.length);
							if (ccdatabuffer[4] != byteConfigResponse) {
								// device discovered. update the device
								ContactClosureVo oDevice = readDataRecord(arrayOfInt1);
								if (oDevice != null) {
									synchronized(this) {
										m_Logger.info("Discovered: "
												+ oDevice.getMacAddress() + ", "
												+ oDevice.getIpAddress());
										ContactClosureVo occ = contactClosureManager
												.getContactClosureVoByMacAddress("");
										if (occ != null) {
											occ.setMacAddress(oDevice
													.getMacAddress());
											occ.setIpAddress(oDevice.getIpAddress());
											contactClosureManager
													.updateDefaultContactClosure(occ);
										} else {
											occ = contactClosureManager
													.getContactClosureVoByMacAddress(oDevice
															.getMacAddress());
											if (occ == null) {
												contactClosureManager
														.addContactClosure(oDevice);
											} else {
												occ.setMacAddress(oDevice
														.getMacAddress());
												occ.setIpAddress(oDevice.getIpAddress());
												contactClosureManager
														.updateContactClosure(occ);
											}
										}
									}
								}
							}
						}
					}
					deviceIDArr = null;
					ccdatabuffer = null;
				} catch (IOException ioe) {
					m_Logger.error("Exiting Barionet discovery: ");
				} finally {
					buffer = null;
				}
			}
			m_Logger.info(getId() + ": Stopping barionet discovery listener...");
		} catch (SocketException e) {
			m_Logger.error("Socket error: ", e);
		} finally {
			if (socket != null) {
				socket.close();
			}
		}

	}

	public void sendBroadcastReq(NetworkInterface paramNetworkInterface) {

		byte[] message = new byte[messageGet.length];
		byte[] arrayOfByte = convertIntToByte(messageGet);
		System.arraycopy(arrayOfByte, 0, message, 0, messageGet.length);

		DatagramSocket dsocket = null;
		if (paramNetworkInterface != null) {
			InetAddress localInetAddress1 = null;
			InetAddress localInetAddress2 = null;

			Enumeration localEnumeration = paramNetworkInterface
					.getInetAddresses();
			int iCount = 0;
			while (localEnumeration.hasMoreElements()) {
				try {
					localInetAddress2 = (InetAddress) localEnumeration
							.nextElement();
					if ((!localInetAddress2.isLoopbackAddress())
							&& (localInetAddress2.getAddress().length == 4)) {
						dsocket = new DatagramSocket(port + 1,
								localInetAddress2);

						dsocket.setBroadcast(true);
						dsocket.setReuseAddress(true);
						localInetAddress1 = InetAddress.getByName(host);
						m_Logger.info("Listen on "
								+ dsocket.getLocalAddress() + " from "
								+ localInetAddress1 + " port "
								+ dsocket.getBroadcast());

						DatagramPacket localDatagramPacket = new DatagramPacket(
								message, message.length, localInetAddress1,
								port);
						// m_Logger.info(getLogPacket(message));

						while (iCount < 1) {
							dsocket.send(localDatagramPacket);
							iCount++;
							Thread.sleep(1000);
						}
					}
				} catch (Exception localException) {
					m_Logger.error("Error: ", localException);
					return;
				} finally {
					if (dsocket != null) {
						dsocket.close();
					}
				}
			}
		}
	}

	private ContactClosureVo readDataRecord(int[] paramArrayOfInt) {
		int errorStatus = -1;
		int[] ethernetAddr = { 0, 0, 0, 0, 0, 0 };
		int[] ipAddr = { 0, 0, 0, 0 };
		int[] ipMask = { 255, 255, 255, 0 };
		int[] ipGateway = { 0, 0, 0, 0 };
		int[] productID;
		int[] hwType;
		int[] fwVersion;
		char[] dhcpName;
		int[] applicationData;
		int i = 0;
		ContactClosureVo oDevice = null;
		try {
			System.arraycopy(convertByteToInt(InetAddress.getLocalHost()
					.getAddress()), 0, ipGateway, 0, ipGateway.length);
		} catch (Exception localException) {
			// TODO
		}
		ipGateway[(ipGateway.length - 1)] = 1;

		i += barixID.length;
		if (paramArrayOfInt[i] == intGetResponse) {
			oDevice = new ContactClosureVo();
			i += 1;
			System.arraycopy(paramArrayOfInt, i, ethernetAddr, 0,
					ethernetAddr.length);
			oDevice.setMacAddress(convertToByteString(ethernetAddr, ":"));

			i += ethernetAddr.length;
			System.arraycopy(paramArrayOfInt, i, ipAddr, 0, ipAddr.length);
			oDevice.setIpAddress(convertToString(ipAddr, "."));
			i += ipAddr.length;

			productID = new int[paramArrayOfInt[i]];
			i += 1;
			System.arraycopy(paramArrayOfInt, i, productID, 0, productID.length);
			oDevice.setProductId(convertToString(productID, ""));
			i += productID.length;

			hwType = new int[paramArrayOfInt[i]];
			i += 1;
			System.arraycopy(paramArrayOfInt, i, hwType, 0, hwType.length);
			oDevice.setHwType(convertToString(hwType, ""));
			i += hwType.length;

			fwVersion = new int[paramArrayOfInt[i]];
			i += 1;
			System.arraycopy(paramArrayOfInt, i, fwVersion, 0, fwVersion.length);
			oDevice.setFwVersion(convertToString(fwVersion, "."));
			i += fwVersion.length;

			dhcpName = new char[paramArrayOfInt[i]];
			i += 1;
			for (int j = 0; j < dhcpName.length; j++) {
				dhcpName[j] = ((char) paramArrayOfInt[(i + j)]);
			}

			i += dhcpName.length;
		} else if (paramArrayOfInt[i] == intSetResponse) {
			oDevice = new ContactClosureVo();
			i += 1;
			System.arraycopy(paramArrayOfInt, i, ethernetAddr, 0,
					ethernetAddr.length);
			oDevice.setMacAddress(convertToByteString(ethernetAddr, ":"));
			i += ethernetAddr.length;
			System.arraycopy(paramArrayOfInt, i, ipAddr, 0, ipAddr.length);
			oDevice.setIpAddress(convertToString(ipAddr, "."));
			i += ipAddr.length;
			errorStatus = paramArrayOfInt[i];
			i += 1;
		}
		int j = paramArrayOfInt[(i + 1)] * 16 * 16 + paramArrayOfInt[i];
		if (j > 0) {
			applicationData = new int[j];
			i += 2;
			System.arraycopy(paramArrayOfInt, i, applicationData, 0,
					applicationData.length);
		} else {
			applicationData = new int[0];
		}
		return oDevice;
	}

	private byte[] convertIntToByte(int[] paramArrayOfInt) {
		int i = paramArrayOfInt.length;
		byte[] arrayOfByte = new byte[i];
		for (int j = 0; j < i; j++) {
			if (paramArrayOfInt[j] > 127) {
				paramArrayOfInt[j] -= 256;
			}
			arrayOfByte[j] = Integer.valueOf(paramArrayOfInt[j] & 0xFF)
					.byteValue();
		}
		return arrayOfByte;
	}

	private int[] convertByteToInt(byte[] paramArrayOfByte) {
		int i = paramArrayOfByte.length;
		int[] arrayOfInt = new int[i];
		for (int j = 0; j < i; j++) {
			arrayOfInt[j] = paramArrayOfByte[j];
			if (arrayOfInt[j] < 0) {
				arrayOfInt[j] += 256;
			}
		}
		return arrayOfInt;
	}

	private String convertToString(int[] ip, String delimiter) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < ip.length; i++) {
			buffer.append(String.valueOf(ip[i] & 0xFF));
			if (i < ip.length - 1) {
				buffer.append(delimiter);
			}
		}
		return buffer.toString();
	}

	private String convertToByteString(int[] ip, String delimiter) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < ip.length; i++) {
			buffer.append(Integer.toHexString(ip[i]));
			if (i < ip.length - 1) {
				buffer.append(delimiter);
			}
		}
		return buffer.toString();
	}

	@Override
	public void cleanUp() {
		isRunning = false;
		if (socket != null) {
			socket.close();
			socket = null;
		}
	}
}
