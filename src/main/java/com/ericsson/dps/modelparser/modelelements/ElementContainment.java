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

public class ElementContainment extends DefaultHandler{

	private final String relationshipName;
	private final ElementMim elementMim;

	private boolean insideParentElement = false;
	private boolean insideChildElement = false;

	private String parentClassName = null;
	private String childClassName = null;

	private String parentMimName = null;
	private String childMimName = null;
	
	/**
	 * @param relationshipName
	 * @param elementMim
	 */
	public ElementContainment(String relationshipName, ElementMim elementMim) {
		this.relationshipName = relationshipName;
		this.elementMim = elementMim;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("parent")){
			insideParentElement = true;
		} else if (qName.equalsIgnoreCase("child")){
			insideChildElement = true;
		} else if(qName.equalsIgnoreCase("hasClass")) {
			if(insideParentElement){
				parentClassName = attributes.getValue("name");
				if(attributes.getValue("inMim") != null){
					parentMimName = attributes.getValue("inMim");
				}
			} else if(insideChildElement) {
				childClassName = attributes.getValue("name");
				if(attributes.getValue("inMim") != null){
					childMimName = attributes.getValue("inMim");
				}
			} else {
				// ????? WTF
			}
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

		if(qName.equalsIgnoreCase("parent")) {
			insideParentElement = false;
		} else if(qName.equalsIgnoreCase("child")) {
			insideChildElement= false;
		} else {
			// junk that we can ignore.
		}
	}
	
	public String getRelationshipName() {
		return relationshipName;
	}


	/**
	 * @return the childClassName
	 */
	public String getChildClassName() {
		return childClassName;
	}

	/**
	 * @return the parentClassName
	 */
	public String getParentClassName() {
		return parentClassName;
	}

	/**
	 * @return the childMimName
	 */
	public String getChildMimName() {
		return childMimName == null ? elementMim.getModelName() : childMimName;
	}

	/**
	 * @return the parentMimName
	 */
	public String getParentMimName() {
		return parentMimName == null ? elementMim.getModelName() : parentMimName;
	}
	
}
