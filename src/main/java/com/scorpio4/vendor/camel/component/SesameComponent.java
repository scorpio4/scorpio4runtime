package com.scorpio4.vendor.camel.component;

import com.scorpio4.oops.IQException;
import com.scorpio4.vendor.camel.component.sesame.SesameHandler;
import org.apache.camel.Endpoint;
import org.apache.camel.component.bean.BeanEndpoint;
import org.apache.camel.component.bean.BeanProcessor;
import org.apache.camel.component.bean.ClassComponent;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.config.RepositoryResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Scorpio (c) 2014
 * Module: com.scorpio4.vendor.camel
 * User  : lee
 * Date  : 22/06/2014
 * Time  : 11:51 PM
 */
public class SesameComponent extends ClassComponent {
	static protected final Logger log = LoggerFactory.getLogger(SesameComponent.class);
	RepositoryResolver manager;
	RepositoryConnection connection;

	public SesameComponent(RepositoryConnection connection) {
		this.connection=connection;
	}

	public SesameComponent(RepositoryResolver manager) {
		this.manager=manager;
	}

	protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
		Boolean isInferred = getAndRemoveParameter(parameters, "isInferred", Boolean.class, true);
		Integer maxQueryTime = getAndRemoveParameter(parameters, "maxQueryTime", Integer.class, 0);
		String sparql = getAndRemoveParameter(parameters, "query", String.class, "");

		boolean autoClose = false;
		Repository repository = null;
		if (connection==null && manager!=null) {
//			if (remaining.equals("")) remaining =
			repository = manager.getRepository(remaining);
			connection = repository.getConnection();
			autoClose = true;
		}
		if (connection==null) throw new IQException("Failed to resolve connection: "+remaining);
		log.debug("SPARQL Repository: "+remaining);
		log.debug("SPARQL Query: "+sparql);
		return new BeanEndpoint(uri, this, new BeanProcessor(new SesameHandler(connection, sparql, isInferred, maxQueryTime, autoClose ), getCamelContext()));
	}

}
