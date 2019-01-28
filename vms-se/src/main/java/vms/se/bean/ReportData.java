package vms.se.bean;

public class ReportData {
	private String msisdn;
	private int action;
	private int status;
	private String channel;
	private String remark;
	private String tid ;

	
	public ReportData(String msisdn, int action, int status, String channel, String remark , String tid) {
		super();
		this.msisdn = msisdn;
		this.action = action;
		this.status = status;
		this.channel = channel;
		this.remark = remark;
		this.tid = tid ;
	}

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

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}
	
	

}
