package vms.se.util;

import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.ws.BindingProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.huawei.bme.cbsinterface.cbs.accountmgr.AdjustAccountRequest;
import com.huawei.bme.cbsinterface.cbs.accountmgr.CBSInterfaceAccountMgrService;
import com.huawei.bme.cbsinterface.cbs.accountmgr.CBSInterfaceAccountMgrServicePortType;
import com.huawei.bme.cbsinterface.cbs.accountmgr.ModifyAcctFeeType;
import com.huawei.bme.cbsinterface.cbs.accountmgr.QueryBalanceRequest;
import com.huawei.bme.cbsinterface.cbs.accountmgr.QueryBalanceResult;
import com.huawei.bme.cbsinterface.cbs.accountmgr.AdjustAccountRequest.ModifyAcctFeeList;
import com.huawei.bme.cbsinterface.cbs.accountmgrmsg.AdjustAccountRequestMsg;
import com.huawei.bme.cbsinterface.cbs.accountmgrmsg.AdjustAccountResultMsg;
import com.huawei.bme.cbsinterface.cbs.accountmgrmsg.QueryBalanceRequestMsg;
import com.huawei.bme.cbsinterface.cbs.accountmgrmsg.QueryBalanceResultMsg;
import com.huawei.bme.cbsinterface.common.RequestHeader;
import com.huawei.bme.cbsinterface.common.ResultHeader;
import com.huawei.bme.cbsinterface.common.SessionEntityType;

import vms.se.bean.AccountTxRequest;
import vms.se.bean.AccountTxResponse;
import vms.se.bean.PackDetails;
import vms.se.config.Config;

@Service
public class CBSUtil {

	private Logger log = LogManager.getRootLogger();

	@Autowired
	private Config config;

	@Autowired
	private CBSInterfaceAccountMgrService service;

