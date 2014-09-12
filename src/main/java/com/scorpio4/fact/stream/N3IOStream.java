package com.scorpio4.fact.stream;
/*
 *   Scorpio4 - Apache Licensed
 *   Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *
 */

import com.scorpio4.util.DateXSD;
import com.scorpio4.vocab.COMMONS;

import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Scorpio4 (c) 2010-2013
 * @author lee
 * Date: 18/01/13
 * Time: 9:10 AM
 * <p/>
 */
public class N3IOStream implements FactStream {

	DateXSD date2XSD = new DateXSD();
	String lastSubject = null;
	private boolean useHeaders = true;
	private boolean useComments = true;
	private Map<String,String> prefices = new HashMap();
	PrintWriter writer;

	public N3IOStream(PrintWriter writer) {
		this.writer=writer;
		init();
	}

	private void init() {
		setUseComments(true);
		setUseHeaders(true);
		setPrefix("rdf", COMMONS.RDF);
		setPrefix("rdfs", COMMONS.RDFS);
		setPrefix("xsd", COMMONS.XSD);

	}

	public void prolog(String header) {
		for(Map.Entry prefix: prefices.entrySet()) {
			writer.print("@prefix ");
			writer.print(prefix.getKey());
			writer.print(": <");
			writer.print(prefix.getValue());
			writer.print(">.\n");
		}
		if (!isUsingComments()) return;
		writer.print("\n#\n");
		heading(header);
		writer.print("# Created: ");
		writer.print(new Date());
		writer.print("\n#\n");
	}

	public N3IOStream heading(String header) {
		if (!isUsingHeaders()) return this;
		writer.print("# ");
		writer.print(header);
		writer.print("\n");
		return this;
	}

	public N3IOStream comment(String comment) {
		if (!isUsingComments()) return this;
		writer.print("\n# ");
		writer.print(comment);
		writer.print("\n");
		return this;
	}

	public N3IOStream blank() {
		writer.print("\n");
		return this;
	}

	public N3IOStream write(String s, String p, String o) {
		writer.print(s);
		writer.print(" ");
		writer.print(p);
		writer.print(" ");
		writer.print(o);
		writer.println(".");
		lastSubject = s;
		return this;
	}

	public N3IOStream write(String s, String p, Date o) {
		writer.print(s);
		writer.print(" ");
		writer.print(p);
		writer.print(" ");
		writer.print(typed(date2XSD.format(o), "xsd:datetime"));
		writer.println(".");
		lastSubject = s;
		return this;
	}

	public N3IOStream write(String s, String p, Object o, String xsd) {
		writer.print(s);
		writer.print(" ");
		writer.print(p);
		writer.print(" ");
		writer.print( typed( o, xsd) );
		writer.println(".");

		lastSubject = s;
		return this;
	}

	@Override
	public void fact(String s, String p, Object o) {
		if (o==null) return;
		write(uri(s), uri(p), uri(o.toString()));
	}

	@Override
	public void fact(String s, String p, Object o, String xsdType) {
		if (o==null) return;
		write(uri(s), uri(p), typed(o.toString(), xsdType) );
	}


	public String typed(Object txt, String type) {
		StringBuilder s = new StringBuilder();
		if (type.indexOf(":")<1) type = COMMONS.XSD+type;
		if (txt.toString().indexOf("\n")>0) {
			// long string
			s.append(" ");
			s.append("\"\"\"");
			s.append(txt);
			s.append("\"\"\"^^<");
			s.append(type);
			s.append(">");
		}
		else {
			s.append(" ");
			s.append("\"");
			s.append(txt);
			s.append("\"^^<");
			s.append(type);
			s.append(">");
		}
		return s.toString();
	}


	private String uri(String txt) {
		String prefix = getPrefix(txt);
		if (prefix!=null) {
			if (prefices.containsKey(prefix)) {
				String ns = prefices.get(prefix);
				String cname = txt.substring(prefix.length());
				return "<"+ns+cname+">";
			}
		}
		return "<"+txt+">";
	}

	public N3IOStream label(String s, String t) {
		write(s, COMMONS.LABEL, t);
		return this;
	}

	public N3IOStream comment(String s, String t) {
		write(s, COMMONS.COMMENT, t);
		return this;
	}

	public static String getPrefix(String cname) {
		if (cname==null) return null;
		int ix = cname.indexOf(":");
		if (ix<0) return null;
		return cname.substring(0,ix);
	}


	public boolean isUsingHeaders() {
		return useHeaders;
	}

	public void setUseHeaders(boolean useHeaders) {
		this.useHeaders = useHeaders;
	}

	public boolean isUsingComments() {
		return useComments;
	}

	public void setUseComments(boolean useComments) {
		this.useComments = useComments;
	}

	public void setPrefix(String prefix, String uri) {
		prefices.put(prefix, uri);
	}

	@Override
	public String getIdentity() {
		return "bean:"+getClass().getCanonicalName();
	}

	public void close() {
		writer.close();
	}
}
