package com.ems.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.Type;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.EventsAndFault;
import com.ems.model.Fixture;
import com.ems.model.Gateway;
import com.ems.model.User;
import com.ems.types.FacilityType;
import com.ems.util.OutageReportVO;
import com.ems.utils.ArgumentUtils;
import com.ems.utils.DateUtil;

@Repository("eventsAndFaultDao")
@Transactional(propagation = Propagation.REQUIRED)
public class EventsAndFaultDao extends BaseDaoHibernate {

    static final Logger logger = Logger.getLogger(EventsAndFaultDao.class.getName());
    
    private static final String SEVERITY_ORDER = "(CASE WHEN severity = 'Info' THEN '1' " +
			"WHEN severity = 'Warning' THEN '2' " +
			"WHEN severity = 'Minor' THEN '3' " +
			"WHEN severity = 'Major' THEN '4' " +
			"WHEN severity = 'Critical' THEN '5' " +
			"  ELSE '6' END) as severity";
    

    @SuppressWarnings("unchecked")
	public EventsAndFault getEventById(Long id) {
        List<EventsAndFault> eventsAndFaults = getSession().createCriteria(EventsAndFault.class)
                .add(Restrictions.eq("id", id)).list();
        if(eventsAndFaults.size() > 0) {
        	return eventsAndFaults.get(0);
        }
        
        return null;
    }
    
