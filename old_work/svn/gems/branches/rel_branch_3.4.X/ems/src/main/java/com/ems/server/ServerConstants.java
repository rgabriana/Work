package com.ems.server;

public class ServerConstants {

    public static final int FRAME_START_MARKER = 0x57;
    public static final int FRAME_NEW_START_MARKER = 0x58;
    public static final int FRAME_END_MARKER = 0x5e;

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
    public static final int GET_AMBIENT_CALIB = 0;
    public static final int SET_AMBIENT_CALIB = 1;
    public static final int CURR_AMBIENT_LEVEL_CALIB = 2;
    public static final int SET_ABS_LIGHT_LEVEL_MSG_TYPE = 0xd6;
    public static final int SET_LIGHT_STATE_MSG_TYPE = 0xd7;
    public static final int PL_REPT_PM_DATA_MSG_TYPE = 0xd8;
    public static final int REPT_PM_DATA_MSG_TYPE = 0xd9;
    //public static final int GET_PM_DATA_MSG_TYPE = 0xda;
    public static final int SU_TRIGGER_MOTION_BIT_MSG_TYPE = 0xda;
    public static final int SET_LIGHT_LEVEL_MSG_TYPE = 0xdb;
    public static final int GET_STATUS_MSG_TYPE = 0xdc;
    public static final int SET_VALIDATION_MSG_TYPE = 0xdd;
    public static final int SET_PROFILE_MSG_TYPE = 0xde;
    public static final int SET_CURRENT_TIME = 0xdf;
    public static final int SET_ACK_SU = 0xc0;

    public static final int CMD_SET_UTC_TIME = 0xe0;

    public static final int SET_PROFILE_ADV_MSG_TYPE = 0xf1;
    public static final int SU_DISCOVERY_TYPE = 0xf2;
    public static final int PROFILE_DOWNLOAD_MSG_TYPE = 0xf4;
    public static final int ZIGBEE_DISCOVERY_REQUEST = 0xf5;

    public static final int CMD_SWITCH_DISCOVER = 0x60;
    public static final int CMD_SWITCH_DISCOVER_RESPONSE = 0x61;
    public static final int CMD_SET_SWITCH_PARAMS = 0x62;
    public static final int CMD_APPLY_SWITCH_PARAMS = 0x63;
    
    public static final int CMD_PLUGLOAD_DISCOVER = 0xf5;
    public static final int CMD_DISCOVERY_RESPONSE = 0xf2;

    public static final int ENABLE_HOPPER_MSG_TYPE = 0xa6;
    public static final int DISABLE_HOPPER_MSG_TYPE = 0xa2;
    
    public static final int SU_CMD_HEART_BEAAT_MSG_TYPE = 0xfb;
    public static final int SU_CMD_CONFIG_HEART_BEAT_MSG_TYPE = 0xfc;
    
    public static final int SU_CMD_HB_STATS_MSG_TYPE = 0xa9;
    public static final int SU_CMD_HB_CONFIG_MSG_TYPE = 0xa8;

    public static final int WEEK_DAY_PROFILE = 0;
    public static final int WEEK_END_PROFILE = 1;
    public static final int HOLIDAY_PROFILE = 2;
    public static final int OVERRIDE_PROFILE = 3;

    public static final int ISP_APP1_INIT_OPCODE = 0x37;
    public static final int ISP_APP2_INIT_OPCODE = 0x38;
    public static final int ISP_DATA_OPCODE = 0x83;
    public static final int ISP_INIT_ACK_OPCODE = 0x84;
    public static final int RESEND_REQUEST = 0x87;
    public static final int ABORT_ISP_OPCODE = 0x88;
    public static final int GET_VERSION_MSG_TYPE = 0x89;
    
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

    // su tx/rx motion group commands
    public static final int CMD_MOTION_GRP_APPLY_ACTION = 0x3b;

