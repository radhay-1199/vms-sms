package vms.se.bean;

import java.util.Date;

public class VmsRenewalRequest {
	private String msisdn;
	private String packId;
	private int retryCount;
	private Date nextRetryTime;
	private String channel ; 
	

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getPackId() {
		return packId;
	}

	public void setPackId(String packId) {
		this.packId = packId;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public Date getNextRetryTime() {
		return nextRetryTime;
	}

	public void setNextRetryTime(Date nextRetryTime) {
		this.nextRetryTime = nextRetryTime;
	}

	@Override
	public String toString() {
		return "VmsRenewalRequest [" + (msisdn != null ? "msisdn=" + msisdn + ", " : "")
				+ (packId != null ? "packId=" + packId + ", " : "") + "retryCount=" + retryCount + ", "
				+ (nextRetryTime != null ? "nextRetryTime=" + nextRetryTime : "") + "]";
	}

}
