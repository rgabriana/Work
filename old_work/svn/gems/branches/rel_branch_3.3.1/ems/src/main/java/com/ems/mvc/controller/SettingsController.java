package com.ems.mvc.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ems.filter.ExternalRestApiValidationFilter;
import com.ems.hvac.utils.CryptographyUtil;
import com.ems.model.Ballast;
import com.ems.model.BallastVoltPower;
import com.ems.model.Bulb;
import com.ems.model.Company;
import com.ems.model.EmailConfiguration;
import com.ems.model.FixtureClass;
import com.ems.model.LampCalibrationConfiguration;
import com.ems.model.LdapSettings;
import com.ems.model.SystemConfiguration;
import com.ems.mvc.util.ControllerUtils;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.ServerMain;
import com.ems.service.BallastManager;
import com.ems.service.BallastVoltPowerManager;
import com.ems.service.BulbManager;
import com.ems.service.CloudServerManager;
import com.ems.service.CommunicatorManager;
import com.ems.service.CompanyManager;
import com.ems.service.EmailManager;
import com.ems.service.FacilityTreeManager;
import com.ems.service.FixtureCalibrationManager;
import com.ems.service.FixtureClassManager;
import com.ems.service.FixtureManager;
import com.ems.service.GroupManager;
import com.ems.service.LdapSettingsManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.BallastCount;
import com.ems.types.BulbCount;
import com.ems.types.UserAuditActionType;
import com.ems.types.VoltageLevels;
import com.ems.util.Constants;
import com.ems.util.HardwareInfoUtils;
import com.ems.utils.AdminUtil;
import com.ems.utils.ArgumentUtils;
import com.ems.vo.AuthenticationType;
import com.ems.vo.MasterGemsSetting;
import com.ems.vo.model.FileUpload;
import com.ems.vo.model.RestApiConfigurationModel;
import com.enlightedinc.keyutil.EnlightedKeyGenerator;

@Controller
@RequestMapping("/settings")
@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
public class SettingsController {
	
	private static final Logger log = Logger.getLogger(SettingsController.class);
	static final long restApiKeyFileSizeLimit  = 1048576;

	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	@Resource
	CommunicatorManager communicatorManager;
    @Resource
    SystemConfigurationManager systemConfigurationManager;
    @Resource(name="facilityTreeManager")
    FacilityTreeManager facilityTreeManager;
    @Resource(name = "fixtureManager")
    private FixtureManager fixtureManager;
    @Resource(name = "groupManager")
    private GroupManager groupManager;
    @Resource(name = "ldapSettingsManager")
    private LdapSettingsManager ldapSettingsManager;
    @Resource(name = "cloudServerManager")
    private CloudServerManager cloudServerManager;
    
    @Resource(name = "ballastManager")
    private BallastManager ballastManager;
    
    @Resource(name = "bulbManager")
    private BulbManager bulbManager;
    
    @Resource(name = "fixtureClassManager")
    private FixtureClassManager fixtureClassManager;
    
    @Resource(name = "ballastVoltPowerManager")
    private BallastVoltPowerManager ballastVoltPowerManager;

	@Resource
    FixtureCalibrationManager fixtureCalibrationManager;
	
	@Resource(name="emailManager")
	private EmailManager emailManager;
	
