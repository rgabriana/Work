package com.emscloud.mvc.controller.view;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.document.AbstractPdfView;

import com.emscloud.model.CustomerSppaBill;
import com.emscloud.model.SppaBill;
import com.lowagie.text.Document;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;
public class BillPDFView extends AbstractPdfView {
	
	@SuppressWarnings("unchecked")
	@Override
	protected void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer, 
	        HttpServletRequest request, HttpServletResponse response) throws Exception {
	    
		Map<String,Object> billingData = (Map<String,Object>) model.get("billingData");
	    
		//TO DISPLAY AGGREGATED DATA to be shown on the Upper part of Billing Use below data model
		CustomerSppaBill customerSppaBill = (CustomerSppaBill) billingData.get("customerSppaBill");
		
		
		// To Show Site Report used below data model
		List<SppaBill> sppaBills = (List<SppaBill>) billingData.get("sppaBill");
		Table table = new Table(12);
		table.addCell("Geo Loc Code");
		table.addCell("Name");
		table.addCell("Guideline Usage(kWh)");
		table.addCell("Actual Usage(kWh)");
		table.addCell("Savings (kWh)");
		table.addCell("SPPA Rate");
		table.addCell("SPPA Cost");
		table.addCell("Tax");
		table.addCell("SPPA Payment Due");
		table.addCell("Savings(%)");
		table.addCell("Block Purchase Remaining (kWh)");
		table.addCell("Block Purchase Term Remaining");
		//NOTE : For Other Column Data, We need to apply formatter logic before displaying into PDF. CHeck Different formatter applied 
		// in the customer_gmb_invoice.jsp
		for(SppaBill sppaBill : sppaBills) {
			table.addCell(sppaBill.getGeoLocation());
			table.addCell(sppaBill.getEmInstance().getName());
		}
		document.add(table);
	}
}