package com.scorpio4.runtime

import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor
import org.springframework.context.support.GenericApplicationContext
import org.springframework.mock.jndi.SimpleNamingContextBuilder

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.runtime
 * User  : lee
 * Date  : 23/07/2014
 * Time  : 12:59 AM
 *
 *
 */
public class RuntimeHelper {

	public static GenericApplicationContext newSpringContext(Map<String, Object> map) {
		SimpleNamingContextBuilder jndi = new SimpleNamingContextBuilder();

		for(String m: map) {

		}

		bindSelf(jndi);
		jndi.activate();

		GenericApplicationContext applicationContext = new GenericApplicationContext();
		RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(CommonAnnotationBeanPostProcessor.class);
		applicationContext.registerBeanDefinition("bean:"+CommonAnnotationBeanPostProcessor.class.getCanonicalName(), rootBeanDefinition);
		rootBeanDefinition.getPropertyValues().add("alwaysUseJndiLookup", true);
	}
}
