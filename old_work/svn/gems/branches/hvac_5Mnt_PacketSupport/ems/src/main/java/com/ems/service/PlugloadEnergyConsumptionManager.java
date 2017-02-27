package com.ems.service;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.PlugloadEnergyConsumptionDao;
import com.ems.model.PlugloadEnergyConsumption;

@Service("plugloadEnergyConsumptionManager")
@Transactional(propagation = Propagation.REQUIRED)
public class PlugloadEnergyConsumptionManager {
	
	static final Logger logger = Logger.getLogger("WSLogger");
	@Resource
    private PlugloadEnergyConsumptionDao plugloadEnergyConsumptionDao;
	
	
	/**
     * save PlugloadEnergyConsumption details.
     * 
     * @param plugloadEnergyConsumption
     *            com.ems.model.PlugloadEnergyConsumption
     */
    public PlugloadEnergyConsumption save(PlugloadEnergyConsumption energyConsumption) {
        return (PlugloadEnergyConsumption) plugloadEnergyConsumptionDao.saveObject(energyConsumption);
    }
    
    /**
     * save PlugloadEnergyConsumption details.
     * 
     * @param plugloadEnergyConsumption
     *            com.ems.model.PlugloadEnergyConsumption
     */
    public void save(Map<String, PlugloadEnergyConsumption> map) {
    	if(map!= null){			
			Iterator<String> it1 = map.keySet().iterator();
			plugloadEnergyConsumptionDao.setFlushMode(FlushMode.MANUAL);	
			try{
				while (it1.hasNext()) {
					
					String snapAddress = it1.next();
					System.out.println("iterating in map"+snapAddress);
					PlugloadEnergyConsumption pec = map.get(snapAddress);				
					try {
						logger.info("saving energy consumption object for plugload with snap address "+pec.getPlugload().getSnapAddress()+" and captureAt "+pec.getCaptureAt());
						plugloadEnergyConsumptionDao.save(pec);
					} catch (Exception e) {
						logger.error("exception occurred while saving EC object of Plugload "+pec.getPlugload().getSnapAddress()+" for captureAt "+pec.getCaptureAt());
						System.out.println("error while saving "+pec.getPlugload().getSnapAddress());
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
				}				
			} catch(Exception e){
				e.printStackTrace();
			}finally{
				plugloadEnergyConsumptionDao.flush();
					
			}
		}
    	
    }

    /**
     * update PlugloadEnergyConsumption details.
     * 
     * @param plugloadEnergyConsumption
     *            com.ems.model.PlugloadEnergyConsumption
     */
    public PlugloadEnergyConsumption update(PlugloadEnergyConsumption energyConsumption) {
        return (PlugloadEnergyConsumption) plugloadEnergyConsumptionDao.saveObject(energyConsumption);
    }

    /**
     * Delete PlugloadEnergyConsumption details
     * 
     * @param id
     *            database id(primary key)
     */
    public void delete(Long id) {
    	plugloadEnergyConsumptionDao.removeObject(PlugloadEnergyConsumption.class, id);
    }
    
    public List<Object[]> getLatestEnergyConsumptionByPlugload(Long plugloadId){
    	return plugloadEnergyConsumptionDao.getLatestPlugloadEnergyConsumptionRecords(plugloadId);
    }

	public void updateZeroBuckets(Long id, Long spreadManagedEnertyCumValues,
			Long spreadUnManagedEnertyCumValues, Date d1, Date d2) {
		plugloadEnergyConsumptionDao.updateZeroBuckets(id,spreadManagedEnertyCumValues,spreadUnManagedEnertyCumValues,d1,d2);
		
	}

}
