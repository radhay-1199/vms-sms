package gl.core.util;

import java.io.FileInputStream;
import java.util.Properties;

public class BaseConfig
{
    Properties properties;
    
    public BaseConfig(final String configFile) throws Exception {
        this.properties = (Properties)new CaselessProperties();
        final FileInputStream propsFile = new FileInputStream(configFile);
        this.properties.load(propsFile);
        propsFile.close();
    }
    
    public String getParamValue(final String param_name) {
        String param_value = null;
        param_value = this.properties.getProperty(param_name);
        if (param_value != null) {
            param_value = param_value.trim();
        }
        return param_value;
    }
    
    public String getParamValue(final String param_name, final String defautValue) {
        String param_value = null;
        param_value = this.properties.getProperty(param_name);
        if (param_value != null) {
            return param_value.trim();
        }
        return defautValue;
    }
    
    public boolean getBooleanProperty(final String propName) {
        String value = this.getParamValue(propName);
        if (value == null) {
            return false;
        }
        value = value.trim();
        return value.equalsIgnoreCase("true");
    }
    
    public boolean getBooleanProperty(final String propName, final boolean defaultValue) {
        String value = this.getParamValue(propName);
        System.out.println("boolean Property [" + propName + "] " + value);
        if (value == null) {
            return defaultValue;
        }
        value = value.trim();
        return value.equalsIgnoreCase("true");
    }
    
    public int getIntProperty(final String propName, final int defaultValue) {
        final String value = this.properties.getProperty(propName, Integer.toString(defaultValue)).trim();
        return Integer.parseInt(value);
    }
    
    public byte getByteProperty(final String propName, final byte defaultValue) {
        final String temp = this.getParamValue(propName);
        if (temp == null) {
            return defaultValue;
        }
        return Byte.parseByte(temp);
    }
    
    public char getCharProperty(final String propName, final char defaultValue) {
        final char value = this.properties.getProperty(propName, Integer.toString(defaultValue)).trim().toCharArray()[0];
        return value;
    }
}