    /**
     * @param order (property on which to order the result)
     * @param orderWay (asc or desc)
     * @param filter (List of objects in order active, search string, group, start date, end date,
     *  										severity, event type, org node type, org node id)
     * @param roleType (user role)
     * @param offset (offset the result)
     * @param limit (number of result rows)
     * @return
     */
    @SuppressWarnings({ "deprecation", "unchecked" })
    public List<Object> getEventsAndFaults(String order, String orderWay, List<Object> filter, String roleType, int offset, int limit) {
    	
    	Criteria data = null;
    	Criteria rowCount = null;
    	
    	//Only fixture events
    	if("TenantAdmin".equals(roleType)) {
    		
    		rowCount = getSession().createCriteria(EventsAndFault.class, "ef")
        			.createAlias("device", "fixt", CriteriaSpecification.INNER_JOIN)
        			//.createAlias("gateway", "gw", CriteriaSpecification.LEFT_JOIN)
        			.setFetchMode("fixt", FetchMode.JOIN)
        			//.setFetchMode("gw", FetchMode.JOIN)
        			.setProjection(Projections.rowCount());
    		
    		data = getSession().createCriteria(EventsAndFault.class, "ef")
        			.createAlias("device", "fixt", CriteriaSpecification.INNER_JOIN)
        			//.createAlias("gateway", "gw", CriteriaSpecification.LEFT_JOIN)
        			.setFetchMode("fixt", FetchMode.JOIN)
        			//.setFetchMode("gw", FetchMode.JOIN)
        			;
        	
        	if(filter != null) {
        		Boolean active = (Boolean)("-1".equals(filter.get(0)) ? null : ("1".equals(filter.get(0)) ? true : false));
        		if(active != null) {
        			rowCount.add(Restrictions.sqlRestriction("{alias}.active = ? ", active, Hibernate.BOOLEAN));
        			data.add(Restrictions.sqlRestriction("{alias}.active = ? ", active, Hibernate.BOOLEAN));
        		}
        		String searchString = (String)filter.get(1);
        		if(searchString != null) {
        			rowCount.add(Restrictions.or(Restrictions.sqlRestriction("lower(to_char({alias}.event_time, 'YYYY:MM:DD HH12:MI:SS AM')) like ? ", "%" + searchString.toLowerCase() + "%", Hibernate.STRING), 
        					Restrictions.or(Restrictions.ilike("fixt.location", searchString, MatchMode.ANYWHERE),
            						Restrictions.or(Restrictions.ilike("fixt.name", searchString, MatchMode.ANYWHERE),
            							Restrictions.or(Restrictions.ilike("ef.severity", searchString, MatchMode.ANYWHERE),
            								Restrictions.or(Restrictions.ilike("ef.eventType", searchString, MatchMode.ANYWHERE),
            									Restrictions.ilike("ef.description", "%" + searchString + "%")))))));
        			data.add(Restrictions.or(Restrictions.sqlRestriction("lower(to_char({alias}.event_time, 'YYYY:MM:DD HH12:MI:SS AM')) like ? ", "%" + searchString.toLowerCase() + "%", Hibernate.STRING), 
        					Restrictions.or(Restrictions.ilike("fixt.location", searchString, MatchMode.ANYWHERE),
        						Restrictions.or(Restrictions.ilike("fixt.name", searchString, MatchMode.ANYWHERE),
        							Restrictions.or(Restrictions.ilike("ef.severity", searchString, MatchMode.ANYWHERE),
        								Restrictions.or(Restrictions.ilike("ef.eventType", searchString, MatchMode.ANYWHERE),
        									Restrictions.ilike("ef.description", "%" + searchString + "%")))))));
        		}
        		Long groupId = (Long)filter.get(2);
        		if(groupId != null) {
        			rowCount.add(Restrictions.sqlRestriction(" {alias}.device_id in (select infixt.id from fixture infixt where infixt.group_id = ? )", groupId, Hibernate.LONG));
        			data.add(Restrictions.sqlRestriction(" {alias}.device_id in (select infixt.id from fixture infixt where infixt.group_id = ? )", groupId, Hibernate.LONG));
        		}
        		Date startDate = (Date)filter.get(3);
        		if(startDate != null) {
        			rowCount.add(Restrictions.ge("ef.eventTime", startDate));
        			data.add(Restrictions.ge("ef.eventTime", startDate));
        		}
        		Date endDate = (Date)filter.get(4);
        		if(endDate != null) {
        			rowCount.add(Restrictions.le("ef.eventTime", endDate));
        			data.add(Restrictions.le("ef.eventTime", endDate));
        		}
        		List<String> severity = (List<String>)filter.get(5);
        		if(severity != null && severity.size() > 0) {
        			rowCount.add(Restrictions.in("ef.severity", severity));
        			data.add(Restrictions.in("ef.severity", severity));
        		}
        		List<String> eventType = (List<String>)filter.get(6);
        		if(eventType != null && eventType.size() > 0) {
        			rowCount.add(Restrictions.in("ef.eventType", eventType));
        			data.add(Restrictions.in("ef.eventType", eventType));
        		}
        		FacilityType orgType = (FacilityType)filter.get(7);
        		Long orgId = (Long)filter.get(8);
        		if(orgType != null && orgId != null) {
        			switch(orgType) {
        				case CAMPUS: {
        					rowCount.add(Restrictions.sqlRestriction(" {alias}.device_id in (select infixt.id from device infixt where infixt.campus_id = ? )", orgId, Hibernate.LONG));
        					data.add(Restrictions.sqlRestriction(" {alias}.device_id in (select infixt.id from device infixt where infixt.campus_id = ? )", orgId, Hibernate.LONG));
        					break;
        				}
        				case BUILDING: {
        					rowCount.add(Restrictions.sqlRestriction(" {alias}.device_id in (select infixt.id from device infixt where infixt.building_id = ? )", orgId, Hibernate.LONG));
        					data.add(Restrictions.sqlRestriction(" {alias}.device_id in (select infixt.id from device infixt where infixt.building_id = ? )", orgId, Hibernate.LONG));
        					break;
        				}
        				case FLOOR: {
        					rowCount.add(Restrictions.sqlRestriction(" {alias}.device_id in (select infixt.id from device infixt where infixt.floor_id = ? )", orgId, Hibernate.LONG));
        					data.add(Restrictions.sqlRestriction(" {alias}.device_id in (select infixt.id from device infixt where infixt.floor_id = ? )", orgId, Hibernate.LONG));
        					break;
        				}
        				case AREA: {
        					rowCount.add(Restrictions.sqlRestriction(" {alias}.device_id in (select infixt.id from device infixt where infixt.area_id = ? )", orgId, Hibernate.LONG));
        					data.add(Restrictions.sqlRestriction(" {alias}.device_id in (select infixt.id from device infixt where infixt.area_id = ? )", orgId, Hibernate.LONG));
        					break;
        				}
        				case COMPANY: {
        					break;
        				}
        				default: {
        					rowCount.add(Restrictions.sqlRestriction(" {alias}.device_id in (select infixt.id from device infixt where infixt.campus_id = ? )", new Long("0"), Hibernate.LONG));
                			data.add(Restrictions.sqlRestriction(" {alias}.device_id in (select infixt.id from device infixt where infixt.campus_id = ? )", new Long("0"), Hibernate.LONG));
        				}
        			}
        		}
        		//In case we wish to query events out of tree context, use the following block.
        		else {
        			rowCount.add(Restrictions.sqlRestriction(" {alias}.device_id in (select infixt.id from device infixt where infixt.campus_id = ? )", new Long("0"), Hibernate.LONG));
        			data.add(Restrictions.sqlRestriction(" {alias}.device_id in (select infixt.id from device infixt where infixt.campus_id = ? )", new Long("0"), Hibernate.LONG));
        		}
        		
        	}

    	}
    	//All events
    	else if("Admin".equals(roleType) || "FacilitiesAdmin".equals(roleType)) {
    		rowCount = getSession().createCriteria(EventsAndFault.class, "ef")
        			.createAlias("device", "fixt", CriteriaSpecification.LEFT_JOIN)
        			//.createAlias("gateway", "gw", CriteriaSpecification.LEFT_JOIN)
        			.setFetchMode("fixt", FetchMode.JOIN)
        			//.setFetchMode("gw", FetchMode.JOIN)
        			.setProjection(Projections.rowCount());
    		
    		data = getSession().createCriteria(EventsAndFault.class, "ef")
        			.createAlias("device", "fixt", CriteriaSpecification.LEFT_JOIN)
        			//.createAlias("gateway", "gw", CriteriaSpecification.LEFT_JOIN)
        			.setFetchMode("fixt", FetchMode.JOIN)
        			//.setFetchMode("gw", FetchMode.JOIN)
        			;
        	
        	if(filter != null) {
        		Boolean active = (Boolean)("-1".equals(filter.get(0)) ? null : ("1".equals(filter.get(0)) ? true : false));
        		if(active != null) {
        			rowCount.add(Restrictions.sqlRestriction("{alias}.active = ? ", active, Hibernate.BOOLEAN));
        			data.add(Restrictions.sqlRestriction("{alias}.active = ? ", active, Hibernate.BOOLEAN));
        		}
        		String searchString = (String)filter.get(1);
        		if(searchString != null) {
        			rowCount.add(Restrictions.or(Restrictions.sqlRestriction("lower(to_char({alias}.event_time, 'YYYY:MM:DD HH12:MI:SS AM')) like ? ", "%" + searchString.toLowerCase() + "%", Hibernate.STRING), 
        					Restrictions.or(Restrictions.ilike("fixt.location", searchString, MatchMode.ANYWHERE),
            					Restrictions.or(Restrictions.ilike("fixt.name", searchString, MatchMode.ANYWHERE),
//            						Restrictions.or(Restrictions.ilike("gw.location", searchString, MatchMode.ANYWHERE),
//            	            			Restrictions.or(Restrictions.ilike("gw.gatewayName", searchString, MatchMode.ANYWHERE),
            	            				Restrictions.or(Restrictions.ilike("ef.severity", searchString, MatchMode.ANYWHERE),
            	            					Restrictions.or(Restrictions.ilike("ef.eventType", searchString, MatchMode.ANYWHERE),
            	            						Restrictions.ilike("ef.description", "%" + searchString + "%")))))));
        			
        			data.add(Restrictions.or(Restrictions.sqlRestriction("lower(to_char({alias}.event_time, 'YYYY:MM:DD HH12:MI:SS AM')) like ? ", "%" + searchString.toLowerCase() + "%", Hibernate.STRING), 
        					Restrictions.or(Restrictions.ilike("fixt.location", searchString, MatchMode.ANYWHERE),
                					Restrictions.or(Restrictions.ilike("fixt.name", searchString, MatchMode.ANYWHERE),
//                						Restrictions.or(Restrictions.ilike("gw.location", searchString, MatchMode.ANYWHERE),
//                	            			Restrictions.or(Restrictions.ilike("gw.gatewayName", searchString, MatchMode.ANYWHERE),
                	            				Restrictions.or(Restrictions.ilike("ef.severity", searchString, MatchMode.ANYWHERE),
                	            					Restrictions.or(Restrictions.ilike("ef.eventType", searchString, MatchMode.ANYWHERE),
                	            						Restrictions.ilike("ef.description", "%" + searchString + "%")))))));
        		}
        		if(filter.get(2) != null) {
        			Long groupId = Long.parseLong(filter.get(2).toString());
        			rowCount.add(Restrictions.sqlRestriction(" {alias}.device_id in (select infixt.id from fixture infixt where infixt.group_id = ? )", groupId, Hibernate.LONG));
        			data.add(Restrictions.sqlRestriction(" {alias}.device_id in (select infixt.id from fixture infixt where infixt.group_id = ? )", groupId, Hibernate.LONG));
        		}
        		Date startDate = (Date)filter.get(3);
        		if(startDate != null) {
        			rowCount.add(Restrictions.ge("ef.eventTime", startDate));
        			data.add(Restrictions.ge("ef.eventTime", startDate));
        		}
        		Date endDate = (Date)filter.get(4);
        		if(endDate != null) {
        			rowCount.add(Restrictions.le("ef.eventTime", endDate));
        			data.add(Restrictions.le("ef.eventTime", endDate));
        		}
        		List<String> severity = (List<String>)filter.get(5);
        		if(severity != null && severity.size() > 0) {
        			rowCount.add(Restrictions.in("ef.severity", severity));
        			data.add(Restrictions.in("ef.severity", severity));
        		}
        		List<String> eventType = (List<String>)filter.get(6);
        		if(eventType != null && eventType.size() > 0) {
        			rowCount.add(Restrictions.in("ef.eventType", eventType));
        			data.add(Restrictions.in("ef.eventType", eventType));
        		}
        		FacilityType orgType = (FacilityType)filter.get(7);
        		Long orgId = (Long)filter.get(8);
        		if(orgType != null && orgId != null) {
        			switch(orgType) {
        				case CAMPUS: {
        					rowCount.add(Restrictions.sqlRestriction(" ({alias}.device_id in (select infixt.id from device infixt where infixt.campus_id = ? ) or {alias}.device_id is null) ", orgId, Hibernate.LONG));
        					data.add(Restrictions.sqlRestriction(" ({alias}.device_id in (select infixt.id from device infixt where infixt.campus_id = ? ) or {alias}.device_id is null )", orgId, Hibernate.LONG));
        					
        					/*
        					rowCount.add( Restrictions.or( Property.forName( "gw.campusId" ).eq(orgId), Property.forName( "gateway" ).isNull()) );
        					data.add( Restrictions.or( Property.forName( "gw.campusId" ).eq(orgId), Property.forName( "gateway" ).isNull()) );
        					
        					rowCount.add(Restrictions.sqlRestriction(" ({alias}.gateway_id in (select ingate.id from gateway ingate where ingate.campus_id = ? ) or {alias}.gateway_id is null) ", orgId, Hibernate.LONG));
                            data.add(Restrictions.sqlRestriction(" ({alias}.gateway_id in (select ingate.id from gateway ingate where ingate.campus_id = ? ) or {alias}.gateway_id is null )", orgId, Hibernate.LONG));
        					*/
        					break;
        				}
        				case BUILDING: {
        					rowCount.add(Restrictions.sqlRestriction(" ({alias}.device_id in (select infixt.id from device infixt where infixt.building_id = ? ) or {alias}.device_id is null) ", orgId, Hibernate.LONG));
        					data.add(Restrictions.sqlRestriction(" ({alias}.device_id in (select infixt.id from device infixt where infixt.building_id = ? ) or {alias}.device_id is null) ", orgId, Hibernate.LONG));
        					
        					/*
        					rowCount.add( Restrictions.or( Property.forName( "gw.buildingId" ).eq(orgId), Property.forName( "gateway" ).isNull()) );
        					data.add( Restrictions.or( Property.forName( "gw.buildingId" ).eq(orgId), Property.forName( "gateway" ).isNull()) );        					
        					
        					rowCount.add(Restrictions.sqlRestriction(" ({alias}.gateway_id in (select gateway.id from gateway where gateway.buildingId = ? ) or {alias}.gateway_id is null) ", orgId, Hibernate.LONG));
                            data.add(Restrictions.sqlRestriction(" ({alias}.gateway_id in (select gateway.id from gateway where gateway.buildingId = ? ) or {alias}.gateway_id is null) ", orgId, Hibernate.LONG));
        					*/
        					break;
        				}
        				case FLOOR: {
        					rowCount.add(Restrictions.sqlRestriction(" ({alias}.device_id in (select infixt.id from device infixt where infixt.floor_id = ? ) or {alias}.device_id is null) ", orgId, Hibernate.LONG));
        					data.add(Restrictions.sqlRestriction(" ({alias}.device_id in (select infixt.id from device infixt where infixt.floor_id = ? ) or {alias}.device_id is null) ", orgId, Hibernate.LONG));
        					/*
        					rowCount.add( Restrictions.or( Property.forName( "gw.floor.id" ).eq(orgId), Property.forName( "gateway" ).isNull()) );
        					data.add( Restrictions.or( Property.forName( "gw.floor.id" ).eq(orgId), Property.forName( "gateway" ).isNull()) );
        					
        					rowCount.add(Restrictions.sqlRestriction(" ({alias}.gateway_id in (select ingate.id from gateway ingate where ingate.floor_id = ? ) or {alias}.gateway_id is null) ", orgId, Hibernate.LONG));
                            data.add(Restrictions.sqlRestriction(" ({alias}.gateway_id in (select ingate.id from gateway ingate where ingate.floor_id = ? ) or {alias}.gateway_id is null) ", orgId, Hibernate.LONG));
        					*/
        					break;
        				}
        				case AREA: {
        					rowCount.add(Restrictions.sqlRestriction(" ({alias}.device_id in (select infixt.id from device infixt where infixt.area_id = ? ) or {alias}.device_id is null) ", orgId, Hibernate.LONG));
        					data.add(Restrictions.sqlRestriction(" ({alias}.device_id in (select infixt.id from device infixt where infixt.area_id = ? ) or {alias}.device_id is null) ", orgId, Hibernate.LONG));
        					
        					/*
        					rowCount.add( Restrictions.or( Property.forName( "gw.area.id" ).eq(orgId), Property.forName( "gateway" ).isNull()) );
        					data.add( Restrictions.or( Property.forName( "gw.area.id" ).eq(orgId), Property.forName( "gateway" ).isNull()) );
        					
        					rowCount.add(Restrictions.sqlRestriction(" ({alias}.gateway_id in (select ingate.id from gateway ingate where ingate.area_id = ? ) or {alias}.gateway_id is null) ", orgId, Hibernate.LONG));
                            data.add(Restrictions.sqlRestriction(" ({alias}.gateway_id in (select ingate.id from gateway ingate where ingate.area_id = ? ) or {alias}.gateway_id is null) ", orgId, Hibernate.LONG));
        					*/
        					break;
        				}
        				case COMPANY: {
        					break;
        				}
        				default: {
        					rowCount.add(Restrictions.sqlRestriction(" {alias}.device_id in (select infixt.id from device infixt where infixt.campus_id = ? )", new Long("0"), Hibernate.LONG));
                			data.add(Restrictions.sqlRestriction(" {alias}.device_id in (select infixt.id from device infixt where infixt.campus_id = ? )", new Long("0"), Hibernate.LONG));
                			/*
                			rowCount.add(Restrictions.sqlRestriction(" {alias}.gateway_id in (select ingate.id from gateway ingate where ingate.campus_id = ? )", new Long("0"), Hibernate.LONG));
                            data.add(Restrictions.sqlRestriction(" {alias}.gateway_id in (select ingate.id from gateway ingate where ingate.campus_id = ? )", new Long("0"), Hibernate.LONG));
                            */
        				}
        			}
        		}
        		//In case we wish to query events out of tree context, use the following block.
        		else {
        			rowCount.add(Restrictions.sqlRestriction(" {alias}.device_id in (select infixt.id from device infixt where infixt.campus_id = ? )", new Long("0"), Hibernate.LONG));
        			data.add(Restrictions.sqlRestriction(" {alias}.device_id in (select infixt.id from device infixt where infixt.campus_id = ? )", new Long("0"), Hibernate.LONG));
        			
        			/*
        			rowCount.add(Restrictions.sqlRestriction(" {alias}.gateway_id in (select ingate.id from gateway ingate where ingate.campus_id = ? )", new Long("0"), Hibernate.LONG));
                    data.add(Restrictions.sqlRestriction(" {alias}.gateway_id in (select ingate.id from gateway ingate where ingate.campus_id = ? )", new Long("0"), Hibernate.LONG));
        			*/
        		}
        		
        	}
    	}
    	
    	data.setProjection(Projections.projectionList().add(Projections.property("id"), "id")
				.add(Projections.sqlProjection("to_char({alias}.event_time, 'YYYY:MM:DD - HH12:MI:SS AM') as eventTimeDisplay", new String[] {"eventTimeDisplay"}, new Type[] {Hibernate.STRING}), "eventTimeDisplay")
				.add(Projections.property("ef.severity"), "severityDisplay")
				.add(Projections.property("ef.eventType"), "eventType")
				.add(Projections.property("ef.description"), "description")
				.add(Projections.property("ef.active"), "active")
				.add(Projections.property("ef.resolvedOn"), "resolvedOn")
				.add(Projections.property("fixt.id"), "fixtureId")
				.add(Projections.property("fixt.location"), "fixturelocation")
				.add(Projections.property("fixt.floor.id"), "floorId")
				.add(Projections.property("fixt.buildingId"), "buildingId")
				.add(Projections.property("fixt.campusId"), "campusId")
				.add(Projections.property("fixt.name"), "name")
//				.add(Projections.property("gw.id"))
//				.add(Projections.property("gw.location"), "gatewaylocation")
//				.add(Projections.property("gw.floor.id"))
//				.add(Projections.property("gw.buildingId"))
//				.add(Projections.property("gw.campusId"))
//				.add(Projections.property("gw.name"))
				.add(Projections.sqlProjection(SEVERITY_ORDER, new String[] { "severity"}, new Type[] {Hibernate.STRING}), "severity")
				.add(Projections.property("ef.eventTime"), "eventTime")
				);
    	
    	if(limit > 0) {
			data.setMaxResults(limit)
			.setFirstResult(offset);
		}
		
		if(order != null && !"".equals(order)) {
			if("desc".equals(orderWay)) {
				data.addOrder(Order.desc(order));
			}
			else {
				data.addOrder(Order.asc(order));
			}
		}
		else {
			data.addOrder(Order.desc("severity"))
			.addOrder(Order.desc("eventTime"))
			;
		}
    	
    	List<Object> output = (List<Object>)rowCount.list();
    	Long count = (Long)output.get(0);
    	if(count.compareTo(new Long("0")) > 0) {
    		output.addAll(data.list());
    	}
    	return output;
    	
    }
     
