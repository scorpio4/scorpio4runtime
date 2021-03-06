package com.scorpio4.fact.stream;
/*
 *   Scorpio4 - Apache Licensed
 *   Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *
 */

import com.scorpio4.vocab.COMMONS;
import com.scorpio4.util.DateXSD;
import com.scorpio4.util.Identifiable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
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
public class N3Stream implements FactStream {
    String id = null;

	private StringBuilder n3 = new StringBuilder();
	DateXSD date2XSD = new DateXSD();
	String lastSubject = null;
	private boolean useHeaders = true;
	private boolean useComments = true;
	private Map<String,String> prefices = new HashMap();

	public N3Stream() {
        this.id = "urn:uuid:"+(java.util.UUID.randomUUID()).toString();
		init();
	}

    public N3Stream(String id) {
        this.id = id;
        init();
    }

    public N3Stream(Identifiable identifiable) {
        this.id = identifiable.getIdentity();
        init();
    }

	private void init() {
		setUseComments(true);
		setUseHeaders(true);
		setPrefix("rdf", COMMONS.RDF);
		setPrefix("rdfs", COMMONS.RDFS);
		setPrefix("xsd", COMMONS.XSD);

	}

	public N3Stream clear() {
		this.n3 = new StringBuilder();
		return this;
	}

	public N3Stream prolog(String header) {
		for(Map.Entry prefix: prefices.entrySet()) {
			n3.append("@prefix ").append(prefix.getKey()).append(": <").append(prefix.getValue()).append(">.\n");
		}
		if (!isUsingComments()) return this;
		n3.append("\n#\n");
		heading(header);
		n3.append("# Created: ").append(new Date());
		n3.append("\n#\n");
		return this;
	}

	public N3Stream heading(String header) {
		if (!isUsingHeaders()) return this;
		n3.append("# ").append(header).append("\n");
		return this;
	}

	public N3Stream comment(String comment) {
		if (!isUsingComments()) return this;
		n3.append("\n# ").append(comment).append("\n");
		return this;
	}

    public N3Stream blank() {
        n3.append("\n");
        return this;
    }

    public N3Stream append(String s, String p, String o) {
		uri(s).uri(p).string(o).append(".\n");
		lastSubject = s;
		return this;
	}

    public N3Stream append(String s, String p, Date o) {
        uri(s).uri(p);
        typed( date2XSD.format(o), "xsd:datetime");
        append(".\n");
        lastSubject = s;
        return this;
    }

	public N3Stream append(String p, String o) {
		return append(lastSubject, p, o);
	}

	public N3Stream append(URI uri) {
		n3.append(uri);
		return this;
	}

	public N3Stream append(long value) {
		n3.append(value);
		return this;
	}

    @Override
    public void fact(String s, String p, Object o) {
        if (o==null) return;
        uri(s, p, o.toString());
    }

    @Override
    public void fact(String s, String p, Object o, String xsdType) {
        if (o==null) return;
        uri(s);
        uri(p);
        typed(o.toString(), xsdType);
    }

	public N3Stream append(String s, String p, boolean o) {
		uri(s).uri(p).append(" ").append(o).append(".\n");
		lastSubject = s;
		return this;
	}

	public N3Stream append(String s, String p, int o) {
		uri(s).uri(p).append(" ").append(o).append(".\n");
		lastSubject = s;
		return this;
	}
    public N3Stream append(String txt) {
		n3.append(" ").append(txt);
		return this;
	}

	public N3Stream string(String txt) {
		typed(txt, "xsd:string");
		return this;
	}

	public N3Stream append(int number) {
		typed(Integer.toString(number), "xsd:integer");
		return this;
	}

	public N3Stream append(boolean truth) {
		typed(Boolean.toString(truth), "xsd:boolean");
		return this;
	}

	public N3Stream append(Date date) {
		typed(date2XSD.format(date), "xsd:datetime");
		return this;
	}

	public N3Stream typed(String txt, String type) {
        if (type.indexOf(":")<1) type = COMMONS.XSD+type;
        if (txt.indexOf("\n")>0)
            n3.append(" ").append("\"\"\"").append(txt).append("\"\"\"^^<").append(type).append(">.\n");
        else
		    n3.append(" ").append("\"").append(txt).append("\"^^<").append(type).append(">.\n");
		return this;
	}


    public N3Stream uri(String p, String o) {
        return uri(lastSubject, p, o);
    }

    public N3Stream uri(String s, String p, String o) {
        if (p.equals("a")) {
            uri(s).append(p).append(" ").uri(o).append(".\n");
        } else {
            uri(s).uri(p).append(" ").uri(o).append(".\n");
        }
        lastSubject = s;
        return this;
    }

    private N3Stream uri(String txt) {
		String prefix = getPrefix(txt);
		if (prefix!=null) {
			if (prefices.containsKey(prefix)) {
				return append(txt);
			}
		}
		n3.append("<").append(txt).append("> ");
		return this;
	}

    public N3Stream typeOf(String s, String t) {
        uri(s, COMMONS.A, t);
        return this;
    }

    public N3Stream label(String s, String t) {
        append(s, COMMONS.LABEL, t);
        return this;
    }

    public N3Stream comment(String s, String t) {
        append(s, COMMONS.COMMENT, t);
        return this;
    }

    public static String getPrefix(String cname) {
		if (cname==null) return null;
		int ix = cname.indexOf(":");
		if (ix<0) return null;
		return cname.substring(0,ix);
	}

	public String toString() {
		return n3.toString();
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

    public InputStream asInputStream() {
        return new ByteArrayInputStream(toString().getBytes(Charset.forName("UTF-8")));
    }

    @Override
    public String getIdentity() {
        return id;
    }
}
