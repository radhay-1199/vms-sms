package vms.se.service;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vms.se.bean.AccountTxRequest;
import vms.se.bean.AccountTxResponse;
import vms.se.config.Config;
import vms.se.config.Constants;
import vms.se.db.AccountRequestRepository;
import vms.se.db.HlrReqRepository;
import vms.se.util.CBSUtil;
import vms.se.util.HttpUtil;
import vms.se.util.SoapUtil;

@Service
public class ProcessAccountRequest implements Runnable {
	private Logger log = LogManager.getRootLogger();

	@Autowired
	private AccountRequestRepository accRepo;

	@Autowired
	private CBSUtil cbsUtil;
	
	@Autowired
	private HlrReqRepository hlrReqRepo ;

	@Override
	public void run() {
		while (true) {
			try {
				List<AccountTxRequest> reqList = accRepo.getPendingRequest();
				for (AccountTxRequest req : reqList) {
					if (req.getAction() == 1) {
						processDeductionReq(req);
					} else if (req.getAction() == 2) {
						processBalanceReq(req);
					}
				}
			} catch (Exception exp) {
				exp.printStackTrace();
			}

			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}
		}

	}

	public void processDeductionReq(AccountTxRequest req) {

		log.info(req.toString());
		if (req.getAmount() > 0) {
			req.setAmount(req.getAmount() * -1);
		}
		AccountTxResponse txResp = cbsUtil.getBalance(req);

		if (txResp.getBalance() > req.getAmount()) {

			cbsUtil.accountTx(req);
			accRepo.deleteRequest(req.getId());
			hlrReqRepo.insertIntoHlrRequest(req.getMsisdn() , Constants.HLR_SUB ) ;

		} else {
			// Retry After Some Time
			Date nextRetryTime = new Date(System.currentTimeMillis() + 5 * 60 * 1000);
			accRepo.updateStatus(req.getId() , "LOW BAL-" + txResp.getCode(), nextRetryTime);
		}
	}

	public void processBalanceReq(AccountTxRequest req) {

		log.info(req.toString());
		AccountTxResponse txResp = cbsUtil.getBalance(req);
		log.info(txResp.toString());
		accRepo.deleteRequest(req.getId());
	}
}
