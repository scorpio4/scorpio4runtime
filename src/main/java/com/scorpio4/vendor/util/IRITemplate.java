package com.scorpio4.vendor.util;

import com.github.fge.uritemplate.URITemplate;
import com.github.fge.uritemplate.URITemplateException;
import com.github.fge.uritemplate.URITemplateParseException;
import com.github.fge.uritemplate.vars.VariableMap;
import com.github.fge.uritemplate.vars.VariableMapBuilder;
import com.github.fge.uritemplate.vars.values.MapValue;

import java.util.Collection;
import java.util.Map;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.vendor.util
 * User  : lee
 * Date  : 9/07/2014
 * Time  : 12:26 AM
 */
public class IRITemplate {
	VariableMapBuilder builder = VariableMap.newBuilder();
	URITemplate uriTemplate;

	public IRITemplate(String template, Map vars) throws URITemplateParseException {
		uriTemplate = new URITemplate(template);
		putAll(vars);
	}

	public IRITemplate(String template) throws URITemplateParseException {
		uriTemplate = new URITemplate(template);
	}

	public void put(String k, Object v) {
		builder.addScalarValue(k,v);
	}

	public void put(String k, Collection c) {
		builder.addListValue(k, c);
	}

	public void putAll(Map m) {
		MapValue.newBuilder().putAll(m);
	}

	public String toString() {
		VariableMap vars = builder.freeze();
		URITemplate uriTemplate = null;
		try {
			return uriTemplate.toString(vars);
		} catch (URITemplateException e) {
			return null;
		}
	}
}
