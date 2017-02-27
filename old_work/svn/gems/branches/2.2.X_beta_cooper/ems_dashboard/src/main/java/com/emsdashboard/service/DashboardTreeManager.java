package com.emsdashboard.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emsdashboard.dao.GemsDao;
import com.emsdashboard.types.FacilityType;
import com.emsdashboard.utils.tree.TreeNode;

@Service("dashboardTreeManager")
@Transactional(propagation = Propagation.REQUIRED)
public class DashboardTreeManager {

    @Resource
    private GemsDao gemsDao;

    public TreeNode<FacilityType> loadGEMShierarchy() {
        TreeNode<FacilityType> gemsHierachy = (TreeNode<FacilityType>) gemsDao.loadGEMShierarchy();
        return gemsHierachy;
    }
    
}
