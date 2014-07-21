package com.scorpio4.vendor.util;

import com.github.fge.uritemplate.URITemplate;
import com.github.fge.uritemplate.URITemplateException;
import com.github.fge.uritemplate.URITemplateParseException;
import com.github.fge.uritemplate.vars.VariableMap;
import com.github.fge.uritemplate.vars.VariableMapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.vendor.util
 * @author lee
 * Date  : 9/07/2014
 * Time  : 12:26 AM
 */
public class IRITemplate {
	static protected final Logger log = LoggerFactory.getLogger(IRITemplate.class);

	VariableMapBuilder builder = VariableMap.newBuilder();
	URITemplate uriTemplate;

	public IRITemplate(String template, Map vars) throws URITemplateParseException {
		uriTemplate = new URITemplate(template);
		putAll(vars);
	}

	public IRITemplate(String template) throws URITemplateParseException {
		uriTemplate = new URITemplate(template);
	}

	public static boolean isTemplated(String template) {
		return (template.contains("{") && template.contains("}"));
	}

	public void put(String k, Object v) {
		builder.addScalarValue(k,v);
	}

	public void put(String k, Collection c) {
		builder.addListValue(k, c);
	}

	public void putAll(Map m) {
		Set set = m.keySet();
		for(Object k:set) {
			put( k.toString(), m.get(k) );
		}
	}

	public String toString(Map m) {
		putAll(m);
		return toString();
	}

	public String toString() {
		VariableMap vars = builder.freeze();
		log.debug("VARS:" +vars);
		try {
			return uriTemplate.toString(vars);
		} catch (URITemplateException e) {
			return null;
		}
	}
}
