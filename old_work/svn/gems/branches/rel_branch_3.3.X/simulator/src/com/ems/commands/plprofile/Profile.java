/**
 * 
 */
package com.ems.commands.plprofile;

import java.util.logging.Logger;

import com.ems.commands.CommandsConstants;
import com.ems.db.SimPlDBHelper;
import com.ems.utils.Utils;

/**
 * @author yogesh
 * 
 */
public class Profile {
    private Logger oLogger = Logger.getLogger(Profile.class.getName());

    public static final int ADVANCE = 1;
    public static final int WEEKDAY = 2;
    public static final int WEEKEND = 3;
    public static final int HOLIDAY = 4;
    public static final int OVERRIDE2 = 5;

    public static final int MORNING = 0;
    public static final int DAY = 1;
    public static final int EVENING = 2;
    public static final int NIGHT = 3;

    private AdvanceProfileFrame oAdvanceProfile = new AdvanceProfileFrame();
    private ScheduleProfileFrame[] oWeekday = new ScheduleProfileFrame[4];
    private ScheduleProfileFrame[] oWeekend = new ScheduleProfileFrame[4];
    private ScheduleProfileFrame[] oHoliday = new ScheduleProfileFrame[4];
    private ScheduleProfileFrame[] oOverride2 = new ScheduleProfileFrame[5];

    private String sName;

    // inits with default profile values;
    public Profile(String sensorName) {
        sName = sensorName;
        setDefault();
    }

    public void setDefault() {
        ScheduleProfileFrame oProfile = null;
        // weekday
        for (int i = 0; i < 4; i++) {
            oProfile = new ScheduleProfileFrame(WEEKDAY, i);
            oWeekday[i] = oProfile;
        }
        // weekend
        for (int i = 0; i < 4; i++) {
            oProfile = new ScheduleProfileFrame(WEEKEND, i);
            oWeekend[i] = oProfile;
        }
        // holiday
        for (int i = 0; i < 4; i++) {
            oProfile = new ScheduleProfileFrame(HOLIDAY, i);
            oHoliday[i] = oProfile;
        }
        // override
        for (int i = 0; i < 4; i++) {
            oProfile = new ScheduleProfileFrame(OVERRIDE2, i);
            oOverride2[i] = oProfile;
        }
    }

    public void setScheduleProfile(byte[] profile, int iType, int idx, boolean bSave) {
        ScheduleProfileFrame oProfile = null;
        StringBuffer oBuff = new StringBuffer();
        oBuff.append(sName).append(" Schedule Profile:").append("\r\n");
        for (int i = 0; i < 4; i++) {
            switch (iType) {
            case CommandsConstants.WEEK_DAY_PROFILE:
                oProfile = oWeekday[i];
                oBuff.append("Weekday\nMOD   ACT_OCC   MAN_OVE_TIME").append("\r\n");
                break;
            case CommandsConstants.WEEK_END_PROFILE:
                oProfile = oWeekend[i];
                oBuff.append("Weekend\nMOD   ACT_OCC   MAN_OVE_TIME").append("\r\n");
                break;
            case CommandsConstants.HOLIDAY_PROFILE:
                oProfile = oHoliday[i];
                oBuff.append("Holiday\nMOD   ACT_OCC   MAN_OVE_TIME").append("\r\n");
                break;
            case CommandsConstants.OVERRIDE_PROFILE2:
                oProfile = oOverride2[i];
                oBuff.append("Override2\nMOD   ACT_OCC   MAN_OVE_TIME").append("\r\n");
                break;
            }
            oProfile.setMode(profile[idx++]);
            oBuff.append(oProfile.getMode()).append("\t");

            int motion_detection_duration = (int) Utils.extractIntFromByteArray(profile, idx);
            idx += 4;
            oProfile.setActiveMotion(motion_detection_duration);
            oBuff.append(oProfile.getActiveMotion()).append("\t");

            int manual_state_time_limit = (int) Utils.extractIntFromByteArray(profile, idx);
            idx += 4;
            oProfile.setManualOverrideTime(manual_state_time_limit);
            oBuff.append(oProfile.getManualOverrideTime()).append("\t");
        }
        if (bSave) {
            SimPlDBHelper.getInstance().updateProfile(sName, getScheduleProfile(iType), iType);
        }
        oLogger.finest(oBuff.toString());
    }

