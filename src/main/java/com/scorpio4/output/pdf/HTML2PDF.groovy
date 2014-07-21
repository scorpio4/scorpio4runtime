package com.scorpio4.output.pdf
/*
 *   Scorpio4 - Apache Licensed
 *   Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *
 */
import com.scorpio4.output.IOProcessor;
import groovy.text.Template
import groovy.text.XmlTemplateEngine
import org.apache.fop.apps.FOUserAgent
import org.apache.fop.apps.Fop
import org.apache.fop.apps.FopFactory
import org.apache.fop.apps.MimeConstants
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.xml.transform.sax.SAXResult
import javax.xml.transform.stream.StreamSource
import javax.xml.transform.*

public class HTML2PDF implements IOProcessor {
	private final static Logger log = LoggerFactory.getLogger(HTML2PDF.class)

	FopFactory fopFactory = FopFactory.newInstance();
	Source xslt = null;

	public HTML2PDF() {
		InputStream xslt_src = getClass().getResourceAsStream("/xhtml2fo_ux.xsl");
		xslt = new StreamSource(xslt_src);
	}
	
	public HTML2PDF(InputStream inp, OutputStream out) throws IOException {
		process(inp,out);
	}

	public HTML2PDF(Source inp, OutputStream out) throws IOException {
		process(inp,out);
	}


	public Object process(String text, Map binding, OutputStream out) throws IOException {
		StringWriter writer = new StringWriter();
		XmlTemplateEngine engine = new XmlTemplateEngine();
		Template template = engine.createTemplate(text);
		template.make(binding).writeTo(writer);
		String gtext = writer.toString();
		process( new StreamSource(new StringReader(gtext)), out );
		return gtext;
	}

	public void process(InputStream inp, OutputStream out) throws IOException {
		process(new StreamSource(inp), out);
	}

	public void process(Source src, OutputStream out) throws IOException {
		try {
			FOUserAgent foAgent = fopFactory.newFOconferenceAgent();
			out = new java.io.BufferedOutputStream(out);
			try {
				// Construct fop with desired output format and output stream
				Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foAgent, out);
				// Setup Identity Transformer
				TransformerFactory factory = TransformerFactory.newInstance();
				Transformer transformer = factory.newTransformer(xslt); // identity transformer

				// Setup input for XSLT transformation

				// Resulting SAX events (the generated FO) must be piped through to FOP
				Result res = new SAXResult(fop.getDefaultHandler());
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
			if (locator) {
				int col = locator.getColumnNumber();
				int line = locator.getLineNumber();
				String publicId = locator.getPublicId();
				String systemId = locator.getSystemId();
				log.error("($col, $line), $publicId, $systemId", e)
			} else {
				log.error("Null locator", e)
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
//		return out;
	}
	
}
