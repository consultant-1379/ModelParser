package com.ericsson.oss.one.platform.data.model.parser;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public abstract class AbstractModelParserMojo extends AbstractMojo {

	protected static final String OUTPUT_DIR_PARAM_NAME = "outputDir";

	/**
	 * @parameter expression="${modelsDirectory}"
	 *            default-value="${basedir}/src/main/config/models"
	 */
	protected String modelsDirectory;

	/**
	 * @parameter expression="${outputDirectory}" default-value=
	 *            "${project.build.directory}/generated-sources/model-parser"
	 */
	protected String outputDirectory;	

}
