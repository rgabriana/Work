package com.emscloud.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class IRUtil {

    public static HashMap<String, Long> totalCommissionedSensors = new HashMap<String, Long>();
    public static List<Object[]> totalBallastAssociated = new ArrayList<Object[]>();
    public static List<Object[]> totalBulbsAssociated = new ArrayList<Object[]>();
    public static List<Object[]> totalCommissionedCus = new ArrayList<Object[]>();
    public static List<Object[]> totalFxTypeAssociated = new ArrayList<Object[]>();
    
    public static Long totalBallastAssociatedCount = (long) 0;
    public static Long totalLampsAssociatedCount = (long) 0;
    public static Long totalFxTypeAssociatedCount = (long) 0;
    public static Long totalCommissionedCuCount = (long) 0;
    public static Long totalBallastsCount = (long) 0;
    public static Long totalBulbsCount= (long) 0;
    
    public static void resetData()
    {
        totalBallastAssociatedCount = (long) 0;
        totalLampsAssociatedCount = (long) 0;
        totalFxTypeAssociatedCount = (long) 0;
        totalCommissionedCuCount = (long) 0;
        totalBallastsCount = (long) 0;
        totalBulbsCount= (long) 0;
        totalCommissionedSensors = new HashMap<String, Long>();
        totalBallastAssociated = new ArrayList<Object[]>();
        totalBulbsAssociated = new ArrayList<Object[]>();
        totalCommissionedCus = new ArrayList<Object[]>();
        totalFxTypeAssociated = new ArrayList<Object[]>();
    }
 // Aggregate Fixture Counts
    public static void aggregateSiteFixtureData(HashMap<String, Long> emFixureData)
    {
        if(totalCommissionedSensors.isEmpty()){
            totalCommissionedSensors.putAll(emFixureData);
        }else{
            if(emFixureData.size()>0){
                Set<String> keySet = emFixureData.keySet();
                Iterator<String> keySetIterator = keySet.iterator();
                while (keySetIterator.hasNext()) {
                   String key = keySetIterator.next();
                   Long value = emFixureData.get(key);
                   if(totalCommissionedSensors.containsKey(key)){
                       Long totalCount = totalCommissionedSensors.get(key);
                       totalCount+=value;
                       totalCommissionedSensors.put(key, totalCount);
                   }else{
                       totalCommissionedSensors.put(key, (Long) value);
                   }
                }
            }
        }
    }
    // Aggregate CU Counts
    public static void aggregateCuData(List<Object[]> cuList)
    {
        if(totalCommissionedCus.isEmpty()){
            totalCommissionedCus.addAll(cuList);
        }else{
            for(int i=0;i<cuList.size();i++)
            {
                Object[] totalCommCuItrObject = cuList.get(i);
                String cuVersion = (String) totalCommCuItrObject[0];
                Long totalCucount = ((Long) totalCommCuItrObject[1]).longValue();
                boolean isFound=false;
                for(int j=0;j<totalCommissionedCus.size();j++)
                {
                    Object[] gItrObject = totalCommissionedCus.get(j);
                    String gNewCuVersion = (String) gItrObject[0];
                    Long gCount = ((Long) gItrObject[1]).longValue();
                    if(cuVersion.equalsIgnoreCase(gNewCuVersion))
                    {
                        isFound=true;
                        gCount+=totalCucount;
                        gItrObject[1]=gCount;
                        break;
                    }
                }
                if(!isFound)
                {
                    totalCommissionedCus.add(totalCommCuItrObject);
                }
            }
        }
    }
    
    //Aggregate Ballast Data
    public static void aggregateBallastData(List<Object[]> ballastList)
    {
        if(totalBallastAssociated.isEmpty()){
            totalBallastAssociated.addAll(ballastList);
        }else{
            for(int i=0;i<ballastList.size();i++)
            {
                Object[] totalCommBallastItrObject = ballastList.get(i);
                String ballastName = (String) totalCommBallastItrObject[0];
                Long totalBallastcount = ((Long) totalCommBallastItrObject[2]).longValue();
                Long totalFxcount = ((Long) totalCommBallastItrObject[3]).longValue();
                boolean isFound=false;
                for(int j=0;j<totalBallastAssociated.size();j++)
                {
                    Object[] gItrObject = totalBallastAssociated.get(j);
                    String gNewBallastName = (String) gItrObject[0];
                    Long gNewBallastCount = ((Long) gItrObject[2]).longValue();
                    Long gNewFxCount = ((Long) gItrObject[3]).longValue();
                    if(ballastName.contains(gNewBallastName))
                    {
                        gNewBallastCount+=totalBallastcount;
                        gNewFxCount+=totalFxcount;
                        gItrObject[2]=gNewBallastCount;
                        gItrObject[3]=gNewFxCount;
                        isFound=true;
                        break;
                    }
                }
                if(!isFound)
                {
                    totalBallastAssociated.add(totalCommBallastItrObject);
                }
            }
        }   
    }
    
    //Aggregate Bulbs Data
    public static void aggregateBulbData(List<Object[]> bulbList)
    {
        if(totalBulbsAssociated.isEmpty()){
            totalBulbsAssociated.addAll(bulbList);
        }else{
            for(int i=0;i<bulbList.size();i++)
            {
                Object[] totalCommBulbItrObject = bulbList.get(i);
                String bulbName = (String) totalCommBulbItrObject[0];
                Long totalBulbcount = ((Long) totalCommBulbItrObject[2]).longValue();
                Long totalFxcount = ((Long) totalCommBulbItrObject[3]).longValue();
                boolean isFound =false;
                for(int j=0;j<totalBulbsAssociated.size();j++)
                {
                    Object[] gItrObject = totalBulbsAssociated.get(j);
                    String gNewBulbName = (String) gItrObject[0];
                    Long gNewBulbCount = ((Long) gItrObject[2]).longValue();
                    Long gNewFxCount = ((Long) gItrObject[3]).longValue();
                    if(bulbName.contains(gNewBulbName))
                    {
                        gNewBulbCount+=totalBulbcount;
                        gNewFxCount+=totalFxcount;
                        gItrObject[2]=gNewBulbCount;
                        gItrObject[3]=gNewFxCount;
                        isFound=true;
                        break;
                    }
                }
                if(!isFound)
                {
                    totalBulbsAssociated.add(totalCommBulbItrObject);
                }
            }
        }   
    }
    
    
    //Aggregate FixtureType Data
    public static void aggregateFixtureTypeData(List<Object[]> fxTypeList)
    {
        if(totalFxTypeAssociated.isEmpty()){
            totalFxTypeAssociated.addAll(fxTypeList);
        }else{
            for(int i=0;i<fxTypeList.size();i++)
            {
                Object[] totalCommFxTypeItrObject = fxTypeList.get(i);
                String fxTypeName = (String) totalCommFxTypeItrObject[2];
                Long totalFxcount = ((Long) totalCommFxTypeItrObject[0]).longValue();
                boolean isFound=false;
                for(int j=0;j<totalFxTypeAssociated.size();j++)
                {
                    Object[] gItrObject = totalFxTypeAssociated.get(j);
                    String gNewFxTypeName = (String) gItrObject[2];
                    Long gNewFxCount = ((Long) gItrObject[0]).longValue();
                    if(fxTypeName.contains(gNewFxTypeName))
                    {
                        gNewFxCount+=totalFxcount;
                        gItrObject[0]=gNewFxCount;
                        isFound=true;
                        break;
                    }
                }
                if(!isFound)
                {
                    totalFxTypeAssociated.add(totalCommFxTypeItrObject);
                }
            }
        }   
    }
    
    public static void calculateAggregateSummaryCount()
    {
      //Calculate Total Cus Associated
        if (totalCommissionedCus != null && !totalCommissionedCus.isEmpty()) {
            Iterator<Object[]> iterator = totalCommissionedCus.iterator();
            while (iterator.hasNext()) {
                Object[] itrObject = (Object[]) iterator.next();
                Long count = (Long) itrObject[1];
                totalCommissionedCuCount+= count;
            }
        }
        
        //Calculate Total Ballast Associated
        if (totalBallastAssociated != null && !totalBallastAssociated.isEmpty()) {
            Iterator<Object[]> iterator = totalBallastAssociated.iterator();
            while (iterator.hasNext()) {
                Object[] itrObject = (Object[]) iterator.next();
                Long count = (Long) itrObject[2];
                Long fxcount = (Long) itrObject[3];
                totalBallastsCount+= count;
                totalBallastAssociatedCount+= fxcount;
            }
        }
        
      //Calculate Total bulbs Associated
        if (totalBulbsAssociated != null && !totalBulbsAssociated.isEmpty()) {
            Iterator<Object[]> iterator1 = totalBulbsAssociated.iterator();
            while (iterator1.hasNext()) {
                Object[] itrObject1 = (Object[]) iterator1.next();
                Long count = ((Long) itrObject1[2]).longValue();
                Long fxcount = ((Long) itrObject1[3]).longValue();
                totalBulbsCount+= count;
                totalLampsAssociatedCount+= fxcount;
            }
        }
        
      //Calculate Total FxType Associated
        if (totalFxTypeAssociated != null && !totalFxTypeAssociated.isEmpty()) {
            Iterator<Object[]> iterator1 = totalFxTypeAssociated.iterator();
            while (iterator1.hasNext()) {
                Object[] itrObject1 = (Object[]) iterator1.next();
                Long count = ((Long) itrObject1[0]).longValue();
                totalFxTypeAssociatedCount+= count;
            }
        }
    }
} //end of class IRUtil
