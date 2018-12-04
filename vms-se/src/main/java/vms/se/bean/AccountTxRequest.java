package vms.se.bean;

public class AccountTxRequest {
	
	private int id ;
	private String msisdn ;
	private int action ; 
	private int status ;
	private int amount ;
	private String tid ;
	private String packId ;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public String getTid() {
		return tid;
	}
	public void setTid(String tid) {
		this.tid = tid;
	}
	
	public String getPackId() {
		return packId;
	}
	public void setPackId(String packId) {
		this.packId = packId;
	}
	@Override
	public String toString() {
		return "AccountRequest [id=" + id + ", " + (msisdn != null ? "msisdn=" + msisdn + ", " : "") + "action="
				+ action + ", status=" + status + ", amount=" + amount + ", " + (tid != null ? "tid=" + tid : "") + "]";
	}
}
