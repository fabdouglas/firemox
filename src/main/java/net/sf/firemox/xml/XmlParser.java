/*
 *   Firemox is a turn based strategy simulator
 *   Copyright (C) 2003-2007 Fabrice Daugan
 *
 *   This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 *   This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details.
 *
 *   You should have received a copy of the GNU General Public License along  
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sf.firemox.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.MToolKit;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XML Parser wrapper. This class wraps any standard JAXP1.1 parser with
 * convenient error and entity handlers and a mini dom-like document tree.
 * <P>
 * By default, the parser is created as a validating parser. This can be changed
 * by setting the "org.mortbay.xml.XmlParser.NotValidating" system property to
 * true.
 * 
 * @version $Id$
 * @author Greg Wilkins (gregw)
 */
public class XmlParser {

	/**
	 * Constructor.
	 * 
	 * @throws SAXException
	 */
	public XmlParser() throws SAXException {
		this(false);
	}

	/**
	 * Constructor.
	 * 
	 * @param validation
	 *          validation flag.
	 * @throws SAXException
	 */
	public XmlParser(boolean validation) throws SAXException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setValidating(validation);
			parser = factory.newSAXParser();
			if (validation) {
				parser.setProperty(JAXP_SCHEMA_LANGUAGE,
						XMLConstants.W3C_XML_SCHEMA_NS_URI);
				parser.setProperty(JAXP_SCHEMA_SOURCE, MToolKit.getFile(MP_XML_SCHEMA));
			}
		} catch (Exception e) {
			throw new SAXException(e.toString());
		}
	}

	/**
	 * @param source
	 *          the document source.
	 * @return the node of document.
	 * @throws IOException
	 *           error while opening the stream.
	 * @throws SAXException
	 *           error while parsing.
	 */
	public synchronized Node parse(InputSource source) throws IOException,
			SAXException {
		Handler handler = new Handler();
		XMLReader reader = parser.getXMLReader();
		reader.setContentHandler(handler);
		reader.setErrorHandler(handler);
		reader.setEntityResolver(handler);
		parser.parse(source, handler);
		if (handler.error != null) {
			throw handler.error;
		}
		Node doc = (Node) handler.top.get(0);
		handler.clear();
		return doc;
	}

	/**
	 * Parse string URL.
	 * 
	 * @param url
	 *          the document source.
	 * @return the node of document.
	 * @throws IOException
	 *           error while opening the stream.
	 * @throws SAXException
	 *           error while parsing.
	 */
	public synchronized Node parse(String url) throws IOException, SAXException {
		return parse(new InputSource(url));
	}

	/**
	 * Parse InputStream.
	 * 
	 * @param in
	 *          the document source.
	 * @return the node of document.
	 * @throws IOException
	 *           error while opening the stream.
	 * @throws SAXException
	 *           error while parsing.
	 */
	public synchronized Node parse(InputStream in) throws IOException,
			SAXException {
		Handler handler = new Handler();
		XMLReader reader = parser.getXMLReader();
		reader.setContentHandler(handler);
		reader.setErrorHandler(handler);
		reader.setEntityResolver(handler);
		parser.parse(new InputSource(in), handler);
		if (handler.error != null) {
			throw handler.error;
		}
		Node doc = (Node) handler.top.get(0);
		handler.clear();
		return doc;
	}

	/**
	 * The Default handler
	 * 
	 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan
	 *         </a>
	 */
	class Handler extends DefaultHandler {

		Node top = new Node(null, null, null);

		SAXParseException error;

		@Override
		public InputSource resolveEntity(String publicId, String systemId) {
			if (systemId.endsWith(".xsd")) {
				String xsdFile;
				try {
					xsdFile = IdConst.TBS_DIR + "/"
							+ new File(new URI(systemId)).getName();
				} catch (URISyntaxException e) {
					return null;
				}
				return new InputSource(xsdFile);
			}
			return null;
		}

		private Node context = top;

		void clear() {
			top = null;
			error = null;
			context = null;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attrs) throws SAXException {
			String name = uri == null || uri.length() == 0 ? qName : localName;
			Node node = new Node(context, name, attrs);
			context.add(node);
			context = node;
			ContentHandler observer = null;
			observers.push(observer);
			for (ContentHandler handler : observers) {
				if (handler != null) {
					handler.startElement(uri, localName, qName, attrs);
				}
			}
		}

		@Override
		public void notationDecl(String name, String publicId, String systemId) {
			System.out.println("name:" + name + ",publicId:" + publicId
					+ ",systemId:" + systemId);
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			context = context.aParent;
			for (ContentHandler handler : observers) {
				if (handler != null) {
					handler.endElement(uri, localName, qName);
				}
			}
			observers.pop();
		}

		@Override
		public void ignorableWhitespace(char[] buf, int offset, int len)
				throws SAXException {
			for (ContentHandler handler : observers) {
				if (handler != null) {
					handler.ignorableWhitespace(buf, offset, len);
				}
			}
		}

		@Override
		public void characters(char[] buf, int offset, int len) throws SAXException {
			context.add(new String(buf, offset, len));
			for (int i = 0; i < observers.size(); i++) {
				if (observers.get(i) != null) {
					observers.get(i).characters(buf, offset, len);
				}
			}
		}

		@Override
		public void warning(SAXParseException ex) {
			XmlConfiguration.warning("@" + getLocationString(ex) + " : "
					+ ex.toString());
		}

		@Override
		public void error(SAXParseException ex) {
			// Save error and continue to report other errors
			if (error == null) {
				error = ex;
			}
			XmlConfiguration.error("@" + getLocationString(ex) + " : "
					+ ex.getMessage());
		}

		@Override
		public void fatalError(SAXParseException ex) {
			error = ex;
			XmlConfiguration.fatal("@" + getLocationString(ex) + " : "
					+ ex.getMessage());
		}

		private String getLocationString(SAXParseException ex) {
			return "line:" + ex.getLineNumber() + " col:" + ex.getColumnNumber();
		}

	}

	/**
	 * XML Attribute.
	 */
	public static class Attribute {

		private String aName;

		private String aValue;

		/**
		 * Create a new instance of this class.
		 * 
		 * @param n
		 *          attribute name
		 * @param v
		 *          attribute value
		 */
		public Attribute(String n, String v) {
			aName = n;
			aValue = v;
		}

		/**
		 * @return name
		 */
		public String getName() {
			return aName;
		}

		/**
		 * Set the attribute value.
		 * 
		 * @param value
		 *          the attrivute value.
		 */
		public void setValue(String value) {
			this.aValue = value;
		}

		/**
		 * @return value
		 */
		public String getValue() {
			return aValue;
		}
	}

	/* ------------------------------------------------------------ */
	/* ------------------------------------------------------------ */
	/**
	 * XML Node. Represents an XML element with optional attributes and ordered
	 * content.
	 */
	public static class Node extends AbstractList<Object> {

		Node aParent;

		/**
		 * Nodes
		 */
		public List<Object> aList;

		/**
		 * Tag name.
		 */
		public String aTag;

		/**
		 * Attributes
		 */
		public Attribute[] aAttrs;

		/**
		 * Is the last object is a string.
		 */
		public boolean aLastString = false;

		/**
		 * Create a new instance of this class.
		 * 
		 * @param parent
		 *          parent node.
		 * @param tag
		 *          tag name.
		 * @param attrs
		 *          attributes
		 */
		public Node(Node parent, String tag, Attributes attrs) {
			aParent = parent;
			aTag = tag;
			if (attrs != null) {
				aAttrs = new Attribute[attrs.getLength()];
				for (int i = 0; i < attrs.getLength(); i++) {
					String name = attrs.getQName(i);
					if (name == null || name.length() == 0) {
						name = attrs.getQName(i);
					}
					aAttrs[i] = new Attribute(name, attrs.getValue(i));
				}
			}
		}

		/**
		 * @param attr
		 *          attribute to add.
		 */
		public void addFirstAttribute(Attribute attr) {
			Attribute[] attrs = new Attribute[this.aAttrs.length + 1];
			System.arraycopy(this.aAttrs, 0, attrs, 1, this.aAttrs.length);
			attrs[0] = attr;
			this.aAttrs = attrs;
		}

		/**
		 * Remove the first attribute with specified name.
		 * 
		 * @param attributeName
		 *          the attribute to remove.
		 */
		public void removeAttribute(String attributeName) {
			for (int i = 0; i < this.aAttrs.length; i++) {
				if (this.aAttrs[i].getName().equals(attributeName)) {
					final Attribute[] attrs = new Attribute[this.aAttrs.length - 1];
					if (attrs.length == 0) {
						this.aAttrs = attrs;
					} else {
						if (i == 0) {
							System.arraycopy(this.aAttrs, 1, attrs, 0, attrs.length);
						} else {
							System.arraycopy(this.aAttrs, 0, attrs, 0, i);
							if (i != attrs.length) {
								System
										.arraycopy(this.aAttrs, i + 1, attrs, i, attrs.length - i);
							}
						}
						this.aAttrs = attrs;
					}
					return;
				}
			}
		}

		/**
		 * @param attr
		 *          attribute to add.
		 */
		public void addAttribute(Attribute attr) {
			for (Attribute attribute : aAttrs) {
				if (attr.getName().equals(attribute.getName())) {
					attribute.setValue(attr.getValue());
					return;
				}
			}
			final Attribute[] aAttrs = new Attribute[this.aAttrs.length + 1];
			System.arraycopy(this.aAttrs, 0, aAttrs, 0, this.aAttrs.length);
			aAttrs[this.aAttrs.length] = attr;
			this.aAttrs = aAttrs;
		}

		/**
		 * @return parent
		 */
		public Node getParent() {
			return aParent;
		}

		/**
		 * @return tag
		 */
		public String getTag() {
			return aTag;
		}

		/**
		 * Get an element attribute.
		 * 
		 * @param name
		 *          the attribute name.
		 * @return attribute or null.
		 */
		public String getAttribute(String name) {
			return getAttribute(name, null);
		}

		/**
		 * Get an element attribute.
		 * 
		 * @param name
		 *          the attribute name.
		 * @param dft
		 *          the default value.
		 * @return attribute or null.
		 */
		public String getAttribute(String name, String dft) {
			if (aAttrs == null || name == null) {
				return dft;
			}
			for (Attribute attribute : aAttrs) {
				if (name.equals(attribute.getName())) {
					return attribute.getValue();
				}
			}
			return dft;
		}

		/**
		 * Get the number of children nodes.
		 * 
		 * @return size
		 */
		@Override
		public int size() {
			if (aList != null) {
				return aList.size();
			}
			return 0;
		}

		/**
		 * Get the number of non text children nodes.
		 * 
		 * @return the number of non text children nodes.
		 */
		public int getNbNodes() {
			if (aList == null)
				return 0;
			int count = 0;
			for (Object obj : aList) {
				if (obj instanceof Node) {
					count++;
				}
			}
			return count;
		}

		/**
		 * Get the ith child node or content.
		 * 
		 * @param i
		 *          index of node.
		 * @return Node or String.
		 */
		@Override
		public Object get(int i) {
			if (aList != null) {
				return aList.get(i);
			}
			return null;
		}

		/**
		 * Get the first child node with the tag.
		 * 
		 * @param tag
		 *          the tag name of node.
		 * @return Node or null.
		 */
		public Node get(String tag) {
			if (aList != null) {
				for (Object o : aList) {
					if (o instanceof Node) {
						Node n = (Node) o;
						if (tag.equals(n.aTag)) {
							return n;
						}
					}
				}
			}
			return null;
		}

		/**
		 * Get the child nodes with the tag.
		 * 
		 * @param tag
		 *          the tag name of node.
		 * @return list of nodes or null.
		 */
		public List<Node> getNodes(String tag) {
			final List<Node> list = new ArrayList<Node>();
			if (aList != null) {
				for (Object o : aList) {
					if (o instanceof Node) {
						Node n = (Node) o;
						if (tag.equals(n.aTag)) {
							list.add(n);
						}
					}
				}
			}
			return list;
		}

		@Override
		public void add(int i, Object o) {
			if (aList == null) {
				aList = new ArrayList<Object>();
			}
			if (o instanceof String) {
				if (aLastString) {
					int last = aList.size() - 1;
					aList.set(last, (String) aList.get(last) + o);
				} else {
					aList.add(i, o);
				}
				aLastString = true;
			} else {
				aLastString = false;
				aList.add(i, o);
			}
		}

		/**
		 * Inserts the specified element at the specified position in this list
		 * (optional operation). Shifts the element currently at that position (if
		 * any) and any subsequent elements to the right (adds one to their
		 * indices).
		 * <p>
		 * This implementation always throws an UnsupportedOperationException.
		 * 
		 * @param o
		 *          element to be inserted.
		 */
		public void removeElement(XmlParser.Node o) {
			aLastString = false;
			aList.remove(o);
		}

		/**
		 * Removes the element at the specified position in this list (optional
		 * operation). Shifts any subsequent elements to the left (subtracts one
		 * from their indices). Returns the element that was removed from the list.
		 * 
		 * @param index
		 *          the index of the element to removed.
		 */
		public void removeElement(int index) {
			aLastString = false;
			aList.remove(index);
		}

		/**
		 * Replaces the element at the specified position in this list with the
		 * specified element (optional operation).
		 * 
		 * @param index
		 *          index of element to replace.
		 * @param element
		 *          element to be stored at the specified position.
		 */
		public void setElement(int index, Object element) {
			aList.set(index, element);
		}

		@Override
		public void clear() {
			if (aList != null) {
				aList.clear();
			}
			aList = null;
		}

		/**
		 * Print this node using the specified printer.
		 * 
		 * @param printer
		 *          used printer to add this node.
		 */
		public synchronized void print(PrintWriter printer) {
			for (Attribute attribute : aAttrs) {
				if ("xsi:schemaLocation".equals(attribute.getName())) {
					XmlParser.Attribute[] attributes = new XmlParser.Attribute[aAttrs.length + 2];
					System.arraycopy(aAttrs, 0, attributes, 2, aAttrs.length);
					attributes[0] = new XmlParser.Attribute("xmlns:xsi",
							XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
					attributes[1] = new XmlParser.Attribute("xmlns", attribute.getValue()
							.split(" ")[0]);
					aAttrs = attributes;
					break;
				}
			}
			print(printer, "");
		}

		@Override
		public synchronized String toString() {
			return toString("");
		}

		@Override
		public boolean equals(Object obj) {
			return this == obj;
		}

		@Override
		public int hashCode() {
			return aTag.hashCode();
		}

		/**
		 * Convert to a string.
		 * 
		 * @param tag
		 *          If false, only content is shown.
		 * @return string
		 */
		private String toString(String ident) {
			StringBuilder buf = new StringBuilder();
			toString(buf, ident);
			return buf.toString();
		}

		private void print(PrintWriter printer, String ident) {
			printer.print(ident);
			printer.print("<");
			printer.print(aTag);
			if (aAttrs != null) {
				for (Attribute attribute : aAttrs) {
					printer.print(' ');
					printer.print(attribute.getName());
					printer.print("=\"");
					printer.print(normalize(attribute.getValue()));
					printer.print("\"");
				}
			}
			if (aList != null) {
				printer.print(">");
				printer.println();
				for (Object o : aList) {
					if (o == null) {
						continue;
					}
					if (o instanceof Node) {
						((Node) o).print(printer, ident + "\t");
					} else {
						printer.print(normalize(StringUtils.trimToEmpty(o.toString())));
					}
				}
				printer.print(ident);
				printer.print("</");
				printer.print(aTag);
				printer.print(">");
			} else {
				printer.print("/>");
			}
			printer.println();
		}

		private void toString(StringBuilder buf, String ident) {
			buf.append(ident);
			buf.append("<");
			buf.append(aTag);
			if (aAttrs != null) {
				for (Attribute attribute : aAttrs) {
					buf.append(' ');
					buf.append(attribute.getName());
					buf.append("=\"");
					buf.append(normalize(attribute.getValue()));
					buf.append("\"");
				}
			}
			if (aList != null) {
				buf.append(">\n");
				for (Object o : aList) {
					if (o == null) {
						continue;
					}
					if (o instanceof Node) {
						((Node) o).toString(buf, ident + "\t");
					} else {
						buf.append(normalize(StringUtils.trimToEmpty(o.toString())));
					}
				}
				buf.append(ident);
				buf.append("</");
				buf.append(aTag);
				buf.append(">\n");
			} else {
				buf.append("/>\n");
			}
		}
	}

	/**
	 * Return the given text as XML string.
	 * 
	 * @param text
	 *          the text containing non serializable tags.
	 * @return the given text as XML string.
	 */
	public static String normalize(String text) {
		return text.replace("&", "`").replace("&", "&amp;").replace("'", "&#39;")
				.replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}

	/**
	 * The SAX parser
	 */
	private SAXParser parser;

	Stack<ContentHandler> observers = new Stack<ContentHandler>();

	static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

	static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

	static final String MP_XML_SCHEMA = IdConst.TBS_DIR + "/" + IdConst.TBS_XSD;
}