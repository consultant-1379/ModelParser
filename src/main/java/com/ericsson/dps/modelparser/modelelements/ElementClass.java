package com.ericsson.dps.modelparser.modelelements;

import java.util.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ElementClass extends AttributeContainer{
	
	private ElementAttribute currentAttr;
	private List<ElementContainment> containments = new ArrayList<ElementContainment>();
	private List<ElementAssociation> associations = new ArrayList<ElementAssociation>();

	
	public ElementClass(final Attributes attributes, final ElementMim elementMim) {
		super(elementMim, attributes.getValue("name"));
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (currentAttr != null){
			currentAttr.startElement(uri, localName, qName, attributes);
		} else if(qName.equalsIgnoreCase("attribute")) {
			currentAttr = new ElementAttribute(attributes, this);
		} else {
			// junk that we can ignore.
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
	throws SAXException {

		if(qName.equalsIgnoreCase("attribute")) {
			if(!currentAttr.getAttributeName().startsWith("pm")){
				mapOfMembers.put(currentAttr.getAttributeName(), currentAttr);
			}
			currentAttr = null;
		} else if (currentAttr != null){
			currentAttr.endElement(uri, localName, qName);
		} else {
			// junk that we can ignore.
		}
	}

	public String getClassName() {
		return classOrStructName;
	}
	
	
	
	
	
	/*
	 * ======================= For Templates =============================
	 */
	
	public List<ElementAttribute> getClassAttributes(){
		return getMembers();
	}
	
	
	public List<ElementContainment> getContainmentRelationships(){
		return containments;
	}

	public List<ElementAssociation> getAssociationRelationships(){
		return associations;
	}

	/**
	 * @param allContainments
	 * @param allAssociations
	 */
	public void copyInRelationships(Map<String, ElementContainment> allContainments, Map<String, ElementAssociation> allAssociations) {

		
		Collection<ElementContainment> allConts = allContainments.values();
		for(ElementContainment oneCont : allConts){
			if(oneCont.getParentMimName().equals(this.getMimElement().getModelName())
					&& oneCont.getParentClassName().equals(this.getClassName())){
				containments.add(oneCont);
			}
		}
		
		Collection<ElementAssociation> allAssoc = allAssociations.values();
		for(ElementAssociation oneAssoc : allAssoc){
			if(oneAssoc.getAssocEnd1MimName().equals(this.getMimElement().getModelName())
					&& oneAssoc.getAssocEnd1ClassName().equals(this.getClassName())){
				associations.add(oneAssoc);
			}
		}
	}	
	
}
