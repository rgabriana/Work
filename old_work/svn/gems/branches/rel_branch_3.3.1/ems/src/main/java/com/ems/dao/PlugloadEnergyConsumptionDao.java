package com.ems.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	public Map<String,Map<Long,List<Object[]>>> getAllPlugloadEnergyConsumptionZBRecords(Long plugloadId){
		String existingKey = "";
		Date previousDate = null;
		List<Object[]> l1,l2 = null;
		Object nonZbObjects[] = null;
		String sql = "select capture_at,plugload_id from plugload_energy_consumption where zero_bucket = 1 and plugload_id="+plugloadId+"  order by capture_at";
		String sql2,sql3  ="";
		Query q = getSession().createSQLQuery(sql);
		Map<String,Map<Long,List<Object[]>>> map = new HashMap<String, Map<Long,List<Object[]>>>();
		List<Object[]> l = q.list();	
		if(l != null){
			for(Object [] zbObjects : l){
				sql2 = "select min(capture_at) as capt_at,plugload_id,managed_energy_cum,unmanaged_energy_cum from plugload_energy_consumption where zero_bucket = 0 "
						+ "and capture_at > '"+zbObjects[0].toString()+"'"
						+ "group by capture_at,plugload_id,managed_energy_cum,unmanaged_energy_cum order by capt_at limit 1";
				sql3 = "select max(capture_at) as capt_at,plugload_id,managed_energy_cum,unmanaged_energy_cum from plugload_energy_consumption where zero_bucket = 0 "
						+ "and capture_at < '"+zbObjects[0].toString()+"'"
						+ " group by capture_at,plugload_id,managed_energy_cum,unmanaged_energy_cum order by capt_at desc limit 1";
				//System.out.println("=========  sql2 is "+sql2);
				q = getSession().createSQLQuery(sql2);	
				l1 = q.list();
				q = getSession().createSQLQuery(sql3);
				l2 = q.list();
				if(l1 != null && l1.size() > 0){
					nonZbObjects = l1.get(0);					
					List<Object[]> nonZBObjectsList = new ArrayList<Object[]>();
					nonZBObjectsList.add(nonZbObjects);
					nonZBObjectsList.add(l2.get(0));
					//System.out.println("for captat "+DateUtil.formatDate((Date)zbObjects[0], "yyyyMMddHHmmss")+" first nzb is "+nonZbObjects[0]);
					existingKey = DateUtil.formatDate((Date)zbObjects[0], "yyyyMMddHHmmss");
					if(previousDate == null){
						Map<Long,List<Object[]>> m = new HashMap<Long, List<Object[]>>();						
						m.put(plugloadId, nonZBObjectsList);
						map.put(DateUtil.formatDate((Date)zbObjects[0], "yyyyMMddHHmmss"), m);					
					}else if(DateUtil.getDateDiffInMinutes(previousDate, (Date)zbObjects[0]) > 5){
						if(map.containsKey(existingKey)){					
							map.get(existingKey).put(plugloadId, nonZBObjectsList);
						}else{
							Map<Long,List<Object[]>> m = new HashMap<Long, List<Object[]>>();
							m.put(plugloadId, nonZBObjectsList);
							map.put(DateUtil.formatDate((Date)zbObjects[0], "yyyyMMddHHmmss"), m);
						}						
					}/*else{
						System.out.println("============ difference is less than 5 mins"+previousDate+" "+o[0].toString());
					}*/
				}
				previousDate = (Date)zbObjects[0];
			}
		}
		return map;		
	}
	
	public void updateZeroBuckets(Long id, Long spreadManagedEnertyCumValues,
			Long spreadUnManagedEnertyCumValues, Date nzbCaptat, Date zbCaptat) {
		String sql ="update plugload_energy_consumption set energy ="+spreadManagedEnertyCumValues+" , unmanaged_energy="+spreadUnManagedEnertyCumValues+""
				+ ", zero_bucket = 2 where plugload_id="+id+" and capture_at >= '"+DateUtil.formatDate(zbCaptat, "yyyy-MM-dd HH:mm:ss")+"' and capture_at < '"+DateUtil.formatDate(nzbCaptat, "yyyy-MM-dd HH:mm:ss")+"'";
		//System.out.println("===sql queyr is "+sql);
		Query q = getSession().createSQLQuery(sql);
		q.executeUpdate();
		
	}
	
	public PlugloadEnergyConsumption getPlugloadEnergyConsumptionFromDB(Date captureAt, Long plugloadId){
    	//System.out.println("======date is "+captureAt);
		String hsql = " from PlugloadEnergyConsumption p where p.captureAt=? and p.plugload.id=?";
    	Query q = getSession().createQuery(hsql);
    	q.setParameter(0, captureAt);
    	q.setLong(1, plugloadId);    	
    	PlugloadEnergyConsumption pec = (PlugloadEnergyConsumption) q.uniqueResult();
//    	System.out.println("================= pec"+pec);
    	return pec;
    }
	
}
