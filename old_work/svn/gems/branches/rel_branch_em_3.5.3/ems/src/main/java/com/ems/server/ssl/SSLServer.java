/**
 * 
 */
package com.ems.server.ssl;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Enumeration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.model.NetworkInterfaceMapping;
import com.ems.server.util.ServerUtil;
import com.ems.service.NetworkSettingsManager;
import com.ems.types.NetworkType;

/**
 * @author Sreedhar
 * 
 */
public class SSLServer {

    private static SSLServerSocketFactory tlsSrvSocketFactory = null;

    private static SSLServer instance = null;

    private static String keyStoreLocation = "";

    private static Logger sslLogger = Logger.getLogger("SSLLogger");

    /**
   * 
   */
    private SSLServer() {
        // TODO Auto-generated constructor stub
    }

    public static SSLServer getInstance() {

        if (instance == null) {
            synchronized (SSLServer.class) {
                if (instance == null) {
                    instance = new SSLServer();
                }
            }
        }
        return instance;

    } // end of method getInstance

    /**
     * @param args
     */
    public static void main(String[] args) {

        // TODO Auto-generated method stub
        keyStoreLocation = args[0];
        init();
        getInstance().startSSLServer();

    } // end of method main

    private String getEth0Address() {
    	NetworkSettingsManager networkSettingsManager = (NetworkSettingsManager) SpringContext.getBean("networkSettingsManager");

		String nimCorporate = networkSettingsManager.loadCurrentMappingByNetworkType(NetworkType.Corporate.getName());
		String corporateMapping="eth0";
		if(nimCorporate != null){
			corporateMapping = nimCorporate;
		}		
        String ip = "localhost";
        try {
            NetworkInterface ni = NetworkInterface.getByName(corporateMapping);
            sslLogger.debug("interface name - " + ni.getDisplayName());
            Enumeration<InetAddress> ipEnum = ni.getInetAddresses();
            while (ipEnum.hasMoreElements()) {
                InetAddress iAddr = (InetAddress) ipEnum.nextElement();
                if (!(iAddr instanceof Inet6Address)) {
                    // sslLogger.debug(" ip -- " + iAddr.getHostAddress());
                    ip = iAddr.getHostAddress();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ip;

    } // end of method getEth0Address

    private void startSSLServer() {
    	

        try {
            SSLServerSocket ssocket = (SSLServerSocket) tlsSrvSocketFactory.createServerSocket();
            String eth0Ip = getEth0Address();
            sslLogger.debug("eth0 ip address - " + eth0Ip);
            ssocket.bind(new InetSocketAddress(eth0Ip, SSLConstants.GW_SSL_PORT));
            // sslLogger.debug(ssocket.getInetAddress().getHostAddress());

            String[] suites = ssocket.getSupportedCipherSuites();
            for (int i = 0; i < suites.length; i++) {
                if (suites[i].startsWith("TLS_RSA")) {
                    // sslLogger.debug(suites[i]);
                }
            }

            String[] protocols = ssocket.getSupportedProtocols();
            for (int i = 0; i < protocols.length; i++) {
                // sslLogger.debug(protocols[i]);
            }
            ssocket.setWantClientAuth(false);
            ssocket.setNeedClientAuth(false);
            ssocket.setEnabledProtocols(new String[] { SSLConstants.TLS_PROTOCOL_VERSION });
            ssocket.setEnabledCipherSuites(SSLConstants.TLS_CIPHER_SUITES);

            // Listen for connections
            SSLSocket socket = null;
            while ((socket = (SSLSocket) ssocket.accept()) != null) {
                sslLogger.debug("got the client socket connection");
                socket.startHandshake();
                SSLClientThread clientThread = new SSLClientThread(socket);
                clientThread.start();
            } // end of while loop
        } catch (Exception e) {
            e.printStackTrace();
        }

    } // end of method startSSLServer

    private static void init() {

        SecureRandom secRand = new SecureRandom();
        try {
            SSLContext tlsSc = SSLContext.getInstance(SSLConstants.TLS_PROTOCOL_VERSION);

            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            char[] password = "enLighted".toCharArray();
            // keyStoreLocation = "d:/work/mySrvKeystore";
            InputStream in = (new FileInputStream(keyStoreLocation));
            ks.load(in, password);
            in.close();

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509", "SunJSSE");
            kmf.init(ks, password);

            tlsSc.init(kmf.getKeyManagers(), new TrustManager[] { new SSLTrustManager() }, secRand);
            tlsSrvSocketFactory = tlsSc.getServerSocketFactory();
            sslLogger.debug("initialized the server socket");
        } catch (Exception e) {
            e.printStackTrace();
        }

    } // end of method init

    public class SSLClientThread extends Thread {

        SSLSocket clientSocket = null;

        public SSLClientThread(SSLSocket socket) {

            clientSocket = socket;

        } // end of constructor

        private byte[] getSSLAuthKey(String authKey) throws Exception {

            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update("Enlighted Auth Key".getBytes());
            md.update(authKey.getBytes());
            return md.digest();

        } // end of method getSSLAuthKey

        public void run() {

            // Create streams to securely send and receive data to the client
            InputStream in = null;
            OutputStream out = null;
            try {
                in = clientSocket.getInputStream();
                out = clientSocket.getOutputStream();
                while (true) {
                    byte[] cmd = receiveCommand(in);                    
                    sslLogger.debug(ServerUtil.getLogPacket(cmd));
                    if (cmd[0] != 0x58) {
                        // it is not a command. it is a authentication key
                        byte[] authAck = getSSLAuthKey("enLightedWorkNow");
                        sendAck(out, authAck);
                        continue;
                    }
                    byte[] ackPkt = { 0x58, 0x1, 0x0, 0xf, 0x0, 0x0, 0x4, 0x55, (byte) 0xbb, (byte) 0xdb, 0x0, 0x0,
                            0x3, 0xb, 0x5e };
                    sendAck(out, ackPkt);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    // Close the socket
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } // end of method run

        private void sendAck(OutputStream os, byte[] pkt) {

            try {
                sslLogger.debug("sending the ack");
                os.write(pkt);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } // end of method sendAck

        private byte[] receiveCommand(InputStream in) {

            byte[] rcvByteArr = new byte[128];
            int len = 0;
            try {
                while (true) {
                    len = in.read(rcvByteArr);
                    if (len == -1) {
                        break;
                    } else {
                        if (rcvByteArr[0] != 0x58) {
                            return rcvByteArr;
                        }
                        byte[] pktLenArr = { rcvByteArr[2], rcvByteArr[3] };
                        int pktLen = ServerUtil.byteArrayToShort(pktLenArr);
                        byte[] pkt = new byte[pktLen];
                        System.arraycopy(rcvByteArr, 0, pkt, 0, pktLen);
                        return pkt;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }

    } // end of class SSLClientThread

} // end of class SSLServer
