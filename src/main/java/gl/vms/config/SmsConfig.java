package gl.vms.config;

import gl.core.util.BaseConfig;

public class SmsConfig {
	public static String kannelURL = null;
	public static String pendingReqMsg = null;
	public static String existingSubscriberMsg = null;
	public static String alreayNonSubscriberMsg = null;
	public static String validNumber = null;
	public static String subUrl = null;
	public static String unSubUrl = null;
	public static String unSubReq = null;

	public SmsConfig(String fileName) {
		try {

			BaseConfig baseConfig = new BaseConfig(fileName);
			loadProperties(baseConfig);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void loadProperties(BaseConfig baseConfig) {
		this.kannelURL = baseConfig.getParamValue("kannel-url");
		this.pendingReqMsg = baseConfig.getParamValue("pending-req-message");
		this.existingSubscriberMsg = baseConfig.getParamValue("already-sub-message");
		this.alreayNonSubscriberMsg = baseConfig.getParamValue("already-nosub-message");
		this.validNumber = baseConfig.getParamValue("valid-number");
		this.subUrl = baseConfig.getParamValue("sub-url");
		this.unSubUrl = baseConfig.getParamValue("un-sub-url");
		this.unSubReq=baseConfig.getParamValue("unsubReq");
	}

}
