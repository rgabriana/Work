package com.emscloud.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.communication.utils.ArgumentUtils;
import com.emscloud.communication.vos.OccSyncVo;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmLastGenericSynctime;
import com.emscloud.model.FacilityEmMapping;
import com.emscloud.model.FloorEnergyConsumption15min;
import com.emscloud.model.FloorSpaceOcc30Min;
import com.emscloud.model.FloorSpaceOccDaily;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.EmLastGenericSynctimeManager;
import com.emscloud.service.FacilityEmMappingManager;
import com.emscloud.util.DateUtil;
import com.emscloud.util.EmGenericSyncOperationEnums;
import com.emscloud.vo.SpaceOccChartData;

@Repository("occReportDao")
@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
public class OccReportDao extends BaseDaoHibernate {

    private static final Logger logger = Logger.getLogger(OccReportDao.class.getName());

    @Resource
    SessionFactory sessionFactory;
    @Resource
    EmInstanceManager emInstanceManager;
    @Resource
    FacilityEmMappingManager facilityEmMappingManager;
    @Resource
    EmLastGenericSynctimeManager emLastGenericSynctimeManager;

    @SuppressWarnings("unchecked")
    public List<Object[]> getDistinctProfiles(final Long customerId, final Long levelId, final int noOfDaysData) {
        final String hsql = " SELECT DISTINCT(t.name), g.id FROM FloorSpaceOccDaily f, ProfileGroups g, ProfileTemplate t WHERE f.groupId = g.profileNo and g.profileTemplate.id = t.id"
                + " and  f.levelId = "
                + levelId
                + " and  f.customerId = "
                + customerId
                + " AND f.captureAt >= '"
                + DateUtil.getDateBeforeNthDay(noOfDaysData) + "' ";
        List<Object[]> facilityList = new ArrayList<Object[]>();
        try {
            final Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
            facilityList = q.list();
        } catch (HibernateException he) {
            throw SessionFactoryUtils.convertHibernateAccessException(he);
        }
        return facilityList;
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> getDistinctProfiles(final Long customerId, final String levelIds, final int noOfDaysData,
            String modelName) {
        if (StringUtils.isEmpty(modelName)) {
            modelName = FloorSpaceOccDaily.class.getSimpleName();
        }

        final String hsql = " SELECT DISTINCT(t.name), g.id FROM "
                + modelName
                + " f, ProfileGroups g, ProfileTemplate t WHERE f.groupId = g.profileNo and g.profileTemplate.id = t.id"
                + " and  f.levelId in ( " + levelIds + " ) " + " and  f.customerId = " + customerId
                + " AND f.captureAt >= '" + DateUtil.getDateBeforeNthDay(noOfDaysData) + "' ";
        List<Object[]> facilityList = new ArrayList<Object[]>();
        try {
            final Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
            facilityList = q.list();
        } catch (HibernateException he) {
            throw SessionFactoryUtils.convertHibernateAccessException(he);
        }
        return facilityList;
    }

    @SuppressWarnings("unchecked")
    public List<SpaceOccChartData> getOccupancyData(final Long customerId, final Long levelId, String modelName,
            final String statType, final String occType, final int noOfDaysData) {

        if (StringUtils.isEmpty(modelName)) {
            modelName = FloorSpaceOccDaily.class.getSimpleName();
        }
        final Date refDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(refDate);
        cal.add(Calendar.DAY_OF_YEAR, (-1 * noOfDaysData));
        final String hsql = " SELECT new com.emscloud.vo.SpaceOccChartData(t.id, cast( AVG(f.avgNoOfSensors) as long) as avgNoOfSensors,"
                + "cast(AVG(f.avgTotalSensors) as long) as totalNoOfSensors,  t.name as profileName, SUM(f.totalNoOf1Bits) as total1bits, SUM(f.totalTotalBits) as totalBits"
                + ")  FROM "
                + modelName
                + " f, ProfileGroups g, ProfileTemplate t WHERE f.groupId = g.profileNo and g.profileTemplate.id = t.id"
                + " and  f.levelId = "
                + levelId
                + " and  f.customerId = "
                + customerId
                // + " AND ( (day(current_date()) - day( f.captureAt)) <= "
                + " AND f.captureAt >= '"
                + DateUtil.getDateBeforeNthDay(noOfDaysData)
                + "' "
                + " GROUP BY t.id, f.customerId,  f.groupId , f.levelId, t.templateNo, t.name";
        List<SpaceOccChartData> facilityList = new ArrayList<SpaceOccChartData>();
        try {
            final Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
            facilityList = q.list();
        } catch (HibernateException he) {
            throw SessionFactoryUtils.convertHibernateAccessException(he);
        }
        return facilityList;
    }

    public Date getLatestCaptureAtFor30MinFloor(Long levelId) {
        Date latest = null;
        try {

            DetachedCriteria maxDateQuery = DetachedCriteria.forClass(FloorSpaceOcc30Min.class);
            maxDateQuery.add(Restrictions.eq("levelId", levelId));
            ProjectionList proj = Projections.projectionList();
            proj.add(Projections.max("captureAt"));
            maxDateQuery.setProjection(proj);
            Criteria cr = sessionFactory.getCurrentSession().createCriteria(FloorEnergyConsumption15min.class);
            FloorSpaceOcc30Min fe = (FloorSpaceOcc30Min) cr.add(Restrictions.eq("levelId", levelId))
                    .add((Subqueries.propertyEq("captureAt", maxDateQuery))).uniqueResult();
            if (fe != null) {
                return latest = fe.getCaptureAt();
            }
        } catch (HibernateException hbe) {
            logger.error(hbe.getMessage(), hbe);
        }
        return latest;
    }

    public void saveOrUpdate(FloorSpaceOcc30Min oc) {

        sessionFactory.getCurrentSession().saveOrUpdate(oc);

    }

    public void saveOccSyncVO(List<OccSyncVo> items, Long emId) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            if (!ArgumentUtils.isNullOrEmpty(items)) {
                for (OccSyncVo item : items) {
                    EmInstance em = emInstanceManager.getEmInstance(emId);
                    if(em != null){
                    	FacilityEmMapping facEmMap = facilityEmMappingManager.getFacilityEmMappingOnEmFloorId(em.getId(),
                                new Long(item.getLevelId()));
                        if (!ArgumentUtils.isNull(facEmMap)) {
                            Long levelId = facEmMap.getFacilityId();
                            final Date d = inputFormat.parse(inputFormat.format(item.getCaptureAt()));
                            FloorSpaceOcc30Min newRow = getFloor30minOccData(em.getCustomer().getId(), item.getGroupId(),
                                    levelId, d);
                            if (newRow == null) {
                                newRow = new FloorSpaceOcc30Min();
                            }
                            newRow.setCustomerId(em.getCustomer().getId());
                            newRow.setLevelId(levelId);
                            newRow.setCaptureAt(d);
                            newRow.setNoOf1Bits(new Long(item.getNoOf1bits()));
                            newRow.setNoOfSensors(new Long(item.getNoOfSensors()));
                            newRow.setGroupId(new Long(item.getGroupId()));
                            newRow.setTotalBits(new Long(item.getTotalBits()));
                            newRow.setTotalSensors(new Long(item.getTotalSensors()));
                            logger.info("RawData From EM" + em.getId() + "Name:" + em.getName() + newRow.toString());
                            saveOrUpdate(newRow);
                            EmLastGenericSynctime lastTime = emLastGenericSynctimeManager.getEmLastGenericSynctimeForEmId(
                                    emId, EmGenericSyncOperationEnums.OCCUPANCY_SYNC.name());
                            if (lastTime == null) {
                                lastTime = new EmLastGenericSynctime();
                                lastTime.setEmId(emId);
                            }
                            if (lastTime.getLastSyncAt() == null || lastTime.getLastSyncAt().before(newRow.getCaptureAt())) {
                                lastTime.setLastSyncAt(newRow.getCaptureAt());
                                lastTime.setSyncOperation(EmGenericSyncOperationEnums.OCCUPANCY_SYNC.name());
                                emLastGenericSynctimeManager.saveOrUpdate(lastTime);
                            }

                        } else {
                            logger.error(em.getMacId()
                                    + ": Facility is not mapped on Cloud. Aggregation for this facility will not be done. "
                                    + " Facility id on EM that is not mapped = " + item.getLevelId());
                        }                    	
                    }                    
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(emId + " :written empty EcSyncVo List for this em");
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    public FloorSpaceOcc30Min getFloor30minOccData(long customerId, long groupId, long floorId, Date capture_at) {
        FloorSpaceOcc30Min energy = null;

        try {

            FloorSpaceOcc30Min fe = (FloorSpaceOcc30Min) sessionFactory.getCurrentSession()
                    .createCriteria(FloorSpaceOcc30Min.class).add(Restrictions.eq("customerId", customerId))
                    .add(Restrictions.eq("groupId", groupId)).add(Restrictions.eq("levelId", floorId))
                    .add(Restrictions.eq("captureAt", capture_at)).uniqueResult();
            if (fe != null) {
                energy = fe;
            }

        } catch (HibernateException hbe) {
            logger.error(hbe.getMessage(), hbe);
        }
        return energy;

    } // end of method getFloorDailyEnergyData

}
