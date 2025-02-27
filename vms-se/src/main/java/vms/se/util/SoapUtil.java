package vms.se.util;

import javax.xml.soap.SOAPMessage;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPPart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vms.se.bean.AccountTxRequest;
import vms.se.config.Config;

@Service
public class SoapUtil {

	private Logger log = LogManager.getRootLogger();

	@Autowired
	private Config config;

	public SOAPMessage createBalanceSOAPRequest(AccountTxRequest req) throws Exception {
		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);

		SOAPMessage soapMessage = messageFactory.createMessage();
		SOAPPart soapPart = soapMessage.getSOAPPart();

		String accApi = "http://www.huawei.com/bme/cbsinterface/cbs/accountmgrmsg";
		String comApi = "http://www.huawei.com/bme/cbsinterface/common";
		String acc1Api = "http://www.huawei.com/bme/cbsinterface/cbs/accountmgr";

		SOAPEnvelope envelope = soapPart.getEnvelope();

		// envelope.removeNamespaceDeclaration(envelope.getPrefix());
		// envelope.removeNamespaceDeclaration("soap");
		// String soap = "http://www.w3.org/2003/05/soap-envelope";
		// envelope.addNamespaceDeclaration("soap", soap);

		envelope.addNamespaceDeclaration("acc", accApi);
		envelope.addNamespaceDeclaration("com", comApi);
		envelope.addNamespaceDeclaration("acc1", acc1Api);

		SOAPBody soapBody = envelope.getBody();
		SOAPHeader header = soapMessage.getSOAPHeader();

		MimeHeaders headers = soapMessage.getMimeHeaders();

		headers.setHeader("SOAPAction", "http://192.168.210.97:7783/services/CBSInterfaceAccountMgrService");
		headers.setHeader("Content-type", " application/soap+xml;charset=\"utf-8\" ");

		envelope.setPrefix("soap");
		header.setPrefix("soap");
		soapBody.setPrefix("soap");

		SOAPElement acc = soapBody.addChildElement("QueryBalanceRequestMsg", "acc");

		SOAPElement reqHeader = acc.addChildElement("RequestHeader", "", null);

		createSOAPElement(reqHeader, "CommandId", "1", "com");
		createSOAPElement(reqHeader, "Version", "1", "com");
		createSOAPElement(reqHeader, "TransactionId", "1", "com");
		createSOAPElement(reqHeader, "SequenceId", "1", "com");
		createSOAPElement(reqHeader, "RequestType", "Event", "com");
		createSOAPElement(reqHeader, "SerialNo", "" + req.getMsisdn(), "com");

		SOAPElement sessionEntity = reqHeader.addChildElement("SessionEntity", "com");
		createSOAPElement(sessionEntity, "Name", config.getCbsUsername(), "com");
		createSOAPElement(sessionEntity, "Password", config.getCbsPassword(), "com");
		createSOAPElement(sessionEntity, "RemoteAddress", "127.0.0.1", "com");

		SOAPElement adjustAccount = acc.addChildElement("QueryBalanceRequest", "", null);
		createSOAPElement(adjustAccount, "SubscriberNo", req.getMsisdn(), "acc1");
		soapMessage.saveChanges();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		soapMessage.writeTo(out);
		String respMsg = new String(out.toByteArray());
		log.info(respMsg);

