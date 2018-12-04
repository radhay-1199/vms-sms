package vms.se.main;

import java.util.ArrayList;

import javax.xml.ws.handler.HandlerResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.huawei.bme.cbsinterface.cbs.accountmgr.CBSInterfaceAccountMgrService;
import com.huawei.bme.cbsinterface.cbs.businessmgr.CBSInterfaceBusinessMgrService;


import vms.se.service.ProcessAccountRequest;
import vms.se.service.ProcessHLRRequest;
import vms.se.util.LogOutputStream;
import vms.se.util.MyHandler;
import vms.se.util.MyHandlerResolver;

import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@ComponentScan(basePackages = "vms.se")

public class VmsSE {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	public static void main(String[] args) {

		ConfigurableApplicationContext ctx = SpringApplication.run(VmsSE.class, args);

		ProcessHLRRequest processReq = ctx.getBean(ProcessHLRRequest.class);
		new Thread(processReq).start();

		ProcessAccountRequest accReq = ctx.getBean(ProcessAccountRequest.class);
		new Thread(accReq).start();

		/*
		 * CheckAndGetEvents check = ctx.getBean(CheckAndGetEvents.class); new
		 * Thread(check).start();
		 */
		/*
		 * Thread renewer = new Thread(ctx.getBean(Renewer.class)); renewer.start();
		 */

	}

	@Bean
	public CBSInterfaceAccountMgrService getCBSInterfaceAccountMgrService(HandlerResolver myHanlderResolver) {
		CBSInterfaceAccountMgrService bean = new CBSInterfaceAccountMgrService();
		bean.setHandlerResolver(myHanlderResolver);
		return bean;
	}

	@Bean
	public CBSInterfaceBusinessMgrService getCBSInterfaceBusinessMgrService(HandlerResolver myHanlderResolver) {
		CBSInterfaceBusinessMgrService bean = new CBSInterfaceBusinessMgrService();
		bean.setHandlerResolver(myHanlderResolver);
		return bean;
	}
	

	@Bean
	public HandlerResolver getHandlerResolver(MyHandler myHandler) {
		
		ArrayList<MyHandler> handlerChain = new ArrayList<MyHandler>();
		handlerChain.add(myHandler);
		return new MyHandlerResolver(handlerChain);
		
	}

	@Bean
	public MyHandler getMyHandler(LogOutputStream outStream) {
		return new MyHandler(outStream);
	}
	

	@Bean
	public LogOutputStream getLogOutputStream() {
		return new LogOutputStream();
	}

	
}
