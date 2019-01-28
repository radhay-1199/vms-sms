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
import vms.se.bean.ReportData;
import vms.se.bean.VmsUser;
import vms.se.config.Config;
import vms.se.config.Constants;
import vms.se.db.HlrReqRepository;
import vms.se.db.PackDetailRepository;
import vms.se.db.VmsReportRepository;
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

	@Autowired
	private VmsReportRepository vmsReportRepo;

	@Override
	public void run() {
		while (true) {
			try {

				List<HLRRequest> reqList = reqRepo.getPendingRequest();
				for (HLRRequest req : reqList) {
					switch (req.getAction()) {
					case Constants.HLR_SUB:
						processSubReq(req);
						break;
					case Constants.HLR_UNSUB:
						processUnSubReq(req);
						break;

					default:
						log.info("Unknown Action in Process HLR Request, Action value=" + req.getAction());
						break;
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

			VmsUser user = vmsUserRepo.getUserDetails(req.getMsisdn());
			if (user == null) {

				log.info("User Details not found for msisdn=" + req.getMsisdn());
				vmsReportRepo.insertIntoReports(new ReportData(req.getMsisdn(), Constants.HLR_UNSUB, 0,
						req.getChannel(), "Unknown User", req.getMsisdn()));
				return;

			}

			PackDetails pack = packRepo.getPackDetails(user.getPackId());

			String respStr = httpUtil.submitRequest(subUri);

			HLRResponse hlrResp = null;
			if (respStr != null) {
				hlrResp = parseResponse(respStr);
				log.info(hlrResp.toString());
				reqRepo.deleteRequest(req.getMsisdn(), Constants.HLR_UNSUB);

				if (hlrResp.getOutputMessage() != null && hlrResp.getOutputMessage().indexOf("SUCCESS") != -1) {
					vmsUserRepo.deleteUser(req.getMsisdn());
					smsUtil.sendSMS(req.getMsisdn(), config.getUnsubSuccessMsgText(), pack);
					vmsReportRepo.insertIntoReports(new ReportData(req.getMsisdn(), Constants.HLR_UNSUB, 1,
							req.getChannel(), "success", req.getMsisdn()));

				} else {
					log.info("Invalid HLR Response=" + hlrResp.getOutputMessage());
					vmsReportRepo.insertIntoReports(new ReportData(req.getMsisdn(), Constants.HLR_UNSUB, 0,
							req.getChannel(), "Invalid HLR Response", req.getMsisdn()));

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
					reqRepo.deleteRequest(subReq.getMsisdn(), Constants.HLR_SUB);
					smsUtil.sendSMS(subReq.getMsisdn(), config.getSubSuccessMsgText(), pack);
					vmsReportRepo.insertIntoReports(new ReportData(subReq.getMsisdn(), Constants.HLR_SUB, 0,
							subReq.getChannel(), hlrResp.getOutputMessage(), subReq.getMsisdn()));
					return;
				}

				if (hlrResp.getOutputCode().indexOf("SUCCESS") != -1) {
					vmsUserRepo.updateHLRStatus(subReq.getMsisdn(), 1);
					reqRepo.deleteRequest(subReq.getMsisdn(), Constants.HLR_SUB);
					smsUtil.sendSMS(subReq.getMsisdn(), config.getSubSuccessMsgText(), pack);
					vmsReportRepo.insertIntoReports(new ReportData(subReq.getMsisdn(), Constants.HLR_SUB, 1,
							subReq.getChannel(), "success", subReq.getMsisdn()));

				} else {

					log.info("Failed Sub|" + hlrResp.toString());
					Date nextRetryTime = addMinuteInDate(15);
					reqRepo.updateStatus(subReq.getMsisdn(), hlrResp.getOutputMessage(), nextRetryTime);
					vmsReportRepo.insertIntoReports(new ReportData(subReq.getMsisdn(), Constants.HLR_SUB, 0,
							subReq.getChannel(), hlrResp.getOutputCode(), subReq.getMsisdn()));

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

	public HLRResponse processRequest(String msisdn, int action) {
		HLRResponse hlrResp = null;
		String uri = null;
		if (action == Constants.HLR_SUB)
			uri = config.getSubApiURL();
		else if (action == Constants.HLR_UNSUB)
			uri = config.getUnSubApiURL();

		if (msisdn.startsWith("93"))
			uri = uri.replaceAll("<MSISDN>", msisdn);
		else
			uri = uri.replaceAll("<MSISDN>", "93" + msisdn);

		String respStr = httpUtil.submitRequest(uri);
		if (respStr != null) {
			hlrResp = parseResponse(respStr);
		}
		return hlrResp;
	}

}
