package com.ems.model;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ems.server.ServerConstants;
import com.ems.server.util.ServerUtil;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
public class ProfileHandler implements Serializable, Cloneable  {

    private static final long serialVersionUID = 5619650083032600792L;
    private static Logger profileLogger = Logger.getLogger("ProfileLogger");

    private Long id;
    private Profile morningProfile;
    private Profile dayProfile;
    private Profile eveningProfile;
    private Profile nightProfile;
    private Profile morningProfileWeekEnd;
    private Profile dayProfileWeekEnd;
    private Profile eveningProfileWeekEnd;
    private Profile nightProfileWeekEnd;
    private Profile morningProfileHoliday;
    private Profile dayProfileHoliday;
    private Profile eveningProfileHoliday;
    private Profile nightProfileHoliday;
    private ProfileConfiguration profileConfiguration;
    private Integer darkLux;
    private Integer neighborLux;
    private Integer envelopeOnLevel;
    private Integer dropPercent;
    private Integer risePercent;
    private Short dimBackoffTime;
    private Short intensityNormTime;
    private Integer onAmbLightLevel;
    private Short minLevelBeforeOff;
    private Integer relaysConnected;
    private Short profileChecksum;
    private Short globalProfileChecksum;
    private Short standaloneMotionOverride;
    private Byte drReactivity;
    private Integer toOffLinger;
    private Byte initialOnLevel;
    private Short profileGroupId;
    private Byte profileFlag;
    private Integer initialOnTime;
    private Byte isHighBay;
    private Integer motionThresholdGain;

