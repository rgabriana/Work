package com.ems.util;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer3D;
import org.jfree.chart.servlet.ServletUtilities;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.DefaultPieDataset;

import com.ems.action.SpringContext;
import com.ems.model.AvgBarChartRecord;
import com.ems.service.EnergyConsumptionManager;
import com.ems.utils.ArgumentUtils;
import com.ems.utils.DateUtil;

/**
 * 
 * @author pankaj kumar chauhan
 *
 */

public class WebHitChart {
	
	@SuppressWarnings("unchecked")
	public static CategoryDataset createDataset(Map<Integer, List<AvgBarChartRecord>> avgBarChartRecords,Date to,Date from,long days,int period) {
		List<double[]> data = new ArrayList<double[]>();
		Collection<List<AvgBarChartRecord>> collection = avgBarChartRecords.values();
		List rowKeys = null;
		Comparable[] columnKeys = new Comparable[new Integer(days+"")];
		/*int count = 0;
		Date fromTemp = from;
		while(count<days){
			columnKeys[count] = DateUtil.formatDate(fromTemp,"yyyy-MM-dd");
			fromTemp = DateUtil.addDays(fromTemp, period);
			count++;
		}*/
		if(collection != null && !collection.isEmpty()){
			rowKeys = new ArrayList();
			Iterator<List<AvgBarChartRecord>> iterator = collection.iterator();
			int j=0;
			while(iterator.hasNext()){
				List<AvgBarChartRecord> chartRecords = iterator.next();
				double[] temp = new double[chartRecords.size()];
				boolean find = false;
				int i=0;
				int k = 0;
				Iterator<AvgBarChartRecord> avgBarChartRecordIterator = chartRecords.iterator();
				while(avgBarChartRecordIterator.hasNext()){
					AvgBarChartRecord avgBarChartRecord = avgBarChartRecordIterator.next();
					if(avgBarChartRecord.getEN() > 0)find = true;
					temp[i] =  avgBarChartRecord.getEN()/1000;
					if(k == 0){
						columnKeys[i] = DateUtil.formatDate(avgBarChartRecord.getShowOn(),"yyyy-MM-dd");
					}
					i++;
				}
				k = 1;
				if(find){
					data.add(temp);
					j++;
					rowKeys.add(chartRecords.get(0).getName());
				}
			}
		}
		Comparable[] rowKeysArray = new Comparable[rowKeys.size()];
		for (int i = 0; i < rowKeys.size(); i++) {
			rowKeysArray[i] = rowKeys.get(i)+"";
		}
		double[][] dataArray = new double[data.size()][];
		
		for (int j = 0; j < data.size(); j++) {
			double[] object = (double[])data.get(j);
			dataArray[j] = object;
		}
		return DatasetUtilities.createCategoryDataset(rowKeysArray, columnKeys, dataArray);
    }

    @SuppressWarnings("deprecation")
	public static JFreeChart createChart(final CategoryDataset dataset) {

        final JFreeChart chart = ChartFactory.createStackedBarChart3D(
                "", "", "Energy used",
                dataset, PlotOrientation.VERTICAL, true, true, false);

        CategoryPlot plot = chart.getCategoryPlot();
        /*plot.getRenderer().setSeriesPaint(0, new Color(128, 0, 0));
        plot.getRenderer().setSeriesPaint(1, new Color(0, 0, 255));*/
        StackedBarRenderer3D  renderer = (StackedBarRenderer3D)plot.getRenderer();
        int len = dataset.getRowCount();
        for(int j=0;j<len;j++){
        	renderer.setSeriesPaint(j, ArgumentUtils.colors[j]);
        }
        renderer.setMaximumBarWidth(.09);
        renderer.setToolTipGenerator(new StandardCategoryToolTipGenerator("({0}, {1}) = {2} kW",NumberFormat.getInstance()));
        plot.setRenderer(renderer);
        CategoryAxis xAxis = (CategoryAxis)plot.getDomainAxis();
        xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        plot.getRangeAxis().setLabel(plot.getRangeAxis().getLabel()+ " (kW)");
        

        return chart;
    }

	
	@SuppressWarnings("deprecation")
	public static String generateStackedBarChart(HttpSession session, PrintWriter pw
												 ,String toDate,String fromDate,String toTime
												 ,String fromTime,String reportLocationId) {
		
		EnergyConsumptionManager energyConsumptionManager = (EnergyConsumptionManager)SpringContext.getBean("energyConsumptionManager");
		String[]arr=reportLocationId.split("-");
		String parentId=arr[0];
		String structureType=arr[1];
		Map<Integer, List<AvgBarChartRecord>> avgBarChartRecords = null;
		if("".equals(toTime) || toTime == null){
			toTime = "11:59 PM";
		}
		Date from = new Date(fromDate);
		Date to = new Date(toDate);
		long days = DateUtil.diffDayPeriods(from, to)+1;
		from = DateUtil.setTimeInDate(fromTime, from);
		to = DateUtil.setTimeInDate(toTime, to);
		int period = 1*60*24;
		period = calculatePeriod(days, period);
		days = calculateDay(days,period);
		if(structureType.equalsIgnoreCase("Company")){
			avgBarChartRecords = energyConsumptionManager.loadEnergyConsumptionStackedBarChart(to,new Integer(days+""), new Integer(period), "company",new Integer(parentId), from);
		}
		if(structureType.equalsIgnoreCase("Campus")){
			avgBarChartRecords = energyConsumptionManager.loadEnergyConsumptionStackedBarChart(to,new Integer(days+""), new Integer(period), "campus",new Integer(parentId), from);
		}
		if(structureType.equalsIgnoreCase("Building")){
			avgBarChartRecords = energyConsumptionManager.loadEnergyConsumptionStackedBarChart(to,new Integer(days+""), new Integer(period), "building",new Integer(parentId), from);
		}
		if(structureType.equalsIgnoreCase("Floor")){
			avgBarChartRecords = energyConsumptionManager.loadEnergyConsumptionStackedBarChart(to,new Integer(days+""), new Integer(period), "floor",new Integer(parentId), from);
		}
		if(structureType.equalsIgnoreCase("Area")){
			avgBarChartRecords = energyConsumptionManager.loadEnergyConsumptionStackedBarChart(to,new Integer(days+""), new Integer(period), "area",new Integer(parentId), from);
		}
		if(avgBarChartRecords == null)return "public_nodata_500x300.png";
		Collection<List<AvgBarChartRecord>> collection = avgBarChartRecords.values();
		if(collection == null)return "public_nodata_500x300.png";
		if(collection.size() == 0)return "public_nodata_500x300.png";
		String filename = null;
		try {
			JFreeChart chart = createChart(createDataset(avgBarChartRecords,to,from,days,period));
			ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
			filename = ServletUtilities.saveChartAsPNG(chart, 800, 400, info, session);

			ChartUtilities.writeImageMap(pw, filename, info,true);
			pw.flush();

		} catch (Exception e) {
			e.printStackTrace();
			filename = "public_nodata_500x300.png";
		}
		return filename;
	}

