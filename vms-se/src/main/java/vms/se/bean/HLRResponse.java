package vms.se.bean;

public class HLRResponse {
	
	private int respCode ;
	private String msisdn ; 
	private String outputCode ;
	private String outputMessage;
	
	private String output2Code ;
	private String output2Message;
	
	
	
	public int getRespCode() {
		return respCode;
	}
	public void setRespCode(int respCode) {
		this.respCode = respCode;
	}
	public String getMsisdn() {
		return msisdn;
	}
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}
	public String getOutputCode() {
		return outputCode;
	}
	public void setOutputCode(String outputCode) {
		this.outputCode = outputCode;
	}
	public String getOutputMessage() {
		return outputMessage;
	}
	public void setOutputMessage(String outputMessage) {
		this.outputMessage = outputMessage;
	}
	public String getOutput2Code() {
		return output2Code;
	}
	public void setOutput2Code(String output2Code) {
		this.output2Code = output2Code;
	}
	public String getOutput2Message() {
		return output2Message;
	}
	public void setOutput2Message(String output2Message) {
		this.output2Message = output2Message;
	}
	@Override
	public String toString() {
		return "HLRResponse [respCode=" + respCode + ", msisdn=" + msisdn + ", outputCode=" + outputCode
				+ ", outputMessage=" + outputMessage + ", output2Code=" + output2Code + ", output2Message="
				+ output2Message + "]";
	}
	
}
