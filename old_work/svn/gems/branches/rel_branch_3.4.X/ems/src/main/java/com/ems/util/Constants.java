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
	
	public static final byte MAX_LOGIN_ATTEMPTS = 3;
	public static final byte MAX_LOGIN_ATTEMPTS_ADMINS = 5;
	
	public static final String NO_LOGIN_ATTEMPT_COLUMN = "no_login_attempts";
	
	public static final String PASSWORD_COLUMN = "password";
	
	public static final String EXCLUDE_PATH = "/services/org/public/nonsecure";
	
	public static final String IDENTIFIER_FORGOT_PASS = "identifier_forgot_password";
	
	private static final String SYS_PATH = System.getProperty("catalina.base");
	public static final String EM_PUB_FILE = SYS_PATH+"/Enlighted/em_public.key";//"/var/lib/tomcat6/Enlighted/em_public.key";
	public static final String EM_PVT_FILE = SYS_PATH+"/Enlighted/em_private.key";//"/var/lib/tomcat6/Enlighted/em_private.key";
	public static final String SUPP_PUB_FILE = SYS_PATH+"/Enlighted/supp_public.key";//"/var/lib/tomcat6/Enlighted/supp_public.key";
	public static final String SUPP_PVT_FILE = SYS_PATH+"/Enlighted/supp_private.key";//"/var/lib/tomcat6/Enlighted/supp_private.key";
	public static final String FORGOT_PW_FILE_EXTENSIONS_ALLOWED = "key,txt,doc,html,pem,der";
	public static final String XSD_SENSOR = SYS_PATH+"/Enlighted/sensor_profile.xsd";//"/var/lib/tomcat6/Enlighted/sensor_profile.xsd";
	public static final String XSD_PLUGLOAD = SYS_PATH+"/Enlighted/plugload_profile.xsd";//"/var/lib/tomcat6/Enlighted/plugload_profile.xsd";
	public static final String SUCCESS_UPLOAD_PROFILE= "message.uploadprofile";
	public static final String ERROR_UPLOAD_PROFILE= "error.uploadprofile";
	public static final String COMMA = ",";
	public static final String FORGOT_REQ_ATTRIBUTE  = "forgotpasssecurity";
	public static final String NETWK_INTERFACE_ETH0="eth0";
	public static final String DEFAULT_DELAY_LOGIN_UNSUCCESS="default.delay.unsuccessful.loginattempt";
	public static final String ADMIN = "admin";
	public static final String PASSWORD_CHANGED_IDENTIFIER = "PASSWORD_CHANGED_BY_ADMIN";
	public static final String ERROR_LOGIN_ATTEMPTS = "error.login.incorrect";
	public static final String ERROR_LOGIN_ATTEMPTS_GENERAL = "error.login.incorrect.general";
	public static final String USB_ERROR_FORGOTPASS = "error.forgotpassword.reset";
	public static final String ADMIN_LOCKED_MESSAGE="error.login.LoginAttemptsException.admin";
	public static final String DEFAULT_DELAY_ADMIN_ATTRIB = "defaultDelayTimeInMillis";
	public static final String ADMIN_LOCK_PERIOD_HOURS = "hours.locking.period.admin";
	public static final String DATE_FORMAT_READABLE = "ddMMMyyyy, hh:mm:ss a";
	public static final String UTF_8 = "utf-8";
	public static final String TEXT_HTML = "text/html";
	public static final String ALGORITHM_LOCAL = "PBEWithMD5AndDES";
	public static final String INACTIVE_ACCT="error.login.inactive";
	public static final String FORGOT_PASS_TEMP_SPLITTER="###";
	public static final String FORGOT_PASS_TIMEZONE_SPLITTER="&&&";
	public static final String FORGOT_PASS_EXPIRED="error.forgot.pass.expired";
	public static final String FORGOT_DATE_PATTERN="yyyy-MM-dd:hh:mm:ss";
	public static final String EXTERNAL_USER_NOT_ALLOWED="error.ldap.notallowed";
}
