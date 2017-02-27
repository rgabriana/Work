package com.ems.dao;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Ballast;
import com.ems.model.BallastList;
import com.ems.utils.ArgumentUtils;

@Repository("ballastDao")
@Transactional(propagation = Propagation.REQUIRED)
public class BallastDao extends BaseDaoHibernate{
	
	public Ballast addBallast(Ballast ballast) {
				
		String bulbStr = "bulbs";
		if(ballast.getLampNum() == 1) {
			bulbStr = "bulb";
		}
		
		String displayLabel = ballast.getDisplayLabel().trim();
		
		if(displayLabel == null || "".equals(displayLabel)){
			displayLabel = ballast.getBallastName() + "(" + ballast.getBallastManufacturer() + "," + ballast.getLampType() + "," + ballast.getWattage() + "W," + ballast.getLampNum() + " " + bulbStr + ")";
		}
		
		ballast.setDisplayLabel(displayLabel);
		
		ballast.setVoltPowerMapId(new Long(1)); // default value
		
		ballast.setIsDefault(new Integer(0));  // default value
		
		return(Ballast)saveObject(ballast);
	}
	
	public void editBallast(Ballast ballast) {
		
		String bulbStr = "bulbs";
		if(ballast.getLampNum() == 1) {
			bulbStr = "bulb";
		}
		
		String displayLabel = ballast.getDisplayLabel().trim();
		
		if(displayLabel == null || "".equals(displayLabel)){
			displayLabel = ballast.getBallastName() + "(" + ballast.getBallastManufacturer() + "," + ballast.getLampType() + "," + ballast.getWattage() + "W," + ballast.getLampNum() + " " + bulbStr + ")";
		}
		
		ballast.setDisplayLabel(displayLabel);
		saveObject(ballast);
		
	}
	
	
	@SuppressWarnings("unchecked")
	public BallastList loadBallastList(String order,
			String orderway, Boolean bSearch, String searchField,
			String searchString, String searchOper, int offset, int limit) {
		BallastList ballastList = new BallastList();
		Criteria oCriteria = null;
		Criteria oRowCount = null;

		oRowCount = sessionFactory.getCurrentSession().createCriteria(
				Ballast.class, "ballast").setProjection(Projections.rowCount());
		oCriteria = sessionFactory.getCurrentSession().createCriteria(
				Ballast.class, "ballast");
		if (bSearch) {
			if (searchField.equals("id")) {
				try {
					oRowCount.add(Restrictions.eq("ballast.id", Long
							.parseLong(searchString)));
					oCriteria.add(Restrictions.eq("ballast.id", Long
							.parseLong(searchString)));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					ballastList.setTotal(0L);
					return ballastList;
				}
			} else if (searchField.equals("displayLabel")) {
				oRowCount.add(Restrictions.like("ballast.displayLabel", "%"
						+ searchString + "%"));
				oCriteria.add(Restrictions.like("ballast.displayLabel", "%"
						+ searchString + "%"));
			} else if (searchField.equals("bulbType")) {
				oRowCount
						.add(Restrictions.eq("ballast.lampType", searchString));
				oCriteria
						.add(Restrictions.eq("ballast.lampType", searchString));
			} else if (searchField.equals("noOfBulbs")) {
				oRowCount.add(Restrictions.eq("ballast.lampNum", Integer
						.parseInt(searchString)));
				oCriteria.add(Restrictions.eq("ballast.lampNum", Integer
						.parseInt(searchString)));
			} else if (searchField.equals("ballastFactor")) {
				oRowCount.add(Restrictions.eq("ballast.ballastFactor", Double
						.parseDouble(searchString)));
				oCriteria.add(Restrictions.eq("ballast.ballastFactor", Double
						.parseDouble(searchString)));
			} else if (searchField.equals("bulbWattage")) {
				oRowCount.add(Restrictions.eq("ballast.wattage", Integer
						.parseInt(searchString)));
				oCriteria.add(Restrictions.eq("ballast.wattage", Integer
						.parseInt(searchString)));
			} else if (searchField.equals("ballastManufacturer")) {
				oRowCount.add(Restrictions.like("ballast.ballastManufacturer",
						"%" + searchString + "%"));
				oCriteria.add(Restrictions.like("ballast.ballastManufacturer",
						"%" + searchString + "%"));
			} else if (searchField.equals("baselineLoad")) {
				oRowCount.add(Restrictions.eq("ballast.baselineLoad",
						BigDecimal.valueOf(Double.valueOf(searchString))));
				oCriteria.add(Restrictions.eq("ballast.baselineLoad",
						BigDecimal.valueOf(Double.valueOf(searchString))));
			} else if (searchField.equals("name")) {
				oRowCount.add(Restrictions.like("ballast.ballastName", "%"
						+ searchString + "%"));
				oCriteria.add(Restrictions.like("ballast.ballastName", "%"
						+ searchString + "%"));
			} else if (searchField.equals("inputvolt")) {
				try {
					oRowCount.add(Restrictions.sqlRestriction("{alias}.id in (select distinct ballast_id from ballast_volt_power where inputvolt="+Double.parseDouble(searchString)+")"));
					oCriteria.add(Restrictions.sqlRestriction("{alias}.id in (select distinct ballast_id from ballast_volt_power where inputvolt="+Double.parseDouble(searchString)+")"));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block	
					ballastList.setTotal(0L);
					return ballastList;
				}
			}
		}
		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}