    public Object clone() {
        return new ProfileHandler();
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the morningProfile
     */
    public Profile getMorningProfile() {
        return morningProfile;
    }

    /**
     * @param morningProfile
     *            the morningProfile to set
     */
    public void setMorningProfile(Profile morningProfile) {
        this.morningProfile = morningProfile;
    }

    /**
     * @return the dayProfile
     */
    public Profile getDayProfile() {
        return dayProfile;
    }

    /**
     * @param dayProfile
     *            the dayProfile to set
     */
    public void setDayProfile(Profile dayProfile) {
        this.dayProfile = dayProfile;
    }

    /**
     * @return the eveningProfile
     */
    public Profile getEveningProfile() {
        return eveningProfile;
    }

    /**
     * @param eveningProfile
     *            the eveningProfile to set
     */
    public void setEveningProfile(Profile eveningProfile) {
        this.eveningProfile = eveningProfile;
    }

    /**
     * @return the nightProfile
     */
    public Profile getNightProfile() {
        return nightProfile;
    }

    /**
     * @param nightProfile
     *            the nightProfile to set
     */
    public void setNightProfile(Profile nightProfile) {
        this.nightProfile = nightProfile;
    }

    /**
     * @return the morningProfileWeekEnd
     */
    public Profile getMorningProfileWeekEnd() {
        return morningProfileWeekEnd;
    }

    /**
     * @param morningProfileWeekEnd
     *            the morningProfileWeekEnd to set
     */
    public void setMorningProfileWeekEnd(Profile morningProfileWeekEnd) {
        this.morningProfileWeekEnd = morningProfileWeekEnd;
    }

    /**
     * @return the dayProfileWeekEnd
     */
    public Profile getDayProfileWeekEnd() {
        return dayProfileWeekEnd;
    }

    /**
     * @param dayProfileWeekEnd
     *            the dayProfileWeekEnd to set
     */
    public void setDayProfileWeekEnd(Profile dayProfileWeekEnd) {
        this.dayProfileWeekEnd = dayProfileWeekEnd;
    }

    /**
     * @return the eveningProfileWeekEnd
     */
    public Profile getEveningProfileWeekEnd() {
        return eveningProfileWeekEnd;
    }

    /**
     * @param eveningProfileWeekEnd
     *            the eveningProfileWeekEnd to set
     */
    public void setEveningProfileWeekEnd(Profile eveningProfileWeekEnd) {
        this.eveningProfileWeekEnd = eveningProfileWeekEnd;
    }

    /**
     * @return the nightProfileWeekEnd
     */
    public Profile getNightProfileWeekEnd() {
        return nightProfileWeekEnd;
    }

    /**
     * @param nightProfileWeekEnd
     *            the nightProfileWeekEnd to set
     */
    public void setNightProfileWeekEnd(Profile nightProfileWeekEnd) {
        this.nightProfileWeekEnd = nightProfileWeekEnd;
    }

    /**
     * @return the morningProfileHoliday
     */
    public Profile getMorningProfileHoliday() {
        return morningProfileHoliday;
    }

    /**
     * @param morningProfileHoliday
     *            the morningProfileHoliday to set
     */
    public void setMorningProfileHoliday(Profile morningProfileHoliday) {
        this.morningProfileHoliday = morningProfileHoliday;
    }

    /**
     * @return the dayProfileHoliday
     */
    public Profile getDayProfileHoliday() {
        return dayProfileHoliday;
    }

    /**
     * @param dayProfileHoliday
     *            the dayProfileHoliday to set
     */
    public void setDayProfileHoliday(Profile dayProfileHoliday) {
        this.dayProfileHoliday = dayProfileHoliday;
    }

    /**
     * @return the eveningProfileHoliday
     */
    public Profile getEveningProfileHoliday() {
        return eveningProfileHoliday;
    }

    /**
     * @param eveningProfileHoliday
     *            the eveningProfileHoliday to set
     */
    public void setEveningProfileHoliday(Profile eveningProfileHoliday) {
        this.eveningProfileHoliday = eveningProfileHoliday;
    }

    /**
     * @return the nightProfileHoliday
     */
    public Profile getNightProfileHoliday() {
        return nightProfileHoliday;
    }

    /**
     * @param nightProfileHoliday
     *            the nightProfileHoliday to set
     */
    public void setNightProfileHoliday(Profile nightProfileHoliday) {
        this.nightProfileHoliday = nightProfileHoliday;
    }

    /**
     * @return the profileConfiguration
     */
    public ProfileConfiguration getProfileConfiguration() {
        return profileConfiguration;
    }

    /**
     * @param profileConfiguration
     *            the profileConfiguration to set
     */
    public void setProfileConfiguration(ProfileConfiguration profileConfiguration) {
        this.profileConfiguration = profileConfiguration;
    }

    /**
     * @return dark lux
     */
    public Integer getDarkLux() {
        return darkLux;
    }

    public void setDarkLux(Integer darkLux) {
        this.darkLux = darkLux;
    }

    /**
     * @return neighbor lux
     */
    public Integer getNeighborLux() {
        return neighborLux;
    }

    public void setNeighborLux(Integer neighborLux) {
        this.neighborLux = neighborLux;
    }

    /**
     * @return envelope on level
     */
    public Integer getEnvelopeOnLevel() {
        return envelopeOnLevel;
    }

    public void setEnvelopeOnLevel(Integer envelopeOnLevel) {
        this.envelopeOnLevel = envelopeOnLevel;
    }

    /**
     * @return drop percentage
     */
    public Integer getDropPercent() {
        return dropPercent;
    }

    public void setDropPercent(Integer dropPercent) {
        this.dropPercent = dropPercent;
    }

    /**
     * @return rise percentage
     */
    public Integer getRisePercent() {
        return risePercent;
    }

    public void setRisePercent(Integer risePercent) {
        this.risePercent = risePercent;
    }

    /**
     * @return dim back off time
     */
    public Short getDimBackoffTime() {
        return dimBackoffTime;
    }

    public void setDimBackoffTime(Short dimBackoffTime) {
        this.dimBackoffTime = dimBackoffTime;
    }

    /**
     * @return intensity norm time
     */
    public Short getIntensityNormTime() {
        return intensityNormTime;
    }

    public void setIntensityNormTime(Short intensityNormTime) {
        this.intensityNormTime = intensityNormTime;
    }

    /**
     * @return on ambience light level
     */
    public Integer getOnAmbLightLevel() {
        return onAmbLightLevel;
    }

    public void setOnAmbLightLevel(Integer onAmbLightLevel) {
        this.onAmbLightLevel = onAmbLightLevel;
    }

    /**
     * @return minimum level before off
     */
    public Short getMinLevelBeforeOff() {
        return minLevelBeforeOff;
    }

    public void setMinLevelBeforeOff(Short minLevelBeforeOff) {
        this.minLevelBeforeOff = minLevelBeforeOff;
    }

    /**
     * @return relays connected
     */
    public Integer getRelaysConnected() {
        return relaysConnected;
    }

    public void setRelaysConnected(Integer relaysConnected) {
        this.relaysConnected = relaysConnected;
    }

    /**
     * @return profile checksum
     */
    public Short getProfileChecksum() {
        return profileChecksum;
    }

    public void setProfileChecksum(Short profileChecksum) {
        this.profileChecksum = profileChecksum;
    }

    /**
     * @return global profile checksum
     */
    public Short getGlobalProfileChecksum() {
        return globalProfileChecksum;
    }

    public void setGlobalProfileChecksum(Short globalProfileChecksum) {
        this.globalProfileChecksum = globalProfileChecksum;
    }

    /**
     * @return standalone motion override
     */
    public Short getStandaloneMotionOverride() {
        return standaloneMotionOverride;
    }

    public void setStandaloneMotionOverride(Short standaloneMotionOverride) {
        this.standaloneMotionOverride = standaloneMotionOverride;
    }

    /**
     * @return dr reactivity
     */
    public Byte getDrReactivity() {
        return drReactivity;
    }

    public void setDrReactivity(Byte drReactivity) {
        this.drReactivity = drReactivity;
    }

    /**
     * @return to off linger
     */
    public Integer getToOffLinger() {
        if (toOffLinger == null)
            return 10;
        return toOffLinger;
    }

    public void setToOffLinger(Integer toOffLinger) {
        this.toOffLinger = toOffLinger;
    }

    /**
     * @return to initial on time
     */
    public Integer getInitialOnTime() {
        if (initialOnTime == null)
            return 5;
        return initialOnTime;
    }

    public void setInitialOnTime(Integer initialOnTime) {
        this.initialOnTime = initialOnTime;
    }

    /**
     * @return initial on level
     */
    public Byte getInitialOnLevel() {
        return initialOnLevel;
    }

    public void setInitialOnLevel(Byte initialOnLevel) {
        this.initialOnLevel = initialOnLevel;
    }

    /**
     * @return profile group id
     */
    public Short getProfileGroupId() {
        return profileGroupId;
    }

    public void setProfileGroupId(Short profileGroupId) {
        this.profileGroupId = profileGroupId;
    }

    /**
     * @return profile flag
     */
    public Byte getProfileFlag() {
        if (profileFlag == null) {
            return 0;
        }
        return profileFlag;
    }

    public void setProfileFlag(Byte profileFlag) {
        this.profileFlag = profileFlag;
    }
    
    /**
     * @return the isHighBay
     */
    public Byte getIsHighBay() {
        if (isHighBay == null)
            return 0;
      return isHighBay;
    }
    
    /**
     * @param isHighBay the isHighBay to set
     */
    public void setIsHighBay(Byte isHighBay) {
    
      this.isHighBay = isHighBay;
    }
    
    /**
     * @return the motionThresholdGain
     */
    public Integer getMotionThresholdGain() {
    if (motionThresholdGain == null)
        return 0;
      return motionThresholdGain;
    }
    
    /**
     * @param motionThresholdGain the motionThresholdGain to set
     */
    public void setMotionThresholdGain(Integer motionThresholdGain) {
    
      this.motionThresholdGain = motionThresholdGain;
    }

    public ProfileHandler copy() {
        ProfileHandler ph = new ProfileHandler();
        ph.setDayProfile(dayProfile.copy());
        ph.setDayProfileHoliday(dayProfileHoliday.copy());
        ph.setDayProfileWeekEnd(dayProfileWeekEnd.copy());
        ph.setEveningProfile(eveningProfile.copy());
        ph.setEveningProfileHoliday(eveningProfileHoliday.copy());
        ph.setEveningProfileWeekEnd(eveningProfileWeekEnd.copy());
        ph.setMorningProfile(morningProfile.copy());
        ph.setMorningProfileHoliday(morningProfileHoliday.copy());
        ph.setMorningProfileWeekEnd(morningProfileWeekEnd.copy());
        ph.setNightProfile(nightProfile.copy());
        ph.setNightProfileHoliday(nightProfileHoliday.copy());
        ph.setNightProfileWeekEnd(nightProfileWeekEnd.copy());
        ph.setProfileConfiguration(profileConfiguration.copy());
        ph.setDarkLux(darkLux);
        ph.setNeighborLux(neighborLux);
        ph.setEnvelopeOnLevel(envelopeOnLevel);
        ph.setDropPercent(dropPercent);
        ph.setRisePercent(risePercent);
        ph.setDimBackoffTime(dimBackoffTime);
        ph.setIntensityNormTime(intensityNormTime);
        ph.setOnAmbLightLevel(onAmbLightLevel);
        ph.setMinLevelBeforeOff(minLevelBeforeOff);
        ph.setRelaysConnected(relaysConnected);
        ph.setProfileChecksum(profileChecksum);
        ph.setGlobalProfileChecksum(globalProfileChecksum);
        ph.setStandaloneMotionOverride(standaloneMotionOverride);
        ph.setDrReactivity(drReactivity);
        ph.setToOffLinger(toOffLinger);
        ph.setInitialOnLevel(initialOnLevel);
        ph.setProfileGroupId(profileGroupId);
        ph.setProfileFlag(profileFlag);
        ph.setInitialOnTime(initialOnTime);
        ph.setIsHighBay(isHighBay);
        ph.setMotionThresholdGain(motionThresholdGain);
        return ph;
    }

    public void copyFrom(ProfileHandler target) {
        this.dayProfile.copyFrom(target.getDayProfile());
        this.dayProfileHoliday.copyFrom(target.getDayProfileHoliday());
        this.dayProfileWeekEnd.copyFrom(target.getDayProfileWeekEnd());
        this.eveningProfile.copyFrom(target.getEveningProfile());
        this.eveningProfileHoliday.copyFrom(target.getEveningProfileHoliday());
        this.eveningProfileWeekEnd.copyFrom(target.getEveningProfileWeekEnd());
        this.morningProfile.copyFrom(target.getMorningProfile());
        this.morningProfileHoliday.copyFrom(target.getMorningProfileHoliday());
        this.morningProfileWeekEnd.copyFrom(target.getMorningProfileWeekEnd());
        this.nightProfile.copyFrom(target.getNightProfile());
        this.nightProfileHoliday.copyFrom(target.getNightProfileHoliday());
        this.nightProfileWeekEnd.copyFrom(target.getNightProfileWeekEnd());
        this.profileConfiguration.copyFrom(target.getProfileConfiguration());
        this.setDarkLux(target.getDarkLux());
        this.setNeighborLux(target.getNeighborLux());
        this.setEnvelopeOnLevel(target.getEnvelopeOnLevel());
        this.setDropPercent(target.getDropPercent());
        this.setRisePercent(target.getRisePercent());
        this.setDimBackoffTime(target.getDimBackoffTime());
        this.setIntensityNormTime(target.getIntensityNormTime());
        this.setOnAmbLightLevel(target.getOnAmbLightLevel());
        this.setMinLevelBeforeOff(target.getMinLevelBeforeOff());
        this.setRelaysConnected(target.getRelaysConnected());
        this.setProfileChecksum(target.getProfileChecksum());
        this.setGlobalProfileChecksum(target.getGlobalProfileChecksum());
        this.setStandaloneMotionOverride(target.getStandaloneMotionOverride());
        this.setDrReactivity(target.getDrReactivity());
        this.setToOffLinger(target.getToOffLinger());
        this.setInitialOnLevel(target.getInitialOnLevel());
        this.setProfileGroupId(target.getProfileGroupId());
        this.setProfileFlag(target.getProfileFlag());
        this.setInitialOnTime(target.getInitialOnTime());
        this.setIsHighBay(target.getIsHighBay());
        this.setMotionThresholdGain(target.getMotionThresholdGain());
    }
    
    public void copyProfilesFrom(ProfileHandler target) {
        this.dayProfile.copyFrom(target.getDayProfile());
        this.dayProfileWeekEnd.copyFrom(target.getDayProfileWeekEnd());
        this.eveningProfile.copyFrom(target.getEveningProfile());
        this.eveningProfileWeekEnd.copyFrom(target.getEveningProfileWeekEnd());
        this.morningProfile.copyFrom(target.getMorningProfile());
        this.morningProfileWeekEnd.copyFrom(target.getMorningProfileWeekEnd());
        this.nightProfile.copyFrom(target.getNightProfile());
        this.nightProfileWeekEnd.copyFrom(target.getNightProfileWeekEnd());
    }

    public void copyPCTimesFrom(ProfileHandler target) {
        this.profileConfiguration.copyPCTimingsFrom(target.getProfileConfiguration());
    }
    
    public void copyAdvanceProfileFrom(ProfileHandler target) {
        this.setDarkLux(target.getDarkLux());
        this.setNeighborLux(target.getNeighborLux());
        this.setEnvelopeOnLevel(target.getEnvelopeOnLevel());
        this.setDropPercent(target.getDropPercent());
        this.setRisePercent(target.getRisePercent());
        this.setDimBackoffTime(target.getDimBackoffTime());
        this.setIntensityNormTime(target.getIntensityNormTime());
        this.setOnAmbLightLevel(target.getOnAmbLightLevel());
        this.setMinLevelBeforeOff(target.getMinLevelBeforeOff());
        if(target.getRelaysConnected()!=null)
           	this.setRelaysConnected(target.getRelaysConnected());
        this.setProfileChecksum(target.getProfileChecksum());
        this.setGlobalProfileChecksum(target.getGlobalProfileChecksum());
        if(target.getStandaloneMotionOverride()!=null)
        	this.setStandaloneMotionOverride(target.getStandaloneMotionOverride());
        this.setDrReactivity(target.getDrReactivity());
        this.setToOffLinger(target.getToOffLinger());
        this.setInitialOnLevel(target.getInitialOnLevel());
        if(target.getProfileGroupId()!=null)
        	this.setProfileGroupId(target.getProfileGroupId());
        if(target.getProfileFlag()!=null)
        	this.setProfileFlag(target.getProfileFlag());
        if(target.getInitialOnTime()!=null)
        	this.setInitialOnTime(target.getInitialOnTime());
        if(target.getIsHighBay()!=null)
        	this.setIsHighBay(target.getIsHighBay());
        if(target.getMotionThresholdGain()!=null)
        	this.setMotionThresholdGain(target.getMotionThresholdGain());
    }

    private short convertProfileTimeToShort(String time) {

        boolean night = true;
        String meridian = time.substring(time.length() - 2, time.length());
        int colIndex = time.indexOf(":");
        short hr = Short.parseShort(time.substring(0, colIndex));
        
        if (meridian.equals("AM") && hr == 12) {
        	hr =0;
        	night = false;
        }
        else if (meridian.equals("AM") && hr < 12) {
            night = false;
        } else if(hr == 12 && meridian.equals("PM")){
            night = false;
        }
      
        if (night) {
            hr += 12;
        }
        int spaceIndex = time.indexOf(" ");
        short min = Short.parseShort(time.substring(colIndex + 1, spaceIndex));
        min = (short) ((hr * 60) + min);
        return min;

    } // end of method convertProfileTimeToShort

    public Profile getCurrentProfile() {

        Calendar currentDate = Calendar.getInstance();
        int minOfDay = currentDate.get(Calendar.HOUR_OF_DAY) * 60 + currentDate.get(Calendar.MINUTE);
        ProfileConfiguration prConfig = profileConfiguration;

        // if it is holiday return holiday profiles
        // 1.3 UI does not support setting holidays dates in the system.
//        boolean holiday = false;
////        if (prConfig.getHolidays() != null) {
////            Iterator<Holiday> holidaysIter = prConfig.getHolidays().iterator();
////            Holiday oHoliday = null;
////            while (holidaysIter.hasNext()) {
////                oHoliday = holidaysIter.next();
////                if (oHoliday.getHoliday().compareTo(currentDate.getTime()) == 0) {
////                    holiday = true;
////                    break;
////                }
////            }
////        }
//        if (holiday) {
//            if (minOfDay >= convertProfileTimeToShort(prConfig.getNightTime())) {
//                return nightProfileHoliday;
//            } else if (minOfDay >= convertProfileTimeToShort(prConfig.getEveningTime())) {
//                return eveningProfileHoliday;
//            } else if (minOfDay >= convertProfileTimeToShort(prConfig.getDayTime())) {
//                return dayProfileHoliday;
//            } else if (minOfDay >= convertProfileTimeToShort(prConfig.getMorningTime())) {
//                return morningProfileHoliday;
//            } else {
//                return nightProfileHoliday;
//            }
//        }

        // In GEMS monday is mapped as 1, tuesday is mapped as 2..... sunday as 7
        // In Java, sunday is mapped as 1, monday is mapped as 2, tuesday is mapped as 3
        int dayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK) - 1;
        if (dayOfWeek == 0) {
            dayOfWeek = 7;
        }
        Iterator<WeekDay> weekDaysIter = prConfig.getWeekDays().iterator();
        WeekDay weekDay = null;
        while (weekDaysIter.hasNext()) {
            weekDay = weekDaysIter.next();
            if (weekDay.getShortOrder() == dayOfWeek) {
                break;
            }
        }
        if (weekDay.getType().equals("weekday")) {
            if (minOfDay >= convertProfileTimeToShort(prConfig.getNightTime())) {
                return nightProfile;
            } else if (minOfDay >= convertProfileTimeToShort(prConfig.getEveningTime())) {
                return eveningProfile;
            } else if (minOfDay >= convertProfileTimeToShort(prConfig.getDayTime())) {
                return dayProfile;
            } else if (minOfDay >= convertProfileTimeToShort(prConfig.getMorningTime())) {
                return morningProfile;
            } else {
                return nightProfile;
            }
        } else {
            if (minOfDay >= convertProfileTimeToShort(prConfig.getNightTime())) {
                return nightProfileWeekEnd;
            } else if (minOfDay >= convertProfileTimeToShort(prConfig.getEveningTime())) {
                return eveningProfileWeekEnd;
            } else if (minOfDay >= convertProfileTimeToShort(prConfig.getDayTime())) {
                return dayProfileWeekEnd;
            } else if (minOfDay >= convertProfileTimeToShort(prConfig.getMorningTime())) {
                return morningProfileWeekEnd;
            } else {
                return nightProfileWeekEnd;
            }
        }

    } // end of method getCurrentProfile