		return soapMessage;

	}

	public SOAPMessage createChargingSOAPRequest(AccountTxRequest req) throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage soapMessage = messageFactory.createMessage();
		SOAPPart soapPart = soapMessage.getSOAPPart();

		String accApi = "http://www.huawei.com/bme/cbsinterface/cbs/accountmgrmsg";
		String comApi = "http://www.huawei.com/bme/cbsinterface/common";
		String acc1Api = "http://www.huawei.com/bme/cbsinterface/cbs/accountmgr";

		// String acc1Api = "http://www.huawei.com/bme/cbsinterface/cbs/accountmgr";

		SOAPEnvelope envelope = soapPart.getEnvelope();

		envelope.addNamespaceDeclaration("acc", accApi);
		envelope.addNamespaceDeclaration("com", comApi);
		envelope.addNamespaceDeclaration("acc1", acc1Api);

		SOAPBody soapBody = envelope.getBody();

		SOAPElement acc = soapBody.addChildElement("AdjustAccountRequestMsg", "acc");
		SOAPElement reqHeader = acc.addChildElement("RequestHeader", "", null);
		createSOAPElement(reqHeader, "CommandId", "ActiveFirst", "com");
		createSOAPElement(reqHeader, "Version", "1", "com");
		createSOAPElement(reqHeader, "TransactionId", "1", "com");
		createSOAPElement(reqHeader, "SequenceId", "1", "com");
		createSOAPElement(reqHeader, "RequestType", "1", "com");
		SOAPElement sessionEntity = reqHeader.addChildElement("SessionEntity", "com");
		createSOAPElement(sessionEntity, "Name", config.getCbsUsername(), "com");
		createSOAPElement(sessionEntity, "Password", config.getCbsPassword(), "com");
		createSOAPElement(sessionEntity, "RemoteAddress", "127.0.0.1", "com");
		createSOAPElement(reqHeader, "SerialNo", req.getMsisdn(), "com");

		SOAPElement adjustAccount = acc.addChildElement("AdjustAccountRequest", "", null);
		createSOAPElement(adjustAccount, "SubscriberNo", req.getMsisdn(), "acc1");
		createSOAPElement(adjustAccount, "OperateType", "2", "acc1");
		createSOAPElement(adjustAccount, "AdditionalInfo", "IvrQuez", "acc1");
		SOAPElement feeEntity = adjustAccount.addChildElement("ModifyAcctFeeList", "acc1");
		createSOAPElement(feeEntity, "AccountType", "1", "acc1");
		createSOAPElement(feeEntity, "CurrAcctChgAmt", "1", "acc1");
		createSOAPElement(adjustAccount, "NotifyFlag", "1", "acc1");

		/*
		 * MimeHeaders headers = soapMessage.getMimeHeaders();
		 * headers.addHeader("SOAPAction", serverURI + "VerifyEmail");
		 */
		soapMessage.saveChanges();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		soapMessage.writeTo(out);
		String respMsg = new String(out.toByteArray());
		log.info(respMsg);
		return soapMessage;

	}

	public void chargeRequest(AccountTxRequest req) {
		try {

			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
			SOAPConnection soapConnection = soapConnectionFactory.createConnection();
			SOAPMessage soapResponse = soapConnection.call(createChargingSOAPRequest(req), config.getChargingUrl());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			soapResponse.writeTo(out);
			String respMsg = new String(out.toByteArray());
			log.info(respMsg);
			soapConnection.close();

		} catch (Exception e) {
			System.err.println(
					"\nError occurred while sending SOAP Request to Server!\nMake sure you have the correct endpoint URL and SOAPAction!\n");
			e.printStackTrace();
		}
	}

	public void getBalance(AccountTxRequest req) {
		try {

			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
			SOAPConnection soapConnection = soapConnectionFactory.createConnection();
			SOAPMessage soapRequest = createBalanceSOAPRequest(req);

			Iterator<MimeHeader> iterator = soapRequest.getMimeHeaders().getAllHeaders();
			while (iterator.hasNext()) {
				MimeHeader header = iterator.next();
				log.info("Mime Header Name: " + header.getName() + "|Value="+header.getValue());
				//System.out.println("Mime Header value: " + header.getValue());
			}

			SOAPMessage soapResponse = soapConnection.call(soapRequest, config.getBalanceUrl());

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			soapResponse.writeTo(out);
			String respMsg = new String(out.toByteArray());
			log.info(respMsg);
			soapConnection.close();

		} catch (Exception e) {
			System.err.println(
					"\nError occurred while sending SOAP Request to Server!\nMake sure you have the correct endpoint URL and SOAPAction!\n");
			e.printStackTrace();
		}
	}

	public SOAPElement createSOAPElement(SOAPElement base, String name, String value, String prefix)
			throws SOAPException {
		SOAPElement element = base.addChildElement(name, prefix);
		element.addTextNode(value);
		return element;
	}

	public static void main(String args[]) {
		try {
			// SoapUtil util = new SoapUtil();
			// util.createBalanceSOAPRequest();

		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

}
