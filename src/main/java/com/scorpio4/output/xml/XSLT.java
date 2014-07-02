package com.scorpio4.output.xml;

import com.scorpio4.output.IOProcessor;
import groovy.text.Template;
import groovy.text.XmlTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;

public class XSLT implements IOProcessor {
	private static final Logger log = LoggerFactory.getLogger(XSLT.class);

	private Transformer transformer;

	public XSLT(String xslt) throws FileNotFoundException, TransformerConfigurationException {
		init(getClass().getResourceAsStream(xslt));
	}

	public XSLT(File xslt_src) throws FileNotFoundException, TransformerConfigurationException {
		init(new FileInputStream(xslt_src));
	}

	public XSLT(InputStream xslt_src) throws FileNotFoundException, TransformerConfigurationException {
		init(xslt_src);
	}

	public XSLT(InputStream inp, String xslt_src, OutputStream out) throws TransformerConfigurationException, IOException {
		init(getClass().getResourceAsStream(xslt_src));
		process(inp,out);
	}

	public XSLT(Source inp, InputStream xslt_src, OutputStream out) throws IOException, TransformerConfigurationException {
		init(xslt_src);
		process(inp,out);
	}

	public void init(InputStream xslt_src) throws TransformerConfigurationException {
		this.transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(xslt_src));
	}

	public void process(String text, Map binding, OutputStream out) throws IOException {
		try {
			StringWriter writer = new StringWriter();
			XmlTemplateEngine engine = new XmlTemplateEngine();
			Template template = engine.createTemplate(text);
			template.make(binding).writeTo(writer);
			String gtext = writer.toString();
			process( new StreamSource(new StringReader(gtext)), out );
//			return gtext;
		} catch(Exception e) {
			log.error("process error", e);
//			return null;
		}
	}

	/* Implements IO interface */

	public void process(InputStream inp, OutputStream out) throws IOException {
		Source source;

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setValidating(false);
			factory.setFeature("http://xml.org/sax/features/validation", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

			source = new DOMSource(factory.newDocumentBuilder().parse(new InputSource(inp)));
		} catch (ParserConfigurationException e) {
			log.error("ParserConfigurationException", e);
			source = new StreamSource(inp);
		} catch (SAXException e) {
			log.error("SAX Exception", e);
			source = new StreamSource(inp);
		}

		process(source, out);
	}

	public void process(Source src, OutputStream out) throws IOException {
		try {
			try {
				// Setup Identity Transformer

				Result res = new StreamResult(out);
				// Start XSLT transformation and FOP processing
				transformer.transform(src, res);
			} finally {
				out.close();
			}
		} catch(UndeclaredThrowableException ute) {
			log.error("Error processing XSLT", ute);
        } catch (TransformerException e) {
            // An error occurred while applying the XSL tools
            // Get location of error in input tools
            SourceLocator locator = e.getLocator();
			if (locator!=null) {
				int col = locator.getColumnNumber();
				int line = locator.getLineNumber();
				String publicId = locator.getPublicId();
				String systemId = locator.getSystemId();
				Object[] args = { col, line, publicId, systemId };
				log.error("Transformer exception in ({}, {}): [publicId: {}, systemId: {}]" , args);
			} else {
				log.error("Transformer Exception occurred", e);
			}
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
	}
	
}
