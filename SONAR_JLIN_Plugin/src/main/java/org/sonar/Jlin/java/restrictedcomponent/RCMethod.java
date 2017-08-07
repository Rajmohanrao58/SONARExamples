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
 * $Id: //tc/jtools/dev_E30/src/_jlint/java/_modules/_jom/_tests/src/com/sap/tc/jtools/jlint/tests/rc/RCMethod.java#4 $
 */
 
package org.sonar.Jlin.java.restrictedcomponent;

import java.util.Iterator;



/**
 * @author D037913
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class RCMethod implements RCConstants {

  public static final String METHOD_SIGNATURE_SEPARATOR = "@";
  public static final String PARAMETER_TYPE_SEPARATOR =";";
  
  private RCStatusInfo statusInfo;
  private String name;
  private String signature;

  public RCMethod(StructureTree methodTree, RCClass parent) {
    name = methodTree.getParameter(ATTR_NAME);
    signature = methodTree.getParameter(ATTR_SIGNATURE);
    this.statusInfo = new RCStatusInfo(methodTree, parent.getStatusInfo());
  }

  public RCMethod(String name, String signature, RCStatusInfo statusInfo) {
    this.name = name;
    this.signature = signature;
    this.statusInfo = statusInfo;
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
  public String getSignature() {
    return signature;
  }

  /**
   * @return
   */
  public RCStatusInfo getStatusInfo() {
    return statusInfo;
  }

  public RCStatusInfo getStatusInfo(String caller, String[] callee) {
    for (Iterator iter = statusInfo.getAuthUsers().iterator();
      iter.hasNext();
      ) {
      if (caller.startsWith((String) iter.next())) {
        return statusInfo.getOKInfo();
      }
    }
    return statusInfo;
  }

}
