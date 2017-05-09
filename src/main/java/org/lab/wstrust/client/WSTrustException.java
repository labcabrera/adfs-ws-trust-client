package org.lab.wstrust.client;

@SuppressWarnings("serial")
public class WSTrustException extends RuntimeException {

	public WSTrustException(String message) {
		super(message);
	}

	public WSTrustException(String message, Throwable ex) {
		super(message, ex);
	}

}
