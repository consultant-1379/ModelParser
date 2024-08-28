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
package com.ericsson.dps.modelparser.modelelements;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ElementRelationship extends DefaultHandler{

	private final String relationshipName;
	private final ElementMim elementMim;

	
	private ElementContainment elemContainment = null;
	private ElementAssociation elemAssociation = null;
	

	public ElementRelationship(final Attributes attributes, final ElementMim elementMim) {
		this.relationshipName = attributes.getValue("name");
		this.elementMim = elementMim;
	}
	
	public ElementRelationship(final Attributes attributes) {
		this.relationshipName = attributes.getValue("name");
		this.elementMim = null;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("containment")){
			elemContainment = new ElementContainment(relationshipName, elementMim);
		} else if(qName.equalsIgnoreCase("biDirectionalAssociation")) {
			elemAssociation = new ElementAssociation(relationshipName, elementMim, true);
		} else if(qName.equalsIgnoreCase("uniDirectionalAssociation")) {
			elemAssociation = new ElementAssociation(relationshipName, elementMim, false);
		} else if(elemContainment != null){
			elemContainment.startElement(uri, localName, qName, attributes);
		} else if(elemAssociation != null){
			elemAssociation.startElement(uri, localName, qName, attributes);
		} else {
			// junk that we can ignore.
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		if(elemContainment != null){
			elemContainment.endElement(uri, localName, qName);
		} else if (elemAssociation != null){
			elemAssociation.endElement(uri, localName, qName);
		} else {
			// junk that we can ignore.
		}
	}

	
	public boolean isContainment(){
		return elemContainment != null;
	}

	public boolean isAssociation(){
		return elemAssociation != null;
	}

	public ElementContainment getContElement(){
		return elemContainment;
	}
	
	public ElementAssociation getAssocElement(){
		return elemAssociation;
	}
	
	
}
