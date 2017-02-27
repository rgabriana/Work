package com.ems.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.BulbDao;
import com.ems.model.Bulb;
import com.ems.model.BulbList;
import com.ems.vo.model.BulbVO;


@Service("bulbManager")
@Transactional(propagation = Propagation.REQUIRED)
public class BulbManager {

	@Resource
	BulbDao bulbDao;
	
	public BulbList loadBulbList(String orderway, int offset, int limit) {
		return bulbDao.loadBulbList(orderway, offset, limit);
	}
	
	public Bulb addBulb(Bulb bulb) {
		return (Bulb)bulbDao.addBulb(bulb);		
    }
	
	public Bulb getBulbById(Long id)
	{
		return bulbDao.getBulbById(id);
	}

	public void editBulb(Bulb bulb) {
		// TODO Auto-generated method stub
		bulbDao.editBulb(bulb);
	}

	public void deleteBulbById(Long id) {
		// TODO Auto-generated method stub
		bulbDao.deleteBulbById(id);		
	}

	public Bulb getBulbByName(String bulbName) {
		// TODO Auto-generated method stub
		return bulbDao.getBulbByName(bulbName);
	}
	
	public List<Bulb> getAllBulbs() {
		return bulbDao.getAllBulbs();
	}
	public List<Object[]> getBulbsCountByBulbName()
    {
       return  bulbDao.getBulbsCountByBulbName();
    }
	
    /**
     * This method to be called when we want to send 
     * all the parameters of Bulb as part of XML in 
     * webservice. 
     * @return
     */
    public List<BulbVO> getAllBulbVO(){
        List<Bulb> bulbList = getAllBulbs();
        List<BulbVO> bulbVO = new ArrayList<BulbVO>(bulbList.size());
        
        for(Bulb b : bulbList){
            BulbVO vo = new BulbVO(b);
            bulbVO.add(vo);
        }
        return bulbVO;
    }
}
