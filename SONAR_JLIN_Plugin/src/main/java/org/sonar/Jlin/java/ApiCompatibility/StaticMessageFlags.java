/** 
 * SonarQube Xanitizer Plugin
 * Copyright 2012-2016 by RIGS IT GmbH, Switzerland, www.rigs-it.ch.
 * mailto: info@rigs-it.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Created on Apr 17, 2017
 *
 */
package org.sonar.Jlin.java.ApiCompatibility;

import org.sonar.Jlin.java.ApiCompatibility.MessageDataLocal.ChangeKind;
import org.sonar.Jlin.java.ApiCompatibility.MessageDataLocal.ElementTypes;

/**
 * @author C5242815
 *
 */
public class StaticMessageFlags {
	
	public  String _messageId;
	public  String _message;
	public  int _prio;
	public  String _flagsString;// used as anchor in wiki
	public   String _chapter; // chapter / subpage in wiki
	public  String _typeName;
	public   String _signature;
	public   boolean _suppress=false;
	//added variables "_flag", "_flagDescription", "_modifiers", "_changeKind", "_elementType" and "_elementName" for MessageWriterDelta 
	public   int _flag;								//flag of change case from Separator
	public   String _flagDescription;				//description of the change case that belongs to the flag number
	public   String _modifiers;						//modifiers (e.x public, public static final...etc)
	public   ChangeKind _changeKind;					//added/deleted/changed
	public   ElementTypes _elementType;				//API/interface/class/method/field.....
	public   String _returnTypeIfNeeded;				//return type of method if available and needed. Null in most of handle methods!
	public   String _elementName;					//name of the element causing the difference (for every flag there is a different _elementName)
	//API Tool upgrade
	public   String _newModifiers;
	public String _packageAndClass;
	
	
	public StaticMessageFlags(String messageId,String message, int prio, String flagsString, String chapter, String typeName, String completeSignature, int flag, 
			String flagDescription, String modifiers, String returnType, ChangeKind kind, ElementTypes elemType, String elemName, String packageAndClass){
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
		_packageAndClass=packageAndClass;
	}

}
