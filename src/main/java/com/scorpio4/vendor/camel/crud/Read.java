package com.scorpio4.vendor.camel.crud;

import com.scorpio4.oops.ConfigException;
import com.scorpio4.oops.FactException;
import com.scorpio4.vendor.camel.CRUDComponent;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.Message;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Scorpio (c) 2014
 * Module: com.scorpio4.vendor.camel.component.crud
 * @author lee
 * Date  : 23/06/2014
 * Time  : 1:50 AM
 */
public class Read extends Base {


	public Read(CRUDComponent crud, String asset, Map<String, Object> params) throws FactException, IOException, ConfigException {
		super(crud, asset);
	}

	@Handler
	public void read(Exchange exchange) throws FactException, IOException, ConfigException {
		Message in = exchange.getIn();
		Message out = exchange.getOut();
		Map<String, Object> headers = in.getHeaders();
		Map body = exchange.getIn().getBody(Map.class);
		body = body==null?headers:body;

		headers.put("sparql.read", assetURI);

		log.debug("READ: "+assetURI+" -> "+exchange);
		log.debug("head: " + headers);

		out.setHeaders(headers);
		out.setAttachments(in.getAttachments());
		try {
			Collection<Map> read = crud.getCRUD().read(assetURI, body);
			out.setBody(read);
		} catch(Exception e){
			out.setFault(true);
		}

	}

}
