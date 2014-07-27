package com.scorpio4.vendor.camel.self;

import com.scorpio4.deploy.Scorpio4SesameDeployer;
import com.scorpio4.oops.AssetNotSupported;
import com.scorpio4.oops.FactException;
import com.scorpio4.oops.IQException;
import com.scorpio4.runtime.ExecutionEnvironment;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Scorpio (c) 2014
 * Module: com.scorpio4.vendor.camel.component.asset
 * @author lee
 * Date  : 23/06/2014
 * Time  : 4:53 PM
 */
public class Deploy extends Base {
	static protected final Logger log = LoggerFactory.getLogger(Deploy.class);

	public Deploy(ExecutionEnvironment engine, String uri) throws IOException, FactException {
		super(engine, uri);

	}

	@Handler
	public void execute(Exchange exchange) throws RepositoryException, ExecutionException, IQException, InterruptedException, IOException, AssetNotSupported, FactException {
		RepositoryConnection connection = engine.getRepository().getConnection();
		Scorpio4SesameDeployer deployer = new Scorpio4SesameDeployer(engine.getIdentity(), connection);
		deployer.setDeployScripts(false);
		deployer.setDeployRDF(true);

		Map<String, Object> headers = exchange.getIn().getHeaders();
		File file = exchange.getIn().getBody(File.class);
		log.info("Exec deploy:"+uri+" -> "+file);

		if (file!=null) {
			String from = exchange.getFromEndpoint().getEndpointUri();
//			log.info("Deploy File: "+from+" -> "+file.getAbsolutePath());
//			log.info("\t"+headers);
			deployer.deploy(file.getParentFile(), file);
		}
		if (!uri.equals("")) {
			log.info("Deploy URL: "+uri);
			deployer.deploy(new URL(uri));
		}
		connection.close();

		exchange.getOut().setBody(file);
		exchange.getOut().setHeaders(headers);
	}

}
