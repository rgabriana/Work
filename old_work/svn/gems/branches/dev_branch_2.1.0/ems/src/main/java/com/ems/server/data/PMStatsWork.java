package com.ems.server.data;

import com.ems.model.Fixture;

public class PMStatsWork {

    private Fixture fixture;
    private byte[] packet;
    private int seqNo;
    private int pktLen;

    public PMStatsWork(Fixture fixture, byte[] packet, int seqNo, int pktLen) {

        this.fixture = fixture;
        this.packet = packet;
        this.seqNo = seqNo;
        this.pktLen = pktLen;

    } // end of constructor

    public Fixture getFixture() {
        return fixture;
    }

    public byte[] getPacket() {
        return packet;
    }

    public int getSeqNo() {
        return seqNo;
    }
    
    public int getPacketLen() {
      return pktLen;
    }

    public void cleanup() {
      
      fixture = null;
      packet = null;
      
    }

} // end of class StatsWork
