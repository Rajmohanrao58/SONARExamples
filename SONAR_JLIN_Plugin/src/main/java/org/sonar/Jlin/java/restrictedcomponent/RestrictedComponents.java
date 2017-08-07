package org.sonar.Jlin.java.restrictedcomponent;

import java.util.Collections;

public class RestrictedComponents {

	
	public static final String PACKAGE_ROOT_NAME = "";
	  
	  public RCPackage[] packages;

	  private static final String ATTR_DEFAULT_STATUS = "DEFAULT_STATUS";
	  private static final String ATTR_DEFAULT_PRIO= "DEFAULT_PRIORITY";
	  private static final String DEFAULT_REASON="http://bis.wdf.sap.corp:1080/twiki/bin/view/Techdev/JavaClassificationDetails#Unknown";

	  private RCStatusInfo defaultStatusForRoot;
	  private String defaultStatus;
	  private int defaultPrio;
	  
	  
	  public RestrictedComponents(StructureTree tree) {
		     Header topHeader = tree.getHeader();
		     this.defaultStatus = topHeader.getParameter(ATTR_DEFAULT_STATUS);
		     if (defaultStatus == null) {
		       defaultStatus = RCStatusInfo.STRING_STATUS_FORBIDDEN;   
		     }
		     String defaultPrioString = topHeader.getParameter(ATTR_DEFAULT_PRIO);
		     if (defaultPrioString == null) {
		       this.defaultPrio = 1;   
		     } else {
		       this.defaultPrio = Integer.parseInt(defaultPrioString.trim());   
		     }
		     this.defaultStatusForRoot = new RCStatusInfo("", this.defaultStatus, this.defaultPrio, DEFAULT_REASON, Collections.EMPTY_LIST); 
		    RCPackage pkgRoot =
		      new RCPackage(
		        "",
		        null,
		        null,
		        defaultStatusForRoot);
		    StructureTree[] pkgTrees = tree.getChildren(RCConstants.TAG_PKG);
		    packages = new RCPackage[pkgTrees.length];
		    for (int i = 0; i < pkgTrees.length; i++) {
		      packages[i] = new RCPackage(pkgTrees[i], pkgRoot);
		    }
		  }
}