    public static final int CMD_LAMP_CALIBRATION_REQ = 0x40;
    public static final int CMD_LAMP_CALIBRATION_RES = 0x41;

    public static final int SU_SWITCH_GRP_BUTTON_MAP_SET_TO_AUTO = 0x0;
    public static final int SU_SWITCH_GRP_BUTTON_MAP_SET_SCENE = 0x1;
    public static final int SU_SWITCH_GRP_BUTTON_MAP_SET_DIM_UP = 0x2;
    public static final int SU_SWITCH_GRP_BUTTON_MAP_SET_DIM_DOWN = 0x3;

    public static final int MOTION_GRP_START_NO = 10001;
    public static final int MOTION_BITS_GRP_START_NO = 40001;
    public static final int SWTICH_GRP_START_NO = 30001;
    
    public static final int AUTO_STATE_ENUM = 101;
    public static final int BASELINE_STATE_ENUM = 102;
    public static final int BYPASS_STATE_ENUM = 103;

    public static final int RESET_REASON_POR = 0;
    public static final int RESET_REASON_EXTR = 1;
    public static final int RESET_REASON_WATR = 2;
    public static final int RESET_REASON_BR = 3;

    public static final int STATS_SAVING_NONE = 0;
    public static final int STATS_SAVING_OCC = 1;
    public static final int STATS_SAVING_AMB = 2;
    public static final int STATS_SAVING_TUNEUP = 3;
    public static final int STATS_SAVING_MANUAL = 4;

    public static final int COMM_TYPE_ZIGBEE = 1;
    public static final int COMM_TYPE_PLC = 2;
    
    public static final int FX_TYPE_DEFAULT = 0;
    public static final int FX_TYPE_EMERGENCY = 1;

    public static final int BALLAST_TYPE_DEFAULT = 0;
    public static final int BALLAST_TYPE_TOMCAT = 1;

    public static final int PROFILE_VER_OPCODE = 0;
    public static final int PROFILE_MIN_LIGHT_LEVEL_OPCODE = 1;
    public static final int PROFILE_MAX_LIGHT_LEVEL_OPCODE = 2;
    public static final int PROFILE_OCC_TIME_OPCODE = 3;
    public static final int PROFILE_OCC_SENS_OPCODE = 5;
    public static final int PROFILE_RAMP_TIME_OPCODE = 6;
    public static final int PROFILE_AMB_SENS_OPCODE = 7;

    public static final int PM_STATS_FIRMWARE_MODE = 1;
    public static final int PM_STATS_GEMS_MODE = 2;

    // packet header structure
    public static final int CMD_PKT_START_MARKER_POS = 0;
    public static final int CMD_PKT_VER_POS = 1;
    public static final int CMD_PKT_LEN_POS = 2;
    public static final int CMD_PKT_TX_ID_POS = 4;
    public static final int CMD_PKT_NO_OF_TARGET_POS = 9;
    public static final int CMD_PKT_TARGETS_POS = 10;

    // response packet header structure
    public static final int RES_CMD_PKT_NODE_ID = 8;
    public static final int RES_CMD_PKT_MSG_TYPE_POS = 11;
    public static final int RES_CMD_PKT_MSG_START_POS = 12;
    public static final int RES_ACK_CMD_PKT_SEQNO_POS = 13;

    // current state constants
    public static final int CURR_STATE_UNKNOWN = 0;
    public static final int CURR_STATE_INIT = 1;
    public static final int CURR_STATE_OCC_ON = 2;
    public static final int CURR_STATE_OCC_OFF = 3;
    public static final int CURR_STATE_AMB_ON = 4;
    public static final int CURR_STATE_NORMAL_HIGH = 5;
    public static final int CURR_STATE_NORMAL_LOW = 6;
    public static final int CURR_STATE_MANUAL = 7;
    public static final int CURR_STATE_VALIDATION = 8;
    public static final int CURR_STATE_DISC = 9;
    public static final int CURR_STATE_BASELINE = 10;
    public static final int CURR_STATE_GOTOAMBHI = 11;
    public static final int CURR_STATE_GOTOAMBLO = 12;
    public static final int CURR_STATE_PWRCURVE = 13;
    public static final int CURR_PL_STATE_TO_SAFETY = 15;
    public static final int CURR_PL_STATE_SAFETY = 16;
    
