package com.emscloud.ws;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import com.emscloud.dao.FacilityDao;
import com.emscloud.dao.OccReportDao;
import com.emscloud.model.BldSpaceOccDaily;
import com.emscloud.model.CampusSpaceOccDaily;
import com.emscloud.model.Facility;
import com.emscloud.model.FloorSpaceOccDaily;
import com.emscloud.model.OccSpaceStatDTO;
import com.emscloud.model.OccuSpaceStatDataDTO;
import com.emscloud.model.OccupancyMasterDTO;
import com.emscloud.model.OrganizationSpaceOccDaily;
import com.emscloud.model.ProfileGroups;
import com.emscloud.model.SpaceDataDTO;
import com.emscloud.service.FacilityManager;
import com.emscloud.service.ProfileGroupManager;
import com.emscloud.types.FacilityType;
import com.emscloud.util.OccupancyTypeEnum;
import com.emscloud.vo.BuildingOccupancyData;
import com.emscloud.vo.ChildLevelOccupancyData;
import com.emscloud.vo.OccupancyReportTO;
import com.emscloud.vo.SpaceOccChartData;

@Controller
@Path("/occupancyreportservice")
public class OccupancyReportService {

    @Resource
    private OccReportDao occReportDao;
    @Resource
    private FacilityDao facilityDao;
    @Resource
    FacilityManager facilityManager;
    @Resource(name = "profileGroupManager")
    private ProfileGroupManager groupManager;

    @Path("occupancychart/{custId}/{levelId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public OccSpaceStatDTO loadoccReportChart(@PathParam("custId") Long custId, @PathParam("levelId") Long levelId,
            @RequestParam("data") String userdata) throws UnsupportedEncodingException, ParseException {
        final OccSpaceStatDTO d = new OccSpaceStatDTO();
        // Get masterstatic
        final Map<Short, String> profIdNameMap = new HashMap<Short, String>();
        final List<ProfileGroups> allProfiles = groupManager.loadAllGroups();
        for (final ProfileGroups profGr : allProfiles) {
            profIdNameMap.put(profGr.getProfileNo(), profGr.getName());
        }

        final List<OccupancyMasterDTO> allStatTypes = getStatMasters();
        final List<OccupancyMasterDTO> allOccTypes = getOccMasters();
        final List<OccupancyMasterDTO> allSpaceTypes = getSpaceMasters(custId, String.valueOf(levelId), null, null);
        d.setAllOccTypes(allOccTypes);
        d.setAllSpaceTypes(allSpaceTypes);
        d.setAllStatTypes(allStatTypes);

        OccupancyMasterDTO s = new OccupancyMasterDTO();
        s.setId(1l);
        s.setName("Average");
        s.setAbbr("Avg");
        d.setStatMaster(s);

        // Get data
        final List<OccuSpaceStatDataDTO> data = getFloorData(custId, levelId);
        d.setData(data);
        return d;
    }

