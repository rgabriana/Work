package com.ems.types;

public enum ProfileOverrideType {
	
	No_Override(0), Override1(1), Override2(2), Override3(3), Override4(4), Override5(5), Override6(6), Override7(7), Override8(8);
	
	private int id ;
	private ProfileOverrideType(int id) {
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
