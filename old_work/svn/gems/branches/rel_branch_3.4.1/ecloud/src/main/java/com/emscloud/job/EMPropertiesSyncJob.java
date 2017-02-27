package com.emscloud.job;

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.communication.utils.ArgumentUtils;
import com.emscloud.action.SpringContext;
import com.emscloud.communication.ResponseWrapper;
import com.emscloud.communication.enlightedUrls.EmMiscUrls;
import com.emscloud.model.EmInstance;
import com.emscloud.service.ECManager;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.GlemManager;
import com.emscloud.types.GlemModeType;

public class EMPropertiesSyncJob implements Job{
	
	public static final Logger logger = Logger.getLogger("EMPropertiesSyncJob");
	ECManager eCManager;
	EmInstanceManager emInstanceManager;
	GlemManager glemManager;
	
	public EMPropertiesSyncJob() {
		eCManager = (ECManager) SpringContext.getBean("eCManager");
		emInstanceManager = (EmInstanceManager) SpringContext.getBean("emInstanceManager");
		glemManager = (GlemManager) SpringContext.getBean("glemManager");
	}
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		if(glemManager.getGLEMMode() != GlemModeType.ECLOUD.getMode()) {
			List<EmInstance> emInstances = emInstanceManager.getActiveEmInstance();
			if(emInstances != null) {
				for(EmInstance em: emInstances) {
					// fire the rest service.
					ResponseWrapper<String> response = glemManager.getAdapter()
							.executePost(em,
									glemManager.getAdapter().getContextUrl() + EmMiscUrls.getTimeZoneUrl,
									MediaType.TEXT_PLAIN,
									MediaType.TEXT_PLAIN,
									String.class,
									null);
					// only if the status is OK go save the data
					if (!ArgumentUtils.isNull(response.getStatus()) &&response.getStatus() == Response.Status.OK.getStatusCode()) {
						String output = response.getItems();
						//System.out.println(output);
						if(output != null && output.contains("###")) {
							String tz = output.split("###")[0];
							if(tz != null && !"".equals(tz) && !tz.equals(em.getTimeZone())) {
								em.setTimeZone(output.split("###")[0]);
								emInstanceManager.saveOrUpdate(em);
							}
						}
					} else {
						logger.info("Em "
							+ em.getMacId()
							+ " with name "
							+ em.getName()
							+ " failed to fetch the em properties data with error code "
							+ response.getStatus());
					}
				}
			}
		}
		
	}

}
