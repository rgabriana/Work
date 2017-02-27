package com.emscloud.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.communication.CommunicationUtils;
import com.emscloud.communication.ResponseWrapper;
import com.emscloud.communication.adaptor.UemAdapter;
import com.emscloud.communication.enlightedUrls.EmFacilitiesUrls;
import com.emscloud.communication.enlightedUrls.EmFixtureUrls;
import com.emscloud.dao.ProfileGroupDao;
import com.emscloud.dao.ProfileSyncStatusDao;
import com.emscloud.dao.ProfileTemplateDao;
import com.emscloud.model.EmInstance;
import com.emscloud.model.Facility;
import com.emscloud.model.ProfileSyncStatus;
import com.emscloud.model.ProfileTemplate;
import com.emscloud.types.FacilityType;
import com.emscloud.vo.EmTemplateList;
import com.sun.jersey.api.client.GenericType;

/**
 * 
 * @author Sharad K Mahajan
 * 
 */
@Service("profileTemplateManager")
@Transactional(propagation = Propagation.REQUIRED)
public class ProfileTemplateManager {


    @Resource
    private ProfileGroupDao profileGroupDao;

    @Resource
    private ProfileTemplateDao profileTemplateDao;

    @Resource
    FacilityManager facilityManager;
    @Resource
    UemAdapter uemAdapter;
    @Resource
    CommunicationUtils communicationUtils;
    
    @Resource
    ProfileSyncStatusDao profileSyncStatusDao;
    @Resource
    FacilityEmMappingManager facilityEmMappingManager;
    @Resource(name="emInstanceManager")
    EmInstanceManager emInstanceManager;
    @Resource
	private GlemManager glemManager;
    static final Logger logger = Logger.getLogger(ProfileTemplateManager.class.getName());
    /**
     * save ProfileTemplate details.
     * 
     * @param ProfileTemplate
     *            com.ems.model.ProfileTemplate
     */
    public ProfileTemplate save(ProfileTemplate profileTemplate) {
        return (ProfileTemplate) profileTemplateDao.saveObject(profileTemplate);
    }

    /**
     * update ProfileTemplate details.
     * 
     * @param ProfileTemplate
     *            com.ems.model.ProfileTemplate
     */
    public ProfileTemplate update(ProfileTemplate profileTemplate) {
        return (ProfileTemplate) profileTemplateDao.saveObject(profileTemplate);
    }

    /**
     * Load company's ProfileTemplate
     * 
     * @param id
     *            company id
     * @return com.ems.model.ProfileTemplate collection
     */
    public List<ProfileTemplate> loadTemplateByCompanyId(Long id) {
        return profileTemplateDao.loadTemplateByCompanyId(id);
    }

    /**
     * Load all ProfileTemplate
     * 
     * @return com.ems.model.ProfileTemplate collection
     */
    public List<ProfileTemplate> loadAllProfileTemplate() {
        return profileTemplateDao.loadAllProfileTemplate();
    }
    
    /**
     * Delete Groups details
     * 
     * @param id
     *            database id(primary key)
     * @return 
     */
    public int delete(Long id) {
    	int status=1;
		try
		{
			profileTemplateDao.removeObject(ProfileTemplate.class, id);
		} catch (Exception e) {
			status=0;
			e.printStackTrace();
		}
		return status;
    	
    }
    
