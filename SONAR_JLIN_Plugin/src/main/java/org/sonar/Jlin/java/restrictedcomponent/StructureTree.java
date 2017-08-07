package org.sonar.Jlin.java.restrictedcomponent;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;



/**
 * generic XML structure.
 *
 * A StructureTree instance contains
 * <p> - a Header header
 * <p> - a String text
 * <p> - any number of StructureTree children
 * <p> - a pointer to its parent
 *
 * <p>
 * Copyright:    Copyright (c) 2002
 * Company:      SAP AG
 * @author       BPL - Tools
 * @version 1.0
 */

public class StructureTree implements  Serializable {

	static public final String MACRO_DELIMITER = "@"; //$NON-NLS-1$

	private Header header;
	private StructureTree[] children;
	private StructureTree parent;
	private String text;

	public StructureTree(Header header) {
		this.header = header;
		children = new StructureTree[0];
		parent = null;
	}

	/**
	 * Returns top header
	 */
	public Header getHeader() {
		return header;
	}

	/**
	 * Returns parent tree (or null)
	 */
	public StructureTree getParent() {
		return parent;
	}

	/**
	 * Returns free text (can be null)
	 */
	public String getText() {
		if (text != null) {
			return text.trim();
		} else {
			return null;
		}
	}

	/**
	 * Sets free text
	 * @param text free text
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Adds child tree with a given header (if not yet present)
	 * @param header header to be added
	 * @return newly created structure tree, or old tree with the given header
	 */
	public StructureTree addChild(Header header) {
		StructureTree tempTree = null;
		if ((tempTree = findChild(header, null)) == null) {
			tempTree = new StructureTree(header);
			addNewChild(tempTree);
		}
		return tempTree;
	}

	/**
	 * Adds new child tree with a given header (even if a child with the same header is already present)
	 * @param header header to be added
	 * @return newly created structure tree
	 */
	public StructureTree addNewChild(Header header) {
		StructureTree tempTree = new StructureTree(header);
		addNewChild(tempTree);
		return tempTree;
	}

	/**
	 * Adds child tree with given header and free text (if not yet present)
	 * @param header header to be added
	 * @param t1 header's free text
	 * @return newly created structure tree, or old tree with the given header and text
	 */
	public StructureTree addChild(Header header, String t1) {
		StructureTree tempTree = null;
		if ((tempTree = findChild(header, t1)) == null) {
			tempTree = new StructureTree(header);
			tempTree.setText(t1);
			addNewChild(tempTree);
		}
		return tempTree;
	}

	/**
	 * Adds child tree with given header and free text (even if a child with the same header and text is already present)
	 * @param header header to be added
	 * @param t1 header's free text
	 * @return newly created structure tree
	 */
	public StructureTree addNewChild(Header header, String t1) {
		StructureTree tempTree = new StructureTree(header);
		tempTree.setText(t1);
		addNewChild(tempTree);
		return tempTree;
	}

	/**
	 * Adds given  tree to the child list. If a given header is already present.
	 * only its children are added (recursively)
	 * @param tree tree to be added
	 * @return added tree, or old tree if the top header was already present
	 */
	public StructureTree addChild(StructureTree tree) {
		if (tree == null)
			return null;
		StructureTree tempTree = null;
		if ((tempTree = findChild(tree.getHeader(), tree.getText())) == null) {
			addNewChild(tree);
			return tree;
		} else {
			for (int i = 0; i < tree.children.length; i++) {
				tempTree.addChild(tree.children[i]);
			}
			return tempTree;
		}
	}

	/**
	 * Returns all childreni
	 *
	 */
	public StructureTree[] getChildren() {
		return children;
	}

	/**
	 * Returns child with given header and free text (or null)
	 * @param inHeader header
	 * @param text free text
	 */
	public StructureTree findChild(Header inHeader, String text) {
		if (inHeader == null)
			return null;
		for (int i = 0; i < children.length; i++) {
			if ((inHeader.equals(children[i].header))
				&& (((text == null) && (children[i].getText() == null))
					|| ((children[i].getText() != null)
						&& (children[i].getText().equals(text))))) {
				return children[i];
			}
		}
		return null;
	}

	/**
	 * Removes all children with a given header
	 * @param inHeader header
	 */
	public void removeChildren(Header inHeader) {
		if (inHeader == null)
			return;
		for (int i = 0; i < children.length; i++) {
			if (inHeader.equals(children[i].header)) {
				StructureTree[] tempChildren =
					new StructureTree[children.length - 1];
				for (int j = 0; j < i; j++)
					tempChildren[j] = children[j];
				for (int j = i + 1; j < children.length; j++)
					tempChildren[j - 1] = children[j];
				children = tempChildren;
			}
		}
	}