    @Path("loadfacilityoccupancy/{occupancytype}/{facilitytype}/{custId}/{levelId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public OccSpaceStatDTO loadFacilityOccReportByOccupanyType(@PathParam("occupancytype") String occupancytype,
            @PathParam("facilitytype") String facilitytype, @PathParam("custId") Long custId,
            @PathParam("levelId") Long levelId, @RequestParam("data") String userdata)
            throws UnsupportedEncodingException, ParseException {
        final OccSpaceStatDTO d = new OccSpaceStatDTO();
        // Get masterstatic
        final Map<Short, String> profIdNameMap = new HashMap<Short, String>();
        final List<ProfileGroups> allProfiles = groupManager.loadAllGroups();
        for (final ProfileGroups profGr : allProfiles) {
            profIdNameMap.put(profGr.getProfileNo(), profGr.getName());
        }

        final List<OccupancyMasterDTO> allStatTypes = getStatMasters();
        final List<OccupancyMasterDTO> allOccTypes = getOccMasters();

        d.setAllOccTypes(allOccTypes);
        d.setAllStatTypes(allStatTypes);

        OccupancyMasterDTO s = new OccupancyMasterDTO();
        s.setId(1l);
        s.setName("Average");
        s.setAbbr("Avg");
        d.setStatMaster(s);

        // Get dummy data for the presense report

        String facilitytypeStr = facilitytype;
        String dataLabel = "";
        String rollOverDataLabel = "";
        int distinctProfileDuration = OccupancyTypeEnum.YTD.getNoOfDays();
        if (facilitytypeStr.equalsIgnoreCase("organization")) {
            dataLabel = CampusSpaceOccDaily.class.getSimpleName();
            rollOverDataLabel = OrganizationSpaceOccDaily.class.getSimpleName();
        } else if (facilitytypeStr.equalsIgnoreCase("Campus")) {
            dataLabel = BldSpaceOccDaily.class.getSimpleName();
            rollOverDataLabel = CampusSpaceOccDaily.class.getSimpleName();
        } else if (facilitytypeStr.equalsIgnoreCase("building")) {
            dataLabel = FloorSpaceOccDaily.class.getSimpleName();
            rollOverDataLabel = BldSpaceOccDaily.class.getSimpleName();
        }

        final List<OccuSpaceStatDataDTO> chartData = new ArrayList<OccuSpaceStatDataDTO>();
        final List<Facility> childFacList = facilityManager.getChildFacilitiesByFacilityId(levelId);
        final StringBuilder levelIds = new StringBuilder();
        final boolean isChildExists = childFacList != null && childFacList.size() > 0;
        final OccupancyReportTO to = new OccupancyReportTO();
        if (isChildExists) {
            for (Facility faci : childFacList) {
                levelIds.append(String.valueOf(faci.getId()) + ",");
                to.setChartData(chartData);
                to.setCustId(custId);
                to.setLevelId(faci.getId());
                to.setModelName(dataLabel);
                // to.setActName(faci.getName());
                // to.setAbbrName(faci.getName());
                to.setDisplayName(faci.getName());
                if (occupancytype.equalsIgnoreCase(OccupancyTypeEnum.YTD.toString())) {
                    addYTDData(to);
                } else if (occupancytype.equalsIgnoreCase(OccupancyTypeEnum.QTD.toString())) {
                    distinctProfileDuration = OccupancyTypeEnum.QTD.getNoOfDays();
                    addLastQuarterData(to);
                } else {
                    distinctProfileDuration = OccupancyTypeEnum.LAST_30_DAYS.getNoOfDays();
                    addLast30DaysData(to);
                }
            }

            // get aggregated data to parent level and add it to the
            final List<OccuSpaceStatDataDTO> rollOverdata = new ArrayList<OccuSpaceStatDataDTO>();
            to.setChartData(rollOverdata);
            to.setCustId(custId);
            to.setLevelId(levelId);
            to.setModelName(rollOverDataLabel);
            to.setActName(null);
            to.setAbbrName(null);
            to.setDisplayName(null);
            if (occupancytype.equalsIgnoreCase(OccupancyTypeEnum.YTD.toString())) {
                addYTDData(to);
            } else if (occupancytype.equalsIgnoreCase(OccupancyTypeEnum.QTD.toString())) {
                addLastQuarterData(to);
            } else {
                addLast30DaysData(to);
            }
            d.setRollOverdata(rollOverdata); // Added the aggraged data in the same json now.

            final List<OccupancyMasterDTO> allSpaceTypes = getSpaceMasters(custId,
                    levelIds.toString().substring(0, levelIds.toString().length() - 1), dataLabel,
                    distinctProfileDuration);
            d.setAllSpaceTypes(allSpaceTypes);
            d.setData(chartData);
        }
        return d;
    }

