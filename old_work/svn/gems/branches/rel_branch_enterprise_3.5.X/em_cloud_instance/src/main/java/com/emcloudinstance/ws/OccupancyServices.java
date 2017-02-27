package com.emcloudinstance.ws;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.communication.utils.ArgumentUtils;
import com.emcloudinstance.service.OccupancyManager;
import com.emcloudinstance.vo.DateEntityVo;
import com.emcloudinstance.vo.OccSyncVo;

@Component
@Path("/org/occ")
public class OccupancyServices {

    private static final Logger logger = Logger.getLogger(OccupancyServices.class.getName());

    @Resource(name = "occupancyManager")
    private OccupancyManager occupancyManager;

    /**
     * Data will be comma seprated list with first element is lastSynTime and reset all are emfacIds
     * 
     * @param data
     * @param headers
     * @return
     */
    @Path("floor/30min/sync")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<OccSyncVo> get30MinFloorOccupancy(String data, @Context HttpHeaders headers) {
        String validLastDate = null;
        final List<Long> emFacIds = new ArrayList<Long>();
        if (!StringUtils.isEmpty(data)) {
            final String dataArr[] = data.split(",");
            final int dataLen = dataArr.length;
            if (dataLen > 0) {
                validLastDate = dataArr[0];
            }
            if (dataLen > 1) {
                for (int i = 1; i < dataLen; i++) {
                    emFacIds.add(Long.parseLong(dataArr[i]));
                }
            }
        }

        String mac = null;
        String emTimeZone = null;
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        // Let everything to be in UTC we will convert date to respective Em time zone while querying.
        inputFormat.setTimeZone(TimeZone.getDefault());
        // No need to convert to a time zone as we are passing timezone in the param for selection.
        ArrayList<OccSyncVo> ecList = new ArrayList<OccSyncVo>();
        Date validLastT1 = null;
        try {
            mac = headers.getRequestHeader("em_mac").get(0);
            emTimeZone = headers.getRequestHeader("em_time_zone").get(0);
            if (validLastDate != null && !ArgumentUtils.isNullOrEmpty(mac) && !ArgumentUtils.isNullOrEmpty(emTimeZone)) {
                if (!validLastDate.equalsIgnoreCase("NA")) {
                    validLastT1 = inputFormat.parse(validLastDate);
                }// else If service is first time called.
                else {
                    validLastT1 = occupancyManager.loadFirstOccDataDate(mac, emTimeZone);
                }
                ecList = (ArrayList<OccSyncVo>) occupancyManager.load30MinOccupancyData(validLastT1, mac, emTimeZone,
                        emFacIds);
            } else {
                logger.warn("Time stamp passed is null or Mac is null or Em time Zone is null. Cannot aggregate for null object");
            }
        } catch (Exception e) {
            logger.error("Error while getting 30 Min sync occupancy data for uem " + e.getMessage(), e);
        }
        return ecList;
    }

    @Path("floor/maxmindate/energyconsumtion")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<DateEntityVo> getOccMaxDate(String validLastDate, @Context HttpHeaders headers) {
        final List<DateEntityVo> returnList = new ArrayList<DateEntityVo>();
        final DateEntityVo vo = new DateEntityVo();
        returnList.add(vo);
        String mac = null;
        String emTimeZone = null;
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        // Let everything to be in UTC we will convert date to respective Em time zone while querying.
        inputFormat.setTimeZone(TimeZone.getDefault());
        try {

            mac = headers.getRequestHeader("em_mac").get(0);
            emTimeZone = headers.getRequestHeader("em_time_zone").get(0);
            vo.setMaxDate(occupancyManager.loadLatestOccDataDate(mac, emTimeZone));
            vo.setMinDate(occupancyManager.loadFirstOccDataDate(mac, emTimeZone));
        } catch (Exception e) {
            logger.error("Error while getting 30 Min sync occupancy data for uem " + e.getMessage(), e);
        }
        return returnList;
    }
}
