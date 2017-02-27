package com.ems.model;

import java.io.Serializable;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
public class FixtureGroup implements Serializable {

    private static final long serialVersionUID = 6652565521615318490L;
    private Long id;
    private Groups group;
    private Fixture fixture;

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
     * @return the group
     */
    public Groups getGroup() {
        return group;
    }

    /**
     * @param group
     *            the group to set
     */
    public void setGroup(Groups group) {
        this.group = group;
    }

    /**
     * @return the fixture
     */
    public Fixture getFixture() {
        return fixture;
    }

    /**
     * @param fixture
     *            the fixture to set
     */
    public void setFixture(Fixture fixture) {
        this.fixture = fixture;
    }
}