    @SuppressWarnings("unchecked")
    public List<EventsAndFault> getEventsAndFaultsByFixtureId(Long fixture_id) {
        List<EventsAndFault> eventsAndFaults = getSession().createCriteria(EventsAndFault.class)
                .add(Restrictions.eq("device.id", fixture_id)).add(Restrictions.eq("active", true))
                .addOrder(Order.desc("eventTime")).list();
        return eventsAndFaults;
    }
    
    public List<EventsAndFault> getEventsAndFaultsByFixtureId(Long fixture_id, String eventType) {
        List<EventsAndFault> eventsAndFaults = getSession().createCriteria(EventsAndFault.class)
                .add(Restrictions.eq("device.id", fixture_id)).add(Restrictions.eq("active", true))
                .add(Restrictions.eq("eventType", eventType)).addOrder(Order.desc("eventTime")).list();
        return eventsAndFaults;
    }

    
    @SuppressWarnings("unchecked")
    public List<EventsAndFault> getEventsAndFaultsByGatewayId(Long gateway_id) {
        List<EventsAndFault> eventsAndFaults = getSession().createCriteria(EventsAndFault.class)
                .add(Restrictions.eq("device.id", gateway_id)).add(Restrictions.eq("active", true))
                .addOrder(Order.desc("eventTime")).list();
        return eventsAndFaults;
    }
    
