package com.scorpio4.iq.exec;

import com.scorpio4.assets.Asset;
import com.scorpio4.assets.AssetHelper;
import com.scorpio4.fact.FactSpace;
import com.scorpio4.oops.AssetNotSupported;
import com.scorpio4.oops.ConfigException;
import com.scorpio4.oops.IQException;
import com.scorpio4.vendor.sesame.io.SPARQLer;
import com.scorpio4.vocab.COMMON;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Scorpio4 (c) 2014
 * Module: com.scorpio4.iq
 * User  : lee
 * Date  : 17/06/2014
 * Time  : 10:11 PM
 */
public class Inferring implements Executable {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    FactSpace factSpace;

    public Inferring(RepositoryConnection connection) {
        this.factSpace = new FactSpace(connection, "bean:"+getClass().getCanonicalName());
    }

    public Inferring(FactSpace factSpace) {
        this.factSpace=factSpace;
    }

    @Override
    public Future execute(Asset asset, Map bindings) throws IQException, AssetNotSupported {
	    if (asset.getMimeType().equals(COMMON.MIME_SPARQL)) throw new AssetNotSupported("Not SPARQL: "+asset);
        try {
            return new NotInferFuture(this, asset, bindings);
        } catch (RepositoryException e) {
            throw new IQException(e.getMessage());
        } catch (QueryEvaluationException e) {
            throw new IQException(e.getMessage());
        } catch (MalformedQueryException e) {
            throw new IQException(e.getMessage());
        } catch (IOException e) {
            throw new IQException(e.getMessage());
        } catch (ConfigException e) {
            throw new IQException(e.getMessage());
        }
    }
}
class NotInferFuture implements Future {
    private final Logger log = LoggerFactory.getLogger(Scripting.class);
    Object result = null;

    public NotInferFuture(Inferring inferring, Asset asset, Map paramaters) throws IQException, RepositoryException, QueryEvaluationException, MalformedQueryException, IOException, ConfigException {
        SPARQLer sparqLer = new SPARQLer(inferring.factSpace);
        Asset newAsset = AssetHelper.getAsset(asset, paramaters);
        int copied = sparqLer.copy(newAsset.toString());
        result = copied;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        return result;
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return result;
    }
}
