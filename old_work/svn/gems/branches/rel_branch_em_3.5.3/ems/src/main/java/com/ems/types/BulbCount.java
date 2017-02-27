package com.ems.types;

public enum BulbCount {
	
	One(1),Two(2),Three(3),Four(4);
	
	private int bulbCount;
	
	private BulbCount(int count)
	{
		this.setBulbCount(count);
	}

	public int getBulbCount() {
		return bulbCount;
	}

	public void setBulbCount(int bulbCount) {
		this.bulbCount = bulbCount;
	}
}
