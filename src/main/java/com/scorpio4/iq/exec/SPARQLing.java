package com.scorpio4.iq.exec;

import com.scorpio4.assets.Asset;
import com.scorpio4.oops.ConfigException;
import com.scorpio4.oops.IQException;
import com.scorpio4.template.PicoTemplate;
import com.scorpio4.vendor.sesame.util.SesameHelper;
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
 * @author lee
 * Date  : 17/06/2014
 * Time  : 10:11 PM
 */
public class SPARQLing implements Executable {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    protected RepositoryConnection connection;

    public SPARQLing(RepositoryConnection connection) {
        this.connection=connection;
    }

    @Override
    public Future execute(Asset asset, Map bindings) throws IQException {
        try {
            return new NotSPARQLFuture(this, asset, bindings);
        } catch (RepositoryException e) {
            throw new IQException(" Repository Error: "+e.getMessage(),e);
        } catch (QueryEvaluationException e) {
            throw new IQException("Query Error: "+e.getMessage(),e);
        } catch (MalformedQueryException e) {
            throw new IQException("Query SyntaxError: "+e.getMessage(),e);
        } catch (IOException e) {
            throw new IQException("Asset Error: "+e.getMessage(),e);
        } catch (ConfigException e) {
            throw new IQException("Config Error: "+e.getMessage(),e);
        }
    }
}
class NotSPARQLFuture implements Future {
    private final Logger log = LoggerFactory.getLogger(Scripting.class);
    Object result = null;

    public NotSPARQLFuture(SPARQLing sparqLing, Asset asset, Map paramaters) throws IQException, RepositoryException, QueryEvaluationException, MalformedQueryException, IOException, ConfigException {
        PicoTemplate picoTemplate = new PicoTemplate(asset.toString());
        result = SesameHelper.toMapCollection(sparqLing.connection, picoTemplate.translate(paramaters));
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