    /**
     * Converts into a string format
     * 
     * @param time
     * @return
     */
    private String convertProfileTimeToString(int time) {
        try {
            DecimalFormat df = new DecimalFormat("#00.###");
            int mm = time % 60;
            int hr = time / 60;
            String paher = "AM";
            if (hr > 12) {
                hr = hr - 12;
                paher = "PM";
            }
            return hr + ":" + df.format(mm) + " " + paher;
        } catch (ArithmeticException ae) {
            profileLogger.warn(ae.getMessage());
        }
        return "12:00 AM";
    } // end of method convertProfileTimeToString

    private void fillPacket(byte[] packet, short value, int index) {

        byte[] byteArr = ServerUtil.shortToByteArray(value);
        System.arraycopy(byteArr, 0, packet, index, byteArr.length);

    } // end of method fillPacket

    public byte[] getProfileByteArray(Profile mornProfile, Profile dayProfile, Profile evenProfile, Profile nightProfile) {

        byte[] profileArray = new byte[72]; // morn, day, even, night * 6 profile attributes
        int index = 0;
        // morning
        profileArray[index++] = mornProfile.getMinLevel().byteValue();
        profileArray[index++] = mornProfile.getOnLevel().byteValue();
        fillPacket(profileArray, (short) (mornProfile.getMotionDetectDuration().shortValue() * 60), index++);
        index++;
        fillPacket(profileArray, (short) (mornProfile.getManualOverrideDuration().shortValue() * 60), index++);
        index++;
        profileArray[index++] = mornProfile.getMotionSensitivity().byteValue(); // (byte)1;
        profileArray[index++] = mornProfile.getRampUpTime().byteValue(); // ramp time (how quickly dim/bright in hms
                                                                         // (100 ms)
        profileArray[index++] = mornProfile.getAmbientSensitivity().byteValue(); // (byte)5 ambient sensitivity
        // pad with 9 bytes
        for (int i = 0; i < 9; i++) {
            profileArray[index++] = 0;
        }

        // day
        profileArray[index++] = dayProfile.getMinLevel().byteValue();
        profileArray[index++] = dayProfile.getOnLevel().byteValue();
        fillPacket(profileArray, (short) (dayProfile.getMotionDetectDuration().shortValue() * 60), index++);
        index++;
        fillPacket(profileArray, (short) (dayProfile.getManualOverrideDuration().shortValue() * 60), index++);
        index++;
        profileArray[index++] = dayProfile.getMotionSensitivity().byteValue(); // (byte)1;
        profileArray[index++] = dayProfile.getRampUpTime().byteValue(); // ramp time (how quickly dim/bright in hms (100
                                                                        // ms)
        profileArray[index++] = dayProfile.getAmbientSensitivity().byteValue(); // (byte)5 ambient sensitivity
        // pad with 9 bytes
        for (int i = 0; i < 9; i++) {
            profileArray[index++] = 0;
        }

        // evening
        profileArray[index++] = evenProfile.getMinLevel().byteValue();
        profileArray[index++] = evenProfile.getOnLevel().byteValue();
        fillPacket(profileArray, (short) (evenProfile.getMotionDetectDuration().shortValue() * 60), index++);
        index++;
        fillPacket(profileArray, (short) (evenProfile.getManualOverrideDuration().shortValue() * 60), index++);
        index++;
        profileArray[index++] = evenProfile.getMotionSensitivity().byteValue(); // (byte)1;
        profileArray[index++] = evenProfile.getRampUpTime().byteValue(); // ramp time (how quickly dim/bright in hms
                                                                         // (100 ms)
        profileArray[index++] = evenProfile.getAmbientSensitivity().byteValue(); // (byte)5 ambient sensitivity
        // pad with 9 bytes
        for (int i = 0; i < 9; i++) {
            profileArray[index++] = 0;
        }

        // night
        profileArray[index++] = nightProfile.getMinLevel().byteValue();
        profileArray[index++] = nightProfile.getOnLevel().byteValue();
        fillPacket(profileArray, (short) (nightProfile.getMotionDetectDuration().shortValue() * 60), index++);
        index++;
        fillPacket(profileArray, (short) (nightProfile.getManualOverrideDuration().shortValue() * 60), index++);
        index++;
        profileArray[index++] = nightProfile.getMotionSensitivity().byteValue(); // (byte)1;
        profileArray[index++] = nightProfile.getRampUpTime().byteValue(); // (byte)50; //ramp time (how quickly
                                                                          // dim/bright in hms (100 ms)
        profileArray[index++] = nightProfile.getAmbientSensitivity().byteValue(); // (byte)5 ambient sensitivity
        // pad with 9 bytes
        for (int i = 0; i < 9; i++) {
            profileArray[index++] = 0;
        }
        return profileArray;

    } // end of method getProfileByteArray

