package com.ems.util;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.ems.utils.DateUtil;
import com.ems.utils.RequestUtil;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

public class ExportPDFUtil {

	private static Float cellHeight= 19F;
	private static Float borderBottom = .4F;
	private static int normalTextZize = 9;
	@SuppressWarnings("unchecked")
	public void createPDF(String file, String file2, Document document,
			HttpServletRequest request,String treePath,String fromDate,String toDate,String fromTime,String toTime) {
		try {
			List list = (List)request.getSession().getAttribute("pieChartList");
			PdfPTable mainTable = new PdfPTable(2);
			float Widths[] = {35, 65};
			mainTable.setWidths(Widths);
			mainTable.setWidthPercentage(100);
			/*Font font = new Font();
			font.setSize(normalTextZize);
			font.setFamily(FontFactory.TIMES);*/
			Font font = FontFactory.getFont(FontFactory.HELVETICA,normalTextZize, Font.NORMAL, Color.BLACK);
			
			PdfPCell pieChartData = new PdfPCell();
			pieChartData.setBorderColor(Color.GRAY);
			pieChartData.setBorderWidth(borderBottom);
			pieChartData.setBorder(Rectangle.NO_BORDER);
			pieChartData.setPaddingBottom(10);
			pieChartData.setColspan(2);
			String contextPath = RequestUtil.getAppURL(request);
			try {
				// Throws a security exception with self signed certificates.
				Image logo = Image.getInstance(new URL(contextPath+"/images/Enlighted_FinalLogo_small.jpg"));
				logo.scaleAbsolute(100, 200);
				pieChartData.addElement(logo);
			} catch(IOException ioe) {
				
			}
			mainTable.addCell(pieChartData);
			
			pieChartData = new PdfPCell(new Paragraph(treePath,font));
			pieChartData.setBorderColor(Color.GRAY);
			pieChartData.setBorderWidth(borderBottom);
			pieChartData.setBorder(Rectangle.NO_BORDER);
			pieChartData.setPaddingBottom(10);
			mainTable.addCell(pieChartData);
			
			String fromDateString = DateUtil.formatDate(DateUtil.parseString(fromDate), "yyyy-MM-dd")+" "+fromTime;
			String toDateString = DateUtil.formatDate(DateUtil.parseString(toDate), "yyyy-MM-dd")+" "+toTime;
			
			pieChartData = new PdfPCell(new Paragraph(fromDateString+"  TO  "+toDateString,font));
			pieChartData.setBorderColor(Color.GRAY);
			pieChartData.setBorderWidth(borderBottom);
			pieChartData.setBorder(Rectangle.NO_BORDER);
			pieChartData.setPaddingBottom(10);
			pieChartData.setHorizontalAlignment(Element.ALIGN_RIGHT);
			mainTable.addCell(pieChartData);
			
			font = FontFactory.getFont(FontFactory.HELVETICA,normalTextZize, Font.NORMAL, Color.WHITE);
			pieChartData = new PdfPCell();
			pieChartData.setBorderColor(Color.GRAY);
			pieChartData.setBorderWidth(borderBottom);
			PdfPTable pieChartTable = new PdfPTable(3);
			float pieChartTableWidths[] = {42, 32 ,25};
			pieChartTable.setWidths(pieChartTableWidths);
			pieChartTable.setWidthPercentage(100);
			PdfPCell pieChartDataRow = new PdfPCell(new Paragraph("Group",font));
			pieChartDataRow.setBorderColor(Color.BLACK);
			pieChartDataRow.setBackgroundColor(Color.GRAY);
			pieChartDataRow.setBorderWidth(borderBottom);
			pieChartTable.addCell(pieChartDataRow);
			pieChartDataRow = new PdfPCell(new Paragraph("Energy used (kW)",font));
			pieChartDataRow.setBorderColor(Color.BLACK);
			pieChartDataRow.setBackgroundColor(Color.GRAY);
			pieChartDataRow.setBorderWidth(borderBottom);
			pieChartTable.addCell(pieChartDataRow);
			pieChartDataRow = new PdfPCell(new Paragraph("No of fixtures",font));
			pieChartDataRow.setBorderColor(Color.BLACK);
			pieChartDataRow.setBackgroundColor(Color.GRAY);
			pieChartDataRow.setBorderWidth(borderBottom);
			pieChartTable.addCell(pieChartDataRow);
			font = FontFactory.getFont(FontFactory.HELVETICA,normalTextZize, Font.NORMAL, Color.BLACK);
			for(int i=0;i<list.size();i++){
				Object[] val= (Object[])list.get(i);
				String group =String.valueOf(val[1]);
				BigDecimal energy = (BigDecimal)val[2];
				BigInteger numberOfFixture= (BigInteger)val[3];
				pieChartDataRow = new PdfPCell(new Paragraph(group,font));
				pieChartDataRow.setBorderColor(Color.GRAY);
				pieChartDataRow.setFixedHeight(cellHeight);
				pieChartDataRow.setBorderWidth(borderBottom);
				pieChartTable.addCell(pieChartDataRow);
				pieChartDataRow = new PdfPCell(new Paragraph(energy+"",font));
				pieChartDataRow.setBorderColor(Color.GRAY);
				pieChartDataRow.setFixedHeight(cellHeight);
				pieChartDataRow.setBorderWidth(borderBottom);
				pieChartTable.addCell(pieChartDataRow);
				pieChartDataRow = new PdfPCell(new Paragraph(numberOfFixture+"",font));
				pieChartDataRow.setBorderColor(Color.GRAY);
				pieChartDataRow.setFixedHeight(cellHeight);
				pieChartDataRow.setBorderWidth(borderBottom);
				pieChartTable.addCell(pieChartDataRow);
			}
			pieChartData.addElement(pieChartTable);
			mainTable.addCell(pieChartData);

			pieChartData = new PdfPCell();
			pieChartData.setBorderColor(Color.GRAY);
			pieChartData.setBorderWidth(borderBottom);
			String path =System.getProperty("java.io.tmpdir")+File.separator+file;
			path = path.replace("%20", " ");
			Image image = Image.getInstance(path);
			pieChartData.addElement(image);
			mainTable.addCell(pieChartData);

			pieChartData = new PdfPCell();
			pieChartData.setBorderColor(Color.GRAY);
			pieChartData.setBorderWidth(borderBottom);
			pieChartData.setColspan(2);
			path =System.getProperty("java.io.tmpdir")+File.separator+file2;
			path = path.replace("%20", " ");
			image = Image.getInstance(path);
			pieChartData.addElement(image);
			mainTable.addCell(pieChartData);
			document.add(mainTable);
		} catch (BadElementException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}

	}
}
