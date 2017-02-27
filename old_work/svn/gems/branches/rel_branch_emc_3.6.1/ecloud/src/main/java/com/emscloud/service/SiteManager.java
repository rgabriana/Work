package com.emscloud.service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communication.types.DatabaseState;
import com.communication.types.EmStatus;
import com.emscloud.dao.CustomerDao;
import com.emscloud.dao.SiteDao;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmInstanceList;
import com.emscloud.model.EmSite;
import com.emscloud.model.EmState;
import com.emscloud.model.EmStats;
import com.emscloud.model.Site;
import com.emscloud.model.SiteAnomalyList;
import com.emscloud.util.UTCConverter;
import com.emscloud.vo.AggregatedSiteReport;
import com.emscloud.vo.SiteList;

@Service("siteManager")
@Transactional(propagation = Propagation.REQUIRED)
public class SiteManager {
	
	@Resource
	private SiteDao	siteDao;
	@Resource
	private EmInstanceManager emInstanceManager;
	@Resource
  private EmStatsManager emStatsManager;
	@Resource
	private EmStateManager emStateManager;
	@Resource
	private CustomerDao custDao;
	
	public List<Site> loadAllSites() {
		
		return siteDao.loadAllSites() ;
		
	} //end of method loadAllSites
	
	public List<Site> getSitesListOfCustomer(String custName) {
				
		long custId = custDao.loadCustomerByName(custName).getId();
		return loadSitesByCustomer(custId);
		
	} //end of method getSitesListOfCustomer

	public Site loadSiteById(long id) {		
		return siteDao.loadSiteById(id) ;
		
	} //end of method loadSiteById
		
	public List<Site> loadSitesByCustomer(long id) {
		
		return siteDao.loadSitesByCustomer(id) ;
		
	} //end of method loadSitesByCustomer
	
	public List<com.emscloud.vo.Site> loadSiteFacilitiesByCustomer(Long custId) {
		
		List<Object[]> objList = siteDao.loadSiteFacilitiesByCustomer(custId);
		Iterator<Object[]> iter = objList.iterator();
		ArrayList<com.emscloud.vo.Site> siteList = new ArrayList<com.emscloud.vo.Site>();
		while(iter.hasNext()) {
			Object[] objArr = iter.next();
			com.emscloud.vo.Site site = new com.emscloud.vo.Site();
			site.setId(((BigInteger)objArr[0]).longValue());
			site.setName(objArr[1].toString());
			site.setGeoLocation(objArr[2].toString());
			site.setRegion(objArr[3].toString());
			BigDecimal sqFt = (BigDecimal)objArr[4];
			if(sqFt != null) {
				site.setSquareFoot(sqFt.doubleValue());
			}
			BigInteger facId = (BigInteger)objArr[5];
			if(facId != null) {
				site.setFacilityId(facId.longValue());
			}
			siteList.add(site);
		}
		return siteList;
		
	} //end of method loadSiteFacilitiesByCustomer
	
	public Site getSiteByGeoLocation(String geoLoc) {
		
		return siteDao.getSiteByGeoLocation(geoLoc);
		
	}
		
	public void saveOrUpdate(Site site) {		
		
		siteDao.saveOrUpdate(site) ;	
	
	} //end of method saveOrUpdate
	
	public List<Long> getSiteEms(long siteId) {
		
		return siteDao.getSiteEms(siteId);
		
	} //end of method getSiteEms

