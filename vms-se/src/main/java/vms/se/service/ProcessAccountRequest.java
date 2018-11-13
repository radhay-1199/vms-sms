package vms.se.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vms.se.bean.AccountRequest;
import vms.se.config.Config;
import vms.se.db.AccountRequestRepository;
import vms.se.db.HlrReqRepository;
import vms.se.util.HttpUtil;
import vms.se.util.SoapUtil;

@Service
public class ProcessAccountRequest implements Runnable {
	private Logger log = LogManager.getRootLogger();

	@Autowired
	private AccountRequestRepository accRepo;

	@Autowired
	private Config config;

	@Autowired
	private HttpUtil httpUtil;
	
	@Autowired
	private SoapUtil soapUtil;

	@Override
	public void run() {
		while (true) {
			try {

				List<AccountRequest> reqList = accRepo.getPendingRequest();

				for (AccountRequest req : reqList) {
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
	public void processDeductionReq ( AccountRequest req) {
		
	}
	public void processBalanceReq ( AccountRequest req) {
		soapUtil.getBalance(req);
		accRepo.deleteRequest(req.getId());
	}
}
