package org.sonar.Jlin.java.ApiCompatibility;

import java.lang.reflect.Modifier;

/**
 * in this class, messages get assigned a priority depending on the change, the 
 * visibility of the element (public, protected...), configuration and suppression 
 * information.
 * The more "true" (compatible) values are provided, the lower is the priority.
 * @author d034003
 * DONE: Add suppression information.
 * DONE: Differentiate message priorities on change type as stored in "flags"
 * DONE: make non-static, in case several instances run in parallel.
 * DONE: evaluate JavaDoc taglets that restrict usage of APIs.
 */
public class MessagePriority {
	private CompatibilityRules releaseRules;
	
	public enum CompatibilityRules{
		RELEASE_COMPATIBILITY, // source and binary compatibility not strictly required.
		EHP_COMPATIBILITY, // binary compatibility not required, source compatibility required.
		SP_COMPATIBILITY, // source and binary compatibility for public/protected, only binary for package visibility.
		PATCH_COMPATIBILITY;// source and binary compatibility required for public, protected, package visibility.
		
		public static CompatibilityRules init(String value){
			CompatibilityRules result;
			if ("RELEASE_COMPATIBILITY".equals(value)){
				result = CompatibilityRules.RELEASE_COMPATIBILITY;
			}else{
				if ("EHP_COMPATIBILITY".equals(value)){
					result = CompatibilityRules.EHP_COMPATIBILITY;
				}else{
					if ("SP_COMPATIBILITY".equals(value)){
						result = CompatibilityRules.SP_COMPATIBILITY;
					}else{
						if ("PATCH_COMPATIBILITY".equals(value)){
							result = CompatibilityRules.PATCH_COMPATIBILITY;
						}else{
							throw new RuntimeException("CompatibilityRules unknown value "+value+" legal values are RELEASE_COMPATIBILITY EHP_COMPATIBILITY SP_COMPATIBILLITY PATCH_COMPATIBILITY");
						}
					}
				}
			}
			return result;
		}
	}
	
	public MessagePriority(CompatibilityRules releaseRules){
		this.releaseRules = releaseRules;
	}
	
	public MessagePriority(String releaseRules){
		this.releaseRules = CompatibilityRules.init(releaseRules);
	}
	
	/**
	 * Calculate a priority depending on a compatibility contract (Release, EHP,...Patch).
	 * Visibility and compatibility define result.
	 * @param oldVisibility as defined in java.lang.reflect.Modifier
	 * @param binaryCompatible
	 * @param sourceCompatibleCaller
	 * @param sourceCompatibleImplementor
	 * @return
	 */
	public int getPriority(int oldVisibility, boolean binaryCompatible,
			boolean sourceCompatibleCaller, boolean sourceCompatibleImplementor){
//		int result = 0;
		switch (releaseRules){
			case PATCH_COMPATIBILITY: // incomplete delivery, no recompile of client code
				if (binaryCompatible&&sourceCompatibleCaller&&sourceCompatibleImplementor){
					return 4;
				}
				else{
					return 1;	
				}
				//break;
			case SP_COMPATIBILITY: // complete SAP delivery, no recompile of ISV Code
				if(Modifier.isProtected(oldVisibility)||Modifier.isPublic(oldVisibility)){
					if(!sourceCompatibleCaller){
						return 1;
					}
					if(!sourceCompatibleImplementor){
						return 2;
					}
					if(!binaryCompatible){
						return 2;
					}
				}
				else{
					// private or package visibility. Ignore source compatibility, as customers will
					// not compile against the code.
					if(!binaryCompatible){
						return 2;
					}
				}
				return 4;
				//break;
			case EHP_COMPATIBILITY: // code will not be recompiled.
				if (Modifier.isProtected(oldVisibility)||Modifier.isPublic(oldVisibility)){
					if(!sourceCompatibleCaller){
						return 1;
					}
					if(!sourceCompatibleImplementor){
						return 2;
					}
					if(!binaryCompatible){
						return 3;
					}
				}
				else{ // private or package visibility. Do not check sourceCompatibility.
					if(!binaryCompatible){
						return 3;
					}
				}
				return 4; // compatible.
				//break;
			case RELEASE_COMPATIBILITY: // code will be recompiled. Ignore binary Compatibility
				if (Modifier.isPrivate(oldVisibility)){
					return 4;
				}
				if (Modifier.isProtected(oldVisibility)||Modifier.isPublic(oldVisibility)){
					if(!sourceCompatibleCaller){
						return 2;
					}
					if(!sourceCompatibleImplementor){
						return 2;
					}
//					if(!binaryCompatible){
//						return 3;
//					}
				}
				return 4;// default visibility or compatible.
				//break;
			default:
				throw new RuntimeException("Unhandled branch ");
		}
	}
		
}
