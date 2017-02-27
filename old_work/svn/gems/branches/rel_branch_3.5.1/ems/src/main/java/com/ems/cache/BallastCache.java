package com.ems.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.ems.action.SpringContext;
import com.ems.model.Ballast;
import com.ems.model.BallastVoltPower;
import com.ems.service.FixtureManager;
import com.ems.vo.model.VoltPowerCurveValue;

public class BallastCache {
  
  private static BallastCache instance = null;
    
  //map to contain ballast to volt power map
  private ConcurrentHashMap<Long, Long> ballastMapIdCache = new ConcurrentHashMap<Long, Long>();
  //map to contain volt power map id to curve map
  private ConcurrentHashMap<Long, HashMap<Double, Double>> ballastVoltPowerCache = 
      new ConcurrentHashMap<Long, HashMap<Double, Double>>();
  //map to contain default curve values
  private HashMap<Double, Double> defaultVoltPowerCache = new HashMap<Double, Double>();
 
  private FixtureManager fixtureManager;  
  private long lastVoltPwrMapId = 1;
  
  /*
   * 
   */
  private BallastCache() {
      
    // TODO Auto-generated constructor stub  
    fixtureManager = (FixtureManager) SpringContext.getBean("fixtureManager");
    lastVoltPwrMapId = fixtureManager.getMaxVoltPowerMapId();
    //initialize the default map
    List<BallastVoltPower> voltPowerList = fixtureManager.getAllBallastVoltPowersFromId(1);
    Iterator<BallastVoltPower> voltPoweritr = voltPowerList.iterator();
    BallastVoltPower obvp = null;    
    while (voltPoweritr.hasNext()) {
      obvp = voltPoweritr.next();
      defaultVoltPowerCache.put(obvp.getVolt(), obvp.getPower());      
    }
    
  } //end of constructor
  
  public static BallastCache getInstance() {
    
    if(instance == null) {
      synchronized(BallastCache.class) {
	if(instance == null) {
	  instance = new BallastCache();
	}
      }
    }
    return instance;
    
  } //end of method getInstance
  
  private void initializeBallastVoltPowerMap(Ballast ballast) {
    
    if(ballastMapIdCache.containsKey(ballast.getId())) {
      if(ballastVoltPowerCache.containsKey(ballast.getVoltPowerMapId())) {
	return;
      }
    }
    List<BallastVoltPower> voltPowerList = fixtureManager.getAllBallastVoltPowersFromId(
	  ballast.getVoltPowerMapId().longValue());
    HashMap<Double, Double> voltPowerMap = convertVoltPowerCurveToMap(voltPowerList);
    ballastVoltPowerCache.put(ballast.getVoltPowerMapId(), voltPowerMap);
    ballastMapIdCache.put(ballast.getId(), ballast.getVoltPowerMapId());
    
  } //end of method initializeBallastVoltPowerMap
  
  private HashMap<Double, Double> convertVoltPowerCurveToMap(List<BallastVoltPower> list) {
    
    HashMap<Double, Double> voltPowerMap = new HashMap<Double, Double>();
    if(list == null || list.size() == 0) {
      return voltPowerMap;
    }
    Iterator<BallastVoltPower> voltPoweriter = list.iterator();
    BallastVoltPower obvp = null;    
    while (voltPoweriter.hasNext()) {
      obvp = voltPoweriter.next();
      voltPowerMap.put(obvp.getVolt(), obvp.getPower());      
    }
    return voltPowerMap;
    
  } //end of method convertVoltPowerCurveToMap
  
  public HashMap<Double, Double> getVoltPowerCurveMap(Ballast ballast) {
    
    initializeBallastVoltPowerMap(ballast);   
    long mapId = ballastMapIdCache.get(ballast.getId());
    //System.out.println("map id -- " + mapId);
    return ballastVoltPowerCache.get(mapId);
	
  } //end of method getVoltPowerCurveMap
  
  public void addVoltPowerCurveMap(Ballast ballast, Collection<VoltPowerCurveValue> curveValues) {
    
    long voltPwrMapId = 1;
    //initializeBallastVoltPowerMap(ballast);
    long ballastId = ballast.getId();
    if(ballastMapIdCache.containsKey(ballastId)) {
      voltPwrMapId = ballastMapIdCache.get(ballastId);
      if(voltPwrMapId == 1) { //default map
	voltPwrMapId = ++lastVoltPwrMapId;
	ballastMapIdCache.put(ballastId, voltPwrMapId);
      }
    } else {
      voltPwrMapId = ++lastVoltPwrMapId;
      ballastMapIdCache.put(ballastId, voltPwrMapId);
    }   
    HashMap<Double, Double> curveMap = ballastVoltPowerCache.get(voltPwrMapId);
    if(curveMap == null) {
      curveMap = new HashMap<Double, Double>();
      ballastVoltPowerCache.put(voltPwrMapId, curveMap);
    }
    Iterator<VoltPowerCurveValue> iter = curveValues.iterator();
    VoltPowerCurveValue curveVal = null;
    while(iter.hasNext()) {
      curveVal = iter.next();
      curveMap.put(curveVal.getVolts(), curveVal.getCurveValue());
    }
    fixtureManager.addVoltPowerMap(voltPwrMapId, curveMap);
    fixtureManager.updateBallastVoltPowerMapId(ballastId, voltPwrMapId);
    
  } //end of method addVoltPowerCurveMap
  
  private double getInterpolatedValue(HashMap<Double, Double> map, double volt) {
      
    volt = volt/10;
    Double value = map.get(volt);
    if(value != null) { //corresponding curve value already exists in the map
      return value;
    }
    double rem = volt % .5;   
    double upperVolt = volt + (.5 - rem);    
    double lowerVolt = upperVolt - 0.5;
    
    double upperValue = 100.0;
    while(upperVolt <= 10) {
      if(map.containsKey(upperVolt)) {
	upperValue = map.get(upperVolt);
	break;
      }
      upperVolt += 0.5;
    }
    
    double lowerValue = 0.0;
    while(lowerVolt >= 0) {
      if(map.containsKey(lowerVolt)) {
	lowerValue = map.get(lowerVolt);
	break;
      }
      lowerVolt -= 0.5;
    }
    
    //System.out.println("lower volt = " + lowerVolt + " upper volt - " + upperVolt);
    //System.out.println("lower value = " + lowerValue + " upper value = " + upperValue);
    double curveVal = lowerValue + (((volt - lowerVolt) * upperValue) - 
	((volt - lowerVolt) * lowerValue)) / (upperVolt - lowerVolt);    
    //System.out.println(" interpolated curve val -- " + curveVal);
    return curveVal;
    
  } //end of method getInterpolatedValue
  
  public double getBallastVoltPowerFactor(Ballast ballast, int volt) {

    if (ballast == null) {
      return 100; //returning 1 not to have any effect in the calculation of the load.
    }
    initializeBallastVoltPowerMap(ballast);
    long mapId = ballastMapIdCache.get(ballast.getId());
    HashMap<Double, Double> map = ballastVoltPowerCache.get(mapId);
    
    if(map == null) {
      map = defaultVoltPowerCache;
    }
    return getInterpolatedValue(map, volt);
    
    /*
    if(map == null) {
      return defaultVoltPowerCache.get(volt);
    }
    if(map.containsKey(volt)) {
      return map.get(volt);
    }
    return defaultVoltPowerCache.get(volt);
    */
    
  } //end of method getBallastVoltPowerFactor
  
} //end of class BallastCache