    private List<OccuSpaceStatDataDTO> getFloorData(long custId, long levelId) {
        final List<OccuSpaceStatDataDTO> chartData = new ArrayList<OccuSpaceStatDataDTO>();
        final OccupancyReportTO to = new OccupancyReportTO();
        to.setChartData(chartData);
        to.setCustId(custId);
        to.setLevelId(levelId);
        addLast30DaysData(to);
        addLastQuarterData(to);
        addYTDData(to);
        return chartData;
    }

    private void addYTDData(final OccupancyReportTO to) {
        final List<SpaceOccChartData> dbdata = occReportDao.getOccupancyData(to.getCustId(), to.getLevelId(),
                to.getModelName(), "Avg", OccupancyTypeEnum.YTD.toString(), OccupancyTypeEnum.YTD.getNoOfDays());
        final OccuSpaceStatDataDTO chartDataRow = new OccuSpaceStatDataDTO();
        final OccupancyMasterDTO o = new OccupancyMasterDTO();
        // o.setId(1l);
        o.setName(StringUtils.isEmpty(to.getActName()) ? OccupancyTypeEnum.YTD.toString() : to.getActName());
        o.setAbbr(StringUtils.isEmpty(to.getAbbrName()) ? OccupancyTypeEnum.YTD.toString() : to.getAbbrName());
        o.setDisplayName(to.getDisplayName());
        chartDataRow.setOccMaster(o);
        final List<SpaceDataDTO> occData = new ArrayList<SpaceDataDTO>();
        chartDataRow.setSpaceData(occData);
        long cnt = 1l;
        for (final SpaceOccChartData dayRow : dbdata) {
            final SpaceDataDTO od = new SpaceDataDTO();
            final OccupancyMasterDTO d = new OccupancyMasterDTO();
            d.setId(cnt++);
            d.setName(dayRow.getProfileName());
            d.setAbbr(dayRow.getProfileName());
            d.setGroupId(dayRow.getGroupId());
            od.setSpaceMaster(d);
            od.setSensors(dayRow.getAvgNoOfSensors());
            od.setTotalSensors(dayRow.getTotalNoOfSensors());
            od.setTotalSqFt(dayRow.getTotalNoOfSensors() * 100);
            od.setValue(dayRow.getTotalBits() > 0 ? (dayRow.getTotal1bits() * 100 / dayRow.getTotalBits()) : 0);
            occData.add(od);
        }
        to.getChartData().add(chartDataRow);
        // chartData.add(chartDataRow);
    }

    private void addLastQuarterData(final OccupancyReportTO to) {
        final List<SpaceOccChartData> dbdata = occReportDao.getOccupancyData(to.getCustId(), to.getLevelId(),
                to.getModelName(), "Avg", OccupancyTypeEnum.QTD.toString(), OccupancyTypeEnum.QTD.getNoOfDays());
        final OccuSpaceStatDataDTO chartDataRow = new OccuSpaceStatDataDTO();
        final OccupancyMasterDTO o = new OccupancyMasterDTO();
        // o.setId(1l);
        o.setName(StringUtils.isEmpty(to.getActName()) ? OccupancyTypeEnum.QTD.toString() : to.getActName());
        o.setAbbr(StringUtils.isEmpty(to.getAbbrName()) ? OccupancyTypeEnum.QTD.toString() : to.getAbbrName());
        o.setDisplayName(to.getDisplayName());
        chartDataRow.setOccMaster(o);
        final List<SpaceDataDTO> occData = new ArrayList<SpaceDataDTO>();
        chartDataRow.setSpaceData(occData);
        long cnt = 1l;
        for (final SpaceOccChartData dayRow : dbdata) {
            final SpaceDataDTO od = new SpaceDataDTO();
            final OccupancyMasterDTO d = new OccupancyMasterDTO();
            d.setId(cnt++);
            d.setName(dayRow.getProfileName());
            d.setAbbr(dayRow.getProfileName());
            d.setGroupId(dayRow.getGroupId());
            od.setSpaceMaster(d);
            od.setSensors(dayRow.getAvgNoOfSensors());
            od.setTotalSensors(dayRow.getTotalNoOfSensors());
            od.setTotalSqFt(dayRow.getTotalNoOfSensors() * 100);
            od.setValue(dayRow.getTotalBits() > 0 ? (dayRow.getTotal1bits() * 100 / dayRow.getTotalBits()) : 0);
            occData.add(od);
        }
        to.getChartData().add(chartDataRow);
    }

