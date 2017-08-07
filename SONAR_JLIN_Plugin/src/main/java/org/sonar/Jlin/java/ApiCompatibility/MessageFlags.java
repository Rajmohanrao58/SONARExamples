package org.sonar.Jlin.java.ApiCompatibility;

import java.lang.reflect.Modifier;
import java.util.List;

import org.sonar.Jlin.java.JlinSensor;
import org.sonar.Jlin.java.ApiCompatibility.MessageDataLocal.ChangeKind;
import org.sonar.Jlin.java.ApiCompatibility.MessageDataLocal.ElementTypes;

/**
 * This class transforms flags that describe a change, as defined in 
 * org.eclipse.pde.api.tools.internal.provisional.comparator.IDelta
 * into a structured message suitable for humans. 
 * TODO: The strings are assembled hard coded. Replace by an external table 
 * of string templates.
 * 
 * DONE: Use "classif" in all "handle" methods: If "sourceCompatibleCaller" is "false", first 
 * call classif. If it is true, pass it on.
 * 
 * TODO: In case of unknown change, this code fails fast. This is bad for a JLin test where 
 * patch delivery is SLOW. Make configurable. Central: Default: Fail fast. JLin: Default: be robust.
 * 
 * TODO: In case of robust error handling: Generate JLin message with prio0, showing that there is a problem.
 * 
 * TODO: Add parameter "ignoreFlags", allowing to turn of handle methods that are missing or not robust.
 * 
 * DONE: Verify that packagaAndClass is set correct in all places (from param(0) in general). "9" and "40" no sample.
 * TODO: param(0) sometimes contains inner classes separated with ".", type_name correctly uses "$"
 * Switch where possible.
 *   
 * DONE: As workaround for Eclipse internal error, only parts of APIs are compared at a time. This results in lots of "missing class"
 * warnings. Add switch to suppress generation of "missing type" warnings. 
 *
 * @author d034003
 *
 */
public class MessageFlags {
	private MessagePriority priorities = null;
	private IUsageOfAPI classif= null;
	private boolean _ignoreMissingClasses = false;

	public static class Data{
		
		public String _messageId;
		public String _message;
		public int _prio;
		public String _flagsString;// used as anchor in wiki
		private String _chapter; // chapter / subpage in wiki
		private String _typeName;
		private String _signature;
		private boolean _suppress=false;
		//added variables "_flag", "_flagDescription", "_modifiers", "_changeKind", "_elementType" and "_elementName" for MessageWriterDelta 
		private int _flag;								//flag of change case from Separator
		private String _flagDescription;				//description of the change case that belongs to the flag number
		private String _modifiers;						//modifiers (e.x public, public static final...etc)
		private ChangeKind _changeKind;					//added/deleted/changed
		private ElementTypes _elementType;				//API/interface/class/method/field.....
		private String _returnTypeIfNeeded;				//return type of method if available and needed. Null in most of handle methods!
		private String _elementName;					//name of the element causing the difference (for every flag there is a different _elementName)
		//API Tool upgrade
		private String _newModifiers;
		private String _packageAndClass;
		
		/**
		 * 
		 * @param message the message text to display
		 * @param prio
		 * @param flagsString
		 * @param typeName package.Class
		 * @param completeSignature null or attributeName or methodName(Signature)
		 * @param flag
		 * @param flagDescription
		 * @param modifiers
		 * @param returnType
		 * @param kind
		 * @param elemType
		 * @param elemName
		 * 
		 */
		private Data(String messageId,String message, int prio, String flagsString, String chapter, String typeName, String completeSignature, int flag, 
				String flagDescription, String modifiers, String returnType, ChangeKind kind, ElementTypes elemType, String elemName, String packageAndClass){
			//System.out.println(packageAndClass);
			_messageId= messageId;
			_message = message;
			_prio = prio;
			_flagsString = flagsString;
			_chapter = chapter;
			_typeName = typeName;
			_signature = typeName;
			if (completeSignature!=null){
				_signature = _signature+"."+completeSignature;
			}
			_message = _message + ".\nUse the following line to suppress: "+_signature+"_"+_flagsString;
			_flag = flag;
			_flagDescription = flagDescription;
			_modifiers = modifiers;
			_returnTypeIfNeeded = returnType;
			_changeKind = kind;
			_elementType = elemType;
			_elementName = elemName;
			_packageAndClass= packageAndClass;
			StaticMessageFlags staticMessageFlags=new StaticMessageFlags(_messageId, _message, _prio, _flagsString, _chapter, _typeName, _signature, _flag, _flagDescription, _modifiers, _returnTypeIfNeeded, _changeKind, _elementType, _elementName,_packageAndClass);
			// Mapping To Sonar Dashboard
			JlinSensor.creteIssueFromJlinMessgae(staticMessageFlags);
			
		
			
		}
		
		public String getWikiURL(String baseUrl){
			String result = baseUrl+"/"+_chapter+"#"+_flagsString;
			return result;
		}
		
		public String getTypeName() {
			return _typeName;
		}
		public String getCompleteSignature() {
			return _signature;
		}
		public void setSuppress(boolean suppress) {
			_suppress = suppress;
		}
		public boolean getSuppressed() {
			return _suppress;
		}
		//added getters for MessageWriterDelta
		public int getFlag() {
			return _flag;
		}
		public String getFlagDescription() {
			return _flagDescription;
		}
		public String getModifiers() {
			return _modifiers;
		}
		public String getReturnType() {
			return _returnTypeIfNeeded;
		}
		public String getChangeKind() {
			return _changeKind.toString();
		}
		public String getElementType() {
			return _elementType.bar();
		}
		public String getElementName() {
			return _elementName;
		}
		//API Tool Upgrade
		public String getNewModifier() {
			return _newModifiers;
		}
		//
	}
	
	public MessageFlags(MessagePriority priorities,IUsageOfAPI classification, boolean ignoreMissingClasses){
		this._ignoreMissingClasses = ignoreMissingClasses;
		if (priorities!=null){
			this.priorities = priorities;
		}
		else{
			this.priorities = new MessagePriority(MessagePriority.CompatibilityRules.EHP_COMPATIBILITY);
		}
		classif = classification;
	}
	
	/**
	 * 
	 * @param oneMessage
	 * @return a data element containing relevant information for message generation or "null".
	 */
	public Data getData(MessageDataLocal oneMessage){
		boolean compatible = oneMessage.getCompatible();
		ElementTypes elementType = oneMessage.getElementType();
		ChangeKind kind = oneMessage.getKind();
		String returnTypeIfNeeded = oneMessage.getReturnType();
		int flagsValue = oneMessage.getFlagsValue();
		int modifiers = oneMessage.getModifiers();
		List<String> argValues = oneMessage.getMessageArguments();
		String typeName = oneMessage.getTypeName();
		String key = oneMessage.getKey();
		//API Tooling Upgrade
		String eclipseMessage = (oneMessage.getMessage() == null)? "" : "\n[EclipseMessage :"+oneMessage.getMessage()+"]";
		int newModifier= oneMessage.getNewModifers();
		MessageFlags.Data daten = getData(compatible, flagsValue, kind,modifiers,returnTypeIfNeeded,argValues,elementType, typeName, key, eclipseMessage, newModifier);
		return daten;
	}
	
