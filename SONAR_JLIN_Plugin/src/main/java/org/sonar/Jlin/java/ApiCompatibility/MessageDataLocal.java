package org.sonar.Jlin.java.ApiCompatibility;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

/**
 * This is the wrapper for reading the XML file with messages from Eclipse API tool.
 * Contains the data of one message until it is saved as JLin message.
 * Verifies that the structure of the eclipse xml file is unchanged.
 * @author d034003
 *
 */
public class MessageDataLocal {

	public enum ElementTypes{
		ANNOTATION_ELEMENT_TYPE,
		API_COMPONENT_ELEMENT_TYPE,
		CLASS_ELEMENT_TYPE,
		CONSTRUCTOR_ELEMENT_TYPE,
		ENUM_ELEMENT_TYPE,
		INTERFACE_ELEMENT_TYPE,
		FIELD_ELEMENT_TYPE,
		METHOD_ELEMENT_TYPE,
		UNKNOWN_ELEMENT_TYPE;
		
		public static ElementTypes init(String value){
			ElementTypes result;
			if("API_COMPONENT_ELEMENT_TYPE".equals(value)){
				result = ElementTypes.API_COMPONENT_ELEMENT_TYPE;
			}else{
				if("CLASS_ELEMENT_TYPE".equals(value)){
					result = ElementTypes.CLASS_ELEMENT_TYPE;
				}else{
					if("INTERFACE_ELEMENT_TYPE".equals(value)){
						result = ElementTypes.INTERFACE_ELEMENT_TYPE;
					}else{
						if("FIELD_ELEMENT_TYPE".equals(value)){
							result = ElementTypes.FIELD_ELEMENT_TYPE;
						}else{
							if("METHOD_ELEMENT_TYPE".equals(value)){
								result = ElementTypes.METHOD_ELEMENT_TYPE;
							}else{
								if("CONSTRUCTOR_ELEMENT_TYPE".equals(value)){
									result = ElementTypes.CONSTRUCTOR_ELEMENT_TYPE;
								}else{
									if("ENUM_ELEMENT_TYPE".equals(value)){
										result = ElementTypes.ENUM_ELEMENT_TYPE;
									}else{
										if("ANNOTATION_ELEMENT_TYPE".equals(value)){
											result = ElementTypes.ANNOTATION_ELEMENT_TYPE;
										}else{
											if("UNKOWN_ELEMENT_KIND".equals(value)){
												result = ElementTypes.UNKNOWN_ELEMENT_TYPE;
											}else{
												throw new RuntimeException("elementType unknown "+value);						
											}					
										}					
									}					
								}					
							}					
						}					
					}	
				}
			}
			return result;
		}
		
		public String bar(){
			switch (this){
			case ANNOTATION_ELEMENT_TYPE:    	return "annotation"; 
			case API_COMPONENT_ELEMENT_TYPE:	return "class/interface"; // TODO: What is it? Method/inner class, enum, other?
			case CLASS_ELEMENT_TYPE:			return "class";
			case CONSTRUCTOR_ELEMENT_TYPE:		return "constructor";
			case ENUM_ELEMENT_TYPE:				return "enumeration";
			case INTERFACE_ELEMENT_TYPE:		return "interface";
			case FIELD_ELEMENT_TYPE:			return "field";
			case METHOD_ELEMENT_TYPE:			return "method";
			case UNKNOWN_ELEMENT_TYPE:			return "unknown";
			default: return "undefined";
			}
		}
	}// ElementTypes
	
	public enum ChangeKind{
		ADDED,
		CHANGED,
		REMOVED,
		UNKNOWN;
		public static ChangeKind init(String value){
			ChangeKind result;
			if("ADDED".equals(value)){
				result = ChangeKind.ADDED;
			}else{
				if("CHANGED".equals(value)){
					result = ChangeKind.CHANGED;
				}else{
					if("REMOVED".equals(value)){
						result = ChangeKind.REMOVED;
					}else{
						if("UNKOWN_KIND".equals(value)){
							result = ChangeKind.UNKNOWN;
						}else{
							throw new RuntimeException("ChangeKind unknown "+value);						
						}					
					}					
				}					
			}
			return result;
		}
		
		@Override
		public String toString(){
			switch (this){
			case ADDED:		return "added";
			case CHANGED:	return "changed";
			case REMOVED: 	return "removed";
			case UNKNOWN: 	return "unknown";
			default: return "undefined";
			}
		}
	}// ChangeKind
	
	public String qualifiedReturnType(String value){ 
		if(value.equals("V")){
			return "void"; 
		}else{
			if(value.equals("I")){
				return "int"; 
			}else{
				if(value.equals("Z")){
					return "boolean";
				}else{
					if(value.equals("F")){
						return "float";
					}else{
						if(value.equals("B")){
							return "byte";
						}else{
							if(value.equals("C")){
								return "char";
							}else{
								if(value.equals("J")){
									return "long";
								}else{
									if(value.equals("D")){
										return "double";
									}else{
										if(value.equals("S")){
											return "short";
										}else{
											if(value.startsWith("L")&&value.endsWith(";")){ // object reference
												String objectName = value.substring(1);
												objectName = objectName.replace(";", "");
												objectName = objectName.replaceAll("/", ".");
												return objectName;
											}else{
												if(value.startsWith("[")){ // array
													arrays++;
													return qualifiedReturnType(value.substring(1));								
												}else{
													throw new RuntimeException("ReturnType unknown "+value);
												}
											}
										}
									}
								}
							}
						}
					}					
				}					
			}					
		}
	}// ReturnType
	
	
	private int messageArgumentsCounter;
	private boolean compatible;      	// true or false. 							Evaluate
	private ElementTypes elementType; 	// 											Evaluate.
	private int flagsValue;				// 7,8,16,21,25,46,48,54, see org.eclipse.pde.api.tools.internal.provisional.comparator.IDelta	Evaluate
//	private String flagsString;			// starting with flagsValue, extended by some other string to be more specific.
	private String key;					// element name! (class/attribute/method)	Evaluate			
	private String returnType;			// return type of method, whether primitive/reference, simple/array......
	private ChangeKind kind;			// ADDED or REMOVED	or CHANGED				Evaluate
	private int modifiers;				// see java.lang.reflect.Modifier 			Evaluate
	private String restrictions;		// 0,256,512								Access restrictions on class, expressed by @taglets or code.
	private String typeName;			// class name								Evaluate
	private List<String> messageArguments;
	
