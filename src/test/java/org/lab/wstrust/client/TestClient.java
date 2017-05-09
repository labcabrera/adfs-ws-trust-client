package org.lab.wstrust.client;

import org.junit.Ignore;
import org.junit.Test;
import org.lab.wstrust.client.WSTrustClient;
import org.lab.wstrust.client.WSTrustResponseProcessor;
import org.w3c.dom.Document;

@Ignore("adfs required")
public class TestClient {

	@Test
	public void test() {
		WSTrustClient client = new WSTrustClient();
		WSTrustResponseProcessor responseProcessor = new WSTrustResponseProcessor();
		String uri = "https://my-adfs-host-name/adfs/services/trust/13/usernamemixed";
		String username = "domain\\username";
		String password = "password";
		String address = "https://my-application-domain";
		Document response = client.jwtTokenRequest(uri, address, username, password);
		String jwtToken = responseProcessor.process(response);
		System.out.println("Token:\n" + jwtToken);
	}
}
