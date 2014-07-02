package com.factcore.output.pdf
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
import com.factcore.output.IOProcessor;

import java.io.*
import javax.xml.transform.*
import javax.xml.transform.stream.*

public class XHTML2FO implements IOProcessor {
	Source xslt = null;

	public XHTML2FO(InputStream inp, OutputStream out) throws IOException {
		InputStream xslt_src = getClass().getResourceAsStream("/xhtml2fo2.xsl");
		xslt = new StreamSource(xslt_src);
		process(inp,out);
	}

	public void process(InputStream inp, OutputStream out) throws IOException {
		try {
			out = new java.io.BufferedOutputStream(out);
			try {
				TransformerFactory factory = TransformerFactory.newInstance();
				Transformer transformer = factory.newTransformer(xslt); // identity transformer
				// Setup input for XSLT transformation
				Source src = new StreamSource(inp);

				// Resulting SAX events (the generated FO) must be piped through to FOP
				Result res = new StreamResult(out);
				// Start XSLT transformation and FOP processing
				transformer.transform(src, res);
			} finally {
				out.close();
			}
		} catch(java.lang.reflect.UndeclaredThrowableException ute) {
			ute.getCause().printStackTrace();
        } catch (TransformerException e) {
            // An error occurred while applying the XSL tools
            // Get location of error in input tools
            SourceLocator locator = e.getLocator();
            int col = locator.getColumnNumber();
            int line = locator.getLineNumber();
            String publicId = locator.getPublicId();
            String systemId = locator.getSystemId();
		} catch (Exception e) {
			e.printStackTrace();
		}
//		return out;
	}
	
}
