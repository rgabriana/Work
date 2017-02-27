package com.ems.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class GemsGroupPlugload {
	
	public static final long SYNC_STATUS_UNKNOWN = 0;
    public static final long SYNC_STATUS_GROUP = 2;
    public static final long SYNC_STATUS_GROUP_NACK = 3;
    public static final long SYNC_STATUS_GROUP_SYNCD = SYNC_STATUS_GROUP;
    public static final long SYNC_STATUS_SWITCHCONF = 4;
    public static final long SYNC_STATUS_SWITCHCONF_NACK = 5;
    public static final long SYNC_STATUS_SWITCHCONF_SYNCD = (SYNC_STATUS_GROUP | SYNC_STATUS_SWITCHCONF);
    public static final long SYNC_STATUS_SWITCHCONF_NOT_SYNCD = (SYNC_STATUS_GROUP_NACK | SYNC_STATUS_SWITCHCONF_NACK);
    public static final long SYNC_STATUS_WDSCONF = 8;
    public static final long SYNC_STATUS_WDSCONF_NACK = 9;
    public static final long SYNC_STATUS_ALL = (SYNC_STATUS_GROUP | SYNC_STATUS_SWITCHCONF | SYNC_STATUS_WDSCONF);
    public static final long SYNC_STATUS_ALL_NACK = (SYNC_STATUS_GROUP_NACK | SYNC_STATUS_SWITCHCONF_NACK | SYNC_STATUS_WDSCONF_NACK);
    
    public static final long USER_ACTION_DEFAULT = 0;
    public static final long USER_ACTION_PLUGLOAD_DELETE = 2;
    public static final long USER_ACTION_MOTION_PARAMS_PUSH = 128;
	
	/*
	 * 
	 *   id bigint NOT NULL,
  group_id bigint NOT NULL,
  plugload_id bigint,
  need_sync bigint,
  user_action bigint,

	 * */
	@XmlElement(name = "id")
	Long id;
	@XmlElement(name = "group")
	GemsGroup  group;
	@XmlElement(name = "plugload")
	Plugload  plugload;
	@XmlElement(name = "needSync")
	Long  needSync;
	@XmlElement(name = "userAction")
	Long  userAction;
	
	@XmlElement(name = "motiongrppgdetails")
  private MotionGroupPlugloadDetails motionGrpPlDetails;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public GemsGroup getGroup() {
		return group;
	}
	public void setGroup(GemsGroup group) {
		this.group = group;
	}
	public Plugload getPlugload() {
		return plugload;
	}
	public void setPlugload(Plugload plugload) {
		this.plugload = plugload;
	}
	public Long getNeedSync() {
		return needSync;
	}
	public void setNeedSync(Long needSync) {
		this.needSync = needSync;
	}
	public Long getUserAction() {
		return userAction;
	}
	public void setUserAction(Long userAction) {
		this.userAction = userAction;
	}
	
	/**
	 * @return the motionGrpPlDetails
	 */
	public MotionGroupPlugloadDetails getMotionGrpPlDetails() {
		return motionGrpPlDetails;
	}
	/**
	 * @param motionGrpPlDetails the motionGrpPlDetails to set
	 */
	public void setMotionGrpPlDetails(MotionGroupPlugloadDetails motionGrpPlDetails) {
		this.motionGrpPlDetails = motionGrpPlDetails;
	}

}
