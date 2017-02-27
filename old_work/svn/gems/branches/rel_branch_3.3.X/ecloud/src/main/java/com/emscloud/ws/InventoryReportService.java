package com.emscloud.ws;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Resource;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import com.emscloud.model.Customer;
import com.emscloud.model.EmInstance;
import com.emscloud.model.Site;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.InventoryReportManager;
import com.emscloud.service.SiteManager;
import com.emscloud.util.IRUtil;
import com.emscloud.vo.AggregatedSiteReport;
import com.emscloud.vo.SiteReportVo;


@Controller
@Path("/inventoryreport")
public class InventoryReportService {
	private static final Logger m_Logger = Logger.getLogger(InventoryReportService.class.getName());

	@Resource
	SiteManager siteManager;

	@Resource
	InventoryReportManager inventoryReportManager;
	
	@Resource
	CustomerManager customerManager;
	
	@Resource
	EmInstanceManager emInstanceManager;
	/**
	 * Export Inventory Report
	 * @return : Inventory Report in csv format
	 */
	@Path("exportdetailreport")
	@POST
	@Produces("application/csv")
	public Response exportdetailreport(@FormParam("siteId") Long siteId) {

	    StringBuffer output = new StringBuffer("");
	    IRUtil.resetData();
        Site site = siteManager.loadSiteById(siteId);

        //get all the em instances of the site
        List<Long> emIdList = siteManager.getSiteEms(site.getId());
        Iterator<Long> emIdIter = emIdList.iterator();
        
        while(emIdIter.hasNext()) {
            EmInstance emInst = emInstanceManager.loadEmInstanceById(emIdIter.next());
            String dbName = emInst.getDatabaseName();
            String replicaServerHost = emInst.getReplicaServer().getInternalIp();
            IRUtil.aggregateSiteFixtureData(inventoryReportManager.getFixturesCountByModelNo(dbName,replicaServerHost));
            IRUtil.aggregateSiteOtherDeviceData(inventoryReportManager.getOtherDevicesCount(dbName,replicaServerHost));
            IRUtil.aggregateErcData(inventoryReportManager.getErcCountByVersionNo(dbName,replicaServerHost));
            IRUtil.aggregateCuData(inventoryReportManager.getCusCountByVersionNo(dbName,replicaServerHost));
            IRUtil.aggregateBallastData(inventoryReportManager.getBallastCountByBallastName(dbName,replicaServerHost));
            IRUtil.aggregateBulbData(inventoryReportManager.getBulbsCountByBulbName(dbName, replicaServerHost));
            IRUtil.aggregateFixtureTypeData(inventoryReportManager.getCommissionedFxTypeCount(dbName, replicaServerHost));
        }
        
        Long totalCommissionedSensorsCount = IRUtil.totalCommissionedSensors.get("TotalCount");
        
        IRUtil.totalCommissionedSensors.remove("TotalCount");
        Long totalOtherDeviceCount = IRUtil.totalOtherDevices.get("TotalCount");
        IRUtil.totalOtherDevices.remove("TotalCount");
        IRUtil.calculateAggregateSummaryCount();
        
        output.append(site.getName() + " Inventory Report\r\n\n");
        
        Iterator<String> iter = IRUtil.totalCommissionedSensors.keySet().iterator();
        output.append("Sensors"
                + ","
                + "Count ("+ totalCommissionedSensorsCount+")");
        while(iter.hasNext()) {
            String key = (String)iter.next();
            Long val = (Long)IRUtil.totalCommissionedSensors.get(key);
            output.append("\r\n");
            output.append((String) key
                    + ","
                    + (val)
                    );
        }
        
        
        output.append("\r\n");
        output.append("\r\n");
        
        output.append("ERC"
                + ","
                + "Count ("+ IRUtil.totalCommissionedErcCount+")");

        for (int i = 0; i < IRUtil.totalCommissionedErc.size(); i++) {
            Object[] each = (Object[]) IRUtil.totalCommissionedErc.get(i);
            output.append("\r\n");
            output.append("ERC (Version - "+(String) each[0]+")"
                    + ","
                    + each[1]
                   );
        }
        
        output.append("\r\n");
        output.append("\r\n");
        
        Iterator<String> iter1 = IRUtil.totalOtherDevices.keySet().iterator();
        output.append("Other Devices"
                + ","
                + "Count ("+ totalOtherDeviceCount+")");
        while(iter1.hasNext()) {
            String key = (String)iter1.next();
            Long val = (Long)IRUtil.totalOtherDevices.get(key);
            output.append("\r\n");
            output.append((String) key
                    + ","
                    + (val)
                    );
        }
        

        output.append("\r\n");
        output.append("\r\n");
        
       
        
        output.append("CU"
                + ","
                + "Count ("+ IRUtil.totalCommissionedCuCount+")");

        for (int i = 0; i < IRUtil.totalCommissionedCus.size(); i++) {
            Object[] each = (Object[]) IRUtil.totalCommissionedCus.get(i);
            output.append("\r\n");
            output.append("CU v."+(String) each[0]
                    + ","
                    + each[1]
                   );
        }
        
        output.append("\r\n");
        output.append("\r\n");
        output.append("\r\n");
        output.append("Ballasts/drivers"
                + ","
                + "Manufacturer"
                + ","
                + "Baseline Load"
                + ","
                + "Count of Ballasts("+ IRUtil.totalBallastsCount+")"
                + ","
                + "Count of Fixture("+ IRUtil.totalBallastAssociatedCount+")");

        for (int i = 0; i < IRUtil.totalBallastAssociated.size(); i++) {
            Object[] each = (Object[]) IRUtil.totalBallastAssociated.get(i);
            output.append("\r\n");
            output.append((String) each[0]
                    + ","
                    + each[1]
                    + ","
                    + each[4]
                    + ","
                    + each[2]
                    + ","
                    + each[3]
                   );
        }
        output.append("\r\n");
        output.append("\r\n");
        output.append("\r\n");
        output.append("Lamps By Type"
                + ","
                + "Manufacturer"
                + ","
                + "Count of Lamps("+ IRUtil.totalBulbsCount+")"
                + ","
                + "Count of Fixture("+ IRUtil.totalLampsAssociatedCount+")");
        
        for (int i = 0; i < IRUtil.totalBulbsAssociated.size(); i++) {
            Object[] each1 = (Object[]) IRUtil.totalBulbsAssociated.get(i);
            output.append("\r\n");
            output.append((String) each1[0]
                    + ","
                    + each1[1]
                    + ","
                     + each1[2]
                    + ","
                    + each1[3]
                   );
        }
        
        output.append("\r\n");
        output.append("\r\n");
        output.append("\r\n");
        output.append("Fixture Type Name"
                + ","
                + "Ballast Display Label"
                + ","
                + "Count of Fixture("+ IRUtil.totalFxTypeAssociatedCount+")");
        
        for (int i = 0; i < IRUtil.totalFxTypeAssociated.size(); i++) {
            Object[] each1 = (Object[]) IRUtil.totalFxTypeAssociated.get(i);
            output.append("\r\n");
            String displayLabel = (String) each1[3];
            displayLabel = displayLabel.replaceAll(",", " ");
            output.append((String) each1[2]
                    + ","
                    + displayLabel
                    + ","
                     + each1[0]
                   );
        }
        
        String siteName = site.getName().replaceAll(" ", "_");
        String outPutFileName = siteName+"_Inventory_Report.csv";
        return Response
                .ok(output.toString(), "text/csv")
                .header("Content-Disposition",
                        "attachment;filename="+outPutFileName)
                .build();
    }
    
