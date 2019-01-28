package vms.se.service;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vms.se.bean.AccountTxRequest;
import vms.se.bean.AccountTxResponse;
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
import vms.se.util.UdpServer;

@Service
public class ProcessUdpRequest implements Runnable {

	private Logger log = LogManager.getRootLogger();
	private BlockingQueue<String> bQueue;

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

	@Override
	public void run() {

		bQueue = new ArrayBlockingQueue<String>(10000);
		UdpServer userServer = new UdpServer(config.getApiUdpPort(), bQueue);
		userServer.start();

		while (true) {
			try {

				String req = bQueue.take();
				if (req == null)
					continue;

				String info[] = req.split("#");
				if (info[0].equalsIgnoreCase("SUB")) {
					String msisdn = info[1];
					String packid = info[2];
					String lang = info[3];
					String respIp = info[4];
					String ressPortStr = info[5];

					AccountTxRequest reqTx = new AccountTxRequest();
					reqTx.setAction(1);
					reqTx.setPackId(packid);
					reqTx.setMsisdn(msisdn);
					reqTx.setLang(lang);

					reqTx.setTid("API-" + System.currentTimeMillis());
					int status = processSubRequest(reqTx);
					sendOverUdp(msisdn + "#" + status, respIp, Integer.parseInt(ressPortStr));

				} else if (info[0].equalsIgnoreCase("UNSUB")) {
					String msisdn = info[1];
					String respIp = info[2];
					String ressPortStr = info[3];
					
					int status = processUnSubRequest(msisdn);
					sendOverUdp(msisdn + "#" + status, respIp, Integer.parseInt(ressPortStr));
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

	public int processUnSubRequest(String msisdn) {
		log.info("processUnSubRequest|" + msisdn);
		hlrReqRepo.insertIntoHlrRequest(msisdn, "P", Constants.HLR_UNSUB, "IVR");
		vmsReportRepo.insertIntoReports(
				new ReportData(msisdn , Constants.UNSUB_REQ , 1, "IVR" , "success" , "API-" + System.currentTimeMillis() ));
		return 1;
	}

	public int processSubRequest(AccountTxRequest req) {

		log.info(req.toString());
		req.setChannel("IVR");

		PackDetails pack = packRepo.getPackDetails(req.getPackId());
		if (pack == null) {
			// Pack Details not found
			log.info("Pack Details not found for id=" + req.getPackId());
			vmsReportRepo.insertIntoReports(new ReportData(req.getMsisdn(), req.getAction(), 0, req.getChannel(),
					"PackInfo Not Found -" + req.getPackId(), req.getMsisdn()));
			return 3;
		}

		if (pack.getPrice() > 0) {
			req.setAmount(pack.getPrice() * -1);
		}

		boolean success = false;
		AccountTxResponse txResp = cbsUtil.getBalance(req);
		req.setAmount( pack.getPrice() * 100 );
		
		if ( txResp.getBalance() > req.getAmount() ) {
			AccountTxResponse chargResp = cbsUtil.accountTx( req, pack );
			if (chargResp.getCode().equals("405000000")) {
				success = true;
			}
		} else {
			// smsUtil.sendSMS(req.getMsisdn(), config.getSubLowBalanceMsgText(), pack);
			vmsReportRepo.insertIntoReports(new ReportData(req.getMsisdn(), req.getAction(), 0, req.getChannel(),
					"Low Balance -" + txResp.getBalance(), req.getTid()));
			return 2;
		}

		if (success) {
			Date nextRenewal = addDayInDate(pack.getValidityDays());
			vmsUserRepo.insertIntoUsers(req.getMsisdn(), req.getPackId(), nextRenewal, req.getLang(), req.getChannel());
			// vmsUserRepo.insertIntoUsers(req.getMsisdn(), req.getPackId(),
			// pack.getValidityDays());
			hlrReqRepo.insertIntoHlrRequest(req.getMsisdn(), req.getPackId(), Constants.HLR_SUB, req.getChannel());
			vmsReportRepo.insertIntoReports(
					new ReportData(req.getMsisdn(), req.getAction(), 1, req.getChannel(), "success", req.getTid()));
			return 1;

		} else {
			return 4;

		}
	}

	public void processBalanceReq(AccountTxRequest req) {

		log.info(req.toString());
		AccountTxResponse txResp = cbsUtil.getBalance(req);
		log.info(txResp.toString());
		accRepo.deleteRequest(req.getId());
	}

	public void addToHlrRquestForUnsub(AccountTxRequest req) {
		hlrReqRepo.insertIntoHlrRequest(req.getMsisdn(), req.getPackId(), Constants.HLR_UNSUB, "IVR");
		
		accRepo.deleteRequest(req.getId());
	}

	public boolean sendOverUdp(String data, String ip, int port) {
		boolean status = true;
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName(ip);
			DatagramPacket sendPacket = new DatagramPacket(data.getBytes(), data.length(), IPAddress, port);
			clientSocket.send(sendPacket);
			clientSocket.close();
			log.info(data + " " + ip + ":" + port);
		} catch (Exception e) {
			status = false;
		}
		return status;
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
