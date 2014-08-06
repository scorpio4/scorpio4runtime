package com.scorpio4.vendor.camel.component.sesame;

import com.scorpio4.vendor.sesame.util.SesameHelper;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.util.ExchangeHelper;
import org.apache.commons.io.output.WriterOutputStream;
import org.openrdf.query.*;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Scorpio (c) 2014
 * Module: com.scorpio4.vendor.camel.component.sparql
 * @author lee
 * Date  : 25/06/2014
 * Time  : 11:33 AM
 */
public class SesameProcessor implements Processor {
	private static final Logger log = LoggerFactory.getLogger(SesameProcessor.class);

	boolean inferred = true;
	private int maxQueryTime = -1;
	String outputType, queryType;
	Repository repository;

	public SesameProcessor(Repository repository, String type, Boolean isInferred, Integer maxQueryTime, String contentType) {
		this.repository=repository;
		this.queryType=type;
		this.inferred=isInferred;
		this.maxQueryTime=maxQueryTime;
		this.outputType = contentType;
		log.debug("SesameProcessor: "+ outputType);

	}

	@Override
	@Handler
	public void process(Exchange exchange) throws Exception {
		Message in = exchange.getIn();
		Map<String,Object> headers = in.getHeaders();

		Message out = exchange.getOut();
		out.setHeaders(headers);

		RepositoryConnection connection = repository.getConnection();
		String sparql = null, contentType = this.outputType;
		// SPARQL query is specified in message Body not in declaration

		contentType = contentType==null? ExchangeHelper.getContentType(exchange):contentType;
		if (contentType==null||contentType.equals("")) {
			log.debug("Accept-Types:"+headers.get("Accept"));
			contentType = (String) headers.get("Accept");
		}

		sparql = in.getBody(String.class);

		if (queryType=="select") {
			if ( sparql==null||sparql.equals("") ) throw new MalformedQueryException("Missing SPARQL SELECT query");
			processSelect(connection, headers, contentType, sparql, out);
		} else if (queryType=="load") {
			if ( sparql==null||sparql.equals("") ) throw new MalformedQueryException("Missing RDF statements");
			processLoad(connection, headers, contentType, sparql, out);
		} else if (queryType=="construct") {
			if ( sparql==null||sparql.equals("") ) throw new MalformedQueryException("Missing SPARQL CONSTRUCT query");
			processConstruct(connection, headers, contentType, sparql, out);
		} else {
			if ( sparql==null||sparql.equals("") ) throw new MalformedQueryException("Missing SPARQL DESCRIBE query");
			log.debug("DEFAULT: ");
			processConstruct(connection, headers, contentType, sparql, out);
		}

		connection.close();

	}

	private void processLoad(RepositoryConnection connection, Map<String, Object> headers, String contentType, String triples, Message out) throws RepositoryException, RDFParseException, IOException {
		RDFFormat rdfFormat = RDFFormat.forMIMEType(contentType, RDFFormat.valueOf(contentType));
		String baseURI = (String) headers.get("Sesame-Context");
		if (baseURI==null||baseURI.equals("")) baseURI = "bean:"+getClass().getCanonicalName();
		connection.begin();
		log.debug("LOADING: " + contentType+" -> "+rdfFormat+" into "+baseURI);
		connection.add( new StringReader(triples), baseURI, rdfFormat );
		connection.commit();
	}

	private void processConstruct(RepositoryConnection connection, Map<String, Object> headers, String contentType, String sparql, Message out) throws RDFHandlerException, MalformedQueryException, RepositoryException, IOException, QueryResultHandlerException, QueryEvaluationException {
		RDFFormat rdfFormat = RDFFormat.forMIMEType(contentType, RDFFormat.valueOf(contentType));
		log.debug("CONSTRUCT: " + contentType+" -> "+rdfFormat);
		if (rdfFormat!=null) {
			headers.put("Content-Type", rdfFormat.getDefaultMIMEType()+";"+rdfFormat.getCharset());
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			GraphQueryResult graphQueryResult = handleGraph(connection, sparql);
			QueryResultIO.write(graphQueryResult, rdfFormat, outputStream);
			outputStream.flush();
			outputStream.close();
			log.debug("GRAPH: "+outputStream);
			out.setBody(outputStream.toString());
		} else {
			GraphQueryResult graphQueryResult = handleGraph(connection, sparql);
			out.setBody(graphQueryResult);
		}
	}

	private void processSelect(RepositoryConnection connection, Map<String, Object> headers, String contentType, String sparql, Message out) throws RepositoryException, QueryResultHandlerException, MalformedQueryException, QueryEvaluationException, IOException {
		TupleQueryResultFormat parserFormatForMIMEType = QueryResultIO.getParserFormatForMIMEType(contentType, null);
		if (parserFormatForMIMEType!=null) {
			headers.put("Content-Type", parserFormatForMIMEType.getDefaultMIMEType()+";"+parserFormatForMIMEType.getCharset());
			StringWriter stringWriter = handle(connection, sparql, parserFormatForMIMEType);
			log.trace("TUPLES: " + stringWriter.toString());
			// output message
			String results = stringWriter.toString();
			out.setBody(results);
		} else {
			// unknown-type, internal
			Collection<Map> results = SesameHelper.toMapCollection(connection, sparql);
			log.debug("COLLECTION: " + results);
			out.setBody(results);
		}
	}

	public StringWriter handle(RepositoryConnection connection, String sparql, TupleQueryResultFormat parserFormatForMIMEType) throws MalformedQueryException, RepositoryException, QueryResultHandlerException, QueryEvaluationException, IOException {
		StringWriter stringWriter = new StringWriter();
		OutputStream out = new WriterOutputStream(stringWriter);
		handle(connection, sparql, out, parserFormatForMIMEType);
		out.close();
		return stringWriter;
	}

	public void handle(RepositoryConnection connection, String sparql, OutputStream out, TupleQueryResultFormat parserFormatForMIMEType) throws MalformedQueryException, RepositoryException, QueryResultHandlerException, QueryEvaluationException {
		// handle query and result set
		TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
		tupleQuery.setIncludeInferred(isInferred());
		if (maxQueryTime>0) tupleQuery.setMaxQueryTime(getMaxQueryTime());

		TupleQueryResultWriter resultWriter = QueryResultIO.createWriter(parserFormatForMIMEType, out);
		resultWriter.startQueryResult(new ArrayList());
		tupleQuery.evaluate(resultWriter);
		resultWriter.endQueryResult();
	}

	public GraphQueryResult handleGraph(RepositoryConnection connection, String sparql) throws MalformedQueryException, RepositoryException, QueryResultHandlerException, QueryEvaluationException, IOException, RDFHandlerException {
		// handle query and result set
		GraphQuery query = connection.prepareGraphQuery(QueryLanguage.SPARQL, sparql);
		query.setIncludeInferred(isInferred());
		if (maxQueryTime>0) query.setMaxQueryTime(getMaxQueryTime());
		GraphQueryResult graphQueryResult = query.evaluate();
		return graphQueryResult;
	}

	public boolean isInferred() {
		return inferred;
	}

	public int getMaxQueryTime() {
		return maxQueryTime;
	}

}
