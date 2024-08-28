/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.dps.schemagenerator;


import java.io.*;
import java.util.*;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.ericsson.dps.modelparser.modelelements.ElementMim;

public class CommonArtefactGenerator {

	private final String outDirRoot;
	private final List<ElementMim> mergedModels;
	private final String packagePrefix;
	
	private File metaInfDir;
	
	public CommonArtefactGenerator(final String packagePrefix, final String outDirRoot, final List<ElementMim> mergedModels) {
		this.outDirRoot = outDirRoot;
		this.mergedModels = mergedModels;
		this.packagePrefix = packagePrefix;
	}

	public void generateArtefacts() {

		// Stuff in META-INF
		createClassList();
	}

	
	@SuppressWarnings("deprecation")
	private void createClassList() {
		
		File workingDir = new File(outDirRoot, "src");
		metaInfDir = new File(workingDir, "META-INF");
		
		try {
			final List<String> persClasses = new ArrayList<String>(300);
			
			for(ElementMim emim : mergedModels){
				Set<String> classNames = emim.getClasses().keySet();
				for(String className : classNames){
					persClasses.add(packagePrefix + emim.getModelName() + ".entity." + className);
				}
				Set<String> structNames = emim.getStructs().keySet();
				for(String structName : structNames){
					persClasses.add(packagePrefix + emim.getModelName() + ".entity." + structName);
				}
				persClasses.add(packagePrefix + emim.getModelName() + ".entity." + emim.getModelName() + "Inter");
			}
			Collections.sort(persClasses);
			
			final VelocityContext context = new VelocityContext();
			context.put("persClasses", persClasses);

			final File f = new File(metaInfDir, "generated.persClasses");
			
			final BufferedWriter out = new BufferedWriter(new FileWriter(f));
			Velocity.mergeTemplate("list_of_classes.vm", context, out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

	
}
