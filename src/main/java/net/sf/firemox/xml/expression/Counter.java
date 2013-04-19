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
package net.sf.firemox.xml.expression;

import java.io.IOException;
import java.io.OutputStream;

import net.sf.firemox.operation.IdOperation;
import net.sf.firemox.test.IdTest;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.xml.XmlConfiguration;
import net.sf.firemox.xml.XmlParser;
import net.sf.firemox.xml.XmlTest;
import net.sf.firemox.xml.XmlToMDB;
import net.sf.firemox.xml.XmlTools;
import net.sf.firemox.xml.XmlParser.Node;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 * @since 0.94 macro-test
 */
public class Counter implements XmlToMDB {

	/**
	 * The macro-test is a test that can be defined from an action node using
	 * counter-test node.
	 */
	public static Node macroCounterTest;

	/**
	 * Write the contained test of the specified node <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>object's name + '\0' [...]
	 * <li>idTestOn [1] (only if object's name specified)
	 * <li>test used to fill counter [...] (only if object's name not specified)
	 * <li>restricyion zone + enable count player [1] (only if object's name not
	 * specified)
	 * </ul>
	 * 
	 * @param node
	 *          the XML test container structure
	 * @param out
	 *          output stream where the card structure will be saved
	 * @return the amount of written action, so return always ZERO.
	 * @throws IOException
	 */
	public final int buildMdb(XmlParser.Node node, OutputStream out)
			throws IOException {
		IdOperation.COUNTER.serialize(out);
		final boolean oldValue = XmlTools.defaultOnMeTag;
		XmlTools.defaultOnMeTag = false;

		// Write the object name
		MToolKit.writeString(out, node.getAttribute("object-name"));

		// Write the common test

		// Is macro enabled for this test?
		if (macroCounterTest != null) {
			IdTest.AND.serialize(out);
			XmlTest.getTest("test").buildMdb(macroCounterTest, out);
		}
		XmlTest.getTest("test").buildMdb(node, out);

		if (node.getAttribute("object-name") != null) {
			// we are counting objects of component
			XmlTools.writeTestOn(out, node.getAttribute("card"));
			// write the card on which the object would be counted
		} else {
			// we are counting cards
			if (node.getAttribute("card") != null) {
				throw new InternalError(
						"'card' attribute can be specified only when we are counting objects.");
			}
			// write the restriction zone and tag specifying if player are counted too
			final String restrictionZone = node.getAttribute("restriction-zone");
			final int countPlayer = "true".equals(node.getAttribute("count-player")) ? net.sf.firemox.expression.Counter.COUNT_PLAYER
					: 0;
			if (restrictionZone != null) {
				final int idRestrictionZone = XmlTools.getZone(restrictionZone);
				if (idRestrictionZone == IdZones.ANYWHERE) {
					out.write(countPlayer);
				} else {
					out.write(countPlayer | idRestrictionZone + 1);
				}
			} else {
				if (countPlayer == 0) {
					XmlConfiguration
							.warning("'restriction-zone' attribute is recommanded for counter action");
				}
				out.write(countPlayer);
			}
		}
		XmlTools.defaultOnMeTag = oldValue;
		return 0;
	}

}