	//API Tooling upgrade changes
	private int newModifers;
	private String message;
	
	public int getNewModifers() {
		return newModifers;
	}

	public String getMessage() {
		return message;
	}

	// End of API Tooling upgrade changes
	private int arrays=0;
		
	protected MessageDataLocal(){
	}
	
	public void init(Attributes attr){
	
		messageArgumentsCounter = 0;
		messageArguments = new ArrayList<String>();
		String elementT;
		String flagsS;
		String modifiersS;
		String returnTypeS;
		
		String compatibleS = attr.getValue("compatible");
		elementT = attr.getValue("element_type");
		flagsS = attr.getValue("flags");	
		key = attr.getValue("key");
		String kindS = attr.getValue("kind");
		
		//API Tooling upgrade changes---> Total number of attributes 11
		//modifiersS = attr.getValue("modifiers");
		if(attr.getLength()== 11){
		modifiersS = attr.getValue("oldModifiers");			
		String newModifiersS;
		newModifiersS = attr.getValue("newModifiers");
		message = attr.getValue("message");
		newModifers = Integer.parseInt(newModifiersS);
		}
		else{
		modifiersS = attr.getValue("modifiers");
		}
		//End of API Tooling upgrade changes
		restrictions = attr.getValue("restrictions");
		typeName = attr.getValue("type_name");
		if(typeName==null){
			typeName="";
		}
		if(key==null){
			key="";
		}
		if(key.contains(")")){
			int pos = key.indexOf(")");
			returnTypeS = key.substring(pos+1);
			
			//API Tooling Upgradation
			if(returnTypeS.equals("TT;")){
			String value=	key.substring(key.indexOf(":L")+2);
			String objectName = value.substring(0, value.indexOf(';'));
			returnType = objectName.replaceAll("/", ".");
			}
			else{
			returnType = qualifiedReturnType(returnTypeS);
			}
			for(int i=0; i<arrays; i++){
				returnType = returnType+"[]";
			}
		}
		elementType = ElementTypes.init(elementT);
		kind = ChangeKind.init(kindS);
		flagsValue = MessageFlags.validate(flagsS);
		modifiers = Integer.parseInt(modifiersS);
		
		if((!"0".equals(restrictions))&&(!"256".equals(restrictions))&&(!"512".equals(restrictions))&&(!"1024".equals(restrictions))&& (!"2".equals(restrictions))&& (!"4".equals(restrictions))){
			// reverse engineering of semantics:
			// class missing in old api.jar, but available in new: 0 or 256 
			// 256:   @noextend @noimplement
			// 512: "final"
			//API Tooling Upgrade.. 2 and 4 Restrictions added.
			// 2: "static" 
			//4 : Methods being removed
			throw new RuntimeException("restrictions changed "+restrictions);
		}
		compatible = Boolean.parseBoolean(compatibleS);
	}

	/**
	 * verify that only one "message_arguments" tag is contained in each "delta"
	 * @param attr
	 */
	public void setArguments(Attributes attr) {
		if(messageArgumentsCounter!=0){
			throw new RuntimeException("Strange1 "+messageArgumentsCounter+attr.getLength());
		}
		messageArgumentsCounter++;	
	}

	/** 
	 * store all message arguments
	 * @param attr
	 */
	public void setArgument(Attributes attr) {
		String value = attr.getValue("value");
		messageArguments.add(value);
	}
	
//	public void write(){
//		write(compatible, elementType, typeName, key, kind, 
//				flags, modifiers, restrictions, messageArguments);
//	}
	
	public void endArguments() {
		if(messageArgumentsCounter!=1){
			throw new RuntimeException("Strange3 "+messageArgumentsCounter);
		}
	}

	public void endArgument() {
	}

	protected boolean getCompatible(){
		return compatible;
	}
	
	protected int getModifiers(){
		return modifiers;
	}
	
	protected ElementTypes getElementType(){
		return elementType;
	}
	
	protected String getKey(){
		return key;
	}
	
	public String getTypeName(){
		return typeName;
	}
	
	protected int getFlagsValue(){
		return flagsValue;
	}
	
	protected ChangeKind getKind(){
		return kind;
	}
	
	protected String getReturnType(){
		return returnType;
	}//
	
	protected List<String> getMessageArguments(){
		return messageArguments;
	}
	
	protected String getRestrictions(){
		return restrictions;
	}

//	protected String getFlagsString() {
//		return flagsString;
//	}


//	public abstract MessageDataLocal createMessage(Attributes attr, String baseName); // {
//	MessageDataLocal result = iwriter.createMessage(attr, baseName);
//	return result;
//}

}