	public Data getData(boolean binaryCompatible, int flags, ChangeKind kind,int modifiers, String returnType,
			List<String> params, ElementTypes elemType, String typeName, String key,String eclipseMessage ,int newModifier){
		
		switch (flags){
		case 0:   return new Data("NoFlag","Nothing changed (bug in Eclipse-Tool) ",4,Integer.toString(0),"",typeName,null,flags,
				"Nothing changed (bug in Eclipse-Tool)",Modifier.toString(modifiers),returnType,kind,elemType,"","");
		case 1:   return handle01(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 2:   return handle02(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 4:   return handle04(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 5:   return handle05(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 6:   return handle06(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 7:   return handle07(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 8:   return handle08(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 9:   return handle09(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 10:  return handle10(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 11:  return handle11(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 12:  return handle12(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 14:  return handle14(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 15:  return handle15(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 16:  return handle16(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 17:  return handle17(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 18:  return handle18(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 19:  return handle19(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 20:  return handle20(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 21:  return handle21(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 22:  return handle22(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 24:  return handle24(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 25:  return handle25(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 26:  return handle26(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 27:  return handle27(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 28:  return handle28(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 30:  return handle30(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 31:  return handle31(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 33:  return handle33(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 34:  return handle34(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 35:  return handle35(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 36:  return handle36(                 kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);		
		case 37:  return handle37(                 kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);		
		case 38:  return handle38(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 39:  return handle39(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 40:  return handle40(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 41:  return handle41(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 42:  return handle42(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 43:  return handle43(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 44:  return handle44(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 45:  return handle45(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 46:  return handle46(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 47:  return handle47(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 48:  return handle48(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 49:  return handle49(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 50:  return handle50(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 51:  return handle51(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 53:  return handle53(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 54:  return handle54(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 55:  return handle55(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 56:  return handle56(binaryCompatible,kind, modifiers,             params, elemType, typeName,eclipseMessage,newModifier);
		case 57:  return handle57(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 58:  return handle58(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 60:  return handle60(                 kind,                                elemType, typeName,eclipseMessage,newModifier);
		//API Tooling Upgradation
		case 61:  return handle61(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 62:  return handle62(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 63:  return handle63(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 67:  return handle67(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 68:  return handle68(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 69:  return handle69(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 70:  return handle70(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		case 72:  return handle72(binaryCompatible,kind, modifiers, returnType, params, elemType, typeName,eclipseMessage,newModifier);
		default:  return new Data("unknownflag","unknown flag "+flags,1, Integer.toString(flags), "", typeName,null,flags,
				"unknown flag",Modifier.toString(modifiers),returnType,kind,elemType,key,"");
		}
	}

	public static int validate(String flagsS){
		int result;
		if((!"0".equals(flagsS))&&
				(!"1".equals(flagsS))&&
				(!"2".equals(flagsS))&&
				(!"4".equals(flagsS))&&
				(!"5".equals(flagsS))&&
				(!"6".equals(flagsS))&&
				(!"7".equals(flagsS))&&
				(!"8".equals(flagsS))&&
				(!"9".equals(flagsS))&& // not documented
				(!"10".equals(flagsS))&&
				(!"11".equals(flagsS))&&
				(!"12".equals(flagsS))&&
				(!"14".equals(flagsS))&&// not documented
				(!"15".equals(flagsS))&&
				(!"16".equals(flagsS))&&
				(!"17".equals(flagsS))&&
				(!"18".equals(flagsS))&&
				(!"19".equals(flagsS))&&
				(!"20".equals(flagsS))&&
				(!"21".equals(flagsS))&&
				(!"22".equals(flagsS))&&
				(!"24".equals(flagsS))&&
				(!"25".equals(flagsS))&&
				(!"26".equals(flagsS))&&
				(!"27".equals(flagsS))&&
				(!"28".equals(flagsS))&&
				(!"30".equals(flagsS))&&				
				(!"31".equals(flagsS))&&
				(!"33".equals(flagsS))&&
				(!"34".equals(flagsS))&&
				(!"35".equals(flagsS))&&
				(!"36".equals(flagsS))&&
				(!"37".equals(flagsS))&&
				(!"38".equals(flagsS))&&
				(!"39".equals(flagsS))&&
				(!"40".equals(flagsS))&&
				(!"41".equals(flagsS))&&
				(!"42".equals(flagsS))&&
				(!"43".equals(flagsS))&&
				(!"44".equals(flagsS))&&
				(!"45".equals(flagsS))&&
				(!"46".equals(flagsS))&&
				(!"47".equals(flagsS))&&
				(!"48".equals(flagsS))&&
				(!"49".equals(flagsS))&&
				(!"50".equals(flagsS))&&
				(!"51".equals(flagsS))&&
				(!"53".equals(flagsS))&&
				(!"54".equals(flagsS))&&
				(!"55".equals(flagsS))&&
				(!"56".equals(flagsS))&&
				(!"57".equals(flagsS))&&
				(!"58".equals(flagsS))&&
				(!"60".equals(flagsS))&&
				(!"61".equals(flagsS))&&
				(!"62".equals(flagsS))&&
				(!"63".equals(flagsS))&&
				(!"67".equals(flagsS))&&
				(!"68".equals(flagsS))&&
				(!"69".equals(flagsS))&&
				(!"70".equals(flagsS))&&
				(!"72".equals(flagsS))){
			
			RuntimeException ex = new RuntimeException("flags unknown "+flagsS);
			ex.printStackTrace();
		}
		result = Integer.parseInt(flagsS);
		return result;
	}

	/**
	 * removing a "abstract" on a class is safe for callers. There are no calls to "new". 
	 * Inheriting a now instantiable class does still work. 
	 * @param binaryCompatible
	 * @param flags
	 * @param kind
	 * @param modifiers
	 * @param returnTypeIfNeeded
	 * @param params
	 * @param elemType
	 * @param typeName
	 * @return
	 */
	private Data handle01(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage,int newModifier) {
		boolean sourceCompatibleCaller;
		boolean sourceCompatibleImplementor;
		String flagsString = "1";
		String packageAndClass = params.get(0);
		
		switch(kind){
			case CHANGED:
				sourceCompatibleCaller = true;
				sourceCompatibleImplementor = true;
				flagsString = flagsString+"c";
				break;
			default: throw new RuntimeException("strange1");
		}
		int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCompatibleCaller, sourceCompatibleImplementor);
		Data result = new Data("messAPIComp1","abstract "+kind+" (deleted?) on "+elemType.bar()+" "+params.get(0)
				+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage, prio,flagsString,"Class",typeName,null, 
				1,"abstract removed from a class. ",Modifier.toString(modifiers),null,kind,elemType,params.get(0),packageAndClass);
	  	return result;
	}

	private Data handle02(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage,int newModifier) {
		boolean compatibleCaller;
		boolean isCompatibleForImplementer;
		String packageAndClass = params.get(0);
		String flagsString = "02";// TODO: Not tested, no testcases.
		String chapter = "Method";
	
		switch(kind){
			case ADDED: compatibleCaller = true;
			    isCompatibleForImplementer = true;
				flagsString = flagsString+"a";
				chapter = "Added"+chapter;
				if(newModifier!=0){
				modifiers=newModifier;
				}
				break;
			case REMOVED:
				compatibleCaller = classif.callForbidden(packageAndClass);
			    isCompatibleForImplementer = true;
				chapter = "Deleted"+chapter;
				break;
			default: throw new RuntimeException("strange02");
		}
		int prio = priorities.getPriority(modifiers, binaryCompatible, compatibleCaller, isCompatibleForImplementer);
		Data result = new Data("messAPIComp2","Method "+kind+" "+params.get(1)+" with modifiers "
				+Modifier.toString(modifiers)+eclipseMessage,prio, flagsString, chapter, typeName,params.get(1), 02,
				"Default value of Annotation",
				Modifier.toString(modifiers),returnTypeIfNeeded,kind,elemType,params.get(1),packageAndClass);
	  	return result;
	}

	private Data handle04(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage,int newModifier) {
		boolean sourceCompatibleCaller;
		boolean sourceCompatibleImplementor;
		String flagsString = "04";
		String packageAndClass = params.get(0);
		
		switch(kind){
			case CHANGED:
				sourceCompatibleCaller = classif.callForbidden(packageAndClass); // TODO: Test with "declare" and with "catch"
				sourceCompatibleImplementor = classif.inheritanceForbidden(packageAndClass);
				flagsString = flagsString+"c";
				break;
			default: throw new RuntimeException("strange04");
		}
		String param1 = params.get(1);
		int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCompatibleCaller, sourceCompatibleImplementor);
		Data result = new Data("messAPIComp4","Changed array of objects to varargs "+param1+" "+kind+" on "+elemType.bar()+" "+params.get(1)
				+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage, prio,flagsString,"",typeName,params.get(1), 
				4,"Changed array of object to varargs ",Modifier.toString(modifiers),null,kind,elemType,param1,packageAndClass);
	  	return result;
	}


	private Data handle05(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage,int newModifier) {
		boolean sourceCompatibleCaller;
		boolean sourceCompatibleImplementor;
		String flagsString = "5";
		String packageAndClass = params.get(0);
		switch(kind){
			case ADDED:
				sourceCompatibleCaller = classif.callForbidden(packageAndClass);
				sourceCompatibleImplementor = classif.inheritanceForbidden(packageAndClass); // TODO: Test
				flagsString = flagsString+"a";
				if(newModifier!=0){
					modifiers=newModifier;
					}
				
				break;
			case REMOVED:
				sourceCompatibleCaller = classif.callForbidden(packageAndClass); // TODO: Test with "declare" and with "catch"
				sourceCompatibleImplementor = classif.inheritanceForbidden(packageAndClass);
				flagsString = flagsString+"d";
				break;
			default: throw new RuntimeException("strange5");
		}
		int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCompatibleCaller, sourceCompatibleImplementor);
		Data result = new Data("messAPIComp5","Exception "+params.get(2)+" "+kind+" on "+elemType.bar()+" "+params.get(1)
				+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage, prio,flagsString,"ThrowableObject",typeName,params.get(1), 
				5,"Exception added to a method or removed from it",Modifier.toString(modifiers),null,kind,elemType,params.get(2),packageAndClass);
	  	return result;
	}

	private Data handle06(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage,int newModifier) {
		boolean sourceCaller;
		// TODO: No testcases, not verified.
		boolean sourceImplement;
		String packageAndClass = params.get(0);
		switch(kind){
			case ADDED: 
				sourceCaller = true; 
				sourceImplement = true; 
				if(newModifier!=0){
					modifiers=newModifier;
					}
				break;
			case CHANGED:
				sourceCaller = classif.callForbidden(packageAndClass);
				sourceImplement = true; 
				break;
			case REMOVED:
				sourceCaller = classif.callForbidden(packageAndClass);
				sourceImplement = true; 
				break;
			default: throw new RuntimeException("strange6");
		}
		int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCaller, sourceImplement);
		Data result = new Data("messAPIComp6","Generics class "+kind+" bound "+Modifier.toString(modifiers)+eclipseMessage,
				prio, Integer.toString(6), "", typeName, params.get(1), 6, // TODO: No testcase, no documentation in wiki, no anchor in wiki.
				"Generic class bound "+kind,Modifier.toString(modifiers),null,kind,elemType,params.get(1),packageAndClass);
	  	return result;
	}

	private Data handle07(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage,int newModifier) {
		boolean sourceCaller;
		boolean sourceImplement;
		String packageAndClass = params.get(0);
		switch(kind){
			case ADDED: 
				sourceCaller = true; 
				sourceImplement = true; 
				if(newModifier!=0){
					modifiers=newModifier;
					}
				break;
			case CHANGED:
				sourceCaller = classif.callForbidden(packageAndClass);
				sourceImplement = true; 
				break;
			case REMOVED:
				sourceCaller = classif.callForbidden(packageAndClass);
				sourceImplement = true; 
				break;
			default: throw new RuntimeException("strange7");
		}
		int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCaller, sourceImplement);
		Data result = new Data("messAPIComp7","static initializer "+kind+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage,
				prio, Integer.toString(7), "", typeName, "static_init", 7, // TODO: No testcase, no documentation in wiki, no anchor in wiki.
				"static intializer "+kind+" (e.x. change static array of constants to enum",Modifier.toString(modifiers),null,kind,elemType,"static initializer",packageAndClass);
				//TODO: elementType is set to "static initializer" because the name of it can't be extracted from eclipse tool...maybe using configuration file?!  
	  	return result;
	}

	private Data handle08(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage,int newModifier) {
		boolean sourceCompatibleCaller;
		boolean sourceCompatibleInherit;
		String packageAndClass = params.get(0);
		switch(kind){
			case ADDED:
				sourceCompatibleCaller = true; 
				// TODO: adding a first constructor that is not the default constructor is incompatible for inheritance.
				sourceCompatibleInherit = true; 
				if(newModifier!=0){
					modifiers=newModifier;
					}
				break;
			case REMOVED:
				sourceCompatibleCaller = classif.callForbidden(packageAndClass);
				sourceCompatibleInherit = classif.inheritanceForbidden(packageAndClass);
				break;
			default: throw new RuntimeException("strange8");
		}
		int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCompatibleCaller, sourceCompatibleInherit);
		Data result = new Data("messAPIComp8","Constructor "+kind+" "+params.get(1)+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage,prio, 
				"8", "Constructor", typeName,params.get(1), 8,"Constructer "+kind.toString(),
				Modifier.toString(modifiers),null,kind,elemType,params.get(1),packageAndClass);
				//TODO: is it constructor removed/added, or that class was changed from static to no-static or vice versa?! which one? 
	  	return result;
	}

	private Data handle09(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage,int newModifier) {
		String packageAndClass = params.get(0);
		switch(kind){
			case CHANGED:
				int prio = priorities.getPriority(modifiers, binaryCompatible, binaryCompatible, binaryCompatible);
				Data result = new Data("messAPIComp9","Some change that is not documented by eclipse. Please tell us what you changed "
						+kind+" "+params.get(0)+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage,
						prio, "9", "", typeName, null,9, // TODO: No testcase, no wiki entry, no anchor.
						"Some change that is not documented by eclipse. Please tell us what you changed (e.x. client code refering to a removed class from a set of superclasses)",
						Modifier.toString(modifiers),null,kind,elemType,params.get(0),packageAndClass);
						//TODO: The name of the removed super class is not mentioned by the eclipse api tool. We need to find a way to get it.
			  	return result;
			default: throw new RuntimeException("strange9");
		}
	}

	private Data handle10(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage,int newModifier) {
		switch(kind){
			case CHANGED:
				if(newModifier!=0){
					modifiers=newModifier;
					}
				String packageAndClass = params.get(0);
				boolean sourceInheritRequired = true; 
				boolean sourceCall = classif.callForbidden(packageAndClass); 
				int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInheritRequired);
				Data result = new Data("messAPIComp10","Removed some super interface of "+elemType.bar()+" "+ packageAndClass
						+" with modifier "+Modifier.toString(modifiers)+eclipseMessage,
						prio, "10", "Class", typeName, null, 10, // TODO: No testcase, no wiki entry, no anchor.
						"an interface was removed from the set of super interfaces, which breaks client code refering to the removed interface",
						Modifier.toString(modifiers),null,kind,elemType,packageAndClass,packageAndClass);
						//TODO: The name of the removed super interface is not mentioned by the eclipse api tool. We need to find a way to get it.
			  	return result;
			default: throw new RuntimeException("strange10");
		}
	}

	private Data handle11(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) { // TODO: depending on element type, different anchors and sections should be used
		String packageAndClass = params.get(0);
		switch(kind){
			case CHANGED:
				boolean sourceCall = classif.callForbidden(packageAndClass);
				int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, true);
				Data result = new Data("messAPIComp11","Decreasing visibility of "+elemType.bar()+" "+params.get(1) 
						+" of class "+packageAndClass+" changed to "+Modifier.toString(modifiers)+eclipseMessage, // TODO: No testcase, no wiki anchor.
						prio, "11", "", typeName, params.get(1), 11,"Decreasing visibility of an element type (its modifier is changed)",
						Modifier.toString(modifiers),returnTypeIfNeeded,kind,elemType,params.get(1),packageAndClass);
			  	return result;
			default: throw new RuntimeException("strange11");
		}
	}

	private Data handle12(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		int prio;
		Data result;
		String packageAndClass = params.get(0);
		
		switch(kind){
			case ADDED:
				if(newModifier!=0){
					modifiers=newModifier;
					}
				prio = priorities.getPriority(modifiers, binaryCompatible, true, true);
				result = new Data("messAPIComp12a","Enum element added "+params.get(1)+" to enum "+params.get(0)
						+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage,
						prio, "12", "Enum", typeName,params.get(1), 12,"Enum element added to an enum",
						Modifier.toString(modifiers),null,kind,elemType,params.get(0)+"."+params.get(1),packageAndClass);
						//TODO: In the elemName parameter, I included the enum name to the enum element name. Do we need the enum name? 
			  	return result;				
			case CHANGED:
			case REMOVED:
				boolean sourceCall = classif.callForbidden(packageAndClass);
				boolean sourceInherit = classif.inheritanceForbidden(packageAndClass);
				prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
				result = new Data("messAPIComp12b","Enum element "+kind+" "+params.get(1)+" to enum "+packageAndClass
						+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage,
						prio, "12", "Enum", typeName,params.get(1), 12,"Enum element removed from an enum",
						Modifier.toString(modifiers),null,kind,elemType,params.get(0)+"."+params.get(1),packageAndClass);
						//TODO: In the elemName parameter, I included the enum name to the enum element name. Do we need the enum name?
			  	return result;
			default: throw new RuntimeException("strange12");
		}
	}

	private Data handle14(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String packageAndClass = params.get(0);
		switch(kind){
			case CHANGED:
				int prio = priorities.getPriority(modifiers, binaryCompatible, binaryCompatible, binaryCompatible);
				Data result = new Data("messAPIComp14","Some change that is not documented by eclipse (14). Please tell us what you changed "
						+kind+" "+params.get(0)+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage, // TODO: No testcase, no wiki anchor / chapter.
						prio, "14", "", typeName,null, 14,"Some change that is not documented by eclipse (14). Please tell us what you changed",
						Modifier.toString(modifiers),null,kind,elemType,params.get(0),packageAndClass);
			  	return result;
			default: throw new RuntimeException("strange14");
		}
	}

	private Data handle15(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String packageAndClass = params.get(0);
		switch(kind){
			case CHANGED:
				int prio = priorities.getPriority(modifiers, binaryCompatible, true, true);
				Data result = new Data("messAPIComp15",""+kind+" Added a new super interface to class/interface "
						+params.get(0)+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage,
						prio, "15", "Class", typeName,null, 15,"a new super interface was added to the class/interface",
						Modifier.toString(modifiers),null,kind,elemType,params.get(0),packageAndClass);
			  	return result;
			default: throw new RuntimeException("strange15");
		}
	}

	private Data handle16(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String flagsString = "16";
		String packageAndClass = params.get(0);
		boolean sourceCall;
		boolean sourceInherit;
		switch(kind){
			case ADDED:
				sourceCall = true;
				sourceInherit = true;
				flagsString = flagsString+"a";
				if(newModifier!=0){
					modifiers=newModifier;
					}
				break;
			case REMOVED:
				sourceCall = classif.callForbidden(packageAndClass);
				sourceInherit = classif.inheritanceForbidden(packageAndClass);
				flagsString = flagsString+"d";
				break;
			default: throw new RuntimeException("strange16");
		}
		int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
		Data result = new Data("messAPIComp16","Field "+kind+" "+params.get(1)+" with modifiers "
				+Modifier.toString(modifiers)+eclipseMessage,prio, flagsString, "Field", typeName,params.get(1), 16,"Field was added to or removed from a class/interface",
				Modifier.toString(modifiers),null,kind,elemType,params.get(1),packageAndClass);
	  	return result;
	}

	private Data handle17(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String flagsString = "17";
String packageAndClass = params.get(0);
		boolean sourceCall;
		boolean sourceInherit;
		switch(kind){
			case ADDED:
				sourceCall = true;
				sourceInherit = true;
				flagsString = flagsString+"a";
				if(newModifier!=0){
					modifiers=newModifier;
					}
				break;
			case REMOVED:
				sourceCall = true;
				sourceInherit = true;
				flagsString = flagsString+"d";
				break;
			default: throw new RuntimeException("strange17");
		}
		int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
		Data result = new Data("messAPIComp17","Field "+kind+" "+params.get(1)+" with modifiers "
				+Modifier.toString(modifiers)+eclipseMessage,prio, flagsString, "Field", typeName,params.get(1), 17,"Field was moved up the class hierarchy",
				Modifier.toString(modifiers),null,kind,elemType,params.get(1),packageAndClass);
	  	return result;
	}

	private Data handle18(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) { // TODO: No testcase, no wiki anchor
		// TODO: depending on element type, different chapters in wiki and different anchors are needed.
		// TODO: Message text not reviewed. What is "params.get(0)"?
		String params0 = params.get(0);
		String packageAndClass = params.get(0);
		switch(kind){
			case CHANGED:
				int prio = priorities.getPriority(modifiers, binaryCompatible, true, true);
				Data result = new Data("messAPIComp18","removed 'final' modifier from "+params0
						+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage,
						prio, "18", "", typeName,params0, 18,"the 'final' modifier was removed from an element",
						Modifier.toString(modifiers),null,kind,elemType,params0,packageAndClass);
			  	return result;
			default: throw new RuntimeException("strange18");
		}
	}

	private Data handle19(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) { // TODO: No testcase, no wiki anchor
		// TODO: depending on element type, different chapters in wiki and different anchors are needed.
		String packageAndClass = params.get(0);
		switch(kind){
			case CHANGED:
				int prio = priorities.getPriority(modifiers, binaryCompatible, true, true);
				Data result = new Data("messAPIComp19","removed 'final' modifier from "+params.get(1)
						+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage,
						prio, "19", "", typeName,params.get(1), 19,"the 'final' modifier was removed from an static element",
						Modifier.toString(modifiers),null,kind,elemType,params.get(1),packageAndClass);
			  	return result;
			default: throw new RuntimeException("strange19");
		}
	}

	private Data handle20(boolean binaryCompatible, ChangeKind kind, int modifiers,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String packageAndClass = params.get(0);
		switch(kind){
			case CHANGED:
				int prio = priorities.getPriority(modifiers, binaryCompatible, true, true);
				Data result = new Data("messAPIComp20","removed 'final' modifier from "+params.get(1)
						+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage,
						prio, "20","Field", typeName,params.get(1), 20,"the 'final' modifier was removed from a constant field",
						Modifier.toString(modifiers),null,kind,elemType,params.get(1),packageAndClass);
			  	return result;
			default: throw new RuntimeException("strange20");
		}
	}

	private Data handle21(boolean binaryCompatible, ChangeKind kind, int modifiers,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String packageAndClass = params.get(0);
		switch(kind){
			case CHANGED:
				int prio = priorities.getPriority(modifiers, binaryCompatible, true, true);
				Data result = new Data("messAPIComp21","removed 'final' modifier from "+params.get(1)
						+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage,
						prio, "21","Field", typeName,params.get(1), 21,"the 'final' modifier was removed from a non-constant field",
						Modifier.toString(modifiers),null,kind,elemType,params.get(1),packageAndClass);
			  	return result;
			default: throw new RuntimeException("strange21");
		}
	}

	// DONE: If the element is "static", an increase is not critical, as it will not be redefined.
	private Data handle22(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String packageAndClass = params.get(0);
		boolean sourceInherit;
		boolean isStatic = Modifier.isStatic(modifiers);
		String params1 = "unknown";
		if(params.size()>1){
			params1 = params.get(1);
		}
		switch(kind){
			case CHANGED:
				sourceInherit = classif.inheritanceForbidden(packageAndClass);
				int prio = priorities.getPriority(modifiers, binaryCompatible, true, sourceInherit);
				if(isStatic){
					prio = 4;
				}
				Data result = new Data("messAPIComp22","increased accessibility for "+params1
						+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage,
						prio, "22","", typeName,params1, 22,"accessibility for a member (class, method, attribute...) was increased",
						Modifier.toString(modifiers),returnTypeIfNeeded,kind,elemType,params1,packageAndClass);
			  	return result;
			default: throw new RuntimeException("strange22");
		}
	}

	private Data handle24(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String packageAndClass = params.get(0);
		// TODO: No Testcase, not implemented.
		switch(kind){
			case REMOVED:
				int prio = priorities.getPriority(modifiers, binaryCompatible, binaryCompatible, binaryCompatible);
				Data result = new Data("messAPIComp24","Some change that is not documented by eclipse. Please tell us what you changed "
						+kind+" "+params.get(0)+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage,
						prio, "24", "", typeName, null,24, // TODO: No testcase, no wiki entry, no anchor.
						"Some change that is not documented by eclipse. Please tell us what you changed ",
						Modifier.toString(modifiers),null,kind,elemType,params.get(0),packageAndClass);
			  	return result;
			default: throw new RuntimeException("strange9");
		}
	}

	private Data handle25(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		boolean compatibleCaller;
		boolean isCompatibleForImplementer;
		boolean isAbstractMethod = Modifier.isAbstract(modifiers);
		String packageAndClass = params.get(0);
		String flagsString = "25";
		String chapter = "Method";
		switch(kind){
			case ADDED: compatibleCaller = true;
			    isCompatibleForImplementer = true;
				flagsString = flagsString+"a";
				chapter = "Added"+chapter;
			    if (isAbstractMethod){
			    	isCompatibleForImplementer = classif.inheritanceForbidden(packageAndClass);
			    }
			    if(newModifier!=0){
					modifiers=newModifier;
					}
				break;
			case REMOVED:
				compatibleCaller = classif.callForbidden(packageAndClass);
			    isCompatibleForImplementer = true;
				chapter = "Deleted"+chapter;
			    if (isAbstractMethod){
					flagsString = flagsString+"d1";
			    }
			    else{
					flagsString = flagsString+"d2";
			    }
				break;
			default: throw new RuntimeException("strange25");
		}
		int prio = priorities.getPriority(modifiers, binaryCompatible, compatibleCaller, isCompatibleForImplementer);
		Data result = new Data("messAPIComp25","Method "+kind+" "+params.get(1)+" with modifiers "
				+Modifier.toString(modifiers)+eclipseMessage,prio, flagsString, chapter, typeName,params.get(1), 25,
				"Method added or deleted, or that the method signature has changed (e.x. parameter was added/removed, or its type was changed)",
				Modifier.toString(modifiers),returnTypeIfNeeded,kind,elemType,params.get(1),packageAndClass);
				//TODO: This flag is not only for added/removed method: it is also for cases where the method signature is changed. 
				//E.x a parameter was added/removed, or its type was changed, or even when the return type of a method was changed (void to int...)
				//What to do with those cases? make an if statement? or let them for the client to do?
				//If a method signature is changed, 2 "flag=25" changes will be generated: one for removing with the element name = the old signature, 
				//and one for adding with the element name = the new signature. 
	  	return result;
	}

	private Data handle26(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		boolean compatibleCaller;
		boolean isCompatibleForImplementer;
		boolean isAbstractMethod = Modifier.isAbstract(modifiers);
		String packageAndClass = params.get(0);
		String flagsString = "26";// TODO: Not tested, no testcases.
		String chapter = "Method";
		switch(kind){
			case ADDED: compatibleCaller = true;
			    isCompatibleForImplementer = true;
				flagsString = flagsString+"a";
				chapter = "Added"+chapter;
			    if (isAbstractMethod){
			    	isCompatibleForImplementer = classif.inheritanceForbidden(packageAndClass);
			    }
			    if(newModifier!=0){
					modifiers=newModifier;
					}
				break;
			case REMOVED:
				compatibleCaller = classif.callForbidden(packageAndClass);
			    isCompatibleForImplementer = true;
				chapter = "Deleted"+chapter;
			    if (isAbstractMethod){
					flagsString = flagsString+"d1";
			    }
			    else{
					flagsString = flagsString+"d2";
			    }
				break;
			default: throw new RuntimeException("strange26");
		}
		int prio = priorities.getPriority(modifiers, binaryCompatible, compatibleCaller, isCompatibleForImplementer);
		Data result = new Data("messAPIComp26","Method "+kind+" "+params.get(1)+" with modifiers "
				+Modifier.toString(modifiers)+eclipseMessage,prio, flagsString, chapter, typeName,params.get(1), 26,
				"Method moved up in class hierarchy",
				Modifier.toString(modifiers),returnTypeIfNeeded,kind,elemType,params.get(1),packageAndClass);
	  	return result;
	}

	/* with default value */
	private Data handle27(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String packageAndClass = params.get(0);
		switch(kind){
			case ADDED:
				if(newModifier!=0){
					modifiers=newModifier;
					}
				break;
			case REMOVED: break;
			default: throw new RuntimeException("strange27");
		}
		int prio = priorities.getPriority(modifiers, binaryCompatible, true, true);
		Data result = new Data("messAPIComp27","Element "+params.get(1)+" "+kind+" at Annotation type "+eclipseMessage,
				prio, "27", "", typeName,params.get(1), 27, // TODO: Depending on element type, different chapters need to be referenced in wiki.
				"an element (e.x. method) was added or removed at Annotation type with a default value",
				Modifier.toString(modifiers),returnTypeIfNeeded,kind,elemType,params.get(1),packageAndClass);
	  	return result;
	}

	/* without default value */
	private Data handle28(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String packageAndClass = params.get(0);
		switch(kind){
			case ADDED: 
				if(newModifier!=0){
					modifiers=newModifier;
					}
				break;
			case REMOVED: break;
			default: throw new RuntimeException("strange28");
		}
		int prio = priorities.getPriority(modifiers, binaryCompatible, true, true);
		Data result = new Data("messAPIComp28","Element "+params.get(1)+" "+kind+" at Annotation type "+eclipseMessage,
				prio, "28", "", typeName,params.get(1), 28,// TODO: Depending on element type, different chapters need to be referenced in wiki.
				"an element (e.x. method) was added or removed at Annotation type without a default value",
				Modifier.toString(modifiers),returnTypeIfNeeded,kind,elemType,params.get(1),packageAndClass);
		return result;
	}

	/**
	 * adding abstract to a class breaks callers that call a constructor. Implementors of the class are not affected.
	 * Adding abstract to a method is compatible for callers, but breaks implementors.
	 * @param binaryCompatible
	 * @param flags
	 * @param kind
	 * @param modifiers
	 * @param params
	 * @param elemType
	 * @return
	 */
	private Data handle30(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		boolean callerCompatible;
		boolean implementorCompatible;
		String packageAndClass = params.get(0);
		String method = null;
		String elementName = null; //attribute defined for assigning the elemName for if the elemType was class or method.
		switch(elemType){
		case CLASS_ELEMENT_TYPE:
			callerCompatible = false;
			implementorCompatible = classif.inheritanceForbidden(packageAndClass);
			elementName = packageAndClass; 
			break;
		case ENUM_ELEMENT_TYPE:
			callerCompatible = false;
			implementorCompatible = classif.inheritanceForbidden(packageAndClass);
			elementName = packageAndClass; 
			break;
			case METHOD_ELEMENT_TYPE:
				callerCompatible = true;
				implementorCompatible = classif.inheritanceForbidden(packageAndClass);
				method = params.get(1);
				elementName = method;
				break;
			default:
				RuntimeException ex = new RuntimeException("30: unhandled element type "+elemType.bar());
				ex.printStackTrace();
				callerCompatible = false;
				implementorCompatible = false;
		}
		switch(kind){
			case CHANGED:
				int prio = priorities.getPriority(modifiers, binaryCompatible, callerCompatible, implementorCompatible);
				Data result = new Data("messAPIComp30","'abstract' added to "+elemType.bar()+" "+params.get(0)
						+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage,
						prio, "30", "Class", typeName,method, 30,"'abstract' added to "+elemType.bar(),// TODO: Depending on element type, different chapters need to be referenced in wiki.
						Modifier.toString(modifiers),returnTypeIfNeeded,kind,elemType,elementName,packageAndClass);
			  	return result;
			default: throw new RuntimeException("strange30");
		}
	}

	private Data handle31(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String packageAndClass = params.get(0);
		boolean sourceInherit;
		String element = null;
		String element1 = null;
		switch(elemType){
		case FIELD_ELEMENT_TYPE: element = params.get(1);
			element1 = "."+element;
		break;
		case METHOD_ELEMENT_TYPE: element = params.get(1);
			element1 = "."+element;
		break;
		case CLASS_ELEMENT_TYPE: // nothing to do.
			element = params.get(0); //
			element1 = "";
		break;
		default:
			throw new RuntimeException("something strange 31");
		}
		switch(kind){
			case CHANGED:
				sourceInherit = classif.inheritanceForbidden(packageAndClass);
				int prio = priorities.getPriority(modifiers, binaryCompatible, true, sourceInherit);
				Data result = new Data("messAPIComp31","'final' added to "+elemType.bar()+" "+packageAndClass+element1
						+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage,
						prio, "31", "", typeName,element, 31,"'final' added to "+elemType.bar(),// TODO: Depending on element type, different chapters need to be referenced in wiki.
						Modifier.toString(modifiers),returnTypeIfNeeded,kind,elemType,element,packageAndClass);
			  	return result;
			default: throw new RuntimeException("strange31");
		}
	}

	private Data handle33(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String element = null;
		String element1 = null;
		String packageAndClass = params.get(0);
		switch(elemType){
		case FIELD_ELEMENT_TYPE: element = params.get(1);
			element1 = "."+element;
		break;
		case METHOD_ELEMENT_TYPE: element = params.get(1);
			element1 = "."+element;
		break;
		case CLASS_ELEMENT_TYPE: // nothing to do.
			element = params.get(0); 
			element1 = "";
		break;
		default:
			throw new RuntimeException("something strange 33");
		}
		switch(kind){
			case CHANGED:
				int prio = priorities.getPriority(modifiers, binaryCompatible, true, true);
				String tmp = params.get(0);
				Data result = new Data("messAPIComp33","'static' added to "+elemType.bar()+" "+tmp+element1
						+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage,// TODO: Depending on element type, different chapters need to be referenced in wiki.
						prio, "33","ChangedMethod", typeName,element, 33,"'static' added to "+elemType.bar(),
						Modifier.toString(modifiers),returnTypeIfNeeded,kind,elemType,element,packageAndClass);
			  	return result;
			default: throw new RuntimeException("strange33");
		}
	}

	// non synchronised to synchronized 
	private Data handle34(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String element = null;
		String element1 = null;
		String packageAndClass = params.get(0);
		switch(elemType){
		case FIELD_ELEMENT_TYPE: element = params.get(1);
			element1 = "."+element;
		break;
		case METHOD_ELEMENT_TYPE: element = params.get(1);
			element1 = "."+element;
		break;
		case CLASS_ELEMENT_TYPE: // nothing to do.
			element = params.get(0); 
			element1 = "";
		break;
		default:
			throw new RuntimeException("something strange 34");
		}
		switch(kind){
			case CHANGED:
				int prio = priorities.getPriority(modifiers, binaryCompatible, true, true);
				String tmp = params.get(0);
				Data result = new Data("messAPIComp34","'synchronized' added to "+elemType.bar()+" "+tmp+element1
						+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage,// TODO: Depending on element type, different chapters need to be referenced in wiki.
						prio, "34","", typeName,element, 34,"'synchronized' added to "+elemType.bar(),
						Modifier.toString(modifiers),returnTypeIfNeeded,kind,elemType,element,packageAndClass);
			  	return result;
			default: throw new RuntimeException("strange34");
		}
	}

	// non transient to transient 
	private Data handle35(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String element = null;
		String element1 = null;
		String packageAndClass = params.get(0);
		switch(elemType){
		case FIELD_ELEMENT_TYPE: element = params.get(1);
			element1 = "."+element;
		break;
		case METHOD_ELEMENT_TYPE: element = params.get(1);
			element1 = "."+element;
		break;
		case CLASS_ELEMENT_TYPE: // nothing to do.
			element = params.get(0); 
			element1 = "";
		break;
		default:
			throw new RuntimeException("something strange 35");
		}
		switch(kind){
			case CHANGED:
				int prio = priorities.getPriority(modifiers, binaryCompatible, true, true);
				String tmp = params.get(0);
				Data result = new Data("messAPIComp35","'transient' added to "+elemType.bar()+" "+tmp+element1
						+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage,// TODO: Depending on element type, different chapters need to be referenced in wiki.
						prio, "35","", typeName,element, 35,"'transient' added to "+elemType.bar(),
						Modifier.toString(modifiers),returnTypeIfNeeded,kind,elemType,element,packageAndClass);
			  	return result;
			default: throw new RuntimeException("strange35");
		}
	}

	// Adding a method that is overriding a exiting definition from a super class. Source compatible. 
	private Data handle36(ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String elementName = null;
		String packageAndClass = params.get(0);
		switch(elemType){
//		case FIELD_ELEMENT_TYPE: 
//		elementName = params.get(1); 
//		break;
		case METHOD_ELEMENT_TYPE: 
		elementName = params.get(1);
		break;
		case CLASS_ELEMENT_TYPE: 
			elementName = params.get(0); 
			break;
		case INTERFACE_ELEMENT_TYPE: 
			elementName = params.get(0); 
			break;
		default:
			throw new RuntimeException("something strange 36"+elemType.bar());
		}
		// No chapter needed, as no message and no link will be generated.
		Data result = new Data("messAPIComp36","added re-definition of existing method "+eclipseMessage,4,"36","", typeName,elementName,36,"added re-definition of existing method",Modifier.toString(modifiers),returnTypeIfNeeded,kind,elemType,elementName,packageAndClass);
		return result;
	}
	
	// added taglet for API restriction. No Reporting planned so far? 
	private Data handle37(ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String packageAndClass = params.get(0);
		String elementName = null;
		switch(elemType){
		case FIELD_ELEMENT_TYPE: 
		elementName = params.get(1); 
		break;
		case METHOD_ELEMENT_TYPE: 
		elementName = params.get(1);
		break;
		case CLASS_ELEMENT_TYPE: 
			elementName = params.get(0); 
			break;
		case INTERFACE_ELEMENT_TYPE: 
			elementName = params.get(0); 
			break;
		default:
			throw new RuntimeException("something strange 37"+elemType.bar());
		}
		// No chapter needed, as no message and no link will be generated.
		Data result = new Data("messAPIComp37","changed API restriction by changing a taglet. "+eclipseMessage,4,"37","", typeName,elementName,37,"",Modifier.toString(modifiers),returnTypeIfNeeded,kind,elemType,elementName,packageAndClass);
		return result;
	}
	
	private Data handle38(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String element = "";
		String elementName = null;
		switch(elemType){
		case FIELD_ELEMENT_TYPE: element = " from "+elemType.bar()+" "+params.get(1);
		elementName = params.get(1); //
		break;
		case METHOD_ELEMENT_TYPE: element = " from "+elemType.bar()+" "+params.get(1);
		elementName = params.get(1);
		break;
		case CLASS_ELEMENT_TYPE: element = " from "+elemType.bar();
		elementName = params.get(0); //
		break;
		default:
			throw new RuntimeException("something strange 38");
		}
		String packageAndClass = params.get(0);
		boolean sourceCall;
		boolean sourceInherit;
		switch(kind){
			case CHANGED:
				sourceCall = classif.callForbidden(packageAndClass);
				sourceInherit = classif.inheritanceForbidden(packageAndClass);
				int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
				Data result = new Data("messAPIComp38","Removed 'static' modifier "+element +" of class "
						+packageAndClass+" with modifier "+Modifier.toString(modifiers)+eclipseMessage,// TODO: Depending on element type, different chapters need to be referenced in wiki.
						prio, "38","", typeName, elementName, 38,"Removed 'static' modifier from"+elemType.bar(),
						Modifier.toString(modifiers),returnTypeIfNeeded,kind,elemType,elementName,packageAndClass);
			  	return result;
			default: throw new RuntimeException("strange38");
		}
	}

	private Data handle39(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String packageAndClass = params.get(0);
		boolean sourceCall;
		boolean sourceInherit;
		switch(kind){
			case ADDED:
			case REMOVED:
			case CHANGED:
				sourceCall = classif.callForbidden(packageAndClass);
				sourceInherit = classif.inheritanceForbidden(packageAndClass);
				int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);// TODO: remove is incompatible, add is compatible!
				Data result = new Data("messAPIComp39","Added or removed some super class of "+elemType.bar()+" "
						+packageAndClass +" with modifier "+Modifier.toString(modifiers)+eclipseMessage,
						prio, "39", "Class", typeName, null, 39,"some super class was added or removed. Please check which super class was added/removed",
						Modifier.toString(modifiers),null,kind,elemType,packageAndClass,packageAndClass);
						//TODO: In the eclipse documentation it says that change kind is "added" or "removed", but in the eclipse tool output it is only of kind "changed"!
			  	return result;
			default: throw new RuntimeException("strange39");
		}
	}

	private Data handle40(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String element = "";
		String packageAndClass = params.get(0);
		boolean sourceCall;
		boolean sourceInherit;
		switch(elemType){
			case FIELD_ELEMENT_TYPE: element = "from "+elemType.bar()+" "+params.get(1);
			break;
			case METHOD_ELEMENT_TYPE: element = " from "+elemType.bar()+" "+params.get(1);
			break;
			default:
				throw new RuntimeException("something strange 401");
		}
		switch(kind){
			case CHANGED:
				sourceCall = classif.callForbidden(packageAndClass);
				sourceInherit = classif.inheritanceForbidden(packageAndClass);
				int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
				Data result = new Data("messAPIComp40","Removed 'synchronized' modifier "+element +" of class "
						+packageAndClass+" with modifier "+Modifier.toString(modifiers)+eclipseMessage,
						prio, "40","Class", typeName, null, 40,"'synchronized' modifier removed from a "+elemType.bar(),
						Modifier.toString(modifiers),returnTypeIfNeeded,kind,elemType,params.get(1),packageAndClass);
			  	return result;
			default: throw new RuntimeException("strange40");
		}
	}

	private Data handle41(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String packageAndClass = params.get(0);
		boolean sourceCall;
		boolean sourceInherit;
		switch(kind){
			case CHANGED:
				sourceCall = classif.callForbidden(packageAndClass);
				sourceInherit = classif.inheritanceForbidden(packageAndClass);
				int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
				Data result = new Data("messAPIComp41","Converted Interface into Annotation in interface "
						+packageAndClass+" with modifier "+Modifier.toString(modifiers)+eclipseMessage,
						prio, "41", "Class", typeName, null, 41,
						"a type was converted to a different kind (e.x. interface converted into Annotation in interface)",
						Modifier.toString(modifiers),null,kind,elemType,packageAndClass,packageAndClass);
						//TODO: How to know which type was converted to which type? Eclipse tool doesn't show this!
			  	return result;
			default: throw new RuntimeException("strange41");
		}
	}

	private Data handle42(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String packageAndClass = params.get(0);
		switch(kind){
			case CHANGED:
				int prio = priorities.getPriority(modifiers, binaryCompatible, binaryCompatible, binaryCompatible);
				Data result = new Data("messAPIComp42","Some change that is not documented by eclipse. Please tell us what you changed "
						+kind+" "+params.get(0)+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage,
						prio, "42", "", typeName, null,42, // TODO: No testcase, no wiki entry, no anchor.
						"Some change that is not documented by eclipse. Please tell us what you changed ",
						Modifier.toString(modifiers),null,kind,elemType,params.get(0),packageAndClass);
			  	return result;
			default: throw new RuntimeException("strange42");
		}
	}

	private Data handle43(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String packageAndClass = params.get(0);
		switch(kind){
			case CHANGED:
				int prio = priorities.getPriority(modifiers, binaryCompatible, binaryCompatible, binaryCompatible);
				Data result = new Data("messAPIComp43","Some change that is not documented by eclipse. Please tell us what you changed "
						+kind+" "+params.get(0)+" with modifiers "+Modifier.toString(modifiers)+"[eclipseMessage :"+eclipseMessage+"]",
						prio, "43", "", typeName, null,43, // TODO: No testcase, no wiki entry, no anchor.
						"Some change that is not documented by eclipse. Please tell us what you changed ",
						Modifier.toString(modifiers),null,kind,elemType,params.get(0),packageAndClass);
			  	return result;
			default: throw new RuntimeException("strange43");
		}
	}

	private Data handle44(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String packageAndClass = params.get(0);
		boolean sourceInherit;
		switch(kind){
			case CHANGED:
				sourceInherit = classif.inheritanceForbidden(packageAndClass);
				int prio = priorities.getPriority(modifiers, binaryCompatible, true, sourceInherit);
				Data result = new Data("messAPIComp44","Converted Class into Interface "+packageAndClass
						+" with modifier "+Modifier.toString(modifiers)+eclipseMessage,
						prio, "44", "Class", typeName, null, 44,"Class was converted into Interface",
						Modifier.toString(modifiers),null,kind,elemType,packageAndClass,packageAndClass);
			  	return result;
			default: throw new RuntimeException("strange44");
		}
	}

	private Data handle45(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String element = "";
		String packageAndClass = params.get(0);
		boolean sourceCall;
		boolean sourceInherit;
		switch(elemType){
			case FIELD_ELEMENT_TYPE: element = "from "+elemType.bar()+" "+params.get(1);
			break;
			case METHOD_ELEMENT_TYPE: element = " from "+elemType.bar()+" "+params.get(1);
			break;
			default:
				throw new RuntimeException("something strange 45");
		}
		switch(kind){
			case CHANGED:
				sourceCall = classif.callForbidden(packageAndClass);
				sourceInherit = classif.inheritanceForbidden(packageAndClass);
				int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
				Data result = new Data("messAPIComp45","Removed 'transient' modifier "+element +" of class "
						+packageAndClass+" with modifier "+Modifier.toString(modifiers)+eclipseMessage,
						prio, "45","Class", typeName, null, 45,"'transient' modifier removed from a "+elemType.bar(),
						Modifier.toString(modifiers),returnTypeIfNeeded,kind,elemType,params.get(1),packageAndClass);
			  	return result;
			default: throw new RuntimeException("strange45");
		}
	}

	private Data handle46(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String flagsString = "46";
		String packageAndClass = params.get(0);
		boolean sourceCall;
		String chapter;
		boolean sourceInherit;
		boolean isClass = false;
		String element = null;
		String element1 = null;
		String elementName = null; //for determining the elemName parameter
		switch(elemType){
		case FIELD_ELEMENT_TYPE: element = params.get(1);
			element1 = "."+element;
			elementName = params.get(1); //
			chapter = "Field";
		break;
		case METHOD_ELEMENT_TYPE: element = params.get(1);
			element1 = "."+element;
			elementName = params.get(1); 
			chapter = "Method"; // TODO: Should point to sub-chapter, No anchors so far.
		break;
		case API_COMPONENT_ELEMENT_TYPE: // nothing to do.
			element1 = "";
			elementName = params.get(0); 
			chapter = ""; // TODO: testcase missing.
			isClass = true;
		break;
		case CLASS_ELEMENT_TYPE: // nothing to do.
			element1 = "";
			elementName = params.get(0); 
			chapter = "Class";
			isClass = true;
		break;
		default:
			throw new RuntimeException("something strange 46");
		}
		switch(kind){
			case ADDED:
				sourceCall = true;
				sourceInherit = true;
				flagsString = flagsString+"a";
				if(newModifier!=0){
					modifiers=newModifier;
					}
				break;
			case CHANGED:
				sourceCall = classif.callForbidden(packageAndClass);
				sourceInherit = classif.inheritanceForbidden(packageAndClass);
				flagsString = flagsString+"c";
				break;
			case REMOVED:
				sourceCall = classif.callForbidden(packageAndClass);
				sourceInherit = classif.inheritanceForbidden(packageAndClass);
				flagsString = flagsString+"d";
				if(isClass&&_ignoreMissingClasses){
					return null;
				}
				break;
			default: throw new RuntimeException("strange46");
		}
		int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
		Data result = new Data("messAPIComp46",elemType.bar()+" "+kind.toString()+" "+packageAndClass+element1+" with modifiers "
				+Modifier.toString(modifiers)+eclipseMessage,prio, flagsString, chapter, typeName, element, 46,elemType.bar()+" was "+kind.toString(),
				Modifier.toString(modifiers),returnTypeIfNeeded,kind,elemType,elementName,packageAndClass);
				//TODO: If an element was changed, how to know what was changed to what?
	  	return result;
	}

	private Data handle47(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String flagsString = "47";
		String elementName = params.get(0);
		String kindString = null;
		boolean sourceCall;
		boolean sourceInherit;
		String packageAndClass = params.get(0);
		switch(kind){
			case ADDED:  
				flagsString = flagsString+"a";
				kindString = "added to "; //
				if(newModifier!=0){
					modifiers=newModifier;
					}
				break;				
			case CHANGED:
				flagsString = flagsString+"c";
				kindString = "changed in "; //
				break;
			case REMOVED:
				flagsString = flagsString+"d";
				kindString = "removed from "; //
				break;
			default: throw new RuntimeException("strange47");
		}
		String elemTypeString = elemType.bar();
		String method = null;
		int length = typeName.length();
		if(elementName.length()>length){
			method = elementName.substring(length+1);// attribute name or method including signature.
			method = method.replaceAll(" ", ""); // blanks are not allowed in property keys.
			elementName = method;
		}
		switch(elemType){
			case CLASS_ELEMENT_TYPE: 
				flagsString = flagsString+"_class";
				
				break;
			case INTERFACE_ELEMENT_TYPE: 
				flagsString = flagsString+"_class";
				break;
			case ANNOTATION_ELEMENT_TYPE: 
				flagsString = flagsString+"_class";
				break;
			case ENUM_ELEMENT_TYPE: 
				flagsString = flagsString+"_class";
				break;
			case CONSTRUCTOR_ELEMENT_TYPE:
				flagsString = flagsString+"_method";
				break;
			case METHOD_ELEMENT_TYPE:
				flagsString = flagsString+"_method";
				break;
			case FIELD_ELEMENT_TYPE:
				flagsString = flagsString+"_field";
				break;
			default: // no addition.
				break;
		}
		sourceCall = classif.callForbidden(elementName);
		sourceInherit = classif.inheritanceForbidden(elementName);
		int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
		Data result = new Data("messAPIComp47","Type argument "+kind.toString()+" "+elemTypeString
				+" "+elementName+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage,
				prio, flagsString, "Generics", typeName, method, 47,"Type argument inside generics(&lt; &gt;) was "+kindString+elemType.bar(),
				Modifier.toString(modifiers),returnTypeIfNeeded,kind,elemType,elementName,packageAndClass);
				//TODO: the object type inside the generics<> cannot be known from eclipse tool. How can we get it?
	  	return result;
	}

	/** add type member
	 * 
	 * @param binaryCompatible
	 * @param flags
	 * @param kind
	 * @param modifiers
	 * @param params
	 * @param elemType
	 * @return
	 */
	private Data handle48(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		boolean sourceCall;
		boolean sourceInherit;
		String innerClass = params.get(0);
		String kindString = null; //
		String packageAndClass = params.get(0);
		switch(kind){
			case ADDED:
				sourceCall = true;
				sourceInherit = true;
				innerClass = params.get(0);
				kindString = "added to "; //
				if(newModifier!=0){
					modifiers=newModifier;
				}
				break;
			case REMOVED:
				sourceCall = classif.callForbidden(typeName);
				sourceInherit = classif.inheritanceForbidden(typeName);
				kindString = "removed from "; //
				break; 
			default: throw new RuntimeException("strange48");
		}
		int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
		Data result = new Data("messAPIComp48",elemType.bar()+" "+kind+" "+innerClass+" to "+typeName+" with modifiers "
				+Modifier.toString(modifiers)+eclipseMessage,prio, "48", "Class", typeName, innerClass, 48,
				"Inner class/Enum was "+kindString+"class",Modifier.toString(modifiers),null,kind,elemType,innerClass,packageAndClass);
				//TODO: How to determine if it was an inner class or enum? and are we sure that it's only for those 2 options?
	  	return result;
	}
	
	private Data handle49(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		boolean sourceCall=true;// TODO: Write test and verify
		boolean sourceInherit=true;// TODO: Write test and verify
		String packageAndClass = params.get(0);
		switch(kind){
		case ADDED:
			// TODO: Write test and verify
//			sourceCall = classif.callForbidden(packageAndClass);
//			sourceInherit = classif.inheritanceForbidden(packageAndClass);
			if(newModifier!=0){
				modifiers=newModifier;
				}
			break;
		case REMOVED:
//			sourceCall = classif.callForbidden(packageAndClass);
//			sourceInherit = classif.inheritanceForbidden(packageAndClass);
			break;
		default: throw new RuntimeException("strange49");
		}
		int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
		Data result = new Data("messAPIComp49",kind.toString()+" parameter names in "+elemType.bar()+" "+packageAndClass+" with modifiers "
				+Modifier.toString(modifiers)+eclipseMessage,prio, "49", "Method", typeName, null, 49,
				" parameter name was "+kind.toString()+" in an element",Modifier.toString(modifiers),null,kind,elemType,packageAndClass,packageAndClass);
	  			//TODO: What exactly is the element? always method or also something else? and how to know the parameters added/removed/changed?
		return result;
	}
	
	private Data handle50(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		boolean sourceCall=true;// TODO: Write test and verify
		boolean sourceInherit=true;// TODO: Write test and verify
		String packageAndClass = params.get(0);
		switch(kind){
		case CHANGED:
			// TODO: Write test and verify
//			sourceCall = classif.callForbidden(packageAndClass);
//			sourceInherit = classif.inheritanceForbidden(packageAndClass);
			break;
//		case REMOVED:
//			sourceCall = classif.callForbidden(packageAndClass);
//			sourceInherit = classif.inheritanceForbidden(packageAndClass);
//			break;
		default: throw new RuntimeException("strange50");
		}
		int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
		Data result = new Data("messAPIComp50",kind.toString()+" parameter names in "+elemType.bar()+" "+packageAndClass+" with modifiers "
				+Modifier.toString(modifiers)+eclipseMessage,prio, "50", "Method", typeName, null, 50,
				" parameter name was "+kind.toString()+" in an element",Modifier.toString(modifiers),null,kind,elemType,packageAndClass,packageAndClass);
	  			//TODO: What exactly is the element? always method or also something else? and how to know the parameters added/removed/changed?
		return result;
	}
	
	private Data handle51(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		boolean sourceCall;
		boolean sourceInherit;
		String packageAndClass = params.get(0);
		switch(kind){
		case ADDED:
			sourceCall = classif.callForbidden(packageAndClass);
			sourceInherit = classif.inheritanceForbidden(packageAndClass);
			if(newModifier!=0){
				modifiers=newModifier;
				}
			break;
		case REMOVED:
			sourceCall = classif.callForbidden(packageAndClass);
			sourceInherit = classif.inheritanceForbidden(packageAndClass);
			break;
		default: throw new RuntimeException("strange51");
		}
		int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
		Data result = new Data("messAPIComp51",kind.toString()+" parameter(s)in "+elemType.bar()+" "+packageAndClass+" with modifiers "
				+Modifier.toString(modifiers)+eclipseMessage,prio, "51", "Class", typeName, null, 51,
				" parameter(s) was "+kind.toString()+" in an element",Modifier.toString(modifiers),null,kind,elemType,packageAndClass,packageAndClass);
	  			//TODO: What exactly is the element? always class/interface or also something else? and how to know the parameters added/removed/changed?
		return result;
	}
	
	//API Tooling Upgrade
	private Data handle53(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String packageAndClass = params.get(0);
		boolean sourceCall;
		boolean sourceInherit;
				int prio;
				Data result;
				switch(kind){
					case ADDED:
						if(newModifier!=0){
							modifiers=newModifier;
							}
						sourceCall = classif.callForbidden(packageAndClass);
						sourceInherit = classif.inheritanceForbidden(packageAndClass);
						prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
						result = new Data("messAPIComp53a"," The unchecked Exception"+params.get(2)+" has been added to constructor/Method"
								+params.get(0)+"."+params.get(1)+"with modifiers"+Modifier.toString(modifiers)+eclipseMessage,
								prio, "53", "Class", typeName,null, 53,"The unchecked exception has been added for constructor",
								Modifier.toString(modifiers),null,kind,elemType,params.get(0),packageAndClass);
					  	return result;
					case REMOVED:
						sourceCall = classif.callForbidden(packageAndClass);
						sourceInherit = classif.inheritanceForbidden(packageAndClass);
						prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
						 result = new Data("messAPIComp53b"," The unchecked Exception"+params.get(2)+" has been removed for constructor/Method"
								+params.get(0)+"."+params.get(1)+"with modifiers"+Modifier.toString(modifiers)+eclipseMessage,
									prio, "53", "Class", typeName,null, 53,"The unchecked exception has been removed for method",
									Modifier.toString(modifiers),null,kind,elemType,params.get(0),packageAndClass);
						  	return result;
					default: throw new RuntimeException("strange53");
				}
			}
	
	private Data handle54(boolean binaryCompatible, ChangeKind kind,int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		int prio;
		Data result;
		String packageAndClass = params.get(0);
		String attributeName = ""; //params.get(1); // old or new value in here. Hard to escape....
		String attrValue = ""; //params.get(2); // old or new value in here. Hard to escape.... 
		boolean sourceInherit;
		String flagsString = "54";
		switch(kind){
			case ADDED:	
				sourceInherit = classif.inheritanceForbidden(packageAndClass);
				if(newModifier!=0){
					modifiers=newModifier;
					}
				break;
			case CHANGED:
				sourceInherit = true;
				break;
			case REMOVED:
				sourceInherit = classif.inheritanceForbidden(packageAndClass);
				break;
			default: throw new RuntimeException("strange54");
		}	  
		prio = priorities.getPriority(modifiers, binaryCompatible, true, sourceInherit);
		result = new Data("messAPIComp54",kind.toString()+" value of attribute "+params.get(1)+" with modifiers "
				+Modifier.toString(modifiers) +" of class "+packageAndClass+" value "+params.get(2)+eclipseMessage,
				prio, flagsString, "Field", typeName, attributeName, 54,
				"value of attribute '"+attributeName+"' was "+kind.toString()+". Could be because of adding/removing the 'final' keyword",
				Modifier.toString(modifiers),null,kind,elemType,attrValue,packageAndClass);
				//TODO: Is it always because of adding/removing the 'final' keyword? 
				//TODO: In case of "changed", is params.get(2) the old or new value? And how to get the other value if it's not specified from eclipse tool??  
	  	return result;
	}

	private Data handle55(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		boolean sourceCompatibleCaller;
		boolean sourceCompatibleImplementor;
		String flagsString = "55";
		String packageAndClass = params.get(0);
		switch(kind){
//			case ADDED:
//				sourceCompatibleCaller = classif.callForbidden(packageAndClass);
//				sourceCompatibleImplementor = classif.inheritanceForbidden(packageAndClass); // TODO: Test
//				flagsString = flagsString+"a";
//				break;
			case CHANGED:
				sourceCompatibleCaller = classif.callForbidden(packageAndClass); // TODO: Test with "declare" and with "catch"
				sourceCompatibleImplementor = classif.inheritanceForbidden(packageAndClass);
				flagsString = flagsString+"c";
				break;
			default: throw new RuntimeException("strange55");
		}
		String param1 = params.get(1);
		int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCompatibleCaller, sourceCompatibleImplementor);
		Data result = new Data("messAPIComp55","Changed varargs "+param1+" "+kind+" on "+elemType.bar()+" "+params.get(1)
				+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage, prio,flagsString,"",typeName,params.get(1), 
				55,"Changed varargs to array of object",Modifier.toString(modifiers),null,kind,elemType,param1,packageAndClass);
	  	return result;
	}


	private Data handle56(boolean binaryCompatible, ChangeKind kind, int modifiers, 
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String packageAndClass = params.get(0);
		boolean sourceCall;
		boolean sourceInherit;
		
		switch(kind){
			case REMOVED:
				sourceCall = classif.callForbidden(packageAndClass);
				sourceInherit = classif.inheritanceForbidden(packageAndClass);
				int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
				Data result = new Data("messAPIComp56","Decreasing visibility of "+elemType.bar()+" "+packageAndClass 
						+" changed to "+Modifier.toString(modifiers)+ eclipseMessage,
						prio, "56", "Class", typeName, null, 56,"visibility of interface/class was decreased to "+Modifier.toString(modifiers),
						Modifier.toString(modifiers),null,kind,elemType,packageAndClass,packageAndClass);
						// TODO: Check; correct for "class". No sample for method / attribute.
			  	return result;
			default: throw new RuntimeException("strange56");
		}
	}
	
	private Data handle57(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String element = "";
		String packageAndClass = params.get(0);
		boolean sourceCall;
		boolean sourceInherit;
		switch(elemType){
			case FIELD_ELEMENT_TYPE: element = "from "+elemType.bar()+" "+params.get(1);
			break;
			case METHOD_ELEMENT_TYPE: element = " from "+elemType.bar()+" "+params.get(1);
			break;
			default:
				throw new RuntimeException("something strange 57");
		}
		switch(kind){
			case CHANGED:
				sourceCall = classif.callForbidden(packageAndClass);
				sourceInherit = classif.inheritanceForbidden(packageAndClass);
				int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
				Data result = new Data("messAPIComp57","Added 'volatile' modifier "+element +" of class "
						+packageAndClass+" with modifier "+Modifier.toString(modifiers)+eclipseMessage,
						prio, "57","Class", typeName, null, 57,"'volatile' modifier added from a "+elemType.bar(),
						Modifier.toString(modifiers),returnTypeIfNeeded,kind,elemType,params.get(1),packageAndClass);
			  	return result;
			default: throw new RuntimeException("strange57");
		}
	}

	private Data handle58(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String element = "";
		String packageAndClass = params.get(0);
		boolean sourceCall;
		boolean sourceInherit;
		switch(elemType){
			case FIELD_ELEMENT_TYPE: element = "from "+elemType.bar()+" "+params.get(1);
			break;
			case METHOD_ELEMENT_TYPE: element = " from "+elemType.bar()+" "+params.get(1);
			break;
			default:
				throw new RuntimeException("something strange 58");
		}
		switch(kind){
			case CHANGED:
				sourceCall = classif.callForbidden(packageAndClass);
				sourceInherit = classif.inheritanceForbidden(packageAndClass);
				int prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
				Data result = new Data("messAPIComp58","Removed 'volatile' modifier "+element +" of class "
						+packageAndClass+" with modifier "+Modifier.toString(modifiers)+eclipseMessage,
						prio, "58","Class", typeName, null, 58,"'volatile' modifier removed from a "+elemType.bar(),
						Modifier.toString(modifiers),returnTypeIfNeeded,kind,elemType,params.get(1),packageAndClass);
			  	return result;
			default: throw new RuntimeException("strange58");
		}
	}

	// change of version counter, no need to generate a message.
	private Data handle60( ChangeKind kind,  
			 ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		switch(kind){
			case CHANGED:
				Data result = new Data("messAPIComp60","Increased minor version counter (EHP compatibility) "+eclipseMessage,
						4, "60", "", typeName, null, 60,"Increased minor version counter (EHP compatibility)",
						null,null,kind,elemType,null,"");
			  	return result;
			default: throw new RuntimeException("strange60");
		}
	}
	//API Tooling Upgradation
	private Data handle61(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		
		String packageAndClass = params.get(0);
		boolean sourceCall;
		boolean sourceInherit;
				int prio;
				Data result;
				switch(kind){
					
					case REMOVED:
						sourceCall = classif.callForbidden(packageAndClass);
						sourceInherit = classif.inheritanceForbidden(packageAndClass);
						prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
						 result = new Data("messAPIComp61"," The field "
									+params.get(1)+" has been removed from"+ params.get(0)+"tagged with @noextend with modifiers"+Modifier.toString(modifiers)+eclipseMessage,
									prio, "61", "Class", typeName,null, 61,"The  field has been removed from a class tagged with ''@noextend''",
									Modifier.toString(modifiers),null,kind,elemType,params.get(0),packageAndClass);
						  	return result;
					default: throw new RuntimeException("strange61");
				}
			}
	
	private Data handle62(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
				
		String packageAndClass = params.get(0);
		boolean sourceCall;
		boolean sourceInherit;
				int prio;
				Data result;
				switch(kind){
				
					case REMOVED:
						sourceCall = classif.callForbidden(packageAndClass);
						sourceInherit = classif.inheritanceForbidden(packageAndClass);
						prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
						 
						 result = new Data("messAPIComp62"," The Method "
									+params.get(1)+" has been removed from"+ params.get(0)+"tagged with @noextend with modifiers"+Modifier.toString(modifiers)+eclipseMessage,
									prio, "62", "Class", typeName,null, 62,"The  method has been removed from a class tagged with ''@noextend''",
									Modifier.toString(modifiers),null,kind,elemType,params.get(0),packageAndClass);
						  	return result;
					default: throw new RuntimeException("strange62");
				}
			}
	
	private Data handle63(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String packageAndClass = params.get(0);
		boolean sourceCall;
		boolean sourceInherit;		
				int prio;
				Data result;
				switch(kind){
					
					case REMOVED:
						sourceCall = classif.callForbidden(packageAndClass);
						sourceInherit = classif.inheritanceForbidden(packageAndClass);
						prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
						 result = new Data("messAPIComp63"," The constructor"
									+params.get(0)+ params.get(1)+"has been removed  with modifiers"+Modifier.toString(modifiers)+eclipseMessage,
									prio, "63", "Class", typeName,null, 63,"The constructor has been removed from class",
									Modifier.toString(modifiers),null,kind,elemType,params.get(0),packageAndClass);
						  	return result;
					default: throw new RuntimeException("strange63");
				}
			}
	
	private Data handle67(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String packageAndClass = params.get(0);
		boolean sourceCall;
		boolean sourceInherit;
				int prio;
				Data result;
				switch(kind){
				
					case REMOVED:
						sourceCall = classif.callForbidden(packageAndClass);
						sourceInherit = classif.inheritanceForbidden(packageAndClass);
						prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
						 result = new Data("messAPIComp67"," The method "
									+params.get(1)+" of class "+ params.get(0)+"has been moved down in the hierarchy with modifiers"+Modifier.toString(modifiers)+eclipseMessage,
									prio, "67", "Class", typeName,null, 67,"The method has been moved down in the hierarchy",
									Modifier.toString(modifiers),null,kind,elemType,params.get(0),packageAndClass);
						  	return result;
					default: throw new RuntimeException("strange67");
				}
			}

	private Data handle68(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String packageAndClass = params.get(0);
		boolean sourceCall;
		boolean sourceInherit;
				int prio;
				Data result;
				switch(kind){
					case ADDED:
						if(newModifier!=0){
							modifiers=newModifier;
							}
						sourceCall = classif.callForbidden(packageAndClass);
						sourceInherit = classif.inheritanceForbidden(packageAndClass);
						prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
						result = new Data("messAPIComp68a"," Message Not Found"+eclipseMessage,
								prio, "68", "Class", typeName,null, 68,"Message Not Found",
								Modifier.toString(modifiers),null,kind,elemType,params.get(0),packageAndClass);
					  	return result;
					case REMOVED:
						sourceCall = classif.callForbidden(packageAndClass);
						sourceInherit = classif.inheritanceForbidden(packageAndClass);
						prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
						 result = new Data("messAPIComp68b"," Message Not Found"+eclipseMessage,
									prio, "68", "Class", typeName,null, 68,"Message Not Found",
									Modifier.toString(modifiers),null,kind,elemType,params.get(0),packageAndClass);
						  	return result;
					case CHANGED:
						sourceCall = classif.callForbidden(packageAndClass);
						sourceInherit = classif.inheritanceForbidden(packageAndClass);
						prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
						 result = new Data("messAPIComp68c"," Message Not Found"+eclipseMessage,
									prio, "68", "Class", typeName,null, 68,"Message Not Found",
									Modifier.toString(modifiers),null,kind,elemType,params.get(0),packageAndClass);
						  	return result;
					default: throw new RuntimeException("strange68");
				}
			}
	
	private Data handle69(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
			List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String packageAndClass = params.get(0);
		boolean sourceCall;
		boolean sourceInherit;
		
				int prio;
				Data result;
				switch(kind){
					case ADDED:
						if(newModifier!=0){
							modifiers=newModifier;
							}
						sourceCall = classif.callForbidden(packageAndClass);
						sourceInherit = classif.inheritanceForbidden(packageAndClass);
						prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
						result = new Data("messAPIComp69a"," The interface "
								+params.get(1)+" adds method "+params.get(2)+"to"+ params.get(0)+"that is not tagged as @noimplement with modifiers"+Modifier.toString(modifiers)+eclipseMessage,
								prio, "69", "Class", typeName,null, 69,"Interface adds method to class that is not tagged as @noimplement",
								Modifier.toString(modifiers),null,kind,elemType,params.get(0),packageAndClass);
					  	return result;
					case REMOVED:
						sourceCall = classif.callForbidden(packageAndClass);
						sourceInherit = classif.inheritanceForbidden(packageAndClass);
						prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
						 result = new Data("messAPIComp69b"," The interface "
									+params.get(1)+" removes method "+params.get(2)+"from"+ params.get(0)+"that is not tagged as @noimplement with modifiers"+Modifier.toString(modifiers)+eclipseMessage,
									prio, "69", "Class", typeName,null, 69,"Interface removes method from class that is not tagged as @noimplement",
									Modifier.toString(modifiers),null,kind,elemType,params.get(0),packageAndClass);
						  	return result;
					default: throw new RuntimeException("strange69");
				}
			}
	
	

	private Data handle70(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
	List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String packageAndClass = params.get(0);
		boolean sourceCall;
		boolean sourceInherit;
		String paramValue;
		int prio;
		Data result;
		switch(kind){
			case ADDED:
				if(newModifier!=0){
					modifiers=newModifier;
					}
				paramValue= (params.size()>1) ?params.get(1):"";
				sourceCall = classif.callForbidden(packageAndClass);
				sourceInherit = classif.inheritanceForbidden(packageAndClass);
				prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
				result = new Data("messAPIComp70"," The re exported type  "+params.get(0)+paramValue+"been added to class/interface"
						+params.get(0)+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage,
						prio, "70", "Class", typeName,null, 70,"The re exported type has been added to class",
						Modifier.toString(modifiers),null,kind,elemType,params.get(0),packageAndClass);
			  	return result;
		
			default: throw new RuntimeException("strange70");
		}
	}



	
	private Data handle72(boolean binaryCompatible, ChangeKind kind, int modifiers, String returnTypeIfNeeded,
	List<String> params, ElementTypes elemType, String typeName, String eclipseMessage, int newModifier) {
		String packageAndClass = params.get(0);
		boolean sourceCall;
		boolean sourceInherit;
		String paramValue;
		int prio;
		Data result;
		switch(kind){
			case ADDED:
				if(newModifier!=0){
					modifiers=newModifier;
					}
				sourceCall = classif.callForbidden(packageAndClass);
				sourceInherit = classif.inheritanceForbidden(packageAndClass);
				prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
				paramValue= (params.size()>1) ?params.get(1):"";
				result = new Data("messAPIComp72a"," Deprecation Modifiers"+paramValue +" for have been added to class/interface "
						+params.get(0)+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage,
						prio, "72", "Class", typeName,null, 72,"Deprecation Modfiers have been added to class/interface",
						Modifier.toString(modifiers),null,kind,elemType,params.get(0),packageAndClass);
			  	return result;
			case REMOVED:
				sourceCall = classif.callForbidden(packageAndClass);
				sourceInherit = classif.inheritanceForbidden(packageAndClass);
				prio = priorities.getPriority(modifiers, binaryCompatible, sourceCall, sourceInherit);
				paramValue= (params.size()>1) ?params.get(1):"";
					result = new Data("messAPIComp72b"," Deprecation Modifiers"+paramValue+" have been removed from class/interface "
							+params.get(0)+" with modifiers "+Modifier.toString(modifiers)+eclipseMessage,
							prio, "72", "Class", typeName,null, 72,"Deprecation Modfiers have been removed from class/interface",
							Modifier.toString(modifiers),null,kind,elemType,params.get(0),packageAndClass);
				  	return result;
			default: throw new RuntimeException("strange72");
		}
	}

	
	

}
