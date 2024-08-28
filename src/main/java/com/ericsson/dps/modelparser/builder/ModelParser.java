package com.ericsson.dps.modelparser.builder;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.ericsson.dps.modelparser.modelelements.*;
import com.ericsson.util.io.VirtualFile;
import com.ericsson.util.io.impl.VirtualFileBasicImpl;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import com.ericsson.dps.schemagenerator.SchemaGenerator;


/**
 * 
 * @author 
 *
 */
public class ModelParser  {
	
	private static final Logger LOGGER = Logger.getLogger(ModelParser.class
			.getCanonicalName());
	
	public static String PACKAGE_PREFIX_FOR_GENERATION = null;

	private VirtualFile modelDir = null;;
	

	private final Map<String, ElementMim> versionedModels = new HashMap<String, ElementMim>();
	private final List<ElementMim> mergedModels = new ArrayList<ElementMim>();

	private final Map<String, ElementInterMim> interMims = new HashMap<String, ElementInterMim>();

	private Map<String, ElementContainment> allContainments = new HashMap<String, ElementContainment>();
	private Map<String, ElementAssociation> allAssociations = new HashMap<String, ElementAssociation>();
	
	
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
	


	
	public ModelParser(final VirtualFile modelDir) {
		this.modelDir = modelDir;
	}
	


	public Map<String, ElementMim> getVersionedModels(){
		return versionedModels;
	}
	
	public List<ElementMim> getMergedModels(){
		return mergedModels;
	}
	
	public void readModels(final boolean merge) throws SAXException, ParserConfigurationException, IOException {
		readInFiles();
		handleRelationships();
		replaceDerivedDataTypes();
		removeInterMimsWhereOneMimDoesNotExist();
		if(merge){
			mergeModels();
			copyRelationshipsIntoMergedModels();
		}
	}
	
	/**
	 * Take all relationships and give them to each ElementClass instance. It will filter
	 * out the ones only applicable to itself.
	 */
	private void copyRelationshipsIntoMergedModels() {
		
		for(ElementMim mergedModel : mergedModels){
			Collection<ElementClass> classes = mergedModel.getClasses().values();
			for(ElementClass elemClass : classes){
				elemClass.copyInRelationships(allContainments, allAssociations);
			}
		}
	}
	
	/**
	 * Method to extract all relationships and remove duplicates (as we operate on the versioned one's)
	 */
	private void handleRelationships() {
		
		for(ElementMim versionedMim : versionedModels.values()){
			List<ElementRelationship> relationships = versionedMim.getRelationships();
			for(ElementRelationship rel : relationships){
				if(rel.isContainment()){
					ElementContainment cont = rel.getContElement();
					String key = cont.getParentMimName() + ":" + cont.getParentClassName() + ":" + cont.getChildMimName() + ":" + cont.getChildClassName();
					if(!allContainments.containsKey(key)){
						allContainments.put(key, cont);
					}
				} else if(rel.isAssociation()){
					ElementAssociation assoc = rel.getAssocElement();
					String key = assoc.getAssociationName() + ":" + assoc.getAssocEnd1MimName() + ":" + assoc.getAssocEnd1ClassName() + ":" + assoc.getAssocEnd2MimName() + ":" + assoc.getAssocEnd2ClassName();
					if(!allAssociations.containsKey(key)){
						allAssociations.put(key, assoc);
					}
					if(assoc.isBiAssociation()){
						key = assoc.getAssociationName() + ":" + assoc.getAssocEnd2MimName() + ":" + assoc.getAssocEnd2ClassName() + ":" + assoc.getAssocEnd1MimName() + ":" + assoc.getAssocEnd1ClassName();
						if(!allAssociations.containsKey(key)){
							allAssociations.put(key, new ElementAssociation(assoc));
						}						
					}
				}
			}
		}
		
		for(ElementInterMim interMim : interMims.values()){
			List<ElementRelationship> relationships = interMim.getRelationships();
			for(ElementRelationship rel : relationships){
				if(rel.isContainment()){
					ElementContainment cont = rel.getContElement();
					String key = cont.getParentMimName() + ":" + cont.getParentClassName() + cont.getChildMimName() + ":" + cont.getChildClassName();
					if(!allContainments.containsKey(key)){
						allContainments.put(key, cont);
					}
				} else if(rel.isAssociation()){
					ElementAssociation assoc = rel.getAssocElement();
					String key = assoc.getAssociationName() + ":" + assoc.getAssocEnd1MimName() + ":" + assoc.getAssocEnd1ClassName() + ":" + assoc.getAssocEnd2MimName() + ":" + assoc.getAssocEnd2ClassName();
					if(!allAssociations.containsKey(key)){
						allAssociations.put(key, assoc);
					}
					if(assoc.isBiAssociation()){
						key = assoc.getAssociationName() + ":" + assoc.getAssocEnd2MimName() + ":" + assoc.getAssocEnd2ClassName() + ":" + assoc.getAssocEnd1MimName() + ":" + assoc.getAssocEnd1ClassName();
						if(!allAssociations.containsKey(key)){
							allAssociations.put(key, new ElementAssociation(assoc));
						}						
					}
				}
			}
		}
		
	}
	
