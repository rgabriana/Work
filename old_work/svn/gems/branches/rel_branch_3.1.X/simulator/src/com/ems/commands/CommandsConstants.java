/**
 * 
 */
package com.ems.commands;

/**
 * @author yogesh
 * 
 */
public final class CommandsConstants {

    public static final int FRAME_NEW_START_MARKER = 0x58;
    public static final int FRAME_END_MARKER = 0x5e;

    // gateway commands
    public static final byte GW_MAGIC_FIRST = 'e';
    public static final byte GW_MAGIC_SECOND = 's';

    public static final short GATEWAY_SET_ADDR = 0x0;
    public static final short GATEWAY_CMD_STATUS = 0x1;
    public static final short GATEWAY_CMD_INFO = 0x2;
    public static final short GATEWAY_INFO_RESP = 0x3;
    public static final short GATEWAY_WIRELESS_CMD = 0x4;
    public static final short GATEWAY_WIRELESS_RESP = 0x5;
    public static final short GATEWAY_START_SCAN = 0x6;
    public static final short GATEWAY_SCAN_RESP = 0x7;
    public static final short GATEWAY_REBOOT_CMD = 0xa;
    public static final short GATEWAY_FILE_XFER = 0x0b;
    public static final short GATEWAY_PGM_RADIO = 0x0c;
    public static final short GATEWAY_FILE_XFER_STATUS = 0x0d;
    public static final short GATEWAY_PMG_RADIO_STATUS = 0x0e;
    public static final short GATEWAY_ACK = 0xbd;
    public static final short GATEWAY_AUTH_CMD = 0xf;
    public static final short GATEWAY_UPGRADE_CMD = 0xab;
    public static final short GATEWAY_UPGRADE_STATUS = 0xad;

    // su commands
    public static final int IMAGE_UPGRADE_MSG_TYPE = 0xE5; // de-mux to isp handler
    public static final int RESP_DATA_OPCODE = 0xa5;
    public static final int RESP_INIT_OPCODE = 0x5a;

    public static final int ACK_TO_MSG = 0xbb;
    public static final int NACK_TO_MSG = 0xbc;
    public static final int SU_STATUS_PING_MSG_TYPE = 0xE6;

    public static final int SU_RDB_MSG_TYPE = 0xc0;
    public static final int REBOOT_MSG_TYPE = 0xb3;
    public static final int SU_EVENT_MSG_TYPE = 0xb5;

    public static final int SET_DISC_MODE_MSG_TYPE = 0xd1;
    public static final int REPT_MOTION_MSG_TYPE = 0xd2;

    public static final int NODE_INFO_MSG_TYPE = 0xd4;
    public static final int MANUAL_CALIB_MSG_TYPE = 0xd5;
    public static final int SET_ABS_LIGHT_LEVEL_MSG_TYPE = 0xd6;
    public static final int SET_LIGHT_STATE_MSG_TYPE = 0xd7;
    public static final int GET_LIGHT_STATE_MSG_TYPE = 0xd8;
    public static final int REPT_PM_DATA_MSG_TYPE = 0xd9;
    public static final int GET_PM_DATA_MSG_TYPE = 0xda;
    public static final int SET_LIGHT_LEVEL_MSG_TYPE = 0xdb;
    public static final int GET_STATUS_MSG_TYPE = 0xdc;
    public static final int SET_VALIDATION_MSG_TYPE = 0xdd;
    public static final int SET_PROFILE_MSG_TYPE = 0xde;
    public static final int SET_CURRENT_TIME = 0xdf;
    public static final int SET_ACK_SU = 0xc0;
    public static final int SU_CMD_HEART_BEAT_MSG_TYPE = 0xfb;

    public static final int SET_PROFILE_ADV_MSG_TYPE = 0xf1;
    public static final int SU_DISCOVERY_TYPE = 0xf2;
    public static final int PROFILE_DOWNLOAD_MSG_TYPE = 0xf4;
    public static final int ZIGBEE_DISCOVERY_REQUEST = 0xf5;

    public static final int ENABLE_HOPPER_MSG_TYPE = 0xa6;
    public static final int DISABLE_HOPPER_MSG_TYPE = 0xa2;

    public static final int WEEK_DAY_PROFILE = 0;
    public static final int WEEK_END_PROFILE = 1;
    public static final int HOLIDAY_PROFILE = 2;

    public static final int ISP_APP1_INIT_OPCODE = 0x37;
    public static final int ISP_APP2_INIT_OPCODE = 0x38;
    public static final int ISP_DATA_OPCODE = 0x83;
    public static final int ISP_INIT_ACK_OPCODE = 0x84;
    public static final int RESEND_REQUEST = 0x87;
    public static final int ABORT_ISP_OPCODE = 0x88;
    public static final int GET_VERSION_MSG_TYPE = 0x89;

    public static final short SU_SET_WIRELESS_CMD = 0xa1;
    public static final short SU_GET_WIRELESS_CMD = 0xa2;
    public static final short SU_APPLY_WIRELESS_CMD = 0xa3;
    
    public static final int CMD_WDS_DISCOVERY_REQUEST = 0x60;
    public static final int CMD_WDS_DISCOVERY_RESPONSE = 0x61;
    public static final int CMD_SET_SWITCH_PARAMS = 0x62;
    public static final int CMD_APPLY_SWITCH_PARAMS = 0x63;
    
    // su groups commands
    public static final int SU_CMD_JOIN_GRP = 0x10;
    public static final int SU_CMD_LEAVE_GRP = 0x11;
    public static final int SU_CMD_REQ_GRP_LIST = 0x12;
    public static final int SU_CMD_REQ_REST_GRP = 0x13;
    public static final int SU_CMD_REQ_DETAIL_CONFIG_CRC_REQ = 0x14;
    public static final int SU_CMD_REQ_DETAIL_CONFIG_CRC_RESP = 0x15;

    // su switch / wds group commands
    public static final int CMD_SET_SWITCH_GRP_PARMS = 0x34;
    public static final int CMD_SET_SWITCH_GRP_WDS = 0x37;
    public static final int CMD_SWITCH_GRP_DEL_WDS = 0x3c;
    public static final int CMD_SWITCH_GRP_APPLY_ACTION = 0x3a;
    
    public static final int SU_SWITCH_GRP_BUTTON_MAP_SET_TO_AUTO = 0x0;
    public static final int SU_SWITCH_GRP_BUTTON_MAP_SET_SCENE = 0x1;
    public static final int SU_SWITCH_GRP_BUTTON_MAP_SET_DIM_UP = 0x2;
    public static final int SU_SWITCH_GRP_BUTTON_MAP_SET_DIM_DOWN = 0x3;

    // Calibration map
    public static final int CMD_LAMP_CALIBRATION_REQ = 0x40;
    public static final int CMD_LAMP_CALIBRATION_RES = 0x41;

}