    /**
     * This will add the data for last 30 days in the chartData
     * 
     * @param profIdNameMap
     * @param chartData
     * @param dbdata
     */
    private void addLast30DaysData(final OccupancyReportTO to) {
        final List<SpaceOccChartData> dbdata = occReportDao.getOccupancyData(to.getCustId(), to.getLevelId(),
                to.getModelName(), "Avg", OccupancyTypeEnum.LAST_30_DAYS.toString(),
                OccupancyTypeEnum.LAST_30_DAYS.getNoOfDays());
        final OccuSpaceStatDataDTO chartDataRow = new OccuSpaceStatDataDTO();
        final OccupancyMasterDTO o = new OccupancyMasterDTO();
        // o.setId(1l);
        o.setName(StringUtils.isEmpty(to.getActName()) ? OccupancyTypeEnum.LAST_30_DAYS.toString() : to.getActName());
        o.setAbbr(StringUtils.isEmpty(to.getAbbrName()) ? OccupancyTypeEnum.LAST_30_DAYS.toString() : to.getAbbrName());
        o.setDisplayName(to.getDisplayName());
        chartDataRow.setOccMaster(o);
        final List<SpaceDataDTO> occData = new ArrayList<SpaceDataDTO>();
        chartDataRow.setSpaceData(occData);
        long cnt = 1l;
        for (final SpaceOccChartData dayRow : dbdata) {
            final SpaceDataDTO od = new SpaceDataDTO();
            final OccupancyMasterDTO d = new OccupancyMasterDTO();
            d.setId(cnt++);
            d.setName(dayRow.getProfileName());
            d.setAbbr(dayRow.getProfileName());
            d.setGroupId(dayRow.getGroupId());
            od.setSpaceMaster(d);
            od.setSensors(dayRow.getAvgNoOfSensors());
            od.setTotalSensors(dayRow.getTotalNoOfSensors());
            od.setTotalSqFt(dayRow.getTotalNoOfSensors() * 100);
            od.setValue(dayRow.getTotalBits() > 0 ? (dayRow.getTotal1bits() * 100 / dayRow.getTotalBits()) : 0);
            occData.add(od);
        }
        to.getChartData().add(chartDataRow);
        // chartData.add(chartDataRow);
    }

    private List<OccupancyMasterDTO> getSpaceMasters(final Long customerId, final String levelIds, String modelName,
            Integer profilesInDuration) {
        if (profilesInDuration == null || profilesInDuration == 0) {
            profilesInDuration = OccupancyTypeEnum.YTD.getNoOfDays();
        }
        final List<OccupancyMasterDTO> l = new ArrayList<OccupancyMasterDTO>();
        final List<Object[]> dbdata = occReportDao.getDistinctProfiles(customerId, levelIds,
                OccupancyTypeEnum.YTD.getNoOfDays(), modelName);
        long cnt = 1l;
        for (final Object[] groupName : dbdata) {
            OccupancyMasterDTO d = new OccupancyMasterDTO();
            d.setId(cnt);
            d.setName(String.valueOf(groupName[0]));
            d.setAbbr(String.valueOf(groupName[0]));
            d.setGroupId((Long.parseLong(String.valueOf(groupName[1]))));
            l.add(d);
        }
        return l;
    }

