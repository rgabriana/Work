package com.ems.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.action.SpringContext;
import com.ems.dao.PricingDao;
import com.ems.model.Company;
import com.ems.model.Pricing;
import com.ems.model.SystemConfiguration;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.utils.ArgumentUtils;
import com.ems.utils.DateUtil;
import com.ems.vo.DRStatus;

/**
 * PricingManagerImpl, Class implementing the PrincingManager interface
 * 
 * @author Shiv Mohan
 */
@Service("pricingManager")
@Transactional(propagation = Propagation.REQUIRED)
public class PricingManager {

    /** The pricing dao. */
    @Resource
    private PricingDao pricingDao;
    
    @Resource
    SystemConfigurationManager sysConfigManager;
    
    @Resource
    CompanyManager compMgr;
    

    private Company company = null;
    private Calendar startCalendar;
    private Calendar endCalendar;
    
    private Long companyId = -1L;

    /**
     * Gets the list of pricing objects in database
     * 
     * @return the pricing list
     */
    @Cacheable(value = "pricing", key="#root.methodName")
    public List<Pricing> getPricingList() {
        return pricingDao.getPricingList();
    }

    /**
     * Saves or updates pricing object
     * 
     * @param the
     *            pricing object to be saved/updated
     */
    @CacheEvict(value = "pricing", allEntries = true)
    public void saveOrUpdatePricing(Pricing pricing) {
        pricingDao.saveOrUpdatePricing(pricing);
    }

    /**
     * Removes a pricing object from database using it's id
     * 
     * @param id
     *            , the unique identifier
     */
    @CacheEvict(value = "pricing", allEntries = true)
    public void removePricing(Long id) {
        pricingDao.removePricing(id);
    }

    public boolean validateTime(Date fromTime, Date toTime, String dayType) {
        return pricingDao.validateTime(fromTime, toTime, dayType);
    }

    public boolean validateTime(Date fromTime, Date toTime, String dayType, long id) {
        return pricingDao.validateTime(fromTime, toTime, dayType, id);
    }

