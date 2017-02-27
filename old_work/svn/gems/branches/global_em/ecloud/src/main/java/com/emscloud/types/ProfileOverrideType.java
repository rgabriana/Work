package com.emscloud.types;

public enum ProfileOverrideType {
	
	No_Override(0), Override1(1), Override2(2), Override3(3), Override4(4);
	
	private int id ;
	private ProfileOverrideType(int id) {
		// TODO Auto-generated constructor stub
		this.setId(id);
	}
	public String getName() {
		return this.name().replaceAll("_", "");
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

}
