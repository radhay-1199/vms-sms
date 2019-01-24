package vms.se.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import vms.se.bean.PackDetails;

@Repository
public class PackDetailRepository {

	private final Logger log = LoggerFactory.getLogger(getClass());
	@Autowired
	private JdbcTemplate jdbc;

	public PackDetails getPackDetails(String packId) {
		String query = "select * from vms_packs where pack_id = '" + packId +"'";
		log.info(query);
		return jdbc.queryForObject(query, reqDataMapper);
		
	}

	private final RowMapper<PackDetails> reqDataMapper = new RowMapper<PackDetails>() {
		public PackDetails mapRow(ResultSet rs, int i) throws SQLException {
			PackDetails rec = new PackDetails();
			
			rec.setPackId(rs.getString("pack_id"));
			rec.setPrice(rs.getInt("pack_price"));
			rec.setValidityDays(rs.getInt("pack_validity"));
			
			rec.setSerialNo(rs.getString("cbs_serialno"));
			rec.setRemark(rs.getString("cbs_remark"));
			rec.setName(rs.getString("pack_name"));
			
			return rec;
		}
	};

}
