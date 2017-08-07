package org.sonar.Jlin.java.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Properties;

import javax.management.modelmbean.XMLParseException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.sonar.Jlin.java.restrictedcomponent.Header;
import org.sonar.Jlin.java.restrictedcomponent.StructureTree;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;



/**
 * Some XML helper methods.
 */

public class XMLTool {

  /**
   * Default doc builder precedence order: JDK 1.5 xerces, JDK 1.4 crimson
   */
  private static final String[] DOC_BUILDERS = {
      "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl",
      "org.apache.crimson.jaxp.DocumentBuilderFactoryImpl" };

  /**
   * Default transformer precedence order: JDK 1.5 xerces, JDK 1.4 crimson
   */
  private static final String[] SAX_PARSERS = {
      "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl",
      "org.apache.crimson.jaxp.SAXParserFactoryImpl" };

  /**
   * Default transformer precedence order: JDK 1.5 xalan, JDK 1.4 xalan
   */
  private static final String[] TRANSFORMERS = {
      "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl",
      "org.apache.xalan.processor.TransformerFactoryImpl" };

  private static final DocumentBuilder docBuilder;
  private static final Transformer serializer;

  /**
   * xalan identity transformer XSL which indents by 2 spaces. Have to do this
   * explicity with an XSL because TransformerFactory.newTransformer() is buggy.
   */
  private static final String XALAN_IDENTITY_INDENT_XSL = "<xsl:stylesheet version=\"1.0\""
      + " xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\""
      + " xmlns:xalan=\"http://xml.apache.org/xalan\""
      + ">"
      + "<xsl:output xalan:indent-amount=\"2\"/> "
      + "<xsl:template match=\"node()|@*\">"
      + "<xsl:copy>"
      + "<xsl:apply-templates select=\"node()|@*\"/>"
      + "</xsl:copy>"
      + "</xsl:template>" + "</xsl:stylesheet>";

