package vms.se.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import vms.se.bean.VmsUser;

@Repository
public class VmsUserRepository {
	private final Logger log = LoggerFactory.getLogger(getClass());
	@Autowired
	private JdbcTemplate jdbc;

	public VmsUser getUserDetails(String msisdn) {
		try {
				String query = "select * from vms_users where msisdn='" + msisdn + "'"	;
				log.debug(query)	;
				return jdbc.queryForObject(query, reqDataMapper) ;
				
		} catch (EmptyResultDataAccessException empty) {
			return null;
		}
	}

	private final RowMapper<VmsUser> reqDataMapper = new RowMapper<VmsUser>() {
		public VmsUser mapRow(ResultSet rs, int i) throws SQLException {
			VmsUser rec = new VmsUser();

			rec.setMsisdn(rs.getString("msisdn"));
			rec.setStatus(rs.getInt("status"));
			rec.setPackId(rs.getString("pack_id"));
			rec.setHlrFlag(rs.getInt("hlr_flag"));
			rec.setNextRenewalDate(rs.getDate("next_renewal_date"));

			return rec;
		}
	};

}
