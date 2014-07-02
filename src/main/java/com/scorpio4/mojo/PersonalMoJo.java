package com.scorpio4.mojo;

import org.apache.maven.project.MavenProject;


/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.mojo
 * User  : lee
 * Date  : 2/07/2014
 * Time  : 11:45 PM
 */

/**
 * Goal which starts a Personal Server
 *
 * @goal Personal Server
 *
 * @phase process-sources
 */

public class PersonalMoJo extends ScorpioMojo {

	/**
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	public MavenProject project;

	public MavenProject getProject() {
		return project;
	}

	@Override
	public void executeInternal() throws Exception {
	}
}