    @Autowired
    private MessageSource messageSource;
    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping("/ldap.ems")
    String getListOfUsers(Model model) {

    
        return "settings/ldap_settings";
    }

    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping("/ldap/save.ems")
    String saveLdapSettings( AuthenticationType authenticationType) {
        SystemConfiguration authTypeConfig = systemConfigurationManager.loadConfigByName("auth.auth_type");
        if (authTypeConfig != null) {
            authTypeConfig.setValue( authenticationType.getAuthenticationType());
            systemConfigurationManager.update(authTypeConfig);
        }

      
        
        return "settings/ldap_login_settings";
    }
    
    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping("/ldap/server/save.ems")
    String saveLdapServerSettings( Model model, LdapSettings ldapSettings) {
    	ldapSettingsManager.save(ldapSettings);
    	model.addAttribute("status", "success");
        return "settings/ldap_server_settings";
    }
    
    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping("/ldap/server_setting.ems")
    String loadLdapServerSettings(Model model) {
    	 LdapSettings ldapSettings= ldapSettingsManager.loadById(1l);
    	 if(ldapSettings == null)
    	 {
    		 ldapSettings = new LdapSettings() ;
    	 }
         model.addAttribute("ldapSettings", ldapSettings);
        userAuditLoggerUtil.log("Update LDAP Server Settings", UserAuditActionType.LDAP_Update.getName());
        
        return "settings/ldap_server_settings";
    }
    
    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping("/ldap/login_setting.ems")
    String loadLdapLoginSettings(Model model) {
        AuthenticationType authenticationType = new AuthenticationType();
        model.addAttribute("authenticationType", authenticationType);

        SystemConfiguration authTypeConfig = systemConfigurationManager.loadConfigByName("auth.auth_type");
        if (authTypeConfig != null) {
        	authenticationType.setAuthenticationType(authTypeConfig.getValue());
        }
        userAuditLoggerUtil.log("Update LDAP Server Settings", UserAuditActionType.LDAP_Update.getName());
        
        return "settings/ldap_login_settings";
    }
    
    
    @RequestMapping(value = "/system_management.ems")
    String cleanup() {
    	return "settings/system_management";
    }
    
    @RequestMapping(value = "/emailconfig.ems")
    String emailconfig(Model model) {
    	
    	EmailConfiguration config = emailManager.loadEmailConfig();
    	if(config == null ){
    		config = new EmailConfiguration();
    	}
    	config.setPass(CryptographyUtil.getLocalDecryptedString(config.getPass(),Constants.ALGORITHM_LOCAL));
    	model.addAttribute("emailModel", config);
         return "settings/emailconfig";
    }
    
    @RequestMapping(value = "/saveemailconfig.ems")
    String saveemailconfig(Model model,EmailConfiguration config) {
    	
    	 config.setPass(CryptographyUtil.getLocalEncryptedString(config.getPass(),Constants.ALGORITHM_LOCAL));
    	 emailManager.save(config);
    	 model.addAttribute("RESULT", "Email Configuration Updated Successfully.");
    	 config = emailManager.loadEmailConfig();
     	if(config == null ){
     		config = new EmailConfiguration();
     	}else{
	     	//Call the shell and execute it.
	     	try {
				String[] cmdArr = { "bash", ServerMain.getInstance().getTomcatLocation() + "adminscripts/enablePort.sh", config.getPort() };
				final Process pr = Runtime.getRuntime().exec(cmdArr);
				AdminUtil.readStreamOfProcess(pr);
				pr.waitFor();
			} catch (Exception e) {
				log.error("ERROR: Not able to enable the port "+ config.getPort(), e);
			}
	        config.setPass(CryptographyUtil.getLocalDecryptedString(config.getPass(),Constants.ALGORITHM_LOCAL));
     	}
     	model.addAttribute("emailModel", config);
         return "settings/emailconfig";
    }
    
    @RequestMapping(value = "/cleanUp.ems")
    String cleanSystem(Model model) {
         return "settings/cleanUpSystem";
    }
   
    
    @RequestMapping(value = "/groups.ems")
    String groupManagement() {
        return "settings/groups";
    }
    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping(value = "/restApi.ems")
    String restApiManagement(Model model) {
    	RestApiConfigurationModel restModel = new RestApiConfigurationModel() ;	
    	SystemConfiguration systemconfig = systemConfigurationManager.loadConfigByName("rest.api.key") ;
   	 if( systemconfig.getValue() !=null && !systemconfig.getValue().isEmpty() )
   	 {
   		byte[] filecontent= null ;
   		String dest = ServerMain.getInstance().getTomcatLocation() + "../../Enlighted/" ;
   		filecontent = ExternalRestApiValidationFilter.readFromFile( dest+ControllerUtils.getTheLicenseFileName(".enlighted", dest)) ;
   	 if(validated(filecontent, restModel))
   	 {
   		 		
   		  restModel.setValidated(true);
   	 }
   	 else
   	 {
   		restModel.setValidated(false);
   		model.addAttribute("restModel", restModel);
   		systemconfig.setValue(null);
   		systemConfigurationManager.save(systemconfig) ;
   		return "redirect:/settings/restApi.ems?expired=true" ;
   	 }
   	 }
   	 else{
   		 	restModel.setValidated(false);
   	 }
        model.addAttribute("restModel", restModel);
        return "settings/restApi";
    }
    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping(value = "/bulbslist.ems")
    String bulbsManagement(Model model) {
    	return "settings/bulblist";
    }
    
    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping(value = "/masterfixtureclasslist.ems")
    String masterFixtureClassManagement(Model model) {
    	return "settings/masterfixtureclass";
    }   