    public static final int CURR_STATE_BYPASS = 100;

    public static final String CURR_STATE_UNKNOWN_STR = "Unknown";
    public static final String CURR_STATE_AUTO_STR = "Auto";
    public static final String CURR_STATE_MANUAL_STR = "Manual Override";
    public static final String CURR_STATE_BASELINE_STR = "Baseline";
    public static final String CURR_STATE_DISABLED_STR = "Disabled";
    public static final String CURR_STATE_SAFETY_STR = "Safety";

    public static final int ZIGBEE_GW_E10 = 1;
    public static final int ZIGBEE_GW_USB = 2;
    public static final int ZIGBEE_GW_SU = 3;
    public static final short UEM_GW = 5;

    public static final String CUSTOM_PROFILE = "Custom";
    public static final String DEFAULT_PROFILE = "Default";
    public static final String OUTDOOR_PROFILE = "Outdoor";
    
    public static final String DEFAULT_PLUGLOAD_PROFILE = "Default";

    public static final int VAL_DETECT_MOTION_TYPE = 1;
    public static final int VAL_DETECT_LIGHT_TYPE = 2;
    public static final int VAL_DETECT_MOTION_LIGHT_TYPE = 3;

    public static final int EVENT_BYPASS = 1;
    public static final int EVENT_BAD_PROFILE_ROM = 2;
    public static final int EVENT_BAD_PROFILE_RAM = 3;
    public static final int EVENT_CU_COMM_FAILURE = 4;
    public static final int REBOOT_WITH_WATCHDOG = 5;
    public static final int BAD_MD5SUM = 6;
    public static final int EVENT_DIMMING = 7;
    public static final int EVENT_HARDWARE_FAILURE = 8;
    public static final int EVENT_TOO_HOT = 9;
    public static final int EVENT_CPU_TOO_HIGH = 10;

    public static final short POWER_CALCULATED_GEMS = 1;
    public static final short POWER_CALCULATED_SU = 2;

    // gateway commands
    public static final short GATEWAY_SET_ADDR = 0x0;
    public static final short GATEWAY_CMD_STATUS = 0x1;
    public static final short GATEWAY_CMD_INFO = 0x2;
    public static final short GATEWAY_INFO_RESP = 0x3;
    public static final short GATEWAY_WIRELESS_CMD = 0x4;
    public static final short GATEWAY_WIRELESS_RESP = 0x5;
    public static final short GATEWAY_START_SCAN = 0x6;
    public static final short GATEWAY_SCAN_RESP = 0x7;
    public static final short GATEWAY_REBOOT_CMD = 0xa;
    public static final short GATEWAY_SYSTEM_REBOOT_CMD = 0xb;
    public static final short GATEWAY_FILE_XFER = 0x0b;
    public static final short GATEWAY_PGM_RADIO = 0x0c;
    public static final short GATEWAY_FILE_XFER_STATUS = 0x0d;
    public static final short GATEWAY_PMG_RADIO_STATUS = 0x0e;
    public static final short GATEWAY_ACK = 0xbd;
    public static final short GATEWAY_AUTH_CMD = 0xf;
    public static final short GATEWAY_UPGRADE_CMD = 0xab;
    public static final short GATEWAY_UPGRADE_STATUS = 0xad;
    public static final short GATEWAY_UPGRADE_FILE_ACK = 0xae;
    public static final Short GATEWAY_SECURITY_CMD = 0xaf;
    public static final Short UEM_FORWARD_CMD = 0xb0;

