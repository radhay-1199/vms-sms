package vms.se.bean;

public class AccountTxResponse {
	
	String code;
	String desc;
	String accountType;
	long balance;
	

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public long getBalance() {
		return balance;
	}

	public void setBalance(long balance) {
		this.balance = balance;
	}

	@Override
	public String toString() {
		return "AccountTxResponse [" + (code != null ? "code=" + code + ", " : "")
				+ (desc != null ? "desc=" + desc + ", " : "")
				+ (accountType != null ? "accountType=" + accountType + ", " : "") + "balance=" + balance + "]";
	}
	
}
