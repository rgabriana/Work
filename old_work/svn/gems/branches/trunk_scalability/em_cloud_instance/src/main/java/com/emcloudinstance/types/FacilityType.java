package com.emcloudinstance.types;

public enum FacilityType {
    ROOT, //
    ORGANIZATION, //
    CAMPUS, //
    BUILDING, //
    FLOOR, //
    REGION,
    ZONE,
    AREA, //
    ROOM,
    SITE,
    FIXTURE,
    CUSTOMER,
    GEMS,//
    GROUP; 
    
    public String getName() {
        return this.toString();
    }

    public String getLowerCaseName() {
        return this.toString().toLowerCase();
    }
    
    public static FacilityType getFacilityType(int type) {
   
    	switch(type) {
    	case 0:
    		return ROOT;
    	case 1:
    		return ORGANIZATION;
    	case 2:
    		return CAMPUS;
    	case 3:
    		return BUILDING;
    	case 4:
    		return FLOOR;
    	case 5:
    		return REGION;
    	case 6:
    		return ZONE;
    	case 7:
    		return AREA;
    	case 8:
    		return ROOM;
    	case 9:
    		return SITE;
    	case 10:
    		return FIXTURE;
    	case 11:
    		return CUSTOMER;
    	case 12:
    		return GEMS;
    	case 13:
    		return GROUP;
    	}
    	return ROOT;
    	
    } //end of method getFacilityType
    
    public static int getFacilityType(FacilityType type) {
    	   
    	switch(type) {
    	case ROOT:
    		return 0;
    	case ORGANIZATION:
    		return 1;
    	case CAMPUS:
    		return 2;
    	case BUILDING:
    		return 3;
    	case FLOOR:
    		return 4;
    	case REGION:
    		return 5;
    	case ZONE:
    		return 6;
    	case AREA:
    		return 7;
    	case ROOM:
    		return 8;
    	case SITE:
    		return 9;
    	case FIXTURE:
    		return 10;
    	case CUSTOMER:
    		return 11;
    	case GEMS:
    		return 12;
    	case GROUP:
    		return 13;
    	}
    	return 0;
    	
    } //end of method getFacilityType
 
}