	/**
	 * Removes all children with a given tag
	 * @param tag
	 */
	public void removeChildren(String tag) {
		if (tag == null)
			return;
		boolean[] remove = new boolean[children.length];
		int numberOfRemovals = 0;
		for (int i = 0; i < children.length; i++) {
			if (tag.equals(children[i].getTag())) {
				remove[i] = true;
				numberOfRemovals++;
			} else
				remove[i] = false;
		}
		StructureTree[] tempChildren =
			new StructureTree[children.length - numberOfRemovals];
		int currentIndex = 0;
		for (int i = 0; i < children.length; i++) {
			if (!remove[i]) {
				tempChildren[currentIndex] = children[i];
				currentIndex++;
			}
		}
		children = tempChildren;
	}

	/**
	 * Adds given tree to the child list.
	 * @param tree tree to be added
	 * @return added tree
	 */
	public StructureTree addNewChild(StructureTree tree) {
		StructureTree[] temp = new StructureTree[children.length + 1];
		for (int i = 0; i < children.length; i++)
			temp[i] = children[i];
		temp[children.length] = tree;
		children = temp;
		tree.parent = this;
		return tree;
	}

	/**
	 * Returns all children with a given tag
	 * @param tag
	 * @return found children
	 */
	public StructureTree[] getChildren(String tag) {
		boolean[] good = new boolean[children.length];
		int found = 0;
		for (int i = 0; i < children.length; i++) {
			if (children[i].getTag().equals(tag)) {
				good[i] = true;
				found++;
			} else {
				good[i] = false;
			}
		}
		StructureTree[] goodArray = new StructureTree[found];
		int currentIndex = 0;
		for (int i = 0; i < children.length; i++) {
			if (good[i]) {
				goodArray[currentIndex] = children[i];
				currentIndex++;
			}
		}
		return goodArray;
	}

	/**
	 * Returns first child with a given tag (or null)
	 * @param tag
	 * @return found child
	 */
	public StructureTree getOnlyChild(String tag) {
		for (int i = 0; i < children.length; i++) {
			if (children[i].getTag().equals(tag))
				return children[i];
		}
		return null;
	}

	/**
	 * Returns object itself
	 */
	public StructureTree toStructureTree() {
		return this;
	}

	/**
	 * Returns value of the specified header parameter
	 * @parameter name parameter name
	 * @return parameter value (null if parameter does not exist)
	 */
	public String getParameter(String name) {
		return header.getParameter(name);
	}

	/**
	 * Returns all header parameters
	 */

	public Properties getParameters() {
		return header.getParameters();
	}

	/**
	 * Returns header tag
	 */
	public String getTag() {
		return header.getTag();
	}

	private String resolveMacros(String string, Properties macros) {
		if (string == null)
			return null;
		Enumeration keyEnum = macros.propertyNames();
		if (!keyEnum.hasMoreElements())
			return string; //no macros are defined
		StringTokenizer st = new StringTokenizer(string, MACRO_DELIMITER, true);
		if (st.countTokens() < 3)
			//we're looking for [...]@...@[...] => at least 3 tokens
			return string;
		String out = string;
		while (keyEnum.hasMoreElements()) {
			String currentBare = (String) keyEnum.nextElement();
			String currentPattern =
				MACRO_DELIMITER + currentBare + MACRO_DELIMITER;
			String currentValue = macros.getProperty(currentBare);
			if (out.length() < currentPattern.length())
				continue;
			for (int i = 0; i <= out.length() - currentPattern.length(); i++) {
				if (out.substring(i).startsWith(currentPattern)) {
					out =
						out.substring(0, i)
							+ currentValue
							+ out.substring(
								i + currentPattern.length(),
								out.length());
					continue;
				}
			}
		}

		return out;
	}

	/**
	 * For each property name = value, the string  '@name@' is replaced
	 * by 'value' in all structure tree parameters (including those of the 
	 * children trees)
	 * 
	 * @return tree with resolved macros
	 */
	public StructureTree resolveMacros(Properties macros) {
		setText(resolveMacros(getText(), macros));
		Properties pars = header.getParameters();
		Enumeration keys = pars.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String value = pars.getProperty(key);
			header.setParameter(key, resolveMacros(value, macros));
		}
		for (int i = 0; i < children.length; i++) {
			children[i].resolveMacros(macros);
		}
		macros = new Properties();
		return this;
	}

}