/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vms.se.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.springframework.stereotype.Component;
   

public class MyHandler implements SOAPHandler<SOAPMessageContext> { 
	private OutputStream out;
	
	public MyHandler(OutputStream out){
		this.out=out;
	}
	
    public boolean handleMessage(SOAPMessageContext smc) { 
    	System.out.println("+++++++in handleMessage++++++");
    	logToSystemOut(smc);
        return true;
   
    } 
   
    public Set getHeaders() { 
        return null; 
    } 
   
    public boolean handleFault(SOAPMessageContext smc) { 
    	System.out.println("+++++++in handleFault++++++");
    	logToSystemOut(smc);
        return true;
    } 
   
    public void close(MessageContext context) { 
    } 
    
    private void logToSystemOut(SOAPMessageContext smc) {
        Boolean outboundProperty = (Boolean)
            smc.get (MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        try {
        	if (outboundProperty.booleanValue()) {
				out.write("\nOutbound message:----".getBytes());
	        } else {
	            out.write("\nInbound message:-----".getBytes());
	        }
        	out.write("\n".getBytes());
        } catch (IOException e) {
			e.printStackTrace();
		}

        SOAPMessage message = smc.getMessage();
        try {
            message.writeTo(out);
            out.write("\n".getBytes());   // just to add a newline
        } catch (Exception e) {
        	System.out.println("Exception in handler: " + e);
        }
    }
    
} 
//</SOAPMessageContext>
