package com.ems.types;

public enum BallastCount {
	
	One(1),Two(2),Three(3),Four(4);
	
	private int ballastCount;

	private BallastCount(int count)
	{
		this.setBallastCount(count);
	}
	
	public int getBallastCount() {
		return ballastCount;
	}

	public void setBallastCount(int ballastCount) {
		this.ballastCount = ballastCount;
	}
	
	

}
