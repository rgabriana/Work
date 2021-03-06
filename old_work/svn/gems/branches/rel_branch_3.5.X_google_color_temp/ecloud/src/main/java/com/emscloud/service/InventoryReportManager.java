package com.emscloud.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.InventoryReportDao;
import com.emscloud.model.EmInstance;
import com.emscloud.vo.AggregatedSiteReport;
import com.emscloud.vo.SiteReportVo;

@Service("inventoryReportManager")
@Transactional(propagation = Propagation.REQUIRED)
public class InventoryReportManager {
	static final Logger logger = Logger.getLogger(InventoryReportManager.class.getName());
	
	@Resource
	private SiteManager siteManager;
	@Resource
	private EmInstanceManager emInstanceManager;
	@Resource
	private InventoryReportDao inventoryReportDao;
    public AggregatedSiteReport loadSiteReportListByCustomerId(Long custId, String userdata) throws UnsupportedEncodingException {
        //long total, records = 0;
        AggregatedSiteReport aggregatedSiteReport = null;
        String orderBy = null;
        String orderWay = null;
        String query = null;
        String searchField = null;
        String searchString = null;
        String searchOper = "cn";
        Boolean bSearch = false;
        int page = 0;
        if(userdata!=null)
        {
            String[] input = userdata.split("&");
            String[] params = null;

            if (input != null && input.length > 0) {
                for (String each : input) {
                    String[] keyval = each.split("=", 2);
                    if (keyval[0].equals("page")) {
                        page = Integer.parseInt(keyval[1]);
                    } else if (keyval[0].equals("userData")) {
                        query = URLDecoder.decode(keyval[1], "UTF-8");
                        params = query.split("#");
                    } else if (keyval[0].equals("sidx")) {
                        orderBy = keyval[1];
                    } else if (keyval[0].equals("sord")) {
                        orderWay = keyval[1];
                    }
                }
            }
            
            if (params != null && params.length > 0) {
    			if (params[1] != null && !"".equals(params[1])) {
    				searchField = params[1];
    			} else {
    				searchField = null;
    			}

    			if (params[2] != null && !"".equals(params[2])) {
    				searchString = URLDecoder.decode(params[2], "UTF-8");
    			} else {
    				searchString = null;
    			}
    			
    			if (params[3] != null && !"".equals(params[3])) {
    				bSearch = true;
    			} else {
    				bSearch = false;
    			}

    		}
            
            aggregatedSiteReport = siteManager.loadSitesByCustomerWithSpecificAttibute(custId,orderBy, orderWay, bSearch, searchField, searchString, searchOper, (page - 1) * AggregatedSiteReport.DEFAULT_ROWS, AggregatedSiteReport.DEFAULT_ROWS);
        }else
        {
            aggregatedSiteReport = siteManager.loadSitesByCustomerWithSpecificAttibute(custId,orderBy, orderWay, bSearch, searchField, searchString, searchOper, (page - 1) * AggregatedSiteReport.DEFAULT_ROWS, -1);
        }
        aggregatedSiteReport.setPage(page);
        //get all the sites for the selected customer
        
        if(aggregatedSiteReport!=null && aggregatedSiteReport.getSiteReport()!=null)
        {
            Iterator<SiteReportVo> siteIter = aggregatedSiteReport.getSiteReport().iterator(); 
            while(siteIter.hasNext()) {
                SiteReportVo site = siteIter.next();   
                Long totalFixtureCount = (long) 0;
                Long totalSensorCount = (long) 0;
                Long totalCuCount = (long) 0;
                Long totalErcCount = (long) 0;
                Long totalgatwayCount = (long) 0;
                Long totalBallastCount = (long) 0;
                Long totalBulbCount = (long) 0;
                Long totalFxTypeCount = (long) 0;
                //get all the em instances of the site
                List<Long> emIdList = siteManager.getSiteEms(site.getId());
                Iterator<Long> emIdIter = emIdList.iterator();
                while(emIdIter.hasNext()) {
                    EmInstance emInst = emInstanceManager.loadEmInstanceById(emIdIter.next());
                    if (emInst == null) {
                    	logger.warn(site.getId() + " Incorrect em mapping!");
                    	continue;
                    }
                    else if (!(emInst.getSppaEnabled() && emInst.getReplicaServer() != null)) {
                    	logger.warn(emInst.getName() + " is not sppa enabled. Ignoring in the report.");
                    	continue;
                    }
                    SiteReportVo siteReportVo = inventoryReportDao.getAggregatedInventoryReport(emInst.getDatabaseName(), emInst.getReplicaServer().getInternalIp());
                    if (siteReportVo != null) {
	                    if(siteReportVo.getFixtureCount()!=null)
	                    {
	                        totalFixtureCount+=siteReportVo.getFixtureCount();
	                    }
	                    if(siteReportVo.getSensorCount()!=null)
	                    {
	                        totalSensorCount+=siteReportVo.getSensorCount();
	                    }
	                    if(siteReportVo.getCuCount()!=null)
	                    {
	                        totalCuCount+=siteReportVo.getCuCount();
	                    }
	                    if(siteReportVo.getGatewayCount()!=null)
	                    {
	                        totalgatwayCount+=siteReportVo.getGatewayCount();
	                    }
	                    if(siteReportVo.getBallastCount()!=null)
	                    {
	                        totalBallastCount+=siteReportVo.getBallastCount();
	                    }
	                    if(siteReportVo.getLampsCount()!=null)
	                    {
	                        totalBulbCount+=siteReportVo.getLampsCount();
	                    }
	                    if(siteReportVo.getFxTypeCount()!=null)
	                    {
	                        totalFxTypeCount+=siteReportVo.getFxTypeCount();
	                    }
                    }else {
						System.out.println("Unable to fetch siteobject for "
								+ emInst.getDatabaseName() + " from "
								+ emInst.getReplicaServer().getInternalIp());
                    }
                    
                    //GET ERC Count
                    List<Object[]> totalCommissionedErc = inventoryReportDao.getErcCountByVersionNo(emInst.getDatabaseName(), emInst.getReplicaServer().getInternalIp());
                    if (totalCommissionedErc != null && !totalCommissionedErc.isEmpty()) {
                        Iterator<Object[]> erciterator = totalCommissionedErc.iterator();
                        while (erciterator.hasNext()) {
                            Object[] itrObject = (Object[]) erciterator.next();
                            Long count = (Long) itrObject[1];
                            totalErcCount+= count;
                        }
                    }

                }
                site.setBallastCount(totalBallastCount);
                site.setFixtureCount(totalFixtureCount);
                site.setGatewayCount(totalgatwayCount);
                site.setLampsCount(totalBulbCount);
                site.setName(site.getName());
                site.setSensorCount(totalSensorCount);
                site.setCuCount(totalCuCount);
                site.setFxTypeCount(totalFxTypeCount);
                site.setCustomer(site.getCustomer());
                site.setErcCount(totalErcCount);
            }
        }
        return aggregatedSiteReport;
    }
    
