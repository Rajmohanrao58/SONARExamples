/*
 * Copyright (c) 2004 by SAP AG, Walldorf.
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 * 
 * $Id: //tc/jtools/dev_E30/src/_jlint/java/_modules/_jom/_tests/src/com/sap/tc/jtools/jlint/tests/rc/RCPackage.java#4 $
 */
 
package org.sonar.Jlin.java.restrictedcomponent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



/**
 * @author D037913
 */
public class RCPackage implements RCConstants {

  public static final String PACKAGE_SEPARATOR = ".";

  private String name;
  public RCPackage[] directSubPackages;
  private RCClass[] classes;
  private RCStatusInfo statusInfo;
  private RCStatusInfo statusInfoInexactMatch;

  public RCPackage(StructureTree packageTree, RCPackage parent) {
    name = packageTree.getParameter(ATTR_NAME);
    RCStatusInfo parentInfo = null;
    if (parent != null) {
    	parentInfo = parent.getStatusInfo();
    }
    this.statusInfo = new RCStatusInfo(packageTree, parentInfo);
    String name = statusInfo.getComponentName();
    if ((name!=null)&&(name!="")){
    	name= name+ ".*";
    }
    this.statusInfoInexactMatch =
      new RCStatusInfo(name,
        statusInfo.getStatus(),
        statusInfo.getPriority(),
        statusInfo.getReason(),
        statusInfo.getAuthUsers());
    StructureTree[] classTrees = packageTree.getChildren(TAG_CLASS);
    List classes = new ArrayList();
    for (int i = 0; i < classTrees.length; i++) {
      classes.add(new RCClass(classTrees[i], statusInfo));
    }
    this.classes = (RCClass[]) classes.toArray(new RCClass[0]);
    StructureTree[] pkgTrees = packageTree.getChildren(TAG_PKG);
    List pkgs = new ArrayList();
    for (int i = 0; i < pkgTrees.length; i++) {
      pkgs.add(new RCPackage(pkgTrees[i], this));
    }
    this.directSubPackages = (RCPackage[]) pkgs.toArray(new RCPackage[0]);
  }

  public RCPackage(
    String name,
    RCPackage[] directSubPackages,
    RCClass[] classes,
    RCStatusInfo statusInfo) {
    this.name = name;
    this.directSubPackages = directSubPackages;
    this.classes = classes;
    this.statusInfo = statusInfo;
    String compName = statusInfo.getComponentName();
    if ((compName!=null)&&(compName!="")){
    	compName = compName+".*";
    }
    this.statusInfoInexactMatch =
      new RCStatusInfo(
        compName ,
        statusInfo.getStatus(),
        statusInfo.getPriority(),
        statusInfo.getReason(),
        statusInfo.getAuthUsers());
  }

  /**
   * @return
   */
  public RCClass[] getClasses() {
    return classes;
  }

  public RCPackage[] getDirectSubPackages() {
    return directSubPackages;
  }

  public String getName() {
    return name;
  }

  public RCStatusInfo getStatusInfo() {
    return statusInfo;
  }

  public RCStatusInfo getStatusInfo(
    String caller,
    String[] callee,
    int curIdx) {
    for (Iterator iter = statusInfo.getAuthUsers().iterator();
      iter.hasNext();
      ) {
      if (caller.startsWith((String) iter.next())) {
        return statusInfo.getOKInfo();
      }
    }
    if (callee.length - curIdx == 0) {
      return statusInfo;
    }
    for (int i = 0; i < directSubPackages.length; i++) {
      if (directSubPackages[i].getName().equals(callee[curIdx])) {
        return directSubPackages[i].getStatusInfo(caller, callee, curIdx + 1);
      }
    }
    for (int i = 0; i < classes.length; i++) {
      if (classes[i].getName().equals(callee[curIdx])) {
        return classes[i].getStatusInfo(caller, callee, curIdx + 1);
      }
    }
    return statusInfoInexactMatch;
  }

}
