package com.scorpio4.output.pdf
/*
 *   Scorpio4 - Apache Licensed
 *   Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *
 */
import com.scorpio4.output.IOProcessor;

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
