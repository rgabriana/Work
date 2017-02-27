/**
 * 
 */
package com.ems.gw;

import java.net.InetAddress;

import com.ems.utils.CommonQueue;
import com.ems.utils.Utils;

/**
 * @author yogesh
 * 
 */
public interface GWInterface {
    public Thread getKeepRunningtransmitter();

    public Thread getKeepRunningreciever();

    public Thread getKeepRunningSuCmdProcessor();

    public Thread getKeepRunningSuCmdProcessorLP();

    public int getCommonQueueNo();

    public void on();

    public void off();

    public CommonQueue getGatewayQueue();

    public void setGateWayQueue(CommonQueue cq);
    
    public InetAddress getM_gwIPAddress();
}
