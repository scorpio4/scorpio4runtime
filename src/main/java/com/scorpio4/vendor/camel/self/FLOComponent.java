package com.scorpio4.vendor.camel.self;

import com.scorpio4.runtime.ExecutionEnvironment;
import org.apache.camel.CamelException;
import org.apache.camel.Endpoint;
import org.apache.camel.component.bean.BeanEndpoint;
import org.apache.camel.component.bean.BeanProcessor;
import org.apache.camel.component.bean.ClassComponent;
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
public class FLOComponent extends ClassComponent {
	protected final Logger log = LoggerFactory.getLogger(FLOComponent.class);
	ExecutionEnvironment engine;

	public FLOComponent(ExecutionEnvironment engine) {
		this.engine = engine;
	}

	protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
		Object executable = null;
		if (remaining.startsWith("script:")) {
			executable = new Script(engine, remaining.substring(7));
		} else if (remaining.startsWith("infer:")||remaining.startsWith("learn:")) {
			executable = new Infer(engine, remaining.substring(6));
		} else if (remaining.startsWith("template:")) {
			executable = new AssetTemplate(engine, remaining.substring(9));
		} else if (remaining.startsWith("sparql:")) {
			executable = new SPARQL(engine, remaining.substring(7));
		} else if (remaining.startsWith("asset:")) {
			executable = new AssetBase(engine, remaining.substring(6));
		} else if (remaining.startsWith("deploy:")) {
			log.debug("Deploy: "+uri+" @ "+remaining);
			String assetURI = remaining.substring(7);
//			if (assetURI.equals("")) assetURI = engine.getIdentity();
			executable = new Deploy(engine, assetURI);
		}
		if (executable==null) throw new CamelException("Unknown command for <flo:"+remaining+">");
		return new BeanEndpoint(uri, this, new BeanProcessor(executable, getCamelContext()));
	}

}
