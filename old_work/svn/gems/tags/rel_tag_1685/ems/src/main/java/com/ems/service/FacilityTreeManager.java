package com.ems.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.cache.SweepTimerCache;
import com.ems.dao.AreaDao;
import com.ems.dao.BuildingDao;
import com.ems.dao.CampusDao;
import com.ems.dao.CompanyDao;
import com.ems.dao.FloorDao;
import com.ems.dao.TenantDao;
import com.ems.dao.UserDao;
import com.ems.model.Area;
import com.ems.model.Building;
import com.ems.model.Campus;
import com.ems.model.Company;
import com.ems.model.Floor;
import com.ems.model.Tenant;
import com.ems.model.User;
import com.ems.model.UserLocations;
import com.ems.security.EmsAuthenticationContext;
import com.ems.types.FacilityType;
import com.ems.types.RoleType;
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

	@Resource
	private UserDao userDao;

	@Resource
	private CompanyManager companyManager;

	@Resource
	private BuildingManager buildingManager;

	@Resource
	private CampusManager campusManager;

	@Resource
	private FloorManager floorManager;

	@Resource
	private AreaManager areaManager;

	@Resource
	private UserManager userManager;

	@Resource
	private GroupManager groupManager;

	@Resource(name = "emsAuthContext")
	private EmsAuthenticationContext emsAuthContext;

	@Resource
	private UserLocationsManager userLocationsManger;

	public TreeNode<FacilityType> loadCompanyHierarchy() {

		if (facilityTreeMap.containsKey(0L)) {
			return facilityTreeMap.get(0L);
		}

		TreeNode<FacilityType> companyHierachy = companyDao
				.loadCompanyHierarchy();
		facilityTreeMap.put(0L, companyHierachy);
		return companyHierachy;
	}

	public TreeNode<FacilityType> loadTenantFacilitiesHierarchy(long tenantId) {
		TreeNode<FacilityType> tenantFacilityHierarchy = new TreeNode<FacilityType>();
		tenantFacilityHierarchy.setNodeId("0");
		tenantFacilityHierarchy.setName("Root");
		tenantFacilityHierarchy.setNodeType(FacilityType.ROOT);

		// Let's get the complete hierarchy
		TreeNode<FacilityType> companyHierachy = companyDao
				.loadCompanyHierarchy();

		for (TreeNode<FacilityType> companyNode : companyHierachy
				.getTreeNodeList()) {

			// Let's see if company is included
			if (companyNode.getTenantid() == tenantId) {
				tenantFacilityHierarchy.addTreeNode(companyNode.deepCopy());
			} else {
				// Let's iterate over campus
				for (TreeNode<FacilityType> campusNode : companyNode
						.getTreeNodeList()) {
					if (campusNode.getTenantid() == tenantId) {
						tenantFacilityHierarchy.addTreeNode(campusNode
								.deepCopy());
					} else {
						// Let's iterate over building
						for (TreeNode<FacilityType> buildingNode : campusNode
								.getTreeNodeList()) {
							if (buildingNode.getTenantid() == tenantId) {
								tenantFacilityHierarchy
										.addTreeNode(buildingNode.deepCopy());
							} else {
								// Let's iterate over area
								for (TreeNode<FacilityType> floorNode : buildingNode
										.getTreeNodeList()) {
									if (floorNode.getTenantid() == tenantId) {
										tenantFacilityHierarchy
												.addTreeNode(floorNode
														.deepCopy());
									} else {
										for (TreeNode<FacilityType> areaNode : floorNode
												.getTreeNodeList()) {
											if (areaNode.getTenantid() == tenantId) {
												tenantFacilityHierarchy
														.addTreeNode(areaNode
																.deepCopy());
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

	public TreeNode<FacilityType> loadFacilityHierarchyForUser(long userId) {
		// Let's get the current user
		User user = userManager.loadUserById(userId);

		// If user role is admin, return the company hierarchy
		if (user.getRole().getRoleType() == RoleType.Admin) {
			return loadCompanyHierarchy();
		}

		// Find the effective top hierarchy
		TreeNode<FacilityType> topHierarchy = null;

		// If tenant admin, return the tenant hierarchy
		if (user.getRole().getRoleType() == RoleType.TenantAdmin) {
			topHierarchy = loadTenantFacilitiesHierarchy(user.getTenant()
					.getId());
		}

		if (user.getRole().getRoleType() == RoleType.FacilitiesAdmin
				|| user.getRole().getRoleType() == RoleType.Auditor) {
			topHierarchy = loadCompanyHierarchy();
		} else if (user.getRole().getRoleType() == RoleType.Employee) {
			if (user.getTenant() == null) {
				topHierarchy = loadCompanyHierarchy();
			} else {
				topHierarchy = loadTenantFacilitiesHierarchy(user.getTenant()
						.getId());
			}
		}

		// Let's now make the effective tree for user
		TreeNode<FacilityType> userFacilityHierarchy = new TreeNode<FacilityType>();
		userFacilityHierarchy.setNodeId("0");
		userFacilityHierarchy.setName("Root");
		userFacilityHierarchy.setNodeType(FacilityType.ROOT);

		// Let's get user locations
		Set<UserLocations> userLocations = user.getUserLocations();

		// Let put the location in a set so that we can easily
		Set<String> userLocationsSet = new HashSet<String>();

		for (UserLocations ul : userLocations) {
			userLocationsSet.add(ul.getApprovedLocationType().getName()
					+ ul.getLocationId());
		}
		// for (TreeNode<FacilityType> companyNode : topHierarchy
		// .getTreeNodeList()) {

		assignNode(userFacilityHierarchy, topHierarchy, userId,
				userLocationsSet);
		// }

		// for (TreeNode<FacilityType> companyNode : topHierarchy
		// .getTreeNodeList()) {

		//
		// // Let's see if company is included
		// if (userLocationsSet.contains(companyNode.getNodeType().getName()
		// + companyNode.getNodeId())) {
		// userFacilityHierarchy.addTreeNode(companyNode.deepCopy());
		// } else {
		// // Let's iterate over campus
		// for (TreeNode<FacilityType> campusNode : companyNode
		// .getTreeNodeList()) {
		// if (userLocationsSet.contains(campusNode.getNodeType()
		// .getName() + campusNode.getNodeId())) {
		// userFacilityHierarchy
		// .addTreeNode(campusNode.deepCopy());
		// } else {
		// // Let's iterate over building
		// for (TreeNode<FacilityType> buildingNode : campusNode
		// .getTreeNodeList()) {
		// if (userLocationsSet.contains(buildingNode
		// .getNodeType().getName()
		// + buildingNode.getNodeId())) {
		// userFacilityHierarchy.addTreeNode(buildingNode
		// .deepCopy());
		// } else {
		// // Let's iterate over area
		// for (TreeNode<FacilityType> floorNode : buildingNode
		// .getTreeNodeList()) {
		// if (userLocationsSet.contains(floorNode
		// .getNodeType().getName()
		// + floorNode.getNodeId())) {
		// userFacilityHierarchy
		// .addTreeNode(floorNode
		// .deepCopy());
		// } else {
		// for (TreeNode<FacilityType> areaNode : floorNode
		// .getTreeNodeList()) {
		// if (userLocationsSet
		// .contains(areaNode
		// .getNodeType()
		// .getName()
		// + areaNode
		// .getNodeId())) {
		// userFacilityHierarchy
		// .addTreeNode(areaNode
		// .deepCopy());
		// }
		// }
		// }
		// }
		//
		// }
		// }
		// }
		// }
		// }
		// }

		return userFacilityHierarchy;
	}

	private void assignNode(TreeNode<FacilityType> parentNode,
			TreeNode<FacilityType> topHierarchy, long userId,
			Set<String> userLocationsSet) {
		for (TreeNode<FacilityType> itrNode : topHierarchy.getTreeNodeList()) {
			if (userLocationsSet.contains(itrNode.getNodeType().getName()
					+ itrNode.getNodeId())) {
				TreeNode<FacilityType> newNode = itrNode.getShallowCopy();
				parentNode.addTreeNode(newNode);
				assignNode(newNode, itrNode, userId, userLocationsSet);
			} else {
				assignNode(parentNode, itrNode, userId, userLocationsSet);
			}

		}
	}

	public void inValidateFacilitiesTreeCache() {
		facilityTreeMap.clear();

	
		//Let' build the new cache
		loadCompanyHierarchy();

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
				Company company = (Company) companyDao.getObject(Company.class,
						facilityId);
				company.setTenant(tenant);
				companyDao.saveObject(company);
			} else if ("campus".equalsIgnoreCase(facilityType)) {
				Campus campus = (Campus) campusDao.getObject(Campus.class,
						facilityId);
				campus.setTenant(tenant);
				campusDao.saveObject(campus);
			} else if ("building".equalsIgnoreCase(facilityType)) {
				Building building = (Building) buildingDao.getObject(
						Building.class, facilityId);
				building.setTenant(tenant);
				buildingDao.saveObject(building);
			} else if ("floor".equalsIgnoreCase(facilityType)) {
				Floor floor = (Floor) floorDao.getObject(Floor.class,
						facilityId);
				floor.setTenant(tenant);
				floorDao.saveObject(floor);
			} else if ("area".equalsIgnoreCase(facilityType)) {
				Area area = (Area) areaDao.getObject(Area.class, facilityId);
				area.setTenant(tenant);
				areaDao.saveObject(area);
			}
		}
	}

	// Get bread crumb for given node type and node id in facility tree.
	public String getNodePath(String nodeType, Long nodeID) {

		String company = "", campus = "", building = "", floor = "", area = "";
		try {
			Company comp = companyManager.loadCompanyById(1L);
			company = comp.getName();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (nodeType.equalsIgnoreCase("CAMPUS")) {

			campus = campusManager.loadCampusById(nodeID).getName();

		} else if (nodeType.equalsIgnoreCase("BUILDING")) {

			campus = buildingManager.getBuildingById(nodeID).getCampus()
					.getName();
			building = buildingManager.getBuildingById(nodeID).getName();

		} else if (nodeType.equalsIgnoreCase("FLOOR")) {

			try {
				campus = floorManager.getFloorById(nodeID).getBuilding()
						.getCampus().getName();
				building = floorManager.getFloorById(nodeID).getBuilding()
						.getName();
				floor = floorManager.getFloorById(nodeID).getName();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (nodeType.equalsIgnoreCase("AREA")) {

			try {
				campus = areaManager.getAreaById(nodeID).getFloor()
						.getBuilding().getCampus().getName();
				building = areaManager.getAreaById(nodeID).getFloor()
						.getBuilding().getName();
				floor = areaManager.getAreaById(nodeID).getFloor().getName();
				area = areaManager.getAreaById(nodeID).getName();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (nodeType.equalsIgnoreCase("GROUP")) {
			company = groupManager.getGroupById(nodeID).getName();
		}
		return company + (campus != "" ? " > " + campus : "")
				+ (building != "" ? " > " + building : "")
				+ (floor != "" ? " > " + floor : "")
				+ (area != "" ? " > " + area : "");
	}

	// ---------------------------------------------------------------
	// Added by Nitin
	// Following is to save facilities assigned to user in DB
	// ---------------------------------------------------------------

	public void setUserFacilities(String[] assignedFacilities, Long userId) {

		User user = (User) userDao.getObject(User.class, userId);

		// Let's get user locations
		Set<UserLocations> userLocations = user.getUserLocations();

		// Let put the location in a set so that we can easily figure out
		// assignments
		Map<String, UserLocations> userLocationsMap = new HashMap<String, UserLocations>();

		for (UserLocations ul : userLocations) {
			userLocationsMap
					.put(ul.getApprovedLocationType().getName()
							+ ul.getLocationId(), ul);
		}

		if (assignedFacilities != null && assignedFacilities.length > 0) {
			for (String facility : assignedFacilities) {

				if (facility.length() == 0) {
					continue;
				}

				String[] facilityDetail = facility.split("_");
				String facilityType = facilityDetail[0];
				Long facilityId = Long.parseLong(facilityDetail[1]);

				UserLocations contains = userLocationsMap.remove(FacilityType
						.valueOf(facilityType.toUpperCase()).getName()
						+ facilityId);
				// Add if user locations does not exists.
				if (contains == null) {
					UserLocations location = new UserLocations();
					// userLocations.add(location);
					location.setLocationId(facilityId);
					location.setUser(user);

					if ("company".equalsIgnoreCase(facilityType)) {
						location.setApprovedLocationType(FacilityType.COMPANY);
					} else if ("campus".equalsIgnoreCase(facilityType)) {
						location.setApprovedLocationType(FacilityType.CAMPUS);
					} else if ("building".equalsIgnoreCase(facilityType)) {
						location.setApprovedLocationType(FacilityType.BUILDING);
					} else if ("floor".equalsIgnoreCase(facilityType)) {
						location.setApprovedLocationType(FacilityType.FLOOR);
					} else if ("area".equalsIgnoreCase(facilityType)) {
						location.setApprovedLocationType(FacilityType.AREA);
					}
					userManager.saveUserLocation(location);
				}
			}
		}

		// Let's get user locations
		User currentUser = (User) userDao.getObject(User.class,
				emsAuthContext.getUserId());
		Set<UserLocations> currentUserLocations = currentUser
				.getUserLocations();

		// Let put the location in a set so that we can easily figure out
		// assignments
		Map<String, UserLocations> currentUserLocationsMap = new HashMap<String, UserLocations>();

		for (UserLocations ul : currentUserLocations) {
			currentUserLocationsMap.put(ul.getApprovedLocationType().getName()
					+ ul.getLocationId(), ul);
		}

		// Let's remove all other userlocations in the hash set as they have
		// been removed now
		Iterator itr = userLocationsMap.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<String, UserLocations> pairs = (Map.Entry<String, UserLocations>) itr
					.next();
			if (currentUser.getRole().getRoleType() == RoleType.Admin) {
				userManager.deleteUserLocation(pairs.getValue());
			} else if (currentUserLocationsMap.containsKey(pairs.getKey())) {
				// Let's get the type of facility
				try {
					deleteParents(pairs.getValue().getApprovedLocationType(),
							user.getId(), pairs.getValue().getLocationId());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	private void deleteParents(FacilityType locationType, long userId,
			long locationid) throws SQLException, IOException {
		switch (locationType) {
		case AREA: {
			Area area = areaManager.getAreaUsingId(locationid);
			deleteParents(FacilityType.FLOOR, userId, area.getFloor().getId());
			break;
		}
		case FLOOR: {
			Floor floor = floorManager.getFloorById(locationid);
			deleteParents(FacilityType.BUILDING, userId, floor.getBuilding()
					.getId());
			break;
		}
		case BUILDING: {
			Building building = buildingManager.getBuildingById(locationid);
			deleteParents(FacilityType.CAMPUS, userId, building.getCampus()
					.getId());
			break;
		}
		case CAMPUS: {
			Campus campus = campusManager.loadCampusById(locationid);
			deleteParents(FacilityType.COMPANY, userId, 1);
			break;
		}
		case COMPANY: {
			break;
		}
		}

		UserLocations location = userLocationsManger.loadUserLocation(userId,
				locationType, locationid);
		if (location != null) {
			userManager.deleteUserLocation(location);
		}

	}

	public void setSweepTimerToFacilities(String[] assignedFacilities) {
		for (String facility : assignedFacilities) {
			String[] facilityDetail = facility.split("_");
			String facilityType = facilityDetail[0];
			Long facilityId = Long.parseLong(facilityDetail[1]);
			Long sweepTimerId = Long.parseLong(facilityDetail[2]);
			// System.out.println("Sweep Timer ------> " + facilityType + ", " +
			// facilityId + " " + sweepTimerId );
			if (sweepTimerId == 0) {
				sweepTimerId = null;
			}

			if ("company".equalsIgnoreCase(facilityType)) {
				Company company = (Company) companyDao.getObject(Company.class,
						facilityId);
				company.setSweepTimerId(sweepTimerId);
				companyDao.saveObject(company);
			} else if ("campus".equalsIgnoreCase(facilityType)) {
				Campus campus = (Campus) campusDao.getObject(Campus.class,
						facilityId);
				campus.setSweepTimerId(sweepTimerId);
				campusDao.saveObject(campus);
			} else if ("building".equalsIgnoreCase(facilityType)) {
				Building building = (Building) buildingDao.getObject(
						Building.class, facilityId);
				building.setSweepTimerId(sweepTimerId);
				buildingDao.saveObject(building);
			} else if ("floor".equalsIgnoreCase(facilityType)) {
				Floor floor = (Floor) floorDao.getObject(Floor.class,
						facilityId);
				floor.setSweepTimerId(sweepTimerId);
				floorDao.saveObject(floor);
		
			} else if ("area".equalsIgnoreCase(facilityType)) {
				Area area = (Area) areaDao.getObject(Area.class, facilityId);
				area.setSweepTimerId(sweepTimerId);
				areaDao.saveObject(area);
			
			}
		}
	}

}
