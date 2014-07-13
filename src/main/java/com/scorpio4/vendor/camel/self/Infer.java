package com.scorpio4.vendor.camel.self;

import com.scorpio4.assets.Asset;
import com.scorpio4.assets.AssetHelper;
import com.scorpio4.oops.AssetNotSupported;
import com.scorpio4.oops.FactException;
import com.scorpio4.oops.IQException;
import com.scorpio4.runtime.ExecutionEnvironment;
import com.scorpio4.vendor.sesame.io.SPARQLRules;
import org.apache.camel.Exchange;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Scorpio (c) 2014
 * Module: com.scorpio4.vendor.camel.component.asset
 * User  : lee
 * Date  : 23/06/2014
 * Time  : 3:28 AM
 */
public class Infer extends Base {

	public Infer(ExecutionEnvironment engine, String uri) throws IOException {
		super(engine,uri);
	}

	@Override
	public void execute(Exchange exchange) throws RepositoryException, ExecutionException, IQException, InterruptedException, IOException, AssetNotSupported, FactException, QueryEvaluationException, MalformedQueryException {
		Map<String, Object> headers = exchange.getIn().getHeaders();
		exchange.getOut().setHeaders(headers);
		exchange.getOut().setAttachments(exchange.getIn().getAttachments());

		RepositoryConnection connection = getEngine().getRepository().getConnection();
		SPARQLRules SPARQLRules = new SPARQLRules(connection, getIdentity());
		Asset newAsset = AssetHelper.getAsset(asset, headers);
		int copied = SPARQLRules.apply(newAsset.toString());

		exchange.getOut().setBody(exchange.getIn().getBody());
		connection.close();
	}


}
