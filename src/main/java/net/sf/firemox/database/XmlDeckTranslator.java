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
package net.sf.firemox.database;

import java.io.IOException;
import java.util.Iterator;

import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.xml.XmlParser;

import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public class XmlDeckTranslator {

	/**
	 * Constructor.
	 * 
	 * @exception SAXException
	 * @exception IOException
	 */
	public XmlDeckTranslator() throws SAXException, IOException {
		if (parser == null) {
			parser = new XmlParser();
		}
		synchronized (parser) {
			config = parser.parse(MToolKit.getTbsUrl("decks/deck-translation.xml")
					.getPath());
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
		final Iterator<?> it = config.iterator();
		String cardNameRec = cardName;
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof XmlParser.Node) {
				try {
					cardNameRec = (String) XmlDeckTranslator.class.getMethod(
							"CONVERT_" + ((XmlParser.Node) obj).getTag(),
							XmlParser.Node.class, String.class)
							.invoke(this, obj, cardNameRec);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return cardNameRec;
	}

	/**
	 * Return the converted card name.
	 * 
	 * @param node
	 *          the node containing the regular expressions converting the given
	 *          string.
	 * @param cardName
	 *          the card name to translate.
	 * @return the converted card name.
	 */
	public String convert(XmlParser.Node node, String cardName) {
		return cardName.replaceAll(node.getAttribute("this"), node
				.getAttribute("by"));
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
