package vms.se.db;

import java.sql.ResultSet;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import vms.se.bean.HLRRequest;

@Repository
public class HlrReqRepository {

	private final Logger log = LoggerFactory.getLogger(getClass());
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public List<HLRRequest> getPendingRequest() {
		String query = "select * from vms_hlr_request where status =0";
		log.info(query);
		return jdbcTemplate.query(query, reqDataMapper);

	}
	
	
	public int deleteRequest(String msisdn , int action) {
		String query = "delete from vms_hlr_request where msisdn='"+msisdn+"' and action = "+action;
		log.info(query);
		return jdbcTemplate.update(query);
	}
	

	private final RowMapper<HLRRequest> reqDataMapper = new RowMapper<HLRRequest>() {
		public HLRRequest mapRow(ResultSet rs, int i) throws SQLException {
			HLRRequest rec = new HLRRequest();
			rec.setMsisdn(rs.getString("msisdn"));
			rec.setStatus(rs.getInt("status"));
			rec.setAction(rs.getInt("action"));
			return rec;
		}
	};

}
