/**
 * 
 */
package com.ems.commands.profile;

import java.util.Calendar;
import java.util.logging.Logger;

import com.ems.commands.CommandsConstants;
import com.ems.db.SimDBHelper;
import com.ems.profile.ProfileConstants;
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

    public static final int MORNING = 0;
    public static final int DAY = 1;
    public static final int EVENING = 2;
    public static final int NIGHT = 3;

    private AdvanceProfileFrame oAdvanceProfile = new AdvanceProfileFrame();
    private ScheduleProfileFrame[] oWeekday = new ScheduleProfileFrame[4];
    private ScheduleProfileFrame[] oWeekend = new ScheduleProfileFrame[4];
    private ScheduleProfileFrame[] oHoliday = new ScheduleProfileFrame[4];

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
    }

    public void setScheduleProfile(byte[] profile, int iType, int idx, boolean bSave) {
        ScheduleProfileFrame oProfile = null;
        StringBuffer oBuff = new StringBuffer();
        oBuff.append(sName).append(" Schedule Profile:").append("\r\n");
        for (int i = 0; i < 4; i++) {
            switch (iType) {
            case CommandsConstants.WEEK_DAY_PROFILE:
                oProfile = oWeekday[i];
                oBuff.append("Weekday\nMIN_L   MAX_L   OCC_T   MOD_T   OCC_S   RMP     AMB_S").append("\r\n");
                break;
            case CommandsConstants.WEEK_END_PROFILE:
                oProfile = oWeekend[i];
                oBuff.append("Weekend\nMIN_L   MAX_L   OCC_T   MOD_T   OCC_S   RMP     AMB_S").append("\r\n");
                break;
            case CommandsConstants.HOLIDAY_PROFILE:
                oProfile = oHoliday[i];
                oBuff.append("Holiday\nMIN_L   MAX_L   OCC_T   MOD_T   OCC_S   RMP     AMB_S").append("\r\n");
                break;
            }
            oProfile.setMin_light_level(profile[idx++]);
            oBuff.append(oProfile.getMin_light_level()).append("\t");

            oProfile.setMax_light_level(profile[idx++]);
            oBuff.append(oProfile.getMax_light_level()).append("\t");
            ;

            short motion_detection_duration = (short) Utils.extractShortFromByteArray(profile, idx);
            idx += 2;
            oProfile.setMotion_detection_duration(motion_detection_duration);
            oBuff.append(oProfile.getMotion_detection_duration()).append("\t");
            ;

            short manual_state_time_limit = (short) Utils.extractShortFromByteArray(profile, idx);
            idx += 2;
            oProfile.setManual_state_time_limit(manual_state_time_limit);
            oBuff.append(oProfile.getManual_state_time_limit()).append("\t");
            ;

            oProfile.setOcc_sensitivity(profile[idx++]);
            oBuff.append(oProfile.getOcc_sensitivity()).append("\t");
            ;

            oProfile.setRamp_time(profile[idx++]);
            oBuff.append(oProfile.getRamp_time()).append("\t");
            ;

            oProfile.setAmbient_light_sensitivity(profile[idx]);
            oBuff.append(oProfile.getAmbient_light_sensitivity()).append("\t");
            oBuff.append("\r\n");
            idx += 10;
        }
        if (bSave) {
            SimDBHelper.getInstance().updateProfile(sName, getScheduleProfile(iType), iType);
        }
        oLogger.finest(oBuff.toString());
    }

    public void setAdvanceProfile(byte[] profile, int idx, boolean bSave) {
        StringBuffer oBuff = new StringBuffer();
        oBuff.append(sName)
                .append("\nMT\tDT\tET\tNT\tDL\tNL\tEL\tDP\tRP\tRC\tBK\tNT\tLO\tSAMO\tDRr\tLOF\tWDB\tIOL\tPGID")
                .append("\r\n");
        short[] paher_start = new short[4];
        short paher = 0;
        for (int i = 0; i < 4; i++) {
            paher = (short) Utils.extractShortFromByteArray(profile, idx);
            idx += 2;
            paher_start[i] = paher;
            oBuff.append(paher).append("\t");
        }
        oAdvanceProfile.setPaher_start(paher_start);

        short dark_intensity = (short) Utils.extractShortFromByteArray(profile, idx);
        idx += 2;
        oAdvanceProfile.setDark_intensity(dark_intensity);
        oBuff.append(oAdvanceProfile.getDark_intensity()).append("\t");

        short neighbor_intensity = (short) Utils.extractShortFromByteArray(profile, idx);
        idx += 2;
        oAdvanceProfile.setNeighbor_intensity(neighbor_intensity);
        oBuff.append(oAdvanceProfile.getNeighbor_intensity()).append("\t");

        short envelop_on_intensity = (short) Utils.extractShortFromByteArray(profile, idx);
        idx += 2;
        oAdvanceProfile.setEnvelop_on_intensity(envelop_on_intensity);
        oBuff.append(oAdvanceProfile.getEnvelop_on_intensity()).append("\t");

        oAdvanceProfile.setAcceptable_drop_percentage(profile[idx++]);
        oBuff.append(oAdvanceProfile.getAcceptable_drop_percentage()).append("\t");

        oAdvanceProfile.setAcceptable_rise_percentage(profile[idx++]);
        oBuff.append(oAdvanceProfile.getAcceptable_rise_percentage()).append("\t");

        oAdvanceProfile.setRelays_connected(profile[idx++]);
        oBuff.append(oAdvanceProfile.getRelays_connected()).append("\t");

        oAdvanceProfile.setDim_back_off_mins(profile[idx++]);
        oBuff.append(oAdvanceProfile.getDim_back_off_mins()).append("\t");

        short intensity_norm_time_limit = (short) Utils.extractShortFromByteArray(profile, idx);
        idx += 2;
        oAdvanceProfile.setIntensity_norm_time_limit(intensity_norm_time_limit);
        oBuff.append(oAdvanceProfile.getIntensity_norm_time_limit()).append("\t");

        oAdvanceProfile.setMinimum_level_before_off(profile[idx++]);
        oBuff.append(oAdvanceProfile.getMinimum_level_before_off()).append("\t");

        oAdvanceProfile.setStand_alone_motion_override(profile[idx++]);
        oBuff.append(oAdvanceProfile.getStand_alone_motion_override()).append("\t");

        oAdvanceProfile.setDR_reactivity(profile[idx++]);
        oBuff.append(oAdvanceProfile.getDR_reactivity()).append("\t");

        short to_off_linger = (short) Utils.extractShortFromByteArray(profile, idx);
        idx += 2;
        oAdvanceProfile.setTo_off_linger(to_off_linger);
        oBuff.append(oAdvanceProfile.getTo_off_linger()).append("\t");

        oAdvanceProfile.setWeekDayBits(profile[idx++]);
        oBuff.append(oAdvanceProfile.getWeekDayBits()).append("\t");

        oAdvanceProfile.setInitialOnLevel(profile[idx++]);
        oBuff.append(oAdvanceProfile.getInitialOnLevel()).append("\t");

        oAdvanceProfile.setProfile_group_id(profile[idx++]);
        oBuff.append(oAdvanceProfile.getProfile_group_id()).append("\t");

        oAdvanceProfile.setFlags(profile[idx++]);
        oBuff.append(oAdvanceProfile.getFlags()).append("\t");

        byte[] initial_on_time = { profile[idx++], profile[idx++] };
        oAdvanceProfile.setInitial_on_time(initial_on_time);
        oBuff.append(initial_on_time[0]).append(initial_on_time[1]).append("\t");

        oAdvanceProfile.setTheBayType(profile[idx++]);
        oBuff.append(oAdvanceProfile.getTheBayType()).append("\t");

        short motionThresholdGain = (short) Utils.extractShortFromByteArray(profile, idx);
        idx += 2;
        oAdvanceProfile.setMotionThresholdGain(motionThresholdGain);
        oBuff.append(oAdvanceProfile.getMotionThresholdGain()).append("\t");
        oBuff.append("\r\n");
        if (bSave) {
            SimDBHelper.getInstance().updateProfile(sName, getAdvanceProfile(), ADVANCE);
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

    public byte getProfileNo() {
        return oAdvanceProfile.getProfile_group_id();
    }

    public int getMaxLightLevel() {
        Calendar oCal = Calendar.getInstance();
        int minutesOfDay = oCal.get(Calendar.HOUR_OF_DAY) * 60 + oCal.get(Calendar.MINUTE);
        short[] paher_times = oAdvanceProfile.getPaher_start();
        if (paher_times != null && paher_times.length == 4) {
            if (getDayFromEpoc(oCal.getTimeInMillis() & oAdvanceProfile.getWeekDayBits()) > 0) {
                // Weekday
                if (minutesOfDay >= paher_times[0] && minutesOfDay < paher_times[1]) {
                    // morning
                    return oWeekday[0].getMax_light_level();
                }else if (minutesOfDay >= paher_times[1] && minutesOfDay < paher_times[2]) {
                    // day
                    return oWeekday[1].getMax_light_level();
                }else if (minutesOfDay >= paher_times[2] && minutesOfDay < paher_times[3]) {
                    // evening
                    return oWeekday[2].getMax_light_level();
                }else if (minutesOfDay >= paher_times[3]) {
                    // night
                    return oWeekday[3].getMin_light_level();
                }
            }else {
                // Weekend
                if (minutesOfDay >= paher_times[0] && minutesOfDay < paher_times[1]) {
                    // morning
                    return oWeekend[0].getMin_light_level();
                }else if (minutesOfDay >= paher_times[1] && minutesOfDay < paher_times[2]) {
                    // day
                    return oWeekend[1].getMin_light_level();
                }else if (minutesOfDay >= paher_times[2] && minutesOfDay < paher_times[3]) {
                    // evening
                    return oWeekend[2].getMin_light_level();
                }else if (minutesOfDay >= paher_times[3]) {
                    // night
                    return oWeekend[3].getMin_light_level();
                }
            }
        }
        return (int)ProfileConstants.MAX_LEVEL;
    }

    private int getDayFromEpoc(long ctime) {
        ctime = ctime / 1000;
        ctime /= 60;
        ctime /= 60;
        ctime /= 24;
        return (1 << (ctime + 3) % 7);
    }
}