    public byte[] getScheduledProfileByteArray() {

        byte[] weekDayByteArr = getProfileByteArray(morningProfile, dayProfile, eveningProfile, nightProfile);

        byte[] weekEndByteArr = getProfileByteArray(morningProfileWeekEnd, dayProfileWeekEnd, eveningProfileWeekEnd,
                nightProfileWeekEnd);

        byte[] holidayByteArr = getProfileByteArray(morningProfileHoliday, dayProfileHoliday, eveningProfileHoliday,
                nightProfileHoliday);

        byte[] profileByteArr = new byte[weekDayByteArr.length + weekEndByteArr.length + holidayByteArr.length];
        System.arraycopy(weekDayByteArr, 0, profileByteArr, 0, weekDayByteArr.length);
        System.arraycopy(weekEndByteArr, 0, profileByteArr, weekDayByteArr.length, weekEndByteArr.length);
        System.arraycopy(holidayByteArr, 0, profileByteArr, weekDayByteArr.length + weekEndByteArr.length,
                holidayByteArr.length);

        return profileByteArr;

    } // end of method getProfileByteArray

    public byte[] getGlobalProfileByteArray() {

        ProfileConfiguration prConfg = getProfileConfiguration();

        byte[] packet = new byte[53];
        int i = 0;
        short mornTime = convertProfileTimeToShort(prConfg.getMorningTime());
        fillPacket(packet, mornTime, i++);
        i++;
        short dayTime = convertProfileTimeToShort(prConfg.getDayTime());
        fillPacket(packet, dayTime, i++);
        i++;
        short evenTime = convertProfileTimeToShort(prConfg.getEveningTime());
        fillPacket(packet, evenTime, i++);
        i++;
        short nightTime = convertProfileTimeToShort(prConfg.getNightTime());
        fillPacket(packet, nightTime, i++);
        i++;

        fillPacket(packet, darkLux.shortValue(), i++);
        i++;
        fillPacket(packet, neighborLux.shortValue(), i++);
        i++;
        fillPacket(packet, envelopeOnLevel.shortValue(), i++);
        i++;
        packet[i++] = dropPercent.byteValue();
        packet[i++] = risePercent.byteValue();
        packet[i++] = relaysConnected.byteValue();
        packet[i++] = dimBackoffTime.byteValue(); // byte in min dim back off time
        fillPacket(packet, intensityNormTime.shortValue(), i++);
        i++;
        packet[i++] = minLevelBeforeOff.byteValue();// min level before off
        // range of standalone motion override is 0 - 200
        short standMotionOverride = standaloneMotionOverride.shortValue();
        if (standMotionOverride > 127) {
            packet[i++] = (byte) (standMotionOverride - 256);
        } else {
            packet[i++] = (byte) (standMotionOverride);
        }
        packet[i++] = drReactivity.byteValue();
        fillPacket(packet, getToOffLinger().shortValue(), i++);
        i++;
        packet[i++] = getWeekDaysAndWeekEnds(prConfg);
        packet[i++] = initialOnLevel;
        packet[i++] = profileGroupId.byteValue();
        packet[i++] = getProfileFlag();
        fillPacket(packet, getInitialOnTime().shortValue(), i++);
        i++;
        packet[i++] = getIsHighBay();
        fillPacket(packet, getMotionThresholdGain().shortValue(), i++);
        i++;
        // pad with 19 bytes
        for (int j = 0; j < 19; j++) {
            packet[i++] = 0;
        }
        return packet;

    } // end of method getGlobalProfileByteArray

