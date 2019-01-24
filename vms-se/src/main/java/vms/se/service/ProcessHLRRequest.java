package vms.se.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vms.se.bean.HLRRequest;
import vms.se.bean.HLRResponse;
import vms.se.bean.PackDetails;
import vms.se.bean.VmsUser;
import vms.se.config.Config;
import vms.se.config.Constants;
import vms.se.db.HlrReqRepository;
import vms.se.db.PackDetailRepository;
import vms.se.db.VmsUserRepository;
import vms.se.util.HttpUtil;
import vms.se.util.SmsUtil;

@Service
public class ProcessHLRRequest implements Runnable {
	private Logger log = LogManager.getRootLogger();

	@Autowired
	private HlrReqRepository reqRepo;

	@Autowired
	private Config config;

	@Autowired
	private HttpUtil httpUtil;

	@Autowired
	private VmsUserRepository vmsUserRepo;

	@Autowired
	private SmsUtil smsUtil;

	@Autowired
	private PackDetailRepository packRepo;

	@Override
	public void run() {
		while (true) {
			try {

				List<HLRRequest> reqList = reqRepo.getPendingRequest();
				for (HLRRequest req : reqList) {
					if (req.getAction() == Constants.HLR_SUB) {
						processSubReq(req);
					} else if (req.getAction() == Constants.HLR_UNSUB) {
						processUnSubReq(req);
					}
				}
			} catch (Exception exp) {
				exp.printStackTrace();
			}

			try {
				Thread.sleep(15000);
			} catch (Exception e) {
			}
		}
	}

	public void processUnSubReq(HLRRequest req) {
		try {

			String subUri = config.getUnSubApiURL();

			if (req.getMsisdn().startsWith("93"))
				subUri = subUri.replaceAll("<MSISDN>", req.getMsisdn());
			else
				subUri = subUri.replaceAll("<MSISDN>", "93" + req.getMsisdn());

			log.info(subUri);

			VmsUser user = vmsUserRepo.getUserDetails( req.getMsisdn() );
			if(user == null) {
				log.info("User Details not found for msisdn="+req.getMsisdn());
				return ; 
			}
			
			PackDetails pack = packRepo.getPackDetails(user.getPackId());

			String respStr = httpUtil.submitRequest(subUri);

			HLRResponse hlrResp = null;
			if (respStr != null) {
				hlrResp = parseResponse(respStr);
				log.info(hlrResp.toString());
				reqRepo.deleteRequest(req.getMsisdn(), 2);

				if (hlrResp.getOutputMessage() != null && hlrResp.getOutputMessage().indexOf("SUCCESS0002") != -1) {
					vmsUserRepo.deleteUser(req.getMsisdn());
					smsUtil.sendSMS(req.getMsisdn(), config.getUnsubSuccessMsgText(), pack);
				} else {
					log.info("Cant Understand HLR Response=" + hlrResp.getOutputMessage());

				}

				/*
				 * if (hlrResp.getOutputCode().equalsIgnoreCase("SUCCESS0001")) {
				 * reqRepo.deleteRequest(req.getMsisdn(), 1); } else { log.info("Failed UnSub|"
				 * + hlrResp.toString()); }
				 */

			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	public void processSubReq(HLRRequest subReq) {
		try {

			String subUri = config.getSubApiURL();

			if (subReq.getMsisdn().startsWith("93"))
				subUri = subUri.replaceAll("<MSISDN>", subReq.getMsisdn());
			else
				subUri = subUri.replaceAll("<MSISDN>", "93" + subReq.getMsisdn());

			// subUri = subUri.replaceAll("<MSISDN>", subReq.getMsisdn());

			log.info(subUri);

			PackDetails pack = packRepo.getPackDetails(subReq.getPackId());
			String respStr = httpUtil.submitRequest(subUri);

			HLRResponse hlrResp = null;
			if (respStr != null) {
				hlrResp = parseResponse(respStr);
				log.info(hlrResp.toString());

				// reqRepo.deleteRequest(subReq.getMsisdn(), 1);

				if (hlrResp.getOutputMessage() != null
						&& hlrResp.getOutputMessage().indexOf("Already have the service") != -1) {
					vmsUserRepo.updateHLRStatus(subReq.getMsisdn(), 1);
					reqRepo.deleteRequest(subReq.getMsisdn(), 1);
					smsUtil.sendSMS(subReq.getMsisdn(), config.getSubSuccessMsgText(), pack);

					return;
				}

				if (hlrResp.getOutputCode().indexOf("SUCCESS0002") != -1) {
					vmsUserRepo.updateHLRStatus(subReq.getMsisdn(), 1);
					reqRepo.deleteRequest(subReq.getMsisdn(), 1);
					smsUtil.sendSMS(subReq.getMsisdn(), config.getSubSuccessMsgText(), pack);

				} else {

					log.info("Failed Sub|" + hlrResp.toString());
					Date nextRetryTime = addMinuteInDate(15);
					reqRepo.updateStatus(subReq.getMsisdn(), hlrResp.getOutputMessage(), nextRetryTime);

				}

			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
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


	public HLRResponse parseResponse(String resp) {
		try {

			log.info("Resp=" + resp);
			String msisdn = null;
			String output2 = null;
			String output = null;

			if (resp.indexOf("<MSISDN>") != -1)
				msisdn = resp.substring(resp.indexOf("<MSISDN>") + 8, resp.indexOf("</MSISDN>"));

			if (resp.indexOf("<output>") != -1)
				output = resp.substring(resp.indexOf("<output>") + 8, resp.indexOf("</output>"));

			if (resp.indexOf("<output2>") != -1)
				output2 = resp.substring(resp.indexOf("<output2>") + 9, resp.indexOf("</output2>"));

			HLRResponse hlrResp = new HLRResponse();
			hlrResp.setMsisdn(msisdn);

			if (output != null) {
				// hlrResp.setOutputCode(output.substring(0, output.indexOf(":")));
				// hlrResp.setOutputMessage(output.substring(output.indexOf(":") + 1));
				hlrResp.setOutputMessage(output);
			}

			if (output2 != null) {
				// hlrResp.setOutput2Code(output2.substring(0, output2.indexOf(":")));
				// hlrResp.setOutput2Message(output2.substring(output.indexOf(":") + 1));
				hlrResp.setOutput2Message(output2);
			}
			return hlrResp;

		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return null;
	}
}
