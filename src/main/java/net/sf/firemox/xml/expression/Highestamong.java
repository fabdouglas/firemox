/*
 * Created on 3 avr. 2005
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
import net.sf.firemox.xml.XmlParser;
import net.sf.firemox.xml.XmlTest;
import net.sf.firemox.xml.XmlToMDB;
import net.sf.firemox.xml.XmlTools;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.83
 */
public class Highestamong implements XmlToMDB {

	/**
	 * Write the contained test of the specified node <br>
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>operation id [1]</li>
	 * <li>restriction Zone id [1]</li>
	 * <li>test [...]</li>
	 * <li>expression [...]</li>
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
		IdOperation.HIGHEST_AMONG.serialize(out);

		// write the restriction zone
		String restrictionZone = node.getAttribute("restriction-zone");
		if (restrictionZone != null) {
			out.write(XmlTools.getZone(restrictionZone) + 1);
		} else {
			out.write(0);
		}

		final boolean oldDefaultOnMeTag = XmlTools.defaultOnMeTag;
		XmlTools.defaultOnMeTag = false;
		XmlTest.getTest("test").buildMdb(node.get("test"), out);
		XmlTools.defaultOnMeTag = oldDefaultOnMeTag;

		XmlTools.writeAttrOptions(node, "expression", out);

		return 0;
	}

}
