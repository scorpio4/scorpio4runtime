package com.scorpio4.output.xml;

import java.io.*;
import java.util.*

import groovy.text.*

import javax.xml.transform.stream.*

public class HTML  {

	public HTML() {
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

	public Object process(String text, Map binding, Writer writer) throws IOException {
		XmlTemplateEngine engine = new XmlTemplateEngine();
		Template template = engine.createTemplate(text);
		template.make(binding).writeTo(writer);
		return writer.toString();
	}

}
