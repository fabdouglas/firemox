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
package net.sf.firemox.xml.modifier;

import java.io.IOException;
import java.io.OutputStream;

import net.sf.firemox.modifier.ModifierType;
import net.sf.firemox.xml.XmlModifier;
import net.sf.firemox.xml.XmlParser;
import net.sf.firemox.xml.XmlToMDB;
import net.sf.firemox.xml.XmlTools;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class Playablezonemodifier implements XmlToMDB {

	/**
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * <li>hasNot tag [boolean]</li>
	 * <li>playable zone [Zone]</li>
	 * </ul>
	 * 
	 * @param node
	 *          the XML card structure
	 * @param out
	 *          output stream where the card structure will be saved
	 * @return the amount of written action, so return always ZERO.
	 * @throws IOException
	 *           error while writing.
	 */
	public final int buildMdb(XmlParser.Node node, OutputStream out)
			throws IOException {
		ModifierType.PLAYABLE_ZONE_MODIFIER.serialize(out);
		XmlModifier.buildMdbModifier(node, out);

		// the hasNOT tag
		out.write("true".equalsIgnoreCase(node.getAttribute("lose")) ? 0 : 1);
		// the modified idcard
		out.write(XmlTools.getZone(node.getAttribute("zone")));

		return 0;
	}
}
