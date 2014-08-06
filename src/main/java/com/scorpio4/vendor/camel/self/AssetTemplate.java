package com.scorpio4.vendor.camel.self;

import com.scorpio4.assets.Asset;
import com.scorpio4.iq.exec.Templating;
import com.scorpio4.oops.AssetNotSupported;
import com.scorpio4.oops.ConfigException;
import com.scorpio4.oops.FactException;
import com.scorpio4.oops.IQException;
import com.scorpio4.runtime.ExecutionEnvironment;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import java.io.IOException;
import java.util.HashMap;
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
public class AssetTemplate extends Base {

	public AssetTemplate(ExecutionEnvironment engine, String uri) throws IOException {
		super(engine,uri);
	}

	@Override
	public void execute(Exchange exchange) throws RepositoryException, ExecutionException, IQException, InterruptedException, IOException, AssetNotSupported, FactException, ConfigException, QueryEvaluationException, MalformedQueryException {
		Message in = exchange.getIn();
		Map<String,Object> headers = in.getHeaders();
		Message out = exchange.getOut();

		Asset template = getAsset(String.valueOf(in.getHeader("asset")),null);

		Templating templating = new Templating();
		Map bindings = new HashMap();

		bindings.put("config", getEngine().getConfig());
		bindings.put("in", in);
		bindings.put("header", headers);
		bindings.put("body", in.getBody());

		log.debug("Template Bindings: "+bindings);
		Future done = templating.execute(template, bindings);

		out.setBody(done.get(), String.class);
		out.setHeaders(headers);
		out.setAttachments(exchange.getIn().getAttachments());
	}

}
