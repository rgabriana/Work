package com.ems.server.device;

import com.ems.server.util.ServerUtil;

//
// This class implements a multicast packet/manipulation that is sent
// over the wire. Each packet has a header (8) bytes
// followed by a list on 'n' targets. This is followed by
// a command (to be executed on the target node) and the
// data (which is arguments to this command).
//
// <STRT><HEADER><MCAST-CMD><NUM-TARGETS><TARGET-LIST...><ORIGINAL-CMD><DATA-ARGS><END>
//
// Technically the 8 byte header is followed by the command
// and arguments. However we put in a fake command (0xb2)
// which is interpreted by the Python script on the CU. This
// understands that this <MCAST-CMD> 0xb2 command contains the packet in 
// the format described. The script will reformat the packet
// into the <HDR><ORIGINAL-CMD><DATA-ARGS> format that the
// SU understands.
//
public class McastPacket {
	
  //
  // Base packet with 0 targets and no command. We add
  // to this packet (the targets and the command)
  //
  private static byte [] nullPkt = new byte [] {
    0x58,1, /* Start, Protocol Version */
    0, 11,	/* Length (MSB, LSB) */
    (byte)0xca, (byte)0xfe, (byte)0xba, (byte)0xbe, /* TXNID */
    (byte)0x5a, /* Multicast Command */
    0, /* Num-Targets */
    //0, 0, 0, 0,
    0x5e };
 
  private boolean commandSet;
  private byte [] packet;
	
  //
  // Create an initial packet
  //
  private void doInit () {
    
    packet = new byte [128];
    for (int x= 0; x< nullPkt.length; x++) {
      packet [x] = nullPkt [x];
    }
    commandSet = false;
  
  } //end of method doInit
  
  public void setTransactionId(int txId) {
    
    byte[] seqByteArr = ServerUtil.intToByteArray(txId);    
    System.arraycopy(seqByteArr, 0, packet, 4, seqByteArr.length);
    //System.out.println("assigned the tx id");
    
  } //end of method setTransactionId
  
  public McastPacket () {
		
    doInit ();

  } //end of constructor

  public void clear () {

    doInit ();

  } //end of method clear
	
  public int getNoOfTargets() {
	  
    return packet[9];
	  
  } //end of method getNoOfTargets

  /*
   * The packet is being built by adding 1 target at a
   * time. We have to assume that there is room in this
   * * Packet. The packet already exists (the constructor
   * took care of this) and may contain 0 or more targets.
   */
  public int addMcastTarget (byte [] dest) {

    int numTargets = packet [9];
    int off , x, tLen;
    
    if (numTargets >= DeviceServiceImpl.NO_OF_MULTICAST_TARGETS) {
      //System.out.println ("addMcastTarget: Too many nodes");
      return -1;
    }
    tLen = ServerUtil.extractShortFromByteArray(packet, 2);      
    /*
     * Copy the balance of the bytes after the last
     * last guy, and make room for the new. (Basically
     * I am adding 3 bytes - moving everything by 3 bytes)
     */
    
    off = 10 + (numTargets * 3);
    for (x = off; x < tLen; x++) {
      packet [x+3] = packet [x];
    }
    packet [9] = (byte)(numTargets + 1);
    packet [off] = dest [0];
    packet [off+1] = dest [1];
    packet [off + 2] = dest [2];
		
    /*
     * Whenever we add a target, we must the length
     */
    ServerUtil.fillShortInByteArray(tLen + 3, packet, 2);    
    return packet[9];
		
  } //end of method addMcastTarget

  public byte [] getPacket () {

    /*
     * This packet/array of bytes that is returned is sent
     * to the RPC. We have to truncate it to the right
     * size otherwise we will pull errors in the RPC (which may
     * complain that the packets are too large).
     */
    int len = packet [3];
    byte []rPkt = new byte [len];
    
    System.arraycopy(packet, 0, rPkt, 0, len);    
    return rPkt;
    
  } //end of method getPacket

  /*
   * The command is the byte that follows the multicast list
   * This command may take some data that are arguments to
   * this command.
   */
  public boolean setCommand (byte command, byte []args) {
    
    int tLen = packet [3];
    int x;
    if (commandSet) {
      return false;
    }
    packet [tLen -1] = command;
    for (x = 0; x < args.length; x++) {
      packet [tLen + x] = args [x];
    }
    packet [x+tLen] = 0x5e; /* End of Packet */
    /*
     * Now change the length (of the packet).
     */
    packet [3] = (byte) (tLen + 1 + args.length);
    commandSet = true;
    return true;
		
  } //end of method setCommand

  //
  // I am sure that there is a better way to do this
  // being a Java Rookie, I just wanted it done quickly.
  // Also I did not use all the printable characters.
  //
  public boolean isPrint (byte c) {
    
    if (c >= 'a' && c <= 'z') {
      return true;
    }
    if (c >= 'A' && c <= 'Z') {
      return true;
    }
    if (c >= '0' && c <= '9') {
      return true;
    }
    if (c == '!' || c == '@' || c == '#' || c == '$' || c == '%') {
      return true;
    }
    if (c == '^' || c == '&' || c == '*' || c == '(' || c == ')') {
      return true;
    }
    if (c == '_' || c == '-' || c == '+' || c == '=' || c == '[' || c == ']') {
      return true;
    }
    if (c == '<' || c == '>' || c == '?' || c == '/' || c == ':' || c == ';') {
      return true;
    }
    return false;
	
  } //end of method isPrint

  /*
   * Hexdump a buffer. We need a print 16 of them in one line
   * followed by the ASCII representation.
   */
  public void hexDump (byte []buf, int pSize) {
    
    for (int x = 0; x < pSize; x+= 16) {
      for (int i = 0; i < 16; i++) {
	if ((i != 0) && ((i % 4) == 0)) {
	  System.out.print(" ");
	}
	if ((i + x) < pSize) {
	  System.out.printf("%02x", buf [x+i]);
	} else {
	  System.out.print ("XX");
	}
      }
      System.out.print("   ");
      for (int i = 0; (i < 16) && ((i + x) < pSize); i++) {
	if ((i % 4) == 0) {
	  System.out.print(" ");
	}
	if (isPrint (buf [x+i])) {
	  System.out.print ((char)buf [x+i]);
	} else {
	  System.out.print(".");
	}
      }
      System.out.println ("");
    }
		
  } //end of method hexDump

  //
  // Print a packet (and then dump it via hexdump)
  //
  public void printPacket () {
    
    int x, pSize, nTargets, off;    
    pSize = packet [3];
    nTargets = packet [9];
    off = 10;
    
    System.out.println ("Packet Size: " + pSize);
    System.out.println ("Num Targets: " + nTargets);
    for (x= 0; x < nTargets; x++) {
      System.out.printf ("%d:%02x:%02x:%02x\n", x, packet [off], packet [off +1], packet [off +2]);
      off += 3;
    }
    hexDump (packet,  pSize);
    
  } //end of method printPacket
		
} //end of class McastPacket
