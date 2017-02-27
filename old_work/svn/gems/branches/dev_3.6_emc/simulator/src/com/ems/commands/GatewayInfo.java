package com.ems.commands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.ems.utils.Utils;

public class GatewayInfo implements ICommandPktFrame {

    char cmd = (byte) 0x3;
    int txnid = Utils.getNextSeqNo();
    byte[] mac = { 0, 0, 0, 0, 0, 0 }; // 6 bytes
    byte[] ip = { 0, 0, 0, 0 }; // 4 bytes
    byte[] netmask = { 0, 0, 0, 0 }; // 4 bytes
    byte[] router_ip = { 0, 0, 0, 0 }; // 4 bytes
    char channel = (byte) 9;
    char radio_rate = (byte) 2;
    byte[] wireless_key = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }; // 17
                                                                                 // bytes
    byte[] net_id = { 0, 0 }; // 2 bytes

    int uptime_in_seconds = (int) System.currentTimeMillis();
    int packets_from_GEMS = 0;
    int packets_to_GEMS = 0;
    int packets_to_nodes = 0;
    int packets_from_nodes = 0;

    // version info
    char release_version = (byte) 2;
    byte[] bug_fix_number = { 1, 3 }; // 2 bytes
    char major = (byte) 4;
    char minor = (byte) 1;
    char app_id = (byte) 2;
    // end

    char release_version_alt = (byte) 1;
    byte[] bug_fix_number_alt = { 1, 2 }; // 2 bytes
    char major_alt = (byte) 4;
    char minor_alt = (byte) 0;
    char boot_loader_version_MJ = (byte) 2;
    char boot_loader_version_MN = (byte) 1;

    @Override
    public byte[] toByte(String nodeName) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            output.write((byte) cmd);
            output.write(Utils.intToByteArray(txnid));
            output.write(mac);
            output.write(ip); // 4 bytes
            output.write(netmask); // 4 bytes
            output.write(router_ip); // 4 bytes
            output.write((byte) channel);
            output.write((byte) radio_rate);
            output.write(wireless_key); // 17 bytes
            output.write(net_id); // 2 bytes

            output.write(Utils.intToByteArray(uptime_in_seconds));
            output.write(Utils.intToByteArray(packets_from_GEMS));
            output.write(Utils.intToByteArray(packets_to_GEMS));
            output.write(Utils.intToByteArray(packets_to_nodes));
            output.write(Utils.intToByteArray(packets_from_nodes));

            // version info
            output.write((byte) release_version);
            output.write(bug_fix_number); // 2 bytes
            output.write((byte) major);
            output.write((byte) minor);
            output.write((byte) app_id);
            // end

            output.write(release_version_alt);
            output.write(bug_fix_number_alt); // 2 bytes
            output.write(major_alt);
            output.write(minor_alt);
            output.write(boot_loader_version_MJ);
            output.write(boot_loader_version_MN);
        } catch (IOException ioe) {

        }
        return output.toByteArray();
    }

    @Override
    public long getLength() {
        return 77;
    }
}
