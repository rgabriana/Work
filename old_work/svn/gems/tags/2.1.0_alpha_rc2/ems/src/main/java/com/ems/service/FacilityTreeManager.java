package com.ems.service;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.AreaDao;
import com.ems.dao.BuildingDao;
import com.ems.dao.CampusDao;
import com.ems.dao.CompanyDao;
import com.ems.dao.FloorDao;
import com.ems.dao.TenantDao;
import com.ems.model.Area;
import com.ems.model.Building;
import com.ems.model.Campus;
import com.ems.model.Company;
import com.ems.model.Floor;
import com.ems.model.Tenant;
import com.ems.types.FacilityType;
import com.ems.util.tree.TreeNode;

@Service("facilityTreeManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FacilityTreeManager {

    Map<Long, TreeNode<FacilityType>> facilityTreeMap = new HashMap<Long, TreeNode<FacilityType>>();

    @Resource
    private CompanyDao companyDao;

    @Resource
    private CampusDao campusDao;

    @Resource
    private BuildingDao buildingDao;

    @Resource
    private FloorDao floorDao;

    @Resource
    private AreaDao areaDao;

    @Resource
    private TenantDao tenantDao;

    public TreeNode<FacilityType> loadCompanyHierarchy() {

        if (facilityTreeMap.containsKey(0L)) {
            return facilityTreeMap.get(0L);
        }

        TreeNode<FacilityType> companyHierachy = companyDao.loadCompanyHierarchy();
        facilityTreeMap.put(0L, companyHierachy);
        return companyHierachy;
    }

    public TreeNode<FacilityType> loadTenantFacilitiesHierarchy(long tenantId) {
        TreeNode<FacilityType> tenantFacilityHierarchy = new TreeNode<FacilityType>();
        tenantFacilityHierarchy.setNodeId("0");
        tenantFacilityHierarchy.setName("Root");
        tenantFacilityHierarchy.setNodeType(FacilityType.ROOT);

        // Let's get the complete hierarchy
        TreeNode<FacilityType> companyHierachy = companyDao.loadCompanyHierarchy();

        for (TreeNode<FacilityType> companyNode : companyHierachy.getTreeNodeList()) {

            // Let's see if company is included
            if (companyNode.getTenantid() == tenantId) {
                tenantFacilityHierarchy.addTreeNode(companyNode.deepCopy());
            } else {
                // Let's iterate over campus
                for (TreeNode<FacilityType> campusNode : companyNode.getTreeNodeList()) {
                    if (campusNode.getTenantid() == tenantId) {
                        tenantFacilityHierarchy.addTreeNode(campusNode.deepCopy());
                    } else {
                        // Let's iterate over building
                        for (TreeNode<FacilityType> buildingNode : campusNode.getTreeNodeList()) {
                            if (buildingNode.getTenantid() == tenantId) {
                                tenantFacilityHierarchy.addTreeNode(buildingNode.deepCopy());
                            } else {
                                // Let's iterate over area
                                for (TreeNode<FacilityType> floorNode : buildingNode.getTreeNodeList()) {
                                    if (floorNode.getTenantid() == tenantId) {
                                        tenantFacilityHierarchy.addTreeNode(floorNode.deepCopy());
                                    } else {
                                        for (TreeNode<FacilityType> areaNode : floorNode.getTreeNodeList()) {
                                            if (areaNode.getTenantid() == tenantId) {
                                                tenantFacilityHierarchy.addTreeNode(areaNode.deepCopy());
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
        return tenantFacilityHierarchy;
    }

    public void inValidateFacilitiesTreeCache() {
        facilityTreeMap.clear();
    }

    public void inValidateFacilitiesTreeCacheForTenant(long tenantId) {
        if (facilityTreeMap.containsKey(tenantId)) {
            facilityTreeMap.remove(tenantId);
        }
    }

    public void setTenantFacilities(String[] assignedFacilities) {
        for (String facility : assignedFacilities) {
            String[] facilityDetail = facility.split("_");
            String facilityType = facilityDetail[0];
            Long facilityId = Long.parseLong(facilityDetail[1]);
            Long TenatId = Long.parseLong(facilityDetail[2]);

            Tenant tenant = null;
            if (TenatId != 0) {
                tenant = (Tenant) tenantDao.getObject(Tenant.class, TenatId);
            }

            if ("company".equalsIgnoreCase(facilityType)) {
                Company company = (Company) companyDao.getObject(Company.class, facilityId);
                company.setTenant(tenant);
                companyDao.saveObject(company);
            } else if ("campus".equalsIgnoreCase(facilityType)) {
                Campus campus = (Campus) campusDao.getObject(Campus.class, facilityId);
                campus.setTenant(tenant);
                campusDao.saveObject(campus);
            } else if ("building".equalsIgnoreCase(facilityType)) {
                Building building = (Building) buildingDao.getObject(Building.class, facilityId);
                building.setTenant(tenant);
                buildingDao.saveObject(building);
            } else if ("floor".equalsIgnoreCase(facilityType)) {
                Floor floor = (Floor) floorDao.getObject(Floor.class, facilityId);
                floor.setTenant(tenant);
                floorDao.saveObject(floor);
            } else if ("area".equalsIgnoreCase(facilityType)) {
                Area area = (Area) areaDao.getObject(Area.class, facilityId);
                area.setTenant(tenant);
                areaDao.saveObject(area);
            }
        }
    }
}
