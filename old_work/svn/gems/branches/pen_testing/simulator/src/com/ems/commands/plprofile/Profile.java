/**
 * 
 */
package com.ems.commands.plprofile;

import java.util.Calendar;
import java.util.logging.Logger;

import com.ems.commands.CommandsConstants;
import com.ems.db.SimPlDBHelper;
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
        byte[][] profileArrays = SimPlDBHelper.getInstance().fetchRecords(sName);
        if (profileArrays != null) {
            byte weekday[] = profileArrays[0];
            for (int i = 0; i < 4; i++) {
                oProfile = new ScheduleProfileFrame(weekday, WEEKDAY, i);
                oWeekday[i] = oProfile;
            }
            // weekend
            byte weekend[] = profileArrays[1];
            for (int i = 0; i < 4; i++) {
                oProfile = new ScheduleProfileFrame(weekend, WEEKEND, i);
                oWeekend[i] = oProfile;
            }
            // holiday
            byte holiday[] = profileArrays[2];

            for (int i = 0; i < 4; i++) {
                oProfile = new ScheduleProfileFrame(holiday, HOLIDAY, i);
                oHoliday[i] = oProfile;
            }
            // override
            byte override[] = profileArrays[3];

            for (int i = 0; i < 4; i++) {
                oProfile = new ScheduleProfileFrame(override, OVERRIDE2, i);
                oOverride2[i] = oProfile;
            }

            byte advanceProfile[] = profileArrays[4];
            oAdvanceProfile = new AdvanceProfileFrame();
            short morningTime = (short) Utils.extractShortFromByteArray(advanceProfile, 0);
            short dayTime = (short) Utils.extractShortFromByteArray(advanceProfile, 2);
            short eveningTime = (short) Utils.extractShortFromByteArray(advanceProfile, 4);
            short nightTime = (short) Utils.extractShortFromByteArray(advanceProfile, 6);
            short[] paher_start = new short[] { morningTime, dayTime, eveningTime, nightTime };
            oAdvanceProfile.setPaher_start(paher_start);

        } else {

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

    }
    
    public byte getProfileNo() {
        return (byte) oAdvanceProfile.getProfileGroupId();
    }

    public void setScheduleProfile(byte[] profile, int iType, int idx, boolean bSave) {

        ScheduleProfileFrame oProfile = null;
        StringBuffer oBuff = new StringBuffer();
        oBuff.append(sName).append(" Schedule Profile:").append("\r\n");
        for (int i = 0; i < 4; i++) {
            switch (iType) {
            case CommandsConstants.WEEK_DAY_PROFILE:
                oProfile = oWeekday[i];
                setScheduleProfileValues(profile, idx, oWeekday[i]);
                oBuff.append("Weekday\nMOD   ACT_OCC   MAN_OVE_TIME").append("\r\n");
                break;
            case CommandsConstants.WEEK_END_PROFILE:
                oProfile = oWeekend[i];
                setScheduleProfileValues(profile, idx, oWeekend[i]);
                oBuff.append("Weekend\nMOD   ACT_OCC   MAN_OVE_TIME").append("\r\n");
                break;
            case CommandsConstants.HOLIDAY_PROFILE:
                oProfile = oHoliday[i];
                setScheduleProfileValues(profile, idx, oHoliday[i]);
                oBuff.append("Holiday\nMOD   ACT_OCC   MAN_OVE_TIME").append("\r\n");
                break;
            case CommandsConstants.OVERRIDE_PROFILE2:
                oProfile = oOverride2[i];
                setScheduleProfileValues(profile, idx, oOverride2[i]);
                oBuff.append("Override2\nMOD   ACT_OCC   MAN_OVE_TIME").append("\r\n");
                break;
            }

            byte[] pkt = profile;
            int i1 = 3;

            int min = pkt[idx++];
            int max = pkt[idx++];

            byte[] occ = new byte[2];
            occ[0] = pkt[idx++];
            occ[1] = pkt[idx++];

            byte[] iot = new byte[2];
            iot[0] = pkt[idx++];
            iot[1] = pkt[idx++];

            byte mode = profile[idx++];

            int rampUpTime = profile[idx++];

            idx += 10;
            oProfile.setMode(mode);
            oBuff.append(oProfile.getMode()).append("\t");

            oProfile.setActiveMotion(Utils.byteArrayToShort(occ));
            oBuff.append(oProfile.getActiveMotion()).append("\t");

            oProfile.setManualOverrideTime(Utils.byteArrayToShort(iot));
            oBuff.append(oProfile.getManualOverrideTime()).append("\t");
            oBuff.append("\r\n");

            SimPlDBHelper.getInstance().updateProfile(sName, getScheduleProfile(iType), iType);
        }

        oLogger.finest("plugload schedule profile is " + oBuff.toString());
    }

    public void setScheduleProfileValues(byte[] profile, int idx, ScheduleProfileFrame oProfile) {

        byte[] pkt = profile;
        int min = pkt[idx++];
        int max = pkt[idx++];
        byte[] occ = new byte[2];
        occ[0] = pkt[idx++];
        occ[1] = pkt[idx++];
        byte[] iot = new byte[2];
        iot[0] = pkt[idx++];
        iot[1] = pkt[idx++];
        byte mode = profile[idx++];
        int rampUpTime = profile[idx++];
        idx += 10;

        oProfile.setMode(mode);
        oProfile.setActiveMotion(Utils.byteArrayToShort(occ));
        oProfile.setManualOverrideTime(Utils.byteArrayToShort(iot));

    }

    public void setGlobalProfileValues(byte[] profile, int idx, GlobalProfileFrame oProfile) {

        byte[] pkt = profile;

        byte[] morning = new byte[2];
        morning[0] = pkt[idx++];
        morning[1] = pkt[idx++];
        byte[] day = new byte[2];
        day[0] = pkt[idx++];
        day[1] = pkt[idx++];
        byte[] evening = new byte[2];
        evening[0] = pkt[idx++];
        evening[1] = pkt[idx++];
        byte[] night = new byte[2];
        night[0] = pkt[idx++];
        night[1] = pkt[idx++];

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
        this.oAdvanceProfile.setPaher_start(paher_start);
        oAdvanceProfile.setStandaloneMotionOverride((short) Utils.extractShortFromByteArray(profile, idx));
        idx += 2;
        oAdvanceProfile.setDrReactivity(profile[idx++]);
        oAdvanceProfile.setToOffLinger(Utils.extractIntFromByteArray(profile, idx));
        idx += 4;
        oAdvanceProfile.setInitialOnLevel(profile[idx++]);
        oAdvanceProfile.setProfileGroupId(profile[idx++]);
        oAdvanceProfile.setInitialOnTime(Utils.extractIntFromByteArray(profile, idx));
        idx += 4;
        oAdvanceProfile.setDrLevels((short) Utils.extractShortFromByteArray(profile, idx));
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

        SimPlDBHelper.getInstance().updateProfile(sName, getAdvanceProfile(), ADVANCE);
        SimPlDBHelper.getInstance().fetchRecords("b:b:1");
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
        return g_profile_cksum;
    }

    public byte[] getScheduleProfile(int profileType) {
        int idx = 0;
        byte[] s_profile = new byte[18 * 4];
        byte[] prf = null;
        if (profileType == 2) {
            for (int i = 0; i < 4; i++) {
                prf = oWeekday[i].toByte("");
                // System.out.println("mode in getprofile is " + oWeekday[i].getMode());
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
        int idx = 0;
        for (int i = 0; i < 4; i++) {
            short paher = (short) Utils.extractShortFromByteArray(g_profile, 0);
            idx += 2;
            //System.out.println("paher " + paher);
        }
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

    public static void main(String args[]) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 22);
        long ctime = c.getTimeInMillis();
        ctime = ctime / 1000;
        ctime /= 60;
        ctime /= 60;
        ctime /= 24;
        System.out.println((1 << (ctime + 3) % 7));

    }

    public int getModeBaseOnProfile() {
        Calendar oCal = Calendar.getInstance();
        int minutesOfDay = oCal.get(Calendar.HOUR_OF_DAY) * 60 + oCal.get(Calendar.MINUTE);
        short[] paher_times = oAdvanceProfile.getPaher_start();
        if (paher_times != null && paher_times.length == 4) {
            if (getDayFromEpoc(oCal.getTimeInMillis()) > 0) {
                // Weekday
             
                if (minutesOfDay >= paher_times[0] && minutesOfDay < paher_times[1]) {
                    // morning
                    return oWeekday[0].getMode();
                } else if (minutesOfDay >= paher_times[1] && minutesOfDay < paher_times[2]) {
                    // day
                    return oWeekday[1].getMode();
                } else if (minutesOfDay >= paher_times[2] && minutesOfDay < paher_times[3]) {
                    // evening
                    return oWeekday[2].getMode();
                } else if (minutesOfDay >= paher_times[3]) {
                    // night
                    return oWeekday[3].getMode();
                }
            } else {
                // Weekend
                if (minutesOfDay >= paher_times[0] && minutesOfDay < paher_times[1]) {
                    // morning
                    return oWeekend[0].getMode();
                } else if (minutesOfDay >= paher_times[1] && minutesOfDay < paher_times[2]) {
                    // day
                    return oWeekend[1].getMode();
                } else if (minutesOfDay >= paher_times[2] && minutesOfDay < paher_times[3]) {
                    // evening
                    return oWeekend[2].getMode();
                } else if (minutesOfDay >= paher_times[3]) {
                    // night
                    return oWeekend[3].getMode();
                }
            }
        }
        return 2;
    }

    public boolean getOccModeBaseOnProfile() {
        Calendar oCal = Calendar.getInstance();
        int minutesOfDay = oCal.get(Calendar.HOUR_OF_DAY) * 60 + oCal.get(Calendar.MINUTE);
        getAdvanceProfile();
        short[] paher_times = oAdvanceProfile.getPaher_start();
        if (paher_times != null && paher_times.length == 4) {
            if (getDayFromEpoc(oCal.getTimeInMillis()) > 0) {
                // Weekday
                /*
                 * System.out.println("min of day " + minutesOfDay + " " + "pahesysr[0]" + paher_times[0] + " paher[1]"
                 * + paher_times[1] + " paher[2]" + paher_times[2] + " paher[3]" + paher_times[3]);
                 * System.out.println("occupancy range is " + paher_times[0] + " - " + (paher_times[0] + (paher_times[1]
                 * - paher_times[0]) / 3)); System.out.println("occupancy range is " + paher_times[1] + " - " +
                 * (paher_times[1] + (paher_times[2] - paher_times[1]) / 3)); System.out.println("occupancy range is " +
                 * paher_times[2] + " - " + (paher_times[2] + (paher_times[3] - paher_times[2]) / 3));
                 * System.out.println("occupancy range is " + paher_times[3] + " - " + (paher_times[3] + (1440 -
                 * paher_times[3]) / 3));
                 * 
                 * System.out.println("vacancy range is " + (paher_times[1] - (paher_times[1] - paher_times[0]) / 3) +
                 * " - " + paher_times[1]); System.out.println("vacancy range is " + (paher_times[2] - (paher_times[2] -
                 * paher_times[1]) / 3) + " - " + paher_times[2]); System.out.println("vacancy range is " +
                 * (paher_times[3] - (paher_times[3] - paher_times[2]) / 3) + " - " + paher_times[3]);
                 * System.out.println("vacancy range is " + (1440 - (1440 - paher_times[2]) / 3) + " - " + 1440);
                 */

                if (minutesOfDay >= paher_times[0] && minutesOfDay < paher_times[1]) {
                    // morning
                    if (oWeekday[0].getMode() == 1
                            && (minutesOfDay >= paher_times[0] && minutesOfDay < (paher_times[0] + (paher_times[1] - paher_times[0]) / 3))) {
                        oLogger.fine("plugload will be switched on during this time as there is occupancy from min "+paher_times[0]+" to "+paher_times[0] + (paher_times[1] - paher_times[0]) / 3);
                        return true;
                    } else if (oWeekday[0].getMode() == 3
                            && (minutesOfDay >= (paher_times[1] - (paher_times[1] - paher_times[0]) / 3) && minutesOfDay < paher_times[1])) {
                        oLogger.fine("plugload will be switched on during this time as there is some vacancy from min "
                            +(paher_times[1] - (paher_times[1] - paher_times[0]) / 3)+ " to "+paher_times[1]);
                        return true;
                    }

                } else if (minutesOfDay >= paher_times[1] && minutesOfDay < paher_times[2]) {
                    // day

                    if (oWeekday[1].getMode() == 1
                            && (minutesOfDay >= paher_times[1] && minutesOfDay < (paher_times[1] + (paher_times[2] - paher_times[1]) / 3))) {
                        oLogger.fine("plugload will be switched on during this time as there is occupancy from min "+paher_times[1]+" to "+paher_times[1] + (paher_times[2] - paher_times[1]) / 3);
                        return true;
                    } else if (oWeekday[1].getMode() == 3
                            && (minutesOfDay >= (paher_times[2] - (paher_times[2] - paher_times[1]) / 3) && minutesOfDay < paher_times[2])) {
                        oLogger.fine("plugload will be switched on during this time as there is some vacancy from min "
                                +(paher_times[2] - (paher_times[2] - paher_times[1]) / 3)+ " to "+paher_times[2]);
                        return true;
                    }

                } else if (minutesOfDay >= paher_times[2] && minutesOfDay < paher_times[3]) {
                    // evening
                    if (oWeekday[2].getMode() == 1
                            && (minutesOfDay >= paher_times[2] && minutesOfDay < (paher_times[2] + (paher_times[3] - paher_times[2]) / 3))) {
                        oLogger.fine("plugload will be switched on during this time as there is occupancy from min "+paher_times[2]+" to "+paher_times[2] + (paher_times[3] - paher_times[2]) / 3);
                        return true;
                    } else if (oWeekday[2].getMode() == 3
                            && (minutesOfDay >= (paher_times[3] - (paher_times[3] - paher_times[2]) / 3) && minutesOfDay < paher_times[3])) {
                        oLogger.fine("plugload will be switched on during this time as there is some vacancy from min "
                                +(paher_times[3] - (paher_times[3] - paher_times[2]) / 3)+ " to "+paher_times[3]);
                        return true;
                    }

                } else if (minutesOfDay >= paher_times[3]) {
                    // night
                    if (oWeekday[3].getMode() == 1
                            && (minutesOfDay >= paher_times[3] && minutesOfDay < (paher_times[3] + paher_times[3] / 3))) {
                        oLogger.fine("plugload will be switched on during this time as there is occupancy from min "+paher_times[3]+" to "+paher_times[3] + ( paher_times[3]) / 3);
                        return true;
                    } else if (oWeekday[3].getMode() == 3
                            && (minutesOfDay >= (1440 - (1440 - paher_times[3]) / 3) && minutesOfDay < 1440)) {
                        oLogger.fine("plugload will be switched on during this time as there is some vacancy from min "
                                +(1440 - (1440 - paher_times[3]) / 3)+ " to "+1440);
                        return true;
                    }

                }
            } else {

                if (minutesOfDay >= paher_times[0] && minutesOfDay < paher_times[1]) {
                    // morning
                    if (oWeekend[0].getMode() == 1
                            && (minutesOfDay >= paher_times[0] && minutesOfDay < (paher_times[0] + (paher_times[1] - paher_times[0]) / 3))) {
                      //  System.out.println("create occupancy");
                        return true;
                    } else if (oWeekend[0].getMode() == 3
                            && (minutesOfDay >= (paher_times[1] - (paher_times[1] - paher_times[0]) / 3) && minutesOfDay < paher_times[1])) {
                      //  System.out.println("create vacancy ");
                        return true;
                    }

                } else if (minutesOfDay >= paher_times[1] && minutesOfDay < paher_times[2]) {
                    // day

                    if (oWeekend[1].getMode() == 1
                            && (minutesOfDay >= paher_times[1] && minutesOfDay < (paher_times[1] + (paher_times[2] - paher_times[1]) / 3))) {
                     //   System.out.println("create occupancy");
                        return true;
                    } else if (oWeekend[1].getMode() == 3
                            && (minutesOfDay >= (paher_times[2] - (paher_times[2] - paher_times[1]) / 3) && minutesOfDay < paher_times[2])) {
                        //System.out.println("create vacancy");
                        return true;
                    }

                } else if (minutesOfDay >= paher_times[2] && minutesOfDay < paher_times[3]) {
                    // evening
                    if (oWeekend[2].getMode() == 1
                            && (minutesOfDay >= paher_times[2] && minutesOfDay < (paher_times[2] + (paher_times[3] - paher_times[2]) / 3))) {
                        //System.out.println("create occupancy");
                        return true;
                    } else if (oWeekend[2].getMode() == 3
                            && (minutesOfDay >= (paher_times[3] - (paher_times[3] - paher_times[2]) / 3) && minutesOfDay < paher_times[3])) {
                     //   System.out.println("create vacancy");
                        return true;
                    }

                } else if (minutesOfDay >= paher_times[3]) {
                    // night
                    if (oWeekend[3].getMode() == 1
                            && (minutesOfDay >= paher_times[3] && minutesOfDay < (paher_times[3] + paher_times[3] / 3))) {
                     //   System.out.println("create occupancy");
                        return true;
                    } else if (oWeekend[3].getMode() == 3
                            && (minutesOfDay >= (1440 - (1440 - paher_times[3]) / 3) && minutesOfDay < 1440)) {
                      //  System.out.println("create vacancy");
                        return true;
                    }

                }

            }

        }

        return false;
    }

}
