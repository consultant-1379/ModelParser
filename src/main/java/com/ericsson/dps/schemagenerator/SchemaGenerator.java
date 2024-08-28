package com.ericsson.dps.schemagenerator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.velocity.app.Velocity;
import org.xml.sax.SAXException;

import com.ericsson.dps.modelparser.builder.ModelParser;
import com.ericsson.dps.modelparser.modelelements.ElementMim;
import com.ericsson.util.io.impl.VirtualFileBasicImpl;

public class SchemaGenerator {

	private static final Logger LOGGER = Logger.getLogger(SchemaGenerator.class
			.getCanonicalName());	
	
	/**
	 * Directory where all the model XML files are.
	 */
	private String modelDir = "";
	/**
	 * Java package prefix to use for all generated Java classes
	 */
	private String packagePrefix = "";
	/**
	 * Where to place the final artefacts
	 */
	private String outDirRoot = "";
	/**
	 * Directory containing all the templates
	 */
	private String templateDir = "";
	
	private File workingDir = null;
	
	public SchemaGenerator(String modelDir, String packagePrefix, String outDir, String templateDir) {
		this.modelDir = modelDir;
		this.packagePrefix = packagePrefix;
		this.outDirRoot = outDir;
		this.templateDir = templateDir;
	}
	
	public void generate() throws SAXException, ParserConfigurationException, IOException {

		LOGGER.info("Using model directory " + this.modelDir);
		LOGGER.info("Using template directory " + this.templateDir);
		LOGGER.info("Generating to " + this.outDirRoot);
		LOGGER.info("Using prefix \"" + packagePrefix +"\"");

		final ModelParser mp = new ModelParser(new VirtualFileBasicImpl(this.modelDir));
		ModelParser.PACKAGE_PREFIX_FOR_GENERATION = packagePrefix;
		mp.readModels(true);
		final List<ElementMim> mergedModels = mp.getMergedModels();	
		
		initialiseVelocity();
		
		prepareWorkingDirectory();
		
		for(final ElementMim em : mergedModels){
			int classCount = em.getClasses().size();
			int structCount = em.getStructs().size();
			LOGGER.info("Generating " + em.getModelName() + "  [Classes: " + classCount + ", Structs: " + structCount + "]");
			final MimArtefactGenerator ag = new MimArtefactGenerator(packagePrefix, outDirRoot, em);
			ag.generateArtefacts();
		}
		
		final CommonArtefactGenerator cag = new CommonArtefactGenerator(packagePrefix, outDirRoot, mergedModels);
		cag.generateArtefacts();

	}

	private void prepareWorkingDirectory() {
		// Let's say out dir is /temp/gen, then our working directory is /temp/gen/

		/*
		 * Create working directory first, e.g.:
		 * 
		 * /temp/gen/
		 */
		workingDir = new File(outDirRoot);
		workingDir.mkdirs();
	}

	/**
	 * Does what it says on the tin
	 */
	private void initialiseVelocity() {
		Velocity.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, templateDir);
		Velocity.setProperty(Velocity.FILE_RESOURCE_LOADER_CACHE, true);
		Velocity.init();
	}	
	
}
