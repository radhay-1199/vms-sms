package vms.se.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import vms.se.bean.AccountTxRequest;

@Repository
public class AccountRequestRepository {

	private final Logger log = LoggerFactory.getLogger(getClass());
	@Autowired
	private JdbcTemplate jdbc;

	public List<AccountTxRequest> getPendingRequest() {
		String query = "select * from vms_charging_request where next_retry_time < now()";
		log.info(query);
		return jdbc.query(query, reqDataMapper);
	}

	public int deleteRequest(int id) {
		String query = "delete from vms_charging_request where id=" + id;
		log.info(query);
		return jdbc.update(query);
	}

	public int updateStatus(int id, String remark, Date nextRetryTime) {
		String query = "update vms_charging_request set retry_counter = retry_counter + 1 , remark = ? , next_retry_time = ? where id  = ? ";
		return jdbc.update(query, new Object[] { remark, new java.sql.Timestamp(nextRetryTime.getTime()), id });
	}

	private final RowMapper<AccountTxRequest> reqDataMapper = new RowMapper<AccountTxRequest>() {
		public AccountTxRequest mapRow(ResultSet rs, int i) throws SQLException {
			AccountTxRequest rec = new AccountTxRequest();
			rec.setId(rs.getInt("id"));
			rec.setMsisdn(rs.getString("msisdn"));
			rec.setStatus(rs.getInt("status"));
			rec.setAction(rs.getInt("action"));
			rec.setAmount(rs.getInt("amount"));
			rec.setTid(rs.getString("tid"));
			rec.setRetryCounter(rs.getInt("retry_counter"));
			rec.setPackId(rs.getString("pack_id"));
			rec.setChannel(rs.getString("channel"));
			return rec;
		}
	};
}
