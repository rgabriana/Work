package com.ems.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "plugloadProfilehandler")
@XmlAccessorType(XmlAccessType.NONE)
public class PlugloadProfileHandler implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@XmlElement(name = "id")
    private Long id;
    @XmlElement(name = "morningProfile")
    private PlugloadProfile morningProfile;
    @XmlElement(name = "dayProfile")
    private PlugloadProfile dayProfile;
    @XmlElement(name = "eveningProfile")
    private PlugloadProfile eveningProfile;
    @XmlElement(name = "nightProfile")
    private PlugloadProfile nightProfile;
    @XmlElement(name = "morningProfileWeekEnd")
    private PlugloadProfile morningProfileWeekEnd;
    @XmlElement(name = "dayProfileWeekEnd")
    private PlugloadProfile dayProfileWeekEnd;
    @XmlElement(name = "eveningProfileWeekEnd")
    private PlugloadProfile eveningProfileWeekEnd;
    @XmlElement(name = "nightProfileWeekEnd")
    private PlugloadProfile nightProfileWeekEnd;
    @XmlElement(name = "morningProfileHoliday")
    private PlugloadProfile morningProfileHoliday;
    @XmlElement(name = "dayProfileHoliday")
    private PlugloadProfile dayProfileHoliday;
    @XmlElement(name = "eveningProfileHoliday")
    private PlugloadProfile eveningProfileHoliday;
    @XmlElement(name = "nightProfileHoliday")
    private PlugloadProfile nightProfileHoliday;
    @XmlElement(name = "plugloadProfileConfiguration")
    private PlugloadProfileConfiguration plugloadProfileConfiguration;      
    
    @XmlElement(name = "holidayLevel")
    private Byte holidayLevel = 0;
    @XmlElement(name = "override5")
    private PlugloadProfile override5;
    @XmlElement(name = "override6")
    private PlugloadProfile override6;
    @XmlElement(name = "override7")
    private PlugloadProfile override7;
    @XmlElement(name = "override8")
    private PlugloadProfile override8;
    
    public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public PlugloadProfile getMorningProfile() {
		return morningProfile;
	}
	public void setMorningProfile(PlugloadProfile morningProfile) {
		this.morningProfile = morningProfile;
	}
	public PlugloadProfile getDayProfile() {
		return dayProfile;
	}
	public void setDayProfile(PlugloadProfile dayProfile) {
		this.dayProfile = dayProfile;
	}
	public PlugloadProfile getEveningProfile() {
		return eveningProfile;
	}
	public void setEveningProfile(PlugloadProfile eveningProfile) {
		this.eveningProfile = eveningProfile;
	}
	public PlugloadProfile getNightProfile() {
		return nightProfile;
	}
	public void setNightProfile(PlugloadProfile nightProfile) {
		this.nightProfile = nightProfile;
	}
	public PlugloadProfile getMorningProfileWeekEnd() {
		return morningProfileWeekEnd;
	}
	public void setMorningProfileWeekEnd(PlugloadProfile morningProfileWeekEnd) {
		this.morningProfileWeekEnd = morningProfileWeekEnd;
	}
	public PlugloadProfile getDayProfileWeekEnd() {
		return dayProfileWeekEnd;
	}
	public void setDayProfileWeekEnd(PlugloadProfile dayProfileWeekEnd) {
		this.dayProfileWeekEnd = dayProfileWeekEnd;
	}
	public PlugloadProfile getEveningProfileWeekEnd() {
		return eveningProfileWeekEnd;
	}
	public void setEveningProfileWeekEnd(PlugloadProfile eveningProfileWeekEnd) {
		this.eveningProfileWeekEnd = eveningProfileWeekEnd;
	}
	public PlugloadProfile getNightProfileWeekEnd() {
		return nightProfileWeekEnd;
	}
	public void setNightProfileWeekEnd(PlugloadProfile nightProfileWeekEnd) {
		this.nightProfileWeekEnd = nightProfileWeekEnd;
	}
	public PlugloadProfile getMorningProfileHoliday() {
		return morningProfileHoliday;
	}
	public void setMorningProfileHoliday(PlugloadProfile morningProfileHoliday) {
		this.morningProfileHoliday = morningProfileHoliday;
	}
	public PlugloadProfile getDayProfileHoliday() {
		return dayProfileHoliday;
	}
	public void setDayProfileHoliday(PlugloadProfile dayProfileHoliday) {
		this.dayProfileHoliday = dayProfileHoliday;
	}
	public PlugloadProfile getEveningProfileHoliday() {
		return eveningProfileHoliday;
	}
	public void setEveningProfileHoliday(PlugloadProfile eveningProfileHoliday) {
		this.eveningProfileHoliday = eveningProfileHoliday;
	}
	public PlugloadProfile getNightProfileHoliday() {
		return nightProfileHoliday;
	}
	public void setNightProfileHoliday(PlugloadProfile nightProfileHoliday) {
		this.nightProfileHoliday = nightProfileHoliday;
	}
	
	
	public PlugloadProfileConfiguration getPlugloadProfileConfiguration() {
		return plugloadProfileConfiguration;
	}
	public void setPlugloadProfileConfiguration(
			PlugloadProfileConfiguration plugloadProfileConfiguration) {
		this.plugloadProfileConfiguration = plugloadProfileConfiguration;
	}
	public Short getProfileChecksum() {
		return profileChecksum;
	}
	public void setProfileChecksum(Short profileChecksum) {
		this.profileChecksum = profileChecksum;
	}
	public Short getGlobalProfileChecksum() {
		return globalProfileChecksum;
	}
	public void setGlobalProfileChecksum(Short globalProfileChecksum) {
		this.globalProfileChecksum = globalProfileChecksum;
	}
	public Short getStandaloneMotionOverride() {
		return standaloneMotionOverride;
	}
	public void setStandaloneMotionOverride(Short standaloneMotionOverride) {
		this.standaloneMotionOverride = standaloneMotionOverride;
	}
	public Byte getDrReactivity() {
		return drReactivity;
	}
	public void setDrReactivity(Byte drReactivity) {
		this.drReactivity = drReactivity;
	}
	public Integer getToOffLinger() {
		return toOffLinger;
	}
	public void setToOffLinger(Integer toOffLinger) {
		this.toOffLinger = toOffLinger;
	}
	public Byte getInitialOnLevel() {
		return initialOnLevel;
	}
	public void setInitialOnLevel(Byte initialOnLevel) {
		this.initialOnLevel = initialOnLevel;
	}
	public Short getProfileGroupId() {
		return profileGroupId;
	}
	public void setProfileGroupId(Short profileGroupId) {
		this.profileGroupId = profileGroupId;
	}
	public Byte getProfileFlag() {
		if (profileFlag == null) {
            return 0;
        }
		return profileFlag;
	}
	public void setProfileFlag(Byte profileFlag) {
		this.profileFlag = profileFlag;
	}
	public Integer getInitialOnTime() {
		return initialOnTime;
	}
	public void setInitialOnTime(Integer initialOnTime) {
		this.initialOnTime = initialOnTime;
	}
	public Byte getDrHighLevel() {
		if (drHighLevel == null)
	        return 0;
		return drHighLevel;
	}
	public void setDrHighLevel(Byte drHighLevel) {
		this.drHighLevel = drHighLevel;
	}
	public Byte getDrModerateLevel() {
		return drModerateLevel;
	}
	public void setDrModerateLevel(Byte drModerateLevel) {
		this.drModerateLevel = drModerateLevel;
	}
	public Byte getDrLowLevel() {
		return drLowLevel;
	}
	public void setDrLowLevel(Byte drLowLevel) {
		this.drLowLevel = drLowLevel;
	}
	public Byte getDrSpecialLevel() {
		return drSpecialLevel;
	}
	public void setDrSpecialLevel(Byte drSpecialLevel) {
		this.drSpecialLevel = drSpecialLevel;
	}
	public Integer getHeartbeatInterval() {
		return heartbeatInterval;
	}
	public void setHeartbeatInterval(Integer heartbeatInterval) {
		this.heartbeatInterval = heartbeatInterval;
	}
	public Integer getHeartbeatLingerPeriod() {
		return heartbeatLingerPeriod;
	}
	public void setHeartbeatLingerPeriod(Integer heartbeatLingerPeriod) {
		this.heartbeatLingerPeriod = heartbeatLingerPeriod;
	}
	
	public Integer getSafetyMode() {
		return safetyMode;
	}
	public void setSafetyMode(Integer safetyMode) {
		this.safetyMode = safetyMode;
	}
	public Integer getNoOfMissedHeartbeats() {
		return noOfMissedHeartbeats;
	}
	public void setNoOfMissedHeartbeats(Integer noOfMissedHeartbeats) {
		this.noOfMissedHeartbeats = noOfMissedHeartbeats;
	}
		
	/**
	 * @return the holidayLevel
	 */
	public Byte getHolidayLevel() {
		if (holidayLevel == null) {
    		return 0;
    	}
		return holidayLevel;
	}
	/**
	 * @param holidayLevel the holidayLevel to set
	 */
	public void setHolidayLevel(Byte holidayLevel) {
		this.holidayLevel = holidayLevel;
	}
	/**
	 * @return the override5
	 */
	public PlugloadProfile getOverride5() {
		return override5;
	}
	/**
	 * @param override5 the override5 to set
	 */
	public void setOverride5(PlugloadProfile override5) {
		this.override5 = override5;
	}
	/**
	 * @return the override6
	 */
	public PlugloadProfile getOverride6() {
		return override6;
	}
	/**
	 * @param override6 the override6 to set
	 */
	public void setOverride6(PlugloadProfile override6) {
		this.override6 = override6;
	}
	/**
	 * @return the override7
	 */
	public PlugloadProfile getOverride7() {
		return override7;
	}
	/**
	 * @param override7 the override7 to set
	 */
	public void setOverride7(PlugloadProfile override7) {
		this.override7 = override7;
	}
	/**
	 * @return the override8
	 */
	public PlugloadProfile getOverride8() {
		return override8;
	}
	/**
	 * @param override8 the override8 to set
	 */
	public void setOverride8(PlugloadProfile override8) {
		this.override8 = override8;
	}


	@XmlElement(name = "profileChecksum")
    private Short profileChecksum;
    @XmlElement(name = "globalProfileChecksum")
    private Short globalProfileChecksum;
    @XmlElement(name = "standaloneMotionOverride")
    private Short standaloneMotionOverride;
    @XmlElement(name = "drReactivity")
    private Byte drReactivity;
    @XmlElement(name = "toOffLinger")
    private Integer toOffLinger;
    @XmlElement(name = "initialOnLevel")
    private Byte initialOnLevel;
    @XmlElement(name = "profileGroupId")
    private Short profileGroupId;
    @XmlElement(name = "profileFlag")
    private Byte profileFlag;
    @XmlElement(name = "initialOnTime")
    private Integer initialOnTime;        
    @XmlElement(name = "drHighLevel")
    private Byte drHighLevel = 0;
    @XmlElement(name = "drModerateLevel")
    private Byte drModerateLevel = 0;
    @XmlElement(name = "drLowLevel")
    private Byte drLowLevel = 0;
    @XmlElement(name = "drSpecialLevel")
    private Byte drSpecialLevel = 0;
    @XmlElement(name = "heartbeatInterval")
    private Integer heartbeatInterval;
    @XmlElement(name = "heartbeatLingerPeriod")
    private Integer heartbeatLingerPeriod;
    @XmlElement(name = "noOfMissedHeartbeats")
    private Integer noOfMissedHeartbeats;
    @XmlElement(name = "safetyMode")
    private Integer safetyMode;

    
    public void copyProfilesFrom(PlugloadProfileHandler target) {
    	//System.out.println("________________ day profile is "+target);
        this.dayProfile.copyFrom(target.getDayProfile());
        this.dayProfileWeekEnd.copyFrom(target.getDayProfileWeekEnd());
        this.eveningProfile.copyFrom(target.getEveningProfile());
        this.eveningProfileWeekEnd.copyFrom(target.getEveningProfileWeekEnd());
        this.morningProfile.copyFrom(target.getMorningProfile());
        this.morningProfileWeekEnd.copyFrom(target.getMorningProfileWeekEnd());
        this.nightProfile.copyFrom(target.getNightProfile());
        this.nightProfileWeekEnd.copyFrom(target.getNightProfileWeekEnd());
        
        //Add holidays too
        this.dayProfileHoliday.copyFrom(target.getDayProfileHoliday());
        this.morningProfileHoliday.copyFrom(target.getMorningProfileHoliday());
        this.eveningProfileHoliday.copyFrom(target.getEveningProfileHoliday());        
        this.nightProfileHoliday.copyFrom(target.getNightProfileHoliday());
        if(target.getOverride5() != null) {
        	this.override5.copyFrom(target.getOverride5());
        }
        if(target.getOverride6() != null) {
        	this.override6.copyFrom(target.getOverride6());
        }
        if(target.getOverride7() != null) {
        	this.override7.copyFrom(target.getOverride7());
        }
        if(target.getOverride8() != null) {
        	this.override8.copyFrom(target.getOverride8());
        }

      //  System.out.println("target"+target.getNightProfileHoliday().getMode()+","+target.getNightProfileHoliday().getId()+","+target.getNightProfileHoliday().getActiveMotion());
    }
    
    public void copyProfiles(PlugloadProfileHandler target){
    	this.morningProfile = target.getMorningProfile();
    	this.dayProfile = target.getDayProfile();
    	this.eveningProfile  = target.getEveningProfile();
    	this.nightProfile = target.getNightProfile();
    //	System.out.println("_____________ morning profile is"+this.morningProfile);
    	this.morningProfileWeekEnd = target.getMorningProfileWeekEnd();
    	this.dayProfileWeekEnd = target.getDayProfileWeekEnd();
    	this.eveningProfileWeekEnd  = target.getEveningProfileWeekEnd();
    	this.nightProfileWeekEnd = target.getNightProfileWeekEnd();
    	
    	this.morningProfileHoliday = target.getMorningProfileHoliday();
    	this.dayProfileHoliday = target.getDayProfileHoliday();
    	this.eveningProfileHoliday  = target.getEveningProfileHoliday();
    	this.nightProfileHoliday = target.getNightProfileHoliday();
    	
    	if(target.getOverride5() != null) {
    		this.override5 = target.getOverride5();
    	}
    	if(target.getOverride6() != null) {
    		this.override6 = target.getOverride6();
    	}
    	if(target.getOverride7() != null) {
    		this.override7 = target.getOverride7();
    	}
    	if(target.getOverride8() != null) {
    		this.override8 = target.getOverride8();
    	}
    }
    
    public void copyPCTimesFrom(PlugloadProfileHandler target) {
    	this.plugloadProfileConfiguration.copyPCTimingsFrom(target.getPlugloadProfileConfiguration());
      //  System.out.println("_______________daytime and weekdays"+target.getPlugloadProfileConfiguration().getDayTime()+" "+target.getPlugloadProfileConfiguration().getWeekDays());
    }
    
    public void copyOverrideProfilesFrom(PlugloadProfileHandler target) {    	
    	this.setDrLowLevel(target.getDrLowLevel());
    	this.setDrModerateLevel(target.getDrModerateLevel());
    	this.setDrHighLevel(target.getDrHighLevel());
    	this.setDrSpecialLevel(target.getDrSpecialLevel());
    }
    
    public void copyAdvancedSettingsFrom(PlugloadProfileHandler target) {    	
    	this.setInitialOnLevel(target.getInitialOnLevel());
    	this.setInitialOnTime(target.getInitialOnTime());
    	this.setSafetyMode(target.getSafetyMode());
    	this.setHeartbeatInterval(target.getHeartbeatInterval());
    	this.setHeartbeatLingerPeriod(target.getHeartbeatLingerPeriod());
    	this.setNoOfMissedHeartbeats(target.getNoOfMissedHeartbeats());
    	this.setProfileFlag(target.getProfileFlag());
    	if(target.getHolidayLevel() != null) {
        	this.setHolidayLevel(target.getHolidayLevel());
        }
    }

}
