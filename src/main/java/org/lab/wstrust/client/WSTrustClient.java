package org.lab.wstrust.client;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class WSTrustClient {

	private static final Logger log = LoggerFactory.getLogger(WSTrustClient.class);

	public Document jwtTokenRequest(String endpointUri, String clientId, String username, String password) {
		try {
			WSTrustRequestBuilder builder = new WSTrustRequestBuilder();
			Document doc = builder.build(endpointUri, clientId, username, password);
			String plain = transformDocument(doc);
			log.debug("Token request: {}", plain);
			HttpEntity entity = new ByteArrayEntity(plain.getBytes(Constants.ENCODING));
			CloseableHttpClient client = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(endpointUri);
			httpPost.setEntity(entity);
			httpPost.addHeader("Content-Type", Constants.CONTENT_TYPE);
			httpPost.addHeader("SOAPAction", Constants.SOAP_ACTION);
			CloseableHttpResponse response = client.execute(httpPost);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			Document document = factory.newDocumentBuilder().parse(response.getEntity().getContent());
			log.debug("Response: {}", transformDocument(document));
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new WSTrustException(String.format("Invalid status: %s", response.getStatusLine()));
			}
			return document;
		} catch (Exception ex) {
			throw new WSTrustException("JWT token request error", ex);
		}
	}

	public String transformDocument(Document doc) {
		try {
			StringWriter sw = new StringWriter();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, Constants.ENCODING);
			transformer.transform(new DOMSource(doc), new StreamResult(sw));
			return sw.toString();
		} catch (Exception ex) {
			throw new WSTrustException("XML transform error", ex);
		}
	}
}
