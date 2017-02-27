package com.emscloud.mvc.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.communication.types.DataPullRequestStateType;
import com.emscloud.model.Customer;
import com.emscloud.model.DataPullRequest;
import com.emscloud.model.EmInstance;
import com.emscloud.security.EmsAuthenticationContext;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.DataPullRequestManager;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.UserManager;
import com.emscloud.vo.EmHealthDataVO;

@Controller
@RequestMapping("/datapullrequest")
public class DataPullRequestController {
	
	@Resource
	CustomerManager customerManager;
	
	@Resource
	EmInstanceManager emInstanceManager;
	
	@Resource
	DataPullRequestManager dataPullRequestManager;
	
	@Resource
	UserManager userManager;
	
	@Resource(name = "emsAuthContext")
	private EmsAuthenticationContext emsAuthContext;
	
	@RequestMapping(value = "/getlist.ems")
	public String reportCustomerInvoicePrompt(@RequestParam("customerId") Long customerId, 
			@RequestParam(value = "createsuccess", required = false) String state, 
			@RequestParam(value = "id", required = false) Long id,
			Model model) {
		model.addAttribute("custId", customerId);
		Customer c = customerManager.loadCustomerById(customerId);
		if(c != null) {
			model.addAttribute("customerName", c.getName());
		}
		if("S".equals(state)) {
			model.addAttribute("create", "true");
			model.addAttribute("id", id);
		}
		else if ("F".equals(state)) {
			model.addAttribute("create", "false");
		}
		return "datapullrequest/list";
	}
	
	@RequestMapping(value = "/create.ems")
	public String addCustomer(@RequestParam("customerId") Long customerId, Model model) {
		model.addAttribute("customerId", customerId);
		model.addAttribute("request", new DataPullRequest());
		List<String> tables = new ArrayList<String>();
		tables.add("energy_consumption");
		tables.add("energy_consumption_hourly");
		tables.add("energy_consumption_daily");
		tables.add("plugload_energy_consumption");
		tables.add("plugload_energy_consumption_hourly");
		tables.add("plugload_energy_consumption_daily");
		model.addAttribute("tables", tables);
		List<EmHealthDataVO> emList = new ArrayList<EmHealthDataVO>();
		List<EmInstance> list = emInstanceManager.loadEmInstancesByCustomerId(customerId);
		if(list == null) {
			list = new ArrayList<EmInstance>();
		}
		else {
			for(EmInstance em : list) {
				EmHealthDataVO evo = new EmHealthDataVO();
				evo.setEmInstanceId(em.getId());
				evo.setEmInstanceName(em.getName());
				emList.add(evo);
			}
		}
		model.addAttribute("emList", emList);
		return "datapullrequest/create";
	}
	
	@RequestMapping("/save.ems")
    String save(DataPullRequest d, @RequestParam("customerId") Long customerId) {
      
		String state = "F";
		Long id = null;
		Set<String> tables = new HashSet<String>();
		tables.add("energy_consumption");
		tables.add("energy_consumption_hourly");
		tables.add("energy_consumption_daily");
		tables.add("plugload_energy_consumption");
		tables.add("plugload_energy_consumption_hourly");
		tables.add("plugload_energy_consumption_daily");
		if(d.getEmId() != null && d.getFromDate() != null && d.getToDate() != null && d.getTableName() != null && tables.contains(d.getTableName().toLowerCase()) && d.getFromDate().before(d.getToDate())) {
			EmInstance em = emInstanceManager.getEmInstance(d.getEmId());
			if(em != null) {
				d.setId(null);
				d.setEm(em);
				d.setLastUpdatedAt(new Date());
				d.setRequestedAt(new Date());
				d.setState(DataPullRequestStateType.Queued);
				d.setTableName(d.getTableName().toLowerCase());
				d.setRequestedBy(userManager.loadUserByUserId(emsAuthContext.getUserId()));
				d = dataPullRequestManager.save(d);
				if(d.getId() != null) {
					state = "S";
					id = d.getId();
				}
			}
		}
		return "redirect:/datapullrequest/getlist.ems?customerId=" + customerId + "&createsuccess=" + state + "&id=" + id ;
	    
    }

}
