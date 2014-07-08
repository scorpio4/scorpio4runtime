package com.scorpio4.iq;

import com.scorpio4.runtime.ExecutionEnvironment;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.iq
 * User  : lee
 * Date  : 7/07/2014
 * Time  : 8:34 PM
 */
public interface ActiveVocabulary {

	public void boot(ExecutionEnvironment engine) throws Exception;

	public void start() throws Exception;
	public void stop() throws Exception;

}
