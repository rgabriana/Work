package com.ems.service;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.SceneLightLevelTemplateDao;
import com.ems.model.SceneLightLevelTemplate;

@Service("sceneLightLevelsManager")
@Transactional(propagation = Propagation.REQUIRED)
public class SceneLightLevelsManager {
static final Logger logger = Logger.getLogger("SceneLevelLogger");
    
    @Resource
    private SceneLightLevelTemplateDao sceneLightLevelTemplateDao;

    public List<SceneLightLevelTemplate> loadAllSceneLightLevels() {
        return sceneLightLevelTemplateDao.loadAllSceneLightLevels();
    }
    public SceneLightLevelTemplate loadSceneLightLevelById(Long sceneLightLevelId)
    {
    	return sceneLightLevelTemplateDao.loadSceneLightLevelById(sceneLightLevelId);
    }
	public SceneLightLevelTemplate saveScene(SceneLightLevelTemplate sceneLightLevelTemplate) {
		return  (SceneLightLevelTemplate) sceneLightLevelTemplateDao.saveObject(sceneLightLevelTemplate);
	}

	public List<SceneLightLevelTemplate> loadSceneLightLevelBySceneTemplateId(
			Long sceneTemplateId) {
		return sceneLightLevelTemplateDao.loadSceneLightLevelBySceneTemplateId(sceneTemplateId);
	}

	public List<SceneLightLevelTemplate> loadSceneLightLevelBySceneTemplateIdAndSceneOrder(
			Long sceneTemplateId, Integer sceneOrder) {		
		return sceneLightLevelTemplateDao.loadSceneLightLevelBySceneTemplateIdAndSceneOrder(sceneTemplateId, sceneOrder);
	}
	
	public List<SceneLightLevelTemplate> loadSceneLightLevelBySceneTemplateIdAndSceneName(
			Long sceneTemplateId, String sceneName) {		
		return sceneLightLevelTemplateDao.loadSceneLightLevelBySceneTemplateIdAndSceneName(sceneTemplateId, sceneName);
	}

	public int getMaxSceneOrderForSceneTemplateID(Long sceneTemplateID) {
		return sceneLightLevelTemplateDao.getMaxSceneOrderForSceneTemplateID(sceneTemplateID);
	}
	public void deleteSceneLightLevel(long id) {
		SceneLightLevelTemplate sceneLightLevelTemplate = sceneLightLevelTemplateDao.loadSceneLightLevelById(id);
		List<SceneLightLevelTemplate> sceneLightLevelList = sceneLightLevelTemplateDao.loadSceneLightLevelBySceneTemplateId(sceneLightLevelTemplate.getSceneTemplateId());
		Iterator<SceneLightLevelTemplate> sceneLightLevelItr = sceneLightLevelList.iterator();
		while(sceneLightLevelItr.hasNext())
		{
			SceneLightLevelTemplate sceneLightLevelTemplateObj = sceneLightLevelItr.next();
			if(sceneLightLevelTemplateObj.getId().longValue()==id)
			{
				sceneLightLevelTemplateDao.deleteSceneLightLevel(id);
			}
			if(sceneLightLevelTemplateObj.getSceneOrder()>sceneLightLevelTemplate.getSceneOrder())
			{
				sceneLightLevelTemplateObj.setSceneOrder(sceneLightLevelTemplateObj.getSceneOrder()-1);
				sceneLightLevelTemplateDao.saveObject(sceneLightLevelTemplateObj);
			}
		}
	}  
}