    /**
     * TODO this has to be updated later and find out the way to get all statistics from db or through configuration
     * 
     * @return
     */
    private List<OccupancyMasterDTO> getOccMasters() {
        final List<OccupancyMasterDTO> l = new ArrayList<OccupancyMasterDTO>();
        OccupancyMasterDTO d = new OccupancyMasterDTO();
        d.setId(1l);
        d.setName(OccupancyTypeEnum.YTD.toString());
        d.setAbbr(OccupancyTypeEnum.YTD.toString());
        l.add(d);

        d = new OccupancyMasterDTO();
        d.setId(2l);
        d.setName(OccupancyTypeEnum.QTD.toString());
        d.setAbbr(OccupancyTypeEnum.QTD.toString());
        l.add(d);
        d = new OccupancyMasterDTO();
        d.setId(3l);
        d.setName(OccupancyTypeEnum.LAST_30_DAYS.toString());
        d.setAbbr(OccupancyTypeEnum.LAST_30_DAYS.toString());
        l.add(d);
        return l;
    }

    /**
     * TODO this has to be updated later and find out the way to get all statistics from db or through configuration
     * 
     * @return
     */
    private List<OccupancyMasterDTO> getStatMasters() {
        final List<OccupancyMasterDTO> l = new ArrayList<OccupancyMasterDTO>();
        OccupancyMasterDTO d = new OccupancyMasterDTO();
        d.setId(1l);
        d.setName("Average");
        d.setAbbr("Avg");
        l.add(d);
        return l;
    }

    @Path("loadBuildingOccupancyData/{custId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<BuildingOccupancyData> getBuildingDataForMapByCustomer(@PathParam("custId") Long custId) {
        List<Facility> buildingList = facilityDao.loadFacilityByCustomerAndFacilityType(custId, FacilityType.BUILDING);
        // Map<Long, BuildingOccupancyData> buildingMap = occReportDao.getBuildingOccupancyData(null, custId, "Avg",
        // OccupancyTypeEnum.LAST_30_DAYS.toString(), 365);
        List<BuildingOccupancyData> buildingOccupancyData = new ArrayList<BuildingOccupancyData>();

        for (Facility f : buildingList) {
            // BuildingOccupancyData bdata = buildingMap.get(f.getId());
            BuildingOccupancyData building = new BuildingOccupancyData();
            building.setBuildingId(f.getId());
            building.setName(f.getName());
            building.setCampusId(f.getParentId());
            building.setLocX(f.getLocX());
            building.setLocY(f.getLocY());
            List<SpaceOccChartData> spaceDataList = occReportDao.getOccupancyData(custId, f.getId(),
                    BldSpaceOccDaily.class.getSimpleName(), "", "", 365);
            long avgNoOfSensors = 0, totalNoOfSensors = 0, occupPercent = 0, total1bits = 0, totalBits = 0;
            for (SpaceOccChartData s : spaceDataList) {
                avgNoOfSensors += s.getAvgNoOfSensors();
                totalNoOfSensors += s.getTotalNoOfSensors();
                totalBits += s.getTotalBits();
                total1bits += s.getTotal1bits();
            }
            occupPercent = Math.round((totalBits > 0 ? (total1bits * 100 / totalBits) : 0));

            building.setAvgNoOfSensors(avgNoOfSensors);
            building.setTotalNoOfSensors(totalNoOfSensors);
            building.setTotalBits(totalBits);
            building.setTotal1bits(total1bits);
            building.setOccupPercent(occupPercent);
            Set<Facility> childFacilities = f.getChildFacilities();
            List<Facility> childFacilitiesList = new ArrayList<Facility>(childFacilities);
            Collections.sort(childFacilitiesList, Facility.FacilityNameComparator);
            List<ChildLevelOccupancyData> childOccDataList = new ArrayList<ChildLevelOccupancyData>();

            for (Facility child : childFacilitiesList) {
                List<SpaceOccChartData> childSpaceDataList = occReportDao.getOccupancyData(custId, child.getId(),
                        FloorSpaceOccDaily.class.getSimpleName(), "", "", 365);
                avgNoOfSensors = 0;
                totalNoOfSensors = 0;
                total1bits = 0;
                totalBits = 0;
                for (SpaceOccChartData s : childSpaceDataList) {
                    avgNoOfSensors += s.getAvgNoOfSensors();
                    totalNoOfSensors += s.getTotalNoOfSensors();
                    totalBits += s.getTotalBits();
                    total1bits += s.getTotal1bits();
                }
                occupPercent = Math.round((totalBits > 0 ? (total1bits * 100 / totalBits) : 0));
                ChildLevelOccupancyData childData = new ChildLevelOccupancyData(child.getId(), child.getName(),
                        avgNoOfSensors, totalNoOfSensors * 100, total1bits, totalBits, occupPercent);
                childOccDataList.add(childData);
            }
            building.setChildLevels(childOccDataList);
            // if (bdata != null) {
            // building.setAvgNoOfSensors(bdata.getAvgNoOfSensors());
            // building.setTotalNoOfSensors(bdata.getTotalNoOfSensors());
            // building.setTotalBits(bdata.getTotalBits());
            // building.setTotal1bits(bdata.getTotal1bits());
            // building.setOccupPercent(bdata.getOccupPercent());
            // building.setChildLevels(bdata.getChildLevels());
            // }
            buildingOccupancyData.add(building);
        }
        return buildingOccupancyData;
    }

