/*
 * Created on 9 mars 2005
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
import java.util.ArrayList;

import net.sf.firemox.xml.XmlParser;
import net.sf.firemox.xml.XmlTbs;
import net.sf.firemox.xml.XmlToMDB;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class Insertability implements XmlToMDB {

	/**
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>actions [...]</li>
	 * </ul>
	 * 
	 * @param node
	 *          the XML action structure
	 * @param out
	 *          output stream where the card structure will be saved.
	 * @return the amount of written action in the output.
	 * @throws IOException
	 */
	public int buildMdb(XmlParser.Node node, OutputStream out) throws IOException {
		XmlParser.Node ability = XmlTbs.getReferencedAbility(node
				.getAttribute("ref"));
		int nbCost = 0;

		// writes the cost part
		XmlParser.Node cost = ability.get("cost");
		if (cost != null) {
			nbCost += XmlTbs.writeActionListNoNb(cost, out);
		}

		// write the effect part only if we are in effect part
		XmlParser.Node effects = ability.get("effects");
		if (effects != null) {
			XmlParser.Node newEffects = new XmlParser.Node(node.getParent(),
					"effects", null);
			if ("cost".equals(node.getParent().getTag())) {
				// in cost element -> add only effects, no write
				XmlParser.Node parentEffect = node.getParent().getParent().get(
						"effects");
				newEffects.aList = new ArrayList<Object>();
				newEffects.aList.addAll(effects.aList);
				if (parentEffect != null) {
					// add to thebeginning the effects of the reference ability
					newEffects.aList.addAll(parentEffect.aList);
					// remove the parent's "effects"
					node.getParent().getParent().aList.remove(parentEffect);
				}
				node.getParent().getParent().aList.add(newEffects);
			} else {
				// in effects element -> write directly actions
				nbCost += XmlTbs.writeActionListNoNb(effects, out);
			}
		}
		return nbCost;
	}

}
