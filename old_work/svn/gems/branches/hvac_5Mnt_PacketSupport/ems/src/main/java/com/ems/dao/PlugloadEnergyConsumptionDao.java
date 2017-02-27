package com.ems.dao;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.PlugloadEnergyConsumption;
import com.ems.utils.DateUtil;

@Repository("plugloadEnergyConsumptionDao")
@Transactional(propagation = Propagation.REQUIRED)
public class PlugloadEnergyConsumptionDao extends BaseDaoHibernate{
	
	public List<Object[]> getLatestPlugloadEnergyConsumptionRecords(Long plugloadId){
		
		String sql = "select max(capture_at), plugload_id,managed_energy_cum,unmanaged_energy_cum from "
				+ "plugload_energy_consumption p	where zero_bucket = 0  and plugload_id = "+plugloadId
				+ "group by capture_at,plugload_id,managed_energy_cum,unmanaged_energy_cum "
				+ "order by capture_At desc limit 2";
		
		Query q = getSession().createSQLQuery(sql);
		
		List<Object[]> l = q.list();		
		return l;
		
	}

	public void updateZeroBuckets(Long id, Long spreadManagedEnertyCumValues,
			Long spreadUnManagedEnertyCumValues, Date d1, Date d2) {
		String sql ="update plugload_energy_consumption set energy ="+spreadManagedEnertyCumValues+" , unmanaged_energy="+spreadUnManagedEnertyCumValues+""
				+ ", zero_bucket = 2 where plugload_id="+id+" and capture_at > '"+DateUtil.formatDate(d2, "yyyy-MM-dd HH:mm:ss")+"' and capture_at < '"+DateUtil.formatDate(d1, "yyyy-MM-dd HH:mm:ss")+"'";
		System.out.println("===sql queyr is "+sql);
		Query q = getSession().createSQLQuery(sql);
		q.executeUpdate();
		
	}
	
	
	
}
