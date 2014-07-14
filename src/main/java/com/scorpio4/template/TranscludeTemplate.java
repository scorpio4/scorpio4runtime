package com.scorpio4.template;
/*
 *   Scorpio4 - Apache Licensed
 *   Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *
 */
import com.scorpio4.oops.ConfigException;
import com.scorpio4.oops.FactException;
import ognl.OgnlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scorpio4 (c) 2010-2013
 * @author lee
 * Date: 18/02/13
 * Time: 9:07 AM
 * <p/>
 * This code does something useful
 */
public class TranscludeTemplate extends PicoTemplate {
    private static final Logger log = LoggerFactory.getLogger(TranscludeTemplate.class);

    Transcluder transcluder = null;

	public TranscludeTemplate(String template, Transcluder transcluder) throws FactException, ConfigException {
        setTranscluder(transcluder);
		try {
			setTokens("{{", "}}");
			parse(template);
		} catch (OgnlException e) {
			throw new FactException("urn:scorpio4:output:template:oops:invalid#"+e.getMessage(), e);
		}
	}

    protected void acceptNode(String $node) {
        if ($node.startsWith("@")) {
            if (transcluder!=null) {
                Object $page = transcluder.transclude($node.substring(1));
                if ($page!=null) nodes.add($page);
                else nodes.add(getStartToken()+"missing-"+$node+getEndToken());
            }
            else nodes.add($node);
        } else {
            super.acceptNode($node);
        }
    }
    public Transcluder getTranscluder() {
        return transcluder;
    }

    public void setTranscluder(Transcluder transcluder) {
        this.transcluder = transcluder;
    }

}

