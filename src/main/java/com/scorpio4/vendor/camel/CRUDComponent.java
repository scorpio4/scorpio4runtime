package com.scorpio4.vendor.camel;

import com.scorpio4.crud.CRUD;
import com.scorpio4.vendor.camel.crud.Create;
import com.scorpio4.vendor.camel.crud.Delete;
import com.scorpio4.vendor.camel.crud.Read;
import com.scorpio4.vendor.camel.crud.Update;
import org.apache.camel.Endpoint;
import org.apache.camel.component.bean.BeanEndpoint;
import org.apache.camel.component.bean.BeanProcessor;
import org.apache.camel.component.bean.ClassComponent;
import org.apache.camel.util.IntrospectionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Scorpio (c) 2014
 * Module: com.scorpio4.vendor.camel
 * @author lee
 * Date  : 22/06/2014
 * Time  : 11:51 PM
 */
public class CRUDComponent extends ClassComponent {
	static protected final Logger log = LoggerFactory.getLogger(CRUDComponent.class);
	protected CRUD crud;

	public CRUDComponent(CRUD crud) {
		this.crud=crud;
	}

	public CRUD getCRUD() {
		return crud;
	}

	protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
		Map<String, Object> params = IntrospectionSupport.extractProperties(parameters, "crud.");
		if (remaining.startsWith("create:")) {
			return new BeanEndpoint(uri, this, new BeanProcessor(new Create(this, remaining.substring(6), params), getCamelContext()));
		} else if (remaining.startsWith("read:")) {
			return new BeanEndpoint(uri, this, new BeanProcessor(new Read(this, remaining.substring(5), params), getCamelContext()));
		} else if (remaining.startsWith("update:")) {
			return new BeanEndpoint(uri, this, new BeanProcessor(new Update(this, remaining.substring(7), params), getCamelContext()));
		} else if (remaining.startsWith("delete:")) {
			return new BeanEndpoint(uri, this, new BeanProcessor(new Delete(this, remaining.substring(7), params), getCamelContext()));
		}

		return null;
	}


}
