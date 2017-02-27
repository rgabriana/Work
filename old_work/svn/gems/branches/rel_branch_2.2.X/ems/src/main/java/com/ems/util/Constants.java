package com.ems.util;



public class Constants {


	public static final int PERM_READ = 1;

	public static final int PERM_UPDATE = 2;

	public static final int PERM_ADD = 4;

	public static final int PERM_DELETE = 8;
	
	public static final String SESSION_KEY_USER = "USER";

	public static final String OPERATION_READ = "read";

	public static final String OPERATION_UPDATE = "update";

	public static final String OPERATION_ADD = "add";

	public static final String OPERATION_DELETE = "delete";
	
	public static final String ROLE_ADMIN = "Admin";
	
	public static final String ROLE_AUDITOR = "Auditor";
	
	public static final String ROLE_EMPLOYEE = "Employee";
	
	public static final String XML_PATH = "C:\\Program Files\\Apache Software Foundation\\apache-tomcat-6.0.18\\webapps\\ems\\data\\";
	
	public static final String WEEK_DAY = "weekday";
	
	public static final String WEEK_END = "weekend";
	
	public static final String HOLIDAY = "holiday";
	
	public static final String SEVERITY_CRITICAL = "Critical";
	
	public static final String SEVERITY_WARNING = "Warning";
	
	public static final String SEVERITY_INFORMATIONAL = "Info";
	
	public static final String SEVERITY_MAJOR = "Major";
	
	public static final String SEVERITY_MINOR = "Minor";
	
	public static final String SEVERITY_CLEAR = "CLEAR";
	
	public static final String COMPANY_STRUCTURE_TYPE = "Company";
	
	public static final String CAMPUS_STRUCTURE_TYPE = "Campus";
	
	public static final String BUILDING_STRUCTURE_TYPE = "Building";
	
	public static final String FLOOR_STRUCTURE_TYPE = "Floor";
	
	public static final String AREA_STRUCTURE_TYPE = "Area";
	
	public static final String SUBAREA_STRUCTURE_TYPE = "Sub Area";
	
	public static final String PARENTGROUP_STRUCTURE_TYPE = "ParentGroup";
	
	public static final String GROUPS_STRUCTURE_TYPE = "Groups";
	
	public static final String FIXTURE_STRUCTURE_TYPE = "Fixture";
	
	public static final int RECORDS_PER_PAGE = 20;
	
	public static final int DR_RECORDS_PER_PAGE = 10;
	
	public static final int PRICING_RECORDS_PER_PAGE = 3;
	
	public static final Long DEFAULT_MOTION_SENSITIVITY = 1L;
	
	public static final Long DEFAULT_RAMP_UP_TIME = 0L;

	public static final int DEFAULT_AMBIENT_SENSITIVITY = 5;

	public static final short DEFAULT_STANDALONE_MOTION_OVERRIDE = 0;

	public static final byte DEFAULT_DR_REACTIVITY = 0;
	
}