    public HashMap<String, Long> getFixturesCountByModelNo(String dbName, String replicaServerHost)
    {
        //Merge the NULL model no into SU2 and also Put Fixture Type like SU2, Ruggedized, Compact into Map
        List<Object[]> inventoryReportSensorList = inventoryReportDao.getFixturesCountByModelNo(dbName,replicaServerHost);
        Long su2Count=(long) 0;
        Long totalFixtures=(long) 0;
        
        Long compactSuCount = (long) 0;
        Long ruggedizedSuCount = (long) 0;
        Long unIdentifiedSuCount = (long) 0;
        Long fixtureMountCount = (long)0;
        HashMap<String, Long> totalCommissionedSensors = new HashMap<String, Long>();
        if (inventoryReportSensorList != null && !inventoryReportSensorList.isEmpty()) {
            for (Iterator<Object[]> iterator = inventoryReportSensorList.iterator(); iterator.hasNext();) {
                Object[] object = (Object[]) iterator.next();
                String modelNo = (String) object[0];
                Long count = (Long) object[1];
                if(modelNo!=null)
                {
                    if(modelNo.startsWith("SU-2"))
                    {
                        su2Count+=count;
                    }else if(modelNo.startsWith("RS"))
                    {
                        ruggedizedSuCount+=count;
                    }else if(modelNo.startsWith("SU-3E"))
                    {
                        compactSuCount+=count;
                    }else if(modelNo.startsWith("FM"))
                    {
                    	fixtureMountCount+=count;
                    }
                    else {
                        unIdentifiedSuCount+=count;
                    }
                }else
                {
                    unIdentifiedSuCount+=count;
                }
                totalFixtures+=count;
             }
         totalCommissionedSensors.put("SU2", su2Count);
         totalCommissionedSensors.put("Unidentified SU", unIdentifiedSuCount);
         totalCommissionedSensors.put("Ruggedized", ruggedizedSuCount);
         totalCommissionedSensors.put("Fixture Mount", fixtureMountCount);
         totalCommissionedSensors.put("Compact", compactSuCount);
         totalCommissionedSensors.put("TotalCount", totalFixtures);
       }
       return totalCommissionedSensors;
    }
    
