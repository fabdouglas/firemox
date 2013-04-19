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
package net.sf.firemox.xml.tbs;

import java.io.IOException;
import java.io.OutputStream;

import net.sf.firemox.clickable.ability.AbilityType;
import net.sf.firemox.token.TrueFalseAuto;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.xml.XmlConfiguration;
import net.sf.firemox.xml.XmlParser;
import net.sf.firemox.xml.XmlTbs;
import net.sf.firemox.xml.XmlToMDB;
import net.sf.firemox.xml.XmlTools;
import net.sf.firemox.xml.XmlParser.Node;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class Ability implements XmlToMDB {

	/**
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>ability type [1]</li>
	 * <li>name name [String]</li>
	 * <li>ability tags [int]</li>
	 * <li>event [Event]</li>
	 * <li>cost [Action[]]</li>
	 * <li>effect [Action[]]</li>
	 * </ul>
	 * 
	 * @param node
	 *          the XML ability structure
	 * @param out
	 *          output stream where the card structure will be saved
	 * @see net.sf.firemox.clickable.ability.UserAbility
	 * @see net.sf.firemox.clickable.ability.ActivatedAbility
	 * @return the amount of written action in the output.
	 * @throws IOException
	 *           error while writing.
	 */
	public final int buildMdb(XmlParser.Node node, OutputStream out)
			throws IOException {
		final String referenceName = node.getAttribute("ref");
		if (referenceName == null) {
			XmlConfiguration.error("Reference ability's name is missing for " + node);
		}

		// a reference to a declared ability
		if (node.get("actions") != null) {
			// Build a copy of the referenced ability as macro
			Node abilityRef = XmlTbs.getReferencedAbility(referenceName);
			if (!Tbs.resolveReferences) {
				XmlConfiguration
						.error("Cannot use action macro ouside cards for ability '"
								+ referenceName + "'");
				return 0;
			}
			Tbs.pushMacroAction(node.getNodes("actions"));
			try {
				Node ability = (Node) abilityRef.get(1);
				XmlTbs.getTbsComponent(ability.getTag()).buildMdb(ability, out);
			} catch (Throwable ie) {
				XmlConfiguration.error("Error In referenced ability (macro) '"
						+ referenceName + "' : " + ie.getMessage() + ". Context="
						+ abilityRef);
			}
			Tbs.popMacroAction();
		} else {
			// write a simple reference
			AbilityType.REFERENCED_ABILITY.write(out);
			MToolKit.writeString(out, referenceName);
		}
		return 0;
	}

	/**
	 * @param abilityType
	 * @param node
	 * @param out
	 * @throws IOException
	 */
	static void buildAbility(AbilityType abilityType, XmlParser.Node node,
			OutputStream out) throws IOException {

		// write the type
		abilityType.write(out);

		// write the name
		MToolKit.writeString(out, node.getAttribute("name"));

		// write the resolution and optimization
		XmlTools.getPriority(node.getAttribute("resolution")).write(out);
		XmlTools.getOptimization(node.getAttribute("optimize")).write(out);

		if (!XmlTools.getPriority(node.getAttribute("resolution")).isHidden()) {
			MToolKit.writeString(out, node.getAttribute("picture"));
		}
		TrueFalseAuto.valueOfXsd(node.getAttribute("play-as-spell")).serialize(out);
	}

}
