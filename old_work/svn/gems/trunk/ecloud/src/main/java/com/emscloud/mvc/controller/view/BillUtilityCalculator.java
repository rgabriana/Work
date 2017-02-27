package com.emscloud.mvc.controller.view;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Locale;

import com.emscloud.model.CustomerSppaBill;
import com.emscloud.model.SppaBill;

public class BillUtilityCalculator {
	
	/**
	 * Formatters used in the Report
	 */ 
	public static NumberFormat usdCostFormat = NumberFormat.getCurrencyInstance(Locale.US);
	public static DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ENGLISH);
	public static NumberFormat numberFormatter = NumberFormat.getNumberInstance(Locale.US);
	public static NumberFormat percentageFomatter = NumberFormat.getPercentInstance(Locale.US);
	
	public static CustomerSppaBill customerSppaBill;
	public static final BigDecimal ZERO_BIG_DECIMAL_VALUE = new BigDecimal(0);
	/**
	 * Formatter Function returning averageSppa
	 * 
	 * @return averageSppa
	 */
	public static String getAverageSppa()
	{
		Double savedCost = (getCustomerSppaBill().getSppaCost() * 1000);
		BigDecimal baselineEnergy = getCustomerSppaBill().getBaselineEnergy();
		BigDecimal getConsumedEnergy = getCustomerSppaBill().getConsumedEnergy();
		MathContext mc = new MathContext(4); // 2 precision
		BigDecimal averageSppA= new BigDecimal(0);
		if(baselineEnergy.compareTo(ZERO_BIG_DECIMAL_VALUE)>0)
		{
			BigDecimal diff = baselineEnergy.subtract(getConsumedEnergy, mc);
			if(diff.compareTo(ZERO_BIG_DECIMAL_VALUE)>0)
			{
			    averageSppA = new BigDecimal(savedCost).divide(diff,4,RoundingMode.HALF_UP);
			}
		}
		usdCostFormat.setMaximumFractionDigits(4);
		return usdCostFormat.format(averageSppA.doubleValue());
	}
	/**
	 * Formatter Function returning averageUtility
	 * 
	 * @return averageUtility
	 */
	public static String getAverageUtility()
	{
		Double diff = (getCustomerSppaBill().getBaseCost() - getCustomerSppaBill().getSavedCost()) * 1000;
		BigDecimal consumedEnergy = getCustomerSppaBill().getConsumedEnergy();
		BigDecimal averageUtility = new BigDecimal(0);
		if(consumedEnergy.compareTo(ZERO_BIG_DECIMAL_VALUE)>0)
		{
			averageUtility = new BigDecimal(diff).divide(consumedEnergy,4,RoundingMode.HALF_UP);
		}
		usdCostFormat.setMaximumFractionDigits(4);
		return usdCostFormat.format(averageUtility.doubleValue());
		
	}
	
	/**
	 * Formatter Function returning totalAmount Due
	 * 
	 * @return totalAmountDue
	 */
	public static String getTotalAmountDue()
	{
		Double totalAmountDue = getCustomerSppaBill().getTotalAmtDue();
		usdCostFormat.setMaximumFractionDigits(2);
		return usdCostFormat.format(totalAmountDue);
		
	}
	/**
	 * Formatter Function returning guideline usage power
	 * 
	 * @return guideLineUsageInPower
	 */
	public static String getGuidelLineUsageInPower()
	{
		BigDecimal guideLineUsageInPower = getCustomerSppaBill().getBaselineEnergy().divide(new BigDecimal(1000),2,RoundingMode.HALF_UP);
		numberFormatter.setMaximumFractionDigits(2);
		return numberFormatter.format(guideLineUsageInPower.doubleValue());
	}
	/**
	 * Formatter Function returning guideline usage Money
	 * 
	 * @return guideLineUsageInMoney
	 */
	public static String getGuidelLineUsageInMoney()
	{
		Double guideLineUsageInMoney = getCustomerSppaBill().getBaseCost();
		usdCostFormat.setMaximumFractionDigits(2);
		return usdCostFormat.format(guideLineUsageInMoney);
	}
	/**
	 * Formatter Function returning Actual Usage in Power
	 * 
	 * @return actualUageInPower
	 */
	public static String getActualUsageInPower()
	{
		BigDecimal actualUageInPower = getCustomerSppaBill().getConsumedEnergy().divide(new BigDecimal(1000),2,RoundingMode.CEILING);
		numberFormatter.setMaximumFractionDigits(2);
		return numberFormatter.format(actualUageInPower.doubleValue());
	}
	/**
	 * Formatter Function returning Actual Usage In Money
	 * 
	 * @return acutalUsageInMoney
	 */
	public static String getActualUsageInMoney()
	{
		Double acutalUsageInMoney = getCustomerSppaBill().getBaseCost()-getCustomerSppaBill().getSavedCost();
		usdCostFormat.setMaximumFractionDigits(2);
		return usdCostFormat.format(acutalUsageInMoney);
	}
	
	/**
	 * Formatter Function returning Savings in Percentage
	 * 
	 * @return savingInPercentage
	 */
	public static String getSavingInPercentage()
	{
		BigDecimal savingInPower = new BigDecimal(0);
		if(getCustomerSppaBill().getBaselineEnergy().compareTo(ZERO_BIG_DECIMAL_VALUE) > 0)
		{
			BigDecimal diff = getCustomerSppaBill().getBaselineEnergy().subtract(getCustomerSppaBill().getConsumedEnergy());
			savingInPower = (diff).divide(getCustomerSppaBill().getBaselineEnergy(),RoundingMode.HALF_UP);
		}
		return percentageFomatter.format(savingInPower.doubleValue());
	}
	/**
	 * Formatter Function returning Savings In Dollar
	 * 
	 * @return savedCost
	 */
	public static String getSavingInMoney()
	{
		usdCostFormat.setMaximumFractionDigits(2);
		return usdCostFormat.format(getCustomerSppaBill().getSavedCost());
	}
	/**
	 * Formatter Function returning Saved By ATT In Dollar
	 * 
	 * @return savedByATT
	 */
	public static String getSavedByATT()
	{
		usdCostFormat.setMaximumFractionDigits(2);
		Double savedByATT = getCustomerSppaBill().getSavedCost()-getCustomerSppaBill().getSppaCost()-getCustomerSppaBill().getTax();
		return usdCostFormat.format(savedByATT);
	}
	/**
	 * Formatter Function returning SPPA Payable In Dollar
	 * 
	 * @return sppaPayable
	 */
	public static String getSppaPayable()
	{
		usdCostFormat.setMaximumFractionDigits(2);
		Double sppaPayable = getCustomerSppaBill().getSppaCost();
		return usdCostFormat.format(sppaPayable);
	}
	/**
	 * Formatter Function returning Tax In Dollar
	 * 
	 * @return tax
	 */
	public static String getTax()
	{
		usdCostFormat.setMaximumFractionDigits(2);
		Double tax = getCustomerSppaBill().getTax();
		return usdCostFormat.format(tax);
	}
	/**
	 * Formatter Function returning SPPA Payable to Enlighted In Dollar
	 * 
	 * @return sppaPayableToEnlighted
	 */
	public static String getSppaPayableToEnlighted()
	{
		usdCostFormat.setMaximumFractionDigits(2);
		Double sppaPayableToEnlighted = getCustomerSppaBill().getSppaCost() + getCustomerSppaBill().getTax();
		return usdCostFormat.format(sppaPayableToEnlighted);
	}
	/**
	 * @return the getCustomerSppaBill()
	 */
	public static CustomerSppaBill getCustomerSppaBill() {
		return customerSppaBill;
	}
	/**
	 * @param customerSppaBill the customerSppaBill to set
	 */
	public static void setCustomerSppaBill(CustomerSppaBill customerSppaBill) {
		BillUtilityCalculator.customerSppaBill = customerSppaBill;
	}

	/**
	 * Formatter Function returning EMInstance Saving
	 * @param sppaBill 
	 * 
	 * @return sppaPayableToEnlighted
	 */
	public static String getEMInstanceSaving(SppaBill sppaBill) {
		BigDecimal savings = new BigDecimal(0);
		savings = (sppaBill.getBaselineEnergy()
				.subtract(sppaBill.getConsumedEnergy())).multiply(
				new BigDecimal(100)).divide(sppaBill.getBaselineEnergy(),
				0, RoundingMode.UP);
		
		return savings.toString();
	}
	/**
	 * Formatter Function returning Current Charges from CustomerSppaBill
	 * @param CustomerSppaBill 
	 * 
	 * @return current charges
	 */
	public static String getCurrentCharges() {
		usdCostFormat.setMaximumFractionDigits(2);
		Double currentCharges = getCustomerSppaBill().getCurrentCharges();
		return usdCostFormat.format(currentCharges);
	}
	
	/**
	 * Formatter Function returning Previous Amount due
	 * @param CustomerSppaBill 
	 * 
	 * @return previous Amount Due from Bill Payment table
	 */
	public static String getPrevAmtDue() {
		usdCostFormat.setMaximumFractionDigits(2);
		Double prevAmtDue = getCustomerSppaBill().getPrevAmtDue();
		if(prevAmtDue!=null)
			return usdCostFormat.format(prevAmtDue);
		else
			return usdCostFormat.format(0);
	}
	public static String getPaymentReceived() {
		usdCostFormat.setMaximumFractionDigits(2);
		Double paymentReceivedAmt = getCustomerSppaBill().getPaymentReceived();
		if(paymentReceivedAmt!=null)
			return usdCostFormat.format(paymentReceivedAmt);
		else
			return usdCostFormat.format(0);
	}
	
	
}