  static {
    try {
      docBuilder = getDocBuilderFactory().newDocumentBuilder();
      TransformerFactory transfomerFactory = getTransfomerFactory();
      // in case of xalan, we can indent the output
      if (transfomerFactory.getClass().getName().indexOf("xalan") != -1) {
        serializer = transfomerFactory.newTransformer(new StreamSource(
            new StringReader(XALAN_IDENTITY_INDENT_XSL)));
      } else {
        serializer = transfomerFactory.newTransformer();
      }
      serializer.setOutputProperty(OutputKeys.METHOD, "xml");
      serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      serializer.setOutputProperty(OutputKeys.INDENT, "yes");
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    } catch (TransformerConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Parser XML -> StructureTree
   * 
   * @param reader
   *          XML source.
   * @return equivalent representation as StructureTree.
   * @deprecated use {@link #parseStream(InputStream)} .
   */
  static public StructureTree parseReader(java.io.Reader reader)
      throws IOException, XMLParseException {
    try {
      Document doc;
      synchronized (docBuilder) {
        doc = docBuilder.parse(new InputSource(reader));
      }
      return wrapDOM(doc.getDocumentElement());
    } catch (Exception e) {
      throw new XMLParseException();
    }
  }

  static public StructureTree parseStream(InputStream stream)
      throws IOException, XMLParseException {
    try {
      Document doc;
      synchronized (docBuilder) {
        doc = docBuilder.parse(stream);
      }
      return wrapDOM(doc.getDocumentElement());
    } catch (Exception e) {
      throw new XMLParseException();
    }
  }

  static public StructureTree parseStreamAndClose(InputStream stream)
      throws IOException, XMLParseException {
    try {
      return parseStream(stream);
    } finally {
      if (stream != null) {
        stream.close();
      }
    }
  }
  
  static public StructureTree wrapDOM(Element node) {
    if (node == null)
      return null;
    String tag = node.getNodeName();
    Header header = new Header(tag);
    NamedNodeMap nnm = node.getAttributes();
    if (nnm != null) {
      for (int i = 0; i < nnm.getLength(); i++) {
        Node currentAttr = nnm.item(i);
        header.setParameter(currentAttr.getNodeName(), currentAttr
            .getNodeValue());
      }
    }
    StructureTree st = new StructureTree(header);
    StringBuilder textBuilder = new StringBuilder();
    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node currentNode = children.item(i);
      short nodeType = currentNode.getNodeType();
      if (nodeType == Node.ELEMENT_NODE) {
        st.addNewChild(wrapDOM((Element) currentNode));
      } else if ((nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE)) {
        textBuilder.append(currentNode.getNodeValue());
      }
    }
    st.setText(textBuilder.toString());
    return st;
  }

  static public Document unWrapDOM(StructureTree st) {
    if (st == null)
      return null;
    Document doc = docBuilder.newDocument();
    Element el = structureTree2Element(st, doc);
    doc.appendChild(el);
    return doc;
  }

  static private Element structureTree2Element(StructureTree st, Document doc) {

    Element el = doc.createElement(st.getTag());

    Properties params = st.getParameters();
    Enumeration keys = params.keys();
    while (keys.hasMoreElements()) {
      String key = (String) keys.nextElement();
      el.setAttribute(key, params.getProperty(key));
    }
    String text = st.getText();
    if (text != null) {
      Text t = doc.createTextNode(text);
      el.appendChild(t);
    }
    StructureTree[] children = st.getChildren();
    for (int i = 0; i < children.length; i++) {
      el.appendChild(structureTree2Element(children[i], doc));
    }
    return el;
  }

  /**
   * Unparser StructureTree -> XML (generic stream)
   * 
   * @param tree
   *          source.
   * @param stream
   *          {@link OutputStream} instance the XML document is written to
   */
  static public void writeDocument(StructureTree tree, OutputStream stream)
      throws IOException {
    if (tree == null)
      return;
    Document doc = unWrapDOM(tree);
    DOMSource domSource = new DOMSource(doc);
    StreamResult streamResult = new StreamResult(stream);
    try {
      synchronized (serializer) {
        serializer.transform(domSource, streamResult);
      }
    } catch (TransformerException e) {
      IOException ioe = new IOException();
      ioe.initCause(e);
      throw ioe;
    }
  }

  /**
   * Unparser StructureTree -> XML (generic writer)
   * 
   * @param tree
   *          source.
   * @param writer
   *          java.io.Writer instance the XML document is written to
   * @deprecated this method does not handle encoding correctly. Use
   *             {@link #writeDocument(StructureTree, OutputStream)} instead.
   */
  static public void writeDocument(StructureTree tree, java.io.Writer writer)
      throws IOException {
    if (tree == null)
      return;
    Document doc = unWrapDOM(tree);
    DOMSource domSource = new DOMSource(doc);
    StreamResult streamResult = new StreamResult(writer);
    try {
      synchronized (serializer) {
        serializer.transform(domSource, streamResult);
      }
    } catch (TransformerException e) {
      IOException ioe = new IOException();
      ioe.initCause(e);
      throw ioe;
    }
  }

  /**
   * Unparser StructureTree -> XML (string variant)
   * 
   * @param tree
   *          source.
   * @return string containing the XML document.
   */
  static public String toString(StructureTree tree) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      writeDocument(tree, baos);
    } catch (IOException e) {
      // $JL-EXC$ can't happen for byte array
    }
    try {
      return new String(baos.toByteArray(), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public static DocumentBuilderFactory getDocBuilderFactory() {
    return getDocBuilderFactory(DOC_BUILDERS);
  }

  public static DocumentBuilderFactory getDocBuilderFactory(
      String[] factoryClasses) {
    DocumentBuilderFactory factory = (DocumentBuilderFactory) instantiate(factoryClasses);
    if (factory != null) {
      return factory;
    }
    return DocumentBuilderFactory.newInstance();
  }

  public static SAXParserFactory getSAXParserFactory() {
    return getSAXParserFactory(SAX_PARSERS);
  }

  public static SAXParserFactory getSAXParserFactory(String[] factoryClasses) {
    SAXParserFactory factory = (SAXParserFactory) instantiate(factoryClasses);
    if (factory != null) {
      return factory;
    }
    return SAXParserFactory.newInstance();
  }

  public static TransformerFactory getTransfomerFactory() {
    return getTransfomerFactory(TRANSFORMERS);
  }

  public static TransformerFactory getTransfomerFactory(String[] factoryClasses) {
    TransformerFactory factory = (TransformerFactory) instantiate(factoryClasses);
    if (factory != null) {
      return factory;
    }
    return TransformerFactory.newInstance();
  }

  private static Object instantiate(String[] classNames) {
    Object obj = null;
    for (int i = 0; i < classNames.length && obj == null; i++) {
      try {
        obj = Class.forName(classNames[i]).newInstance();
      } catch (Throwable t) {
        // $JL-EXC$ try next
      }
    }
    return obj;
  }

}