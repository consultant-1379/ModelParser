package com.ericsson.dps.modelparser.builder;

import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ericsson.dps.modelparser.modelelements.ElementInterMim;
import com.ericsson.dps.modelparser.modelelements.ElementMim;

public class MomDtdRevDParser extends DefaultHandler{

	private final Map<String, ElementMim> mims;
	private final Map<String, ElementInterMim> interMims;

	private ElementMim currentMim;
	private ElementInterMim currentInterMim;

	public MomDtdRevDParser(final Map<String, ElementMim> mims, final Map<String, ElementInterMim> interMims) {
		this.mims = mims;
		this.interMims = interMims;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if (currentMim != null){
			currentMim.startElement(uri, localName, qName, attributes);
		} else if (currentInterMim != null){
			currentInterMim.startElement(uri, localName, qName, attributes);
		} else if(qName.equalsIgnoreCase("mim")) {
			currentMim = new ElementMim(attributes);
		} else if(qName.equalsIgnoreCase("interMim")) {
			currentInterMim = new ElementInterMim(attributes);
		} else {
			// junk that we can ignore.
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
	throws SAXException {

		if(qName.equalsIgnoreCase("mim")) {
			mims.put(currentMim.getModelName()+":"+currentMim.getModelVersion(), currentMim);
			currentMim = null;
		} else if (currentMim != null){
			currentMim.endElement(uri, localName, qName);
		} else if(qName.equalsIgnoreCase("interMim")) {
			interMims.put(currentInterMim.getInterMimName(), currentInterMim);
			currentInterMim = null;
		} else if (currentInterMim != null){
			currentInterMim.endElement(uri, localName, qName);
		} else {
			// junk that we can ignore.
		}
	}

}
