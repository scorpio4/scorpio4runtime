package com.factcore.output.barcode;
/*
 *   Fact:Core - CONFIDENTIAL
 *   Unpublished Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *   NOTICE:  All information contained herein is, and remains the property of Lee Curtis. The intellectual and technical concepts contained
 *   herein are proprietary to Lee Curtis and may be covered by Australian, U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law.
 *   Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 *   from Lee Curtis.  Access to the source code contained herein is hereby forbidden to anyone except current Lee Curtis employees, managers or contractors who have executed
 *   Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 *   The copyright notice above does not evidence any actual or intended publication or disclosure  of  this source code, which includes
 *   information that is confidential and/or proprietary, and is a trade secret, of Lee Curtis.   ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC  PERFORMANCE,
 *   OR PUBLIC DISPLAY OF OR THROUGH USE  OF THIS  SOURCE CODE  WITHOUT  THE EXPRESS WRITTEN CONSENT OF LEE CURTIS IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE
 *   LAWS AND INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF  THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS
 *   TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT  MAY DESCRIBE, IN WHOLE OR IN PART.
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
