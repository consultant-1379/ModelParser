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

public class ElementAssociation extends DefaultHandler{

	private final String relationshipName;
	private final ElementMim elementMim;
	private final boolean isBDA;



	private String assocEnd1ClassName = null;		// For UDA, this is the "from" side
	private String assocEnd1MimName = null;
	private String assocEnd2ClassName = null;		// For UDA, this is the "to" side
	private String assocEnd2MimName = null;

	private boolean inFirstEnd = false;
	private boolean inSecondEnd = false;

	/**
	 * @param relationshipName
	 * @param elementMim
	 * @param isBDA 
	 */
	public ElementAssociation(String relationshipName, ElementMim elementMim, boolean isBDA) {
		this.relationshipName = relationshipName;
		this.elementMim = elementMim;
		this.isBDA = isBDA;
	}

	/**
	 * Create new association that is a mirror of the supplied one - for BDAs
	 * 
	 * @param assoc
	 */
	public ElementAssociation(ElementAssociation assoc) {
		this.relationshipName = assoc.relationshipName;
		this.elementMim = null;
		this.isBDA = true;

		this.assocEnd1MimName = assoc.getAssocEnd2MimName();			// Reverse endpoint 1 <-> 2
		this.assocEnd1ClassName = assoc.getAssocEnd2ClassName();
		this.assocEnd2MimName = assoc.getAssocEnd1MimName();
		this.assocEnd2ClassName = assoc.getAssocEnd1ClassName();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if(isBDA && qName.equalsIgnoreCase("associationEnd")){		// Start of an BDA End, can be first or second.
			if(assocEnd1ClassName == null){
				inFirstEnd = true;
			} else {
				inSecondEnd = true;
			}
		} else if (isBDA && qName.equalsIgnoreCase("hasClass")){		// IntraMim
			if(inFirstEnd){
				assocEnd1ClassName = attributes.getValue("name");
			} else {
				assocEnd2ClassName = attributes.getValue("name");
			}
		} else if (isBDA && qName.equalsIgnoreCase("mimClass")){		// InterMim
			if(inFirstEnd){
				assocEnd1ClassName = attributes.getValue("name");
			} else {
				assocEnd2ClassName = attributes.getValue("name");
			}
		} else if (isBDA && qName.equalsIgnoreCase("mimName")){
			if(inFirstEnd){
				assocEnd1MimName = attributes.getValue("name");
			} else {
				assocEnd2MimName = attributes.getValue("name");
			}
		} else if (!isBDA && qName.equalsIgnoreCase("associationEnd")){
			inSecondEnd = true;
		} else if (!isBDA && qName.equalsIgnoreCase("hasClass")){	// Intra
			if(inSecondEnd){
				assocEnd2ClassName = attributes.getValue("name");
			} else {
				assocEnd1ClassName = attributes.getValue("name");
				if(attributes.getValue("inMim") != null){
					assocEnd1MimName = attributes.getValue("inMim");	// Inter
				}
			}
		} else if(!isBDA && inSecondEnd && qName.equalsIgnoreCase("mimName")){		// Inter
			assocEnd2MimName = attributes.getValue("name");
		} else if(!isBDA && inSecondEnd && qName.equalsIgnoreCase("mimClass")){		// Inter
			assocEnd2ClassName = attributes.getValue("name");
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

		if(isBDA && qName.equalsIgnoreCase("associationEnd")){
			inFirstEnd = false;
			inSecondEnd = false;
		}
		else {
			// junk that we can ignore.
		}
	}


	public String getRelationshipName() {
		return relationshipName;
	}



	/**
	 * @return the biAssociation
	 */
	public boolean isBiAssociation() {
		return isBDA;
	}

	/**
	 * @return the assocEndAClassName
	 */
	public String getAssocEnd1ClassName() {
		return assocEnd1ClassName;
	}

	/**
	 * @return the assocEndBClassName
	 */
	public String getAssocEnd2ClassName() {
		return assocEnd2ClassName;
	}

	/**
	 * @return the assocEndAMimName
	 */
	public String getAssocEnd1MimName() {
		return assocEnd1MimName == null ? elementMim.getModelName() : assocEnd1MimName;
	}

	/**
	 * @return the assocEndBMimName
	 */
	public String getAssocEnd2MimName() {
		return assocEnd2MimName == null ? elementMim.getModelName() : assocEnd2MimName;
	}

	/**
	 * name of association is 
	 */
	public String getAssociationName() {
		return relationshipName;
	}

}
