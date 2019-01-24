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

import vms.se.bean.VmsRenewalRequest;

@Repository
public class VmsRenewalRepository {
	private final Logger log = LoggerFactory.getLogger(getClass());
	@Autowired
	private JdbcTemplate jdbc;

	public List<VmsRenewalRequest> getRequestList() {

		String query = "select * from vms_renewal_request where retry_time <= ?";
		log.info(query);
		return jdbc.query(query, new Object[] { new Date() }, reqDataMapper);

	}

	public int deleteRequest(String msisdn) {
		String query = "delete from vms_renewal_request where msisdn = '" + msisdn + "'";
		log.info(query);
		return jdbc.update(query);

	}

	public int insertIntoRenewalRequest(String msisdn, String packId, int retryCount, Date nextRetry) {
		String query = "insert into vms_renewal_request (msisdn , pack_id , retry_count , retry_time ) values(?,?,?,?)";
		Object[] input = new Object[4];
		input[0] = msisdn;
		input[1] = packId;
		input[2] = retryCount;
		input[3] = nextRetry;
		return jdbc.update(query, input);
	}

	public int updateNextRenewalRetry(String msisdn, Date nextRetry) {

		String query = "update vms_renewal_request set retry_time = ? , retry_count = retry_count + 1 where msisdn = ?";
		Object[] input = new Object[2];
		input[0] = nextRetry;
		input[1] = msisdn;
		return jdbc.update(query, input);

	}

	private final RowMapper<VmsRenewalRequest> reqDataMapper = new RowMapper<VmsRenewalRequest>() {
		public VmsRenewalRequest mapRow(ResultSet rs, int i) throws SQLException {
			VmsRenewalRequest rec = new VmsRenewalRequest();
			rec.setMsisdn(rs.getString("msisdn"));
			rec.setPackId(rs.getString("pack_id"));
			rec.setNextRetryTime(rs.getTimestamp("retry_time"));
			rec.setRetryCount(rs.getInt("retry_count"));
			rec.setChannel(rs.getString("channel"));

			return rec;
		}
	};

}