	/**
     * Export High Level Inventory Report
     * @return : Inventory Report in csv format
	 * @throws UnsupportedEncodingException 
     */
    @Path("getexportaggregateddata")
    @POST
    @Produces("application/csv")
    public Response exportHighLevelInventoryReport(@FormParam("customerId") Long customerId) throws UnsupportedEncodingException {

        StringBuffer output = new StringBuffer("");
       
        AggregatedSiteReport aggregatedSiteReport = inventoryReportManager.loadSiteReportListByCustomerId(customerId,null);
        Customer customer = customerManager.loadCustomerById(customerId);
        String customerName = customer.getName().replaceAll(" ", "_");
        String outPutFileName = customerName+"_Inventory_Report.csv";
        output.append(customerName+" Inventory Report\r\n\n");
        Long totalFixtureCount = (long) 0;
        Long totalSesnorCount = (long) 0;
        Long totalGatewayCount = (long) 0;
        Long totalBallastCount = (long) 0;
        Long totalBulbsCount = (long) 0;
        output.append("Site Name"
                + ","
                + "Fixture Count" 
                + ","
                + "Sensor Count"
                + ","
                + "Gateway Count"
                + ","
                + "Ballast Count"
                + ","
                + "Bulb Count"
                );

        if(aggregatedSiteReport!=null && aggregatedSiteReport.getSiteReport()!=null)
        {
            for (int i = 0; i < aggregatedSiteReport.getSiteReport().size(); i++) {
                SiteReportVo each = aggregatedSiteReport.getSiteReport().get(i);
                output.append("\r\n");
                totalFixtureCount+=each.getFixtureCount();
                totalSesnorCount+=each.getSensorCount();
                totalGatewayCount+=each.getGatewayCount();
                totalBallastCount+=each.getBallastCount();
                totalBulbsCount+=each.getLampsCount();
                
                output.append((String) each.getName()
                        + ","
                        + each.getFixtureCount()
                        + ","
                        + each.getSensorCount()
                        + ","
                        + each.getGatewayCount()
                        + ","
                        + each.getBallastCount()
                        + ","
                        + each.getLampsCount()
                       );
            }
        }
        output.append("\r\n");
        output.append("Grand Total"
                + ","
                + totalFixtureCount
                + ","
                + totalSesnorCount
                + ","
                + totalGatewayCount
                + ","
                + totalBallastCount
                + ","
                + totalBulbsCount
               );
        return Response
                .ok(output.toString(), "text/csv")
                .header("Content-Disposition",
                        "attachment;filename="+outPutFileName)
                .build();
    }
    @Path("loadsitelistbycustomerid/{custId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public AggregatedSiteReport loadSiteReportByCustomerId(@PathParam("custId") Long custId,
            @RequestParam("data") String userdata) throws UnsupportedEncodingException,ParseException {
        AggregatedSiteReport aggregatedSiteReport = inventoryReportManager.loadSiteReportListByCustomerId(custId,userdata);
        return aggregatedSiteReport;
    }
    