    public static final short SU_SET_WIRELESS_CMD = 0xa1;
    public static final short SU_GET_WIRELESS_CMD = 0xa2;
    public static final short SU_APPLY_WIRELESS_CMD = 0xa3;
    public static final short SU_SET_APPLY_WIRELESS_CMD = 0xa4;

    public static final short GET_MANUF_INFO_REQ = 0x65;
    public static final short GET_MANUF_INFO_RSP = 0x66;

    // gateway header positions
    public static final short GW_HEADER_MAGIC_POS = 0;
    public static final short GW_HEADER_LEN_POS = 2;
    public static final short GW_HEADER_FLAGS_POS = 4;
    public static final short GW_HEADER_HASH_POS = 5;

    // gateway cmd packet positions
    public static final short GATEWAY_CMD_MSG_POS = 0;
    public static final short GATEWAY_CMD_SEQ_NO_POS = 1;

    public static final byte GW_MAGIC_FIRST = 'e';
    public static final byte GW_MAGIC_SECOND = 's';

    public static final int GW_UDP_PORT = 8085;

    public static final String FIXTURE_STATE_DISCOVER_STR = "DISCOVERED";
    public static final String FIXTURE_STATE_COMMISSIONED_STR = "COMMISSIONED";
    public static final String FIXTURE_STATE_PLACED_STR = "PLACED";
    public static final String FIXTURE_STATE_VALIDATED_STR = "VALIDATED";
    public static final String FIXTURE_STATE_DELETED_STR = "DELETED";

    public static final int DEVICE_FIXTURE = 0;
    public static final int DEVICE_GATEWAY = 1;
    public static final int DEVICE_SWITCH = 2;
    public static final byte DEVICE_PLUGLOAD = 0xa;
    
    public static final int DR_AUDIT = 2;

    public static final int ETH_SEC_TYPE = 0;
    public static final int ETH_SEC_ENCRYPT_TYPE = 0;
    public static final String DEF_WLESS_SECURITY_KEY = "enLightedWorkNow";
    public static final String GW_DEF_WLESS_KEY_DISP_STR = "Default is in use";
    public static final int DEFAUL_NETWORK_ID = 26708;
    public static final int WIRELESS_DEF_RADIO_RATE = 2;
    public static final int WIRELESS_DEF_CHANNEL = 4;

    public static final String CMD_STATUS_SUCCESS = "SUCCESS";
    public static final String CMD_STATUS_PENDING = "PENDING";
    public static final String CMD_STATUS_FAIL = "FAIL";

    public static final String SET_LIGHT_LEVEL_STR = "Relative Dim";
    public static final String SET_ABS_LIGHT_LEVEL_STR = "Absolute Dim";
    public static final String SET_PROFILE_ADV_STR = "Advanced Profile";
    public static final String SET_PROFILE_STR = "Schedued Profile";
    public static final String SET_VALIDATION_STR = "Commission";
    public static final String SET_LIGHT_STATE_STR = "Fixture State";
    public static final String SET_DISC_MODE_MSG_STR = "Discovery";
    public static final String SU_APPLY_WIRELESS_STR = "Apply Wireless";
    public static final String SU_SET_WIRELESS_STR = "Change Wireless";
    public static final String SU_REBOOT_STR = "Reboot";
    public static final String SU_MANUAL_CALIB_STR = "Manual Calibration";

    public static final String DEFAULT_PROFILE_V2 = "Default Profile v2";

    public static final int DEFAULT_PROFILE_GID = 1;
    
    public static final int DEFAULT_PLUGLOAD_PROFILE_GID = 1;

    public static final int COMMISSION_STATUS_UNKNOWN = 0;
    public static final int COMMISSION_STATUS_COMMUNICATION = 2;
    public static final int COMMISSION_STATUS_MOTION = 4;
    public static final int COMMISSION_STATUS_DIMMING = 8;
    public static final int COMMISSION_STATUS_ALL = 14;
    public static final int COMMISSION_STATUS_WIRELESS = 16;

