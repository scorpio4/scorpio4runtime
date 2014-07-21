package com.scorpio4.vendor.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.vendor.spring
 * @author lee
 * Date  : 4/07/2014
 * Time  : 2:24 AM
 */
public class CachedBeanFactory extends DefaultListableBeanFactory {
	Map beans = new HashMap();

	public CachedBeanFactory() {
	}

	public void cache(String name, Object bean) {
		Object put = beans.put(name, bean);
		assert put!=null;
	}

	@Override
	public boolean containsBeanDefinition(String name) {
		return (beans.containsKey(name));
	}

	@Override
	public Object getBean(String name) throws BeansException {
		if (name.startsWith("self:")) {
			String selfName = name.substring(5);
			return beans.get(selfName);
		}
		return null;
	}

	public Map getBeanConfig() {
		return beans;
	}
}
