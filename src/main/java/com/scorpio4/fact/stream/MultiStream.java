package com.scorpio4.fact.stream;
/*
 *
 */


import com.scorpio4.oops.FactException;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Date  : 8/02/2014
 *
 * An observable stream. All registered streams will be notified of incoming facts.
 * Thinking towards a reactive programming model.
 *
 * See https://github.com/Reactive-Extensions/RxJS and http://www.reactivemanifesto.org/
 *
 */
public class MultiStream implements FactStream {
    Collection<FactStream> streams = new ArrayList();
    String id = "bean:"+getClass().getCanonicalName();

    public MultiStream() {
    }

    public MultiStream(String id) {
        this.id=id;
    }

    public MultiStream add(FactStream stream) {
        streams.add(stream);
        return this;
    }

    public void clear() {
        streams.clear();
    }

    @Override
    public void fact(String s, String p, Object o) throws FactException {
        for(FactStream stream: streams) stream.fact(s,p,o);
    }

    @Override
    public void fact(String s, String p, Object o, String xsdType) throws FactException {
        for(FactStream stream: streams) stream.fact(s,p,o,xsdType);
    }

    @Override
    public String getIdentity() {
        return id;
    }
}
