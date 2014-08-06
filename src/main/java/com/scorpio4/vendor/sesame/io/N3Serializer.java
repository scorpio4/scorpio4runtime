package com.scorpio4.vendor.sesame.io;
/*
 *   Scorpio4 - Apache Licensed
 *   Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *
 */
import com.scorpio4.fact.stream.N3Stream;

import java.io.IOException;

/**
 * Scorpio4 (c) 2010-2013
 *
 * Expresses the ability of a class to generate an N3 representation of itself
 */
public interface N3Serializer {

	public String getBaseURI();

	public N3Stream toN3() throws IOException;

}
