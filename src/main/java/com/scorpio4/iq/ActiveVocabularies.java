package com.scorpio4.iq;

import com.scorpio4.runtime.Engine;
import com.scorpio4.vendor.camel.component.Any23Component;
import com.scorpio4.vendor.camel.component.CoreComponent;
import com.scorpio4.vendor.camel.component.SelfComponent;
import com.scorpio4.vendor.camel.planner.CamelFLO;
import org.apache.camel.CamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.iq
 * User  : lee
 * Date  : 3/07/2014
 * Time  : 11:47 PM
 */
public class ActiveVocabularies {
	final Logger log = LoggerFactory.getLogger(this.getClass());
	private CamelFLO floSupport;
	Engine engine;

	public ActiveVocabularies(Engine engine) throws Exception {
		this.engine=engine;
		log.debug("Activating Vocabularies");

		initCamelFLO();
		initSpringyBeans();
		booted();
	}

	protected void initSpringyBeans() throws Exception {
	}

	protected void initCamelFLO() throws Exception {
		log.debug("Booting CamelFLO");
		CamelContext camel = engine.getCamelContext();
		camel.addComponent("self", new SelfComponent(this));
		camel.addComponent("core", new CoreComponent(engine.getFactSpace(), engine.getAssetRegister()));
		camel.addComponent("any23", new Any23Component());

		log.debug("Routing API"+engine.getFactSpace().getIdentity());

		floSupport = new CamelFLO(camel, engine.getFactSpace());
		floSupport.plan();
	}

	private void booted() {
		String bootstrapURI = "direct:self:booted:active:vocabularies";
		try {
			floSupport.trigger(bootstrapURI, null, engine.getConfig());
		} catch(Exception e) {
			log.debug("Bootstrap Failed: "+bootstrapURI+" -> "+e.getMessage());
		}
	}

}