    public int getProfileTemplateCountByName(String name)
    {
    	int count=0;
    	try{
    		count = profileTemplateDao.getProfileTemplateCountByName(name);
    		return count;
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	return 0;
    }

    public ProfileTemplate getProfileTemplateById(Long id) {
        return profileTemplateDao.getProfileTemplateById(id);
    }

    public Long getGroupByProfileAndTenantDetails(byte profileno, long tenantid) {
    	return profileGroupDao.getGroupByProfileAndTenantDetails(profileno, tenantid);
    }
    
    public ProfileTemplate getProfileTemplateByName(String profileTemplateName) {
        return profileTemplateDao.getProfileTemplateByName(profileTemplateName);
    }

    public ProfileTemplate editName(ProfileTemplate profileTemplate) {
        return profileTemplateDao.editName(profileTemplate);
    }
    public List<EmTemplateList>getAllDerivedProfileTemplate(Long emId)
    {
        List<EmTemplateList> emDerivedTemplatesList = new ArrayList<EmTemplateList>();
        // Get List of all Registered Energy Manager
        List<EmInstance> emList = null;
        //If emId >0 -> Download only for current EM Instance
        if(emId.longValue() > 0)
        {
            emList = new ArrayList<EmInstance>();
            EmInstance emInstanceObj = emInstanceManager.getEmInstance(emId.longValue());
            emList.add(emInstanceObj);
        }else
        {
            // Download for all mapped energy manager
            List<Long> emInstanceIds= facilityEmMappingManager.getDistictMappedEMIdList();
            if(emInstanceIds!=null && emInstanceIds.size()>0)
            {
                emList = new ArrayList<EmInstance>();
                Iterator<Long> rwitr = emInstanceIds.iterator();
                while (rwitr.hasNext()) {
                    Long nextRw = rwitr.next();
                    EmInstance emInstanceObj = emInstanceManager.getEmInstance(nextRw);
                    emList.add(emInstanceObj);
                }
            }else
            {
                Long companyId = (long) 1;
                Facility company = facilityManager.getFacility(companyId);
                emList = communicationUtils.getEmMap(company);
            }
        }
        
        try {
          
            if (emList!=null && emList.size()>0) {
              
                List<ResponseWrapper<List<ProfileTemplate>>> response = uemAdapter
                            .executeGet(emList, glemManager.getAdapter().getContextUrl() + EmFixtureUrls.getEMTemplates,
                                    "application/xml",new GenericType<List<ProfileTemplate>>(){});
                    // Iterate Over the all the Response Wrapper result
                    Iterator<ResponseWrapper<List<ProfileTemplate>>> rwitr = response.iterator();
                    while (rwitr.hasNext()) {
                        ResponseWrapper<List<ProfileTemplate>> nextRw = rwitr.next();
                        if (nextRw.getStatus() == Response.Status.OK.getStatusCode()) {
                            List<ProfileTemplate> emTemplate = (List<ProfileTemplate>) nextRw.getItems();
                            EmTemplateList emTemplateList = new EmTemplateList();
                            EmInstance em = nextRw.getEm();
                            emTemplateList.setEmTemplate(emTemplate);
                            emTemplateList.setEm(em);
                            emDerivedTemplatesList.add(emTemplateList);
                            
                            ProfileSyncStatus profileSyncStatus = profileSyncStatusDao.getProfileSyncStatusByEMId(em.getId());
                            if(profileSyncStatus==null)
                            {
                                profileSyncStatus= new ProfileSyncStatus();
                                profileSyncStatus.setEmId(em.getId());
                                profileSyncStatusDao.saveOrUpdate(profileSyncStatus);
                            }
                        } else {
                            logger.error("Not able to get Templates from EM:- "
                                    + nextRw.getEm().getIpAddress()
                                    + " reason :- " + nextRw.getStatus());
                        }
                    }
            } else {
            	logger.error("None of the EM is mapped to UEM Floor. Please Map EM Instance to UEM Floors");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return emDerivedTemplatesList;
    }

    public Long getNextProfileTemplateNo() {
        return profileTemplateDao.getNextProfileTemplateNo();
    }
    
    public String getFixtureCountByProfileTemplateId(Long ptId){
    	
		long result = 0L;
		ProfileTemplate profileTemplate = getProfileTemplateById(ptId);
		HashMap<EmInstance, Long> emProfileTemplateMap = communicationUtils.getEmProfileTemplateMap(profileTemplate);
		
		for(Entry<EmInstance, Long> e : emProfileTemplateMap.entrySet()) {
	        EmInstance emInstance = e.getKey();
	        Long emTemplateId = e.getValue();
	        
	        ResponseWrapper<com.emscloud.communication.vos.Response> response = uemAdapter
			.executeGet(emInstance, glemManager.getAdapter().getContextUrl() + EmFixtureUrls.getFixtureCountByProfileTemplateId+emTemplateId, MediaType.APPLICATION_XML,com.emscloud.communication.vos.Response.class);
	        
	        if (response.getStatus()== Response.Status.OK
					.getStatusCode()) {
				result += Long.parseLong(response.getItems().getMsg());
			} else {
				logger.error("Not able to get Fixture Count Details from EM:- "
						+ response.getEm().getIpAddress()
						+ " reason :- " + response.getStatus());
			}
	                    
	    }
		return String.valueOf(result);
    }

    public com.emscloud.communication.vos.Response editEmTemplate(List<ProfileTemplate> profileTemplates) {
    	com.emscloud.communication.vos.Response result = null;
            List<EmInstance> emList=null;
            
            List<ResponseWrapper<com.emscloud.communication.vos.Response>> response = uemAdapter
                    .executePost(emList, glemManager.getAdapter().getContextUrl() + EmFixtureUrls.updateEMProfiles,MediaType.APPLICATION_XML,MediaType.APPLICATION_XML,com.emscloud.communication.vos.Response.class,communicationUtils.convertModelListToString(profileTemplates,ProfileTemplate.class));
            if (response.get(0).getStatus() == Response.Status.OK.getStatusCode()) 
            {
                result = response.get(0).getItems();
            } else
            {
                logger.error("Edit Profile Template Name command failed from UEM:- " + response.get(0).getEm().getIpAddress()+ " reason :- " + response.get(0).getStatus());
            }
            return result;
    }
}
