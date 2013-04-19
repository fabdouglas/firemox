/*
 * Created on Dec 29, 2004
 * 
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

import java.io.IOException;

import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.MToolKit;

import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class XmlDeckTranslator {

	/**
	 * Constructor.
	 * 
	 * @param tbsName
	 *          An input stream containing a complete e.g. configuration file
	 * @exception SAXException
	 * @exception IOException
	 */
	public XmlDeckTranslator(String tbsName) throws SAXException, IOException {
		if (parser == null) {
			parser = new XmlParser();
		}
		synchronized (parser) {
			String ori = IdConst.TBS_DIR + "/" + tbsName + "/deck-translation.xml";
			config = parser.parse(MToolKit.getResourceAsStream(ori));
		}
	}

	/**
	 * Return the translated card name of specified original card name.
	 * 
	 * @param cardName
	 *          the name to translate.
	 * @return the translated card name
	 */
	public String convert(String cardName) {
		String cardNameRec = cardName;
		for (Object obj : config) {
			if (obj instanceof XmlParser.Node) {
				try {
					cardNameRec = (String) XmlDeckTranslator.class.getMethod(
							((XmlParser.Node) obj).getTag(), XmlParser.Node.class,
							String.class).invoke(this, obj, cardNameRec);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return cardNameRec;
	}

	/**
	 * @param node
	 *          the node containing the replacement expressions.
	 * @param cardName
	 *          the unconverted card name.
	 * @return the converted name.
	 */
	public String replace(XmlParser.Node node, String cardName) {
		return cardName.replace(node.getAttribute("this"), node.getAttribute("by"));
	}

	/**
	 * 
	 */
	private static XmlParser parser;

	/**
	 * 
	 */
	private static XmlParser.Node config;
}