    public void resolveEventsAndFaults(Long[] ids, User user, String description) {
        String newComment = "[" + DateUtil.formatDate(new Date(), DateUtil.INTERNATIONAL_FORMAT) + ": @"
                + user.getEmail() + ":]" + description + ";\n";
        Query query = getSession()
                .createQuery(
                        "update EventsAndFault set active='f', resolvedBy=:user, resolvedOn=:today, resolutionComments = :comments where id in (:ids)");
        query.setParameter("user", user);
        query.setTimestamp("today", new Date());
        query.setString("comments", newComment);
        query.setParameterList("ids", ids).executeUpdate();
    }   

    public void resolveAlarms(Long deviceId, String eventType) {
      
      Query query = getSession().createQuery("update EventsAndFault set active='f', resolvedOn=:today " +
	  " where device.id = :deviceId and eventType = :eventType and active = 't'");
      query.setParameter("deviceId", deviceId);
      query.setTimestamp("today", new Date());
      query.setString("eventType", eventType).executeUpdate();
      
    } //end of method resolveAlarms
    
    @SuppressWarnings("rawtypes")
	public List<EventsAndFault> getEventsOnFaultyFixtures(Integer floorId, java.util.Date toDate,
            java.util.Date fromDate) {
        SQLQuery sqlQuery = getSession()
                .createSQLQuery(
                        "select ef.event_time,ef.severity,ef.event_type,ef.description,ef.active,ef.device_id,fl.name as floorName,b.name as buildingname,c.name as campusname from events_and_fault ef inner join device f on ef.device_id=f.id inner join floor fl on  fl.id=f.floor_id inner join building b on b.id=f.building_id inner join campus c on c.id=f.campus_id where f.floor_id="
                                + floorId
                                + " and  (ef.severity='Warning' or ef.severity='Critical') and active=true and event_time>='"
                                + fromDate + "' and event_time<='" + toDate + "'");
        List list = sqlQuery.list();
        List<EventsAndFault> eventsAndFaults = new ArrayList<EventsAndFault>();
        for (int i = 0; i < list.size(); i++) {
            Object[] data = (Object[]) list.get(i);
            EventsAndFault eventsAndFault = new EventsAndFault();
            eventsAndFault.setEventTime(DateUtil.parseString(data[0].toString(), "MM/dd/yyyy"));
            eventsAndFault.setSeverity(data[1].toString());
            eventsAndFault.setEventType(data[2].toString());
            eventsAndFault.setDescription(data[3].toString());
            eventsAndFault.setActive(Boolean.valueOf(data[4].toString()));
            eventsAndFaults.add(eventsAndFault);
        }
        return eventsAndFaults;
    }

