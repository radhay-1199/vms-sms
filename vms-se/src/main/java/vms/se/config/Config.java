package vms.se.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@PropertySource({ "classpath:vms.properties" })
public class Config {

	/*
	 * @Autowired private Environment env;
	 */

	@Value("${vms.sub.url}")
	private String subApiURL;

	@Value("${vms.unsub.url}")
	private String unSubApiURL;

	@Value("${vms.balance.url}")
	private String balanceUrl;

	@Value("${vms.charging.url}")
	private String chargingUrl;

/*	@Value("${vms.hlr.username}")
	private String hlrUsername;

	@Value("${vms.hlr.password}")
	private String hlrPassword;
*/
	@Value("${vms.balance.commandId}")
	private String balanceCommandId;
	
	@Value("${vms.balance.remoteAddress}")
	private String balanceRemoteAddress;
	
	@Value("${vms.balance.Version}")
	private String balanceVersion;
	
	@Value("${vms.balance.RequestType}")
	private String balanceRequestType;
	
	
	@Value("${vms.charging.RequestType}")
	private String chargingRequestType;
	
	@Value("${vms.charging.remark}")
	private String chargingRemark;
	
	
	@Value("${vms.charging.ac.type}")
	private String chargingAcType;
	
	@Value("${vms.charging.commandId}")
	private String chargingCommandId;
	
	public String getChargingCommandId() {
		return chargingCommandId;
	}

	public void setChargingCommandId(String chargingCommandId) {
		this.chargingCommandId = chargingCommandId;
	}

	public String getChargingAcType() {
		return chargingAcType;
	}

	public void setChargingAcType(String chargingAcType) {
		this.chargingAcType = chargingAcType;
	}

	public String getBalanceCommandId() {
		return balanceCommandId;
	}

	public String getChargingRequestType() {
		return chargingRequestType;
	}

	public void setChargingRequestType(String chargingRequestType) {
		this.chargingRequestType = chargingRequestType;
	}

	public String getChargingRemark() {
		return chargingRemark;
	}

	public void setChargingRemark(String chargingRemark) {
		this.chargingRemark = chargingRemark;
	}

	public void setBalanceCommandId(String balanceCommandId) {
		this.balanceCommandId = balanceCommandId;
	}

	public String getBalanceRemoteAddress() {
		return balanceRemoteAddress;
	}

	public void setBalanceRemoteAddress(String balanceRemoteAddress) {
		this.balanceRemoteAddress = balanceRemoteAddress;
	}

	public String getBalanceVersion() {
		return balanceVersion;
	}

	public void setBalanceVersion(String balanceVersion) {
		this.balanceVersion = balanceVersion;
	}

	public String getBalanceRequestType() {
		return balanceRequestType;
	}

	public void setBalanceRequestType(String balanceRequestType) {
		this.balanceRequestType = balanceRequestType;
	}

	@Value("${vms.cbs.username}")
	private String cbsUsername;

	@Value("${vms.cbs.password}")
	private String cbsPassword;

	public String getSubApiURL() {
		return subApiURL;
	}

	public void setSubApiURL(String subApiURL) {
		this.subApiURL = subApiURL;
	}

	public String getUnSubApiURL() {
		return unSubApiURL;
	}

	public void setUnSubApiURL(String unSubApiURL) {
		this.unSubApiURL = unSubApiURL;
	}

	public String getBalanceUrl() {
		return balanceUrl;
	}

	public void setBalanceUrl(String balanceUrl) {
		this.balanceUrl = balanceUrl;
	}

	public String getChargingUrl() {
		return chargingUrl;
	}

	public void setChargingUrl(String chargingUrl) {
		this.chargingUrl = chargingUrl;
	}

/*	public String getHlrUsername() {
		return hlrUsername;
	}

	public void setHlrUsername(String hlrUsername) {
		this.hlrUsername = hlrUsername;
	}

	public String getHlrPassword() {
		return hlrPassword;
	}

	public void setHlrPassword(String hlrPassword) {
		this.hlrPassword = hlrPassword;
	}
*/
	public String getCbsUsername() {
		return cbsUsername;
	}

	public void setCbsUsername(String cbsUsername) {
		this.cbsUsername = cbsUsername;
	}

	public String getCbsPassword() {
		return cbsPassword;
	}

	public void setCbsPassword(String cbsPassword) {
		this.cbsPassword = cbsPassword;
	}

}
