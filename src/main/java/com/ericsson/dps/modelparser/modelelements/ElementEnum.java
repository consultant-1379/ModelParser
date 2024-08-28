package com.ericsson.dps.modelparser.modelelements;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ElementEnum extends DefaultHandler{

	private final String enumName;
	private final ElementMim elementMim;
	
	private String currentEnumMember = null;
	private boolean inValue = false;
	private Integer memberValue = null;
	
	private Map<String, Integer> mapOfMembers = new HashMap<String, Integer>();
	
	public ElementEnum(final Attributes attributes, final ElementMim elementMim) {
		this.enumName = attributes.getValue("name");
		this.elementMim = elementMim;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (currentEnumMember != null && qName.equalsIgnoreCase("value")){
			inValue = true;
		} else if(qName.equalsIgnoreCase("enumMember")) {
			currentEnumMember = attributes.getValue("name");
		} else {
			// junk that we can ignore.
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if(inValue){
			String string = String.copyValueOf(ch, start, length);
			memberValue = Integer.parseInt(string);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
	throws SAXException {

		if(qName.equalsIgnoreCase("value")) {
			inValue = false;
		} else if(qName.equalsIgnoreCase("enumMember")) {
			mapOfMembers.put(currentEnumMember, memberValue);
			currentEnumMember = null;
		} else {
			// junk that we can ignore.
		}
	}

	public String getEnumName() {
		return enumName;
	}

	
	
	
}




/*

<enum name="ActivationScheme">
<description>Activation Scheme.</description>
<enumMember name="DEFAULT">
<value>0</value></enumMember>
<enumMember name="INTRA_MIB_ORDERING">
<value>1</value></enumMember>
<enumMember name="INTER_MIB_ORDERING">
<value>2</value></enumMember></enum>


*/