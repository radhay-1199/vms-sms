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
import vms.se.config.Config;
import vms.se.config.Constants;
import vms.se.db.AccountRequestRepository;
import vms.se.db.HlrReqRepository;
import vms.se.db.PackDetailRepository;
import vms.se.db.VmsUserRepository;
import vms.se.util.CBSUtil;
import vms.se.util.HttpUtil;
import vms.se.util.SmsUtil;
import vms.se.util.SoapUtil;

@Service
public class ProcessAccountRequest implements Runnable {
	private Logger log = LogManager.getRootLogger();

	@Autowired
	private AccountRequestRepository accRepo;

	@Autowired
	private CBSUtil cbsUtil;

	@Autowired
	private HlrReqRepository hlrReqRepo;

	@Autowired
	private VmsUserRepository vmsUserRepo;

	@Autowired
	private PackDetailRepository packRepo;

	@Autowired
	private SmsUtil smsUtil;

	@Autowired
	private Config config;

	@Override
	public void run() {
		while (true) {
			try {
				List<AccountTxRequest> reqList = accRepo.getPendingRequest();
				for (AccountTxRequest req : reqList) {
					if (req.getAction() == 1) {
						processSubRequest(req);
					} else if (req.getAction() == 2) {
						processBalanceReq(req);
					} else if (req.getAction() == 3) {
						processUnSubRequest(req);
						// UnSub Request

					} else if (req.getAction() == 4) {
						// Renewal Request
					}
				}
			} catch (Exception exp) {
				exp.printStackTrace();
			}

			try {
				Thread.sleep(10000);
			} catch (Exception e) {
			}
		}

	}

	public void processUnSubRequest(AccountTxRequest req) {
		log.info(req.toString());
		hlrReqRepo.insertIntoHlrRequest(req.getMsisdn(), req.getPackId() , Constants.HLR_UNSUB , req.getChannel() );
		accRepo.deleteRequest(req.getId());
	}

	public void processSubRequest(AccountTxRequest req) {

		log.info(req.toString());
		if (req.getAmount() > 0) {
			req.setAmount(req.getAmount() * -1);
		}

		PackDetails pack = packRepo.getPackDetails(req.getPackId());
		if (pack == null) {
			// Pack Details not found
			log.info("Pack Details not found for id=" + req.getPackId());
			accRepo.deleteRequest(req.getId());
			return;
		}

		boolean success = false;
		AccountTxResponse txResp = cbsUtil.getBalance(req);
		if (txResp.getBalance() > req.getAmount()) {

			AccountTxResponse chargResp = cbsUtil.accountTx(req, pack);
			if (chargResp.getCode().equals("405000000")) {
				success = true;
			}

		} else {
			smsUtil.sendSMS(req.getMsisdn(), config.getSubLowBalanceMsgText(), pack);

		}

		if (success) {
			// accRepo.deleteRequest(req.getId());
			Date nextRenewal = addDayInDate(pack.getValidityDays());
			
			vmsUserRepo.insertIntoUsers( req.getMsisdn(), req.getPackId() , nextRenewal , req.getLang() , req.getChannel() );
			hlrReqRepo.insertIntoHlrRequest(req.getMsisdn(), req.getPackId(), Constants.HLR_SUB , req.getChannel() );

		} else {
			// Retry After Some Time
			/*
			 * Date nextRetryTime = new Date(System.currentTimeMillis() + 5 * 60 * 1000);
			 * accRepo.updateStatus(req.getId(), "LOW BAL-" + txResp.getCode(),
			 * nextRetryTime);
			 */
		}
		accRepo.deleteRequest(req.getId());

	}

	public void processBalanceReq(AccountTxRequest req) {

		log.info(req.toString());
		AccountTxResponse txResp = cbsUtil.getBalance(req);
		log.info(txResp.toString());
		accRepo.deleteRequest(req.getId());
	}

	public void addToHlrRquestForUnsub(AccountTxRequest req) {
		hlrReqRepo.insertIntoHlrRequest(req.getMsisdn(), req.getPackId() , Constants.HLR_UNSUB , req.getChannel() );
		accRepo.deleteRequest(req.getId());
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
}