 @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping(value = "/addbulb.ems")
    String addBulb(Model model,@RequestParam("from") String from) {    	
    	Bulb bulb = new Bulb();
    	model.addAttribute("bulb",bulb);
    	model.addAttribute("mode","add");
    	
    	if(from.equalsIgnoreCase("fixtureclass"))
    	{
    		model.addAttribute("from","fixtureclass");
    	}
    	return "settings/addbulb";
    }
    
    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping(value = "/editbulb.ems")
    String editBulb(@RequestParam("bulbId") Long bulbId,Model model) {
    	Bulb bulb = bulbManager.getBulbById(bulbId);
    	model.addAttribute("bulb", bulb);
    	model.addAttribute("mode","edit");   	
    	return "settings/editbulb";
    }
    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping(value = "/ballastlist.ems")
    String ballastManagement(Model model) {
    	return "settings/ballastlist";
    }
    
    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping(value = "/addballast.ems")
    String addBallast(Model model,@RequestParam("from") String from) {
    	Ballast ballast = new Ballast();
    	model.addAttribute("ballast", ballast);
    	model.addAttribute("mode","add");
    	model.addAttribute("bulbcount",BulbCount.values());
    	
    	if(from.equalsIgnoreCase("fixtureclass"))
    	{
    		model.addAttribute("from","fixtureclass");
    	}
    	return "settings/addballast";
    }
    
    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping(value = "/editballast.ems")
    String editBallast(@RequestParam("ballastId") Long ballastId,Model model) {
    	Ballast ballast = ballastManager.getBallastById(ballastId);
    	model.addAttribute("ballast", ballast);
    	model.addAttribute("mode","edit");
    	model.addAttribute("bulbcount",BulbCount.values());
    	return "settings/editballast";
    }
    
    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping(value = "/fixtureclasslist.ems")
    String FixtureClassManagement(Model model) {
    	return "settings/fixtureclasslist";
    }
    
    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping(value = "/addfixtureclass.ems")
    String addFixtureClass(Model model, @RequestParam("page") String page) {
    	FixtureClass fixtureClass = new FixtureClass();
    	model.addAttribute("fixtureClass", fixtureClass);
    	model.addAttribute("ballasts", ballastManager.getAllBallasts());
    	model.addAttribute("bulbs", bulbManager.getAllBulbs());
    	model.addAttribute("mode","add");
    	model.addAttribute("voltagelevels",VoltageLevels.values());
    	model.addAttribute("ballastcount",BallastCount.values());
    	
    	Ballast ballast = new Ballast();
    	model.addAttribute("ballast", ballast);    	
    	model.addAttribute("ballastmode", "ballastadd");
    	model.addAttribute("bulbmode", "bulbadd");
    	
    	model.addAttribute("page", page);
    	
    	return "settings/addfixtureclass";
    }
    
    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping(value = "/editfixtureclass.ems")
    String editFixtureClass(Model model,@RequestParam("fixtureClassId") Long fixtureClassId,@RequestParam("page") String page) {
    	FixtureClass fixtureClass = fixtureClassManager.getFixtureClassById(fixtureClassId);
    	model.addAttribute("fixtureClass", fixtureClass);
    	model.addAttribute("ballasts", ballastManager.getAllBallasts());
    	model.addAttribute("ballastId", fixtureClass.getBallast().getId());
    	model.addAttribute("bulbs", bulbManager.getAllBulbs());
    	model.addAttribute("bulbId",fixtureClass.getBulb().getId());
    	model.addAttribute("mode","edit");
    	model.addAttribute("voltagelevels",VoltageLevels.values());
    	model.addAttribute("ballastcount",BallastCount.values());
    	
    	model.addAttribute("page", page);
    	
    	return "settings/editfixtureclass";
    }
    
    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping(value = "/editsystemconfig.ems")
    String editSystemConfig(Model model) {
    	return "settings/editsystemconfig";
    }
    
    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping(value = "/editfloorplanconfig.ems")
    String editFloorPlanSizeConfig(Model model) {
    	SystemConfiguration systemconfig = systemConfigurationManager.loadConfigByName("floorplan.imagesize.limit") ;
    	model.addAttribute("floorplansize", systemconfig.getValue());
    	return "settings/floorplanimageconfig";
    }
    
