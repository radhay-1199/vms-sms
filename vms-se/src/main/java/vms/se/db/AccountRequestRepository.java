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

import vms.se.bean.AccountRequest;

@Repository
public class AccountRequestRepository {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	
	public List<AccountRequest> getPendingRequest() {
		String query = "select * from vms_charging_request where status =0" ;
		log.info(query);
		return jdbcTemplate.query( query, reqDataMapper);
	}
	
	public int deleteRequest(int id) {
		String query = "delete from vms_charging_request where id="+id ;
		log.info(query);
		return jdbcTemplate.update(query);
	}
	
	
	private final RowMapper<AccountRequest> reqDataMapper = new RowMapper<AccountRequest>() {
		public AccountRequest mapRow(ResultSet rs, int i) throws SQLException {
			AccountRequest rec = new AccountRequest();
			rec.setId(rs.getInt("id"));
			rec.setMsisdn(rs.getString("msisdn"));
			rec.setStatus(rs.getInt("status"));
			rec.setAction(rs.getInt("action"));
			rec.setAmount(rs.getInt("amount"));
			rec.setTid(rs.getString("tid"));
			return rec;
			
		}
	};
	
}
