package com.ericsson.dps.modelparser.modelelements;

import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ericsson.dps.modelparser.builder.ModelParser;

public class ElementAttribute extends DefaultHandler{

	private final AttributeContainer parentContainer;
	private final String attributeName;

	private boolean isSequence = false;
	private DataType modelDataType = null;
	private DataType storageDataType = null;
	
	private String structName = null;
	private String structModel = null;		// Model where struct is defined, can be the current one.
	private String structModelVersion = null;

	// We use ints for enums so not used.

	private String enumName = null;
	private String enumModel = null;		// Model where enum is defined, can be the current one.
	private String enumModelVersion = null;
	
	// Our models don't have DDTs at the moment so not used
	
	private String derivedTypeName = null;
	private String derivedTypeModel = null;	// Model where derived type is defined, can be current one.
	private String derivedTypeModelVersion = null;

	public ElementAttribute(final Attributes attributes, final AttributeContainer parent) {
		this.parentContainer = parent;
		this.attributeName = attributes.getValue("name");
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if(qName.equalsIgnoreCase("sequence")){
			this.isSequence = true;
		}

		if(qName.equalsIgnoreCase("string")){
			this.modelDataType = DataType.STRING;
			this.storageDataType = DataType.STRING;
		} else if(qName.equalsIgnoreCase("wstring")){
			this.modelDataType = DataType.STRING;
			this.storageDataType = DataType.STRING;
		} else if(qName.equalsIgnoreCase("char")){
			this.modelDataType = DataType.CHAR;
			this.storageDataType = DataType.STRING;
		} else if(qName.equalsIgnoreCase("moRef")){
			this.modelDataType = DataType.MOREF;
			this.storageDataType = DataType.STRING;
		} else if(qName.equalsIgnoreCase("boolean")){
			this.modelDataType = DataType.BOOLEAN;
			this.storageDataType = DataType.BOOLEAN;
		} else if(qName.equalsIgnoreCase("enumRef")){
			this.modelDataType = DataType.ENUM;
			this.storageDataType = DataType.INTEGER;
			this.enumName = attributes.getValue("name");
			final String enumMim = attributes.getValue("mimName");
			if(enumMim != null){
				this.enumModel = enumMim;
			} else {
				this.enumModel = this.parentContainer.getMimElement().getModelName();
				this.enumModelVersion = this.parentContainer.getMimElement().getModelVersion();
			}
		} else if(qName.equalsIgnoreCase("structRef")){
			this.modelDataType = DataType.STRUCT;
			this.storageDataType = DataType.STRUCT;
			this.structName = attributes.getValue("name");
			final String structMim = attributes.getValue("mimName");
			if(structMim != null){
				this.structModel = structMim;
			} else {
				this.structModel = this.parentContainer.getMimElement().getModelName();
				this.structModelVersion = this.parentContainer.getMimElement().getModelVersion();
			}
		} else if(qName.equalsIgnoreCase("long")){
			this.modelDataType = DataType.INTEGER;
			this.storageDataType  = DataType.INTEGER;
		} else if(qName.equalsIgnoreCase("longlong")){
			this.modelDataType = DataType.LONG;
			this.storageDataType  = DataType.LONG;
		} else if(qName.equalsIgnoreCase("short")){
			this.modelDataType = DataType.SHORT;
			this.storageDataType  = DataType.INTEGER;
		} else if(qName.equalsIgnoreCase("octet")){
			this.modelDataType = DataType.OCTET;
			this.storageDataType  = DataType.INTEGER;
		} else if(qName.equalsIgnoreCase("double")){
			this.modelDataType = DataType.DOUBLE;
			this.storageDataType  = DataType.DOUBLE;
		} else if(qName.equalsIgnoreCase("float")){
			this.modelDataType = DataType.FLOAT;
			this.storageDataType  = DataType.DOUBLE;
		} else if(qName.equalsIgnoreCase("derivedDataTypeRef")){
			this.modelDataType = DataType.DERIVED;
			this.storageDataType  = DataType.DERIVED;
			this.derivedTypeName = attributes.getValue("name");
			final String derivedTypeMim = attributes.getValue("mimName");
			if(derivedTypeMim != null){
				this.derivedTypeModel = derivedTypeMim;
			} else {
				this.derivedTypeModel = this.derivedTypeName;
			}
			System.err.println("PANIC!!!! Found a derived data type Ref! Not implemented yet!!!");
		}
	}

