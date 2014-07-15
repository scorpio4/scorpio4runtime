package com.scorpio4.vendor.camel.self;

import com.scorpio4.oops.AssetNotSupported;
import com.scorpio4.oops.ConfigException;
import com.scorpio4.oops.FactException;
import com.scorpio4.oops.IQException;
import com.scorpio4.runtime.ExecutionEnvironment;
import com.scorpio4.template.PicoTemplate;
import com.scorpio4.vendor.sesame.util.SesameHelper;
import org.apache.camel.Exchange;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Scorpio (c) 2014
 * Module: com.scorpio4.vendor.camel.component.asset
 * User  : lee
 * Date  : 23/06/2014
 * Time  : 3:28 AM
 */
public class SPARQL extends Base {

	public SPARQL(ExecutionEnvironment engine, String uri) throws IOException {
		super(engine,uri);
	}

	@Override
	public void execute(Exchange exchange) throws RepositoryException, ExecutionException, IQException, InterruptedException, IOException, AssetNotSupported, FactException, ConfigException, QueryEvaluationException, MalformedQueryException {
		Map<String, Object> headers = exchange.getIn().getHeaders();
		exchange.getOut().setHeaders(headers);
		exchange.getOut().setAttachments(exchange.getIn().getAttachments());

		PicoTemplate picoTemplate = new PicoTemplate(asset.toString());
		RepositoryConnection connection = getEngine().getRepository().getConnection();

		Collection<Map> result = SesameHelper.toMapCollection(connection, picoTemplate.translate(headers));
		exchange.getOut().setBody(result);
		connection.close();

	}

}
