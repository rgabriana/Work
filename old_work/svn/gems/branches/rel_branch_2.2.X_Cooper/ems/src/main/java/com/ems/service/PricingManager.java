package com.ems.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.action.SpringContext;
import com.ems.dao.PricingDao;
import com.ems.model.Company;
import com.ems.model.Pricing;
import com.ems.utils.ArgumentUtils;
import com.ems.utils.DateUtil;

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

    private Company company = null;
    private Calendar startCalendar;
    private Calendar endCalendar;

    /**
     * Gets the pricing object by id
     * 
     * @param id
     *            , the unique identifier
     * @return the Pricing object
     */
    public Pricing getPricingById(Long id) {
        return pricingDao.getPricingById(id);
    }

    /**
     * Gets the list of pricing objects in database
     * 
     * @return the pricing list
     */
    public List<Pricing> getPricingList() {
        return pricingDao.getPricingList();
    }

    /**
     * Saves or updates pricing object
     * 
     * @param the
     *            pricing object to be saved/updated
     */
    public void saveOrUpdatePricing(Pricing pricing) {
        pricingDao.saveOrUpdatePricing(pricing);
    }

    /**
     * Removes a pricing object from database using it's id
     * 
     * @param id
     *            , the unique identifier
     */
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
            price = pricingDao.getPrice(cal.getTime(), "weekend");
        } else {
            // weekday
            price = pricingDao.getPrice(cal.getTime(), "weekday");
        }
        if (price == 0) {
            if (company == null) {
                CompanyManager compMgr = (CompanyManager) SpringContext.getBean("companyManager");
                company = compMgr.loadCompany();
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
}