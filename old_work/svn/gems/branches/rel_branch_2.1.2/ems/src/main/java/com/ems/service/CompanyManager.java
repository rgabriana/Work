package com.ems.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.annotation.Resource;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.action.SpringContext;
import com.ems.dao.CompanyDao;
import com.ems.model.Area;
import com.ems.model.Building;
import com.ems.model.Campus;
import com.ems.model.Company;
import com.ems.model.Floor;
import com.ems.model.InventoryDevice;
import com.ems.model.ProfileHandler;
import com.ems.model.SubArea;
import com.ems.model.Timezone;
import com.ems.model.User;
import com.ems.server.ServerConstants;
import com.ems.server.ServerMain;
import com.ems.server.util.ServerUtil;
import com.ems.util.MD5;

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

    //TODO Can be removed. loadCompanyById(id) already exists.
    /**
     * load full details of company
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.Company object
     */
    public Company loadCompanyDetails(Long id) {
        return (Company) companyDao.getObject(Company.class, id);
    }
    //TODO Can be removed. loadCompanyById(id) already exists.
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

    /**
     * save company full details like campus,building,floor etc. this method use in XML simulation.
     * 
     * @param company
     *            com.ems.model.Company
     */

    /**
     * TODO: This method is commented out to be removed. The whole CompanyComapus notion is redundant now. - lalit
     * public void saveCompanyDetails(Company company) { save(company); if (company.getName() != null &&
     * !"".equals(company.getName())) { Set<CompanyCampus> companyCampuses = company.getCompanyCampus(); if
     * (companyCampuses != null && !companyCampuses.isEmpty()) { saveCampusDetails(companyCampuses, company); } } }
     * 
     * private void saveCampusDetails(Set<CompanyCampus> companyCampuses, Company company) { Iterator<CompanyCampus>
     * companyCampusIterator = companyCampuses.iterator(); while (companyCampusIterator.hasNext()) { CompanyCampus
     * companyCampus = companyCampusIterator.next(); Campus campus = companyCampus.getCampus(); if (campus != null &&
     * campus.getName() != null && !"".equals(campus.getName())) { companyCampus.setCompany(company);
     * companyDao.saveObject(campus); companyCampus.setCampus(campus); companyDao.saveObject(companyCampus);
     * Set<Building> buildings = campus.getBuildings(); if (buildings != null && !buildings.isEmpty()) {
     * saveBuildingDetails(buildings, campus); } } } }
     */
    
    //TODO no use case found. Can be removed.
    private void saveBuildingDetails(Set<Building> buildings, Campus campus) {
        Iterator<Building> buildingIterator = buildings.iterator();
        while (buildingIterator.hasNext()) {
            Building building = buildingIterator.next();
            if (building != null && building.getName() != null && !"".equals(building.getName())) {
                building.setCampus(campus);
                companyDao.saveObject(building);
                Set<Floor> floors = building.getFloors();
                if (floors != null && !floors.isEmpty()) {
                    saveFloorDetails(floors, building);
                }
            }
        }
    }
    
  //TODO no use case found. Can be removed.
    private void saveFloorDetails(Set<Floor> floors, Building building) {
        Iterator<Floor> floorIterator = floors.iterator();
        while (floorIterator.hasNext()) {
            Floor floor = floorIterator.next();
            if (floor != null && floor.getName() != null && !"".equals(floor.getName())) {
                floor.setBuilding(building);
                companyDao.saveObject(floor);
                Set<Area> areas = floor.getAreas();
                if (areas != null && !areas.isEmpty()) {
                    saveAreaDetails(areas, floor);
                }
            }
        }
    }

  //TODO no use case found. Can be removed.
    private void saveAreaDetails(Set<Area> areas, Floor floor) {
        Iterator<Area> areaIterator = areas.iterator();
        while (areaIterator.hasNext()) {
            Area area = areaIterator.next();
            if (area != null && area.getName() != null && !"".equals(area.getName())) {
                area.setFloor(floor);
                companyDao.saveObject(area);
                Set<SubArea> subAreas = area.getSubAreas();
                if (subAreas != null && !subAreas.isEmpty()) {
                    saveSubAreaDetails(subAreas, area);
                }
            }
        }
    }

  //TODO no use case found. Can be removed.
    private void saveSubAreaDetails(Set<SubArea> subAreas, Area area) {
        Iterator<SubArea> subAreaIterator = subAreas.iterator();
        while (subAreaIterator.hasNext()) {
            SubArea subArea = subAreaIterator.next();
            if (subArea != null && subArea.getName() != null && !"".equals(subArea.getName())) {
                subArea.setArea(area);
                companyDao.saveObject(subArea);
            }
        }
    }

    public List<Company> getAllCompanies() {
        return companyDao.getAllCompanies();
    }

    /**
     * Load trre path if type and id is given
     * 
     * @param type
     *            like company,building,floor
     * @param id
     *            database id
     * @return String
     */
    public String loadTreePath(String type, String id) {
        return companyDao.loadTreePath(type, id);
    }

    public void updateCompanyDetails(String column, String value, String id) {
        companyDao.updateCompanyDetails(column, value, id);
        if (column.equals("timeZone")) {
            setServerTimeZone(value);
        }
    }

    //TODO Don't think it is required any more.
    public Company updateCompleteCompany(Company company) {
        return companyDao.updateCompleteCompany(company);
    }

    public Company editName(Company company) {
    	company.setName(company.getName().trim());
        return companyDao.editName(company);
    }

    /**
     * Check if UnCommissioned fixture available
     * 
     * @return
     */
    public boolean isUnCommissionedFixtureAvailable() {
        InventoryDeviceService inventoryDeviceService = (InventoryDeviceService) SpringContext
                .getBean("inventoryDeviceService");
        List<InventoryDevice> inventoryDevices = inventoryDeviceService
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
                    StringTokenizer st = null;
                    // System.out.println("started reading error of auto map");
                    System.out.println("started reading timezone process output");
                    while (true) {
                        line = br.readLine();
                        if (line == null) {
                            break;
                        }
                        // System.out.println("error line - " + line);
                        System.out.println("error line - " + line);
                    }
                    // System.out.println("done with reading the error of auto map");
                    System.out.println("done with reading the error of timezone setting");
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

    //TODO not used.
    public String[] getAllTimeZones() {

        String TIMEZONE_ID_PREFIXES = "^(Africa|America|Asia|Atlantic|Australia|Europe|Indian|Pacific)/.*";
        String[] ids = TimeZone.getAvailableIDs();
        Arrays.sort(ids);
        System.out.println("no. of timezones -- " + ids.length);
        /*
         * for (int i = 0; i < ids.length; i++) { if (ids[i].matches(TIMEZONE_ID_PREFIXES)) {
         * System.out.println("id -- " + ids[i] + " name - " + TimeZone.getTimeZone(ids[i]).getDisplayName()); } }
         */
        return ids;

    } // end of method getAllTimeZones

    public List<Timezone> getTimezoneList() {

        String TIMEZONE_ID_PREFIXES = "^(Africa|America|Asia|Atlantic|Australia|Europe|Indian|Pacific)/.*";
        String[] ids = TimeZone.getAvailableIDs();
        Arrays.sort(ids);
        List<Timezone> timezones = new ArrayList<Timezone>();
        Timezone tzone = null;
        System.out.println("no. of timezones -- " + ids.length);
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

        String TIMEZONE_ID_PREFIXES = "^(Africa|America|Asia|Atlantic|Australia|Europe|Indian|Pacific)/.*";
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
            System.out.println("changing the timezone to " + timeZone);
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
                    System.out.println("changing the time to " + sTime);
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

    public void registerCompany(Company company, String newPassword) {
        if (getAllCompanies().size() < 1) {
            ProfileHandler profileHandler = profileManager.createProfile("default.",
                    ServerConstants.DEFAULT_PROFILE_GID);
            company.setProfileHandler(profileHandler);
            company.setCompletionStatus(1);
            company.setPrice(company.getPrice().floatValue() / 100);
            save(company);
            setServerTimeZone(getTimezoneList().get(getTimeZoneId(company.getTimeZone())).getName());
            profileManager.saveDefaultGroups(profileHandler, company);
            String md5Password = MD5.hash(newPassword);
            userManager.updateUserDetails(
                    "password",
                    md5Password,
                    String.valueOf(((User) userManager.loadUserByUserName(SecurityContextHolder.getContext()
                            .getAuthentication().getName())).getId()));

        } else {
            Company company1 = getAllCompanies().get(0);
            company.setProfileHandler(company1.getProfileHandler());
            company.setCompletionStatus(company1.getCompletionStatus());
            updateCompany(company);
            if (newPassword != null && !"".equals(newPassword)) {
                String md5Password = MD5.hash(newPassword);
                userManager.updateUserDetails(
                        "password",
                        md5Password,
                        String.valueOf(((User) userManager.loadUserByUserName(SecurityContextHolder.getContext()
                                .getAuthentication().getName())).getId()));
            }
        }

    }

    public void completeSetup() {
        Company company = companyDao.loadCompany();
        company.setCompletionStatus(3);
        // Company company = getAllCompanies().get(0);
        // updateCompanyDetails("completionStatus", "3", String.valueOf(company.getId()));
    }

    public void updateCompany(Company company) {
        Company company1 = companyDao.getCompanyById(company.getId());
        company1.setName(company.getName().trim());
        company1.setAddress(company.getAddress());
        company1.setEmail(company.getEmail());
        company1.setContact(company.getContact());
        boolean changeTimeZone = !company1.getTimeZone().equals(company.getTimeZone());
        company1.setTimeZone(company.getTimeZone());
        company1.setSelfLogin(company.getSelfLogin());
        company1.setValidDomain(company.getValidDomain());
        company1.setNotificationEmail(company.getNotificationEmail());
        company1.setSeverityLevel(company.getSeverityLevel());
        company1.setPrice(company.getPrice().floatValue() / 100);
        update(company1);
        if (changeTimeZone) {
            setServerTimeZone(getTimezoneList().get(getTimeZoneId(company.getTimeZone())).getName());
        }

    }

    public String getCurrentTime() {
        Calendar oCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        String sTime = sdf.format(oCal.getTime());
        System.out.println("Time: " + sTime);
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

}
