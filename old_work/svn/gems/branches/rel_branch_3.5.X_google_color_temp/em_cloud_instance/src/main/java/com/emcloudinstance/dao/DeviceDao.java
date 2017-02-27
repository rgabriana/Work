/**
 * 
 */
package com.emcloudinstance.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emcloudinstance.vo.Fixture;

/**
 * @author yogesh
 * 
 */
@Repository("deviceDao")
@Transactional(propagation = Propagation.REQUIRED)
public class DeviceDao extends AbstractJdbcDao {
	static final Logger logger = Logger.getLogger(DeviceDao.class.getName());

	public List<Fixture> loadFixtures(String mac, String property, Long pid,
			String limit) {
		String query = "SELECT f.id, d.name, d.x, d.y, d.mac_address, d.version, d.model_no, d.area_id, d.floor_id, d.building_id, d.campus_id from fixture f join device d on f.id=d.id";
		if (property.equalsIgnoreCase("floor")) {
			query = "SELECT f.id, d.name, d.x, d.y, d.mac_address, d.version, d.model_no, d.area_id, d.floor_id, d.building_id, d.campus_id from fixture f join device d on f.id=d.id WHERE d.floor_id="
					+ pid;
		} else if (property.equalsIgnoreCase("building")) {
			query = "SELECT f.id, d.name, d.x, d.y, d.mac_address, d.version, d.model_no, d.area_id, d.floor_id, d.building_id, d.campus_id from fixture f join device d on f.id=d.id WHERE d.building_id="
					+ pid;
		} else if (property.equalsIgnoreCase("campus")) {
			query = "SELECT f.id, d.name, d.x, d.y, d.mac_address, d.version, d.model_no, d.area_id, d.floor_id, d.building_id, d.campus_id from fixture f join device d on f.id=d.id WHERE d.campus_id="
					+ pid;
		}
		return loadFixtures(mac, query);
	}

	private List<Fixture> loadFixtures(String mac, String query) {
		JdbcTemplate jdbcTemplate = this.getJdbcTemplate(mac);
		List<Fixture> results = jdbcTemplate.query(query.toString(),
				new RowMapper<Fixture>() {
					@Override
					public Fixture mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						Fixture oFx = new Fixture();
						oFx.setId(rs.getLong("id"));
						oFx.setName(rs.getString("name"));
						oFx.setXaxis(rs.getInt("x"));
						oFx.setYaxis(rs.getInt("y"));
						oFx.setMacAddress(rs.getString("mac_address"));
						oFx.setVersion(rs.getString("version"));
						oFx.setModelNo(rs.getString("model_no"));
						oFx.setAreaId(rs.getLong("area_id"));
						oFx.setFloorId(rs.getLong("floor_id"));
						oFx.setBuildingId(rs.getLong("building_id"));
						oFx.setCampusId(rs.getLong("campus_id"));
						return oFx;
					}
				});
		return results;
	}

}