	public String getAttributeName() {
		return attributeName;
	}


	/**
	 * Returns the Java Type used for storing of the Attribute.
	 * Note: Only the base type will be returned, irrespective
	 * of whether this is a sequence.
	 */
	public String getStorageBaseType(){
		String ret = null;
		
		if(storageDataType == null){
			System.err.println("Cannot determine storage type for " + toString());
		}
		
		switch(storageDataType){
		case BOOLEAN:
			ret = "java.lang.Boolean";
			break;
		case DOUBLE:
			ret = "java.lang.Double";
			break;
		case FLOAT:
			ret = "java.lang.Float";
			break;
		case INTEGER:
			ret = "java.lang.Integer";
			break;
		case LONG:
			ret = "java.lang.Long";
			break;
		case STRING:
			ret = "java.lang.String";
			break;
		case STRUCT:
			ret = ModelParser.PACKAGE_PREFIX_FOR_GENERATION + structModel + ".entity." + structName;
			break;
		default:
			System.err.println("Cannot determine storage type for " + toString());
		}
		
		return ret;
	}
	
	/**
	 * Returns the Java Type used for stroing of the Attribute
	 */
	private String getFullStorageType(){
		String ret = getStorageBaseType();
		
		if(isSequence){
			ret = "java.util.List<" + ret + ">";
		}
		
		return ret;
	}
	
	
	/**
	 * Does a merge with the "other" attribute. In reality no merge is happening, but
	 * we check that storage data types are compatible.
	 */
	public void merge(final ElementAttribute otherAttribute){
		if(!this.getFullStorageType().equals(otherAttribute.getFullStorageType())){
			System.err.println("Attribute Data Type re-declaring! Mine: " + toString() + " - other version=" + otherAttribute.parentContainer.getMimElement().getModelVersion() + " - My type: " + getFullStorageType() + " - Other Type: " + otherAttribute.getFullStorageType());
		}
	}
	
	public ElementStruct getStructDef(Map<String, ElementMim> versionedModels) {
		final String key = structModel + ":" + structModelVersion;
		final ElementMim mim = versionedModels.get(key);
		return mim.getStructs().get(structName);
	}
	
	public Object fromString(final String data){
		if(data == null){
			return null;
		}
		switch(storageDataType){
		case BOOLEAN:
			return Boolean.parseBoolean(data);			
		case DOUBLE:
			return data.length() ==0 ? 0 : Double.parseDouble(data);
		case FLOAT:
			return data.length() ==0 ? 0 : Float.parseFloat(data);
		case INTEGER:
			if(modelDataType == DataType.ENUM){
				return 0;		// TODO: Convert to correct enum value. We don't care for now, just shove in zero.
			} else {
				try {				
					return data.length() ==0 ? 0 : Integer.parseInt(data);
				} catch ( java.lang.NumberFormatException nfe){
					System.out.println("ERROR_NOT_INTEGER --- attribute: " + attributeName +" parent:  " + parentContainer.getMimElement().getModelName() + " : " + parentContainer.getMimElement().getModelVersion());
					System.err.println("ERROR_NOT_INTEGER --- attribute: " + attributeName +" parent:  " + parentContainer.getMimElement().getModelName() + " : " + parentContainer.getMimElement().getModelVersion());
					throw nfe;
				}
			}
		case LONG:
			return data.length() ==0 ? 0 : Long.parseLong(data);
		case STRING:
			return data;
		default :
			// DataType Not Supported !!!!! but returning Null 
			// May be this is the reason we are getting NullPointer Exception ?
			System.out.println("\n\t\t attribute: " + attributeName + " \t-- TYPE NOT SUPPORTED --\t " + storageDataType);
			break;
		}
		// I shouldn't  be here !!! 		
		return null;
	}
	
	/*
	 * ======================= For Templates =============================
	 */
	
	public boolean getIsSequence(){
		return isSequence;
	}

	public boolean getIsStruct(){
		return storageDataType == DataType.STRUCT;
	}
	
	public boolean getIsComplexDataType(){
		return (getIsSequence() || getIsStruct());
	}
	
	@Override
	public String toString() {
		return "Attribute=" + attributeName + "; Parent=" + parentContainer.classOrStructName + "; Model=" + parentContainer.getMimElement().getModelName() + "; Version=" + parentContainer.getMimElement().getModelVersion();
	}


}
