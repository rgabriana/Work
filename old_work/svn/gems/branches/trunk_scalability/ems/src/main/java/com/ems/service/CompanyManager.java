package com.ems.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.Resource;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.action.SpringContext;
import com.ems.dao.CompanyDao;
import com.ems.model.Company;
import com.ems.model.InventoryDevice;
import com.ems.model.ProfileHandler;
import com.ems.model.Timezone;
import com.ems.model.User;
import com.ems.security.util.PasswordUtils;
import com.ems.server.ServerConstants;
import com.ems.server.ServerMain;
import com.ems.server.util.ServerUtil;
import com.ems.types.FacilityType;
import com.ems.util.MD5;
import com.ems.util.tree.TreeNode;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Service("companyManager")
@Transactional(propagation = Propagation.REQUIRED)
public class CompanyManager {

    @Resource
    private CompanyDao companyDao;
    @Resource(name = "userManager")
    private UserManager userManager;
    @Resource(name = "profileManager")
    private ProfileManager profileManager;

    /**
     * save company details.
     * 
     * @param company
     *            com.ems.model.Company
     */
    @CacheEvict(value = "company", allEntries=true)
    public Company save(Company company) {
    	company.setName(company.getName().trim());
        return (Company) companyDao.saveObject(company);
    }

    /**
     * update company details.
     * 
     * @param company
     *            com.ems.model.Company
     */
    @CacheEvict(value = "company", allEntries=true)
    public Company update(Company company) {
    	company.setName(company.getName().trim());
        return (Company) companyDao.saveObject(company);
    }

    /**
     * load company
     * 
     * @param id
     *            database id(primary key)
     * @return Company com.ems.model.Company object load only id,name,address,contact details of comapny other details
     *         loads as null.
     */
    @Cacheable(value = "company", key="#id")
    public Company loadCompanyById(Long id) {
        return companyDao.loadCompanyById(id);
    }

    /**
     * this method for flex. if need to load single company without id.
     * 
     * @return Company com.ems.model.Company object load only id,name,address,contact details of comapny other details
     *         loads as null.
     */
    public Company loadCompany() {
        return companyDao.loadCompany();
    }

    /**
     * load full details of company
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.Company object
     */
    public Company getCompany() {
    	Company company = null ;
        ArrayList<Company> companyList = new ArrayList<Company>() ;
        companyList = (ArrayList<Company>) getAllCompanies() ;
        	if(companyList!=null && !companyList.isEmpty()){
        	company = companyList.get(0);
        	}
      
        return company ;

    }

    public List<Company> getAllCompanies() {
        return companyDao.getAllCompanies();
    }

