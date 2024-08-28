package com.ericsson.dps.modelparser.modelelements;

import java.util.*;
import java.util.Map.Entry;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ElementMim extends DefaultHandler{

	private String modelName;
	private String modelVersion;

	private Map<String, ElementClass> mapOfClasses = new HashMap<String, ElementClass>();
	private Map<String, ElementStruct> mapOfStructs = new HashMap<String, ElementStruct>();
	private Map<String, ElementEnum> mapOfEnums = new HashMap<String, ElementEnum>();
	
	private List<ElementRelationship> relationships = new ArrayList<ElementRelationship>();

	private ElementClass currentClass;
	private ElementStruct currentStruct;
	private ElementEnum currentEnum;
	private ElementRelationship currentRelationship;

	public ElementMim(final Attributes attributes) {
		this.modelName = attributes.getValue("name");
		this.modelVersion = attributes.getValue("version");
		final String release = attributes.getValue("release");
		if(release != null){
			this.modelVersion += "." + release;
		}
	}

	public ElementMim(final String modelName) {
		this.modelName = modelName;
	}

	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if (currentClass != null){
			currentClass.startElement(uri, localName, qName, attributes);
		} else if (currentStruct != null){
			currentStruct.startElement(uri, localName, qName, attributes);
		} else if (currentEnum != null){
			currentEnum.startElement(uri, localName, qName, attributes);
		} else if (currentRelationship != null) {
			currentRelationship.startElement(uri, localName, qName, attributes);
		} else if(qName.equalsIgnoreCase("class")) {
			currentClass = new ElementClass(attributes, this);
		} else if(qName.equalsIgnoreCase("struct")) {
			currentStruct = new ElementStruct(attributes, this);
		} else if(qName.equalsIgnoreCase("enum")) {
			currentEnum = new ElementEnum(attributes, this);
		} else if(qName.equalsIgnoreCase("relationship")) {
			currentRelationship = new ElementRelationship(attributes, this);
		} else {
			// junk that we can ignore.
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
	throws SAXException {

		if(qName.equalsIgnoreCase("class")) {
			if(!currentClass.getClassName().equalsIgnoreCase("ManagedObject")){
				mapOfClasses.put(currentClass.getClassName(), currentClass);
			}
			currentClass = null;
		} else if(qName.equalsIgnoreCase("struct")) {
			mapOfStructs.put(currentStruct.getStructName(), currentStruct);
			currentStruct = null;
		} else if(qName.equalsIgnoreCase("enum")) {
			mapOfEnums.put(currentEnum.getEnumName(), currentEnum);
			currentEnum = null;
		} else if(qName.equalsIgnoreCase("relationship")) {
			if(currentRelationship.isContainment() && 
					currentRelationship.getContElement().getChildClassName().equals("ManagedObject")){
			} else {
			relationships.add(currentRelationship);
			}
			currentRelationship = null;
		} else if (currentClass != null){
			currentClass.endElement(uri, localName, qName);
		} else if (currentStruct != null){
			currentStruct.endElement(uri, localName, qName);
		} else if (currentEnum != null){
			currentEnum.endElement(uri, localName, qName);
		} else if (currentRelationship != null){
			currentRelationship.endElement(uri, localName, qName);
		} else {
			// junk that we can ignore.
		}
	}

	public String getModelName() {
		return modelName;
	}

	public String getModelVersion() {
		return modelVersion;
	}

	public Map<String, ElementClass> getClasses(){
		return mapOfClasses;
	}

	public Map<String, ElementStruct> getStructs(){
		return mapOfStructs;
	}

	public Map<String, ElementEnum> getEnums(){
		return mapOfEnums;
	}

	/**
	 * @return the relationships
	 */
	public List<ElementRelationship> getRelationships() {
		return relationships;
	}
	

	
	
	/**
	 * Merges the contents of the "otherMim" into this one here.
	 */
	public void merge(final ElementMim otherMim){

		// Do Classes first
		
		Set<Entry<String, ElementClass>> otherClasses = otherMim.mapOfClasses.entrySet();
		for(Entry<String, ElementClass> otherClassesEntry : otherClasses){
			final String className = otherClassesEntry.getKey();
			if(this.mapOfClasses.containsKey(className)){
				this.mapOfClasses.get(className).merge(otherClassesEntry.getValue());
			} else {
				this.mapOfClasses.put(className, otherClassesEntry.getValue());
			}
		}
		
		// now structs
		
		Set<Entry<String, ElementStruct>> otherStructs = otherMim.mapOfStructs.entrySet();
		for(Entry<String, ElementStruct> otherStructsEntry : otherStructs){
			final String className = otherStructsEntry.getKey();
			if(this.mapOfStructs.containsKey(className)){
				this.mapOfStructs.get(className).merge(otherStructsEntry.getValue());
			} else {
				this.mapOfStructs.put(className, otherStructsEntry.getValue());
			}
		}
	}
	
}
