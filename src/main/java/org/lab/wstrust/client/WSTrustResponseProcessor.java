package org.lab.wstrust.client;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class WSTrustResponseProcessor {

	private static final Logger log = LoggerFactory.getLogger(WSTrustResponseProcessor.class);

	public String process(Document response) {
		NodeList nodes = response.getElementsByTagNameNS(Constants.XMLNS_WS_SEC, "BinarySecurityToken");
		if (nodes.getLength() == 0) {
			throw new WSTrustException("Missing BinarySecurityToken element");
		}
		Element element = (Element) nodes.item(0);
		String b64 = element.getTextContent();
		String token = new String(Base64.decodeBase64(b64));
		log.debug("JWT token: {}", token);
		return token;
	}
}
