package vms.se.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vms.se.bean.AccountTxRequest;
import vms.se.bean.AccountTxResponse;
import vms.se.bean.PackDetails;
import vms.se.bean.VmsRenewalRequest;
import vms.se.bean.VmsUser;
import vms.se.config.Config;
import vms.se.db.PackDetailRepository;
import vms.se.db.VmsRenewalRepository;
import vms.se.db.VmsUserRepository;
import vms.se.util.CBSUtil;
import vms.se.util.SmsUtil;

@Service
public class ProcessRenewalRequest implements Runnable {
	private Logger log = LogManager.getRootLogger();

	@Autowired
	private VmsUserRepository vmsUserRepo;

	@Autowired
	private PackDetailRepository packRepo;

	@Autowired
	private CBSUtil cbsUtil;

	@Autowired
	private SmsUtil smsUtil;

	@Autowired
	private Config config;

	@Autowired
	private VmsRenewalRepository vmsRenewalRepo;

	@Override
	public void run() {
		while (true) {
			try {
				List<VmsUser> renewalUserList = vmsUserRepo.getUsersForRenewal();
				for (VmsUser user : renewalUserList) {
					processRequestForRenewal(user);
				}

				List<VmsRenewalRequest> renewalRetryList = vmsRenewalRepo.getRequestList();
				for (VmsRenewalRequest renewalReq : renewalRetryList) {
					processRenewalRetry(renewalReq);
				}

			} catch (Exception exp) {
				exp.printStackTrace();

			}

			try {
				Thread.sleep(5 * 60 * 1000);
			} catch (Exception exp) {

			}
		}

	}

	public void processRenewalRetry(VmsRenewalRequest renewalReq) {
		PackDetails pack = packRepo.getPackDetails(renewalReq.getPackId());
		if (pack == null) {
			log.info("Pack Details not found for id=" + renewalReq.getPackId());
			return;
		}

		AccountTxRequest req = new AccountTxRequest();
		req.setMsisdn(renewalReq.getMsisdn());
		req.setTid("RE-" + System.currentTimeMillis());
		req.setAction(2);// Anything but 1

		AccountTxResponse txResp = cbsUtil.getBalance(req);
		if (txResp.getBalance() > pack.getPrice()) {
			AccountTxResponse chargResp = cbsUtil.accountTx(req, pack);
			if (chargResp.getCode().equals("405000000")) {
				Date nextRenewalDate = new Date(
						System.currentTimeMillis() + (pack.getValidityDays() * 24 * 60 * 60 * 1000));
				vmsUserRepo.updateValidityAfterRenewal(renewalReq.getMsisdn(), nextRenewalDate);
				smsUtil.sendSMS(renewalReq.getMsisdn(), config.getRenewalSuccessMsgText(), pack);
				vmsRenewalRepo.deleteRequest(renewalReq.getMsisdn());
			}

		} else {
			// Next Retry will be after 4 Hours
			Date nextDate = new Date(System.currentTimeMillis() + (4 * 60 * 60 * 1000));
			vmsRenewalRepo.updateNextRenewalRetry(renewalReq.getMsisdn(), nextDate);
		}
	}

	public void processRequestForRenewal(VmsUser user) {

		PackDetails pack = packRepo.getPackDetails(user.getPackId());
		if (pack == null) {
			log.info("Pack Details not found for id=" + user.getPackId());
			return;
		}

		AccountTxRequest req = new AccountTxRequest();
		req.setMsisdn(user.getMsisdn());
		req.setTid("RE-" + System.currentTimeMillis());
		req.setAction(2);// Anything but 1

		AccountTxResponse txResp = cbsUtil.getBalance(req);

		if (txResp.getBalance() > pack.getPrice()) {
			AccountTxResponse chargResp = cbsUtil.accountTx(req, pack);
			if (chargResp.getCode().equals("405000000")) {
				// Date nextRenewalDate = new Date( System.currentTimeMillis() + (
				// pack.getValidityDays() * 24 * 60 * 60 * 1000 ));
				Date nextRenewalDate = addDayInDate(pack.getValidityDays());

				vmsUserRepo.updateValidityAfterRenewal(user.getMsisdn(), nextRenewalDate);
				smsUtil.sendSMS(user.getMsisdn(), config.getRenewalSuccessMsgText(), pack);

			}

		} else {

			// Next Retry will be after 4 Hours
			// Date nextDate = new Date(System.currentTimeMillis() + ( 4 * 60 * 60 * 1000)
			// );
			
			Date nextDate = addDayInDate(1);
			vmsUserRepo.updateServiceStatus(user.getMsisdn(), 0);
			vmsRenewalRepo.insertIntoRenewalRequest(user.getMsisdn(), user.getPackId(), 1, nextDate);
		}
	}

	public Date addDayInDate(int days) {
		Calendar cal = null;
		try {
			cal = Calendar.getInstance();
			cal.add(Calendar.DATE, days);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cal.getTime();

	}

	public Date addMinuteInDate(int min) {
		Calendar cal = null;
		try {
			cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, min);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cal.getTime();

	}

}
