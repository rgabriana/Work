package com.ems.server.data;

import com.ems.model.Plugload;

public class PlugloadPMStatsWork {

    private Plugload plugload;
    private byte[] packet;
    private int seqNo;

    public PlugloadPMStatsWork(Plugload plugload, byte[] packet, int seqNo) {

        this.plugload = plugload;
        this.packet = packet;
        this.seqNo = seqNo;

    } // end of constructor

    public Plugload getPlugload() {
        return plugload;
    }

    public byte[] getPacket() {
        return packet;
    }

    public int getSeqNo() {
        return seqNo;
    }

    public void cleanup() {
      
      plugload = null;
      packet = null;
      
    }

} // end of class PlugloadPMStatsWork
