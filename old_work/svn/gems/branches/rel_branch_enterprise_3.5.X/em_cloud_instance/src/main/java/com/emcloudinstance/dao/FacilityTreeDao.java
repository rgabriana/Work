package com.emcloudinstance.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emcloudinstance.types.FacilityType;
import com.emcloudinstance.util.tree.TreeNode;
import com.emcloudinstance.vo.Building;
import com.emcloudinstance.vo.Campus;
import com.emcloudinstance.vo.Floor;


/**
 * 
 * @author Sampath Akula
 * 
 */
@Repository("facilityTreeDao")
@Transactional(propagation = Propagation.REQUIRED)
public class FacilityTreeDao extends AbstractJdbcDao {
	
	static final Logger logger = Logger.getLogger(FacilityTreeDao.class
			.getName());

    public TreeNode<String> loadCompanyHierarchyForEmInstance(String emMac) {

        TreeNode<String> rootNode = new TreeNode<String>();
        rootNode.setNodeId(0L);
        rootNode.setName("Root");
        rootNode.setNodeType(FacilityType.ROOT);
        TreeNode<String> companyNode = new TreeNode<String>();
        
        
        JdbcTemplate jdbcTemplate = this.getJdbcTemplate(emMac);
        Long companyId = 1L;
        String companyName = "";
        List<String> result = null;
		try {
			String companyNamehsql = "select name from company where id = 1";
			result = jdbcTemplate.queryForList(companyNamehsql,String.class);
			if (result.isEmpty() || result == null) {
				companyName = "";
		    } else {
		    	companyName =  result.get(0);
		    }
		} catch (Exception ex) {
			logger.error(
					"error while getting Company Details for mac :- "
							+ emMac, ex);
		}
		 
        
        if (!"".equals(companyName)) {
            companyNode.setNodeId(companyId);
            companyNode.setName(companyName);
            companyNode.setNodeType(FacilityType.ORGANIZATION);
            rootNode.addTreeNode(companyNode);
            
            String campushsql = "select * from campus where company_id = 1";
            
            List<Campus> campuslist = new ArrayList<Campus>();
            
            List<Map<String, Object>> campusrows = jdbcTemplate.queryForList(campushsql);
        	for (Map<String, Object> campusrow : campusrows) {
        		Campus campus = new Campus();
        		campus.setId((Long)(campusrow.get("ID")));
        		campus.setName((String)campusrow.get("NAME"));
        		campuslist.add(campus);
        	}
            
            Collections.sort(campuslist, new CampusComparator());
            
            for (Campus campus : campuslist) {
                TreeNode<String> campusNode = new TreeNode<String>();
                campusNode.setNodeId(campus.getId());
                campusNode.setName(campus.getName());
                campusNode.setNodeType(FacilityType.CAMPUS);
                                
                companyNode.addTreeNode(campusNode);
                
                
                String buildinghsql = "select * from building where campus_id = "+campus.getId();
                List<Building> buildinglist = new ArrayList<Building>();
                
                List<Map<String, Object>> buildingrows = jdbcTemplate.queryForList(buildinghsql);
            	for (Map<String, Object> buildingrow : buildingrows) {
            		Building building = new Building();
            		building.setId((Long)(buildingrow.get("ID")));
            		building.setName((String)buildingrow.get("NAME"));
            		buildinglist.add(building);
            	}
                
            	Collections.sort(buildinglist, new BuildingComparator());
                
            	for (Building building : buildinglist) {
                    TreeNode<String> buildingNode = new TreeNode<String>();
                    buildingNode.setNodeId(building.getId());
                    buildingNode.setName(building.getName());
                    buildingNode.setNodeType(FacilityType.BUILDING);
                         
                    campusNode.addTreeNode(buildingNode);
                    
                    
                    String floorhsql = "select * from floor where building_id = "+building.getId();
                    List<Floor> floorlist = new ArrayList<Floor>();
                    
                    List<Map<String, Object>> floorrows = jdbcTemplate.queryForList(floorhsql);
                	for (Map<String, Object> floorrow : floorrows) {
                		Floor floor = new Floor();
                		floor.setId((Long)(floorrow.get("ID")));
                		floor.setName((String)floorrow.get("NAME"));
                		floorlist.add(floor);
                	}
                    
                	Collections.sort(floorlist, new FloorComparator());
                    
                    
                    for (Floor floor : floorlist) {
                        TreeNode<String> floorNode = new TreeNode<String>();
                        floorNode.setNodeId(floor.getId());
                        floorNode.setName(floor.getName());
                        floorNode.setNodeType(FacilityType.FLOOR);
                        
                        buildingNode.addTreeNode(floorNode);
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
    
}
