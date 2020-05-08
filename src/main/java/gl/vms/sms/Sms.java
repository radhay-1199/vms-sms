package gl.vms.sms;

import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gl.core.db.DataSource;
import gl.core.fw.StateInfo;
import gl.core.util.Utility;
import gl.vms.beans.PackDetails;
import gl.vms.config.SmsConfig;

public class Sms extends Utility {

	private SmsConfig smsConfig = new SmsConfig("sms.cfg");
	private Logger log = LogManager.getRootLogger();

	public void print(String msisdn, String event, StateInfo stateInfo) {
		log.info("LoggerPrint|msisdn=" + msisdn + "|Event=" + event + ",StateMessage=" + stateInfo.getEventMsg());
	}

	public void disableRenewal(String msisdn, String event, StateInfo stateInfo) {
		try {
			msisdn = msisdn.substring(msisdn.length() - 9);

			Connection con = DataSource.getConnection();
			boolean alreadySub = isSubscriber(msisdn, con);

			if (!alreadySub) {
				String url = SmsConfig.kannelURL;
				url = url.replaceAll("<TO>", "+93" + msisdn);
				url = url.replaceAll("<TEXT>", SmsConfig.alreayNonSubscriberMsg);
				log.info("SENDMSG=" + url);
				log.info(CallURL(url));
				return;
			}
			String query = "update vms_users set renewal_flag = 0 where msisdn='" + msisdn + "'";
			log.info(query);
			Statement st = con.createStatement();
			int updateCount = st.executeUpdate(query);
			log.info("Auto Renewal Stop for msisdn=" + msisdn + ",Record Update=" + updateCount);

			String url = SmsConfig.kannelURL;
			url = url.replaceAll("<TO>", "+93" + msisdn);
			url = url.replaceAll("<TEXT>", URLEncoder.encode(stateInfo.getEventMsg(), "UTF-8"));
			log.info("SENDMSG=" + url);
			log.info(CallURL(url));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void enableRenewal(String msisdn, String event, StateInfo stateInfo) {
		try {
			msisdn = msisdn.substring(msisdn.length() - 9);

			Connection con = DataSource.getConnection();
			boolean alreadySub = isSubscriber(msisdn, con);

			if (!alreadySub) {
				String url = SmsConfig.kannelURL;
				url = url.replaceAll("<TO>", "+93" + msisdn);
				url = url.replaceAll("<TEXT>", SmsConfig.alreayNonSubscriberMsg);
				log.info("SENDMSG=" + url);
				log.info(CallURL(url));
				return;
			}
			String query = "update vms_users set renewal_flag = 1 where msisdn='" + msisdn + "'";
			log.info(query);
			Statement st = con.createStatement();
			int updateCount = st.executeUpdate(query);
			log.info("Auto Renewal Stop for msisdn=" + msisdn + ",Record Update=" + updateCount);

			String url = SmsConfig.kannelURL;
			url = url.replaceAll("<TO>", "+93" + msisdn);
			url = url.replaceAll("<TEXT>", URLEncoder.encode(stateInfo.getEventMsg(), "UTF-8"));
			log.info("SENDMSG=" + url);
			log.info(CallURL(url));
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void sendSMS(String msisdn, String event, StateInfo stateInfo) {
		try {
			String smsMsisdn = null;
			if (msisdn.startsWith("+93")) {
				smsMsisdn = msisdn;
			} else
				smsMsisdn = "+93" + msisdn;

			log.info("LoggerPrint|msisdn=" + msisdn + "|Event=" + event + ",StateMessage=" + stateInfo.getEventMsg());
			String url = SmsConfig.kannelURL;
			url = url.replaceAll("<TO>", smsMsisdn);
			url = url.replaceAll("<TEXT>", URLEncoder.encode(stateInfo.getEventMsg(), "UTF-8"));
			log.info("SENDMSG=" + url);
			log.info(CallURL(url));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addInUnSubQueue(String msisdn, String event, StateInfo stateInfo) {
		log.info("UnSub Request For =" + msisdn);
		try {

			String smsMsisdn = null;
			if (msisdn.startsWith("+93")) {
				smsMsisdn = msisdn;
			} else
				smsMsisdn = "+93" + msisdn;

			Connection con = DataSource.getConnection();
			boolean alreadySub = isSubscriber(msisdn, con);

			if (!alreadySub) {

				String url = SmsConfig.kannelURL;
				url = url.replaceAll("<TO>", smsMsisdn);
				url = url.replaceAll("<TEXT>", SmsConfig.alreayNonSubscriberMsg);
				log.info("SENDMSG=" + url);
				log.info(CallURL(url));
				return;

			}

			String tid = "SE-" + System.currentTimeMillis();
			String query = "insert into vms_charging_request(msisdn,action,req_time,tid) values (?,?,?,?)";

			PreparedStatement ps = con.prepareStatement(query);
			ps.setString(1, msisdn);
			ps.setInt(2, 3);
			ps.setString(3, getDate());
			ps.setString(4, tid);

			int update = ps.executeUpdate();
			log.info("Request Added for msisdn=" + msisdn + ",Tid=" + tid + ",action=1|update=" + update);
			String url1 = SmsConfig.kannelURL;
			url1 = url1.replaceAll("<TO>", smsMsisdn);
			url1 = url1.replaceAll("<TEXT>", SmsConfig.unSubReq);
			log.info("SENDMSG=" + url1);
			log.info(CallURL(url1));
			ps.close();
			con.close();

		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	public void subDailyPack(String msisdn, String event, StateInfo stateInfo) {
		addInSubQueue(msisdn, event, stateInfo, "P1");
	}

	public void subWeeklyPack(String msisdn, String event, StateInfo stateInfo) {
		addInSubQueue(msisdn, event, stateInfo, "P2");
	}

	public void subMonthlyPack(String msisdn, String event, StateInfo stateInfo) {
		addInSubQueue(msisdn, event, stateInfo, "P3");
	}

	/*
	 * public void addInSubQueue(String msisdn, String event, StateInfo stateInfo,
	 * String packId) { try {
	 * 
	 * String smsMsisdn = null;
	 * 
	 * if (msisdn.startsWith("+93")) { smsMsisdn = msisdn; } else smsMsisdn = "+93"
	 * + msisdn;
	 * 
	 * Connection con = DataSource.getConnection();
	 * 
	 * boolean alreadySub = isSubscriber(msisdn, con); if (alreadySub) { String url
	 * = SmsConfig.kannelURL; url = url.replaceAll("<TO>", smsMsisdn); url =
	 * url.replaceAll("<TEXT>", SmsConfig.existingSubscriberMsg);
	 * log.info("SENDMSG=" + url); log.info(CallURL(url)); return; }
	 * 
	 * boolean reqExist = isPending(msisdn, con); if (reqExist) { String url =
	 * SmsConfig.kannelURL; url = url.replaceAll("<TO>", smsMsisdn); url =
	 * url.replaceAll("<TEXT>", SmsConfig.pendingReqMsg); log.info("SENDMSG=" +
	 * url); log.info(CallURL(url)); return; }
	 * 
	 * PackDetails pack = getPackDetails(packId, con); if (pack == null) {
	 * log.info("Pack Details not found"); return; }
	 * 
	 * String tid = "SE-" + System.currentTimeMillis(); String query =
	 * "insert into vms_charging_request( msisdn,action,req_time,amount,pack_id,tid,next_retry_time) values (?,?,?,?,?,?,?) "
	 * ; PreparedStatement ps = con.prepareStatement(query);
	 * 
	 * ps.setString(1, msisdn); ps.setInt(2, 1); ps.setString(3, getDate());
	 * ps.setInt(4, pack.getPrice()); ps.setString(5, pack.getPackId());
	 * ps.setString(6, tid); ps.setTimestamp(7, new
	 * java.sql.Timestamp(System.currentTimeMillis())); int update =
	 * ps.executeUpdate(); log.info("Request Added for msisdn=" + msisdn + ",Tid=" +
	 * tid + ",action=1|update=" + update); ps.close(); con.close();
	 * 
	 * } catch (Exception exp) { exp.printStackTrace(); } }
	 */

	public void addInSubQueue(String msisdn, String event, StateInfo stateInfo, String packId) {
		try {
			String smsMsisdn = null;
			if (msisdn.startsWith("+93")) {
				smsMsisdn = msisdn;
			} else
				smsMsisdn = "+93" + msisdn;

			Connection con = DataSource.getConnection();
			boolean alreadySub = isSubscriber(msisdn, con);
			if (alreadySub) {
				String url = SmsConfig.kannelURL;
				url = url.replaceAll("<TO>", smsMsisdn);
				url = url.replaceAll("<TEXT>", SmsConfig.existingSubscriberMsg);
				log.info("SENDMSG=" + url);
				log.info(CallURL(url));
				return;
			}

			/*
			 * boolean reqExist = isPending(msisdn, con); if (reqExist) { String url =
			 * SmsConfig.kannelURL; url = url.replaceAll("<TO>", smsMsisdn); url =
			 * url.replaceAll("<TEXT>", SmsConfig.pendingReqMsg); log.info("SENDMSG=" +
			 * url); log.info(CallURL(url)); return; }
			 * 
			 */

			String subUrl = smsConfig.subUrl;
			subUrl = subUrl.replace("MSISDN", msisdn);
			subUrl = subUrl.replace("PACK", packId);
			subUrl = subUrl.replace("CHANNEL", "SMS");
			subUrl = subUrl.replace("LANG", "1");

			log.info(subUrl);
			String resp = CallURL(subUrl);
			log.info("Response=" + resp);
			if("5".equals(resp)) {
				String url = SmsConfig.kannelURL;
				url = url.replaceAll("<TO>", smsMsisdn);
				url = url.replaceAll("<TEXT>", SmsConfig.existingSubscriberMsg);
				log.info("SENDMSG=" + url);
				log.info(CallURL(url));
				return;
			}

			/*
			 * PackDetails pack = getPackDetails( packId , con); if (pack == null) {
			 * log.info("Pack Details not found"); return; }
			 * 
			 * String tid = "SE-" + System.currentTimeMillis(); String query =
			 * "insert into vms_charging_request( msisdn,action,req_time,amount,pack_id,tid,next_retry_time) values (?,?,?,?,?,?,?) "
			 * ; PreparedStatement ps = con.prepareStatement(query);
			 * 
			 * ps.setString(1, msisdn); ps.setInt(2, 1); ps.setString(3, getDate());
			 * ps.setInt(4, pack.getPrice()); ps.setString(5, pack.getPackId());
			 * ps.setString(6, tid); ps.setTimestamp(7, new
			 * java.sql.Timestamp(System.currentTimeMillis())); int update =
			 * ps.executeUpdate(); log.info("Request Added for msisdn=" + msisdn + ",Tid=" +
			 * tid + ",action=1|update=" + update); ps.close();
			 */

			con.close();

		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	public boolean isSubscriber(String msisdn, Connection con) {
		boolean isExist = false;
		try {

			String query = "select count(*) from vms_users where msisdn ='"+ msisdn+"'";
			log.info(query);
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(query);
			if (rs.next()) {
				if (rs.getInt(1) > 0) {
					isExist = true;
				}
			}
			rs.close();
			st.close();

		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return isExist;
	}

	public boolean isPending(String msisdn, Connection con) {
		boolean isExist = false;
		try {
			String query = "select count(*) from vms_charging_request where msisdn = " + msisdn;
			log.info(query);
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(query);
			if (rs.next()) {
				if (rs.getInt(1) > 0) {
					isExist = true;
				}
			}
			rs.close();
			st.close();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return isExist;
	}

	public PackDetails getPackDetails(String packId, Connection con) {
		PackDetails pack = null;
		try {

			String query = "select * from vms_packs where pack_id = '" + packId + "'";
			log.info(query);
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(query);
			if (rs.next()) {

				pack = new PackDetails();
				pack.setPackId(rs.getString("pack_id"));
				pack.setPrice(rs.getInt("pack_price"));
				pack.setValidityDays(rs.getInt("pack_validity"));

			}
			rs.close();
			st.close();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return pack;
	}

	private String getDate() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(new Date());
	}
}