    public HashMap<String, Long> getOtherDevicesCount(String dbName, String replicaServerHost)
    {
        List<Object[]> inventoryReportOtherDeviceList = inventoryReportDao.getOtherDevicesCount(dbName,replicaServerHost);
        Long mPlugloadListCount = inventoryReportDao.loadAllCommissionedPlugloadsCount(dbName,replicaServerHost);
        Long unManagedFixtureCnt=(long) 0;
        Long unManagedEmergencyFixtureCnt=(long) 0;
        Long totalOtherDeviceCnt=(long) 0;
        HashMap<String, Long> totalOtherDevices = new HashMap<String, Long>();
        if (inventoryReportOtherDeviceList != null && !inventoryReportOtherDeviceList.isEmpty()) {
            for (Iterator<Object[]> iterator = inventoryReportOtherDeviceList.iterator(); iterator.hasNext();) {
                Object[] object = (Object[]) iterator.next();
                String deviceType = (String) object[0];
                Long count = (Long) object[1];
                if(deviceType!=null)
                {
                    if(deviceType.startsWith("Unmanaged_fixture"))
                    {
                    	unManagedFixtureCnt+=count;
                    }else if(deviceType.startsWith("Unmanaged_emergency_fixture"))
                    {
                    	unManagedEmergencyFixtureCnt+=count;
                    }
                }
                totalOtherDeviceCnt+=count;
                totalOtherDevices.put("Unmanaged Emergency Fixture", unManagedEmergencyFixtureCnt);
                totalOtherDevices.put("Unmanaged Fixture", unManagedFixtureCnt);
                totalOtherDevices.put("TotalCount", totalOtherDeviceCnt);
            }
        }
        totalOtherDeviceCnt+=mPlugloadListCount;
        totalOtherDevices.put("TotalCount", totalOtherDeviceCnt);
        totalOtherDevices.put("Plugload", mPlugloadListCount);
       return totalOtherDevices;
    }
        
    public List<Object[]> getCusCountByVersionNo(String dbName, String replicaServerHost)
    {
        return inventoryReportDao.getCusCountByVersionNo(dbName,replicaServerHost);
    }
    
    public List<Object[]> getBallastCountByBallastName(String dbName, String replicaServerHost)
    {
       return  inventoryReportDao.getBallastCountByBallastName(dbName,replicaServerHost);
    }
    public List<Object[]> getBulbsCountByBulbName(String dbName, String replicaServerHost)
    {
       return  inventoryReportDao.getBulbsCountByBulbName(dbName,replicaServerHost);
    }
    public List<Object[]> getCommissionedFxTypeCount(String dbName, String replicaServerHost)
    {
        return inventoryReportDao.getCommissionedFxTypeCount(dbName,replicaServerHost);
    }

	public void getFixturesCountByModelNo(String dbName,
			String replicaServerHost, Map<String, Long> oMap) {
		// Merge the NULL model no into SU2 and also Put Fixture Type like SU2,
		// Ruggedized, Compact into Map
		List<Object[]> inventoryReportSensorList = inventoryReportDao
				.getFixturesCountByModelNo(dbName, replicaServerHost);
		if (inventoryReportSensorList != null
				&& !inventoryReportSensorList.isEmpty()) {
			for (Iterator<Object[]> iterator = inventoryReportSensorList
					.iterator(); iterator.hasNext();) {
				Object[] object = (Object[]) iterator.next();
				String key = (String) object[0];
				Long count = (Long) object[1];
				if (count == null)
					count = 0L;
				if (key != null) {
					if (key.toUpperCase().startsWith("SU-2")) {
						key = "SU2";
					} else if (key.toUpperCase().startsWith("RS")) {
						key = "Ruggedized";
					} else if (key.toUpperCase().startsWith("SU-3E")) {
						key = "Compact";
					} else {
						key = "SU";
					}
				}
				Long value = oMap.get(key);
				if (value == null) {
					oMap.put(key, count);
				} else {
					oMap.put(key, (value.longValue() + count.longValue()));
				}
			}
		}
	}