	/**
	 * Export Inventory Report
	 * @return : Inventory Report in csv format
	 */
	@Path("exportreportv1")
	@POST
	@Produces("application/zip")
	public Response exportreport(@FormParam("customerId") Long customerId) throws UnsupportedEncodingException {

		StringBuffer output = new StringBuffer("");

		AggregatedSiteReport aggregatedSiteReport = inventoryReportManager
				.loadSiteReportListByCustomerId(customerId, null);
		Customer customer = customerManager.loadCustomerById(customerId);
		String customerName = customer.getName().replaceAll(" ", "_");
		//String outPutFileName = customerName + "_Inventory_Report.zip";
		Long totalFixtureCount = (long) 0;
		Long totalSesnorCount = (long) 0;
		Long totalCuCount = (long) 0;
		Long totalGatewayCount = (long) 0;
		Long totalBallastCount = (long) 0;
		Long totalBulbsCount = (long) 0;
		Long totalFxTypeCount = (long) 0;
		output.append(customerName + " Inventory Report\r\n\n");
		output.append("Site Name" + "," + "Fixture Count" + ","
				+ "Sensor Count" + "," + "Gateway Count" + ","
				+ "Ballast Count" + "," + "Bulb Count");
		output.append("\r\n");
		
		String reportDirname = "/tmp/" + customer.getId() + "_" + System.currentTimeMillis();
		File oDir = new File(reportDirname);
		oDir.mkdirs();

		if (aggregatedSiteReport != null
				&& aggregatedSiteReport.getSiteReport() != null) {
			for (int i = 0; i < aggregatedSiteReport.getSiteReport().size(); i++) {
				StringBuffer strinventorydetails = new StringBuffer("");
				StringBuffer strsensors = new StringBuffer("");
				StringBuffer strcus = new StringBuffer("");
				StringBuffer strballast = new StringBuffer("");
				StringBuffer strbulb = new StringBuffer("");
				StringBuffer strfxtypes = new StringBuffer("");

				SiteReportVo each = aggregatedSiteReport.getSiteReport().get(i);
				totalFixtureCount += each.getFixtureCount();
				totalSesnorCount += each.getSensorCount();
				totalCuCount += each.getCuCount();
				totalGatewayCount += each.getGatewayCount();
				totalBallastCount += each.getBallastCount();
				totalBulbsCount += each.getLampsCount();
				totalFxTypeCount += each.getFxTypeCount();

				output.append((String) each.getName() + ","
						+ each.getFixtureCount() + "," + each.getSensorCount()
						+ "," + each.getGatewayCount() + ","
						+ each.getBallastCount() + "," + each.getLampsCount());
				output.append("\r\n");
				// Details report for the site
				Site site = siteManager.loadSiteById(each.getId());
				// get all the em instances of the site
				List<Long> emIdList = siteManager.getSiteEms(site.getId());
				Iterator<Long> emIdIter = emIdList.iterator();

				strsensors.append("\r\n").append("\r\n").append("EM" + "," + "Address" + ","
						+ "Sensor by Type" + "," + "Count ("
						+ each.getSensorCount() + ")");
				strcus.append("\r\n").append("\r\n").append("EM" + "," + "Address" + "," + "CU by Type" + ","
						+ "Count (" + each.getCuCount() + ")");
				strballast.append("\r\n").append("\r\n").append("EM" + "," + "Address" + ","
						+ "Ballasts/drivers" + "," + "Manufacturer" + ","
						+ "Count of Ballasts (" + each.getBallastCount()
						+ ")" + "," + "Count of Fixtures ("
						+ each.getSensorCount() + ")");
				strbulb.append("\r\n").append("\r\n").append("EM" + "," + "Address" + "," + "Lamps By Type"
						+ "," + "Manufacturer" + "," + "Count of Lamps ("
						+ each.getLampsCount() + ")" + ","
						+ "Count of Fixtures ("
						+ each.getSensorCount() + ")");
				strfxtypes.append("\r\n").append("\r\n").append("EM" + "," + "Address" + ","
						+ "Fixture Type Name" + "," + "Ballast Display Label"
						+ "," + "Count of Fixtures ("
						+ each.getFxTypeCount() + ")");

				while (emIdIter.hasNext()) {
				    IRUtil.resetData();
					EmInstance emInst = emInstanceManager
							.loadEmInstanceById(emIdIter.next());
					String dbName = emInst.getDatabaseName();
					String replicaServerHost = emInst.getReplicaServer()
							.getInternalIp();
					IRUtil.aggregateSiteFixtureData(inventoryReportManager
							.getFixturesCountByModelNo(dbName,
									replicaServerHost));
					IRUtil.aggregateCuData(inventoryReportManager
							.getCusCountByVersionNo(dbName, replicaServerHost));
					IRUtil.aggregateBallastData(inventoryReportManager
							.getBallastCountByBallastName(dbName,
									replicaServerHost));
					IRUtil.aggregateBulbData(inventoryReportManager
							.getBulbsCountByBulbName(dbName, replicaServerHost));
					IRUtil.aggregateFixtureTypeData(inventoryReportManager
							.getCommissionedFxTypeCount(dbName,
									replicaServerHost));
					IRUtil.totalCommissionedSensors.remove("TotalCount");
					IRUtil.calculateAggregateSummaryCount();

					// SU's
					Iterator<String> iter = IRUtil.totalCommissionedSensors
							.keySet().iterator();
					while (iter.hasNext()) {
						String key = (String) iter.next();
						Long val = (Long) IRUtil.totalCommissionedSensors
								.get(key);
						strsensors.append("\r\n");
						strsensors.append(emInst.getDatabaseName() + ",\"" + emInst.getAddress() + "\"," + (String) key + "," + (val));
					}

					// Cu's
					for (int i1 = 0; i1 < IRUtil.totalCommissionedCus.size(); i1++) {
						Object[] each1 = (Object[]) IRUtil.totalCommissionedCus
								.get(i1);
						strcus.append("\r\n");
						strcus.append(emInst.getDatabaseName() + ",\"" + emInst.getAddress() + "\","  + "CU v." + (String) each1[0] + ","
								+ each1[1]);
					}

					// Ballats
					for (int i2 = 0; i2 < IRUtil.totalBallastAssociated.size(); i2++) {
						Object[] each2 = (Object[]) IRUtil.totalBallastAssociated
								.get(i2);
						strballast.append("\r\n");
						strballast.append(emInst.getDatabaseName() + ",\"" + emInst.getAddress() + "\","  + (String) each2[0] + "," + each2[1] + ","
								+ each2[2] + "," + each2[3]);
					}

					// Bulbs
					for (int i3 = 0; i3 < IRUtil.totalBulbsAssociated.size(); i3++) {
						Object[] each3 = (Object[]) IRUtil.totalBulbsAssociated
								.get(i3);
						strbulb.append("\r\n");
						strbulb.append(emInst.getDatabaseName() + ",\"" + emInst.getAddress() + "\","  + (String) each3[0] + "," + each3[1] + ","
								+ each3[2] + "," + each3[3]);
					}

					// FxType
					for (int i4 = 0; i4 < IRUtil.totalFxTypeAssociated.size(); i4++) {
						Object[] each4 = (Object[]) IRUtil.totalFxTypeAssociated
								.get(i4);
						strfxtypes.append("\r\n");
						String displayLabel = (String) each4[3];
						displayLabel = displayLabel.replaceAll(",", " ");
						strfxtypes.append(emInst.getDatabaseName() + ",\"" + emInst.getAddress() + "\","  + (String) each4[2] + "," + displayLabel
								+ "," + each4[0]);
					}
				}
				strinventorydetails.append(strsensors).append(strcus).append(strballast).append(strbulb).append(strfxtypes);
				writeReportFile(each.getName(), "", reportDirname + File.separator + each.getName() + "_inventory_details.csv", strinventorydetails.toString());
//				writeReportFile(each.getName(), "", reportDirname + File.separator + each.getName() + "_cu_details.csv", strcus.toString());
//				writeReportFile(each.getName(), "", reportDirname + File.separator + each.getName() + "_ballast_details.csv", strballast.toString());
//				writeReportFile(each.getName(), "", reportDirname + File.separator + each.getName() + "_bulbs_details.csv", strbulb.toString());
//				writeReportFile(each.getName(), "", reportDirname + File.separator + each.getName() + "_fxtype_details.csv", strfxtypes.toString());
			}
        }
		output.append("Grand Total" + "," + totalFixtureCount + ","
				+ totalSesnorCount + "," + totalGatewayCount + ","
				+ totalBallastCount + "," + totalBulbsCount);
		
		writeReportFile(customerName, "", reportDirname + File.separator + customerName + "_Inventory_Report.csv", output.toString());
		String strRptName = customerName + "_Inventory_Report" + ".zip";
		File oRptFile = new File("/tmp/" + strRptName);
		writeZipFile(oDir, strRptName);
		oDir.delete();
		
		return Response
	            .ok(oRptFile, "application/zip")
	            .header("Content-Disposition",
	                    "attachment; filename =" + strRptName).build();	
	}
	
