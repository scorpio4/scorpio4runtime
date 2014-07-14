package com.scorpio4.vendor.sesame.sail;
/*
 *   Scorpio4 - Apache Licensed
 *   Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *
 */
import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.*;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.NotifyingSailBase;
import org.openrdf.sail.helpers.NotifyingSailConnectionBase;
import org.openrdf.sail.helpers.SailBase;

import java.util.HashMap;
import java.util.Map;

/**
 * scorpio4 (c) 2013
 * Module: com.scorpio4.vendor.sesame
 * User  : lee
 * Date  : 21/10/13
 * Time  : 11:03 PM
 */
public class SQLSail extends NotifyingSailBase {

    @Override
    protected void shutDownInternal() throws SailException {

    }

    @Override
    protected NotifyingSailConnection getConnectionInternal() throws SailException {
        return null;  
    }

    @Override
    public boolean isWritable() throws SailException {
        return false;  
    }

    @Override
    public ValueFactory getValueFactory() {
        return null;  
    }
}

class SQLSailConnection extends NotifyingSailConnectionBase {
    Map<String, Map> statements = new HashMap();

    public SQLSailConnection(SailBase sailBase) {
        super(sailBase);
    }

    @Override
    protected void closeInternal() throws SailException {

    }

    @Override
    protected CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateInternal(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings, boolean b) throws SailException {
        return null;  
    }

    @Override
    protected CloseableIteration<? extends Resource, SailException> getContextIDsInternal() throws SailException {
        return null;  
    }

    @Override
    protected CloseableIteration<? extends Statement, SailException> getStatementsInternal(Resource resource, URI uri, Value value, boolean b, Resource... resources) throws SailException {
        return null;  
    }

    @Override
    protected long sizeInternal(Resource... resources) throws SailException {
        return 0;  
    }

    @Override
    protected void startTransactionInternal() throws SailException {

    }

    @Override
    protected void commitInternal() throws SailException {

    }

    @Override
    protected void rollbackInternal() throws SailException {

    }

    @Override
    protected void addStatementInternal(Resource resource, URI uri, Value value, Resource... resources) throws SailException {

    }

    @Override
    protected void removeStatementsInternal(Resource resource, URI uri, Value value, Resource... resources) throws SailException {

    }

    @Override
    protected void clearInternal(Resource... resources) throws SailException {

    }

    @Override
    protected CloseableIteration<? extends Namespace, SailException> getNamespacesInternal() throws SailException {
        return null;  
    }

    @Override
    protected String getNamespaceInternal(String s) throws SailException {
        return null;  
    }

    @Override
    protected void setNamespaceInternal(String s, String s2) throws SailException {

    }

    @Override
    protected void removeNamespaceInternal(String s) throws SailException {

    }

    @Override
    protected void clearNamespacesInternal() throws SailException {

    }
}
