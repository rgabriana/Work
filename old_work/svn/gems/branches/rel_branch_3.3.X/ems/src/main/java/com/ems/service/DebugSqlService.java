package com.ems.service;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

@Service("debugSqlService")
public class DebugSqlService {

    @Resource
    JdbcTemplate debugJdbcTemplate;

    public SqlRowSet getResultList(String sqlString) {
        return this.debugJdbcTemplate.queryForRowSet(sqlString);
    }

}
