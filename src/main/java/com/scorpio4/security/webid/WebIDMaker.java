package com.scorpio4.security.webid;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.netscape.NetscapeCertRequest;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import sun.misc.BASE64Decoder;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Date;
import java.util.Hashtable;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.security.webid
 * User  : lee
 * Date  : 25/07/2014
 * Time  : 5:10 PM
 *
 * Based on the great examples by
 * @author Rene Mayrhofer
 *
 *
 */
public class WebIDMaker {
	long FROM = 1000L * 60 * 60 * 24; // 1 day
	long UNTIL = FROM * 365 * 10; // 10 years

	static {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}

	public WebIDMaker() {
	}

	public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
		return generate("RSA", 1024);
	}

	public KeyPair generate(String algorithm, int size) throws NoSuchAlgorithmException {
		java.security.KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
		keyPairGenerator.initialize(size, new SecureRandom());
		return keyPairGenerator.generateKeyPair();
	}

	public String getWebIDProfile(KeyPair keyPair) {
		PublicKey publicKey = keyPair.getPublic();
		return getWebIDProfile((RSAPublicKey)publicKey);
	}

	public String getWebIDProfile(RSAPublicKey publicKey) {
		return DatatypeConverter.printHexBinary(publicKey.getModulus().toByteArray());
	}

	public RSAPublicKey getPublicKey(String modulus$, String exponent$) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
		byte[] bytes = DatatypeConverter.parseHexBinary(modulus$);
		BigInteger modulus = new BigInteger(bytes);
		BigInteger exponent = new BigInteger(exponent$);
		return getPublicKey(modulus, exponent);
	}

	public RSAPublicKey getPublicKey(BigInteger modulus, BigInteger exponent) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
		RSAPublicKeySpec rsaPublicKey = new RSAPublicKeySpec(modulus, exponent);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return (RSAPublicKey) keyFactory.generatePublic(rsaPublicKey);
	}

	public X509Certificate selfCertificate(KeyPair keyPair, String webID, Hashtable _subjectDN) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException, CertificateException, NoSuchProviderException {
		return generateCertificate(keyPair.getPublic(), keyPair.getPrivate(), webID, new X509Principal(_subjectDN), getWebIDIssuer());
	}

	public X509Certificate generateCertificate(PublicKey publicKey, PrivateKey privateKey, String webID, X509Principal subjectDN, X509Principal webIDIssuer) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException, CertificateException, NoSuchProviderException {
		long now = System.currentTimeMillis();
		X509V3CertificateGenerator v3CertGen =  new X509V3CertificateGenerator();
		v3CertGen.setSerialNumber(BigInteger.valueOf(now));
		v3CertGen.setIssuerDN(webIDIssuer);
		v3CertGen.setNotBefore(new Date(now - FROM));
		v3CertGen.setNotAfter(new Date(now + UNTIL));
		v3CertGen.setSubjectDN(subjectDN);
		v3CertGen.setPublicKey(publicKey);
		v3CertGen.setSignatureAlgorithm("SHA1withRSA");

//		GeneralNames names = new GeneralNames(new GeneralName(GeneralName.uniformResourceIdentifier,webID));
//		v3CertGen.addExtension(X509Extensions.SubjectAlternativeName, false, names);
//		v3CertGen.addExtension(X509Extensions.KeyUsage, false, names);

		X509Certificate certificate = v3CertGen.generate(privateKey);
		certificate.verify(publicKey);
		return certificate;
	}

	public X509Certificate generateCertificate(Hashtable attribs, String spkacData, PrivateKey privateKey) throws IOException, CertificateEncodingException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		X509Principal x509Principal = new X509Principal(attribs);
		NetscapeCertRequest netscapeCertReq = new NetscapeCertRequest(Base64.decode(spkacData));
		PublicKey certPubKey = netscapeCertReq.getPublicKey();

		X509V3CertificateGenerator v3CertGen =  new X509V3CertificateGenerator();
		v3CertGen.setSubjectDN(x509Principal);
		v3CertGen.setPublicKey(certPubKey);
		X509Certificate cert = v3CertGen.generate(privateKey);
		return cert;
	}

	public static void storeCertificate(String certName, X509Certificate cert, Key key, String password, File file) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		file.getParentFile().mkdirs();
		if (file.exists()) {
			FileInputStream inputStream = new FileInputStream(file);
			keyStore.load(inputStream, password.toCharArray());
			inputStream.close();
		} else {
			keyStore.load(null, null);
		}
		keyStore.setKeyEntry(certName, key, password.toCharArray(),  new java.security.cert.Certificate[]{cert});
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		keyStore.store(fileOutputStream, password.toCharArray() );
		fileOutputStream.close();
	}

	public static X509Certificate loadCertificate(String certName, String password, File file) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		FileInputStream inputStream = new FileInputStream(file);
		keyStore.load(inputStream, password.toCharArray());
		X509Certificate certificate = (X509Certificate)keyStore.getCertificate(certName);
		return certificate;
	}

	public void encrypt(String key, byte[] encrypted, Writer out) throws Exception {

		BASE64Decoder b64 = new BASE64Decoder();

		AsymmetricKeyParameter publicKey = PublicKeyFactory.createKey(b64.decodeBuffer(key));
		AsymmetricBlockCipher rsaEngine = new RSAEngine();
		rsaEngine = new PKCS1Encoding(rsaEngine);
		rsaEngine.init(true, publicKey);

		byte[] hexEncodedCipher = rsaEngine.processBlock(encrypted, 0, encrypted.length);

		System.out.println(getHexString(hexEncodedCipher));
		out.write(getHexString(hexEncodedCipher));
	}

	public X509Principal getWebIDIssuer() {
		Hashtable attrs = new Hashtable();
		attrs.put(X509Principal.O, "{}");
		attrs.put(X509Principal.CN, "WebID");
		attrs.put(X509Principal.EmailAddress, "webid@scorpio4demo.com");
		return new X509Principal(attrs);
	}

	public static String getHexString(byte[] b) throws Exception {
		StringBuilder result = new StringBuilder();
		for (int i=0; i < b.length; i++) {
			result.append( Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 ) );
		}
		return result.toString();
	}

	public static boolean isSameKey(RSAPublicKey publicKey, RSAPublicKey publicKey2) {
		return publicKey.getModulus().equals( publicKey2.getModulus() )
			&& publicKey.getPublicExponent().equals( publicKey2.getPublicExponent() )
			&& publicKey.getAlgorithm().equals( publicKey2.getAlgorithm() );
	}