    public static final int TOP_TO_BOTTOM = 0;
    public static final int BOTTOM_TO_TOP = 1;
    public static final int LEFT_TO_RIGHT = 2;
    public static final int RIGHT_TO_LEFT = 3;

    public static final String AUDIT_SCHEDULED_STATUS = "Scheduled";
    public static final String AUDIT_INPROGRESS_STATUS = "In progress";
    public static final String AUDIT_SUCCESS_STATUS = "Success";
    public static final String AUDIT_FAIL_STATUS = "Failed";

    // image upgrade constants
    public static final String IMG_UP_STATUS_SCHEDULED = "Scheduled";
    public static final String IMG_UP_STATUS_INPROGRESS = "In Progress";
    public static final String IMG_UP_STATUS_SUCCESS = "Success";
    public static final String IMG_UP_STATUS_FAIL = "Fail";
    public static final String IMG_UP_STATUS_PARTIAL = "Partial";
    public static final String IMG_UP_STATUS_NOT_PENDING = "Not Pending";
    public static final String IMG_UP_STATUS_ABORTED = "Aborted";

    public static final String DR_STATUS_SCHEDULED = "Scheduled";
    public static final String DR_STATUS_ACTIVE = "Active";
    public static final String DR_STATUS_FINISHED = "Finished";
    
    public static final String hopperDefErrStr = "Hopper didn't move to default wireless parameters";
    public static final String hopperConfErrStr = "Hopper didn't move to configured wireless parameters";
    
    public static final String WDS_STATE_DISCOVER_STR = "DISCOVERED";
    public static final String WDS_STATE_COMMISSIONED_STR = "COMMISSIONED";
    public static final String WDS_STATE_DELETED_STR = "DELETED";
    
    public static final String PLUGLOAD_STATE_DISCOVER_STR = "DISCOVERED";
    public static final String PLUGLOAD_STATE_COMMISSIONED_STR = "COMMISSIONED";
    public static final String PLUGLOAD_STATE_DELETED_STR = "DELETED";
    public static final String PLUGLOAD_STATE_PLACED_STR = "PLACED";
    
    public static final int WDS_STATE_NOT_ASSOCIATED = 0;
    public static final int WDS_STATE_ASSOCIATED = 1;

    public static final String SWITCH_TYPE_WDS = "Real";
    
    //group sync config types enumeration
    public static final int GROUP_SYNC_CONFIG_TYPE_GLOBAL_PR = 1;
    public static final int GROUP_SYNC_CONFIG_TYPE_WEEKDAY_PR = 2;
    public static final int GROUP_SYNC_CONFIG_TYPE_WEEKEND_PR = 3;
    public static final int GROUP_SYNC_CONFIG_TYPE_HOLIDAY_PR = 4;
    public static final int GROUP_SYNC_CONFIG_TYPE_MOTION_BITS = 5;
    public static final int GROUP_SYNC_CONFIG_TYPE_GROUPS = 6;
    public static final int GROUP_SYNC_CONFIG_TYPE_GROUPS_OVERRIDE_PROFILE = 7;

    // Ack or Nack from SU
    public static final int SU_ACK = 1;
    public static final int SU_NACK = 2;

    public static final int DIM_TYPE_RELATIVE = 1;
    public static final int DIM_TYPE_ABSOLUTE = 2;
    public static final int WDS_BATTERY_LEVEL_MSG = 0x20;
    
    //SU motion report trigger type enums
    public static final int MOT_RPT_1_MIN = 0;
    public static final int MOT_RPT_VAC_TO_OCC = 1;
    public static final int MOT_RPT_OCC_TO_VAC = 2;
    public static final int MOT_RPT_LIGHTS_ON = 3;
    
} // end of class ServerConstants