    private byte getWeekDaysAndWeekEnds(ProfileConfiguration prConfg) {
        char days[] = { '0', '0', '0', '1', '1', '1', '1', '1' };
        Set<WeekDay> oWeekDays = prConfg.getWeekDays();
        // Count starts from 1.
        if (oWeekDays != null) {
            for (WeekDay weekDay : oWeekDays) {
                if (weekDay.getDay().equals("Sunday")) {
                    if (weekDay.getType().equals("weekday")) {
                        days[1] = '1';
                    } else {
                        days[1] = '0';
                    }
                } else if (weekDay.getDay().equals("Saturday")) {
                    if (weekDay.getType().equals("weekday")) {
                        days[2] = '1';
                    } else {
                        days[2] = '0';
                    }
                } else if (weekDay.getDay().equals("Friday")) {
                    if (weekDay.getType().equals("weekend")) {
                        days[3] = '0';
                    } else {
                        days[3] = '1';
                    }
                } else if (weekDay.getDay().equals("Thursday")) {
                    if (weekDay.getType().equals("weekend")) {
                        days[4] = '0';
                    } else {
                        days[4] = '1';
                    }
                } else if (weekDay.getDay().equals("Wednesday")) {
                    if (weekDay.getType().equals("weekend")) {
                        days[5] = '0';
                    } else {
                        days[5] = '1';
                    }
                } else if (weekDay.getDay().equals("Tuesday")) {
                    if (weekDay.getType().equals("weekend")) {
                        days[6] = '0';
                    } else {
                        days[6] = '1';
                    }
                } else if (weekDay.getDay().equals("Monday")) {
                    if (weekDay.getType().equals("weekend")) {
                        days[7] = '0';
                    } else {
                        days[7] = '1';
                    }
                }
            }
        }
        String strDays = String.valueOf(days);
        return Byte.parseByte(strDays, 2);
    }

    private void setWeekDaysAndWeekEnds(ProfileConfiguration prConfg, char[] days) {
        Set<WeekDay> oWeekDays = new HashSet<WeekDay>();
        for (int count = 1; count < days.length; count++) {
            WeekDay oDay = new WeekDay();
            if (days[count] == '0') {
                oDay.setType("weekend");
            } else {
                oDay.setType("weekday");
            }
            if (count == 1) {
                oDay.setDay("Sunday");
                oDay.setShortOrder(7);
            } else if (count == 2) {
                oDay.setDay("Saturday");
                oDay.setShortOrder(6);
            } else if (count == 3) {
                oDay.setDay("Friday");
                oDay.setShortOrder(5);
            } else if (count == 4) {
                oDay.setDay("Thursday");
                oDay.setShortOrder(4);
            } else if (count == 5) {
                oDay.setDay("Wednesday");
                oDay.setShortOrder(3);
            } else if (count == 6) {
                oDay.setDay("Tuesday");
                oDay.setShortOrder(2);
            } else if (count == 7) {
                oDay.setDay("Monday");
                oDay.setShortOrder(1);
            }
            oDay.setProfileConfiguration(prConfg);
            oWeekDays.add(oDay);
        }
        prConfg.setWeekDays(oWeekDays);
    }

    public void create() {
        morningProfile = new Profile();
        dayProfile = new Profile();
        eveningProfile = new Profile();
        nightProfile = new Profile();
        morningProfileWeekEnd = new Profile();
        dayProfileWeekEnd = new Profile();
        eveningProfileWeekEnd = new Profile();
        nightProfileWeekEnd = new Profile();
        morningProfileHoliday = new Profile();
        dayProfileHoliday = new Profile();
        eveningProfileHoliday = new Profile();
        nightProfileHoliday = new Profile();
        profileConfiguration = new ProfileConfiguration();
    }

    /**
     * Constructs a Weekday profile object from packet received from SU
     * 
     * @param pkt
     */
    public void setWeekdayProfileFromByteArray(byte[] pkt) {

        if (morningProfile == null) {
            morningProfile = new Profile();
        }
        if (dayProfile == null) {
            dayProfile = new Profile();
        }
        if (eveningProfile == null) {
            eveningProfile = new Profile();
        }
        if (nightProfile == null) {
            nightProfile = new Profile();
        }
        setProfileFromByteArray(pkt, morningProfile, dayProfile, eveningProfile, nightProfile);
        printWeekdayProfile();

    } // end of method setWeekdayProfileFromByteArray

    /**
     * Constructs a Weekend profile from packet received from SU
     * 
     * @param pkt
     */
    public void setWeekendProfileFromByteArray(byte[] pkt) {

        if (morningProfileWeekEnd == null) {
            morningProfileWeekEnd = new Profile();
        }
        if (dayProfileWeekEnd == null) {
            dayProfileWeekEnd = new Profile();
        }
        if (eveningProfileWeekEnd == null) {
            eveningProfileWeekEnd = new Profile();
        }
        if (nightProfileWeekEnd == null) {
            nightProfileWeekEnd = new Profile();
        }
        setProfileFromByteArray(pkt, morningProfileWeekEnd, dayProfileWeekEnd, eveningProfileWeekEnd,
                nightProfileWeekEnd);
        printWeekendProfile();

    } // end of method setWeekendProfileFromByteArray