    /**
     * CUversion -> count Map
     * @param dbName
     * @param replicaServerHost
     * @return
     */
	public void getCuCountByVersionNo(String dbName, String replicaServerHost,
			Map<String, Long> oMap) {
		List<Object[]> inventoryReportCuList = inventoryReportDao
				.getCusCountByVersionNo(dbName, replicaServerHost);
		if (inventoryReportCuList != null && !inventoryReportCuList.isEmpty()) {
			for (Iterator<Object[]> iterator = inventoryReportCuList.iterator(); iterator
					.hasNext();) {
				Object[] object = (Object[]) iterator.next();
				String key = "CUv" + (String) object[0];
				Long count = (Long) object[1];
				if (count == null)
					count = 0L;
				Long value = oMap.get(key);
				if (value == null) {
					oMap.put(key, count);
				} else {
					oMap.put(key, (value.longValue() + count.longValue()));
				}
			}
		}
	}
	
	/**
	 * Ballast name -> count Map
	 * @param dbName
	 * @param replicaServerHost
	 * @return
	 */
	public void getBallastCountByType(String dbName, String replicaServerHost,
			Map<String, Long> oMap) {
		List<Object[]> inventoryReportList = inventoryReportDao
				.getBallastCountByBallastDisplayName(dbName, replicaServerHost);
		if (inventoryReportList != null && !inventoryReportList.isEmpty()) {
			for (Iterator<Object[]> iterator = inventoryReportList.iterator(); iterator
					.hasNext();) {
				Object[] object = (Object[]) iterator.next();
				String key = (String) object[0];
				Long count = (Long) object[1];
				if (count == null)
					count = 0L;
				Long value = oMap.get(key);
				if (value == null) {
					oMap.put(key, count);
				} else {
					oMap.put(key, (value.longValue() + count.longValue()));
				}
			}
		}
	}

	/**
	 * Lamp type -> count
	 * @param dbName
	 * @param replicaServerHost
	 * @return
	 */
	public void getBulbsCountByType(String dbName, String replicaServerHost,
			Map<String, Long> oMap) {
		List<Object[]> inventoryReportList = inventoryReportDao
				.getBulbsCountByBulbName(dbName, replicaServerHost);
		if (inventoryReportList != null && !inventoryReportList.isEmpty()) {
			for (Iterator<Object[]> iterator = inventoryReportList.iterator(); iterator
					.hasNext();) {
				Object[] object = (Object[]) iterator.next();
				String key = (String) object[0];
				Long count = (Long) object[2];
				if (count == null)
					count = 0L;
				Long value = oMap.get(key);
				if (value == null) {
					oMap.put(key, count);
				} else {
					oMap.put(key, (value.longValue() + count.longValue()));
				}
			}
		}
	}
	
	public void getGatewayCount(String dbName, String replicaServerHost,
			Map<String, Long> oMap) {
		List<Object[]> inventoryReportList = inventoryReportDao
				.getGatewayCount(dbName, replicaServerHost);
		if (inventoryReportList != null && !inventoryReportList.isEmpty()) {
			for (Iterator<Object[]> iterator = inventoryReportList.iterator(); iterator
					.hasNext();) {
				Object[] object = (Object[]) iterator.next();
				Long fxcount = (Long) object[0];
				Long gwcount = (Long) object[1];
				if (fxcount == null)
					fxcount = 0L;
				if (gwcount == null)
					gwcount = 0L;

				Long value = oMap.get("FX");
				if (value == null) {
					oMap.put("FX", fxcount);
				} else {
					oMap.put("FX", (value.longValue() + fxcount.longValue()));
				}

				value = oMap.get("GW");
				if (value == null) {
					oMap.put("GW", gwcount);
				} else {
					oMap.put("GW", (value.longValue() + gwcount.longValue()));
				}
			}
		}
	}

	public List<Object[]> getErcCountByVersionNo(String dbName,
			String replicaServerHost) {
		  return inventoryReportDao.getErcCountByVersionNo(dbName,replicaServerHost);
	}

	public void getErcCountByVersionNo(String dbName, String replicaServerHost,
			Map<String, Long> ercCountsMap) {
		List<Object[]> inventoryReportErcList = inventoryReportDao
				.getErcCountByVersionNo(dbName, replicaServerHost);
		if (inventoryReportErcList != null && !inventoryReportErcList.isEmpty()) {
			for (Iterator<Object[]> iterator = inventoryReportErcList.iterator(); iterator
					.hasNext();) {
				Object[] object = (Object[]) iterator.next();
				String key = "ERC (Version - " + (String) object[0]+")";
				Long count = (Long) object[1];
				if (count == null)
					count = 0L;
				Long value = ercCountsMap.get(key);
				if (value == null) {
					ercCountsMap.put(key, count);
				} else {
					ercCountsMap.put(key, (value.longValue() + count.longValue()));
				}
			}
		}
		
	}


} //end of class inventoryReportManager
