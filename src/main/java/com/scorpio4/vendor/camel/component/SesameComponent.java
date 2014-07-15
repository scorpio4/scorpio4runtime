package com.scorpio4.vendor.camel.component;

import com.scorpio4.runtime.ExecutionEnvironment;
import com.scorpio4.vendor.camel.component.sesame.SesameHandler;
import org.apache.camel.Endpoint;
import org.apache.camel.component.bean.BeanEndpoint;
import org.apache.camel.component.bean.BeanProcessor;
import org.apache.camel.component.bean.ClassComponent;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.config.RepositoryResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
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
	String identity;
	RepositoryResolver manager;
	Map<String,String> outputType2contentType;

	public SesameComponent(ExecutionEnvironment engine) {
		this(engine.getIdentity(), engine.getRepositoryManager());
	}

	public SesameComponent(String identity, RepositoryResolver manager) {
		this.identity=identity;
		this.manager=manager;
		outputType2contentType = new HashMap();
		outputType2contentType.put("xml", TupleQueryResultFormat.SPARQL.getDefaultMIMEType());
		outputType2contentType.put("sparql", TupleQueryResultFormat.SPARQL.getDefaultMIMEType());
		outputType2contentType.put("json", TupleQueryResultFormat.JSON.getDefaultMIMEType());
		outputType2contentType.put("csv", TupleQueryResultFormat.CSV.getDefaultMIMEType());
		outputType2contentType.put("tsv", TupleQueryResultFormat.TSV.getDefaultMIMEType());
		outputType2contentType.put("binary", TupleQueryResultFormat.BINARY.getDefaultMIMEType());
	}

	protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
		Boolean isInferred = getAndRemoveParameter(parameters, "isInferred", Boolean.class, true);
		Integer maxQueryTime = getAndRemoveParameter(parameters, "maxQueryTime", Integer.class, 0);
		String sparql = getAndRemoveParameter(parameters, "sparql.query", String.class, "");

		String contentType = getAndRemoveParameter(parameters, "outputType", String.class);
		contentType = outputType2contentType.containsKey(contentType)?outputType2contentType.get(contentType):contentType;

		Repository repository = null;
		if (remaining.equals("self")) remaining = identity;
		repository = manager.getRepository(remaining);

		log.debug("SPARQL Repository: "+remaining);
		return new BeanEndpoint(uri, this, new BeanProcessor(
			new SesameHandler(repository, sparql, isInferred, maxQueryTime, contentType ), getCamelContext()));
	}

}
