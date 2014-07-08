package com.scorpio4.iq;

import com.scorpio4.runtime.ExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.iq
 * User  : lee
 * Date  : 3/07/2014
 * Time  : 11:47 PM
 */
public class DefaultActiveVocabularies implements ActiveVocabulary {
	final Logger log = LoggerFactory.getLogger(this.getClass());
	SpringyBeansVocabulary springBeans;
	FLOVocabulary flo;

	public DefaultActiveVocabularies(ExecutionEnvironment engine) throws Exception {
		boot(engine);
	}

	public void boot(ExecutionEnvironment engine) throws Exception {
		bind(engine);

		this.springBeans=new SpringyBeansVocabulary(engine);
		this.flo = new FLOVocabulary(engine);
		log.debug("Activating Vocabularies");
	}

	@Override
	public void start() throws Exception {
		springBeans.start();
		flo.start();
	}

	@Override
	public void stop() throws Exception {
		springBeans.stop();
		flo.stop();
	}

	protected void bind(ExecutionEnvironment engine) throws Exception {

	}

	public void trigger(String triggerURI) {
		try {
			flo.trigger(triggerURI, null);
		} catch (Exception e) {
			log.warn("Faulty Trigger: "+triggerURI+" ->"+e.getCause().getMessage());
		}
	}



}