	public AccountTxResponse accountTx(AccountTxRequest req , PackDetails pack) {
		AccountTxResponse accTxResp = new AccountTxResponse();
		try {

			CBSInterfaceAccountMgrServicePortType porttype = service.getCBSInterfaceAccountMgrServiceSOAP11PortHttp();
			Map<String, Object> requestContext = ((BindingProvider) porttype).getRequestContext();

			int connectTimeout = 5000;
			int readTimeout = 10000;

			requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, config.getChargingUrl());
			requestContext.put("sun.net.client.defaultConnectTimeout", connectTimeout);
			requestContext.put("sun.net.client.defaultReadTimeout", readTimeout);
			requestContext.put("com.sun.xml.internal.ws.connect.timeout", connectTimeout);
			requestContext.put("com.sun.xml.internal.ws.request.timeout", connectTimeout);
			requestContext.put("com.sun.xml.ws.request.timeout", connectTimeout);
			requestContext.put("com.sun.xml.ws.connect.timeout", connectTimeout);

			AdjustAccountRequestMsg adjustAccReqMsg = new AdjustAccountRequestMsg();
			AdjustAccountRequest adjustAccReq = new AdjustAccountRequest();

			SessionEntityType sesstype = new SessionEntityType();
			sesstype.setName(config.getCbsUsername());
			sesstype.setPassword(config.getCbsPassword());
			sesstype.setRemoteAddress("127.0.0.1");
			

			long serialno = System.currentTimeMillis();
			String serialnum = String.valueOf(serialno).substring(3);

			RequestHeader reqHead = new RequestHeader();
			reqHead.setCommandId(config.getChargingCommandId());// AdjustAccount
			reqHead.setVersion(config.getBalanceVersion());

			reqHead.setOperatorID( "VMS" );
			reqHead.setTransactionId(req.getTid());
			reqHead.setSequenceId("" + req.getId());
			reqHead.setRequestType(config.getChargingRequestType());
			reqHead.setSessionEntity(sesstype);
			
			reqHead.setSerialNo( pack.getName() + serialnum );
			reqHead.setRemark( pack.getRemark() );

			adjustAccReq.setSubscriberNo(req.getMsisdn());
			adjustAccReq.setOperateType(2);
			
			if(req.getAction() == 1)
				adjustAccReq.setAdditionalInfo( "SUB " + pack.getName() );
			else
				adjustAccReq.setAdditionalInfo( "RENEWAL " + pack.getName());
				
			adjustAccReq.setSPCode("0") ;
			
			ModifyAcctFeeList list = new ModifyAcctFeeList();
			List<ModifyAcctFeeType> listB = list.getModifyAcctFee();
			ModifyAcctFeeType ele = new ModifyAcctFeeType();
			ele.setAccountType(config.getChargingAcType());
			ele.setCurrAcctChgAmt( req.getAmount() * 100 );
			listB.add(ele);

			adjustAccReq.setModifyAcctFeeList(list);
			adjustAccReq.setNotifyFlag(0);// 1
			adjustAccReqMsg.setRequestHeader(reqHead);
			adjustAccReqMsg.setAdjustAccountRequest(adjustAccReq);
			AdjustAccountResultMsg adjustAccResultMsg = porttype.adjustAccount(adjustAccReqMsg);

			if (adjustAccResultMsg != null) {
				ResultHeader resultHeader = adjustAccResultMsg.getResultHeader();
				accTxResp.setCode(resultHeader.getResultCode());
				accTxResp.setDesc(resultHeader.getResultDesc());
				log.debug("msisdn|RespCode=" + accTxResp.getCode() + "|Desc=" + accTxResp.getDesc());
				
			} else {
				accTxResp.setCode("2");
				accTxResp.setDesc("Null");
			}

		} catch (Exception e) {
			accTxResp.setCode("2");
			accTxResp.setDesc(e.getMessage());
		}
		return accTxResp;

	}

	public AccountTxResponse getBalance(AccountTxRequest req) {

		AccountTxResponse accTxResp = new AccountTxResponse();
		try {

			CBSInterfaceAccountMgrServicePortType porttype = service.getCBSInterfaceAccountMgrServiceSOAP11PortHttp();
			Map<String, Object> requestContext = ((BindingProvider) porttype).getRequestContext();

			int connectTimeout = 5000;
			int readTimeout = 10000;

			requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, config.getBalanceUrl());
			requestContext.put("sun.net.client.defaultConnectTimeout", connectTimeout);
			requestContext.put("sun.net.client.defaultReadTimeout", readTimeout);
			requestContext.put("com.sun.xml.internal.ws.connect.timeout", connectTimeout);
			requestContext.put("com.sun.xml.internal.ws.request.timeout", connectTimeout);
			requestContext.put("com.sun.xml.ws.request.timeout", connectTimeout);
			requestContext.put("com.sun.xml.ws.connect.timeout", connectTimeout);

			QueryBalanceRequestMsg integEnqReq = new QueryBalanceRequestMsg();
			QueryBalanceRequest intEnqReq = new QueryBalanceRequest();

			RequestHeader reqHead = new RequestHeader();

			SessionEntityType sesstype = new SessionEntityType();
			sesstype.setName(config.getCbsUsername());
			sesstype.setPassword(config.getCbsPassword());
			sesstype.setRemoteAddress(config.getBalanceRemoteAddress());// 127.0.0.1

			reqHead.setCommandId(config.getBalanceCommandId());// AdjustAccount
			reqHead.setVersion(config.getBalanceVersion());
			reqHead.setTransactionId(req.getTid());
			reqHead.setSequenceId("" + req.getId());

			reqHead.setRequestType(config.getBalanceRequestType());// Event
			reqHead.setSessionEntity(sesstype);

			// long serialno = System.currentTimeMillis();
			reqHead.setSerialNo("" + req.getId());
			reqHead.setRemark("BAL_CHK");// MOD

			intEnqReq.setSubscriberNo(req.getMsisdn());// 729323221
			integEnqReq.setRequestHeader(reqHead);
			integEnqReq.setQueryBalanceRequest(intEnqReq);
			QueryBalanceResultMsg integEnqResult = porttype.queryBalance(integEnqReq);

			if (integEnqResult != null) {
				ResultHeader resultHeader = integEnqResult.getResultHeader();
				accTxResp.setCode(resultHeader.getResultCode());
				accTxResp.setDesc(resultHeader.getResultDesc());
				log.debug("msisdn|RespCode=" + accTxResp.getCode() + "|Desc" + accTxResp.getDesc());
				if ("405000000".equals(resultHeader.getResultCode())) {
					JAXBElement<QueryBalanceResult> integrationEnquiry = integEnqResult.getQueryBalanceResult();
					QueryBalanceResult integrationEnquiryResult = integrationEnquiry.getValue();

					for (com.huawei.bme.cbsinterface.cbs.accountmgr.BalanceRecordType balanceReco : integrationEnquiryResult
							.getBalanceRecord()) {
						if ("2000".equals(balanceReco.getAccountType())
								|| "3000".equals(balanceReco.getAccountType())) {
							long balance = balanceReco.getBalance();
							accTxResp.setBalance(balance);
							accTxResp.setAccountType(balanceReco.getAccountType());
							log.debug(req.getMsisdn() + "|accountType=" + balanceReco.getAccountType() + "|balance="
									+ balanceReco.getBalance());
						}
					}
				}
			} else {
				accTxResp.setCode("2");
				accTxResp.setDesc("Null");
			}
		} catch (Exception e) {
			accTxResp.setCode("2");
			accTxResp.setDesc(e.getMessage());
		}
		return accTxResp;
	}
}
