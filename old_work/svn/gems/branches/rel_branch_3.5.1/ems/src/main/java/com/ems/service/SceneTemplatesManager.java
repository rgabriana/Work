package com.ems.service;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.SceneTemplateDao;
import com.ems.model.SceneTemplate;

@Service("sceneTemplatesManager")
@Transactional(propagation = Propagation.REQUIRED)
public class SceneTemplatesManager {
static final Logger logger = Logger.getLogger("SceneTemplateLogger");
     
    @Resource
    private SceneTemplateDao sceneTemplateDao;

    public List<SceneTemplate> loadAllSceneTemlates() {
        return sceneTemplateDao.loadAllSceneTemlates();
    }

	public List<SceneTemplate> getSceneTemplateByName(String name) {		
		return sceneTemplateDao.getSceneTemplateByName(name);
	}  
	
	public SceneTemplate createNewSceneTemplate(String name){
		SceneTemplate newSceneTemplate = new SceneTemplate();
		newSceneTemplate.setName(name);
		return sceneTemplateDao.createNewSceneTemplate(newSceneTemplate);		 		
	}

	public SceneTemplate getSceneTemplateById(Long scenetemplateId) {
		return sceneTemplateDao.getSceneTemplateById(scenetemplateId);		
	}
	public int deleteSceneTemplate(Long scenetemplateId) {
		return sceneTemplateDao.deleteSceneTemplate(scenetemplateId);		
	}

	public SceneTemplate editSceneTemplateName(Long id, String sceneTemplateName) {
		SceneTemplate sceneTemplate = getSceneTemplateById(id);
		if(sceneTemplate==null)
		{
			sceneTemplate = new SceneTemplate();
		}
		sceneTemplate.setName(sceneTemplateName);
		return (SceneTemplate)sceneTemplateDao.saveObject(sceneTemplate);
	}
	
}