    /**
     * Check if UnCommissioned fixture available
     * 
     * @return
     */
    public boolean isUnCommissionedFixtureAvailable() {
    	InventoryDeviceManager inventoryDeviceManager = (InventoryDeviceManager) SpringContext
                .getBean("inventoryDeviceManager");
        List<InventoryDevice> inventoryDevices = inventoryDeviceManager
                .loadAllInventoryDeviceByType(ServerConstants.DEVICE_FIXTURE);
        if (inventoryDevices != null && !inventoryDevices.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    private static void readErrorStream(final Process process) {

        new Thread() {
            public void run() {
                BufferedReader br = null;
                try {
                    ServerUtil.sleep(1);
                    br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    String line = "";
                    // System.out.println("started reading error of auto map");
                    //System.out.println("started reading timezone process output");
                    while (true) {
                        line = br.readLine();
                        if (line == null) {
                            break;
                        }
                        // System.out.println("error line - " + line);
                        //System.out.println("error line - " + line);
                    }
                    // System.out.println("done with reading the error of auto map");
                    //System.out.println("done with reading the error of timezone setting");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }.start();

    } // end of method readErrorStream

    public List<Timezone> getTimezoneList() {
        String[] ids = TimeZone.getAvailableIDs();
        Arrays.sort(ids);
        List<Timezone> timezones = new ArrayList<Timezone>();
        Timezone tzone = null;
        //System.out.println("no. of timezones -- " + ids.length);
        int i = 0;
        for (i = 0; i < ids.length; i++) {
            tzone = new Timezone();
            tzone.setId(new Long(i));
            tzone.setName(ids[i]);
            timezones.add(tzone);
        }
        return timezones;

    } // end of method getTimezoneList
    
    
    public int getTimeZoneId(String name) {
        String[] ids = TimeZone.getAvailableIDs();
        Arrays.sort(ids);
        int i = 0;
        for(String tz: ids) {
        	if(name.equals(tz)) {
        		return i;
        	}
        	i++;
        }
        return 0;

    } // end of method getTimezoneList

    /*
     * function to set the time zone of the gems linux server
     */
    public void setServerTimeZone(String timeZone) {

        Process pr = null;
        String tomcatLocation = ServerMain.getInstance().getTomcatLocation();
        try {
            //System.out.println("changing the timezone to " + timeZone);
            String[] cmdArr = { "/bin/bash", tomcatLocation + "/adminscripts/settimezone.sh", timeZone };
            pr = Runtime.getRuntime().exec(cmdArr);
            readErrorStream(pr);
            pr.waitFor();
            System.getProperties().remove("user.timezone");
            TimeZone.setDefault(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    } // end of method setServerTimeZone

    public void setServerTime(String sTime) {

        if (sTime != null) {
            sTime = sTime.trim().toUpperCase();
            if (sTime != "") {
                Process pr = null;
                String tomcatLocation = ServerMain.getInstance().getTomcatLocation();
                try {
                    //System.out.println("changing the time to " + sTime);
                    String[] cmdArr = { "/bin/bash", tomcatLocation + "/adminscripts/settime.sh", sTime };
                    pr = Runtime.getRuntime().exec(cmdArr);
                    readErrorStream(pr);
                    pr.waitFor();
                    System.getProperties().remove("user.timezone");
                    TimeZone.setDefault(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
    
    @CacheEvict(value = "company", allEntries=true)
    public void registerCompany(Company company, String newPassword) {
        if (getAllCompanies().size() < 1) {
            ProfileHandler profileHandler = profileManager.createProfile("default.",
                    ServerConstants.DEFAULT_PROFILE_GID,true);
            company.setProfileHandler(profileHandler);
            company.setCompletionStatus(1);
            company.setPrice(company.getPrice().floatValue() / 100);
            String timezone = System.getProperty("user.timezone");
			List<Timezone> tzlist = getTimezoneList();
			for(Timezone tz: tzlist) {
				if(tz.getName().equals(timezone)) {
					timezone = tz.getName();
				}
			}
            company.setTimeZone(timezone);
            save(company);
            profileManager.saveDefaultGroups(profileHandler, company);
            String md5Password = MD5.hash(newPassword);
            userManager.updateUserDetails(
                    "password",
                    md5Password,
                    String.valueOf(((User) userManager.loadUserByUserName(SecurityContextHolder.getContext()
                            .getAuthentication().getName())).getId()));
            try {
				PasswordUtils.updatePassword(newPassword);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        } else {
            Company company1 = getAllCompanies().get(0);
            company.setProfileHandler(company1.getProfileHandler());
            company.setCompletionStatus(company1.getCompletionStatus());
            updateCompany(company);
            if (newPassword != null && !"".equals(newPassword)) {
                String md5Password = MD5.hash(newPassword);
                try {
    				PasswordUtils.updatePassword(newPassword);
    			} catch (FileNotFoundException e) {
    				e.printStackTrace();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
                userManager.updateUserDetails(
                        "password",
                        md5Password,
                        String.valueOf(((User) userManager.loadUserByUserName(SecurityContextHolder.getContext()
                                .getAuthentication().getName())).getId()));
            }
        }

    }

    @CacheEvict(value = "company", allEntries=true)
    public void completeSetup() {
        Company company = companyDao.loadCompany();
        company.setCompletionStatus(3);
        update(company);
    }

    @CacheEvict(value = "company", allEntries=true)
    public void updateCompany(Company company) {
        Company company1 = companyDao.getCompanyById(company.getId());
        company1.setName(company.getName().trim());
        company1.setAddress(company.getAddress());
        company1.setEmail(company.getEmail());
        company1.setContact(company.getContact());
        company1.setSelfLogin(company.getSelfLogin());
        company1.setValidDomain(company.getValidDomain());
        company1.setNotificationEmail(company.getNotificationEmail());
        company1.setSeverityLevel(company.getSeverityLevel());
        company1.setPrice(company.getPrice().floatValue());
        update(company1);

    }

    public String getCurrentTime() {
        Calendar oCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        String sTime = sdf.format(oCal.getTime());
        //System.out.println("Time: " + sTime);
        return sTime;
    }

    /**
     * Call this method to get the list of campus materialized
     */

    public Company laodCompanyWithCampus(Long id) {
        Company company = companyDao.getCompanyById(id);
        company.getCampuses().size();
        return company;
    }
    
    public TreeNode<FacilityType> loadCompanyHierarchy() {
    	return companyDao.loadCompanyHierarchy();
    }
    
    public TreeNode<String> loadCompanyHierarchyForUem() {
    	return companyDao.loadCompanyHierarchyForUem();
    }

}
