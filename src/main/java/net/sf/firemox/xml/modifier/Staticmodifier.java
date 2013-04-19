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

import org.apache.commons.lang.StringUtils;

import net.sf.firemox.modifier.ModifierType;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.xml.XmlModifier;
import net.sf.firemox.xml.XmlParser;
import net.sf.firemox.xml.XmlToMDB;
import net.sf.firemox.xml.XmlTools;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class Staticmodifier implements XmlToMDB {

	/**
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * <li>filtering zone identifier [Zone]</li>
	 * <li>source zone identifier [Zone]</li>
	 * <li>modifiers [ModifierModel[]]</li>
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
		ModifierType.STATIC_MODIFIER.serialize(out);
		// override the linked attribute
		node.addAttribute(new XmlParser.Attribute("linked", "true"));
		XmlModifier.buildMdbModifier(node, out);

		// the filter identifier
		final String zone = node.getAttribute("filter-zone");
		if (StringUtils.isBlank(zone)) {
			out.write(IdZones.PLAY);
		} else {
			out.write(XmlTools.getZone(zone));
		}

		XmlParser.Node modifiers = node.get("modifiers");
		out.write(modifiers.getNbNodes());
		for (java.lang.Object obj : modifiers) {
			if (obj instanceof XmlParser.Node) {
				((XmlParser.Node) obj).addAttribute(new XmlParser.Attribute("linked",
						"true"));
				XmlModifier.getModifier(((XmlParser.Node) obj).getTag()).buildMdb(
						(XmlParser.Node) obj, out);
			}
		}
		return 0;
	}
}
