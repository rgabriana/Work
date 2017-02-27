package com.ems.ws;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;

import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.BallastManager;
import com.ems.service.BulbManager;
import com.ems.service.FixtureClassManager;
import com.ems.service.FixtureManager;
import com.ems.service.LocatorDeviceManager;
import com.ems.service.PlugloadManager;

@Controller
@Path("/inventoryreport")
public class InventoryReportService {
	private static final Logger m_Logger = Logger.getLogger("WSLogger");

	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	@Resource
	BallastManager ballastManager;
	@Resource
	BulbManager bulbManager;
	@Resource(name = "fixtureManager")
	private FixtureManager fixtureManager;
	@Autowired
	private MessageSource messageSource;
    @Resource
    FixtureClassManager fixtureClassManager;
    @Resource
    LocatorDeviceManager locatorDeviceManager;
    @Resource
	PlugloadManager plugloadManager ;
	/**
	 * Export Inventory Report
	 * @return : Inventory Report in csv format
	 */
	@Path("getexportdata")
	@POST
	@Produces("application/csv")
	public Response exportInventoryReport() {

		StringBuffer output = new StringBuffer("");

		HashMap<String, Long> totalCommissionedSensors = fixtureManager.getFixturesCountByModelNo();
		HashMap<String, Long> totalOtherDevices = locatorDeviceManager.getOtherDevicesCount();
		Long mPlugloadListCount = plugloadManager.loadAllCommissionedPlugloadsCount();
	    List<Object[]> totalBallastAssociated = ballastManager.getBallastCountByBallastName();
	    List<Object[]> totalBulbsAssociated = bulbManager.getBulbsCountByBulbName();
	    List<Object[]> totalCommissionedCus = fixtureManager.getCusCountByVersionNo();
        List<Object[]> totalFxTypeAssociated = fixtureClassManager.getCommissionedFxTypeCount();    
	    Long totalCommissionedSensorsCount = totalCommissionedSensors.get("TotalCount");
	    Long totalOtherDeviceCount = totalOtherDevices.get("TotalCount");
        totalOtherDevices.remove("TotalCount");
        Long totalBallastAssociatedCount = (long) 0;
        Long totalLampsAssociatedCount = (long) 0;
        Long totalFxTypeAssociatedCount = (long) 0;
        Long totalCommissionedCuCount = (long) 0;
        Long totalBallastsCount = (long) 0;
        Long totalBulbsCount= (long) 0;
        totalOtherDevices.put("Plugload", mPlugloadListCount);
        
        totalCommissionedSensors.remove("TotalCount");
        
        //Calculate Total Cus Associated
        if (totalCommissionedCus != null && !totalCommissionedCus.isEmpty()) {
            Iterator<Object[]> iterator = totalCommissionedCus.iterator();
            while (iterator.hasNext()) {
                Object[] itrObject = (Object[]) iterator.next();
                Long count = ((BigInteger) itrObject[1]).longValue();
                totalCommissionedCuCount+= count;
            }
        }
        
        //Calculate Total Ballast Associated
        if (totalBallastAssociated != null && !totalBallastAssociated.isEmpty()) {
            Iterator<Object[]> iterator = totalBallastAssociated.iterator();
            while (iterator.hasNext()) {
                Object[] itrObject = (Object[]) iterator.next();
                Long count = (Long) itrObject[2];
                Long fxcount = (Long) itrObject[3];
                totalBallastsCount+= count;
                totalBallastAssociatedCount+= fxcount;
            }
        }
        
      //Calculate Total bulbs Associated
        if (totalBulbsAssociated != null && !totalBulbsAssociated.isEmpty()) {
            Iterator<Object[]> iterator1 = totalBulbsAssociated.iterator();
            while (iterator1.hasNext()) {
                Object[] itrObject1 = (Object[]) iterator1.next();
                Long count = ((BigInteger) itrObject1[2]).longValue();
                Long fxcount = ((BigInteger) itrObject1[3]).longValue();
                totalBulbsCount+= count;
                totalLampsAssociatedCount+= fxcount;
            }
        }
        
        //Calculate Total FxType Associated
        if (totalFxTypeAssociated != null && !totalFxTypeAssociated.isEmpty()) {
            Iterator<Object[]> iterator1 = totalFxTypeAssociated.iterator();
            while (iterator1.hasNext()) {
                Object[] itrObject1 = (Object[]) iterator1.next();
                Long count = ((BigInteger) itrObject1[0]).longValue();
                totalFxTypeAssociatedCount+= count;
            }
        }
        
	    output.append("Inventory Report\r\n\n");
	    
	    Iterator<String> iter = totalCommissionedSensors.keySet().iterator();
	    output.append("Sensors"
                + ","
                + "Count ("+ totalCommissionedSensorsCount+")");
	    while(iter.hasNext()) {
	        String key = (String)iter.next();
	        Long val = (Long)totalCommissionedSensors.get(key);
	        output.append("\r\n");
            output.append((String) key
                    + ","
                    + (val)
                    );
	    }
	    
	    output.append("\r\n");
        output.append("\r\n");
        
        Iterator<String> iter1 = totalOtherDevices.keySet().iterator();
        output.append("Other Devices"
                + ","
                + "Count ("+ totalOtherDeviceCount+mPlugloadListCount+")");
        while(iter1.hasNext()) {
            String key = (String)iter1.next();
            Long val = (Long)totalOtherDevices.get(key);
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
                + "Count ("+ totalCommissionedCuCount+")");

        for (int i = 0; i < totalCommissionedCus.size(); i++) {
            Object[] each = (Object[]) totalCommissionedCus.get(i);
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
                + "Count of Ballasts("+ totalBallastsCount+")"
                + ","
                + "Count of Fixture("+ totalBallastAssociatedCount+")");

        for (int i = 0; i < totalBallastAssociated.size(); i++) {
            Object[] each = (Object[]) totalBallastAssociated.get(i);
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
                + "Count of Lamps("+ totalBulbsCount+")"
                + ","
                + "Count of Fixture("+ totalLampsAssociatedCount+")");
	    
	    for (int i = 0; i < totalBulbsAssociated.size(); i++) {
            Object[] each1 = (Object[]) totalBulbsAssociated.get(i);
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
                + "Count of Fixture("+ totalFxTypeAssociatedCount+")");
        
        for (int i = 0; i < totalFxTypeAssociated.size(); i++) {
            Object[] each1 = (Object[]) totalFxTypeAssociated.get(i);
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
		return Response
				.ok(output.toString(), "text/csv")
				.header("Content-Disposition",
						"attachment;filename=Inventory_Report.csv")
				.build();
	}
}
