/**
 * 
 */
package com.ems.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.MotionBitRecordDao;
import com.ems.model.MotionBitRecord;

/**
 * @author yogesh
 * 
 */
@Service("motionBitRecordManager")
@Transactional(propagation = Propagation.REQUIRED)
public class MotionBitRecordManager {

    @Resource
    MotionBitRecordDao motionBitRecordDao;

    public MotionBitRecord save(MotionBitRecord motionBitRecord) {
        return (MotionBitRecord) motionBitRecordDao.saveObject(motionBitRecord);
    }
}
