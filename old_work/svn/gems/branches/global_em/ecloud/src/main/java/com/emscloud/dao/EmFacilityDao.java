package com.emscloud.dao;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communication.utils.ArgumentUtils;
import com.emscloud.model.EmFacility;
import com.emscloud.types.FacilityType;


@Repository("emFacilityDao")
@Transactional(propagation = Propagation.REQUIRED,readOnly=false)
public class EmFacilityDao extends BaseDaoHibernate{
	
	@SuppressWarnings("unchecked")
	public List<EmFacility> getEMFacilitiesByFacilityType(FacilityType type, Long emId){
    	
   	 	List<EmFacility> facilityList = sessionFactory.getCurrentSession().createCriteria(EmFacility.class)
   	 			.add(Restrictions.eq("type", type))
                .add(Restrictions.eq("emId", emId)).list();
        if (!ArgumentUtils.isNullOrEmpty(facilityList)) {
            return facilityList;
        } else {
            return null;
        }
    }
	
	@SuppressWarnings("unchecked")
	public List<EmFacility> getEMFacilitiesByEmId(Long emId){
    	
   	 	List<EmFacility> facilityList = sessionFactory.getCurrentSession().createCriteria(EmFacility.class)
                .add(Restrictions.eq("emId", emId)).list();
        if (!ArgumentUtils.isNullOrEmpty(facilityList)) {
            return facilityList;
        } else {
            return null;
        }
    }
	

}
