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
 * $Id: //tc/jtools/dev_E30/src/_jlint/java/_modules/_jom/_tests/src/com/sap/tc/jtools/jlint/tests/rc/RCStatusInfo.java#6 $
 */
 
package org.sonar.Jlin.java.restrictedcomponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



/**
 * @author D037913
 */
public class RCStatusInfo implements RCConstants {

  public static final int PRIORITY_DEFAULT = 3;

  public static final String STRING_STATUS_FORBIDDEN = "Forbidden"; //$NON-NLS-1$
  public static final String STRING_STATUS_OK = "OK"; //$NON-NLS-1$
  private static final String STRING_STATUS_DEFAULT = STRING_STATUS_OK; 

  private RCStatusInfo okInfo;

  private String componentName;
  private String status;
  private int prio;
  private String reason;
  private List authUsers = new ArrayList();

  public RCStatusInfo(StructureTree tree, RCStatusInfo parent) {
    String relativeName = tree.getParameter(ATTR_NAME);
    if (parent == null
      || RestrictedComponents.PACKAGE_ROOT_NAME.equals(
        parent.getComponentName())) {
      componentName = relativeName;
    } else {
      componentName = parent.getComponentName() + "." + relativeName;
    }
    String statusString = tree.getParameter(ATTR_STATUS);
    if (statusString != null) {
      status = statusString;
    } else {
      if (parent != null) {
        status = parent.getStatus();
      } else {
        status = RCStatusInfo.STRING_STATUS_DEFAULT;
      }
    }
    String prioString = tree.getParameter(ATTR_PRIORITY);
    if (prioString != null) {
      prio = Integer.parseInt(prioString);
    } else {
      if (parent != null) {
        prio = parent.getPriority();
      } else {
        prio = RCStatusInfo.PRIORITY_DEFAULT;
      }
    }
    reason = tree.getParameter(ATTR_REASON);
    if (reason == null) {
      if (parent != null) {
        reason = parent.getReason();
      }
    }
    StructureTree[] authUserTrees = tree.getChildren(TAG_AUTHUSER);
    for (int i = 0; i < authUserTrees.length; i++) {
      authUsers.add(authUserTrees[i].getParameter(ATTR_NAME));
    }
    // inherit authusers from parent
    if (parent != null) {
      authUsers.addAll(parent.getAuthUsers());
    }
    okInfo =
      new RCStatusInfo(
        componentName,
        STRING_STATUS_OK,
        PRIORITY_DEFAULT,
        reason,
        Collections.EMPTY_LIST);
  }

  public RCStatusInfo(
    String componentName,
    String status,
    int prio,
    String reason,
    List authUsers) {
    this.componentName = componentName;
    this.status = status;
    this.prio = prio;
    this.reason = reason;
    this.authUsers = authUsers;
    if (!status.equals(STRING_STATUS_OK)) {
      okInfo =
        new RCStatusInfo(
          componentName,
          STRING_STATUS_OK,
          PRIORITY_DEFAULT,
          reason,
          Collections.EMPTY_LIST);
    } else {
      okInfo = this;
    }
  }

  public String getReason() {
    return reason;
  }

  public int getPriority() {
    return prio;
  }

  public String getStatus() {
    return status;
  }

  public String getComponentName() {
    return componentName;
  }

  public List getAuthUsers() {
    return authUsers;
  }

  public RCStatusInfo getOKInfo() {
    return okInfo;
  }

}
