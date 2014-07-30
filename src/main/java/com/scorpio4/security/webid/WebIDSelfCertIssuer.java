package com.scorpio4.security.webid;

import com.scorpio4.util.IdentityHelper;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.bouncycastle.jce.X509Principal;

import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import java.util.Map;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.security.webid
 * User  : lee
 * Date  : 26/07/2014
 * Time  : 1:36 PM
 */
public class WebIDSelfCertIssuer implements Processor {

	/**
	 * Generate a self-signed certificate.
	 * A unique URI is generated for each login.
	 * The certificate's subject attributes and SPKAC are expected in the header.
	 *
	 * @param exchange the message exchange
	 * @throws Exception if an internal processing error has occurred.
	 */
	@Override
	public void process(Exchange exchange) throws Exception {
		Message in = exchange.getIn();
		Map<String,Object> headers = in.getHeaders();
		String c = (String) headers.get("c");
		assert c!=null && !c.equals("");
		String cn = (String)headers.get("cn");
		assert cn!=null && !cn.equals("");
		String o = (String)headers.get("o");
		assert o!=null && !o.equals("");
		String ou = (String)headers.get("ou");
		assert ou!=null && !ou.equals("");
		String newSPKAC = (String)headers.get("newSPKAC");
		assert newSPKAC!=null && !newSPKAC.equals("");

		Hashtable subjectAttributes = new Hashtable();
		subjectAttributes.put(X509Principal.CN, cn);
		subjectAttributes.put(X509Principal.C, c);
		subjectAttributes.put(X509Principal.O, o);
		subjectAttributes.put(X509Principal.OU, ou);

		X509Certificate certificate = selfCertificate(cn, subjectAttributes);
		Message out = exchange.getOut();
		out.setHeaders(headers);
		out.setBody( certificate.getEncoded() );
		out.setHeader(Exchange.CONTENT_TYPE, "application/x-x509-user-cert");
		out.setHeader("Pragma", "No-Cache");
		out.setHeader("EXPIRES", -1);
	}

	X509Certificate selfCertificate(String newSPKAC, Hashtable subjectAttributes) throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException, SignatureException, InvalidKeyException {
		String cn = (String) subjectAttributes.get(X509Principal.CN);
		String uri = IdentityHelper.uuid("http://scorpio4.com/webid/");
		WebIDMaker webIDMaker = new WebIDMaker();
		KeyPair keyPair = webIDMaker.generateKeyPair();
		PrivateKey privateKey = keyPair.getPrivate();
		X509Certificate cert = webIDMaker.generateCertificate(subjectAttributes, newSPKAC, privateKey);
		File keyFile = new File( "webid"+File.separator+uri);
		WebIDMaker.storeCertificate(cn, cert, privateKey, "scorpio4", keyFile);

		return cert;
	}

}