    public void setAdvanceProfile(byte[] profile, int idx, boolean bSave) {
        StringBuffer oBuff = new StringBuffer();
        oBuff.append(sName).append("\nHLEVEL").append("\r\n");
        short[] paher_start = new short[4];
        short paher = 0;
        for (int i = 0; i < 4; i++) {
            paher = (short) Utils.extractShortFromByteArray(profile, idx);
            idx += 2;
            paher_start[i] = paher;
            oBuff.append(paher).append("\t");
        }
        oAdvanceProfile.setPaher_start(paher_start);
        oAdvanceProfile.setStandaloneMotionOverride((short)Utils.extractShortFromByteArray(profile, idx));
        idx += 2;
        oAdvanceProfile.setDrReactivity(profile[idx++]);
        oAdvanceProfile.setToOffLinger(Utils.extractIntFromByteArray(profile, idx));
        idx += 4;
        oAdvanceProfile.setInitialOnLevel(profile[idx++]);
        oAdvanceProfile.setProfileGroupId(profile[idx++]);
        oAdvanceProfile.setInitialOnTime(Utils.extractIntFromByteArray(profile, idx));
        idx += 4;
        oAdvanceProfile.setDrLevels((short)Utils.extractShortFromByteArray(profile, idx));
        idx += 2;
        oAdvanceProfile.setHeartbeatInterval(Utils.extractIntFromByteArray(profile, idx));
        idx += 4;
        oAdvanceProfile.setHeartbeatLingerPeriod(Utils.extractIntFromByteArray(profile, idx));
        idx += 4;
        oAdvanceProfile.setNoOfMissedHeartbeats(Utils.extractIntFromByteArray(profile, idx));
        idx += 4;
        oAdvanceProfile.setSafetyMode(Utils.extractIntFromByteArray(profile, idx));
        idx += 4;
        oAdvanceProfile.setHolidayLevel(profile[idx++]);
        oBuff.append(oAdvanceProfile.getHolidayLevel()).append("\t");

        oBuff.append("\r\n");
        if (bSave) {
            SimPlDBHelper.getInstance().updateProfile(sName, getAdvanceProfile(), ADVANCE);
        }
        oLogger.finest(oBuff.toString());
    }

    public byte getDefaultScheduleProfileChecksum() {
        byte[] s_profile = new byte[18 * 4 * 3];
        int idx = 0;
        for (int i = 0; i < 4; i++) {
            byte[] prf = oWeekday[i].toByte("");
            System.arraycopy(prf, 0, s_profile, idx, prf.length);
            idx += prf.length;
        }
        for (int i = 0; i < 4; i++) {
            byte[] prf = oWeekend[i].toByte("");
            System.arraycopy(prf, 0, s_profile, idx, prf.length);
            idx += prf.length;
        }
        for (int i = 0; i < 4; i++) {
            byte[] prf = oHoliday[i].toByte("");
            System.arraycopy(prf, 0, s_profile, idx, prf.length);
            idx += prf.length;
        }
        byte s_profile_cksum = Utils.computeChecksum(s_profile);
        oLogger.finest(sName + " (Schedule Profile) - (" + s_profile.length + ", " + s_profile_cksum + ") "
                + Utils.getPacket(s_profile));
        // System.out.println(sName + " (Schedule Profile) - (" + s_profile.length + ") " + Utils.getPacket(s_profile));
        return s_profile_cksum;
    }

    public byte getDefaultAdvanceProfileChecksum() {
        byte[] g_profile = oAdvanceProfile.toByte("");
        byte g_profile_cksum = Utils.computeChecksum(g_profile);
        oLogger.finest(sName + " (Advance Profile) - (" + g_profile.length + ", " + g_profile_cksum + ") "
                + Utils.getPacket(g_profile));
        // System.out.println(sName + " (Advance Profile) - (" + g_profile.length + ") " + Utils.getPacket(g_profile));
        return g_profile_cksum;
    }

    public byte[] getScheduleProfile(int profileType) {
        int idx = 0;
        byte[] s_profile = new byte[18 * 4];
        byte[] prf = null;
        if (profileType == 2) {
            for (int i = 0; i < 4; i++) {
                prf = oWeekday[i].toByte("");
                System.arraycopy(prf, 0, s_profile, idx, prf.length);
                idx += prf.length;
            }
        } else if (profileType == 3) {
            for (int i = 0; i < 4; i++) {
                prf = oWeekend[i].toByte("");
                System.arraycopy(prf, 0, s_profile, idx, prf.length);
                idx += prf.length;
            }
        } else if (profileType == 4) {
            for (int i = 0; i < 4; i++) {
                prf = oHoliday[i].toByte("");
                System.arraycopy(prf, 0, s_profile, idx, prf.length);
                idx += prf.length;
            }
        }
        return s_profile;
    }

    public byte[] getAdvanceProfile() {
        byte[] g_profile = oAdvanceProfile.toByte("");
        oLogger.finest(sName + " (Advance Profile) - (" + g_profile.length + ") " + Utils.getPacket(g_profile));
        return g_profile;
    }

    private int getDayFromEpoc(long ctime) {
        ctime = ctime / 1000;
        ctime /= 60;
        ctime /= 60;
        ctime /= 24;
        return (1 << (ctime + 3) % 7);
    }
}
