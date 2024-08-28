package com.ericsson.dps.modelparser.modelelements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.xml.sax.helpers.DefaultHandler;

public class AttributeContainer extends DefaultHandler{

	protected final ElementMim elementMim;
	protected final String classOrStructName;

	protected Map<String, ElementAttribute> mapOfMembers = new HashMap<String, ElementAttribute>();
	private List<ElementAttribute> listOfOrderedMembers = new ArrayList<ElementAttribute>();

	public AttributeContainer(final ElementMim elementMim, final String name) {
		this.elementMim = elementMim;
		this.classOrStructName = name;
	}

	public ElementMim getMimElement() {
		return elementMim;
	}
	
	public ElementAttribute getMember(final String memberName){
		return mapOfMembers.get(memberName);
	}
	
	public void merge(final AttributeContainer otherAttrContainer){
		Set<Entry<String, ElementAttribute>> otherMembers = otherAttrContainer.mapOfMembers.entrySet();
		for(Entry<String, ElementAttribute> otherMembersEntry : otherMembers){
			final String className = otherMembersEntry.getKey();
			if(this.mapOfMembers.containsKey(className)){
				this.mapOfMembers.get(className).merge(otherMembersEntry.getValue());
			} else {
				this.mapOfMembers.put(className, otherMembersEntry.getValue());
			}
		}
	}	

	public List<ElementAttribute> getMembers(){
		if(listOfOrderedMembers.size() == 0){
			listOfOrderedMembers.addAll(mapOfMembers.values());
			Collections.sort(listOfOrderedMembers, new Comparator<ElementAttribute>(){
				@Override
				public int compare(ElementAttribute arg0, ElementAttribute arg1) {
					return arg0.getAttributeName().toLowerCase().compareTo(arg1.getAttributeName().toLowerCase());
				}
			});	
		}
		
		return listOfOrderedMembers;
	}



}
