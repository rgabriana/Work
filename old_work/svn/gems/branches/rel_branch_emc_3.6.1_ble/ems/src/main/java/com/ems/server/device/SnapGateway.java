/**
 * 
 */
package com.ems.server.device;

import redstone.xmlrpc.XmlRpcFault;
import redstone.xmlrpc.XmlRpcStruct;

public interface SnapGateway {
  
  public static int SNAP_SERIAL_CONNECT = 1;
  public static int SNAP_USB_CONNECT = 2;
 
  public boolean addSpy(String filename, byte[] data);
  public boolean clearQueuedResponses(byte[] snapNetAddr);
  public boolean connectSerial(int type, int port, boolean reconnect) throws XmlRpcFault;
  public boolean connectSerial(int type, String port, boolean reconnect) throws XmlRpcFault;
  public boolean delSpy(String filename);
  public boolean disconnect(byte[] snapNetAddr, boolean clear);
  public boolean exit();
  public InfoResponse gatewayInfo();
  public boolean mcastRpc(byte[] snapNetAddr, byte[] group, int ttl, String scriptName, Object[] args) throws XmlRpcFault;
  public boolean rpc(byte[] snapNetAddr, byte[] remoteNetAddr, String scriptName, Object[] args) throws XmlRpcFault;
  public boolean sendDataModePkt(byte[] snapNetAddr, byte[] remoteNetAddr, Object data) throws XmlRpcFault;
  public boolean uploadSpy(byte[] remoteNetAddr, String filename) throws XmlRpcFault;
  public XmlRpcStruct waitOnEvent(byte[] snapNetAddr, boolean flush, int portType, int portNum, double timeout) throws XmlRpcFault;
  public XmlRpcStruct waitOnXmlMethod(byte[] snapNetAddr, boolean flush, int portType, int portNum, double timeout) throws XmlRpcFault;
	
} //end of interface SnapGateway
