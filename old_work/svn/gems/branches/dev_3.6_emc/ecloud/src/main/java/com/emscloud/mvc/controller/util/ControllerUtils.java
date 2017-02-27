package com.emscloud.mvc.controller.util;


public class ControllerUtils {
    
    static private ControllerUtils    instance = null;
    // 0 : No other bill process is running
    // 1 : Some other bill process is running
    private int billProcessRunningStatus = 0; 
    
    public static ControllerUtils getInstance() {
        if (instance == null) {
            synchronized (ControllerUtils.class) {
                if (instance == null) {
                    instance = new ControllerUtils();
                }
            }
        }
        return instance;
    } // end of method getInstance
    
    private ControllerUtils()
    {
        
    }
    /**
     * @return the billProcessRunningStatus
     */
    public synchronized int getBillProcessRunningStatus() {
        return billProcessRunningStatus;
    }
    /**
     * @param billProcessRunningStatus the billProcessRunningStatus to set
     */
    public synchronized void setBillProcessRunningStatus(int billProcessRunningStatus) {
        this.billProcessRunningStatus = billProcessRunningStatus;
    }
}
