package com.ems.mvc.controller.view;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.document.AbstractPdfView;

import com.ems.model.Wds;
import com.ems.utils.ArgumentUtils;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ExtendedColor;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class WdsReportPdfView extends AbstractPdfView{
	private static int extraLargeTextZize = 16;
	private static int MediumTextZize = 8;
	private static int smallTextZize = 7;
	Color customGreenColor = new Color (0, 51, 0);
	Font headerFont = FontFactory.getFont("Tahoma",FontFactory.defaultEncoding, BaseFont.EMBEDDED,extraLargeTextZize, Font.BOLD,customGreenColor);
	Font tahomaSmallBold = FontFactory.getFont("Tahoma",FontFactory.defaultEncoding, BaseFont.EMBEDDED,MediumTextZize,Font.BOLD,customGreenColor);
	Font tahomaSmall = FontFactory.getFont("Tahoma",FontFactory.defaultEncoding, BaseFont.EMBEDDED,smallTextZize,Font.NORMAL,customGreenColor);
	@SuppressWarnings("unchecked")
	@Override
	protected void buildPdfDocument(Map model, Document document,
		PdfWriter writer, HttpServletRequest request,
		HttpServletResponse response) throws Exception {
 
		Map<String,Object> wdsReportData = (Map<String,Object>) model.get("wdsReportData");
 
		// TITLE LABEL
		Chunk chunk = new Chunk("ERC Battery Report",headerFont); 
		Paragraph headerPara = new Paragraph(chunk);
		headerPara.setFont(headerFont);
		headerPara.setIndentationLeft((PageSize.A4.getWidth()/2)-70);
		headerPara.setSpacingBefore(38);
		document.add(headerPara);
			
		int[] widths = { 5, 15, 9, 9};
		PdfPTable table = new PdfPTable(4);
	    table.getDefaultCell().setBackgroundColor(ExtendedColor.LIGHT_GRAY);
	    table.setWidthPercentage(108);
		table.setWidths(widths);
		
		
		table.addCell(new Phrase("ERC Name", tahomaSmallBold));
		table.addCell(new Phrase("Location", tahomaSmallBold));
		table.addCell(new Phrase("Battery Level", tahomaSmallBold));
		table.addCell(new Phrase("Last Reported Time", tahomaSmallBold));
		
		table.getDefaultCell().setBackgroundColor(null);
		List<Wds> wdsList = (List<Wds>) wdsReportData.get("wdsList");
		
		
		if(!ArgumentUtils.isNullOrEmpty(wdsList)){
		for (Wds wds : wdsList) {
			PdfPCell name= new PdfPCell();
			name.addElement(new Phrase(wds.getName(), tahomaSmall));
			table.addCell(name);
			
			PdfPCell loc= new PdfPCell();
			loc.addElement(new Phrase(wds.getLocation(),tahomaSmall));
			table.addCell(loc);
			
			PdfPCell batteryLvl = new PdfPCell();
			batteryLvl.addElement(new Phrase(wds.getBatteryLevel(), tahomaSmall));
			table.addCell(batteryLvl);
			
			PdfPCell capAt = new PdfPCell();
			capAt.addElement(new Phrase(wds.getCaptureAtStr(), tahomaSmall));
			table.addCell(capAt);
			
			}
		}
		table.setSpacingBefore(20);
		document.add(table);
		document.close();
	}

}
