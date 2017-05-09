package org.lab.wstrust.client;

import static org.lab.wstrust.client.Constants.DATE_PATTERN;
import static org.lab.wstrust.client.Constants.XMLNS_ADDRESSING;
import static org.lab.wstrust.client.Constants.XMLNS_ENVELOPE;
import static org.lab.wstrust.client.Constants.XMLNS_SEC_UTIL;
import static org.lab.wstrust.client.Constants.XMLNS_TRUST;
import static org.lab.wstrust.client.Constants.XMLNS_WSP;
import static org.lab.wstrust.client.Constants.XMLNS_WS_SEC;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class WSTrustRequestBuilder {

	public Document build(String adfsEndpoint, String clientId, String username, String password) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			Element envelope = document.createElementNS(XMLNS_ENVELOPE, "s:Envelope");
			envelope.setAttribute("xmlns:a", XMLNS_ADDRESSING);
			envelope.setAttribute("xmlns:u", XMLNS_SEC_UTIL);
			document.appendChild(envelope);
			envelope.appendChild(createHeader(document, adfsEndpoint, username, password));
			envelope.appendChild(createBody(document, clientId));
			return document;
		} catch (Exception ex) {
			throw new WSTrustException("Request build error", ex);
		}
	}

	private Element createHeader(Document document, String adfsEndpoint, String username, String password) {
		Element header = document.createElementNS(XMLNS_ENVELOPE, "s:Header");

		Element action = document.createElementNS(XMLNS_ADDRESSING, "a:Action");
		action.setAttributeNS(XMLNS_ENVELOPE, "s:mustUnderstand", "1");
		action.setTextContent("http://docs.oasis-open.org/ws-sx/ws-trust/200512/RST/Issue");
		header.appendChild(action);

		Element messageID = document.createElementNS(XMLNS_ADDRESSING, "a:messageID");
		messageID.setTextContent("urn:uuid:" + UUID.randomUUID().toString());
		header.appendChild(messageID);

		Element replyTo = document.createElementNS(XMLNS_ADDRESSING, "a:ReplyTo");
		Element address = document.createElementNS(XMLNS_ADDRESSING, "a:Address");
		address.setTextContent("http://www.w3.org/2005/08/addressing/anonymous");
		replyTo.appendChild(address);
		header.appendChild(replyTo);

		Element to = document.createElementNS(XMLNS_ADDRESSING, "a:To");
		to.setAttributeNS(XMLNS_ENVELOPE, "s:mustUnderstand", "1");
		to.setTextContent(adfsEndpoint);
		header.appendChild(to);

		Element security = document.createElementNS(XMLNS_WS_SEC, "o:Security");
		security.setAttributeNS(XMLNS_ENVELOPE, "s:mustUnderstand", "1");
		security.setAttribute("xmlns:o", XMLNS_WS_SEC);
		header.appendChild(security);

		Date dateCreated = Calendar.getInstance().getTime();
		Date dateExpires = new Date(dateCreated.getTime() + 10 * 60 * 1000);
		DateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Element timestamp = document.createElementNS(XMLNS_SEC_UTIL, "u:Timestamp");
		Element created = document.createElementNS(XMLNS_SEC_UTIL, "u:Created");
		Element expires = document.createElementNS(XMLNS_SEC_UTIL, "u:Expires");
		timestamp.setAttributeNS(XMLNS_SEC_UTIL, "u:Id", "_0");
		created.setTextContent(dateFormat.format(dateCreated));
		expires.setTextContent(dateFormat.format(dateExpires));
		timestamp.appendChild(created);
		timestamp.appendChild(expires);
		security.appendChild(timestamp);

		Element usernameToken = document.createElementNS(XMLNS_WS_SEC, "o:UsernameToken");
		Element usernameElement = document.createElementNS(XMLNS_WS_SEC, "o:Username");
		Element passwordElement = document.createElementNS(XMLNS_WS_SEC, "o:Password");
		usernameElement.setTextContent(username);
		passwordElement.setTextContent(password);
		usernameToken.setAttributeNS(XMLNS_SEC_UTIL, "u:Id", "uuid-" + UUID.randomUUID().toString());
		usernameToken.appendChild(usernameElement);
		usernameToken.appendChild(passwordElement);
		security.appendChild(usernameToken);

		return header;
	}

	private Element createBody(Document document, String clientId) {
		Element body = document.createElementNS(XMLNS_ENVELOPE, "s:Body");

		Element requestSecurityToken = document.createElementNS(XMLNS_TRUST, "trust:RequestSecurityToken");
		requestSecurityToken.setAttribute("xmlns:trust", XMLNS_TRUST);
		body.appendChild(requestSecurityToken);

		Element appliesTo = document.createElementNS(XMLNS_WSP, "wsp:AppliesTo");
		appliesTo.setAttribute("xmlns:wsp", XMLNS_WSP);
		requestSecurityToken.appendChild(appliesTo);

		Element endpointReference = document.createElementNS(XMLNS_ADDRESSING, "a:EndpointReference");
		Element address = document.createElementNS(XMLNS_ADDRESSING, "a:Address");
		address.setTextContent(clientId);
		endpointReference.appendChild(address);
		appliesTo.appendChild(endpointReference);

		Element keyType = document.createElementNS(XMLNS_TRUST, "trust:KeyType");
		Element requestType = document.createElementNS(XMLNS_TRUST, "trust:RequestType");
		Element tokenType = document.createElementNS(XMLNS_TRUST, "trust:TokenType");
		keyType.setTextContent("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Bearer");
		requestType.setTextContent("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue");
		tokenType.setTextContent("urn:ietf:params:oauth:token-type:jwt");
		requestSecurityToken.appendChild(keyType);
		requestSecurityToken.appendChild(requestType);
		requestSecurityToken.appendChild(tokenType);

		return body;
	}

}
