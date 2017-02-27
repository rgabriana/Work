package com.ems.service;

import java.util.Properties;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.BACnetConfig;
import com.ems.server.device.BacnetService;

@Service("bacnetManager")
@Transactional(propagation = Propagation.REQUIRED)
public class BacnetManager {

    public BACnetConfig getConfig() {
        Properties props = BacnetService.getInstance().getBacnetConfiguration();
        BACnetConfig bacnetConfig = new BACnetConfig();
        if (props != null) {

            bacnetConfig.setApduLength(Integer.parseInt(props.getProperty("MaxAPDU")));
            bacnetConfig.setApduTimeout(Integer.parseInt(props.getProperty("APDUTimeout")));
            bacnetConfig.setDeviceBaseInstance(Long.parseLong(props.getProperty("DeviceBaseInstance")));
            bacnetConfig.setServerPort(Integer.parseInt(props.getProperty("ListenPort")));
            bacnetConfig.setNetworkId(Integer.parseInt(props.getProperty("NetworkId")));
            bacnetConfig.setVendorId(props.getProperty("VendorId"));

        } else {
            return null;
        }
        return bacnetConfig;
    }

    public String saveConfig(BACnetConfig config) {
        Properties props = new Properties();
        props.setProperty("APDUTimeout", "" + config.getApduTimeout());
        props.setProperty("DeviceBaseInstance", "" + config.getDeviceBaseInstance());
        props.setProperty("ListenPort", "" + config.getServerPort());
        props.setProperty("MaxAPDU", "" + config.getApduLength());
        props.setProperty("NetworkId", "" + config.getNetworkId());
        props.setProperty("VendorId", "" + config.getVendorId());
        try {
            BacnetService.getInstance().setBacnetConfiguration(props);
        } catch (Exception e) {
            e.printStackTrace();
            return "SAVE_ERROR";
        }
        return "SAVE_SUCCESS";
    }

}
