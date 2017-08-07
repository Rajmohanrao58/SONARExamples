package org.sonar.Jlin.java.restrictedcomponent;

import java.util.ArrayList;
import java.util.List;

public class RestrictedComponentUtil {
	
	public static List<String> restrictionComponents= new ArrayList<String>();
	public static  void getRestrictedComponent(RestrictedComponents rc){
		for(RCPackage rcPackage : rc.packages){
			
			checkForRestrictedComponent(rcPackage);
			if(hasDirectSubpackage(rcPackage)){
				for(RCPackage rcPackageSub1 : rcPackage.getDirectSubPackages()){
					checkForRestrictedComponent(rcPackageSub1);
					if(hasDirectSubpackage(rcPackageSub1)){
						for(RCPackage rcPackageSub2 : rcPackageSub1.getDirectSubPackages()){
							checkForRestrictedComponent(rcPackageSub2);
							if(hasDirectSubpackage(rcPackageSub2)){
								
								for(RCPackage rcPackageSub3 : rcPackageSub2.getDirectSubPackages()){
									checkForRestrictedComponent(rcPackageSub3);
									if(hasDirectSubpackage(rcPackageSub3)){
										for(RCPackage rcPackageSub4 : rcPackageSub3.getDirectSubPackages()){
											checkForRestrictedComponent(rcPackageSub4);
											if(hasDirectSubpackage(rcPackageSub4)){
												for(RCPackage rcPackageSub5 : rcPackageSub4.getDirectSubPackages()){
													checkForRestrictedComponent(rcPackageSub5);
												
													if(hasDirectSubpackage(rcPackageSub5)){
														for(RCPackage rcPackageSub6 : rcPackageSub5.getDirectSubPackages()){
															checkForRestrictedComponent(rcPackageSub6);
															
															if(hasDirectSubpackage(rcPackageSub6)){
																for(RCPackage rcPackageSub7 : rcPackageSub6.getDirectSubPackages()){
																	checkForRestrictedComponent(rcPackageSub7);
																	
																	if(hasDirectSubpackage(rcPackageSub7)){
																		for(RCPackage rcPackageSub8 : rcPackageSub7.getDirectSubPackages()){
																			checkForRestrictedComponent(rcPackageSub8);
																			
																			if(hasDirectSubpackage(rcPackageSub8)){
																				for(RCPackage rcPackageSub9 : rcPackageSub8.getDirectSubPackages()){
																					checkForRestrictedComponent(rcPackageSub9);
																					
																					if(hasDirectSubpackage(rcPackageSub9)){
																						for(RCPackage rcPackageSub10 : rcPackageSub9.getDirectSubPackages()){
																							checkForRestrictedComponent(rcPackageSub10);
																							
																							if(hasDirectSubpackage(rcPackageSub10)){
																								for(RCPackage rcPackageSub11 : rcPackageSub10.getDirectSubPackages()){
																									checkForRestrictedComponent(rcPackageSub11);
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
		}
		
	}
	
	public static boolean hasDirectSubpackage(RCPackage rcPackage){
	if(rcPackage.getDirectSubPackages().length > 0){
		return true;
	}
		return false;
		
	}
	
	public static boolean hasRestrictionClass(RCPackage rcPackage){
		if(rcPackage.getClasses().length > 0){
			return true;
		}
		return false;
		
	}
	
	public static boolean hasRestrictionMethod(RCClass rcClass){
		
		if(rcClass.getMethods().length > 0){
			return true;
		}
		
		return false;
		
	}
	
	public static boolean hasRestrictionField(RCClass rcClass){
		if(rcClass.getFields().length > 0){
			return true;
		}
		return false;
		
	}
	
	public static void checkForRestrictedComponent(RCPackage rcPackage){
		
		
		
		if(rcPackage.getStatusInfo().getPriority() < 3){
			restrictionComponents.add(rcPackage.getStatusInfo().getComponentName().concat(".*"));
			
		}
		
		if(hasRestrictionClass(rcPackage)){
		 for(RCClass rcClass : rcPackage.getClasses()){
			 if(rcClass.getStatusInfo().getPriority() < 3){
				 restrictionComponents.add(rcClass.getStatusInfo().getComponentName().concat(".*"));
				
				}
		
			if(hasRestrictionMethod(rcClass)){
				  
				for(RCMethod rcMethod : rcClass.getMethods()){
					
					if(rcMethod.getStatusInfo().getPriority() < 3){
						restrictionComponents.add(rcMethod.getStatusInfo().getComponentName().concat(".*"));
						
					}
				}
				
				
			}
			
			if(hasRestrictionField(rcClass)){
				for(RCField rcField : rcClass.getFields()){
					
					if(rcField.getStatusInfo().getPriority() < 3){
						restrictionComponents.add(rcField.getStatusInfo().getComponentName().concat(".*"));
						
					}
				}
				
			}
		 }
		}
		
	}
}
