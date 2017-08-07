package org.sonar.Jlin.java.ApiCompatibility;

/**
 * This is the central entry point to tell for a given API element
 * (class, interface, method, attribute,...) if it may be used
 * at all, if it may be used for inheritance / extension, if
 * it may be called...
 * 
 * Before calculating the priority of an incompatible change, use this 
 * interface to check if that usage was allowed at all.
 * 
 * Improved version in comparison to com.sap.tc.jtools.jlint.tests.javadiff.tests.IUsageOfAPI
 * 
 * @author D034003
 *
 */
public interface IUsageOfAPI {

	public enum CodeClasses{
		NO_API,
		ONLY_CALL,
		ONLY_REDEFINE,
		ANY_USAGE;
		
		public boolean isFreeForCall(){
			return this.equals(ANY_USAGE)||this.equals(ONLY_CALL);
		}
		
		public boolean isFreeForInheritance(){
			return this.equals(ANY_USAGE)||this.equals(ONLY_REDEFINE);
		}
		
		public static CodeClasses getValue(boolean freeForCall, boolean freeForInherit){
			CodeClasses result = null;
			if(freeForCall){
				if(freeForInherit){
					result = ANY_USAGE;
				}
				else{
					result = ONLY_CALL;
				}
			}
			else{
				if(freeForInherit){
					result = ONLY_REDEFINE;
				}
				else{
					result = NO_API;
				}
			}
			return result;
		}
	}

	/**
	 * some positive tests on class / interface /enum or method / attribute / enum-value.
	 * @param packageAndClass
	 * @return
	 */
	public boolean isFreeForInheritance(String packageAndClass);
	public boolean isFreeForCall(String packageAndClass);
	public boolean isFreeForInheritance(String packageAndClass,String element);
	public boolean isFreeForCall(String packageAndClass, String element);
	
	/**
	 * some negative tests, inverted results to above methods.
	 */
	public boolean inheritanceForbidden(String packageAndClass);
	public boolean callForbidden(String packageAndClass);
	public boolean inheritanceForbidden(String packageAndClass,String element);
	public boolean callForbidden(String packageAndClass, String element);
}
