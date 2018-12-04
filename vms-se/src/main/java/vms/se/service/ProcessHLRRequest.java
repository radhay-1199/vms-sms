package vms.se.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vms.se.bean.HLRRequest;
import vms.se.bean.HLRResponse;
import vms.se.config.Config;
import vms.se.config.Constants;
import vms.se.db.HlrReqRepository;
import vms.se.util.HttpUtil;

@Service
public class ProcessHLRRequest implements Runnable {
	private Logger log = LogManager.getRootLogger();

	@Autowired
	private HlrReqRepository reqRepo;

	@Autowired
	private Config config;

	@Autowired
	private HttpUtil httpUtil;

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
				Thread.sleep(1000);
			} catch (Exception e) {
			}
		}
	}

	public void processUnSubReq(HLRRequest req) {
		try {

			String subUri = config.getUnSubApiURL();

			subUri = subUri.replaceAll("<MSISDN>", req.getMsisdn());
			log.info(subUri);
			String respStr = httpUtil.submitRequest(subUri);

			HLRResponse hlrResp = null;
			if (respStr != null) {
				hlrResp = parseResponse(respStr);

				log.info(hlrResp.toString());
				reqRepo.deleteRequest(req.getMsisdn(), 2);

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
			subUri = subUri.replaceAll("<MSISDN>", subReq.getMsisdn());
			log.info(subUri);
			String respStr = httpUtil.submitRequest(subUri);

			HLRResponse hlrResp = null;
			if (respStr != null) {
				hlrResp = parseResponse(respStr);
				log.info(hlrResp.toString());
				reqRepo.deleteRequest(subReq.getMsisdn(), 1);

				if (hlrResp.getOutputMessage() != null
						&& hlrResp.getOutputMessage().equalsIgnoreCase("Already have the service"))
					reqRepo.deleteRequest(subReq.getMsisdn(), 1);

				if (hlrResp.getOutputCode().equalsIgnoreCase("SUCCESS0002")) {
					reqRepo.deleteRequest(subReq.getMsisdn(), 1);
				} else
					log.info("Failed Sub|" + hlrResp.toString());

			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
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