    /**
     * Constructs a Holiday profile from packet received from SU.
     * 
     * @param pkt
     */
    public void setHolidayProfileFromByteArray(byte pkt[]) {

        if (morningProfileHoliday == null) {
            morningProfileHoliday = new Profile();
        }
        if (dayProfileHoliday == null) {
            dayProfileHoliday = new Profile();
        }
        if (eveningProfileHoliday == null) {
            eveningProfileHoliday = new Profile();
        }
        if (nightProfileHoliday == null) {
            nightProfileHoliday = new Profile();
        }
        setProfileFromByteArray(pkt, morningProfileHoliday, dayProfileHoliday, eveningProfileHoliday,
                nightProfileHoliday);
        printHolidayProfile();

    } // end of method setHolidayProfileFromByteArray

    /**
     * Constructs a profile from packet received from SU.
     * 
     * @param pkt
     */
    public void setProfileFromByteArray(byte[] pkt, Profile morningProfile, Profile dayProfile, Profile eveningProfile,
            Profile nightProfile) {

        int index = ServerConstants.RES_CMD_PKT_MSG_START_POS;
        if (pkt[0] == ServerConstants.FRAME_START_MARKER) { // old packet
            return;
        }
        index++; // profile type
        // morning

        morningProfile.setMinLevel((long) pkt[index++]);
        morningProfile.setOnLevel((long) pkt[index++]);
        byte[] tempShortByteArr = { pkt[index++], pkt[index++] };
        morningProfile.setMotionDetectDuration((long) ServerUtil.byteArrayToShort(tempShortByteArr) / 60);
        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        morningProfile.setManualOverrideDuration((long) ServerUtil.byteArrayToShort(tempShortByteArr) / 60);
        morningProfile.setMotionSensitivity((long) pkt[index++]);
        morningProfile.setRampUpTime((long) pkt[index++]);
        morningProfile.setAmbientSensitivity((int) pkt[index++]);
        index += 9; // holes in the profile

        // day
        dayProfile.setMinLevel((long) pkt[index++]);
        dayProfile.setOnLevel((long) pkt[index++]);
        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        dayProfile.setMotionDetectDuration((long) ServerUtil.byteArrayToShort(tempShortByteArr) / 60);
        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        dayProfile.setManualOverrideDuration((long) ServerUtil.byteArrayToShort(tempShortByteArr) / 60);
        dayProfile.setMotionSensitivity((long) pkt[index++]);
        dayProfile.setRampUpTime((long) pkt[index++]);
        dayProfile.setAmbientSensitivity((int) pkt[index++]);
        index += 9;

        // evening
        eveningProfile.setMinLevel((long) pkt[index++]);
        eveningProfile.setOnLevel((long) pkt[index++]);
        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        eveningProfile.setMotionDetectDuration((long) ServerUtil.byteArrayToShort(tempShortByteArr) / 60);
        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        eveningProfile.setManualOverrideDuration((long) ServerUtil.byteArrayToShort(tempShortByteArr) / 60);
        eveningProfile.setMotionSensitivity((long) pkt[index++]);
        eveningProfile.setRampUpTime((long) pkt[index++]);
        eveningProfile.setAmbientSensitivity((int) pkt[index++]);
        index += 9;

        // night
        nightProfile.setMinLevel((long) pkt[index++]);
        nightProfile.setOnLevel((long) pkt[index++]);
        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        nightProfile.setMotionDetectDuration((long) ServerUtil.byteArrayToShort(tempShortByteArr) / 60);
        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        nightProfile.setManualOverrideDuration((long) ServerUtil.byteArrayToShort(tempShortByteArr) / 60);
        nightProfile.setMotionSensitivity((long) pkt[index++]);
        nightProfile.setRampUpTime((long) pkt[index++]);
        nightProfile.setAmbientSensitivity((int) pkt[index++]);

    } // end of method setHolidayProfileFromByteArray

    /**
     * Constructs a Advance profile from the packet received from SU
     * 
     * @param pkt
     */
    public void setAdvanceProfile(byte[] pkt) {
        int index = ServerConstants.RES_CMD_PKT_MSG_START_POS;
        if (pkt[0] == ServerConstants.FRAME_START_MARKER) { // old packet
            return;
        }
        index++; // profile type
        if (profileConfiguration == null) {
            profileConfiguration = new ProfileConfiguration();
        }

        byte[] tempShortByteArr = { pkt[index++], pkt[index++] };
        profileConfiguration.setMorningTime(convertProfileTimeToString(ServerUtil.byteArrayToShort(tempShortByteArr)));

        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        profileConfiguration.setDayTime(convertProfileTimeToString(ServerUtil.byteArrayToShort(tempShortByteArr)));

        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        profileConfiguration.setEveningTime(convertProfileTimeToString(ServerUtil.byteArrayToShort(tempShortByteArr)));

        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        profileConfiguration.setNightTime(convertProfileTimeToString(ServerUtil.byteArrayToShort(tempShortByteArr)));

        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        setDarkLux((int) ServerUtil.byteArrayToShort(tempShortByteArr));

        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        setNeighborLux((int) ServerUtil.byteArrayToShort(tempShortByteArr));

        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        setEnvelopeOnLevel((int) ServerUtil.byteArrayToShort(tempShortByteArr));

        setDropPercent((int) pkt[index++]);
        setRisePercent((int) pkt[index++]);
        setRelaysConnected((int) pkt[index++]);
        setDimBackoffTime((short) pkt[index++]);

        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        setIntensityNormTime((short) ServerUtil.byteArrayToShort(tempShortByteArr));

        setMinLevelBeforeOff((short) pkt[index++]);
        setStandaloneMotionOverride((short) pkt[index++]);
        setDrReactivity(pkt[index++]);
        tempShortByteArr = null;
        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        setToOffLinger((int) ServerUtil.byteArrayToShort(tempShortByteArr));
        int iBits = (int) pkt[index++];
        setWeekDaysAndWeekEnds(profileConfiguration, Integer.toBinaryString(0x100 | iBits).substring(1).toCharArray());
        setInitialOnLevel(pkt[index++]);
        setProfileGroupId((short) pkt[index++]);
        setProfileFlag(pkt[index++]);
        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        setInitialOnTime((int) ServerUtil.byteArrayToShort(tempShortByteArr));
        setIsHighBay(pkt[index++]);
        tempShortByteArr = new byte[] { pkt[index++], pkt[index++] };
        setMotionThresholdGain((int) ServerUtil.byteArrayToShort(tempShortByteArr));
        printAdvanceProfile();
    }

