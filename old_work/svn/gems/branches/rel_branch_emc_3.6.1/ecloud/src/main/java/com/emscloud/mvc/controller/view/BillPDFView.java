package com.emscloud.mvc.controller.view;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.document.AbstractPdfView;

import com.emscloud.model.CustomerSppaBill;
import com.emscloud.model.SppaBill;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ExtendedColor;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;


public class BillPDFView extends AbstractPdfView {

	private static int normalTextZize = 9;
	private static int smallTextZize = 7;
	private static int MediumTextZize = 8;
	private static int largeTextZize = 11;
	private static int extraLargeTextZize = 16;
	Color customGreenColor = new Color (0, 51, 0);
	Color RedColor = new Color (255, 0, 0);
	
	/**
	 * Fonts used in the Report
	 */ 
	Font tahoma = FontFactory.getFont("Tahoma",FontFactory.defaultEncoding, BaseFont.EMBEDDED,normalTextZize,Font.NORMAL,customGreenColor);
	Font tahomaSmall = FontFactory.getFont("Tahoma",FontFactory.defaultEncoding, BaseFont.EMBEDDED,smallTextZize,Font.NORMAL,customGreenColor);
	Font tahomaSmallBold = FontFactory.getFont("Tahoma",FontFactory.defaultEncoding, BaseFont.EMBEDDED,MediumTextZize,Font.BOLD,customGreenColor);
	Font tahomaBold = FontFactory.getFont("Tahoma",FontFactory.defaultEncoding, BaseFont.EMBEDDED,normalTextZize, Font.BOLD,customGreenColor);
	Font headerFont = FontFactory.getFont("Tahoma",FontFactory.defaultEncoding, BaseFont.EMBEDDED,extraLargeTextZize, Font.BOLD,customGreenColor);
	Font tahomaRedBold = FontFactory.getFont("Tahoma",FontFactory.defaultEncoding, BaseFont.EMBEDDED,largeTextZize,Font.BOLD,RedColor);
		
