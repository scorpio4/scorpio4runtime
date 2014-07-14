package com.scorpio4.output.barcode;
/*
 *   Scorpio4 - Apache Licensed
 *   Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *
 */

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class Barcoder {
	int width = 400;
	int height = 400;

	public Barcoder() {
	}

	public Barcoder(int width, int height) {
		this.width=width;
		this.height=height;
	}

    public void qr(String payload, HttpServletResponse resp) throws IOException {
    	qr(payload, resp.getOutputStream() );
    }

    public void qr(String payload, OutputStream out) throws IOException {
	    Charset charset = Charset.forName("ISO-8859-1");
	    CharsetEncoder encoder = charset.newEncoder();
	    byte[] b = null;
	    try {
	        // Convert a string to ISO-8859-1 bytes in a ByteBuffer
	        ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(payload));
	        b = bbuf.array();
	    } catch (CharacterCodingException e) {
	        System.out.println(e.getMessage());
	    }
	
	    String data = null;
	    try {
	        data = new String(b, "ISO-8859-1");
	    } catch (UnsupportedEncodingException e) {
	        System.out.println(e.getMessage());
	        return;
	    }
	
	    // get a byte matrix for the data
	    com.google.zxing.common.BitMatrix matrix = null;
	    com.google.zxing.Writer writer = new com.google.zxing.qrcode.QRCodeWriter();
	    try {
	        matrix = writer.encode(data, com.google.zxing.BarcodeFormat.QR_CODE, width, height);
	    } catch (com.google.zxing.WriterException e) {
	        System.out.println(e.getMessage());
	    }

		com.google.zxing.client.j2se.MatrixToImageWriter.writeToStream(matrix, "PNG", out);
    }
	
}
