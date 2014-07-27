package com.scorpio4.security.webid;

import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;

import java.security.cert.X509Certificate;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.security.webid
 * User  : lee
 * Date  : 25/07/2014
 * Time  : 9:20 PM
 */
public class WebID {
	X509Certificate cert;
	URI subject;
	Model model = new LinkedHashModel();

	public WebID(X509Certificate cert, URI subject) {
		this.cert=cert;
		this.subject=subject;
	}

	public X509Certificate getCertificate() {
		return cert;
	}

	public void setCertificate(X509Certificate cert) {
		this.cert = cert;
	}

	public URI getSubject() {
		return subject;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

}
