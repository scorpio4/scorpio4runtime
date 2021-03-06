package com.scorpio4.iq.capability;

import com.scorpio4.util.Bindable;

import java.util.Map;

/**
 * Scorpio4 (c) 2014
 * Module: com.scorpio4.iq.empower
 * @author lee
 * Date  : 19/06/2014
 * Time  : 12:32 AM
 */
public class BasicCapability implements Bindable {
	Map bindings;

    public BasicCapability() {
    }

    @Override
    public void bind(Map bindings) {
		this.bindings=bindings;
    }

	@Override
	public Map getBindings() {
		return bindings;
	}
}
