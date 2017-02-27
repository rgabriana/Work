package com.ems.mvc.debug;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ems.security.EmsAuthenticationContext;
import com.ems.service.DebugSqlService;
import com.ems.types.RoleType;

@Controller
@RequestMapping("/debug")
public class DebugController {

    private static final Logger log = Logger.getLogger(DebugController.class);

    @Resource
    EmsAuthenticationContext authenticaionContext;

    @Resource
    DebugSqlService debugSqlService;

    @RequestMapping("/sqlconsole.ems")
    public String sqlConsole(@RequestParam(value = "sqlString", required = false) String sqlString, Model model) {
        // If it's not admin let's not allow the user to run this
        if (this.authenticaionContext.getCurrentUserRoleType() != RoleType.Admin) {
            String message = "User not allowed to access this. This will be reported";
            log.info(message);
            throw new RuntimeException(message);
        }

        if (sqlString != null) {
            SqlRowSet data = debugSqlService.getResultList(sqlString);
            SqlRowSetMetaData metadataSet = data.getMetaData();
            int noOfColumn = metadataSet.getColumnCount();

            data.beforeFirst();

            List<List<String>> dataList = new ArrayList<List<String>>();
            while (data.next()) {
                List<String> rowDataList = new ArrayList<String>(noOfColumn);

                for (int i = 1; i <= noOfColumn; i++) {
                    rowDataList.add(data.getString(i));
                }
                dataList.add(rowDataList);
            }

            model.addAttribute("metadataSet", metadataSet);
            model.addAttribute("dataSet", dataList);
            model.addAttribute("noOfRows", dataList.size());
        }

        return "/debug/sqlconsole";
    }
}