	/**
	 * Export Inventory Report
	 * @return : Inventory Report in csv format
	 */
	@Path("exportreport")
	@POST
    @Produces("application/csv")
	public Response exportreportv1(@FormParam("customerId") Long customerId)
			throws UnsupportedEncodingException {

		StringBuffer output = new StringBuffer("");

		AggregatedSiteReport aggregatedSiteReport = inventoryReportManager
				.loadSiteReportListByCustomerId(customerId, null);
		Customer customer = customerManager.loadCustomerById(customerId);
		String outPutFileName = customer.getName() + "_Inventory_Report.csv";
		List<String> genheaderList = new ArrayList<String>();
		List<String> suheaderList = new ArrayList<String>();
		List<String> cuheaderList = new ArrayList<String>();
		List<String> ercheaderList = new ArrayList<String>();
		List<String> gwheaderList = new ArrayList<String>();
		List<String> ballastheaderList = new ArrayList<String>();
		List<String> lampsheaderList = new ArrayList<String>();

		if (aggregatedSiteReport != null
				&& aggregatedSiteReport.getSiteReport() != null) {
			for (int i = 0; i < aggregatedSiteReport.getSiteReport().size(); i++) {
				SiteReportVo each = aggregatedSiteReport.getSiteReport().get(i);
				Site site = siteManager.loadSiteById(each.getId());
				// get all the em instances of the site
				List<Long> emIdList = siteManager.getSiteEms(site.getId());
				Iterator<Long> emIdIter = emIdList.iterator();
				Map<String, Long> sensorsCountsMap = new HashMap<String, Long>();
				Map<String, Long> cuCountsMap = new HashMap<String, Long>();
				Map<String, Long> ercCountsMap = new HashMap<String, Long>();
				Map<String, Long> gwCountsMap = new HashMap<String, Long>();
				Map<String, Long> ballastsCountsMap = new HashMap<String, Long>();
				Map<String, Long> bulbsCountsMap = new HashMap<String, Long>();
				while (emIdIter.hasNext()) {
					EmInstance emInst = emInstanceManager
							.loadEmInstanceById(emIdIter.next());
					if (!(emInst.getSppaEnabled() && emInst.getReplicaServer() != null)) {
						m_Logger.warn(emInst.getName() + " is not sppa enabled. Ignoring in the report.");
                    	continue;
                    }
					String dbName = emInst.getDatabaseName();
					String replicaServerHost = emInst.getReplicaServer()
							.getInternalIp();
					inventoryReportManager.getFixturesCountByModelNo(dbName,
							replicaServerHost, sensorsCountsMap);
					inventoryReportManager.getCuCountByVersionNo(dbName,
							replicaServerHost, cuCountsMap);
					inventoryReportManager.getErcCountByVersionNo(dbName,
							replicaServerHost, ercCountsMap);
					inventoryReportManager.getGatewayCount(dbName,
							replicaServerHost, gwCountsMap);
					inventoryReportManager.getBallastCountByType(dbName,
							replicaServerHost, ballastsCountsMap);
					inventoryReportManager.getBulbsCountByType(dbName,
							replicaServerHost, bulbsCountsMap);
				}
				addHeader("Site Name", genheaderList);
				addHeader("Geo Loc", genheaderList);
				addHeader("EM Count", genheaderList);
				Long total = 0L;
				Iterator<String> oItr = sensorsCountsMap.keySet().iterator();
				while (oItr.hasNext()) {
					String key = oItr.next();
					if (key == null)
						continue;
					addHeader(key, suheaderList);
					Long value = (Long) sensorsCountsMap.get(key);
					total += value;
				}
				// CU
				total = 0L;
				oItr = cuCountsMap.keySet().iterator();
				while (oItr.hasNext()) {
					String key = oItr.next();
					if (key == null)
						continue;
					addHeader(key, cuheaderList);
					Long value = (Long) cuCountsMap.get(key);
					total += value;
				}
				// ERC
				total = 0L;
				oItr = ercCountsMap.keySet().iterator();
				while (oItr.hasNext()) {
					String key = oItr.next();
					if (key == null)
						continue;
					addHeader(key, ercheaderList);
					Long value = (Long) ercCountsMap.get(key);
					total += value;
				}
				// GW
				total = 0L;
				oItr = gwCountsMap.keySet().iterator();
				addHeader("Gateway Count", gwheaderList);
				while (oItr.hasNext()) {
					String key = oItr.next();
					if (key == null)
						continue;
					Long value = (Long) gwCountsMap.get(key);
					total += value;
				}
				// Ballast
				total = 0L;
				oItr = ballastsCountsMap.keySet().iterator();
				while (oItr.hasNext()) {
					String key = oItr.next();
					if (key == null)
						continue;
					addHeader(key, ballastheaderList);
					Long value = (Long) ballastsCountsMap.get(key);
					total += value;
				}
				// Bulbs
				total = 0L;
				oItr = bulbsCountsMap.keySet().iterator();
				while (oItr.hasNext()) {
					String key = oItr.next();
					if (key == null)
						continue;
					addHeader(key, lampsheaderList);
					Long value = (Long) bulbsCountsMap.get(key);
					total += value;
				}
			}
			addHeader("SU Count", suheaderList);
			addHeader("CU Count", cuheaderList);
			addHeader("ERC Count", ercheaderList);
			addHeader("Ballast Count", ballastheaderList);
			addHeader("Lamps Count", lampsheaderList);

			genheaderList.addAll(suheaderList);
			genheaderList.add("Emergency Fixture Count");
			genheaderList.add("Fixture Count");
			genheaderList.addAll(cuheaderList);
			genheaderList.addAll(ercheaderList);
			genheaderList.addAll(gwheaderList);
			genheaderList.addAll(ballastheaderList);
			genheaderList.addAll(lampsheaderList);

			// iteration 2. Needs optimization
			StringBuffer sitedetails = new StringBuffer("");
			for (int i = 0; i < aggregatedSiteReport.getSiteReport().size(); i++) {
				SiteReportVo each = aggregatedSiteReport.getSiteReport().get(i);
				Site site = siteManager.loadSiteById(each.getId());
				// get all the em instances of the site
				List<Long> emIdList = siteManager.getSiteEms(site.getId());
				Iterator<Long> emIdIter = emIdList.iterator();
				Map<String, Long> sensorsCountsMap = new HashMap<String, Long>();
				Map<String, Long> cuCountsMap = new HashMap<String, Long>();
				Map<String, Long> ercCountsMap = new HashMap<String, Long>();
				Map<String, Long> gwCountsMap = new HashMap<String, Long>();
				Map<String, Long> ballastsCountsMap = new HashMap<String, Long>();
				Map<String, Long> bulbsCountsMap = new HashMap<String, Long>();
				int iEMCount = 0;
				int iEmergencyFxCount = 0;
				while (emIdIter.hasNext()) {
					EmInstance emInst = emInstanceManager
							.loadEmInstanceById(emIdIter.next());
					if (!(emInst.getSppaEnabled() && emInst.getReplicaServer() != null)) {
						m_Logger.warn(emInst.getName() + " is not sppa enabled. Ignoring in the report.");
                    	continue;
                    }
					String dbName = emInst.getDatabaseName();
					String replicaServerHost = emInst.getReplicaServer()
							.getInternalIp();
					inventoryReportManager.getFixturesCountByModelNo(dbName,
							replicaServerHost, sensorsCountsMap);
					inventoryReportManager.getCuCountByVersionNo(dbName,
							replicaServerHost, cuCountsMap);
					inventoryReportManager.getErcCountByVersionNo(dbName,
							replicaServerHost, ercCountsMap);
					inventoryReportManager.getGatewayCount(dbName,
							replicaServerHost, gwCountsMap);
					inventoryReportManager.getBallastCountByType(dbName,
							replicaServerHost, ballastsCountsMap);
					inventoryReportManager.getBulbsCountByType(dbName,
							replicaServerHost, bulbsCountsMap);
					iEMCount++;
					if (emInst.getNoOfEmergencyFixtures() != null)
						iEmergencyFxCount += emInst.getNoOfEmergencyFixtures();
				}
				sitedetails.append("\r\n").append("\"" + site.getName() + "\"").append(",")
						.append(site.getGeoLocation()).append(",").append(iEMCount);
				Long total = 0L;
				int hi = 0;
				Iterator<String> oItr = null;
				Long gwCount = 0L, fxCount = 0L;
				total = 0L;

				// Get the fixture and the gateway counts
				oItr = gwCountsMap.keySet().iterator();
				while (oItr.hasNext()) {
					String key = oItr.next();
					if (key == null)
						continue;

					Long value = (Long) gwCountsMap.get(key);
					if (key.equals("GW")) {
						gwCount = value;
					}else if (key.equals("FX")) {
						fxCount = value;
					}
				}

				// SU count
				total = 0L;
				for (hi = 0; hi < suheaderList.size()-1; hi++) {
					String sKey = suheaderList.get(hi);
					oItr = sensorsCountsMap.keySet().iterator();
					Long value = 0L;
					while (oItr.hasNext()) {
						value = 0L;
						String key = oItr.next();
						if (key == null)
							continue;

						if (sKey.equals(key)) {
							value = (Long) sensorsCountsMap.get(key);
							total += value;
							break;
						}
					}
					sitedetails.append(",\"").append(value).append("\"");
				}
				sitedetails.append(",\"").append(total).append("\"");
				sitedetails.append(",\"").append(iEmergencyFxCount).append("\"");
				sitedetails.append(",\"").append(fxCount).append("\"");

				total = 0L;
				for (hi = 0; hi < cuheaderList.size()-1; hi++) {
					String sKey = cuheaderList.get(hi);
					oItr = cuCountsMap.keySet().iterator();
					Long value = 0L;
					while (oItr.hasNext()) {
						value = 0L;
						String key = oItr.next();
						if (key == null)
							continue;

						if (sKey.equals(key)) {
							value = (Long) cuCountsMap.get(key);
							total += value;
							break;
						}
					}
					sitedetails.append(",\"").append(value).append("\"");
				}
				sitedetails.append(",\"").append(total).append("\"");
				
				total = 0L;
				for (hi = 0; hi < ercheaderList.size()-1; hi++) {
					String sKey = ercheaderList.get(hi);
					oItr = ercCountsMap.keySet().iterator();
					Long value = 0L;
					while (oItr.hasNext()) {
						value = 0L;
						String key = oItr.next();
						if (key == null)
							continue;

						if (sKey.equals(key)) {
							value = (Long) ercCountsMap.get(key);
							total += value;
							break;
						}
					}
					sitedetails.append(",\"").append(value).append("\"");
				}
				sitedetails.append(",\"").append(total).append("\"");
				
				// Add gateway count here
				sitedetails.append(",\"").append(gwCount).append("\"");

				total = 0L;
				for (hi = 0; hi < ballastheaderList.size()-1; hi++) {
					String sKey = ballastheaderList.get(hi);
					oItr = ballastsCountsMap.keySet().iterator();
					Long value = 0L;
					while (oItr.hasNext()) {
						String key = oItr.next();
						if (key == null)
							continue;

						value = 0L;
						if (sKey.equals(key)) {
							value = (Long) ballastsCountsMap.get(key);
							total += value;
							break;
						}
					}
					sitedetails.append(",\"").append(value).append("\"");
				}
				sitedetails.append(",\"").append(total).append("\"");

				total = 0L;
				for (hi = 0; hi < lampsheaderList.size()-1; hi++) {
					String sKey = lampsheaderList.get(hi);
					oItr = bulbsCountsMap.keySet().iterator();
					Long value = 0L;
					while (oItr.hasNext()) {
						String key = oItr.next();
						if (key == null)
							continue;

						value = 0L;
						if (sKey.equals(key)) {
							value = (Long) bulbsCountsMap.get(key);
							total += value;
							break;
						}
					}
					sitedetails.append(",\"").append(value).append("\"");
				}
				sitedetails.append(",\"").append(total).append("\"");
			}
//			printHeaderList(genheaderList);
			output.append(getHeader(genheaderList)).append("\r\n").append(sitedetails.toString());
		}
		outPutFileName = outPutFileName.replaceAll(" ", "_");
		return Response
				.ok(output.toString(), "text/csv")
				.header("Content-Disposition",
						"attachment;filename=" + outPutFileName).build();
	}
	
