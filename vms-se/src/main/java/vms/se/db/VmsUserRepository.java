package vms.se.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

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

	public int updateHLRStatus(String msisdn, int status) {

		String query = "update vms_users set hlr_flag = " + status + " where msisdn='" + msisdn + "'";
		log.info(query);
		return jdbc.update(query);

	}

	public int updateServiceStatus(String msisdn, int status) {
		String query = "update vms_users set status = " + status + " where msisdn='" + msisdn + "'";
		log.info(query);
		return jdbc.update(query);
	}

	public int deleteUser(String msisdn) {
		String query = "delete from vms_users where msisdn='" + msisdn + "'";
		log.info(query);
		return jdbc.update(query);

	}

	public VmsUser getUserDetails(String msisdn) {
		try {
			String query = "select * from vms_users where msisdn='" + msisdn + "'";
			log.debug(query);
			return jdbc.queryForObject(query, reqDataMapper);

		} catch (EmptyResultDataAccessException empty) {
			return null;
		}
	}

	public List<VmsUser> getUsersForRenewal() {
		try {

			String query = "select * from vms_users where status = 1 and next_renewal_date <= ? ";
			log.debug(query);
			return jdbc.query(query, new Object[] { new Date() }, reqDataMapper);

		} catch (EmptyResultDataAccessException empty) {
			return null;
		}
	}

	public int insertIntoUsers(String msisdn, String packId, Date nextRenewal, String lang, String channel) {

		String query = "insert into vms_users ( msisdn , status , pack_id , next_renewal_date , hlr_flag , lang_code , channel ) values (?,?,?,?,?,? , ?)";
		log.info(query);
		Object[] input = new Object[7];
		input[0] = msisdn;
		input[1] = 1;
		input[2] = packId;
		input[3] = nextRenewal;
		input[4] = 0;
		input[5] = lang;
		input[6] = channel;

		return jdbc.update(query, input);
	}

	public int updateValidityAfterRenewal(String msisdn, Date nextDate) {
		String query = "update vms_users set status = 1 , next_renewal_date = ? where msisdn = ?";
		log.info(query);

		Object[] input = new Object[2];
		input[0] = nextDate;
		input[1] = msisdn;

		int count = jdbc.update(query, input);
		log.info("Renewal Update | msisdn =" + msisdn + ",next RenewalDate=" + nextDate.toString() + ",update count="
				+ count);

		return count;

	}

	private final RowMapper<VmsUser> reqDataMapper = new RowMapper<VmsUser>() {
		public VmsUser mapRow(ResultSet rs, int i) throws SQLException {
			VmsUser rec = new VmsUser();

			rec.setMsisdn(rs.getString("msisdn"));
			rec.setStatus(rs.getInt("status"));
			rec.setPackId(rs.getString("pack_id"));
			rec.setHlrFlag(rs.getInt("hlr_flag"));
			rec.setNextRenewalDate(rs.getDate("next_renewal_date"));
			rec.setChannel(rs.getString("channel"));
			return rec;
		}
	};

}
