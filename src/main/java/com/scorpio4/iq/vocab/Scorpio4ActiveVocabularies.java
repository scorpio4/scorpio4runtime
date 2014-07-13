package com.scorpio4.iq.vocab;

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
public class Scorpio4ActiveVocabularies implements ActiveVocabulary {
	final Logger log = LoggerFactory.getLogger(this.getClass());
	ActiveBeansVocabulary springBeans;
	ActiveFLOVocabulary flo;

	public Scorpio4ActiveVocabularies(ExecutionEnvironment engine) throws Exception {
		boot(engine);
	}

	public void boot(ExecutionEnvironment engine) throws Exception {
		this.springBeans=new ActiveBeansVocabulary(engine);
		this.flo = new ActiveFLOVocabulary(engine);
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

	public Object activate(String triggerURI, Object body) {
		try {
			return flo.activate(triggerURI, null);
		} catch (Exception e) {
			log.warn("Faulty Trigger: "+triggerURI+" ->"+e.getCause().getMessage());
			return null;
		}
	}
}