    @Path("loadreportofchild/{occupancytype}/{facilitytype}/{custId}/{levelId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<ChildLevelOccupancyData> loadReportofChildLevel(@PathParam("occupancytype") String occupancytype,
            @PathParam("facilitytype") String parentFacilitytype, @PathParam("custId") Long custId,
            @PathParam("levelId") Long parentFacilityId, @RequestParam("data") String userdata)
            throws UnsupportedEncodingException, ParseException {
        final OccSpaceStatDTO d = new OccSpaceStatDTO();
        // Get masterstatic

        // Get dummy data for the presense report
        List<ChildLevelOccupancyData> result = new ArrayList<ChildLevelOccupancyData>();
        String dataLabel = "";
        int distinctProfileDuration = OccupancyTypeEnum.YTD.getNoOfDays();
        final OccupancyTypeEnum e = OccupancyTypeEnum.getEnumFromName(occupancytype);
        if (e != null) {
            distinctProfileDuration = e.getNoOfDays();
        }
        Facility parentFacility = facilityDao.getFacility(parentFacilityId);
        if (parentFacility.getLevel().equals(FacilityType.ORGANIZATION)) {
            dataLabel = CampusSpaceOccDaily.class.getSimpleName();
        } else if (parentFacility.getLevel().equals(FacilityType.CAMPUS)) {
            dataLabel = BldSpaceOccDaily.class.getSimpleName();
        } else if (parentFacility.getLevel().equals(FacilityType.BUILDING)) {
            dataLabel = FloorSpaceOccDaily.class.getSimpleName();
        }
        for (Facility child : parentFacility.getChildFacilities()) {
            List<SpaceOccChartData> childSpaceDataList = occReportDao.getOccupancyData(custId, child.getId(),
                    dataLabel, "", "", distinctProfileDuration);
            long avgNoOfSensors = 0, totalNoOfSensors = 0, total1bits = 0, totalBits = 0;
            for (SpaceOccChartData s : childSpaceDataList) {
                avgNoOfSensors += s.getAvgNoOfSensors();
                totalNoOfSensors += s.getTotalNoOfSensors();
                totalBits += s.getTotalBits();
                total1bits += s.getTotal1bits();
            }
            long occupPercent = Math.round((totalBits > 0 ? (total1bits * 100 / totalBits) : 0));
            ChildLevelOccupancyData childData = new ChildLevelOccupancyData(child.getId(), child.getName(),
                    avgNoOfSensors, totalNoOfSensors * 100, total1bits, totalBits, occupPercent);
            result.add(childData);
        }

        // List<ChildLevelOccupancyData> result = occReportDao.getOccupancyDataOfChildLevel(custId, levelId, dataLabel,
        // distinctProfileDuration);
        return result;
    }
}
