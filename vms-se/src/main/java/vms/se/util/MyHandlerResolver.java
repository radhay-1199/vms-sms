/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vms.se.util;

import java.util.ArrayList;
import java.util.List;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;

import org.springframework.stereotype.Component;


public class MyHandlerResolver implements HandlerResolver {  
	ArrayList<MyHandler> handlerChain;  
	 
	public MyHandlerResolver(ArrayList<MyHandler> list){
		this.handlerChain = list;
	}
    public List getHandlerChain(PortInfo portInfo) {  
        return handlerChain;  
    }  
   
} 