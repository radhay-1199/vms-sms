package vms.se.util;

import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


public class LogOutputStream extends OutputStream{
	
	 /** The logger where to log the written bytes. */
    private static Logger logger = LoggerFactory.getLogger(LogOutputStream.class);;

    /** The internal memory for the written bytes. */
    private String mem;




    /**
     * Writes a byte to the output stream. This method flushes automatically at the end of a line.
     *
     * @param b DOCUMENT ME!
     */
    public void write (int b) {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) (b & 0xff);
        mem = mem + new String(bytes);

        if (mem.endsWith ("\n")) {
            mem = mem.substring (0, mem.length () - 1);
            flush ();
        }
    }
    /**
     * Flushes the output stream.
     */
    public void flush () {
    	if(mem != null)
    		logger.error(mem) ;
        mem = "";
    }
}
