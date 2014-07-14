package com.scorpio4.output;
/*
 *   Scorpio4 - Apache Licensed
 *   Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *
 */
import java.io.*;


public interface IOProcessor {

	public void process(InputStream in, OutputStream out) throws IOException;
	
}
