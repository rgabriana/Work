package com.emscloud.mvc.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.emscloud.model.Customer;
import com.emscloud.model.EmInstance;
import com.emscloud.model.Site;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.InventoryReportManager;
import com.emscloud.service.SiteManager;
import com.emscloud.util.IRUtil;

@Controller
@RequestMapping("/reports")
public class InventoryReportsController {
    @Resource
    CustomerManager customerManager;
    @Resource
    SiteManager siteManager;
    @Resource
    EmInstanceManager emInstanceManager;
    @Resource
    InventoryReportManager inventoryReportManager;
    
    @RequestMapping("/inventoryDetail.ems")
    public String loadInventoryReportDetail(Model model, @RequestParam("siteId") long siteId){
      
        IRUtil.resetData();
        Site site = siteManager.loadSiteById(siteId);
	    //get all the em instances of the site
        List<Long> emIdList = siteManager.getSiteEms(site.getId());
        Iterator<Long> emIdIter = emIdList.iterator();
        try
        {
            while(emIdIter.hasNext()) {
                EmInstance emInst = emInstanceManager.loadEmInstanceById(emIdIter.next());
                String dbName = emInst.getDatabaseName();
                String replicaServerHost = emInst.getReplicaServer().getInternalIp();
                IRUtil.aggregateSiteFixtureData(inventoryReportManager.getFixturesCountByModelNo(dbName,replicaServerHost));
                IRUtil.aggregateSiteOtherDeviceData(inventoryReportManager.getOtherDevicesCount(dbName,replicaServerHost));
                IRUtil.aggregateCuData(inventoryReportManager.getCusCountByVersionNo(dbName,replicaServerHost));
                IRUtil.aggregateBallastData(inventoryReportManager.getBallastCountByBallastName(dbName,replicaServerHost));
                IRUtil.aggregateBulbData(inventoryReportManager.getBulbsCountByBulbName(dbName, replicaServerHost));
                IRUtil.aggregateFixtureTypeData(inventoryReportManager.getCommissionedFxTypeCount(dbName, replicaServerHost));
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
	    
        Long totalCommissionedSensorsCount = IRUtil.totalCommissionedSensors.get("TotalCount");
        Long totalOtherDeviceCount = IRUtil.totalOtherDevices.get("TotalCount");
        IRUtil.calculateAggregateSummaryCount();
        IRUtil.totalCommissionedSensors.remove("TotalCount");
        IRUtil.totalOtherDevices.remove("TotalCount");
        model.addAttribute("totalCommissionedSensorsCount", totalCommissionedSensorsCount);
        model.addAttribute("totalOtherDeviceCount", totalOtherDeviceCount);
        model.addAttribute("totalBallastAssociatedCount", IRUtil.totalBallastAssociatedCount);
        model.addAttribute("totalLampsAssociatedCount", IRUtil.totalLampsAssociatedCount);
        model.addAttribute("totalFxTypeAssociatedCount", IRUtil.totalFxTypeAssociatedCount);
        model.addAttribute("totalCommissionedCuCount", IRUtil.totalCommissionedCuCount);
        model.addAttribute("totalBallastsCount", IRUtil.totalBallastsCount);
        model.addAttribute("totalBulbsCount", IRUtil.totalBulbsCount);
        
        model.addAttribute("totalCommissionedSensors", IRUtil.totalCommissionedSensors);
        model.addAttribute("totalOtherDevices", IRUtil.totalOtherDevices);
        model.addAttribute("totalCommissionedCus", IRUtil.totalCommissionedCus);
        model.addAttribute("totalBallastAssociated", IRUtil.totalBallastAssociated);
        model.addAttribute("totalBulbsAssociated", IRUtil.totalBulbsAssociated);
        model.addAttribute("totalFxTypeAssociated", IRUtil.totalFxTypeAssociated);
        model.addAttribute("site", site);
        model.addAttribute("customerId", site.getCustomer().getId());
      return "reports/inventoryreportdetail";
   }
   @RequestMapping(value = "/inventoryList.ems", method = { RequestMethod.GET, RequestMethod.POST })
   public String listEmInstance(Model model,  @RequestParam("customerId") long customerId)
   {
       model.addAttribute("customerId", customerId);
       Customer customer = customerManager.loadCustomerById(customerId);
       model.addAttribute("customerName", customer.getName());
       return "reports/inventoryreportsitelist";
   }
}