    public AggregatedSiteReport loadSitesByCustomerWithSpecificAttibute(Long custId, String orderBy, String orderWay,
            Boolean bSearch, String searchField, String searchString, String searchOper, int offset, int limit) {
        return siteDao.loadSitesByCustomerWithSpecificAttibute(custId, orderBy, orderWay, bSearch, searchField, searchString, searchOper, offset, limit);
    }
    public SiteList getSitesByCustomerWithSpecificAttibute(Long custId, String userdata) throws UnsupportedEncodingException {
        
        SiteList siteList = null;
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
                    }else if(keyval[0].equals("_search"))
                    {
                        bSearch= Boolean.parseBoolean(keyval[1]);
                    }else if(keyval[0].equals("searchField"))
                    {
                        searchField = keyval[1];
                        
                    }else if(keyval[0].equals("searchString"))
                    {
                        searchString = URLDecoder.decode(keyval[1], "UTF-8");
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
            siteList = siteDao.getSitesByCustomerWithSpecificAttibute(custId,orderBy, orderWay, bSearch, searchField, searchString, searchOper, (page - 1) * AggregatedSiteReport.DEFAULT_ROWS, AggregatedSiteReport.DEFAULT_ROWS);
        }
        siteList.setPage(page);
        return siteList;
    }

    public int deleteSite(Long siteId) {
        return siteDao.deleteSite(siteId);     
    }

    public EmInstanceList loadEmInstBySiteId(Long siteId, String userdata) throws ParseException {
      EmInstanceList emInstanceList = new EmInstanceList();
      List<Long> siteemIdList = getSiteEms(siteId);
      ArrayList<EmInstance> emList = new ArrayList<EmInstance>();
      if(siteemIdList!=null && siteemIdList.size()>0)
      {
          Iterator<Long> itr = siteemIdList.iterator();
          while(itr.hasNext())
          {
              Long emId = itr.next();
              EmInstance emInstance = emInstanceManager.getEmInstance(emId);
              if(emInstance != null){
            	  emInstance.setUtcLastConnectivityAt(UTCConverter.getUTCTimestamp(emInstance.getLastConnectivityAt(), emInstance.getTimeZone()));
                  EmStats latestEmStats = emStatsManager.getLatestEmStatsByEmInstanceId(emInstance.getId());
                  if(latestEmStats != null){
                      emInstance.setHealthOfEmInstance(emInstanceManager.getHelathOfEmInstance(latestEmStats));
                  }
                  EmState emState = emStateManager.loadLastEmStatsByEmInstanceId(emInstance.getId());
                  if (emState != null){
                      if (emState.getEmStatus() == EmStatus.CALL_HOME){
                          emInstance.setSyncConnectivity("NA");
                      }else{
                              if (emState.getDatabaseState() != DatabaseState.SYNC_READY){
                                  emInstance.setSyncConnectivity(emState.getDatabaseState().getName());
                              }
                              else{
                                  if(emState.getLog() == null){
                                      emInstance.setSyncConnectivity(DatabaseState.SYNC_READY.getName());
                                  }else{
                                      emInstance.setSyncConnectivity(DatabaseState.SYNC_READY.getName()+" "+emState.getLog());
                                  }
                              }
                      }
                      
                  }else{
                      emInstance.setSyncConnectivity("NA");
                  }
                  emList.add(emInstance);            	  
              }              
          }
      }
      emInstanceList.setEmInsts(emList);
      int page=1;
      emInstanceList.setPage(page);
      emInstanceList.setTotal(1);
      return emInstanceList;
    }

    public List<Object[]> getUnmappedEmInstList(Long customerId) {
        return siteDao.getUnmappedEmInstList(customerId);
    }

    public int assignEmToSite(EmSite emSite) {
        return siteDao.assignEmToSite(emSite);
    }
    public EmSite loadEMSiteByEmIdAndSiteId(long emId,long siteId)
    {
        return siteDao.loadEMSiteByEmIdAndSiteId(emId,siteId);
    }
    public int unAssignEmToSite(EmSite emSite) {
        return siteDao.unAssignEmToSite(emSite);
    }
    public Site getSiteByName(String siteName) {
        return siteDao.getSiteByName(siteName);
    }

	public SiteAnomalyList getAllAnamoliesListBySiteId(List<String> geoLocList, String userdata) throws UnsupportedEncodingException {
	        
			SiteAnomalyList siteAnomaliesList = null;
	        String orderBy = null;
	        String orderWay = null;
	        String query = null;
	        String searchField = null;
	        String searchString = null;
	        String searchOper = "cn";
	        Boolean bSearch = false;
	        Date startDate =null;
	        Date endDate = null;
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
	                    }else if(keyval[0].equals("_search"))
	                    {
	                        bSearch= Boolean.parseBoolean(keyval[1]);
	                    }else if(keyval[0].equals("searchField"))
	                    {
	                        searchField = keyval[1];
	                        
	                    }else if(keyval[0].equals("searchString"))
	                    {
	                        searchString = URLDecoder.decode(keyval[1], "UTF-8");
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
	            	SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd hh:mm:ss a");
	    			if (params[4] != null && !"".equals(params[4])) {
	    				try {
	    					String startDateStr = URLDecoder.decode(params[4], "UTF-8");
							startDate = (Date) sdf.parse(startDateStr);
						} catch (ParseException e) {
							startDate =null;
						}
	    			} else {
	    				startDate =null;
	    			}
	                if (params[5] != null && !"".equals(params[5])) {
	                	try {
	                		String endDateStr = URLDecoder.decode(params[5], "UTF-8");
							endDate = (Date) sdf.parse(endDateStr);
						} catch (ParseException e) {
							 endDate = null;
						}
	                } else {
	                    endDate = null;
	                }

	            }
				siteAnomaliesList = siteDao.getAllAnamoliesListBySiteId(geoLocList,startDate,endDate,orderBy, orderWay, bSearch, searchField, searchString, searchOper, (page - 1) * SiteAnomalyList.DEFAULT_ROWS, SiteAnomalyList.DEFAULT_ROWS);
	        }
	        siteAnomaliesList.setPage(page);
	        return siteAnomaliesList;
	    }
} //end of class SiteManager
