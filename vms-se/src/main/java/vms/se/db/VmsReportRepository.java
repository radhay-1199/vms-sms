package vms.se.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import vms.se.bean.ReportData;

@Repository
public class VmsReportRepository {

	private final Logger log = LoggerFactory.getLogger(getClass());
	@Autowired
	private JdbcTemplate jdbc;

	public void insertIntoReports(ReportData report) {
		String query = "insert into vms_action_report ( msisdn , channel , action , status , remark, tid ) values ( ? , ? ,? ,? , ? , ? )";
		Object[] input = new Object[6];
		input[0] = report.getMsisdn();
		input[1] = report.getChannel();
		input[2] = report.getAction();
		input[3] = report.getStatus();
		input[4] = report.getRemark();
		input[5] = report.getTid();
		jdbc.update(query, input);
	}

}
