package com.ericsson.dps.schemagenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.ericsson.dps.modelparser.modelelements.ElementClass;
import com.ericsson.dps.modelparser.modelelements.ElementMim;
import com.ericsson.dps.modelparser.modelelements.ElementStruct;

public class MimArtefactGenerator {

	private final String outDirRoot;
	private final ElementMim em;
	private final String entitesPackage;
	private final String entitesUtilsPackage;
	private final String packagePrefix;
	
	private File metaInfDir;
	private File entityFilesDir;
	private File utilFilesDir;
	
	public MimArtefactGenerator(final String packagePrefix, final String outDirRoot, final ElementMim em) {
		this.outDirRoot = outDirRoot;
		this.em = em;
		this.packagePrefix = packagePrefix;
		this.entitesPackage = packagePrefix + em.getModelName() + ".entity";
		this.entitesUtilsPackage = packagePrefix + em.getModelName() + ".entityutils";
	}

	public void generateArtefacts() {
		prepareWorkingDirectory();

		// The Entities
		createIntermediateClass();
		createClasses();
		createStructs();
		
		// The utility stuff
		createMoFactory();
		
		// Stuff in META-INF
//		createClassList();
//		createPersistenceXml();
	}



	private void prepareWorkingDirectory() {
		// Let's say out dir is /temp/gen, then our working directory is /temp/gen/<modelname>/


		/*
		 * Create source directory, e.g.:
		 * 
		 * /temp/gen/src
		 */
		File workingDir = new File(outDirRoot, "src");
		if(!workingDir.exists()){
			workingDir.mkdir();
		}

		/*
		 * Create META-INF folder, e.g.:
		 * 
		 * /temp/gen/src/META-INF
		 */
		metaInfDir = new File(workingDir, "META-INF");
		metaInfDir.mkdir();
		
		/*
		 * Create folder for all the entity Java classes, so e.g.:
		 * 
		 * /temp/gen/src/com/ericsson/oss/dps/model/RNC_NODE_MODEL/entity
		 */
		final String[] entityPathSplit = entitesPackage.split("\\.");

		entityFilesDir = workingDir;
		for(final String s : entityPathSplit){
			entityFilesDir = new File(entityFilesDir, s);
			entityFilesDir.mkdir();
		}
		
		/*
		 * Create folder for all the util classes, so e.g.:
		 * 
		 * /temp/gen/src/com/ericsson/oss/dps/model/RNC_NODE_MODEL/entityutils
		 */
		final String[] utilsPathSplit = entitesUtilsPackage.split("\\.");

		utilFilesDir = workingDir;
		for(final String s : utilsPathSplit){
			utilFilesDir = new File(utilFilesDir, s);
			utilFilesDir.mkdir();
		}
		
	}

	
	/**
	 * Helper method to recursively delete a tree of files
	 */
	private void deleteDir(final File f) {
		final String[] contents = f.list();
		if(contents != null){
			for(final String oneEntry : contents){
				final File f2 = new File(f, oneEntry);
				if(f2.isDirectory()){
					deleteDir(f2);		// recursive call
				}
				f2.delete();
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void createIntermediateClass() {
		try {
			final VelocityContext context = new VelocityContext();
			context.put("packageName", entitesPackage);
			context.put("modelName", em.getModelName());

			final File f = new File(entityFilesDir, em.getModelName() + "Inter.java");
			final BufferedWriter out = new BufferedWriter(new FileWriter(f));
			Velocity.mergeTemplate("intermediate_class.vm", context, out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("deprecation")
	private void createClasses() {
		final Map<String, ElementClass> classes = em.getClasses();
		for(final ElementClass ec : classes.values()){
			try {
				final VelocityContext context = new VelocityContext();
				context.put("packageName", entitesPackage);
				context.put("modelName", em.getModelName());
				context.put("className", ec.getClassName());
				context.put("classElement", ec);
				context.put("mimElement", em);
				context.put("packagePrefix", packagePrefix);


				final File f = new File(entityFilesDir, ec.getClassName() + ".java");
				final BufferedWriter out = new BufferedWriter(new FileWriter(f));
				Velocity.mergeTemplate("class.vm", context, out);
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
	}
	

	@SuppressWarnings("deprecation")
	private void createStructs() {
		final Map<String, ElementStruct> structs = em.getStructs();
		for(final ElementStruct es : structs.values()){
			try {
				final VelocityContext context = new VelocityContext();
				context.put("packageName", entitesPackage);
				context.put("modelName", em.getModelName());
				context.put("structName", es.getStructName());
				context.put("structElement", es);

				final File f = new File(entityFilesDir, es.getStructName() + ".java");
				final BufferedWriter out = new BufferedWriter(new FileWriter(f));
				Velocity.mergeTemplate("struct.vm", context, out);
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void createMoFactory() {
		try {
			List<String> classList = new ArrayList<String>(em.getClasses().keySet());
			Collections.sort(classList);
			
			final VelocityContext context = new VelocityContext();
			context.put("packageName", entitesUtilsPackage);
			context.put("entitiesPackageName", entitesPackage);
			context.put("modelName", em.getModelName());
			context.put("classList", classList);

			final File f = new File(utilFilesDir, "MoFactory.java");
			
			final BufferedWriter out = new BufferedWriter(new FileWriter(f));
			Velocity.mergeTemplate("mo_factory.vm", context, out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
//	@SuppressWarnings("deprecation")
//	private void createClassList() {
//		try {
//			final List<String> persClasses = new ArrayList<String>(300);
//			persClasses.addAll(em.getClasses().keySet());
//			persClasses.addAll(em.getStructs().keySet());
//			Collections.sort(persClasses);
//			
//			final VelocityContext context = new VelocityContext();
//			context.put("packageName", entitesPackage);
//			context.put("modelName", em.getModelName());
//			context.put("persClasses", persClasses);
//
//			final File f = new File(metaInfDir, em.getModelName() + ".persClasses");
//			
//			final BufferedWriter out = new BufferedWriter(new FileWriter(f));
//			Velocity.mergeTemplate("list_of_classes.vm", context, out);
//			out.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//	}
	
//	@SuppressWarnings("deprecation")
//	private void createPersistenceXml() {
//		try {
//			final List<String> persClasses = new ArrayList<String>(300);
//			persClasses.addAll(em.getClasses().keySet());
//			persClasses.addAll(em.getStructs().keySet());
//			Collections.sort(persClasses);
//			
//			final VelocityContext context = new VelocityContext();
//			context.put("packageName", entitesPackage);
//			context.put("persClasses", persClasses);
//
//			final File f = new File(metaInfDir, "persistence.xml");
//			
//			final BufferedWriter out = new BufferedWriter(new FileWriter(f));
//			Velocity.mergeTemplate("persistence.xml.vm", context, out);
//			out.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}		
//	}
	
}