	/**
	 * Remove containment and association relationships where one of the Mims does not exist
	 */
	private void removeInterMimsWhereOneMimDoesNotExist() {
		
		// Get names of all MIMs that we have parsed.
		
		Set<String> uniqueMimNames = new HashSet<String>();
		
		Collection<ElementMim> versionedMimElements = versionedModels.values();
		for(ElementMim versionedMimElement : versionedMimElements){
			uniqueMimNames.add(versionedMimElement.getModelName());
		}
		
		// Go over all relationships and remove whatever relationship has at least one
		// endpoint (parent, child, association end) in a MIM that does not exist, other
		// wise we would geenrate it in source and the compile would fail.
		
		
		Map<String, ElementContainment> containmentsOk = new HashMap<String, ElementContainment>();
		Set<Entry<String, ElementContainment>> allConts = allContainments.entrySet();
		for(Entry<String, ElementContainment> entry : allConts){
			ElementContainment oneCont = entry.getValue();
			if(uniqueMimNames.contains(oneCont.getParentMimName()) && uniqueMimNames.contains(oneCont.getChildMimName())){
				containmentsOk.put(entry.getKey(), entry.getValue());
			}
		}
		allContainments = containmentsOk;
		
		
		Map<String, ElementAssociation> associationsOk = new HashMap<String, ElementAssociation>();
		Set<Entry<String, ElementAssociation>> allAssoc = allAssociations.entrySet();
		for(Entry<String, ElementAssociation> entry : allAssoc){
			ElementAssociation oneAssoc = entry.getValue();
			if(uniqueMimNames.contains(oneAssoc.getAssocEnd1MimName()) && uniqueMimNames.contains(oneAssoc.getAssocEnd2MimName())){
				associationsOk.put(entry.getKey(), entry.getValue());
			}
		}
		allAssociations = associationsOk;
	}
	

	/**
	 * Reads in all the .xml files from the specified directory.
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */
	private void readInFiles() throws SAXException, ParserConfigurationException, IOException {
		final VirtualFile[] files = modelDir.listFiles();
		LOGGER.info("Found files number: "+files.length);
		for(VirtualFile file : files){
			if(file.isFile() && file.canRead() && file.getName().endsWith(".xml")){
				readInOneFile(file);
			}
		}
	}
	
	/**
	 * Reads in one particular XML file and uses a SAX Parser to process it.
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 */
	private void readInOneFile(final VirtualFile file) throws SAXException, ParserConfigurationException, IOException {
		try {
			LOGGER.info("Processing: " + file.getName());
			final MomDtdRevDParser parser = new MomDtdRevDParser(versionedModels, interMims);
			final SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setValidating(false);

			final SAXParser sp = spf.newSAXParser();
			sp.parse(file.getInputStream(), parser);
		}catch(SAXException se) {
			throw se;
		}catch(ParserConfigurationException pce) {
			throw pce;
		}catch (IOException ie) {
			throw ie;
		}
	}	

	private void replaceDerivedDataTypes() {
		// TODO Not needed (yet)
		
	}

	/**
	 * Merges the multiple versions of the models into one model, for each model type.
	 */
	private void mergeModels() {

		/*
		 * Build a Map: Keys = Unique Model names; Values = the list of models of that type
		 */
		Map<String, ArrayList<ElementMim>> sortedModels = new HashMap<String, ArrayList<ElementMim>>();
		
		for(ElementMim em : versionedModels.values()){
			ArrayList<ElementMim> al = sortedModels.get(em.getModelName());
			if(al == null){
				al = new ArrayList<ElementMim>(20);
				sortedModels.put(em.getModelName(), al);
			}
			al.add(em);
		}

		/*
		 * Now process
		 */

		Set<Entry<String,ArrayList<ElementMim>>> entrySet = sortedModels.entrySet();
		for(Entry<String,ArrayList<ElementMim>> entry : entrySet){
			final String modelName = entry.getKey();
			System.out.println("Merging: " + modelName);

			final ArrayList<ElementMim> models = entry.getValue();
			if(models.size() == 1){
				// Nothing to merge, only one version of the model.
				mergedModels.add(models.get(0));
			} else {
				ElementMim into = new ElementMim(models.get(0).getModelName());
				for(ElementMim em : models){
					into.merge(em);
				}
				mergedModels.add(into);
			}
		}
	}

	

}
