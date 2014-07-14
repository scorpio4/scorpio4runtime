package com.scorpio4.vendor.sesame.util;
/*
 *   Scorpio4 - Apache Licensed
 *   Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *
 */
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.XMLGregorianCalendar;
import java.net.URL;

/**
 * Scorpio4 (c) 2010-2013
 * @author lee
 * Date: 24/01/13
 * Time: 9:21 AM
 * <p/>
 * This code does something useful
 */
public class ValueTypeConverter {
	private static final Logger log = LoggerFactory.getLogger(ValueTypeConverter.class);
	private static final String XSD_NS = "http://www.w3.org/2001/XMLSchema#";
	
	public ValueTypeConverter() {
		
	}

	public Object convert(Object value) {
		if (value == null) return null;
		if (value instanceof BNode) return convert((BNode)value);
		if (value instanceof URI) return convert((URI)value);
		if (value instanceof Literal) return convert((Literal)value);
		return value.toString();
	}

	public Object convert(URI value) {
		if (value == null) return null;
		return value.getNamespace() + value.getLocalName();
	}

	public Object convert(BNode value) {
		if (value == null) return null;
		return value.getID();
	}

	public Object convert(Literal value) {
		if (value == null) return null;
		URI type = value.getDatatype();
		if (type == null) return value.getLabel();

		try {
			if (matches(XSD_NS+"simpleType", type)) {
				return value.getLabel();
			} else if (matches(XSD_NS+"anyURI", type)) {
				return new URL(value.getLabel());
			} else if (matches(XSD_NS+"boolean", type)) {
				return new Boolean(value.booleanValue());
			} else if (matches(XSD_NS+"integer", type)) {
				return new Integer(value.intValue());
			} else if (matches(XSD_NS+"numeric", type)) {
				return new Double(value.doubleValue());
			} else if (matches(XSD_NS+"double", type)) {
				return new Double(value.doubleValue());
			} else if (matches(XSD_NS+"string", type)) {
				return value.getLabel();
			} else if (matches(XSD_NS+"dateTime", type)) {
				return ((XMLGregorianCalendar) value.calendarValue()).toXMLFormat();
			} else if (matches(XSD_NS+"date", type)) {
				return ((XMLGregorianCalendar) value.calendarValue()).toXMLFormat();
			} else {
				log.info("default XSD type:" + type + " -> " + String.valueOf(value));
				return value.getLabel();
			}
		} catch (Exception e) {
			log.error("invalid XSD value: " + value.getLabel() + " -> " + e.getMessage() + " as " + type);
			return value.getLabel();
		}

	}

	protected boolean matches(String type, URI uriType) {
		if (uriType == null || type == null) return false;
		return type.equals(uriType.toString());
	}
	
}
