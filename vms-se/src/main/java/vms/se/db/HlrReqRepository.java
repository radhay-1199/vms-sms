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

import vms.se.bean.HLRRequest;

@Repository
public class HlrReqRepository {

	private final Logger log = LoggerFactory.getLogger(getClass());
	@Autowired
	private JdbcTemplate jdbc;

	public List<HLRRequest> getPendingRequest() {
		String query = "select * from vms_hlr_request where status =0";
		log.info(query);
		return jdbc.query(query, reqDataMapper);

	}

	public int updateStatus(String msisdn, String remark, Date nextRetryTime) {
		String query = "update vms_hlr_request set retry_counter = retry_counter + 1 , remark = ? , next_retry_time = ? where msisdn  = ? ";
		return jdbc.update(query, new Object[] { remark, new java.sql.Timestamp(nextRetryTime.getTime()), msisdn });
	}

	public int deleteRequest(String msisdn, int action) {
		String query = "delete from vms_hlr_request where msisdn='" + msisdn + "' and action = " + action;
		log.info(query);
		return jdbc.update(query);
	}

	public int insertIntoHlrRequest(String msisdn, String packId, int action , String channel) {
		String query = "insert into vms_hlr_request( msisdn , action , req_time , next_retry_time , pack_id , channel ) values (? ,? ,? , ? , ? , ?) ";
		log.info(query);
		return jdbc.update(query, new Object[] { msisdn, action, new java.sql.Timestamp(System.currentTimeMillis()),
				new java.sql.Timestamp(System.currentTimeMillis()), packId , channel });

	}

	private final RowMapper<HLRRequest> reqDataMapper = new RowMapper<HLRRequest>() {
		public HLRRequest mapRow(ResultSet rs, int i) throws SQLException {
			HLRRequest rec = new HLRRequest();
			rec.setMsisdn(rs.getString("msisdn"));
			rec.setStatus(rs.getInt("status"));
			rec.setAction(rs.getInt("action"));
			rec.setRetryCounter(rs.getInt("retry_counter"));
			rec.setPackId(rs.getString("pack_id"));
			rec.setChannel(rs.getString("channel"));
			return rec;
			
		}
	};

}
