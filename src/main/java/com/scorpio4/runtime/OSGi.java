package com.scorpio4.runtime;

import com.scorpio4.vocab.COMMON;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.osgi.framework.*;
import org.osgi.framework.launch.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.runtime
 * User  : lee
 * Date  : 3/07/2014
 * Time  : 3:32 AM
 */
public class OSGi extends Engine implements BundleActivator {
	static final Logger log = LoggerFactory.getLogger(OSGi.class);

	BundleContext bundleContext;
	public static String INSTALL_BUNDLE = COMMON.CORE+"osgi/bundle";

	public OSGi() {
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		log.debug("Starting: "+getIdentity()+" -> "+bundleContext);
		this.bundleContext=bundleContext;
		installBundles(getIdentity());
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		this.bundleContext=null;
	}

	protected void installBundles(String coreURI) throws Exception {
		RepositoryConnection connection = getFactSpace().getConnection();
		ValueFactory vf = connection.getValueFactory();
		RepositoryResult<Statement> bundles = connection.getStatements(vf.createURI(coreURI), vf.createURI(INSTALL_BUNDLE), null, true);
		while(bundles.hasNext()) {
			Statement next = bundles.next();
			Bundle bundle = bundleContext.installBundle(next.getObject().stringValue());
			if (bundle!=null) {
				log.debug("Bundle: "+bundle.getBundleId());
				bundle.start();
			}
		}
	}

	public static void main(String[] args) {

		try
		{
			String filename = "scorpio4.properties";
			if (args.length>0) filename = args[0];
			Properties config = new Properties();
			config.load(new FileInputStream(new File(filename)));

			// Create host activator;
			List activators = new ArrayList();
			activators.add(new OSGi());
			config.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, activators);

			Framework scorpio4 = new Felix(config);
//			scorpio4.init();

			scorpio4.start();
			scorpio4.waitForStop(0);
			System.exit(0);
		}
		catch (Exception ex)
		{
			System.err.println("Could not create framework: " + ex);
			ex.printStackTrace();
			System.exit(-1);
		}


	}

}
