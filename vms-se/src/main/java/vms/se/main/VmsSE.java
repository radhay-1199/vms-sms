package vms.se.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import vms.se.service.ProcessAccountRequest;
import vms.se.service.ProcessHLRRequest;

import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@ComponentScan(basePackages = "vms.se")

public class VmsSE {
	
	protected final Logger log = LoggerFactory.getLogger(getClass());

	public static void main(String[] args) {

		ConfigurableApplicationContext ctx = SpringApplication.run( VmsSE.class, args );
		
		ProcessHLRRequest processReq = ctx.getBean(ProcessHLRRequest.class);
		new Thread(processReq).start();
		
		ProcessAccountRequest accReq = ctx.getBean(ProcessAccountRequest.class);
		new Thread(accReq).start();
		
		
		/*CheckAndGetEvents check = ctx.getBean(CheckAndGetEvents.class);
		new Thread(check).start();
*/
		/*
		 * Thread renewer = new Thread(ctx.getBean(Renewer.class)); renewer.start();
		 */
		
		
	}

}
