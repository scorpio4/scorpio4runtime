package com.scorpio4.vendor.camel.self;

import com.scorpio4.assets.Asset;
import com.scorpio4.oops.AssetNotSupported;
import com.scorpio4.oops.ConfigException;
import com.scorpio4.oops.FactException;
import com.scorpio4.oops.IQException;
import com.scorpio4.runtime.ExecutionEnvironment;
import com.scorpio4.util.Identifiable;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Scorpio (c) 2014
 * Module: com.scorpio4.vendor.camel.component.asset
 * @author lee
 * Date  : 23/06/2014
 * Time  : 3:28 AM
 */
abstract public class Base implements Identifiable {
	static protected final Logger log = LoggerFactory.getLogger(Base.class);
	protected ExecutionEnvironment engine;
	protected String uri;
	protected Asset asset;

	protected Base() throws IOException {
	}

	public Base(ExecutionEnvironment engine, String uri) throws IOException {
		init(engine,uri);
	}

	public void init(ExecutionEnvironment engine, String uri) throws IOException {
		this.engine = engine;
		this.uri=uri;
		if (uri!=null && !uri.equals("")) {
			try {
				asset = engine.getAssetRegister().getAsset(uri,null);
			} catch(Exception e) {
				throw new IOException(e.getMessage(),e);
			}
			log.info("Asset for "+getClass().getSimpleName()+" -> "+(asset==null?"not found: ":asset.getMimeType())+" -> "+uri+"\n"+asset);
		}
	}

	public ExecutionEnvironment getEngine() {
		return engine;
	}

	public Asset getAsset(String uri, String type) throws IOException {
		if (asset==null) return engine.getAssetRegister().getAsset( uri, type );
		else return asset;
	}

	@Handler
	public abstract  void execute(Exchange exchange) throws RepositoryException, ExecutionException, IQException, InterruptedException, IOException, AssetNotSupported, FactException, ConfigException, QueryEvaluationException, MalformedQueryException;

	public String getIdentity() {
		return uri;
	}

}
