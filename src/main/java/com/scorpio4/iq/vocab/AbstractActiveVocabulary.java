package com.scorpio4.iq.vocab;

import com.scorpio4.oops.IQException;
import com.scorpio4.runtime.ExecutionEnvironment;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.iq.vocab
 * @author lee
 * Date  : 16/07/2014
 * Time  : 11:22 PM
 */
public abstract class AbstractActiveVocabulary  implements ActiveVocabulary {
	final Logger log = LoggerFactory.getLogger(ASQVocabulary.class);

	protected ExecutionEnvironment engine;
	protected String vocabURI = null;
	protected boolean isActive = false;
	protected RepositoryConnection connection = null;
	protected ValueFactory vf = null;

	public AbstractActiveVocabulary(String vocabURI, ExecutionEnvironment engine, boolean useInferencing) throws Exception {
		if (vocabURI==null) throw new IQException("Missing Vocabulary URI");
		if (engine==null) throw new IQException("Missing Vocabulary Engine");

		this.vocabURI=vocabURI;
		this.useInferencing=useInferencing;
	}

	@Override
	public void start() throws Exception {
		isActive = true;
	}

	@Override
	public void stop() throws Exception {
		isActive = false;
	}

	@Override
	abstract public Object activate(String resource, Object body) throws IQException;

	@Override
	public void boot(ExecutionEnvironment engine) throws Exception {
		this.engine=engine;
		this.connection = engine.getRepository().getConnection();
		this.vf = connection.getValueFactory();
	}

	public boolean isUseInferencing() {
		return useInferencing;
	}

	boolean useInferencing = false;

	public boolean isActive() {
		return isActive;
	}

	@Override
	public String getIdentity() {
		return vocabURI;
	}

	public ExecutionEnvironment getEngine() {
		return engine;
	}

}
