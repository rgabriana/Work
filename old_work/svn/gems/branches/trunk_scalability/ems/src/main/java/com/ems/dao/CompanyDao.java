package com.ems.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.cache.SweepTimerCache;
import com.ems.model.Area;
import com.ems.model.Building;
import com.ems.model.Campus;
import com.ems.model.Company;
import com.ems.model.Floor;
import com.ems.types.FacilityType;
import com.ems.util.tree.TreeNode;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Repository("companyDao")
@Transactional(propagation = Propagation.REQUIRED)
public class CompanyDao extends BaseDaoHibernate {

    /**
     * load company
     * 
     * @param id
     *            database id(primary key)
     * @return Company com.ems.model.Company object load only id,name,address,contact details of comapny other details
     *         loads as null.
     */
    public Company loadCompanyById(Long id) {
        Session session = getSession();
        Company company = (Company) session.get(Company.class, id);
        return company;
    }

    /**
     * this method for flex. if need to load single company without id.
     * 
     * @return Company com.ems.model.Company object load only id,name,address,contact details of comapny other details
     *         loads as null.
     */
    public Company loadCompany() {
    	Company company = null ;
        ArrayList<Company> companyList = new ArrayList<Company>() ;
        companyList = (ArrayList<Company>) getAllCompanies() ;
        	if(companyList!=null && !companyList.isEmpty()){
        	company = companyList.get(0);
        	}
      
        return company ;
    }

    /**
     * 
     * @return the list of all companies
     */
    @SuppressWarnings("unchecked")
    public List<Company> getAllCompanies() {
        List<Company> companies = getSession().createQuery("from Company order by id").list();
        return companies;
    }



    public Company updateCompleteCompany(Company company) {
        Session session = getSession();
        session.update(company);
        return company;
    }

    public Company editName(Company company) {
        Session session = getSession();
        session.update(company);
        return company;
    }

    public Company getCompanyById(Long companyId) {
        Company company = (Company) getSession().createCriteria(Company.class).add(Restrictions.idEq(companyId))
                .uniqueResult();
        return company;
    }

    public TreeNode<FacilityType> loadCompanyHierarchy() {

        TreeNode<FacilityType> rootNode = new TreeNode<FacilityType>();
        rootNode.setNodeId("0");
        rootNode.setName("Root");
        rootNode.setNodeType(FacilityType.ROOT);

        TreeNode<FacilityType> companyNode = new TreeNode<FacilityType>();
        
        SweepTimerCache sweepTimerCache = SweepTimerCache.getInstance();
             
        //Get companies
        Company company = loadCompany();
        if (company !=null) {
            companyNode.setNodeId(company.getId().toString());
            companyNode.setName(company.getName());
            companyNode.setNodeType(FacilityType.COMPANY);
            companyNode.setTenantid(company.getTenant() == null ? 0 : company.getTenant().getId());
            companyNode.setSweepTimerId(company.getSweepTimerId() == null ? 0 : company.getSweepTimerId());
            rootNode.addTreeNode(companyNode);

            //Get campuses
            List<Campus> campuslist = company.getCampuses();
            Collections.sort(campuslist, new CampusComparator());
            
            for (Campus campus : campuslist) {
                TreeNode<FacilityType> campusNode = new TreeNode<FacilityType>();
                campusNode.setNodeId(campus.getId().toString());
                campusNode.setName(campus.getName());
                campusNode.setNodeType(FacilityType.CAMPUS);
                campusNode.setTenantid(campus.getTenant() == null ? 0 : campus.getTenant().getId());
                if(campus.getSweepTimerId() == null) {
            //      campusNode.setSweepTimerId(companyNode.getSweepTimerId());
                  SweepTimerCache.getInstance().addSweepTimerAssociation(
                      "campus_" + campus.getId(), "company_" + companyNode.getNodeId());
                } else {
                  campusNode.setSweepTimerId(campus.getSweepTimerId());
                }                
                companyNode.addTreeNode(campusNode);
                
                //Get buildings
                List<Building> buildinglist = campus.getBuildingsList(campus.getBuildings());
                Collections.sort(buildinglist, new BuildingComparator());
                
                for (Building building : buildinglist) {
                    TreeNode<FacilityType> buildingNode = new TreeNode<FacilityType>();
                    buildingNode.setNodeId(building.getId().toString());
                    buildingNode.setName(building.getName());
                    buildingNode.setNodeType(FacilityType.BUILDING);
                    buildingNode.setTenantid(building.getTenant() == null ? 0 : building.getTenant().getId());
                    if(building.getSweepTimerId() == null) {
                //      buildingNode.setSweepTimerId(campusNode.getSweepTimerId());
                      SweepTimerCache.getInstance().addSweepTimerAssociation(
                          "bld_" + building.getId(), "campus_" + campusNode.getNodeId());
                    } else {
                      buildingNode.setSweepTimerId(building.getSweepTimerId());
                    }                    
                    campusNode.addTreeNode(buildingNode);
                    
                    //Get floors
                    List<Floor> floorlist = building.getFloorsList(building.getFloors());
                    Collections.sort(floorlist, new FloorComparator());
                    
                    for (Floor floor : floorlist) {
                        TreeNode<FacilityType> floorNode = new TreeNode<FacilityType>();
                        floorNode.setNodeId(floor.getId().toString());
                        floorNode.setName(floor.getName());
                        floorNode.setNodeType(FacilityType.FLOOR);
                        floorNode.setTenantid(floor.getTenant() == null ? 0 : floor.getTenant().getId());
                        Long sweepTimerId = null;
                        if(floor.getSweepTimerId() == null) {
                          //floorNode.setSweepTimerId(buildingNode.getSweepTimerId());
                      //    sweepTimerId = buildingNode.getSweepTimerId();
                          SweepTimerCache.getInstance().addSweepTimerAssociation(
                              "floor_" + floor.getId(), "bld_" + buildingNode.getNodeId());
                        } else {
                          sweepTimerId = floor.getSweepTimerId();
                          floorNode.setSweepTimerId(sweepTimerId);
                        }
                        sweepTimerCache.addFloorSweepTimer(Long.parseLong(floorNode.getNodeId()), sweepTimerId);
                        buildingNode.addTreeNode(floorNode);
                        
                        //Get areas
                        List<Area> arealist = floor.getAreasList(floor.getAreas());
                        Collections.sort(arealist, new AreaComparator());
                        
                        for (Area area : arealist) {
                            TreeNode<FacilityType> areaNode = new TreeNode<FacilityType>();
                            areaNode.setNodeId(area.getId().toString());
                            areaNode.setName(area.getName());
                            areaNode.setNodeType(FacilityType.AREA);
                            areaNode.setTenantid(area.getTenant() == null ? 0 : area.getTenant().getId());
                            if(area.getSweepTimerId() != null) {
                              areaNode.setSweepTimerId(area.getSweepTimerId());
                              sweepTimerCache.addAreaSweepTimer(Long.parseLong(areaNode.getNodeId()), area.getSweepTimerId());
                            }                            
                            floorNode.addTreeNode(areaNode);
                        }
                    }
                }
            }
        }
        return rootNode;
    }
    