	private static long calculateDay(long days, int period) {
		if(days >= 15 && days <= 30){
			Double temp =Math.ceil(new Float(days)/2);
			days = temp.longValue();
		}
		
		if(days >= 31 && days <= 75){
			Double temp =Math.ceil(new Float(days)/5);
			days = temp.longValue();
		}
		
		if(days >= 76 && days <= 105){
			Double temp =Math.ceil(new Float(days)/7);
			days = temp.longValue();
		}
		
		if(days >= 106 && days <= 150){
			Double temp =Math.ceil(new Float(days)/10);
			days = temp.longValue();
		}
		
		if(days > 150){
			Double temp =Math.ceil(new Float(days)/30);
			days = temp.longValue();
		}
		return days;
	}
	
	private static int calculatePeriod(long days, int period) {
		if(days >= 15 && days <= 30){
			period = 2*60*24;
		}
		
		if(days >= 31 && days <= 75){
			period = 5*60*24;
		}
		
		if(days >= 76 && days <= 105){
			period = 7*60*24;
		}
		
		if(days >= 106 && days <= 150){
			period = 10*60*24;
		}
		
		if(days > 150){
			period = 30*60*24;
		}
		return period;
	}

	@SuppressWarnings("unchecked")
	public static String generatePieChart(HttpSession session, PrintWriter pw) {
		String filename = null;
		try {
			java.util.List list = (java.util.List)session.getAttribute("pieChartList");

			//  Throw a custom NoDataException if there is no data
			if(list == null)return "public_nodata_500x300.png";
			if (list.size() == 0) {
				System.out.println("No data has been found");
				throw new NoDataException();
			}

			//  Create and populate a PieDataSet
			DefaultPieDataset data = new DefaultPieDataset();
			for(int i=0;i<list.size();i++){
				Object[] val= (Object[])list.get(i);
				String x=String.valueOf(val[1]);
				BigDecimal y= (BigDecimal)val[2];
				data.setValue(x, y);
			}

			//  Create the chart object
			PiePlot3D plot = new PiePlot3D(data);
			//plot.setInsets(new RectangleInsets(0, 5, 5, 5));
			JFreeChart chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
			chart.setBackgroundPaint(java.awt.Color.white);
			plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
    		"{0} ({1} kW)"));
			plot.setToolTipGenerator(new StandardPieToolTipGenerator("{0}: ({1} kW, {2})")); 
			List <Comparable> keys = data.getKeys();
	        for (int i = 0; i < keys.size(); i++){
	        	if(i<ArgumentUtils.colors.length){
	        		plot.setSectionPaint(keys.get(i),ArgumentUtils.colors[i]);
	        	}
	        }

			//  Write the chart image to the temporary directory
			ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
			filename = ServletUtilities.saveChartAsPNG(chart, 500, 300, info, session);

			//  Write the image map to the PrintWriter
			ChartUtilities.writeImageMap(pw, filename, info,true);
			pw.flush();

		} catch (NoDataException e) {
			e.printStackTrace();
			filename = "public_nodata_500x300.png";
		} catch (Exception e) {
			e.printStackTrace();
			filename = "public_nodata_500x300.png";
		}
		return filename;
	}
	
	public static void main(String[] args) {
		long days = 11;
		Double ss = Math.ceil(new Float(days)/2);
		System.out.println(ss.intValue());
	}
}