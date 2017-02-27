package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import com.ems.service.EmsUserAuditManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.utils.CommonUtils;
import com.ems.ws.util.Response;

@Controller
@Path("/audits")
public class EmsUserAuditService {
	
	
	@Resource
	EmsUserAuditManager emsUserAuditManager;
	
	@Autowired
	private MessageSource messageSource;
	@Resource
	private SystemConfigurationManager systemConfigurationManager;
	
	private static final int DEFAULT_ROWS = 20;
	private static final Logger m_Logger = Logger.getLogger("WSLogger");
	
	/**
	 * Get the list of audits based on filter
	 * 
	 * @param userdata
	 *            : (List of objects in order username, active types, start time, end time)
	 * @return: audits in json format
	 * @throws UnsupportedEncodingException 
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor')")
	@Path("getdata")
	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	public String getAuditsList(@RequestParam("data") String userdata) throws UnsupportedEncodingException {
		
		Response resp = new Response();
		/*resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "userdata", userdata);
		if(resp!= null && resp.getStatus()!=200){
			m_Logger.error("Validation error"+resp.getMsg());
			return "";
		}*/
		String[] input = userdata.split("&");
		StringBuffer output = new StringBuffer("{");
		int page = 0;
		long total, records = 0;
		String orderBy = null;
		String orderWay = null;
		String query = null;


		if (input != null && input.length > 0) {
			for (String each : input) {
				String[] keyval = each.split("=", 2);
				if (keyval[0].equals("page")) {
					Double pageNum = Double.parseDouble(keyval[1]);
					pageNum = Math.floor(pageNum);
					page= pageNum.intValue();
					//page = Integer.parseInt(keyval[1]);
				} else if (keyval[0].equals("userData")) {
					query = URLDecoder.decode(keyval[1], "UTF-8");
					output.append("\"" + keyval[0] + "\": \"" + query + "\"");
				} else if (keyval[0].equals("sidx")) {
					orderBy = keyval[1];
				} else if (keyval[0].equals("sord")) {
					orderWay = keyval[1];
				}
			}
		}

		

		List<Object> emsUserAudits = emsUserAuditManager.getUserAudits(orderBy, orderWay, query,
				(page - 1) * DEFAULT_ROWS, DEFAULT_ROWS);
		records = (Long) emsUserAudits.get(0);
		total = (int) (Math.ceil(records / new Double(DEFAULT_ROWS)));
		if (total == 0) {
			total = 1;
		}
		output.append(", \"page\": " + page);
		output.append(", \"total\": " + total);
		output.append(", \"records\": " + records);

		output.append(", \"rows\": [");

		/*
		 * AuditId - 0, logTime - 1, username - 2, actionType - 3, Description
		 * - 4, logTimeSort - 5, ipAddress - 6
		 */

		for (int i = 1; i < emsUserAudits.size(); i++) {
			Object[] each = (Object[]) emsUserAudits.get(i);
			if (i > 1) {
				output.append(", ");
			}
			output.append("{ \"id\": \""
					+ (Long) each[0]
					+ "\", \"cell\": [ \""
					+ (String) each[1]
					+ "\", \"" + (String) each[2] + "\", \""
					+ (String) each[6] + "\", \""
					+ each[3] + "\", \"" + each[4] + "\" ]}");
		}
		output.append(" ]");

		output.append("}");

		return output.toString();
	}

}
