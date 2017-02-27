package com.ems.types;

public enum VoltageLevels {
	
	OneHundreadTwenty(120), TwoHundereadSeventySeven(277);
	
	private int voltage;
	
	private VoltageLevels(int voltage)
	{
		this.setVoltage(voltage);
	}

	public int getVoltage() {
		return voltage;
	}

	public void setVoltage(int voltage) {
		this.voltage = voltage;
	}
	


}