		if ("desc".equals(orderway)) {
			oCriteria.addOrder(Order.desc("ballast.ballastName"));
		} else {
			oCriteria.addOrder(Order.asc("ballast.ballastName"));
		}

		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		if (count.compareTo(new Long("0")) > 0) {
			ballastList.setTotal(count);
			ballastList.setBallasts(oCriteria.list());
			return ballastList;
		}

		return ballastList;
	
	}
	
	public void deleteBallastById(Long id)
	{
		String hsql = "delete from Ballast where id=?";
        Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
        q.setParameter(0, id);
		q.executeUpdate();
	}
	
	@SuppressWarnings("unchecked")
	public Ballast getBallastById(Long id) {
        List<Ballast> ballastList = getSession().createCriteria(Ballast.class)
                .add(Restrictions.eq("id", id)).list();
        if(ballastList.size() > 0) {
        	return ballastList.get(0);
        }
        
        return new Ballast();
    }
		
	@SuppressWarnings("unchecked")
	public Ballast getBallastByDisplayLabel(String displayLabel){
		List<Ballast> ballastList = getSession().createCriteria(Ballast.class)
        			.add(Restrictions.eq("displayLabel", displayLabel)).list();
		if (!ArgumentUtils.isNullOrEmpty(ballastList)) {
            return ballastList.get(0);
        } else {
            return null;
        }
	}
	
	@SuppressWarnings("unchecked")
	public Ballast getBallastByName(String name){
		List<Ballast> ballastList = getSession().createCriteria(Ballast.class)
        			.add(Restrictions.eq("ballastName", name)).list();
		if (!ArgumentUtils.isNullOrEmpty(ballastList)) {
            return ballastList.get(0);
        } else {
            return null;
        }
	}

	@SuppressWarnings("unchecked")
	public List<Ballast> getAllBallasts() {
        List<Ballast> ballastList = getSession().createCriteria(Ballast.class)
        							.addOrder(Order.asc("ballastName")).list();
        if (!ArgumentUtils.isNullOrEmpty(ballastList)) {
            return ballastList;
        } else {
            return null;
        }
    }
	
	@SuppressWarnings("unchecked")
	public BallastList loadBallastListByUsage(String order,
			String orderway, Boolean bSearch, String searchField,
			String searchString, String searchOper, int offset, int limit) {
		BallastList ballastList = new BallastList();		
	
		Criteria oCriteria = null;
		Criteria oRowCount = null;
		
		oRowCount = sessionFactory.getCurrentSession().createCriteria(Ballast.class, "ballast").setProjection(Projections.rowCount());
		oCriteria = sessionFactory.getCurrentSession().createCriteria(Ballast.class, "ballast");
		
		oRowCount.add(Restrictions.sqlRestriction("{alias}.id in (select ballast_id from fixture f join ballasts b on f.ballast_id=b.id where f.state='COMMISSIONED' group by f.ballast_id order by f.ballast_id)"));
		oCriteria.add(Restrictions.sqlRestriction("{alias}.id in (select ballast_id from fixture f join ballasts b on f.ballast_id=b.id where f.state='COMMISSIONED' group by f.ballast_id order by f.ballast_id)"));
		 if(bSearch)
		  {
			  	if (searchField.equals("id")) {
				try {
					oRowCount.add(Restrictions.eq("ballast.id",
							Long.parseLong(searchString)));
					oCriteria.add(Restrictions.eq("ballast.id",
							Long.parseLong(searchString)));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					ballastList.setTotal(0L);
					return ballastList;
				}
			  	}
			  	else if (searchField.equals("displayLabel")) {
					oRowCount.add(Restrictions.like("ballast.displayLabel","%"+
							searchString + "%"));
					oCriteria.add(Restrictions.like("ballast.displayLabel","%"+
							searchString+"%"));
				  	}
			  	else if (searchField.equals("bulbType")) {
					oRowCount.add(Restrictions.eq("ballast.lampType",
							searchString));
					oCriteria.add(Restrictions.eq("ballast.lampType",
							searchString));
				  	}
			  	else if (searchField.equals("noOfBulbs")) {
					oRowCount.add(Restrictions.eq("ballast.lampNum",
							Integer.parseInt(searchString)));
					oCriteria.add(Restrictions.eq("ballast.lampNum",
							Integer.parseInt(searchString)));
				  	}
			  	else if (searchField.equals("ballastFactor")) {
					oRowCount.add(Restrictions.eq("ballast.ballastFactor",
							Double.parseDouble(searchString)));
					oCriteria.add(Restrictions.eq("ballast.ballastFactor",
							Double.parseDouble(searchString)));
				  	}
			  	else if (searchField.equals("bulbWattage")) {
					oRowCount.add(Restrictions.eq("ballast.wattage",
							Integer.parseInt(searchString)));
					oCriteria.add(Restrictions.eq("ballast.wattage",
							Integer.parseInt(searchString)));
				  	}			  	
			  	else if (searchField.equals("ballastManufacturer")) {
					oRowCount.add(Restrictions.like("ballast.ballastManufacturer",
							"%"+searchString+"%"));
					oCriteria.add(Restrictions.like("ballast.ballastManufacturer",
							"%"+searchString+"%"));
				  	}
			  	else if (searchField.equals("baselineLoad")) {
					oRowCount.add(Restrictions.eq("ballast.baselineLoad",
							BigDecimal.valueOf(Double.valueOf(searchString))));
					oCriteria.add(Restrictions.eq("ballast.baselineLoad",
							BigDecimal.valueOf(Double.valueOf(searchString))));
				  	}
			  	else if (searchField.equals("name")) {
					oRowCount.add(Restrictions.like("ballast.ballastName",
							"%"+searchString+"%"));
					oCriteria.add(Restrictions.like("ballast.ballastName",
							"%"+searchString+"%"));
				  	}
			  	else if (searchField.equals("inputvolt")) {
					try {
						oRowCount.add(Restrictions.sqlRestriction("{alias}.id in (select distinct ballast_id from ballast_volt_power where inputvolt="+Double.parseDouble(searchString)+")"));
						oCriteria.add(Restrictions.sqlRestriction("{alias}.id in (select distinct ballast_id from ballast_volt_power where inputvolt="+Double.parseDouble(searchString)+")"));
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						ballastList.setTotal(0L);
						return ballastList;
					}
				}
		  }
		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}
		
		if ("desc".equals(orderway)) {
			oCriteria.addOrder(Order.desc("ballast.ballastName"));
		} 
		else {
			oCriteria.addOrder(Order.asc("ballast.ballastName"));
		}
		
		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		if (count.compareTo(new Long("0")) > 0) {
			ballastList.setTotal(count);
			ballastList.setBallasts(oCriteria.list());
			return ballastList;
		}
		
		return ballastList;	
		
	}
	
	
	
}
