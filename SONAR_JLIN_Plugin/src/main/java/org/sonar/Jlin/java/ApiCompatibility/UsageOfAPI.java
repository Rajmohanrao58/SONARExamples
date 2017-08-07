package org.sonar.Jlin.java.ApiCompatibility;

import java.util.Map;

/**
 * This class tells for any API element, if it may be used by callers
 * or implementers / extenders / overriders.
 * By default any element is allowed for any usage. Only if a JavaDoc
 * taglet indicates other usage, it may be limited.
 * @author D034003
 *
 */
public class UsageOfAPI implements IUsageOfAPI {
	// Key "package.Class$InnerClass"
	private Map<String,IUsageOfAPI.CodeClasses> classInfo = null;
	// Key: "package.Class$InnerClass.element" where element can be any attribute or method (including signature)
	private Map<String,IUsageOfAPI.CodeClasses> elementInfo = null;
	
	/**
	 * parameters may be null.
	 * @param classInfo map with key "package.Class$InnerClass"
	 * @param elementInfo map with key "package.Class$InnerClass.method(signature)"
	 */
	public UsageOfAPI(Map<String,IUsageOfAPI.CodeClasses> classInfo,Map<String,IUsageOfAPI.CodeClasses> elementInfo){
		this.classInfo = classInfo;
		this.elementInfo = elementInfo;
	}
	
//	public UsageOfAPI(String fileClassUsage, String fileElementUsage){
//		throw new RuntimeException("implement me");
//	}
	
	public void addUsageClass(String packageAndClass, CodeClasses value){
		classInfo.put(packageAndClass, value);
	}
	
	public void addUsageElement(String packageAndClass, String element, CodeClasses value){
		elementInfo.put(packageAndClass+"."+element, value);
	}
	
	@Override
	public boolean isFreeForCall(String packageAndClass) {
		IUsageOfAPI.CodeClasses tmp = null;
		if (classInfo!=null){
			tmp = classInfo.get(packageAndClass);
		}
		if (tmp!=null){
			return tmp.isFreeForCall();
		}
		return true;
	}

	@Override
	public boolean isFreeForInheritance(String packageAndClass) {
		IUsageOfAPI.CodeClasses tmp = null;
		if(classInfo!=null){
			tmp = classInfo.get(packageAndClass);
		}
		if (tmp!=null){
			return tmp.isFreeForInheritance();
		}
		return true;
	}

	@Override
	public boolean isFreeForCall(String packageAndClass, String element) {
		boolean result = true; // default
		// override default with class setting if available
		IUsageOfAPI.CodeClasses tmp = null;
		if(classInfo!=null){
			tmp = classInfo.get(packageAndClass);
		}
		if (tmp!=null){
			result = tmp.isFreeForCall();
		}
		// override current setting with element setting, if available.
		if(elementInfo!=null){
			tmp = elementInfo.get(element);
		}
		if (tmp!=null){
			result = tmp.isFreeForCall();
		}
		return result;
	}

	@Override
	public boolean isFreeForInheritance(String packageAndClass, String element) {
		boolean result = true; // default
		// override default with class setting if available
		IUsageOfAPI.CodeClasses tmp = null;
		if(classInfo!=null){
			tmp = classInfo.get(packageAndClass);
		}
		if (tmp!=null){
			result = tmp.isFreeForInheritance();
		}
		// override current setting with element setting, if available.
		if(elementInfo!=null){
			tmp = elementInfo.get(element);
		}
		if (tmp!=null){
			result = tmp.isFreeForInheritance();
		}
		return result;
	}

	@Override
	public boolean callForbidden(String packageAndClass) {
		boolean tmp = this.isFreeForCall(packageAndClass);
//		if(tmp){
//			System.out.println("forbidden call "+packageAndClass);
//		}
		return !tmp;
	}

	@Override
	public boolean callForbidden(String packageAndClass, String element) {
		boolean tmp = this.isFreeForCall(packageAndClass, element);
//		if(tmp){
//			System.out.println("forbidden call "+packageAndClass+"."+element);
//		}
		return !tmp;
	}

	@Override
	public boolean inheritanceForbidden(String packageAndClass) {
		boolean tmp = this.isFreeForInheritance(packageAndClass);
//		if(tmp){
//			System.out.println("forbidden inherit "+packageAndClass);
//		}
		return !tmp;
	}

	@Override
	public boolean inheritanceForbidden(String packageAndClass, String element) {
		boolean tmp = this.isFreeForInheritance(packageAndClass, element);
//		if(tmp){
//			System.out.println("forbidden inherit "+packageAndClass+"."+element);
//		}
		return !tmp;
	}

	
}
