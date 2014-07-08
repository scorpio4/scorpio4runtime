package com.scorpio4.iq.exec;

import com.scorpio4.assets.Asset;
import com.scorpio4.assets.AssetRegister;
import com.scorpio4.assets.AssetRegisters;
import com.scorpio4.fact.FactSpace;
import com.scorpio4.oops.AssetNotSupported;
import com.scorpio4.oops.IQException;
import com.scorpio4.vendor.sesame.util.RDFCollections;
import com.scorpio4.vocab.COMMON;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Scorpio4 (c) 2014
 * Module: com.scorpio4.vendor.sesame.iq
 * User  : lee
 * Date  : 18/06/2014
 * Time  : 9:57 AM
 */
public class Executor implements Executable {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    AssetRegister assetRegister = null;
    FactSpace factSpace = null;
    RDFCollections rdfCollections = null;
    String DO_EXECUTES = COMMON.CORE+"iq/executes";
    String DO_EXECUTE = COMMON.CORE+"iq/execute";
    String DO_CHAIN = COMMON.CORE+"iq/runs";
//    String IS_BEAN = COMMON.CORE+"iq/Bean";
    Map<String,Executable> beanFactory = new HashMap();
    Map<String,Boolean> seen = new HashMap();

    public Executor(FactSpace factSpace) {
        this(factSpace, new AssetRegisters(factSpace.getConnection()));
    }

    public Executor(FactSpace factSpace, AssetRegister assetRegister) {
        this.factSpace = factSpace;
        this.assetRegister = assetRegister;
        this.rdfCollections = new RDFCollections(factSpace.getConnection(),factSpace.getIdentity());
    }

    public void addExecutable(String name, Executable executable) {
        beanFactory.put(name,executable);
    }

    public void addExecutable(Executable executable) {
        addExecutable("bean:"+executable.getClass().getCanonicalName(), executable);
    }

    public Map<String,Future> run(String listURI, Map bindings) throws RepositoryException, ExecutionException, IQException, InterruptedException, IOException, AssetNotSupported {
        log.debug("doRun: {} @ {}", listURI, DO_CHAIN);
        Map<String,Future> results = new HashMap();
        Collection<Value> allRuns = rdfCollections.getList(listURI, DO_CHAIN);

        for(Value runURI: allRuns) {
            Map<String, Future> futureMap = execute(runURI.stringValue(), bindings);
            results.putAll(futureMap);
        }
        return results;
    }

    public Map<String, Future> execute(String listURI, Map bindings) throws RepositoryException, ExecutionException, IQException, InterruptedException, IOException, AssetNotSupported {
        seen.clear();
        log.debug("doExecute: {}", listURI);
        Collection<Value> allExecs = rdfCollections.getList(listURI, DO_EXECUTES);

        Map results = new HashMap();
        RepositoryConnection connection = factSpace.getConnection();

        for(Value execURI: allExecs) {
            if (execURI instanceof URI) {
                Map<String, Future> futures = doExecutables((URI) execURI, bindings);
                log.debug("Results: {} -> {}", execURI, futures);
                if (futures!=null) results.putAll(futures);
            }
        }

        ValueFactory vf = connection.getValueFactory();
        RepositoryResult<Statement> singleExecs = connection.getStatements(vf.createURI(listURI), vf.createURI(DO_EXECUTE), null, true);
        while(singleExecs.hasNext()) {
            Statement next = singleExecs.next();
            Value execURI = next.getObject();
            if (execURI instanceof URI) {
                Map<String, Future> futures = doExecutables((URI) execURI, bindings);
                log.debug("Result: {} -> {}", execURI, futures);
                if (futures!=null) results.putAll(futures);
            }
        }

        return results;
    }

    protected Map<String, Future> doExecutables(URI execURI, Map bindings) throws RepositoryException, ExecutionException, IQException, InterruptedException, IOException, AssetNotSupported {
        RepositoryConnection connection = factSpace.getConnection();
        Map beans = findBeans(connection, execURI);
        if (beans==null|beans.isEmpty()) return null;
        Asset asset = assetRegister.getAsset(execURI.stringValue(), null);
        if (asset==null) return null; //throw new IQException("Missing asset: "+execURI);

        Map<String, Future> results = new HashMap();
        log.debug("doExecutables: {} -> {}", execURI, beans);
        for(Object beanName: beans.keySet()) {
            // ensure we don't run twice
            if (!seen.containsKey(beanName)) {
                Executable executable = beanFactory.get(beanName);
                if (executable!=null) {
                    if (asset!=null) {
                        Future done = executable.execute(asset, bindings);
                        log.debug("\texecuted: {} -> {}", execURI, done.get());
                        results.put(beanName.toString(), done);
                        seen.put(beanName.toString(), true);
                    }
                }
            }
        }
        return results;
    }

    protected Map<String,String> findBeans(RepositoryConnection connection, URI execURI) throws RepositoryException {
        log.debug("beanFinder: {}", execURI);
        Map<String,String> beans = new HashMap();

//        ValueFactory vf = connection.getValueFactory();
//        URI isABean = vf.createURI(IS_BEAN);
        RepositoryResult<Statement> execTypes = connection.getStatements(execURI, RDF.TYPE, null, true );
        while(execTypes.hasNext()) {
            Statement next = execTypes.next();
            Value beanType = next.getObject();
//            log.trace(" -> " + beanType);
            String bean = beanType.stringValue();
            if (bean.startsWith("bean:")) {
                beans.put(bean, bean.substring(5));
                log.trace("\t---> " + beanType);
            }
        }
        return beans;
    }

	@Override
	public Future execute(Asset asset, Map bindings) throws IQException, AssetNotSupported {
		throw new IQException("Executor Not Implemented: "+asset);
	}
}