    public double getPrice(Date currentDate) {
    	
    	double price = 0;
       
		DeviceServiceImpl deviceServiceImpl = DeviceServiceImpl.getInstance();
		if(deviceServiceImpl.getDRTimeRemaining() != 0)
		{
	    	DRStatus drStatus = deviceServiceImpl.getDrStatus();
	    	if(drStatus.getStatus().equals(true))
			{
	    		Double drPrice = drStatus.getPrice();
	    		if(drPrice != null && drPrice > 0)
	    		{
	    			price = drPrice;
	    			return price;
	    		}
			}
		}
		
    	//ENL - 4179 , should return the fixed price from company table when fixed pricing is selected. 
    	SystemConfiguration pricingTypeConfig = sysConfigManager.loadConfigByName("enable.pricing");
    	if(pricingTypeConfig != null)
    	{    		
    		//1 for Fixed Pricing , 2 for Time Of Day Pricing
    		if ("1".equalsIgnoreCase(pricingTypeConfig
					.getValue())) {
    			if(companyId.compareTo(-1L) == 0) {
        	        company = compMgr.loadCompany();
        	        companyId = company.getId();
    			}
    			else {
    				company = compMgr.loadCompanyById(companyId);
    			}
    	        price = company.getPrice();
    	        return price;									
			}       
    	}
    	// 
    	//End ENL - 4179.

        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

        // in the database pricing, from time and to time are stored as default year/month/day
        cal.set(Calendar.YEAR, Pricing.DEFAULT_YEAR);
        cal.set(Calendar.MONTH, Pricing.DEFAULT_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, Pricing.DEFAULT_DAY);

        // TODO holiday is not implemented
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            // weekend
            price = getPriceFromCache(cal.getTime(), "weekend");
        } else {
            // weekday
            price = getPriceFromCache(cal.getTime(), "weekday");
        }
        if (price == 0) {
            if (company == null) {
            	CompanyManager compMgr = (CompanyManager) SpringContext.getBean("companyManager");
    			if(companyId.compareTo(-1L) == 0) {
        	        company = compMgr.loadCompany();
        	        companyId = company.getId();
    			}
    			else {
    				company = compMgr.loadCompanyById(companyId);
    			}
            }
            price = company.getPrice();
        }
        return price;

    } // end of method getPrice

    private String validateInterval(String interval, String dayType) {
        if (!ArgumentUtils.isNullOrEmpty(dayType) && !ArgumentUtils.isNullOrEmpty(interval)) {
            extractDatesFromInterval(interval);
            if (startCalendar.equals(endCalendar)) {
                return "noInterval";
            } else {
                if (startCalendar.after(endCalendar)) {
                    endCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    if (!validateTime(startCalendar.getTime(), endCalendar.getTime(), dayType)) {
                        return "overlap";
                    }
                } else {
                    if (!validateTime(startCalendar.getTime(), endCalendar.getTime(), dayType)) {
                        return "overlap";
                    } else {
                        startCalendar.add(Calendar.DAY_OF_MONTH, 1);
                        endCalendar.add(Calendar.DAY_OF_MONTH, 1);
                        if (!validateTime(startCalendar.getTime(), endCalendar.getTime(), dayType)) {
                            return "overlap";
                        }
                    }
                }
            }
        }
        return "S";
    }

    private String validateInterval(String interval, String dayType, long id) {
        if (!ArgumentUtils.isNullOrEmpty(dayType) && !ArgumentUtils.isNullOrEmpty(interval)) {
            extractDatesFromInterval(interval);
            if (startCalendar.equals(endCalendar)) {
                return "noInterval";
            } else {
                if (startCalendar.after(endCalendar)) {
                    endCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    if (!validateTime(startCalendar.getTime(), endCalendar.getTime(), dayType, id)) {
                        return "overlap";
                    }
                } else {
                    if (!validateTime(startCalendar.getTime(), endCalendar.getTime(), dayType, id)) {
                        return "overlap";
                    } else {
                        startCalendar.add(Calendar.DAY_OF_MONTH, 1);
                        endCalendar.add(Calendar.DAY_OF_MONTH, 1);
                        if (!validateTime(startCalendar.getTime(), endCalendar.getTime(), dayType, id)) {
                            return "overlap";
                        }
                    }
                }
            }
        }
        return "S";
    }

    private void extractDatesFromInterval(String interval) {
        String[] times = interval.split("-");
        startCalendar = Calendar.getInstance();
        startCalendar.set(Pricing.DEFAULT_YEAR, Pricing.DEFAULT_MONTH, Pricing.DEFAULT_DAY, 0, 0, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);
        DateUtil.setTimeInCalendar(times[0].trim(), startCalendar);

        endCalendar = Calendar.getInstance();
        endCalendar.set(Pricing.DEFAULT_YEAR, Pricing.DEFAULT_MONTH, Pricing.DEFAULT_DAY, 0, 0, 0);
        endCalendar.set(Calendar.MILLISECOND, 0);
        DateUtil.setTimeInCalendar(times[1].trim(), endCalendar);
    }

    /**
     * Updates a specific pricing
     */
    @CacheEvict(value = "pricing", allEntries = true)
    public String updatePricing(Pricing pricing) {
        try {
            if (pricing == null || pricing.getDayType() == null || pricing.getId() == null
                    || pricing.getInterval() == null || pricing.getPrice() == null || pricing.getPriceLevel() == null
                    || !("weekday".equals(pricing.getDayType()) || "weekend".equals(pricing.getDayType()))) {
                return "E";
            }
            String validateIntervalMsg = validateInterval(pricing.getInterval().toUpperCase(), pricing.getDayType(),
                    pricing.getId());
            if (!"S".equals(validateIntervalMsg)) {
                return validateIntervalMsg;
            } else {
                pricing.setPrice(Double.valueOf(pricing.getPrice()));
                pricing.setFromTime(startCalendar.getTime());
                pricing.setToTime(endCalendar.getTime());
                saveOrUpdatePricing(pricing);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "S";
    }

    /**
     * Adds new pricing
     */
    @CacheEvict(value = "pricing", allEntries = true)
    public String addPricing(Pricing pricing) {
        try {
            if (pricing == null || pricing.getDayType() == null || pricing.getId() != null
                    || pricing.getInterval() == null || pricing.getPrice() == null || pricing.getPriceLevel() == null
                    || !("weekday".equals(pricing.getDayType()) || "weekend".equals(pricing.getDayType()))) {
                return "E";
            }
            String validateIntervalMsg = validateInterval(pricing.getInterval().toUpperCase(), pricing.getDayType());
            if (!"S".equals(validateIntervalMsg)) {
                return validateIntervalMsg;
            } else {
                pricing.setPrice(Double.valueOf(pricing.getPrice()));
                pricing.setFromTime(startCalendar.getTime());
                pricing.setToTime(endCalendar.getTime());
                saveOrUpdatePricing(pricing);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "S";
    }

    /**
     * Deletes the pricing object
     */
    @CacheEvict(value = "pricing", allEntries = true)
    public String deletePricing(Pricing pricing) {
        try {
            Long id = pricing.getId();
            if (!ArgumentUtils.isNullOrEmpty(id.toString())) {
                removePricing(Long.valueOf(id));
            } else {
                return "E";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "S";
    }
    
    public double getPriceFromCache(Date date, String dayType) {
        PricingManager pricingManager = (PricingManager) SpringContext.getBean("pricingManager");
    	List<Pricing> pricing = pricingManager.getPricingByDayType(dayType);
    	double price = 0D;
        if(pricing != null && pricing.size() > 0) {
        	for(Pricing p: pricing) {
            	if(p.getFromTime().compareTo(date) >= 0 && p.getToTime().compareTo(date) < 0) {
            		price = p.getPrice();
            		break;
            	}
            	else {
            		price = p.getPrice();
            	}
            }
        }
    	return price;
    }
    
    @Cacheable(value = "pricing", key="#dayType")
    public List<Pricing> getPricingByDayType(String dayType) {
    	return pricingDao.getPricingByDayType(dayType);
    }
}