    /**
     * Prints profile details
     * 
     * @param pf
     */
    private void printWeekdayProfile() {
        StringBuffer oBuff = new StringBuffer();
        oBuff.append("Schedule Profile:").append("\r\n");
        oBuff.append("Weekday MIN_L   MAX_L   OCC_T   MOD_T   OCC_S   RMP     AMB_S").append("\r\n");
        oBuff.append("Morning\t");
        // morning
        oBuff.append(morningProfile.getMinLevel()).append("\t");
        oBuff.append(morningProfile.getOnLevel()).append("\t");
        oBuff.append(morningProfile.getMotionDetectDuration()).append("\t");
        oBuff.append(morningProfile.getManualOverrideDuration()).append("\t");
        oBuff.append(morningProfile.getMotionSensitivity()).append("\t");
        oBuff.append(morningProfile.getRampUpTime()).append("\t");
        oBuff.append(morningProfile.getAmbientSensitivity()).append("\r\n");

        // day
        oBuff.append("Day\t");
        oBuff.append(dayProfile.getMinLevel()).append("\t");
        oBuff.append(dayProfile.getOnLevel()).append("\t");
        oBuff.append(dayProfile.getMotionDetectDuration()).append("\t");
        oBuff.append(dayProfile.getManualOverrideDuration()).append("\t");
        oBuff.append(dayProfile.getMotionSensitivity()).append("\t");
        oBuff.append(dayProfile.getRampUpTime()).append("\t");
        oBuff.append(dayProfile.getAmbientSensitivity()).append("\r\n");

        // evening
        oBuff.append("Evening\t");
        oBuff.append(eveningProfile.getMinLevel()).append("\t");
        oBuff.append(eveningProfile.getOnLevel()).append("\t");
        oBuff.append(eveningProfile.getMotionDetectDuration()).append("\t");
        oBuff.append(eveningProfile.getManualOverrideDuration()).append("\t");
        oBuff.append(eveningProfile.getMotionSensitivity()).append("\t");
        oBuff.append(eveningProfile.getRampUpTime()).append("\t");
        oBuff.append(eveningProfile.getAmbientSensitivity()).append("\r\n");

        // night
        oBuff.append("Night\t");
        oBuff.append(nightProfile.getMinLevel()).append("\t");
        oBuff.append(nightProfile.getOnLevel()).append("\t");
        oBuff.append(nightProfile.getMotionDetectDuration()).append("\t");
        oBuff.append(nightProfile.getManualOverrideDuration()).append("\t");
        oBuff.append(nightProfile.getMotionSensitivity()).append("\t");
        oBuff.append(nightProfile.getRampUpTime()).append("\t");
        oBuff.append(nightProfile.getAmbientSensitivity()).append("\r\n");
        profileLogger.debug(oBuff.toString());
    }

    /**
     * Prints weekend profile details
     * 
     * @param pf
     */
    private void printWeekendProfile() {
        StringBuffer oBuff = new StringBuffer();
        oBuff.append("Schedule Profile:").append("\r\n");
        oBuff.append("Weekend MIN_L   MAX_L   OCC_T   MOD_T   OCC_S   RMP     AMB_S").append("\r\n");
        oBuff.append("Morning\t");
        // morning
        oBuff.append(morningProfileWeekEnd.getMinLevel()).append("\t");
        oBuff.append(morningProfileWeekEnd.getOnLevel()).append("\t");
        oBuff.append(morningProfileWeekEnd.getMotionDetectDuration()).append("\t");
        oBuff.append(morningProfileWeekEnd.getManualOverrideDuration()).append("\t");
        oBuff.append(morningProfileWeekEnd.getMotionSensitivity()).append("\t");
        oBuff.append(morningProfileWeekEnd.getRampUpTime()).append("\t");
        oBuff.append(morningProfileWeekEnd.getAmbientSensitivity()).append("\r\n");

        // day
        oBuff.append("Day\t");
        oBuff.append(dayProfileWeekEnd.getMinLevel()).append("\t");
        oBuff.append(dayProfileWeekEnd.getOnLevel()).append("\t");
        oBuff.append(dayProfileWeekEnd.getMotionDetectDuration()).append("\t");
        oBuff.append(dayProfileWeekEnd.getManualOverrideDuration()).append("\t");
        oBuff.append(dayProfileWeekEnd.getMotionSensitivity()).append("\t");
        oBuff.append(dayProfileWeekEnd.getRampUpTime()).append("\t");
        oBuff.append(dayProfileWeekEnd.getAmbientSensitivity()).append("\r\n");

        // evening
        oBuff.append("Evening\t");
        oBuff.append(eveningProfileWeekEnd.getMinLevel()).append("\t");
        oBuff.append(eveningProfileWeekEnd.getOnLevel()).append("\t");
        oBuff.append(eveningProfileWeekEnd.getMotionDetectDuration()).append("\t");
        oBuff.append(eveningProfileWeekEnd.getManualOverrideDuration()).append("\t");
        oBuff.append(eveningProfileWeekEnd.getMotionSensitivity()).append("\t");
        oBuff.append(eveningProfileWeekEnd.getRampUpTime()).append("\t");
        oBuff.append(eveningProfileWeekEnd.getAmbientSensitivity()).append("\r\n");

        // night
        oBuff.append("Night\t");
        oBuff.append(nightProfileWeekEnd.getMinLevel()).append("\t");
        oBuff.append(nightProfileWeekEnd.getOnLevel()).append("\t");
        oBuff.append(nightProfileWeekEnd.getMotionDetectDuration()).append("\t");
        oBuff.append(nightProfileWeekEnd.getManualOverrideDuration()).append("\t");
        oBuff.append(nightProfileWeekEnd.getMotionSensitivity()).append("\t");
        oBuff.append(nightProfileWeekEnd.getRampUpTime()).append("\t");
        oBuff.append(nightProfileWeekEnd.getAmbientSensitivity()).append("\r\n");
        profileLogger.debug(oBuff.toString());
    }

    /**
     * Prints holiday profile details
     * 
     * @param pf
     */
    private void printHolidayProfile() {
        StringBuffer oBuff = new StringBuffer();
        oBuff.append("Schedule Profile:").append("\r\n");
        oBuff.append("Holiday MIN_L   MAX_L   OCC_T   MOD_T   OCC_S   RMP     AMB_S").append("\r\n");
        oBuff.append("Morning\t");
        // morning
        oBuff.append(morningProfileHoliday.getMinLevel()).append("\t");
        oBuff.append(morningProfileHoliday.getOnLevel()).append("\t");
        oBuff.append(morningProfileHoliday.getMotionDetectDuration()).append("\t");
        oBuff.append(morningProfileHoliday.getManualOverrideDuration()).append("\t");
        oBuff.append(morningProfileHoliday.getMotionSensitivity()).append("\t");
        oBuff.append(morningProfileHoliday.getRampUpTime()).append("\t");
        oBuff.append(morningProfileHoliday.getAmbientSensitivity()).append("\r\n");

        // day
        oBuff.append("Day\t");
        oBuff.append(dayProfileHoliday.getMinLevel()).append("\t");
        oBuff.append(dayProfileHoliday.getOnLevel()).append("\t");
        oBuff.append(dayProfileHoliday.getMotionDetectDuration()).append("\t");
        oBuff.append(dayProfileHoliday.getManualOverrideDuration()).append("\t");
        oBuff.append(dayProfileHoliday.getMotionSensitivity()).append("\t");
        oBuff.append(dayProfileHoliday.getRampUpTime()).append("\t");
        oBuff.append(dayProfileHoliday.getAmbientSensitivity()).append("\r\n");

        // evening
        oBuff.append("Evening\t");
        oBuff.append(eveningProfileHoliday.getMinLevel()).append("\t");
        oBuff.append(eveningProfileHoliday.getOnLevel()).append("\t");
        oBuff.append(eveningProfileHoliday.getMotionDetectDuration()).append("\t");
        oBuff.append(eveningProfileHoliday.getManualOverrideDuration()).append("\t");
        oBuff.append(eveningProfileHoliday.getMotionSensitivity()).append("\t");
        oBuff.append(eveningProfileHoliday.getRampUpTime()).append("\t");
        oBuff.append(eveningProfileHoliday.getAmbientSensitivity()).append("\r\n");

        // night
        oBuff.append("Night\t");
        oBuff.append(nightProfileHoliday.getMinLevel()).append("\t");
        oBuff.append(nightProfileHoliday.getOnLevel()).append("\t");
        oBuff.append(nightProfileHoliday.getMotionDetectDuration()).append("\t");
        oBuff.append(nightProfileHoliday.getManualOverrideDuration()).append("\t");
        oBuff.append(nightProfileHoliday.getMotionSensitivity()).append("\t");
        oBuff.append(nightProfileHoliday.getRampUpTime()).append("\t");
        oBuff.append(nightProfileHoliday.getAmbientSensitivity()).append("\r\n");
        profileLogger.debug(oBuff.toString());
    }