    public class CampusComparator implements Comparator<Campus>{
        @Override
        public int compare(Campus c1, Campus c2) {
           //return c1.getName().compareTo(c2.getName());
            return c1.getName().toLowerCase().compareTo(c2.getName().toLowerCase());
        }     
    }
    
    public class BuildingComparator implements Comparator<Building>{
        @Override
        public int compare(Building b1, Building b2) {
            return b1.getName().toLowerCase().compareTo(b2.getName().toLowerCase());
        }
    }
    
    public class FloorComparator implements Comparator<Floor>{
        @Override
        public int compare(Floor f1, Floor f2) {
            return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
        }
    }
    
    public class AreaComparator implements Comparator<Area>{
        @Override
        public int compare(Area a1, Area a2) {
            return a1.getName().toLowerCase().compareTo(a2.getName().toLowerCase());
        }
    }
    
    public TreeNode<String> loadCompanyHierarchyForUem() {

        TreeNode<String> rootNode = new TreeNode<String>();
        rootNode.setNodeId("0");
        rootNode.setName("Root");
        rootNode.setNodeType(FacilityType.ROOT.getName());
        TreeNode<String> companyNode = new TreeNode<String>();
        //Get companies
        Company company = loadCompany();
        if (company !=null) {
            companyNode.setNodeId(company.getId().toString());
            companyNode.setName(company.getName());
            companyNode.setNodeType(FacilityType.COMPANY.getName());
            rootNode.addTreeNode(companyNode);

            //Get campuses
            List<Campus> campuslist = company.getCampuses();
            Collections.sort(campuslist, new CampusComparator());
            
            for (Campus campus : campuslist) {
                TreeNode<String> campusNode = new TreeNode<String>();
                campusNode.setNodeId(campus.getId().toString());
                campusNode.setName(campus.getName());
                campusNode.setNodeType(FacilityType.CAMPUS.getName());
                                
                companyNode.addTreeNode(campusNode);
                
                //Get buildings
                List<Building> buildinglist = campus.getBuildingsList(campus.getBuildings());
                Collections.sort(buildinglist, new BuildingComparator());
                
                for (Building building : buildinglist) {
                    TreeNode<String> buildingNode = new TreeNode<String>();
                    buildingNode.setNodeId(building.getId().toString());
                    buildingNode.setName(building.getName());
                    buildingNode.setNodeType(FacilityType.BUILDING.getName());
                         
                    campusNode.addTreeNode(buildingNode);
                    
                    //Get floors
                    List<Floor> floorlist = building.getFloorsList(building.getFloors());
                    Collections.sort(floorlist, new FloorComparator());
                    
                    for (Floor floor : floorlist) {
                        TreeNode<String> floorNode = new TreeNode<String>();
                        floorNode.setNodeId(floor.getId().toString());
                        floorNode.setName(floor.getName());
                        floorNode.setNodeType(FacilityType.FLOOR.getName());
                        
                        buildingNode.addTreeNode(floorNode);
                    }
                }
            }
        }
        return rootNode;
    }
    
}
