package com.ems.dao;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    @SuppressWarnings("unchecked")
    public Company loadCompanyById(Long id) {
        Session session = getSession();
        Company company = (Company) session.get(Company.class, id);
        return company;

        // try {
        // List<Company> results = null;
        // String hsql =
        // "Select new Company(c.id,c.name,c.address,c.contact,c.email,c.timezone,c.completionStatus,c.selfLogin,c.validDomain,c.notificationEmail,c.severityLevel,c.price,c.profileHandler.id,c.timeZone) from Company c where c.id=?";
        // Query q = getSession().createQuery(hsql.toString());
        // q.setParameter(0, id);
        // results = q.list();
        // if (results != null && !results.isEmpty()) {
        // return (Company) results.get(0);
        // }
        // } catch (HibernateException hbe) {
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
        // return null;
    }

    /**
     * this method for flex. if need to load single company without id.
     * 
     * @return Company com.ems.model.Company object load only id,name,address,contact details of comapny other details
     *         loads as null.
     */
    @SuppressWarnings("unchecked")
    public Company loadCompany() {
        Session session = getSession();
        Company company = (Company) session.get(Company.class, 1L);
        return company;

        // try {
        // List<Company> results = null;
        // String hsql =
        // "Select new Company(c.id,c.name,c.address,c.contact,c.email,c.timezone,c.completionStatus,c.selfLogin,c.validDomain,c.notificationEmail,c.severityLevel,c.price,c.profileHandler.id,c.timeZone) from Company c";
        // Query q = getSession().createQuery(hsql.toString());
        // results = q.list();
        // if (results != null && !results.isEmpty()) {
        // return (Company) results.get(0);
        // }
        // } catch (HibernateException hbe) {
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
        // return null;
    }

    /**
     * 
     * @return the list of all companies
     */
    @SuppressWarnings("unchecked")
    public List<Company> getAllCompanies() {
        List<Company> companies = getSession().createQuery("from Company order by name").list();
        return companies;
    }

    /**
     * Load tree path if type and id is given
     * 
     * @param type
     *            like company,building,floor
     * @param id
     *            database id
     * @return String
     */
    // TODO Redundant code, to be removed
    @SuppressWarnings("unchecked")
    public String loadTreePath(String type, String id) {
        // try {
        // List results = null;
        // String hsql = "select (select name from company "
        // + "where id=(select company_id from company_campus where campus_id =c.id)) "
        // + "||' --> '||c.name||' --> '||b.name||' --> '||f.name||' --> '||a.name as path "
        // + "from area a join floor f on f.id=a.floor_id join building b on b.id = f.building_id "
        // + "join campus c on c.id = b.campus_id and f.id=" + id;
        //
        // if (type.equalsIgnoreCase("Floor")) {
        // hsql = "select (select name from company "
        // + "where id=(select company_id from company_campus where campus_id =c.id)) "
        // + "||' --> '||c.name||' --> '||b.name||' --> '||f.name as path "
        // + "from floor f join building b on b.id = f.building_id "
        // + "join campus c on c.id = b.campus_id and f.id=" + id;
        // }
        //
        // if (type.equalsIgnoreCase("Building")) {
        // hsql = "select (select name from company "
        // + "where id=(select company_id from company_campus where campus_id =c.id)) "
        // + "||' --> '||c.name||' --> '||b.name as path "
        // + "from building b join campus c on c.id = b.campus_id " + "and b.id=" + id;
        // }
        //
        // if (type.equalsIgnoreCase("Campus")) {
        // hsql = "select (select name from company "
        // + "where id=(select company_id from company_campus where campus_id =c.id)) "
        // + "||' --> '||c.name as path from campus c where c.id=" + id;
        // }
        //
        // if (type.equalsIgnoreCase("Company")) {
        // hsql = "select name from company " + "where id=" + id;
        // }
        //
        // Query q = getSession().createSQLQuery(hsql.toString());
        // results = q.list();
        // if (results != null && !results.isEmpty()) {
        // return (String) results.get(0);
        // }
        // } catch (HibernateException hbe) {
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
        return null;
    }

    // TODO: Remove redundant code
    public void updateCompanyDetails(String column, String value, String id) {
        // String hql = "update Company set " + column + " = :newValue where id = :id";
        // Query query = getSession().createQuery(hql);
        // if ("selfLogin".equals(column)) {
        // query.setBoolean("newValue", new Boolean(value));
        // } else if ("timezone".equals(column)) {
        // query.setLong("newValue", new Long(value));
        // } else if ("completionStatus".equals(column)) {
        // query.setInteger("newValue", new Integer(value));
        // } else if ("price".equals(column)) {
        // query.setFloat("newValue", new Float(value));
        // } else {
        // query.setString("newValue", value);
        // }
        // query.setLong("id", new Long(id));
        // query.executeUpdate();
    }

    public Company updateCompleteCompany(Company company) {
        Session session = getSession();
        session.update(company);
        return company;

        // getSession().clear();
        // getSession().saveOrUpdate(company);
        // return company;
    }

    public Company editName(Company company) {
        Session session = getSession();
        session.update(company);
        return company;

        // getSession().createQuery("Update Company set name = :name where id = :id").setString("name",
        // company.getName())
        // .setLong("id", company.getId()).executeUpdate();
        // return loadCompany();
    }

    public Company getCompanyById(Long companyId) {
        Company company = (Company) getSession().createCriteria(Company.class).add(Restrictions.idEq(companyId))
                .uniqueResult();
        return company;
    }

    public TreeNode<FacilityType> loadCompanyHierarchy() {

        /*
         * String hql =
         * "select company.id as company_id, company.name as company_name, campus.id as campus_id,  campus.name as campus_name, "
         * +
         * " building.id as building_id, building.name as building_name, floor.id as floor_id, floor.name as floor_name "
         * + " from Company company, Building building, Campus campus, Floor floor " + " where " +
         * " floor.building = building  and " + " building.campus = campus " +
         * " order by company.id, campus.id, building.id ";
         * 
         * Query query = getSession().createQuery(hql);
         * 
         * TreeNode companyNode = null; TreeNode latestCampusNode = null; TreeNode latestBuildingNode = null;
         * List<Object[]> resultSet = query.list(); for(Object[] row: resultSet){
         * 
         * if(companyNode == null){ //This means it's the root company node companyNode = new TreeNode();
         * companyNode.setNodeId(row[0].toString()); companyNode.setName(row[1].toString()); companyNode.setLeaf(false);
         * 
         * TreeNode currentCampusNode = new TreeNode(); currentCampusNode.setNodeId(row[2].toString());
         * currentCampusNode.setName(row[3].toString()); currentCampusNode.setLeaf(false); latestCampusNode =
         * currentCampusNode; companyNode.addTreeNode(currentCampusNode);
         * 
         * TreeNode currentBuildingNode = new TreeNode(); currentBuildingNode.setNodeId(row[4].toString());
         * currentBuildingNode.setName(row[5].toString()); currentBuildingNode.setLeaf(false); latestBuildingNode =
         * currentBuildingNode; latestCampusNode.addTreeNode(currentBuildingNode);
         * 
         * TreeNode currentFloorNode = new TreeNode(); currentFloorNode.setNodeId(row[6].toString());
         * currentFloorNode.setName(row[7].toString()); currentFloorNode.setLeaf(false);
         * latestBuildingNode.addTreeNode(currentFloorNode); }else{
         * if(row[2].toString().equals(latestCampusNode.getNodeId())){ //check if we are still on same campus
         * if(row[4].toString().equals(latestBuildingNode.getNodeId())){ //check if we are in same building //Floors are
         * always different TreeNode currentFloorNode = new TreeNode(); currentFloorNode.setNodeId(row[6].toString());
         * currentFloorNode.setName(row[7].toString()); currentFloorNode.setLeaf(false);
         * latestBuildingNode.addTreeNode(currentFloorNode); }else{ //we are in a new building TreeNode
         * currentBuildingNode = new TreeNode(); currentBuildingNode.setNodeId(row[4].toString());
         * currentBuildingNode.setName(row[5].toString()); currentBuildingNode.setLeaf(false); latestBuildingNode =
         * currentBuildingNode; latestCampusNode.addTreeNode(currentBuildingNode);
         * 
         * TreeNode currentFloorNode = new TreeNode(); currentFloorNode.setNodeId(row[6].toString());
         * currentFloorNode.setName(row[7].toString()); currentFloorNode.setLeaf(false);
         * latestBuildingNode.addTreeNode(currentFloorNode); } }else{ //We are now dealing with new campus TreeNode
         * currentCampusNode = new TreeNode(); currentCampusNode.setNodeId(row[2].toString());
         * currentCampusNode.setName(row[3].toString()); currentCampusNode.setLeaf(false); latestCampusNode =
         * currentCampusNode; companyNode.addTreeNode(currentCampusNode);
         * 
         * TreeNode currentBuildingNode = new TreeNode(); currentBuildingNode.setNodeId(row[4].toString());
         * currentBuildingNode.setName(row[5].toString()); currentBuildingNode.setLeaf(false); latestBuildingNode =
         * currentBuildingNode; latestCampusNode.addTreeNode(currentBuildingNode);
         * 
         * TreeNode currentFloorNode = new TreeNode(); currentFloorNode.setNodeId(row[6].toString());
         * currentFloorNode.setName(row[7].toString()); currentFloorNode.setLeaf(false);
         * latestBuildingNode.addTreeNode(currentFloorNode); } } }
         * 
         * 
         * return companyNode;
         */

        TreeNode<FacilityType> rootNode = new TreeNode<FacilityType>();
        rootNode.setNodeId("0");
        rootNode.setName("Root");
        rootNode.setNodeType(FacilityType.ROOT);

        TreeNode<FacilityType> companyNode = new TreeNode<FacilityType>();
        
        //Get companies
        List<Company> companyList = getAllCompanies();
        for (Company company : companyList) {
            companyNode.setNodeId(company.getId().toString());
            companyNode.setName(company.getName());
            companyNode.setNodeType(FacilityType.COMPANY);
            companyNode.setTenantid(company.getTenant() == null ? 0 : company.getTenant().getId());
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
           return c1.getName().compareTo(c2.getName());
        }     
    }
    
    public class BuildingComparator implements Comparator<Building>{
        @Override
        public int compare(Building b1, Building b2) {
            return b1.getName().compareTo(b2.getName());
        }
    }
    
    public class FloorComparator implements Comparator<Floor>{
        @Override
        public int compare(Floor f1, Floor f2) {
            return f1.getName().compareTo(f2.getName());
        }
    }
    
    public class AreaComparator implements Comparator<Area>{
        @Override
        public int compare(Area a1, Area a2) {
            return a1.getName().compareTo(a2.getName());
        }
    }
}
