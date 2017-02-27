package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.ModulePermmissionDao;

/**
 * 
 * @author Abhishek Sinha
 * 
 */
@Service("modulePermmissionManager")
@Transactional(propagation = Propagation.REQUIRED)
public class ModulePermmissionManager {

    @Resource
    private ModulePermmissionDao modulePermmissionDao;

    public List<String[]> loadModulePermissionByRoleId(Long roleId) {
        return modulePermmissionDao.loadModulePermissionByRoleId(roleId);
    }
}
