package vms.se.util;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component

public class HttpUtil {

	private Logger log = LogManager.getRootLogger();
	
	public String submitJsonReuqestToServer(String url, String json) {
		try {
			StringEntity input = new StringEntity(json);
			input.setContentType("application/json");

			HttpClient httpClient = HttpClients.createDefault();
			HttpPost postRequest = new HttpPost(url);
			postRequest.addHeader("Content-Type", "application/json");
			postRequest.setEntity(input);

			log.info("request:" + json);

			HttpResponse response = httpClient.execute(postRequest);
			int respCode = response.getStatusLine().getStatusCode();
			if (respCode == 200) {
				return EntityUtils.toString(response.getEntity());
			} else {
				log.info("Response Code=" + response.toString() );
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return null;
	}

	public String submitRequest(String url) {
		try {
	
			HttpClient httpClient = HttpClients.createDefault();
			HttpGet getRequest = new HttpGet(url);
			HttpResponse response = httpClient.execute(getRequest);
			int respCode = response.getStatusLine().getStatusCode();
			
			if (respCode == 200) {
				return EntityUtils.toString(response.getEntity());
			} else {
				log.info("Response Code=" + response.toString() );
			}
			
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return null;
	}

}
