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
package net.sf.firemox.xml.action;

import java.io.IOException;
import java.io.OutputStream;

import net.sf.firemox.action.Actiontype;
import net.sf.firemox.xml.XmlAction;
import net.sf.firemox.xml.XmlConfiguration;
import net.sf.firemox.xml.XmlEvent;
import net.sf.firemox.xml.XmlParser;
import net.sf.firemox.xml.XmlTbs;
import net.sf.firemox.xml.XmlToMDB;
import net.sf.firemox.xml.XmlParser.Node;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class Addability implements XmlToMDB {

	/**
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>idAction [1]</li>
	 * <li>ability To Add [...]</li>
	 * <li>nb exists until this triggered event [1]</li>
	 * <li>exists until this triggered event i [...]</li>
	 * </ul>
	 * 
	 * @param node
	 *          the XML action structure
	 * @param out
	 *          output stream where the cardToAdd structure will be saved
	 * @return the amount of written action in the output.
	 * @throws IOException
	 */
	public int buildMdb(XmlParser.Node node, OutputStream out) throws IOException {
		XmlAction.buildMdb(Actiontype.ADD_ABILITY, node, out);

		if (node.getAttribute("ref") != null) {
			// a reference has been found
			XmlTbs.getTbsComponent("ability").buildMdb(node, out);
		} else {
			// search nested ability
			boolean found = false;
			for (Object nodeObject : node) {
				if (nodeObject instanceof Node) {
					Node ability = (Node) nodeObject;
					if (!ability.getTag().equalsIgnoreCase("until")) {
						found = true;
						XmlTbs.getTbsComponent(ability.getTag()).buildMdb(ability, out);
					}
					break;
				}
			}
			if (!found) {
				XmlConfiguration
						.error("add-ability action requires either 'ref' attribute, either a nested ability");
			}
		}

		// write the until events
		XmlParser.Node until = node.get("until");
		if (until == null) {
			// permanent attachment (until attached object remove this modifier)
			out.write(0);
		} else {
			until.addAttribute(new XmlParser.Attribute("zone", "play"));
			out.write(until.getNbNodes());
			for (Object obj : until) {
				if (obj instanceof XmlParser.Node) {
					XmlParser.Node child = (XmlParser.Node) obj;
					if ("text".equals(child.getTag())) {
						System.out
								.println("text element in 'until condition' of modifier is not yet implement");
					} else {
						XmlEvent.getEvent(child.getTag()).buildMdb(child, out);
						break;
					}
				}
			}
		}
		return 1;
	}
}
