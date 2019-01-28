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
import vms.se.bean.HLRResponse;
import vms.se.bean.PackDetails;
import vms.se.bean.ReportData;
import vms.se.config.Config;
import vms.se.config.Constants;
import vms.se.db.AccountRequestRepository;
import vms.se.db.HlrReqRepository;
import vms.se.db.PackDetailRepository;
import vms.se.db.VmsReportRepository;
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

	@Autowired
	private VmsReportRepository vmsReportRepo;
	
	@Autowired
	private ProcessHLRRequest processHLRRequest ; 

	@Override
	public void run() {
		while (true) {
			try {
				List<AccountTxRequest> reqList = accRepo.getPendingRequest();
				for (AccountTxRequest req : reqList) {
					switch (req.getAction()) {
					case Constants.SUB_REQ:
						processSubRequest(req);
						break;
					case Constants.BAL_REQ:
						processBalanceReq(req);
						break;
					case Constants.UNSUB_REQ:
						processUnSubRequest(req);
						break;
					default:
						log.info("Unknown Action in Process Account Request,Action Value=" + req.getAction());
						break;
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
		hlrReqRepo.insertIntoHlrRequest(req.getMsisdn(), req.getPackId(), Constants.HLR_UNSUB, req.getChannel());
		vmsReportRepo.insertIntoReports(
				new ReportData(req.getMsisdn(), Constants.UNSUB_REQ, 1, req.getChannel(), "success", req.getTid()));
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
			vmsReportRepo.insertIntoReports(new ReportData(req.getMsisdn(), req.getAction(), 0, req.getChannel(),
					"PackInfo Not Found -" + req.getPackId(), req.getTid()));
			accRepo.deleteRequest(req.getId());
			return;
		}
		
		/*HLRResponse hlrResp = processHLRRequest.processRequest(req.getMsisdn() , Constants.HLR_SUB );
		if(hlrResp == null || hlrResp.getOutputCode().indexOf("SUCCESS") != -1 ) {
			accRepo.deleteRequest(req.getId());
			return ; 
		}*/
		
		
		boolean success = false ;
		AccountTxResponse txResp = cbsUtil.getBalance(req);
		req.setAmount( pack.getPrice() * 100 );
		
		if (txResp.getBalance() > req.getAmount()) {

			AccountTxResponse chargResp = cbsUtil.accountTx(req, pack);
			if (chargResp.getCode().equals("405000000")) {
				success = true;
			}

		} else {
			
			smsUtil.sendSMS(req.getMsisdn(), config.getSubLowBalanceMsgText(), pack);
			vmsReportRepo.insertIntoReports(new ReportData(req.getMsisdn(), req.getAction(), 0, req.getChannel(),
					"Low Balance -" + txResp.getBalance(), req.getTid()));
			//processHLRRequest.processRequest(req.getMsisdn() , Constants.HLR_UNSUB );
			
		}

		if (success) {
			// accRepo.deleteRequest(req.getId());
			vmsReportRepo.insertIntoReports(
					new ReportData(req.getMsisdn(), req.getAction(), 1, req.getChannel(), "success", req.getTid()));

			Date nextRenewal = addDayInDate(pack.getValidityDays());
			vmsUserRepo.insertIntoUsers(req.getMsisdn(), req.getPackId(), nextRenewal, req.getLang(), req.getChannel());
			hlrReqRepo.insertIntoHlrRequest(req.getMsisdn(), req.getPackId(), Constants.HLR_SUB, req.getChannel());

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

	/*public void addToHlrRquestForUnsub(AccountTxRequest req) {
		hlrReqRepo.insertIntoHlrRequest(req.getMsisdn(), req.getPackId(), Constants.HLR_UNSUB, req.getChannel());
		accRepo.deleteRequest(req.getId());
	}*/

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
