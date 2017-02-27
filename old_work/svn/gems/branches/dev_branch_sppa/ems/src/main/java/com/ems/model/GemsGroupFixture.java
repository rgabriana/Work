package com.ems.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class GemsGroupFixture implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = -7847348169567111880L;

    @XmlElement(name = "id")
    private Long id;

    @XmlElement(name = "group")
    private GemsGroup group;

    @XmlElement(name = "fixture")
    private Fixture fixture;
    
    @XmlElement(name = "needSync")
    private Long needSync;
    
    @XmlElement(name = "useraction")
    private Long userAction;
    
    public static final long SYNC_STATUS_UNKNOWN = 0;
    public static final long SYNC_STATUS_GROUP = 2;
    public static final long SYNC_STATUS_GROUP_SYNCD = SYNC_STATUS_GROUP;
    public static final long SYNC_STATUS_SWITCHCONF = 4;
    public static final long SYNC_STATUS_SWITCHCONF_SYNCD = (SYNC_STATUS_GROUP | SYNC_STATUS_SWITCHCONF);
    public static final long SYNC_STATUS_WDSCONF = 8;
    public static final long SYNC_STATUS_ALL = (SYNC_STATUS_GROUP | SYNC_STATUS_SWITCHCONF | SYNC_STATUS_WDSCONF);
    
    public static final long USER_ACTION_DEFAULT = 0;
    public static final long USER_ACTION_FIXTURE_DELETE = 2;
    public static final long USER_ACTION_SCENE_PUSH = 4;
    public static final long USER_ACTION_SCENE_ORDER = 8;
    public static final long USER_ACTION_SCENE_DELETE = 16;
    public static final long USER_ACTION_WDS_REMOVE = 32;
    public static final long USER_ACTION_SWITCH_DELETE = 64;
    
	/**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the gems group
     */
    public GemsGroup getGroup() {
        return group;
    }

    public void setGroup(GemsGroup group) {
        this.group = group;
    }

    /**
     * @return the fixture
     */
    public Fixture getFixture() {
        return fixture;
    }

    public void setFixture(Fixture fixture) {
        this.fixture = fixture;
    }
    
    /**
	 * @return the needSync
	 */
	public Long getNeedSync() {
		return needSync;
	}

	/**
	 * @param needSync the needSync to set
	 */
	public void setNeedSync(Long needSync) {
		this.needSync = needSync;
	}

	/**
	 * @return the userAction
	 */
	public Long getUserAction() {
		return userAction;
	}

	/**
	 * @param userAction the userAction to set
	 */
	public void setUserAction(Long userAction) {
		this.userAction = userAction;
	}
}
