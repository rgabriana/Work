package com.communicator.model.vo;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ClientToServerVO implements IData {
	@XmlElement(name = "company")
	private List<CompanyVO> company = null;
	@XmlElement(name = "campus")
	private List<CampusVO> campus = null;
	@XmlElement(name = "building")
	private List<BuildingVO> building = null;
	@XmlElement(name = "floor")
	private List<FloorVO> floor = null;
	@XmlElement(name = "area")
	private List<AreaVO> area = null;
	@XmlElement(name = "fixture")
	private List<FixtureVO> fixture = null;
	@XmlElement(name = "gateway")
	private List<GatewayVO> gateway = null;
	@XmlElement(name = "macAddress")
	private String macAddress;
	@XmlElement(name = "version")
	private String version;
	
	public ClientToServerVO() {
	}
	
	public ClientToServerVO(List<CompanyVO> company, List<CampusVO> campus, List<BuildingVO> building,
			List<FloorVO> floor, List<AreaVO> area, List<FixtureVO> fixture,
			List<GatewayVO> gateway, String macAddress, String version) {
		this.setCompany(company);
		this.setCampus(campus);
		this.building = building;
		this.floor = floor;
		this.area = area;
		this.fixture = fixture;
		this.gateway = gateway;
		this.macAddress = macAddress;
		this.version = version;
	}

	/**
	 * @return the macAddress
	 */
	public String getMacAddress() {
		return macAddress;
	}

	/**
	 * @param macAddress the macAddress to set
	 */
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the company
	 */
	public List<CompanyVO> getCompany() {
		return company;
	}

	/**
	 * @param company the company to set
	 */
	public void setCompany(List<CompanyVO> company) {
		this.company = company;
	}

	/**
	 * @return the campus
	 */
	public List<CampusVO> getCampus() {
		return campus;
	}

	/**
	 * @param campus the campus to set
	 */
	public void setCampus(List<CampusVO> campus) {
		this.campus = campus;
	}

	/**
	 * @return the building
	 */
	public List<BuildingVO> getBuilding() {
		return building;
	}

	/**
	 * @param building the building to set
	 */
	public void setBuilding(List<BuildingVO> building) {
		this.building = building;
	}

	/**
	 * @return the floor
	 */
	public List<FloorVO> getFloor() {
		return floor;
	}

	/**
	 * @param floor the floor to set
	 */
	public void setFloor(List<FloorVO> floor) {
		this.floor = floor;
	}

	/**
	 * @return the area
	 */
	public List<AreaVO> getArea() {
		return area;
	}

	/**
	 * @param area the area to set
	 */
	public void setArea(List<AreaVO> area) {
		this.area = area;
	}

	/**
	 * @return the fixture
	 */
	public List<FixtureVO> getFixture() {
		return fixture;
	}

	/**
	 * @param fixture the fixture to set
	 */
	public void setFixture(List<FixtureVO> fixture) {
		this.fixture = fixture;
	}

	/**
	 * @return the gateway
	 */
	public List<GatewayVO> getGateway() {
		return gateway;
	}

	/**
	 * @param gateway the gateway to set
	 */
	public void setGateway(List<GatewayVO> gateway) {
		this.gateway = gateway;
	}
}
