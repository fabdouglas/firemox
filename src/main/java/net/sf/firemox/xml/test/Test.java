/*
 * Created on 27 févr. 2005
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
package net.sf.firemox.xml.test;

import java.io.OutputStream;
import java.util.Iterator;

import net.sf.firemox.test.IdTest;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.xml.XmlConfiguration;
import net.sf.firemox.xml.XmlParser;
import net.sf.firemox.xml.XmlTest;
import net.sf.firemox.xml.XmlToMDB;
import net.sf.firemox.xml.tbs.Tbs;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class Test implements XmlToMDB {

	/**
	 * Write the contained test of the specified node <br>
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>test identifier [1]</li>
	 * <li>tested on identifier [1]</li>
	 * <li>depending on the test [...]</li>
	 * </ul>
	 * 
	 * @param node
	 *          the XML test container structure
	 * @param out
	 *          output stream where the card structure will be saved
	 * @return the amount of written action, so return always ZERO.
	 * @see net.sf.firemox.test.IdTest
	 */
	public final int buildMdb(XmlParser.Node node, OutputStream out) {
		try {
			if (node == null) {
				// write the null test
				IdTest.TRUE.serialize(out);
			} else {
				String referenceName = node.getAttribute("ref");
				if (referenceName != null) {
					// a reference to a declared test
					if ("context.test".equals(node.getAttribute("ref"))) {
						IdTest.CONTEXT_TEST.serialize(out);
					} else {
						if (Tbs.resolveReferences) {
							if (Tbs.referencedTest.get(referenceName) == null) {
								XmlConfiguration.error("Referenced test '" + referenceName
										+ "' is unknown");
								return 0;
							}
						}
						IdTest.REFERENCED_TEST.serialize(out);
						MToolKit.writeString(out, referenceName);
					}
				} else {
					final Iterator<?> it = node.iterator();
					while (it.hasNext()) {
						Object obj = it.next();
						if (obj instanceof XmlParser.Node) {
							return XmlTest.getTest(((XmlParser.Node) obj).getTag()).buildMdb(
									(XmlParser.Node) obj, out);
						}
					}
					// no test
					IdTest.TRUE.serialize(out);
				}
			}
		} catch (Throwable e2) {
			XmlConfiguration.error("Error found in test '"
					+ (node == null ? "null" : node.getTag()) + "' : " + e2.getMessage()
					+ ". Context=" + (node == null ? "null" : node.toString()));
		}
		return 0;
	}

}
