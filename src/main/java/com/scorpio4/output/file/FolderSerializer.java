package com.scorpio4.output.file;
/*
 *   Scorpio4 - Apache Licensed
 *   Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *
 */
import com.scorpio4.fact.stream.N3Stream;
import com.scorpio4.output.rdf.N3Serializer;
import com.scorpio4.util.DateXSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Scorpio4 (c) 2010-2013
 * @author lee
 * Date: 17/01/13
 * Time: 7:55 PM
 * <p/>
 * This code does something useful
 */
public class FolderSerializer implements N3Serializer {
	private static final Logger log = LoggerFactory.getLogger(FolderSerializer.class);

	private File home;
	private String baseURI;

	public FolderSerializer(File home, String baseURI) {
		init(home, baseURI);
	}

	public File getHome() {
		return home;
	}

	public void setHome(File home) {
		this.home = home;
	}

	public void init(File home, String baseURI) {
		this.setHome(home);
		this.setBaseURI(baseURI);
	}

	public N3Stream toN3() throws IOException {
		return toN3(getHome());
	}

	public N3Stream toN3(File folder) throws IOException {
		N3Stream n3 = new N3Stream();
		n3.append("@prefix drive: <urn:scorpio4:drive:>.\n@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>.\n@prefix xsd: <http://www.w3.org/2001/XMLSchema#>.\n");
		n3.append("\n# FactDrive scan from: ").append(folder.getAbsolutePath());
		n3.append("\n# Generated on: ").append(new Date()).append("\n");
		n3.append("\n<").append(toRelativePath(folder)).append("> a drive:Home.\n\n");
		scanN3(n3, folder);
		return n3;
	}

	public void scanN3(N3Stream n3, File folder) throws IOException {
		DateXSD dateXSD = new DateXSD();
		String home = this.getHome().getCanonicalPath();
		String local = folder.getCanonicalPath();
//		String uri = getBaseURI() +"#"+local.substring(home.length());

		n3.append("<").append(toRelativePath(folder)).append(">");
		if (folder.isDirectory()) {
			n3.append(" a drive:Folder;");
		} else {
			n3.append(" a drive:File;");
			n3.append("\n\tdrive:url <").append(folder.toURI()).append(">;");
			n3.append("\n\tdrive:length \"").append(folder.length()).append("\"^^xsd:integer;");
		}

		n3.append("\n\tdrive:modified \"").append(dateXSD.format(folder.lastModified())).append("\"^^xsd:datetime;");
		n3.append("\n\tdrive:created \"").append(dateXSD.format(folder.lastModified())).append("\"^^xsd:datetime;");
		n3.append("\n\tdrive:name \"").append(folder.getName()).append("\"^^xsd:string;");

		n3.append("\n.\n");

		if (folder.isDirectory()) {
			File[] files = folder.listFiles();
			if (files==null) {
				return;
			}
			for(File file: files) {
				if (!file.isHidden()) {
					n3.append("<").append(toRelativePath(folder)).append("> drive:contains ").append("<").append(toRelativePath(file)).append(">.\n");
					scanN3(n3, file);
				}
			}
		}
	}

	public String toRelativePath(File file) throws IOException {
		String home = this.getHome().getCanonicalPath();
		String local = file.getCanonicalPath();
		return getBaseURI() +"#"+local.substring(home.length());
	}

	public static void main(String args[]) {
		try {
			FolderSerializer learner = new FolderSerializer(new File("/opt/IQKernel/IQ/FactDrive"), "urn:example:conference");
			System.err.println(learner.toN3());
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	public String getBaseURI() {
		return baseURI;
	}

	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}
}