    @RequestMapping(value = "/validate.ems")
    String restApiValidation( Model model, RestApiConfigurationModel restModel) {
    	try{
    	byte[] filecontent= null ;
    	String uploadSatus = "false" ;
    	String fileName="";
    	String dest = ServerMain.getInstance().getTomcatLocation() + "../../Enlighted/" ;
    	SystemConfiguration systemconfig = systemConfigurationManager.loadConfigByName("rest.api.key") ;
    	FileUpload file = restModel.getLicenseFile();
    	 
		if(file.getFile().getSize()==0){
			uploadSatus="false" ;
			 model.addAttribute("restModel", restModel);
			return "redirect:/settings/restApi.ems?uploadSatus="+uploadSatus ;
			
		}
		else if(file.getFile().getSize() > restApiKeyFileSizeLimit){
			uploadSatus="filesizeFail" ;
			model.addAttribute("restModel", restModel);
			return "redirect:/settings/restApi.ems?uploadSatus="+uploadSatus ;
		}
		else
		{	
			MultipartFile multipartFile = file.getFile();
			fileName = multipartFile.getOriginalFilename();
			 File licenseFile = new File(dest+fileName);
			 if(licenseFile.exists())
			 {
				 licenseFile.delete() ;
				 try {
					licenseFile.createNewFile() ;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }
			
			
			if(multipartFile!=null){
				
				 try {
					multipartFile.transferTo(licenseFile);
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		
    	filecontent = ExternalRestApiValidationFilter.readFromFile( dest+fileName) ;
    	 if(systemconfig!=null&&validated(filecontent, restModel))
    	 {
    		  systemconfig.setValue(restModel.getApiKey()) ;
    		  systemConfigurationManager.save(systemconfig) ;
    		  restModel.setValidated(true);
    		  restModel.setApiKey("") ;
    		  uploadSatus="true" ;
    	 }
    	 else
    	 {
    		 restModel.setValidated(false);
 			systemconfig.setValue(null);
 			 model.addAttribute("restModel", restModel);
 			 systemConfigurationManager.save(systemconfig) ;
     	        return "redirect:/settings/restApi.ems?uploadSatus="+"false" ;
    	 }
    	 
        model.addAttribute("restModel", restModel);
        uploadSatus="true" ;
        return "redirect:/settings/restApi.ems?uploadSatus="+uploadSatus ;
		}
    	}
    	catch(Exception e)
    	{
    		restModel.setValidated(false);
    		SystemConfiguration systemconfig = systemConfigurationManager.loadConfigByName("rest.api.key") ;
			systemconfig.setValue("");
			 model.addAttribute("restModel", restModel);
			 systemConfigurationManager.save(systemconfig) ;
    	        return "redirect:/settings/restApi.ems?uploadSatus="+"false" ;
    	}
    }
    
   private boolean validated(byte[] key, RestApiConfigurationModel restModel)
    {
  
    	boolean valid = false ;
    	
    	EnlightedKeyGenerator keyGenerator =  EnlightedKeyGenerator.getInstance() ;	
    	 keyGenerator.setEncrptedApiKey(key) ;
    	 byte[] mac = HardwareInfoUtils.getMacAddressForIp(ServerMain.getInstance().getIpAddress("eth0"));
    	 String macString = HardwareInfoUtils.macBytetoString(':',mac);	
    	 try {
    		 	macString =macString.toLowerCase();
    		 	byte[] salt = macString.getBytes("UTF-8") ;
    		 	keyGenerator.setSeceretKey(salt);
    	 	} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
    	 	}
    	 String decryptedString = keyGenerator.doDecryption() ;
    	 if(!ArgumentUtils.isNullOrEmpty(decryptedString))
    	 {
	    	 String keyMac[] = decryptedString.split("\\|") ;
	    	
	    	 if(keyMac[0].equalsIgnoreCase(macString))
	    	 {
	    		 restModel.setApiKey(new String(key)) ;
	    		 if(ExternalRestApiValidationFilter.checkDateValidation(keyMac))
	    		 {
	    		 valid=true ; 
	    		 }
	    	 }
    	 }
			
    	return valid ;
    }
 
    @RequestMapping(value = "/invalidate.ems")
    String restApiInValidation() {
    	byte[] filecontent= null ;
    	String type = "false" ;
    	SystemConfiguration systemconfig = systemConfigurationManager.loadConfigByName("rest.api.key") ;
    	 if(systemconfig!=null)
    	 {
    		  systemconfig.setValue("") ;
    		  systemConfigurationManager.save(systemconfig) ;
    		  String dest =ServerMain.getInstance().getTomcatLocation() + "../../Enlighted/" ;
    		  String fNameString =  ControllerUtils.getTheLicenseFileName(".enlighted", dest);
    		  if(!ArgumentUtils.isNullOrEmpty(fNameString))
    		  {
    			  
	    		  File licenseFile = new File(dest+fNameString) ;
	    	    	 if(licenseFile.exists())
	    	    	 {
	    	    		 licenseFile.delete() ;
	    	    		 type="true" ;
	    	    	 }
	    	    	 
    		  }else
 	    	 {
 	    		 type="noFile" ;
 	    	 }
    		  
    	 }
    	
    	 
        return "redirect:/settings/restApi.ems?status="+type;
    }
    @RequestMapping(value = "/cleancache.ems")
    String cleancache() {
    	fixtureManager.clearBallastVoltPowersMap();
    	facilityTreeManager.inValidateFacilitiesTreeCache();
    	groupManager.inValidateProfilesTreeCache();
    	return "redirect:/settings/cleanUp.ems";
    }
    @RequestMapping(value = "/master_gems_setting.ems")
    String masterGemsSetting(Model model) {
    	MasterGemsSetting masterGemsSetting = communicatorManager.loadConfiguration();
    	if(masterGemsSetting==null)
    	{
    		masterGemsSetting = new MasterGemsSetting() ;
    	}
    	   
          model.addAttribute("masterGemsSetting", masterGemsSetting);
    	return "settings/master_gems_settings";
    }
    @RequestMapping(value = "/master_gems_setting/save.ems")
    String saveMasterGemsSetting(MasterGemsSetting masterGemsSetting) {
    	
    	communicatorManager.setConfiguration(masterGemsSetting);
    	return "redirect:/settings/cleanUp.ems";
    }
    @PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping("/bulbConfiguration.ems")
    public String loadBulbConfigurationDetail(Model model) {
		LampCalibrationConfiguration lampCalibrationConf = fixtureCalibrationManager.getCalibrationConfiguration();
        model.addAttribute("lampCalibrationConf", lampCalibrationConf);
        return "settings/bulbconfiguration";
    }
	
	@PreAuthorize("hasAnyRole('Admin')")
    @RequestMapping("/updateLampConfiguration.ems")
	@ResponseBody
    public String updateLampConfiguration(LampCalibrationConfiguration lcm,Locale local) {
		String responseMsg="";
		Boolean status = true;
		try {
			LampCalibrationConfiguration oriLcm = fixtureCalibrationManager.getCalibrationConfiguration();
			oriLcm.setWarmupTime(lcm.getWarmupTime());
			oriLcm.setStabilizationTime(lcm.getStabilizationTime());
			oriLcm.setPotentialDegradeThreshold(lcm.getPotentialDegradeThreshold());
			fixtureCalibrationManager.updateLampCalibrationConfiguration(oriLcm);
		} catch (SQLException e) {
			status = false;
			responseMsg ="Lamp Configuration Failed to Update.";
		} catch (IOException e) {
			status = false;
			responseMsg ="Lamp Configuration Failed to Update.";
		}
		if(status)
		{
			return "{\"success\":1, \"message\" : \""
					+ messageSource.getMessage("lampCalibrationForm.message.success", null, local) + "\"}";
		}else
		{
			return "{\"Fail\":-1, \"message\" : \""
					+ responseMsg + "\"}";
		}
    }
    @PreAuthorize("hasAnyRole('Admin')")
	 @RequestMapping("/importvoltpowercurve.ems")
	 public String importVoltPowerCurveDialog(@RequestParam("ballastId") Long ballastId,Model model) {
	        model.addAttribute("ballastId", ballastId);
	        return "settings/importvoltpowercurveDialog";
	 }
    
    @PreAuthorize("hasAnyRole('Admin')")
	 @RequestMapping("/showpowermaps.ems")
	 public String showPowerMapsDialog(@RequestParam("ballastId") Long ballastId,Model model) {
    		List<Double> powerMapVoltageList = ballastVoltPowerManager.getVoltageLevelsByBallastId(ballastId); 
    		Ballast ballast = ballastManager.getBallastById(ballastId);
	        model.addAttribute("ballastId", ballastId);
	        model.addAttribute("ballastType",ballast.getBallastName());
	        String displayLampStr = ballast.getLampNum() + "," + ballast.getWattage() +"W" + ","+ ballast.getLampType();
	        model.addAttribute("lamps",displayLampStr);
	        model.addAttribute("ballastvoltagelevels", powerMapVoltageList);
	        model.addAttribute("lampManufacturer",ballast.getBallastManufacturer());
	        model.addAttribute("firstVoltageLevel", powerMapVoltageList.get(0));
	        return "settings/showpowermapsDialog";
	 }
    
    @RequestMapping("/uploadBallastCSVFile.ems")	
	public String uploadImageFile(HttpServletRequest request,
			@RequestParam("upload") MultipartFile file,				
			@RequestParam("voltage") Double voltage,
			@RequestParam("ballastid") Long ballastId, Model model,
			Locale locale) {

    	Double maxVoltValue = new Double(100);
		Double minVoltValue = new Double(0);
		Boolean successFlag = true;
		Integer invalidEntriesCount = 0;
		Integer totalCommentsCnt = 6;
		Map<Double, Double> databaseVoltPowerMap = new HashMap<Double, Double>();
		Map<Double,Boolean> databaseVoltEnableMap = new HashMap<Double, Boolean>();
		Map<Double, Double> csvVoltPowerMap = new HashMap<Double, Double>();
		Map<Double,Boolean> csvVoltEnableMap = new HashMap<Double, Boolean>();
		List<BallastVoltPower> ballastVoltPower = null;
		if(file != null && voltage != null && ballastId != null) {
		try {
			InputStream iReader = file.getInputStream();			
			
			BufferedReader br = new BufferedReader(new InputStreamReader(iReader));
			String line = "";
			int lineCounter = 0;
			boolean itsHeader=false;
				while ((line = br.readLine()) != null) {
					// System.out.println("line ------->" + line +
					// " line.length() == >" + line.length());
					boolean isComments = line.startsWith("#");
					String[] columnCount = line.split(",");
					if (itsHeader == false)
						if (lineCounter == 0 && isComments == false) {
							// Check for headers presence when comments are not
							// there.
							for (int i = 0; i < columnCount.length; i++) {
								try {
									Double d = Double
											.parseDouble(columnCount[i]);
									// It's a number return back to UI
									successFlag = false;
									model.addAttribute("successflag",
											successFlag);
									return "settings/ballastlist";
								} catch (NumberFormatException nfe) {

								}
							}
							itsHeader = true;
							lineCounter++;
							continue;
						}
					// CSV File with comments
					if (isComments == true) {
						lineCounter++;
						continue;
					}					
					if (itsHeader == false)
						if (lineCounter > 0 && isComments == false) {
							// this has to be the comment for the first time
							for (int i = 0; i < columnCount.length; i++) {
								try {
									Double d = Double
											.parseDouble(columnCount[i]);
									// It's a number return back to UI
									successFlag = false;
									model.addAttribute("successflag",
											successFlag);
									return "settings/ballastlist";
								} catch (NumberFormatException nfe) {

								}
							}
							itsHeader = true;
							continue;
						}
					String[] valuesArray = line.split(",");
					Double volt = Double.parseDouble(valuesArray[0]);
					Double power = Double.parseDouble(valuesArray[1]);
					Integer enableValue = Integer.parseInt(valuesArray[2]);
					Boolean enableFlag = false;
					if(enableValue.compareTo(new Integer(1))==0)
					{
						enableFlag = true;						
					}
					else
					{
						enableFlag = false;						
					}					
					if (volt.compareTo(minVoltValue) >= 0
							&& volt.compareTo(maxVoltValue) <= 0) // Condition
																	// check for
																	// min and
																	// max volts
					{
						String doubleAsText = volt.toString();
						Integer decimal = Integer.parseInt(doubleAsText
								.split("\\.")[0]);
						Integer fractional = Integer.parseInt(doubleAsText
								.split("\\.")[1]);

						if (decimal == null) {
							decimal = Integer.parseInt(volt.toString());
						} else if (fractional == null) {
							fractional = 0;
						}

						if ((decimal % 5 == 0) && fractional.compareTo(0) == 0) {
							csvVoltPowerMap.put(volt / 10, power);
							csvVoltEnableMap.put(volt / 10, enableFlag);
						} else {
							invalidEntriesCount++;
						}
					} else {
						invalidEntriesCount++;
					}
				}

				if (invalidEntriesCount != 0) {
					successFlag = false;
					model.addAttribute("successflag", successFlag);
					model.addAttribute("invalidfileentries",
							invalidEntriesCount);
					return "settings/ballastlist";
				}

				ballastVoltPower = ballastVoltPowerManager
						.getBallastVoltPowerByBallastIdInputVoltage(ballastId,
								voltage);
				if (ballastVoltPower != null && ballastVoltPower.size() > 0) {
					for (Iterator<BallastVoltPower> iterator = ballastVoltPower
							.iterator(); iterator.hasNext();) {
						BallastVoltPower ballastVoltPowerObj = (BallastVoltPower) iterator
								.next();
						databaseVoltPowerMap.put(ballastVoltPowerObj.getVolt(),
								ballastVoltPowerObj.getPower());
						databaseVoltEnableMap.put(ballastVoltPowerObj.getVolt(), ballastVoltPowerObj.getEnabled());
					}
				}

				Long maxVoltPowerMapId = fixtureManager.getMaxVoltPowerMapId() + 1;
				// Check for already does not exist and insert them

				for (Map.Entry<Double, Double> entry : csvVoltPowerMap
						.entrySet()) {
					Double dbValue, csvValue;
					Boolean dbFlag , csvFlag;
					if (!databaseVoltPowerMap.containsKey(entry.getKey())) {
						//get the enable flag
						Boolean enableFlag = csvVoltEnableMap.get(entry.getKey());						
						ballastVoltPowerManager.add(ballastId, voltage, entry
								.getKey(), entry.getValue(), maxVoltPowerMapId,enableFlag);
					} else if (databaseVoltPowerMap.containsKey(entry.getKey())) {
						dbValue = databaseVoltPowerMap.get(entry.getKey());
						csvValue = entry.getValue();
						if (!(dbValue.compareTo(csvValue) == 0)) {
							// Update the existing list only if power values
							// differ
							ballastVoltPowerManager.updatePower(ballastId,
									voltage, entry.getKey(), entry.getValue());
						}
						dbFlag = databaseVoltEnableMap.get(entry.getKey());
						csvFlag = csvVoltEnableMap.get(entry.getKey());
						if(!(dbFlag.compareTo(csvFlag)==0))
						{
							ballastVoltPowerManager.updateEnable(ballastId,
									voltage, entry.getKey(), csvFlag);	
							
						}
					}
				}
				// Clean up ballast cache
				fixtureManager.invalidateBallastVPCurve(ballastId);
				userAuditLoggerUtil.log("Import Ballast Curve for ballast "
						+ ballastId, UserAuditActionType.Import_Ballast_Curve
						.getName());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				successFlag = false;
			} catch (Exception e) {
				successFlag = false;
			}
		}
		//Check whether the map is not empty
		if(csvVoltPowerMap!=null)
		{
			if(csvVoltPowerMap.isEmpty())
			{
				successFlag = false;
			}
		}		
		model.addAttribute("successflag",successFlag);		
		return "settings/ballastlist";
	}
}
