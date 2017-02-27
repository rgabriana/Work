package com.emscloud.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.util.DatabaseUtil;
import com.emscloud.vo.SiteReportVo;

@Repository("inventoryReportDao")
@Transactional(propagation = Propagation.REQUIRED)
public class InventoryReportDao {
	
	static final Logger logger = Logger.getLogger(InventoryReportDao.class.getName());
	
	@Resource 
	SessionFactory sessionFactory;	

    private Connection getDbConnection(String dbName, String replicaServer) {
        
        String dbUser = "postgres";
        String dbPassword = "postgres";
        
        Connection connection = null;       
        if(dbName == null) {
            return null;
        }
        try {
            String conString = "jdbc:postgresql://" + replicaServer + ":" + DatabaseUtil.port + "/" + dbName +  "?characterEncoding=utf-8";
            connection = DriverManager.getConnection(conString, dbUser, dbPassword);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return connection;
        
    } //end of method getDbConnection
    
    public SiteReportVo getAggregatedInventoryReport(String dbName, String replicaServerHost) {
        
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        if(dbName == null) {
            return null;
        }
        try {
            connection = getDbConnection(dbName, replicaServerHost);
            stmt = connection.createStatement();
            
            String query = "SELECT sum(f.no_of_fixtures) AS fixtureCount, "
            		+ " count(f.id) as sensorCount, "
            		+ " count(distinct f.gateway_id) as gatewayCount, "
            		+ " sum(f.no_of_fixtures) as ballastCount, "
            		+ " sum(f.no_of_fixtures * f.no_of_bulbs) as bulbCount, "
            		+ " count(f.no_of_fixtures) as cuCount "
                    + " FROM fixture f WHERE f.state='COMMISSIONED'"; 
            rs = stmt.executeQuery(query);      
            SiteReportVo siteReportVo = null;
            if(rs.next()) {
                siteReportVo = new SiteReportVo();
                siteReportVo.setBallastCount(rs.getLong("ballastCount"));
                siteReportVo.setFixtureCount(rs.getLong("fixtureCount"));
                siteReportVo.setSensorCount(rs.getLong("sensorCount"));
                siteReportVo.setCuCount(rs.getLong("cuCount"));
                siteReportVo.setGatewayCount(rs.getLong("gatewayCount"));
                siteReportVo.setLampsCount(rs.getLong("bulbCount"));
                siteReportVo.setFxTypeCount(0L);
                logger.info("Fixture Count : "+ siteReportVo.getFixtureCount() +"\n SensorCount "+siteReportVo.getSensorCount() + "\n GatewayCount"+siteReportVo.getGatewayCount() + "\n BallastCount "+ siteReportVo.getBallastCount()+"\n BulbCount"+siteReportVo.getLampsCount());
                return siteReportVo;
            }
            if(siteReportVo==null)
            {
                logger.debug("getAggregatedInventoryReport() returned null while fetching aggregated Count of data of EMInstance :" + replicaServerHost + "from DB :"+dbName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(stmt);
            DatabaseUtil.closeConnection(connection);
        }
        logger.debug("Connection Failed to Established to database "+dbName);
        return null;
        
    } //end of method getAggregatedInventoryReport
    
    /**
     * Name = Value
     * @param dbName
     * @param replicaServerHost
     * @return
     */
    public List<Object[]> getFixturesCountByModelNo(String dbName, String replicaServerHost)
    {
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        if(dbName == null) {
            return null;
        }
        try {
            connection = getDbConnection(dbName, replicaServerHost);
            stmt = connection.createStatement();
            String query = "select distinct d.model_no as modelNo, count(d.id) as fixtureCount from device d join fixture f on f.id=d.id where d.type ='Fixture' and f.state='COMMISSIONED' group by d.model_no";
            rs = stmt.executeQuery(query);
            List<Object[]> fxList=new ArrayList<Object[]>();
            while(rs.next()) {
                
                Object[] fxObj = new Object[2];
                fxObj[0] = rs.getString("modelNo");
                fxObj[1] = rs.getLong("fixtureCount");
                logger.info("Fixture Count : "+ fxObj[1] +"\n modelNo "+fxObj[0] +"\r\n");
                fxList.add(fxObj);
            }
            if(fxList==null && fxList.size()==0)
            {
                logger.debug("getFixturesCountByModelNo() returned null while fetching aggregated Count of data of EMInstance :" + replicaServerHost + "from DB :"+dbName);
            }
            return fxList;    
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(stmt);
            DatabaseUtil.closeConnection(connection);
        }
        logger.debug("Connection Failed to Established to database "+dbName);
        return null;
    }
    
    /**
     * Name = value
     * @param dbName
     * @param replicaServerHost
     * @return
     */
    public List<Object[]> getCusCountByVersionNo(String dbName, String replicaServerHost)
    {
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        if(dbName == null) {
            return null;
        }
        try {
            connection = getDbConnection(dbName, replicaServerHost);
            stmt = connection.createStatement();
            String query = "select distinct f.cu_version as CUVersion, count(f.id) as FxCount from fixture f where f.state='COMMISSIONED' group by f.cu_version";
            rs = stmt.executeQuery(query);
            List<Object[]> cuList=new ArrayList<Object[]>();
            while(rs.next()) {
                Object[] cuObj = new Object[2];
                cuObj[0] = rs.getString("CUVersion");
                cuObj[1] = rs.getLong("FxCount");
                logger.info("CUVersion : "+ cuObj[1] +"\n FxCount "+cuObj[0]+"\r\n");
                cuList.add(cuObj);
            }
            if(cuList==null && cuList.size()==0)
            {
                logger.debug("getCusCountByVersionNo() returned null while fetching aggregated Count of data of EMInstance :" + replicaServerHost + "from DB :"+dbName);
            }
            return cuList;  
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(stmt);
            DatabaseUtil.closeConnection(connection);
        }
        logger.debug("Connection Failed to Established to database "+dbName);
        return null;
    }
    
    public List<Object[]> getBallastCountByBallastName(String dbName, String replicaServerHost)
    {
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        if(dbName == null) {
            return null;
        }
        try {
            connection = getDbConnection(dbName, replicaServerHost);
            stmt = connection.createStatement();
            String query = "select cast(b.ballast_name as varchar) as ballastName, cast(b.manufacturer as varchar) as manufacturer, b.baseline_load as baselineLoad,f.no_of_fixtures * count(f.id) as ballastCount, count(f.id) as fxcount from fixture f join ballasts b on b.id=f.ballast_id where f.state='COMMISSIONED' group by cast(b.ballast_name as varchar), cast(b.manufacturer as varchar), b.baseline_load, f.no_of_fixtures order by cast(b.manufacturer as varchar)";
            rs = stmt.executeQuery(query);
            List<Object[]> ballastList = new ArrayList<Object[]>();
            while(rs.next()) {
                Object[] ballastObj = new Object[5];
                ballastObj[0] = rs.getString("ballastName");
                ballastObj[1] = rs.getString("manufacturer");
                ballastObj[2] =  rs.getLong("ballastCount");
                ballastObj[3] =  rs.getLong("fxcount");
                ballastObj[4] =  rs.getBigDecimal("baselineLoad");
                logger.info("ballastName : "+ ballastObj[0] +"\n manufacturer "+ballastObj[1] + "\n baselineLoad "+ballastObj[4] +"\n ballastCount"+ballastObj[2] +"\n fxcount"+ ballastObj[3] +  "\r\n");
                ballastList.add(ballastObj);
            }
            if(ballastList==null && ballastList.size()==0)
            {
                logger.debug("getCusCountByVersionNo() returned null while fetching aggregated Count of data of EMInstance :" + replicaServerHost + "from DB :"+dbName);
            }
            return ballastList;  
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(stmt);
            DatabaseUtil.closeConnection(connection);
        }
        logger.debug("Connection Failed to Established to database "+dbName);
        return null;
    }

    /**
     * Name = Value
     * @param dbName
     * @param replicaServerHost
     * @return
     */
	public List<Object[]> getBallastCountByBallastDisplayName(String dbName,
			String replicaServerHost) {
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		if (dbName == null) {
			return null;
		}
		try {
			connection = getDbConnection(dbName, replicaServerHost);
			stmt = connection.createStatement();
			String query = "select cast(b.display_label as varchar) as ballastName, f.no_of_fixtures * count(f.id) as ballastCount from fixture f join ballasts b on b.id=f.ballast_id where f.state='COMMISSIONED' group by cast(b.display_label as varchar), f.no_of_fixtures order by cast(b.display_label as varchar)";
			rs = stmt.executeQuery(query);
			List<Object[]> ballastList = new ArrayList<Object[]>();
			while (rs.next()) {
				Object[] ballastObj = new Object[2];
				String ballastDisplayName = rs.getString("ballastName");
				ballastDisplayName = ballastDisplayName.replaceAll(",", " ");
				ballastObj[0] = ballastDisplayName;
				ballastObj[1] = rs.getLong("ballastCount");
				logger.info("ballastName : " + ballastObj[0]
						+ "\n ballastCount" + ballastObj[1] + "\r\n");
				ballastList.add(ballastObj);
			}
			if (ballastList == null && ballastList.size() == 0) {
				logger.debug("getCusCountByVersionNo() returned null while fetching aggregated Count of data of EMInstance :"
						+ replicaServerHost + "from DB :" + dbName);
			}
			return ballastList;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeConnection(connection);
		}
		logger.debug("Connection Failed to Established to database " + dbName);
		return null;
	}

    public List<Object[]> getBulbsCountByBulbName(String dbName, String replicaServerHost)
    {
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        if(dbName == null) {
            return null;
        }
        try {
            connection = getDbConnection(dbName, replicaServerHost);
            stmt = connection.createStatement();
            String query = "select cast(bb.bulb_name as varchar) as bulbName,cast(bb.manufacturer as varchar) as manufacturer, sum(f.no_of_bulbs * f.no_of_fixtures) as bulbCount, count(f.id) as fxcount from fixture f join bulbs bb on bb.id=f.bulb_id where f.state='COMMISSIONED' group by cast(bb.bulb_name as varchar),cast(bb.manufacturer as varchar) order by cast(bb.manufacturer as varchar)";
            rs = stmt.executeQuery(query);
            List<Object[]> bulbList = new ArrayList<Object[]>();
            while(rs.next()) {
                Object[] bulbObj = new Object[4];
                bulbObj[0] = rs.getString("bulbName");
                bulbObj[1] = rs.getString("manufacturer");
                bulbObj[2] =  rs.getLong("bulbCount");
                bulbObj[3] =  rs.getLong("fxcount");
                logger.info("bulbName : "+ bulbObj[0] +"\n manufacturer "+bulbObj[1] + "\n bulbCount"+bulbObj[2] +"\n fxcount"+ bulbObj[3] +  "\r\n");
                bulbList.add(bulbObj);
            }
            if(bulbList==null && bulbList.size()==0)
            {
                logger.debug("getCusCountByVersionNo() returned null while fetching aggregated Count of data of EMInstance :" + replicaServerHost + "from DB :"+dbName);
            }
            return bulbList;  
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(stmt);
            DatabaseUtil.closeConnection(connection);
        }
        logger.debug("Connection Failed to Established to database "+dbName);
        return null;
    }

    public List<Object[]> getCommissionedFxTypeCount(String dbName, String replicaServerHost)
    {
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        if(dbName == null) {
            return null;
        }
        try {
            connection = getDbConnection(dbName, replicaServerHost);
            stmt = connection.createStatement();
            String query = "select count(f.id) as fxCount, f.fixture_class_id as fixtureClassId,cast(fc.name as varchar) as fixtureClassName,cast(b.display_label as varchar) as displayLabel from fixture f join fixture_class fc on f.fixture_class_id=fc.id  join ballasts b on fc.ballast_id =b.id " +
                "where f.state='COMMISSIONED' group by f.fixture_class_id, cast(fc.name as varchar), cast(b.display_label as varchar) order by f.fixture_class_id";
            rs = stmt.executeQuery(query);
            List<Object[]> fxTypeList = new ArrayList<Object[]>();
            while(rs.next()) {
                Object[] fxTypeObj = new Object[4];
                fxTypeObj[0] = rs.getLong("fxCount");
                fxTypeObj[1] = rs.getLong("fixtureClassId");
                fxTypeObj[2] = rs.getString("fixtureClassName");
                fxTypeObj[3] =  rs.getString("displayLabel");
                logger.info("fxCount : "+ fxTypeObj[0] +"\n fixtureClassId "+fxTypeObj[1] + "\n fixtureClassName"+fxTypeObj[2] +"\n displayLabel"+ fxTypeObj[3] +  "\r\n");
                fxTypeList.add(fxTypeObj);
            }
            if(fxTypeList==null && fxTypeList.size()==0)
            {
                logger.debug("getCusCountByVersionNo() returned null while fetching aggregated Count of data of EMInstance :" + replicaServerHost + "from DB :"+dbName);
            }
            return fxTypeList;  
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(stmt);
            DatabaseUtil.closeConnection(connection);
        }
        logger.debug("Connection Failed to Established to database "+dbName);
        return null;
    }
    
    public List<Object[]> getGatewayCount(String dbName, String replicaServerHost)
    {
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        if(dbName == null) {
            return null;
        }
        try {
            connection = getDbConnection(dbName, replicaServerHost);
            stmt = connection.createStatement();
            String query = "select sum(f.no_of_fixtures) AS fixtureCount, count(distinct f.gateway_id) as gatewayCount FROM fixture f WHERE f.state='COMMISSIONED'";
            rs = stmt.executeQuery(query);
            List<Object[]> oList = new ArrayList<Object[]>();
            while(rs.next()) {
                Object[] oObj = new Object[2];
                oObj[0] = rs.getLong("fixtureCount");
                oObj[1] = rs.getLong("gatewayCount");
                oList.add(oObj);
            }
            if(oList==null && oList.size()==0)
            {
                logger.debug("getGatewayCount() returned null while fetching aggregated Count of data of EMInstance :" + replicaServerHost + "from DB :"+dbName);
            }
            return oList;  
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(stmt);
            DatabaseUtil.closeConnection(connection);
        }
        logger.debug("Connection Failed to Established to database "+dbName);
        return null;
    }

} //end of class InventoryReportDao
