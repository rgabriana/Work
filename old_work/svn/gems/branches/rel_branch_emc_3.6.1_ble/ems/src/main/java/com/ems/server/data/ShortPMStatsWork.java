package com.ems.server.data;

import com.ems.model.Fixture;

public class ShortPMStatsWork {

    private Fixture fixture;
    private byte[] packet;
    private int seqNo;

    public ShortPMStatsWork(Fixture fixture, byte[] packet, int seqNo) {

        this.fixture = fixture;
        this.packet = packet;
        this.seqNo = seqNo;

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

    public void cleanup() {
      
      fixture = null;
      packet = null;
      
    }

} // end of class StatsWork
