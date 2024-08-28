package com.ericsson.dps.modelparser.modelelements;

import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ElementStruct extends AttributeContainer{

	private ElementAttribute currentAttr;

	public ElementStruct(final Attributes attributes, final ElementMim elementMim) {
		super(elementMim, attributes.getValue("name"));
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (currentAttr != null){
			currentAttr.startElement(uri, localName, qName, attributes);
		} else if(qName.equalsIgnoreCase("structMember")) {
			currentAttr = new ElementAttribute(attributes, this);
		} else {
			// junk that we can ignore.
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
	throws SAXException {

		if(qName.equalsIgnoreCase("structMember")) {
			mapOfMembers.put(currentAttr.getAttributeName(), currentAttr);
			currentAttr = null;
		} else if (currentAttr != null){
			currentAttr.endElement(uri, localName, qName);
		} else {
			// junk that we can ignore.
		}
	}

	public String getStructName() {
		return classOrStructName;
	}
	
	/*
	 * ======================= For Templates =============================
	 */
	
	public List<ElementAttribute> getStructMembers(){
		return getMembers();
	}


}
