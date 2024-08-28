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

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ElementInterMim extends DefaultHandler{

	private String interMimName;

	private List<ElementRelationship> relationships = new ArrayList<ElementRelationship>();

	private ElementRelationship currentRelationship;

	public ElementInterMim(final Attributes attributes) {
		this.interMimName = attributes.getValue("name");
	}



	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if (currentRelationship != null) {
			currentRelationship.startElement(uri, localName, qName, attributes);
		} else if(qName.equalsIgnoreCase("relationship")) {
			currentRelationship = new ElementRelationship(attributes);
		} else {
			// junk that we can ignore.
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		if(qName.equalsIgnoreCase("relationship")) {
			relationships.add(currentRelationship);
			currentRelationship = null;
		} else if (currentRelationship != null){
			currentRelationship.endElement(uri, localName, qName);
		} else {
			// junk that we can ignore.
		}
	}


	/**
	 * @return the interMimName
	 */
	public String getInterMimName() {
		return interMimName;
	}


	/**
	 * @return the relationships
	 */
	public List<ElementRelationship> getRelationships() {
		return relationships;
	}
	

}
