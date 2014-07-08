package com.scorpio4.vendor.camel.crud;

import com.scorpio4.oops.ConfigException;
import com.scorpio4.oops.FactException;
import com.scorpio4.vendor.camel.CRUDComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Scorpio (c) 2014
 * Module: com.scorpio4.vendor.camel.component.crud
 * User  : lee
 * Date  : 23/06/2014
 * Time  : 1:50 AM
 */
public class Base {
	static protected final Logger log = LoggerFactory.getLogger(Base.class);

	protected CRUDComponent crud = null;
	protected String assetURI = null;

	public Base(CRUDComponent crud, String asset) throws FactException, IOException, ConfigException {
		this.crud=crud;
		this.assetURI=asset;
	}
}