	/**
	 * Formatters used in the Report
	 */ 
	NumberFormat usdCostFormat1 = NumberFormat.getCurrencyInstance(Locale.US);
	DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ENGLISH);
	NumberFormat numberFormatter1 = NumberFormat.getNumberInstance(Locale.US);
	
	/**
	 * Method Generates the Bill in the form of PDF
	 * 
	 * @return PdfPTable
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void buildPdfDocument(Map<String, Object> model,
			Document document, PdfWriter writer, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		// billingData is the Map of all Data required to generate bill
		Map<String, Object> billingData = (Map<String, Object>) model
				.get("billingData");
		document.setPageSize(PageSize.A4);
		
		//SITE REPORTS TABLE
		List<SppaBill> sppaBills = (List<SppaBill>) billingData.get("sppaBill");
		int noOfSites = sppaBills.size();
		
		// TO DISPLAY AGGREGATED DATA to be shown on the Upper part of Billing
		// Use below data model
		CustomerSppaBill customerSppaBill = (CustomerSppaBill) billingData.get("customerBill");
		
		BillUtilityCalculator.setCustomerSppaBill(customerSppaBill);
		
		//ADDING LOGO - ENLIGHTED, ATT AND RED BG
		String clientLogo = request.getSession().getServletContext().getRealPath("/themes/default/images/att.jpg");
		Image image1 = Image.getInstance(clientLogo);
		image1.scaleAbsolute(66, 87);
		image1.setAbsolutePosition(5,(PageSize.A4.getHeight() - image1.getScaledHeight()));

		String companyLogo = request.getSession().getServletContext().getRealPath("/themes/default/images/logo.png");
		Image image2 = Image.getInstance(companyLogo);
		image2.scaleAbsolute(143, 28);
		image2.setAbsolutePosition((PageSize.A4.getWidth() - image2.getScaledWidth()),(PageSize.A4.getHeight() - image2.getScaledHeight()-10));
		
		String redBg = request.getSession().getServletContext().getRealPath("/themes/default/images/ReportBG.png");
		Image image3 = Image.getInstance(redBg);
		image3.scaleAbsolute(595,78);
		System.out.println("Page Width "+ PageSize.A4.getWidth() +  "  Page Hight " + PageSize.A4.getHeight());
		System.out.println("image1 " + image1.getAbsoluteY() + "  " + image1.getPlainHeight());
		image3.setAbsolutePosition(0,(image1.getAbsoluteY() - image1.getPlainHeight()+20));
		
		document.add(image1);                                                                 
		document.add(image2);
		document.add(image3);
		
		// INVOICE LABEL
		Chunk chunk = new Chunk("Invoice",headerFont); 
		Paragraph headerPara = new Paragraph(chunk);
		headerPara.setFont(headerFont);
		headerPara.setIndentationLeft((PageSize.A4.getWidth()/2)-70);
		headerPara.setSpacingBefore(38);
		document.add(headerPara);
		
		//PERIOD SECTION showing startDate , EndDate and Created Date
		PdfPTable periodTable = getPeriodDisplayTable(customerSppaBill);
		PdfContentByte canvas = writer.getDirectContent();
	    // draw the first two columns on one page
		periodTable.writeSelectedRows(0, 2, 420, 792, canvas);
		
		//ADDRESS SECTION 
		PdfPTable addressTable = getSiteAddressTable(customerSppaBill,writer, noOfSites);
		PdfContentByte addressCanvas = writer.getDirectContent();
	    // draw the first two columns on one page
		addressTable.writeSelectedRows(0, 2, 10, 727, addressCanvas);
		
		//Rounded Rectangle Section : Displaying Cost/Kwh
		PdfContentByte cb = writer.getDirectContent();
		cb.roundRectangle(465f, 642f, 100f, 50f, 10f);
		cb.stroke();
		Paragraph paragraph = getCostPerKWH(customerSppaBill,writer);
		paragraph.setIndentationLeft(440);
		paragraph.setSpacingBefore(48);
		document.add(paragraph);
		
		//Get Consolidated Usage Summary
		PdfPTable usageTable = getConsolidateUsageTable(customerSppaBill);
		PdfContentByte usageCanvas = writer.getDirectContent();
		usageTable.writeSelectedRows(0, 8, 10, 627, usageCanvas);
		
		//Get Payment Due Summary
		Date billDueDate = (Date) billingData.get("billDueDate");
		PdfPTable paymentDueTable = getPaymentDueTable(customerSppaBill,billDueDate);
		PdfContentByte payDueCanvas = writer.getDirectContent();
		paymentDueTable.writeSelectedRows(0, 5, 10, 480, payDueCanvas);
		
		//Site Report Header label
		Chunk siteReportChunk = new Chunk("Sites Report",headerFont); 
		Paragraph headerPara2 = new Paragraph(siteReportChunk);
		headerPara2.setFont(headerFont);
		headerPara2.setIndentationLeft((PageSize.A4.getWidth()/2)-70);
		headerPara2.setSpacingBefore(260);
		document.add(headerPara2);
		
		//SITE REPORTS
		PdfPTable siteReports = getSiteReportTable(sppaBills);
		siteReports.setSpacingBefore(20);
		
		document.add(siteReports);
		document.close();
		/*CustomerManager customerManager = (CustomerManager) SpringContext.getBean("customerManager");
		Customer c = customerManager.loadCustomerById(customerSppaBill.getCustomer().getId());*/
		DateFormat df = new SimpleDateFormat("yyyy.MM.dd");
		String filename = customerSppaBill.getCustomer().getName().replaceAll("[^A-Za-z0-9_]", "") + "_" + df.format(customerSppaBill.getBillStartDate()) + "-" + df.format(customerSppaBill.getBillEndDate());
		response.setHeader("Content-Disposition","attachment;filename=" + filename + ".pdf");
	}
	/**
	 * Returns Site Report Table
	 * 
	 * @return PdfPTable
	 */
	public PdfPTable getSiteReportTable(List<SppaBill> sppaBills) throws DocumentException{
		// To Show Site Report used below data model
		int[] widths = { 5, 7, 9, 9, 9, 9, 6, 8, 8, 8, 6, 8, 8 };
		PdfPTable table = new PdfPTable(13);
	    table.getDefaultCell().setBackgroundColor(ExtendedColor.LIGHT_GRAY);
	    table.setWidthPercentage(108);
		table.setWidths(widths);
	
		table.addCell(new Phrase("Geo Loc Code", tahomaSmallBold));
		table.addCell(new Phrase("Name", tahomaSmallBold));
		table.addCell(new Phrase("PO Number", tahomaSmallBold));
		table.addCell(new Phrase("Guideline Usage (kWh)", tahomaSmallBold));
		table.addCell(new Phrase("Actual Usage\n(kWh)", tahomaSmallBold));
		table.addCell(new Phrase("Savings (kWh)", tahomaSmallBold));
		table.addCell(new Phrase("SPPA Rate", tahomaSmallBold));
		table.addCell(new Phrase("SPPA Cost", tahomaSmallBold));
		table.addCell(new Phrase("Tax", tahomaSmallBold));
		table.addCell(new Phrase("SPPA Payment Due", tahomaSmallBold));
		table.addCell(new Phrase("Savings\n(%)", tahomaSmallBold));
		table.addCell(new Phrase("Block Purchase Remaining (kWh)", tahomaSmallBold));
		table.addCell(new Phrase("Block Purchase Term Remaining",tahomaSmallBold));
		
		table.getDefaultCell().setBackgroundColor(null);
		// NOTE : For Other Column Data, We need to apply formatter logic before
		// displaying into PDF. CHeck Different formatter applied
		// in the customer_gmb_invoice.jsp
		for (SppaBill sppaBill : sppaBills) {
			PdfPCell geoLocationCell= new PdfPCell();
			geoLocationCell.addElement(new Phrase(sppaBill.getGeoLocation(), tahomaSmall));
			table.addCell(geoLocationCell);
			PdfPCell nameCell= new PdfPCell();
			nameCell.addElement(new Phrase(sppaBill.getName(),tahomaSmall));
			table.addCell(nameCell);
			PdfPCell poNumberCell = new PdfPCell();
			poNumberCell.addElement(new Phrase(sppaBill.getPoNumber(), tahomaSmall));
			table.addCell(poNumberCell);
			table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
			BigDecimal baseLineEnergy = new BigDecimal(0);
			BigDecimal consumedEnergy = new BigDecimal(0);
			if(sppaBill.getBaselineEnergy()!=null)
			{
				baseLineEnergy = sppaBill.getBaselineEnergy().divide(new BigDecimal(1000),2,RoundingMode.HALF_UP);
			}
			if(sppaBill.getConsumedEnergy()!=null)
			{
				consumedEnergy = sppaBill.getConsumedEnergy().divide(new BigDecimal(1000),2,RoundingMode.HALF_UP);
			}
			BigDecimal savedEnergy = sppaBill.getEnergySaved().divide(new BigDecimal(1000),2,RoundingMode.HALF_UP);
			
			String savingStr="0";
			if(sppaBill.getBaselineEnergy()!=null && sppaBill.getBaselineEnergy().compareTo(BillUtilityCalculator.ZERO_BIG_DECIMAL_VALUE)>0)
			{
				savingStr = BillUtilityCalculator.getEMInstanceSaving(sppaBill);
			}
			BigDecimal blockEnergyRemaining = sppaBill
					.getBlockEnergyRemaining().divide(new BigDecimal(1), 2,RoundingMode.CEILING);
			
			BigDecimal blockTermRemaining = new BigDecimal(0);
			if(sppaBill.getBlockTermRemaining()!=null)
			{
				blockTermRemaining = BigDecimal.valueOf((Double.valueOf(String.valueOf(sppaBill.getBlockTermRemaining()))));
			}
			String blockTermRemainingString = "";
			
			blockTermRemaining = blockTermRemaining.divide(new BigDecimal(365),2,RoundingMode.CEILING);
			if (blockTermRemaining.compareTo(new BigDecimal(1)) == 0) {
				blockTermRemainingString = blockTermRemaining + " year";
			} else
				blockTermRemainingString = blockTermRemaining + " years";
			
	
			BigDecimal sppaRate = new BigDecimal(String.valueOf(sppaBill
					.getSppaPrice()));// .setScale(4,
														// BigDecimal.ROUND_UP);
			BigDecimal sppaCost = new BigDecimal(String.valueOf(sppaBill
					.getSppaCost()));
			BigDecimal sppaTax = new BigDecimal(String.valueOf(sppaBill
					.getTax()));
			BigDecimal sppaPaymentDue = new BigDecimal(String.valueOf(sppaBill
					.getSppaPayableDue()));
			
			table.addCell(new Phrase(numberFormatter1.format(baseLineEnergy.doubleValue()),tahomaSmall));
			table.addCell(new Phrase(numberFormatter1.format(consumedEnergy.doubleValue()),tahomaSmall));
			table.addCell(new Phrase(numberFormatter1.format(savedEnergy.doubleValue()), tahomaSmall));
	
			// display currecy wise
			usdCostFormat1.setMinimumFractionDigits(4);
			usdCostFormat1.setMaximumFractionDigits(4);
			table.addCell(new Phrase(String.valueOf(usdCostFormat1
					.format(sppaRate.doubleValue())), tahomaSmall));
			usdCostFormat1.setMinimumFractionDigits(2);
			usdCostFormat1.setMaximumFractionDigits(2);
			table.addCell(new Phrase(String.valueOf(usdCostFormat1
					.format(sppaCost.doubleValue())), tahomaSmall));
			usdCostFormat1.setMinimumFractionDigits(2);
			usdCostFormat1.setMaximumFractionDigits(2);
			table.addCell(new Phrase(String.valueOf(usdCostFormat1
					.format(sppaTax.doubleValue())), tahomaSmall));
			usdCostFormat1.setMinimumFractionDigits(2);
			usdCostFormat1.setMaximumFractionDigits(2);
			table.addCell(new Phrase(String.valueOf(usdCostFormat1
					.format(sppaPaymentDue.doubleValue())),tahomaSmall));
			table.addCell(new Phrase(savingStr, tahomaSmall));
			table.addCell(new Phrase(numberFormatter1.format(blockEnergyRemaining),tahomaSmall));
			table.addCell(new Phrase(String.valueOf(blockTermRemainingString),tahomaSmall));
		}
		return table;
	}
	/**
	 * Returns Period Table containing Bill Start Date, Bill End Date and Bill Created Date
	 * 
	 * @return PdfPTable
	 */
	public PdfPTable getPeriodDisplayTable(CustomerSppaBill customerSppaBill) throws DocumentException{
		Date startDate =customerSppaBill.getBillStartDate();
		Date endDate = customerSppaBill.getBillEndDate();
		Date creationDate = customerSppaBill.getBillCreationTime();
		Chunk billStartDateStr = new Chunk(df.format(startDate),tahomaSmall);
		Chunk billEndDateStr = new Chunk(df.format(endDate),tahomaSmall);
		Chunk billCreatedDateStr = new Chunk(df.format(creationDate),tahomaSmall);
		
		Chunk reportPeriod = new Chunk("Report Period : ",tahomaSmall);
		Chunk reportDate = new Chunk("Report Date : ",tahomaSmall);
		Chunk to = new Chunk(" to ",tahoma);
		Paragraph periodpara1 = new Paragraph();
		periodpara1.add(reportPeriod);
		periodpara1.add(billStartDateStr);
		periodpara1.add(to);
		periodpara1.add(billEndDateStr);
		
		Paragraph periodpara2 = new Paragraph();
		periodpara2.add(reportDate);
		periodpara2.add(billCreatedDateStr);
		
		PdfPTable periodTable = new PdfPTable(1);
		periodTable.setTotalWidth(250);
		
		PdfPCell periodTableCell1= new PdfPCell();
		periodTableCell1.addElement(periodpara1);
		periodTableCell1.setPadding(0);
		periodTableCell1.setNoWrap(false);

		periodTableCell1.setBorder(PdfPCell.NO_BORDER);
		
		PdfPCell periodTableCell2= new PdfPCell();
		periodTableCell2.setNoWrap(false);
		periodTableCell2.setPadding(0);
		periodTableCell2.setHorizontalAlignment(0); 

		periodTableCell2.addElement(periodpara2);
		periodTableCell2.setBorder(PdfPCell.NO_BORDER);

		periodTable.addCell(periodTableCell1);
		periodTable.addCell(periodTableCell2);
		periodTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
		return periodTable;
	}
	/**
	 * Returns Site Address Table containing site Address, Bill No Of Days and Number Of Sites.
	 * 
	 * @return PdfPTable
	 */
	public PdfPTable getSiteAddressTable(CustomerSppaBill customerSppaBill,PdfWriter writer, int sitesCount) throws DocumentException{
		
		Chunk siteaddress =  new Chunk("AT&T Energy Department\n208 S.Akard St.\nDallas,TX 75202",tahomaSmall);
		Chunk billingDays = new Chunk("\nBilling No of Days : "+customerSppaBill.getNoOfDays(),tahomaSmall);
		Chunk noOfSites = new Chunk("\nNumber of Sites : "+ sitesCount ,tahomaSmall);

		Paragraph periodpara1 = new Paragraph();
		periodpara1.add(siteaddress);
		
		Paragraph periodpara2 = new Paragraph();
		periodpara2.add(billingDays);
		periodpara2.add(noOfSites);
		
		PdfPTable periodTable = new PdfPTable(1);
		periodTable.setTotalWidth(250);
		
		PdfPCell periodTableCell1= new PdfPCell();
		periodTableCell1.addElement(periodpara1);
		periodTableCell1.setPadding(0);
		periodTableCell1.setNoWrap(false);

		periodTableCell1.setBorder(PdfPCell.NO_BORDER);
		
		PdfPCell periodTableCell2= new PdfPCell();
		periodTableCell2.setNoWrap(false);
		periodTableCell2.setPadding(0);
		periodTableCell2.setHorizontalAlignment(0); 

		periodTableCell2.addElement(periodpara2);
		periodTableCell2.setBorder(PdfPCell.NO_BORDER);

		periodTable.addCell(periodTableCell1);
		periodTable.addCell(periodTableCell2);
		periodTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
		
		return periodTable;
	}
	/**
	 * Returns Cost/KWh Section in the Bill which contains Average SPPA and Average Utility
	 * 
	 * @return Cost/KWh Paragraph
	 */
	public Paragraph getCostPerKWH(CustomerSppaBill customerSppaBill,PdfWriter writer)throws DocumentException{
		Paragraph roundRectable = new Paragraph();
		Chunk costKwhLabel =  new Chunk("Cost per kWh",tahomaRedBold);
		Chunk averageSppaLabel =  new Chunk("Average sPPA:  "+BillUtilityCalculator.getAverageSppa()+"\n",tahomaSmall);
		Chunk averageUtilityLabel =  new Chunk("Average Utility:  "+BillUtilityCalculator.getAverageUtility(),tahomaSmall);
		roundRectable.add(costKwhLabel);
		roundRectable.add(averageSppaLabel);
		roundRectable.add(averageUtilityLabel);
		return roundRectable;
	}
	
	/**
	 * Returns Consolidated Guideline Usage Summary Table containing Guideline Usage, Actual Usage, Savings, Saved by AT&T etc.
	 * 
	 * @return PdfPTable
	 */
	public PdfPTable getConsolidateUsageTable(CustomerSppaBill customerSppaBill) throws DocumentException{
		
		PdfPTable usageTable = new PdfPTable(3);
		usageTable.setTotalWidth(570);
		
		PdfPCell spaceCell= new PdfPCell();
		spaceCell.addElement(new Phrase("",tahoma));
		spaceCell.setBorder(PdfPCell.NO_BORDER);
		usageTable.addCell(spaceCell);
		
		PdfPCell kwhLabel= new PdfPCell();
		kwhLabel.addElement(new Phrase("KWh",tahomaBold));
		kwhLabel.setBorder(PdfPCell.NO_BORDER);
		usageTable.addCell(kwhLabel);
		
		usageTable.addCell(spaceCell);

		PdfPCell guideLineUsageLabel= new PdfPCell();
		guideLineUsageLabel.addElement(new Phrase("Guideline Usage:",tahomaBold));
		guideLineUsageLabel.setBorder(PdfPCell.NO_BORDER);
		usageTable.addCell(guideLineUsageLabel);
		
		PdfPCell guideLineUsagePowerValue= new PdfPCell();
		guideLineUsagePowerValue.addElement(new Phrase(BillUtilityCalculator.getGuidelLineUsageInPower(),tahoma));
		guideLineUsagePowerValue.setBorder(PdfPCell.NO_BORDER);
		usageTable.addCell(guideLineUsagePowerValue);
		
		PdfPCell guideLineUsageMoneyValue= new PdfPCell();
		guideLineUsageMoneyValue.addElement(new Phrase(BillUtilityCalculator.getGuidelLineUsageInMoney(),tahoma));
		guideLineUsageMoneyValue.setBorder(PdfPCell.NO_BORDER);
		usageTable.addCell(guideLineUsageMoneyValue);
		
		PdfPCell actualUsageLabel= new PdfPCell();
		actualUsageLabel.addElement(new Phrase("Actual Usage:",tahomaBold));
		actualUsageLabel.setBorder(PdfPCell.NO_BORDER);
		usageTable.addCell(actualUsageLabel);
		
		PdfPCell actualUsagePowerValue= new PdfPCell();
		actualUsagePowerValue.addElement(new Phrase(BillUtilityCalculator.getActualUsageInPower(),tahoma));
		actualUsagePowerValue.setBorder(PdfPCell.NO_BORDER);
		usageTable.addCell(actualUsagePowerValue);
		
		PdfPCell actualUsageMoneyValue= new PdfPCell();
		actualUsageMoneyValue.addElement(new Phrase(BillUtilityCalculator.getActualUsageInMoney(),tahoma));
		actualUsageMoneyValue.setBorder(PdfPCell.NO_BORDER);
		usageTable.addCell(actualUsageMoneyValue);
		
		PdfPCell savingLabel= new PdfPCell();
		savingLabel.addElement(new Phrase("Savings",tahomaBold));
		savingLabel.setBorder(PdfPCell.NO_BORDER);
		usageTable.addCell(savingLabel);
		
		PdfPCell savingPercValue= new PdfPCell();
		savingPercValue.addElement(new Phrase(BillUtilityCalculator.getSavingInPercentage(),tahoma));
		savingPercValue.setBorder(PdfPCell.NO_BORDER);
		usageTable.addCell(savingPercValue);
		
		PdfPCell savingMoneyValue= new PdfPCell();
		savingMoneyValue.addElement(new Phrase(BillUtilityCalculator.getSavingInMoney(),tahoma));
		savingMoneyValue.setBorder(PdfPCell.NO_BORDER);
		usageTable.addCell(savingMoneyValue);
		
		PdfPCell savedByAttLabel= new PdfPCell();
		savedByAttLabel.addElement(new Phrase("Saved by AT&T",tahomaBold));
		savedByAttLabel.setBorder(Rectangle.BOTTOM | Rectangle.TOP);
		usageTable.addCell(savedByAttLabel);
		
		PdfPCell spaceBorderCell= new PdfPCell();
		spaceBorderCell.addElement(new Phrase("",tahoma));
		spaceBorderCell.setBorder(Rectangle.BOTTOM | Rectangle.TOP);
		usageTable.addCell(spaceBorderCell);
		
		PdfPCell savedByAttValue= new PdfPCell();
		savedByAttValue.addElement(new Phrase(BillUtilityCalculator.getSavedByATT(),tahomaBold));
		savedByAttValue.setBorder(Rectangle.BOTTOM | Rectangle.TOP);
		usageTable.addCell(savedByAttValue);
		
		PdfPCell sppaPayableLabel= new PdfPCell();
		sppaPayableLabel.addElement(new Phrase("SPPA Payable",tahoma));
		sppaPayableLabel.setBorder(PdfPCell.NO_BORDER);
		usageTable.addCell(sppaPayableLabel);
		
		usageTable.addCell(spaceCell);
		
		PdfPCell sppaPayableValue= new PdfPCell();
		sppaPayableValue.addElement(new Phrase(BillUtilityCalculator.getSppaPayable(),tahoma));
		sppaPayableValue.setBorder(PdfPCell.NO_BORDER);
		usageTable.addCell(sppaPayableValue);
		
		PdfPCell taxLabel= new PdfPCell();
		taxLabel.addElement(new Phrase("Tax",tahomaBold));
		taxLabel.setBorder(PdfPCell.NO_BORDER);
		usageTable.addCell(taxLabel);
		
		usageTable.addCell(spaceCell);
		
		PdfPCell taxValue= new PdfPCell();
		taxValue.addElement(new Phrase(BillUtilityCalculator.getTax(),tahoma));
		taxValue.setBorder(PdfPCell.NO_BORDER);
		usageTable.addCell(taxValue);
		
		PdfPCell sppaPayableToEnlightedLabel= new PdfPCell();
		sppaPayableToEnlightedLabel.addElement(new Phrase("SPPA payable to Enlighted ",tahomaBold));
		sppaPayableToEnlightedLabel.setBorder(Rectangle.TOP);
		usageTable.addCell(sppaPayableToEnlightedLabel);
		
		PdfPCell spaceTopBorderCell= new PdfPCell();
		spaceTopBorderCell.addElement(new Phrase("",tahoma));
		spaceTopBorderCell.setBorder(Rectangle.TOP);
		usageTable.addCell(spaceTopBorderCell);
		
		PdfPCell sppaPayableToEnlightedValue= new PdfPCell();
		sppaPayableToEnlightedValue.addElement(new Phrase(BillUtilityCalculator.getSppaPayableToEnlighted(),tahomaBold));
		sppaPayableToEnlightedValue.setBorder(Rectangle.TOP);
		usageTable.addCell(sppaPayableToEnlightedValue);
		
		return usageTable;
	}
	
	/**
	 * Returns Consolidated Guideline Usage Summary Table containing Guideline Usage, Actual Usage, Savings, Saved by AT&T etc.
	 * @param billDueDate 
	 * 
	 * @return PdfPTable
	 */
	public PdfPTable getPaymentDueTable(CustomerSppaBill customerSppaBill, Date billDueDate) throws DocumentException{
		PdfPTable paymentDueTable = new PdfPTable(2); // Code 1
		paymentDueTable.setTotalWidth(570);
		
		PdfPCell previousAmtDueLabel= new PdfPCell();
		previousAmtDueLabel.addElement(new Phrase("Previous Amount Due:",tahoma));
		previousAmtDueLabel.setBorder(PdfPCell.NO_BORDER);
		paymentDueTable.addCell(previousAmtDueLabel);
		
		String prevAmtDueStr = BillUtilityCalculator.getPrevAmtDue();
		Phrase prevAmtDueStrPara = new Phrase(prevAmtDueStr, tahoma);
		
		PdfPCell previousAmtDueValue= new PdfPCell();
		previousAmtDueValue.addElement(prevAmtDueStrPara);
		previousAmtDueValue.setBorder(PdfPCell.NO_BORDER);
		paymentDueTable.addCell(previousAmtDueValue);
		
		PdfPCell rceivedLabel= new PdfPCell();
		rceivedLabel.addElement(new Phrase("Received:",tahoma));
		rceivedLabel.setBorder(PdfPCell.NO_BORDER);
		paymentDueTable.addCell(rceivedLabel);
		
		String amountRecievedStr = BillUtilityCalculator.getPaymentReceived();
		Phrase amountRecievedPara = new Phrase(amountRecievedStr, tahoma);
		
		PdfPCell rceivedValue= new PdfPCell();
		rceivedValue.addElement(amountRecievedPara);
		rceivedValue.setBorder(PdfPCell.NO_BORDER);
		paymentDueTable.addCell(rceivedValue);
		
		PdfPCell currentChargesLabel= new PdfPCell();
		currentChargesLabel.addElement(new Phrase("Current Charges:",tahoma));
		currentChargesLabel.setBorder(PdfPCell.NO_BORDER);
		paymentDueTable.addCell(currentChargesLabel);
		
		String currentChargesStr = BillUtilityCalculator.getCurrentCharges();
		Phrase currentChargesPara = new Phrase(currentChargesStr, tahoma);
		
		PdfPCell currentChargesValue= new PdfPCell();
		currentChargesValue.addElement(currentChargesPara);
		currentChargesValue.setBorder(PdfPCell.NO_BORDER);
		paymentDueTable.addCell(currentChargesValue);
		
		String totalAmountDueStr = BillUtilityCalculator.getTotalAmountDue();
		Phrase totalAmountDuePara = new Phrase(totalAmountDueStr, tahoma);
		
		//Date Formatter
		String billDueDateStr = df.format(billDueDate);
		Phrase billDueDatePara = new Phrase(billDueDateStr, tahoma);
		
		PdfPCell totalAmountDueLabel= new PdfPCell();
		totalAmountDueLabel.addElement(new Phrase("Total Amount Due:",tahomaBold));
		totalAmountDueLabel.setBorder(PdfPCell.NO_BORDER);
		paymentDueTable.addCell(totalAmountDueLabel);
		
		PdfPCell totalAmountDueValue= new PdfPCell();
		totalAmountDueValue.addElement(totalAmountDuePara);
		totalAmountDueValue.setBorder(PdfPCell.NO_BORDER);
		paymentDueTable.addCell(totalAmountDueValue);
		
		PdfPCell billDueDateLabel= new PdfPCell();
		billDueDateLabel.addElement(new Phrase("Bill Due date:",tahomaBold));
		billDueDateLabel.setBorder(PdfPCell.NO_BORDER);
		paymentDueTable.addCell(billDueDateLabel);
		
		PdfPCell billDueDateValue= new PdfPCell();
		billDueDateValue.addElement(billDueDatePara);
		billDueDateValue.setBorder(PdfPCell.NO_BORDER);
		paymentDueTable.addCell(billDueDateValue);
		
		return paymentDueTable;
	}
}