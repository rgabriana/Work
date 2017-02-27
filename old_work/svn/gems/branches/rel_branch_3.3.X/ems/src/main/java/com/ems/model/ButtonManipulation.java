package com.ems.model;

import java.io.Serializable;

public class ButtonManipulation implements Serializable {
	
	private static final long serialVersionUID = -8346640146071015941L;
	private Long id;
	private Long buttonMapId;
	private Integer sceneToggleOrder;
	private Long buttonManipAction;

	public ButtonManipulation() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the buttonMapId
	 */
	public Long getButtonMapId() {
		return buttonMapId;
	}

	/**
	 * @param buttonMapId the buttonMapId to set
	 */
	public void setButtonMapId(Long buttonMapId) {
		this.buttonMapId = buttonMapId;
	}

    /**
     * @return the scene_toggle_order
     */
    public Integer getSceneToggleOrder() {
        return sceneToggleOrder;
    }

    /**
     * @param scene_toggle_order the scene_toggle_order to set
     */
    public void setSceneToggleOrder(Integer sceneToggleOrder) {
        this.sceneToggleOrder = sceneToggleOrder;
    }

    /**
     * @return the button_manip_action_table
     */
    public Long getButtonManipAction() {
        return buttonManipAction;
    }

    /**
     * @param buttonManipAction the buttonManipAction to set
     */
    public void setButtonManipAction(Long buttonManipAction) {
        this.buttonManipAction = buttonManipAction;
    }
	
	

}
