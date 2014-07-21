package com.scorpio4.vendor.camel.self;

import com.scorpio4.oops.AssetNotSupported;
import com.scorpio4.oops.ConfigException;
import com.scorpio4.oops.FactException;
import com.scorpio4.oops.IQException;
import com.scorpio4.runtime.ExecutionEnvironment;
import com.scorpio4.iq.exec.Scripting;
import org.apache.camel.Exchange;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Scorpio (c) 2014
 * Module: com.scorpio4.vendor.camel.component.asset
 * @author lee
 * Date  : 23/06/2014
 * Time  : 3:28 AM
 */
public class Script extends Base {

	public Script(ExecutionEnvironment engine, String uri) throws IOException {
		super(engine, uri);
	}

	@Override
	public void execute(Exchange exchange) throws RepositoryException, ExecutionException, IQException, InterruptedException, IOException, AssetNotSupported, FactException, ConfigException, QueryEvaluationException, MalformedQueryException {
		Map<String, Object> headers = exchange.getIn().getHeaders();
		exchange.getOut().setHeaders(headers);
		exchange.getOut().setAttachments(exchange.getIn().getAttachments());

		Scripting scripting = new Scripting();
		Future done = scripting.execute(asset, headers);
		exchange.getOut().setBody(done.get());
	}
}