    private void printAdvanceProfile() {
        StringBuffer oBuff = new StringBuffer();
        oBuff.append("MT\tDT\tET\tNT\tDL\tNL\tEL\tDP\tRP\tRC\tBK\tNT\tLO\tSAMO\tDRr\tLOF\tWDB\tIOL\tPGID").append(
                "\r\n");
        oBuff.append(profileConfiguration.getMorningTime()).append("\t");
        oBuff.append(profileConfiguration.getDayTime()).append("\t");
        oBuff.append(profileConfiguration.getEveningTime()).append("\t");
        oBuff.append(profileConfiguration.getNightTime()).append("\t");
        oBuff.append(getDarkLux()).append("\t");
        oBuff.append(getNeighborLux()).append("\t");
        oBuff.append(getEnvelopeOnLevel()).append("\t");
        oBuff.append(getDropPercent()).append("\t");
        oBuff.append(getRisePercent()).append("\t");
        oBuff.append(getRelaysConnected()).append("\t");
        oBuff.append(getDimBackoffTime()).append("\t");
        oBuff.append(getIntensityNormTime()).append("\t");
        oBuff.append(getMinLevelBeforeOff()).append("\t");
        oBuff.append(getStandaloneMotionOverride()).append("\t");
        oBuff.append(getDrReactivity()).append("\t");
        oBuff.append(getToOffLinger()).append("\t");
        oBuff.append(getWeekDaysAndWeekEnds(profileConfiguration));
        oBuff.append(getInitialOnLevel()).append("\t");
        oBuff.append(getProfileGroupId()).append("\t");
        oBuff.append(getProfileFlag()).append("\t");
        oBuff.append(getInitialOnTime()).append("\t");
        oBuff.append(getIsHighBay()).append("\t");
        oBuff.append(getMotionThresholdGain()).append("\t");
        profileLogger.debug(oBuff.toString());
    }

    public int compare(ProfileHandler p2) {
        // Compare Advance attributes
        if (this.getDarkLux().intValue() != p2.getDarkLux().intValue())
        {
        	Integer darkLux = this.getDarkLux().intValue();
        	Integer darkLux2 = p2.getDarkLux().intValue();
        	System.out.println("Place Holder"+darkLux + ":"+darkLux2);
            return -1;
        }
        if (this.getNeighborLux().intValue() != p2.getNeighborLux().intValue())
            return -1;
        if (this.getEnvelopeOnLevel().intValue() != p2.getEnvelopeOnLevel().intValue())
            return -1;
        if (this.getDropPercent().intValue() != p2.getDropPercent().intValue())
            return -1;
        if (this.getRisePercent().intValue() != p2.getRisePercent().intValue())
            return -1;
        if (this.getDimBackoffTime().intValue() != p2.getDimBackoffTime().intValue())
            return -1;
        if (this.getIntensityNormTime().intValue() != p2.getIntensityNormTime().intValue())
            return -1;
        if (this.getOnAmbLightLevel().intValue() != p2.getOnAmbLightLevel().intValue())
            return -1;
        if (this.getMinLevelBeforeOff().intValue() != p2.getMinLevelBeforeOff().intValue())
            return -1;
        if (this.getRelaysConnected().intValue() != p2.getRelaysConnected().intValue())
            return -1;
        if (this.getStandaloneMotionOverride().intValue() != p2.getStandaloneMotionOverride().intValue())
            return -1;
        if (this.getDrReactivity().intValue() != p2.getDrReactivity().intValue())
            return -1;
        if (this.getToOffLinger().intValue() != p2.getToOffLinger().intValue())
            return -1;
        if (this.getInitialOnLevel().intValue() != p2.getInitialOnLevel().intValue())
            return -1;
        if (this.getProfileGroupId().intValue() != p2.getProfileGroupId().intValue())
            return -1;
        if (this.getProfileFlag().intValue() != p2.getProfileFlag().intValue())
            return -1;
        if (this.getInitialOnTime().intValue() != p2.getInitialOnTime().intValue())
            return -1;
        if (this.getIsHighBay().intValue() != p2.getIsHighBay().intValue())
            return -1;
        if (this.getMotionThresholdGain().intValue() != p2.getMotionThresholdGain().intValue())
            return -1;

        // Compare profile now
        if (this.getMorningProfile().compare(p2.getMorningProfile()) <= -1)
            return -1;

        if (this.getDayProfile().compare(p2.getDayProfile()) <= -1)
            return -1;

        if (this.getEveningProfile().compare(p2.getEveningProfile()) <= -1)
            return -1;

        if (this.getNightProfile().compare(p2.getNightProfile()) <= -1)
            return -1;

        if (this.getMorningProfileWeekEnd().compare(p2.getMorningProfileWeekEnd()) <= -1)
            return -1;

        if (this.getDayProfileWeekEnd().compare(p2.getDayProfileWeekEnd()) <= -1)
            return -1;

        if (this.getEveningProfileWeekEnd().compare(p2.getEveningProfileWeekEnd()) <= -1)
            return -1;

        if (this.getNightProfileWeekEnd().compare(p2.getNightProfileWeekEnd()) <= -1)
            return -1;

        if (this.getMorningProfileHoliday().compare(p2.getMorningProfileHoliday()) <= -1)
            return -1;

        if (this.getDayProfileHoliday().compare(p2.getDayProfileHoliday()) <= -1)
            return -1;

        if (this.getEveningProfileHoliday().compare(p2.getEveningProfileHoliday()) <= -1)
            return -1;

        if (this.getNightProfileHoliday().compare(p2.getNightProfileHoliday()) <= -1)
            return -1;

        // Compare profile configuration
        if (this.getProfileConfiguration().compare(p2.getProfileConfiguration()) <= -1)
            return -1;
        
        return 0;
    }

}
