package com.scorpio4.iq.exec;

import com.scorpio4.assets.Asset;
import com.scorpio4.oops.IQException;
import groovy.lang.Writable;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.HashMap;
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
 * Time  : 9:56 PM
 */
public class Templating implements Executable {
//    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public Templating() {
    }

    @Override
    public Future execute(Asset asset, Map bindings) throws IQException {
        try {
            return new NotFutureTemplate(asset, bindings);
        } catch (ScriptException e) {
            throw new IQException(e.getMessage());
        } catch (IOException e) {
            throw new IQException("IO Error: "+e.getMessage(),e);
        } catch (ClassNotFoundException e) {
            throw new IQException("Class Path: " +e.getMessage(), e);
        }
    }
}

class NotFutureTemplate implements Future {
    private final Logger log = LoggerFactory.getLogger(Templating.class);
    SimpleTemplateEngine templateEngine = new SimpleTemplateEngine();
    Writable result = null;

    public NotFutureTemplate(Asset script, Map parameters) throws ScriptException, IQException, IOException, ClassNotFoundException {
        result = execute(script, parameters);
    }

    public Writable execute(Asset script, Map parameters) throws ScriptException, IQException, IOException, ClassNotFoundException {
        Template template = templateEngine.createTemplate(script.toString());
        Map<Object, Object> bindings = new HashMap<Object, Object>();
        bindings.putAll(parameters);
        log.debug("Template with {} ", bindings);
        return template.make(bindings);
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
