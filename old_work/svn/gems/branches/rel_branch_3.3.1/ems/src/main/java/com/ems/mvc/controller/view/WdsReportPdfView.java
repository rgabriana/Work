package com.ems.mvc.controller.view;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.view.document.AbstractPdfView;

import com.ems.model.Wds;
import com.ems.utils.ArgumentUtils;
import com.lowagie.text.Document;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;

public class WdsReportPdfView extends AbstractPdfView{
	
	@SuppressWarnings("unchecked")
	@Override
	protected void buildPdfDocument(Map model, Document document,
		PdfWriter writer, HttpServletRequest request,
		HttpServletResponse response) throws Exception {
 
		Map<String,Object> wdsReportData = (Map<String,Object>) model.get("wdsReportData");
 
		Table table = new Table(4);
		table.addCell("ERC Name");
		table.addCell("Location");
		table.addCell("Battery Level");
		table.addCell("Last Reported Time");
		
		List<Wds> wdsList = (List<Wds>) wdsReportData.get("wdsList");
		if(!ArgumentUtils.isNullOrEmpty(wdsList)){
			for (int i = 0; i < wdsList.size(); i++) {
				Wds wds = wdsList.get(i);
				table.addCell(wds.getName());
				table.addCell(wds.getLocation());
				table.addCell(wds.getBatteryLevel());
				table.addCell(wds.getCaptureAtStr());
			}
		}
		
		document.add(table);
		document.close();
	}

}