//	public void sign(X509Certificate certificate, CipherParameters caPrivateKey) throws IOException, InvalidCipherTextException {
//		SHA1Digest sha = new SHA1Digest();
//		AsymmetricBlockCipher rsa = new PKCS1Encoding(new RSAEngine());
//		ByteArrayOutputStream   byteOut = new ByteArrayOutputStream();
//		DEROutputStream         derOut = new DEROutputStream(byteOut);
//		derOut.writeObject(certificate);
//		byte[] signature;
//		byte[] certBlock = byteOut.toByteArray();
//		// first create digest
//		sha.update(certBlock, 0, certBlock.length);
//		byte[] hash = new byte[sha.getDigestSize()];
//		sha.doFinal(hash, 0);
//		// and sign that
//		rsa.init(true, caPrivateKey);
//		DigestInfo dInfo = new DigestInfo( new AlgorithmIdentifier(X509ObjectIdentifiers.id_SHA1, null), hash);
//		byte[] digest = dInfo.getEncoded(ASN1Encodable.DER);
//		signature = rsa.processBlock(digest, 0, digest.length);
//
//		v.add(tbsCert);
//		v.add(sigAlgId);
//		v.add(new DERBitString(signature));
//	}

//	public void write(KeyStore store, X509Certificate known, X509Certificate agent) throws NoSuchProviderException, CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, KeyStoreException, IOException {
//		ASN1EncodableVector  asn1EncodableVector = new ASN1EncodableVector();
//		X509CertificateObject clientCert = new X509CertificateObject(new X509CertificateStructure(new DERSequence(asn1EncodableVector)));
//		clientCert.verify(known.getPublicKey());
//
//		AsymmetricBlockCipher rsa = new PKCS1Encoding(new RSAEngine());
//		TBSCertificateStructure tbsCert = certGen.generateTBSCertificate();
//
//		asn1EncodableVector.add(tbsCert);
//		asn1EncodableVector.add(sigAlgId);
//		asn1EncodableVector.add(new DERBitString(signature));
//
//		PKCS12BagAttributeCarrier bagCert = clientCert;
//		bagCert.setBagAttribute( PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString("My friendly name for the new certificate"));
//		bagCert.setBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_localKeyId, new SubjectKeyIdentifierStructure(pubKey));
//
//		X509Certificate[] chain = new X509Certificate[2];
//		// first the client, then the CA certificate
//		chain[0] = agent;
//		chain[1] = known;
//
//		store.setKeyEntry("My friendly name for the new private key", privKey, exportPassword.toCharArray(), chain);
//	}

//	FileOutputStream fOut = new FileOutputStream(exportFile);
//	store.store(fOut, exportPassword.toCharArray());


}
