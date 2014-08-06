package com.scorpio4.vendor.camel.component.sesame;

import com.scorpio4.runtime.ExecutionEnvironment;
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
 * @author lee
 * Date  : 22/06/2014
 * Time  : 11:51 PM
 */
public class SesameComponent extends ClassComponent {
	static protected final Logger log = LoggerFactory.getLogger(SesameComponent.class);
	String identity;
	RepositoryResolver manager;
	public static Map<String,String> outputType2contentType = new HashMap();
	static {
		outputType2contentType.put("xml", TupleQueryResultFormat.SPARQL.getDefaultMIMEType());
		outputType2contentType.put("sparql", TupleQueryResultFormat.SPARQL.getDefaultMIMEType());
		outputType2contentType.put("json", TupleQueryResultFormat.JSON.getDefaultMIMEType());
		outputType2contentType.put("csv", TupleQueryResultFormat.CSV.getDefaultMIMEType());
		outputType2contentType.put("tsv", TupleQueryResultFormat.TSV.getDefaultMIMEType());
		outputType2contentType.put("binary", TupleQueryResultFormat.BINARY.getDefaultMIMEType());
	}

	public SesameComponent(ExecutionEnvironment engine) {
		this(engine.getIdentity(), engine.getRepositoryManager());
	}

	public SesameComponent(String identity, RepositoryResolver manager) {
		this.identity=identity;
		this.manager=manager;
	}

	protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
		Boolean isInferred = getAndRemoveParameter(parameters, "isInferred", Boolean.class, true);
		Integer maxQueryTime = getAndRemoveParameter(parameters, "maxQueryTime", Integer.class, 0);

		String contentType = getAndRemoveParameter(parameters, "outputType", String.class);
		contentType = outputType2contentType.containsKey(contentType)?outputType2contentType.get(contentType):contentType;

		String type = "describe", repoURI = null;
		Repository repository;

		if (remaining.startsWith("select:")) {
			type = "select";
			repoURI = remaining.substring(type.length()+1);
		} else if (remaining.startsWith("load:")) {
			type = "load";
			repoURI = remaining.substring(type.length()+1);
		} else if (remaining.startsWith("construct:")) {
			type = "construct";
			repoURI = remaining.substring(type.length()+1);
		} else {
			type = "construct";
			repoURI = identity;
		}
		if (repoURI.equals("")||repoURI.equals("self")) repoURI = identity;
		repository = manager.getRepository(repoURI);

		log.debug("SPARQL Repository: "+repoURI);
		return new BeanEndpoint(uri, this, new BeanProcessor(
			new SesameProcessor(repository, type, isInferred, maxQueryTime, contentType ), getCamelContext()));
	}

}
