/**
 * 
 */
package com.ems.dao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author yogesh
 * 
 */
@Repository("motionBitsRecord")
@Transactional(propagation = Propagation.REQUIRED)
public class MotionBitRecordDao extends BaseDaoHibernate {

}
