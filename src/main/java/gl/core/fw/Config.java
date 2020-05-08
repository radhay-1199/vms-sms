package gl.core.fw;

import gl.core.util.BaseConfig;

public class Config extends BaseConfig
{
    public static int udp_port;
    public static String db_conn;
    public static String db_user;
    public static String db_pass;
    public static String inputType;
    public static int workerThread;
    
    public Config(final String fileName) throws Exception {
        super(fileName);
    }
    
    public void loadProperties() {
        Config.udp_port = this.getIntProperty("udp-port", Config.udp_port);
        Config.db_user = this.getParamValue("dbuser");
        Config.db_pass = this.getParamValue("dbpassword");
        Config.db_conn = this.getParamValue("db-conn");
        Config.workerThread = this.getIntProperty("workerthread", Config.workerThread);
    }
    
    static {
        Config.udp_port = 0;
        Config.db_conn = null;
        Config.db_user = null;
        Config.db_pass = null;
        Config.inputType = null;
        Config.workerThread = 1;
    }
}