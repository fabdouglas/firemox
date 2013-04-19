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

import java.io.IOException;
import java.io.OutputStream;

import net.sf.firemox.test.IdTest;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.xml.XmlParser;
import net.sf.firemox.xml.XmlTest;
import net.sf.firemox.xml.XmlToMDB;
import net.sf.firemox.xml.XmlTools;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
public class Has implements XmlToMDB {

	/**
	 * Write the contained test of the specified node <br>
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>idTest [1]</li>
	 * <li>test used to fill counter [...]
	 * <li>restriction zone [1]
	 * </ul>
	 * 
	 * @param node
	 *          the XML test container structure
	 * @param out
	 *          output stream where the card structure will be saved
	 * @return the amount of written action, so return always ZERO.
	 * @throws IOException
	 *           error while writing.
	 */
	public final int buildMdb(XmlParser.Node node, OutputStream out)
			throws IOException {
		IdTest.HAS.serialize(out);
		// Write the common test
		if (node.getAttribute("ref") != null) {
			// a reference to a declared test
			IdTest.REFERENCED_TEST.serialize(out);
			MToolKit.writeString(out, node.getAttribute("ref"));
		} else {
			final boolean oldValue = XmlTools.defaultOnMeTag;
			XmlTools.defaultOnMeTag = false;
			XmlTest.getTest("test").buildMdb(node, out);
			XmlTools.defaultOnMeTag = oldValue;
		}
		final String restrictionZone = node.getAttribute("restriction-zone");
		out.write(XmlTools.getZone(restrictionZone) + 1);
		return 0;
	}
}
