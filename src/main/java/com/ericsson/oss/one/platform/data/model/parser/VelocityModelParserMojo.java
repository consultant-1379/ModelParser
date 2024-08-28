package com.ericsson.oss.one.platform.data.model.parser;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.xml.sax.SAXException;

import com.ericsson.dps.schemagenerator.SchemaGenerator;

/**
 * 
 * @author eanatpe
 * @goal velocity
 * @phase generate-sources
 *
 */
public class VelocityModelParserMojo extends AbstractModelParserMojo {

	/**
	 * @parameter expression="${packagePrefix}"
	 *            default-value="com.ericsson.oss.itpf.datalayer.datapersistencyservice.gen."
	 */
	private String packagePrefix;
	
	/**
	 * @parameter expression="${templateDirectory}"
	 *            default-value="${basedir}/src/main/config/templates/cs"
	 */
	private String templateDirectory;
	
	 @Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		//getLog().info("Starting Model parser maven plugin...");
		long startTime = System.currentTimeMillis();

		try {
			SchemaGenerator schemaGenerator = new SchemaGenerator(
					modelsDirectory, packagePrefix, outputDirectory,
					templateDirectory);
			schemaGenerator.generate();
			
		} catch (SAXException e) {
			throw new MojoExecutionException("A SAX Exception occured in ModelParser while reading models", e);
		} catch (ParserConfigurationException e) {
			throw new MojoExecutionException("A ParseConfiguration Exception occurred in ModelParser while reading models");
		} catch (IOException e) {
			throw new MojoExecutionException("An IO Exception occurred in ModelParser while reading models");
		} catch (Exception e) {
			throw new MojoExecutionException("General execution exception ", e);
		}
		
		long finishTime = System.currentTimeMillis();
		long time = finishTime - startTime;
		getLog().debug(
				String.valueOf("Finished Model Parser: " + System.nanoTime()));
		getLog().debug("Model Parser execution took: " + time + "ms");		
	}

	
}