	private void addHeader(String key, List<String> strList) {
		if (!strList.contains(key)) {
			strList.add(key);
		}
	}
	
	private String getHeader(List<String> strList) {
		StringBuffer header = new StringBuffer();
		for (int count = 0; count < strList.size(); count++) {
			if (count != 0) {
				header.append(", ");
			}
			header.append("\"" + strList.get(count) + "\"");
		}
		return header.toString();
	}
	
	public void writeReportFile(String site, String em, String reportName, String data) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(reportName)));
			bw.write(data);
			bw.close();
		} catch (IOException e) {
			m_Logger.error(e.getMessage(), e);
		}
	}
	
	private void writeZipFile(File directoryToZip, String sRptName) {

		try {
			FileOutputStream fos = new FileOutputStream("/tmp/" + sRptName);
			ZipOutputStream zos = new ZipOutputStream(fos);

			File[] files = directoryToZip.listFiles();
			for (File file : files) {
				if (!file.isDirectory()) {
					addToZip(directoryToZip, file, zos);
				}
			}
			zos.close();
			fos.close();
		} catch (FileNotFoundException e) {
			m_Logger.error(e.getMessage(), e);
		} catch (IOException e) {
			m_Logger.error(e.getMessage(), e);
		}
	}

	private void addToZip(File directoryToZip, File file,
			ZipOutputStream zos) throws FileNotFoundException, IOException {

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
	
			// we want the zipEntry's path to be a relative path that is relative
			// to the directory being zipped, so chop off the rest of the path
			String zipFilePath = file.getCanonicalPath().substring(
					directoryToZip.getCanonicalPath().length() + 1,
					file.getCanonicalPath().length());
	//		System.out.println("Writing '" + zipFilePath + "' to zip file");
			ZipEntry zipEntry = new ZipEntry(zipFilePath);
			zos.putNextEntry(zipEntry);
	
			byte[] bytes = new byte[1024];
			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zos.write(bytes, 0, length);
			}
			zos.closeEntry();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			IOUtils.closeQuietly(zos);
			IOUtils.closeQuietly(fis);
		}
		file.delete();

		
	}
}