    @SuppressWarnings({ "rawtypes" })
    public List getFaultyFloorsByCampusAndBuilding(Integer locationId, String locationType, java.util.Date toDate,
            java.util.Date fromDate) {
        String queryString = "";
        if (locationType.equals("campus")) {
            queryString = "select distinct( floor_id) from device where id in ( select device_id from events_and_fault where  event_time>='"
                    + fromDate
                    + "' and event_time<='"
                    + toDate
                    + "' and active=true and (severity='Warning' or severity='Critical')) and campus_id="
                    + locationId
                    + "";
        }
        if (locationType.equals("building")) {
            queryString = "select distinct( floor_id) from device where id in ( select device_id from events_and_fault where  event_time>='"
                    + fromDate
                    + "' and event_time<='"
                    + toDate
                    + "' and active=true and (severity='Warning' or severity='Critical')) and building_id="
                    + locationId + "";
        }
        SQLQuery sqlQuery = getSession().createSQLQuery(queryString);
        List list = sqlQuery.list();
        ;
        return list;
    }

    @SuppressWarnings("rawtypes")
	public List<Fixture> getFaultyFixtures(Integer floorId, java.util.Date toDate, java.util.Date fromDate) {
        SQLQuery sqlQuery = getSession().createSQLQuery(
                "select id,x,y from device where id in (select device_id from events_and_fault where  event_time>='"
                        + fromDate + "' and event_time<='" + toDate
                        + "' and active=true and (severity='Warning' or severity='Critical')) and floor_id=" + floorId
                        + " order by id");
        List list = sqlQuery.list();
        List<Fixture> fixtures = new ArrayList<Fixture>();
        for (int i = 0; i < list.size(); i++) {
            Object[] data = (Object[]) list.get(i);
            Fixture fixture = new Fixture();
            fixture.setId(Long.valueOf(data[0].toString()));
            fixture.setXaxis(Integer.valueOf(data[1].toString()));
            fixture.setYaxis(Integer.valueOf(data[2].toString()));
            fixtures.add(fixture);
        }
        return fixtures;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<Fixture> getFaultyFixturesByNode(OutageReportVO outageVO) {
        String nodeQuery = null;
        if (outageVO.getNodeType().equalsIgnoreCase("campus")) {
            nodeQuery = new String("where campus_id = ");
        } else if (outageVO.getNodeType().equalsIgnoreCase("building")) {
            nodeQuery = new String("where building_id = ");
        } else if (outageVO.getNodeType().equalsIgnoreCase("floor")) {
            nodeQuery = new String("where floor_id = ");
        } else if (outageVO.getNodeType().equalsIgnoreCase("area")) {
            nodeQuery = new String("where area_id = ");
        }
        if (!ArgumentUtils.isNullOrEmpty(outageVO.getNodeType())) {
            nodeQuery = nodeQuery + outageVO.getNodeId();
            Session session = getSession();
            if (session != null) {
                try {
                    StringBuffer q = new StringBuffer("select id from fixture ");
                    q.append(nodeQuery);
                    q.append(" and id in (select device_id from events_and_fault where active = 't')");
                    SQLQuery query = session.createSQLQuery(q.toString());
                    List idList = query.list();
                    List<Long> ids = new ArrayList<Long>();
                    for (Object object : idList) {
                        ids.add(Long.valueOf(object.toString()));
                    }
                    return session.createCriteria(Fixture.class).add(Restrictions.in("id", ids)).list();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Gateway> getFaultyGWByNode(OutageReportVO outageVO) {
        String nodeQuery = null;
        if (outageVO.getNodeType().equalsIgnoreCase("campus")) {
            nodeQuery = new String("where campus_id = ");
        } else if (outageVO.getNodeType().equalsIgnoreCase("building")) {
            nodeQuery = new String("where building_id = ");
        } else if (outageVO.getNodeType().equalsIgnoreCase("floor")) {
            nodeQuery = new String("where floor_id = ");
        } else if (outageVO.getNodeType().equalsIgnoreCase("area")) {
            nodeQuery = new String("where area_id = ");
        }
        if (!ArgumentUtils.isNullOrEmpty(outageVO.getNodeType())) {
            nodeQuery = nodeQuery + outageVO.getNodeId();
            Session session = getSession();
            if (session != null) {
                try {
                    StringBuffer q = new StringBuffer("select id from gateway ");
                    q.append(nodeQuery);
                    q.append(" and id in (select device_id from events_and_fault where active = 't')");
                    SQLQuery query = session.createSQLQuery(q.toString());
                    List idList = query.list();
                    List<Long> ids = new ArrayList<Long>();
                    for (Object object : idList) {
                        ids.add(Long.valueOf(object.toString()));
                    }
                    return session.createCriteria(Gateway.class).add(Restrictions.in("id", ids)).list();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public void saveOrUpdateEvent(EventsAndFault event) {
        try {
            getSession().saveOrUpdate(event);
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }

    }

}
