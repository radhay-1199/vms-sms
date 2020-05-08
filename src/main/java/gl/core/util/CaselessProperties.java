package gl.core.util;

import java.util.Properties;

public class CaselessProperties extends Properties
{
    @Override
    public Object put(final Object key, final Object value) {
        final String lowercase = ((String)key).toLowerCase();
        return super.put(lowercase, value);
    }
    
    @Override
    public String getProperty(final String key) {
        final String lowercase = key.toLowerCase();
        return super.getProperty(lowercase);
    }
    
    @Override
    public String getProperty(final String key, final String defaultValue) {
        final String lowercase = key.toLowerCase();
        return super.getProperty(lowercase, defaultValue);
    }
}