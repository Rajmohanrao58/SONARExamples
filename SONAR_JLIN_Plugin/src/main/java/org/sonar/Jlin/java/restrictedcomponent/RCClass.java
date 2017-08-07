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
 * $Id: //tc/jtools/dev_E30/src/_jlint/java/_modules/_jom/_tests/src/com/sap/tc/jtools/jlint/tests/rc/RCClass.java#4 $
 */
 
package org.sonar.Jlin.java.restrictedcomponent;

import java.util.Iterator;



/**
 * @author D037913
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class RCClass implements RCConstants {

  private RCStatusInfo statusInfo;
  private String name;
  private RCMethod[] methods;
  private RCField[] fields;
  private RCClass[] innerClasses;

  public RCClass(StructureTree classTree, RCStatusInfo parentStatus) {
    name = classTree.getParameter(ATTR_NAME);
    this.statusInfo = new RCStatusInfo(classTree, parentStatus);
    StructureTree[] methodTrees = classTree.getChildren(TAG_METHOD);
    methods = new RCMethod[methodTrees.length];
    for (int i = 0; i < methodTrees.length; i++) {
      methods[i] = new RCMethod(methodTrees[i], this);
    }
    StructureTree[] fieldTrees = classTree.getChildren(TAG_FIELD);
    fields = new RCField[fieldTrees.length];
    for (int i = 0; i < fieldTrees.length; i++) {
      fields[i] = new RCField(fieldTrees[i], this);
    }
    StructureTree[] innerClassTrees = classTree.getChildren(TAG_CLASS);
    innerClasses = new RCClass[innerClassTrees.length];
    for (int i = 0; i < innerClassTrees.length; i++) {
      innerClasses[i] = new RCClass(innerClassTrees[i], statusInfo);
    }
  }

  public RCClass(
    String name,
    RCStatusInfo statusInfo,
    RCMethod[] methods,
    RCField[] fields,
    RCClass[] innerClasses) {
    this.name = name;
    this.statusInfo = statusInfo;
    this.methods = methods;
    this.fields= fields;
    this.innerClasses = innerClasses;
  }

  /**
   * @return
   */
  public RCClass[] getInnerClasses() {
    return innerClasses;
  }

  /**
   * @return
   */
  public RCMethod[] getMethods() {
    return methods;
  }

  public RCField[] getFields() {
    return fields;
  }

  /**
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * @return
   */
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
    if (callee.length - curIdx > 1) {
      // we search an inner class
      for (int i = 0; i < innerClasses.length; i++) {
        if (innerClasses[i].getName().equals(callee[curIdx])) {
          return innerClasses[i].getStatusInfo(caller, callee, curIdx + 1);
        }
      }
    } else {
      // we search for a method or field
      if (callee[curIdx].indexOf('@') != -1) {
        //it's a method
        for (int i = 0; i < methods.length; i++) {
          String signature = methods[i].getSignature();
          String methName = methods[i].getName();
          if ((signature == null && callee[curIdx].startsWith(methName + "@"))
            || callee[curIdx].equals(methName + "@" + signature)) {
            return methods[i].getStatusInfo(caller, callee);
          }
        }
      } else {
        //it's a field
        for (int i = 0; i < fields.length; i++) {
          String fieldName = fields[i].getName();
          if (callee[curIdx].equals(fieldName)) {
            return fields[i].getStatusInfo(caller, callee);
          }
        }
      }
    }
    return statusInfo;
  }

}
