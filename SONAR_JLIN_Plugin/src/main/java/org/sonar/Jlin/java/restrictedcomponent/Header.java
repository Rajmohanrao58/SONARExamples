package org.sonar.Jlin.java.restrictedcomponent;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * generic XML header.
 * 
 * A Header instance contains
 * <p>- a String tag
 * <p>- any number of String (parameter,value) pairs.
 * 
 * <p>
 * Copyright: Copyright (c) 2002 Company: SAP AG
 * 
 * @author BPL - Tools
 * @version 1.0
 */


public class Header implements Serializable {

  private String tag;
  private Properties params = new Properties();

  /**
   * Constructor
   * 
   * @param tag
   *          the header tag
   */
  public Header(String tag) {
    this.tag = tag;
  }

  /**
   * Returns the header tag
   * 
   * @return tag
   */
  public String getTag() {
    return tag;
  }

  /**
   * Returns the value of the parameter with a given name (or null)
   * 
   * @param string
   *          parameter name
   * @return parameter value
   */
  public String getParameter(String string) {
    return params.getProperty(string);
  }

  /**
   * Sets the value of a given parameter. If the parameter is new, it is added
   * to the list
   * 
   * @param name
   *          parameter name
   * @param value
   *          parameter value
   */
  public void setParameter(String name, String value) {
    if (value == null)
      value = "<null>";
    params.setProperty(name, value);
  }



  /**
   * Compares two headers
   * 
   * @return true if tags, parameter names, and parameter values are
   *         respectively equal
   */
  public boolean equals(Object obj) {
    if (!(obj instanceof Header))
      return false;
    Header header = (Header) obj;
    return header.tag.equals(tag) && header.params.equals(params);
  }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
    int hash = 0;
    Set paramSet = params.entrySet();
    for (Iterator iter = paramSet.iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
      hash += ((String)entry.getKey()).hashCode() + 31 * ((String) entry.getValue()).hashCode();
		}
		return hash + 29 * tag.hashCode();
	}
    
  /**
   * Returns all parameters
   */
  public Properties getParameters() {
    return params;
  }

}