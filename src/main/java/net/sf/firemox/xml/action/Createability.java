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
import java.util.List;

import net.sf.firemox.action.Actiontype;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.xml.XmlAction;
import net.sf.firemox.xml.XmlEvent;
import net.sf.firemox.xml.XmlParser;
import net.sf.firemox.xml.XmlTbs;
import net.sf.firemox.xml.XmlToMDB;
import net.sf.firemox.xml.XmlTools;
import net.sf.firemox.xml.XmlParser.Node;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class Createability implements XmlToMDB {

	/**
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>super [Action]</li>
	 * <li>nb registers to set [int]</li>
	 * <li>register index i [Expression]</li>
	 * <li>expression i [Expression]</li>
	 * <li>idToken of card to save [TestOn]</li>
	 * <li>idToken of card to save [TestOn]</li>
	 * <li>ability To Add [...]</li>
	 * <li>nb exists until this triggered event [int]</li>
	 * <li>exists until this triggered event i [Event]</li>
	 * </ul>
	 * 
	 * @param node
	 *          the XML action structure
	 * @param out
	 *          output stream where the cardToAdd structure will be saved
	 * @return the amount of written action in the output.
	 * @throws IOException
	 */
	public int buildMdb(Node node, OutputStream out) throws IOException {
		XmlAction.buildMdb(Actiontype.CREATE_ABILITY, node, out);

		// write registers
		Node registers = node.get("setregister");
		if (registers == null) {
			out.write(0);
		} else {
			final List<Node> list = registers.getNodes("register");
			out.write(list.size());
			for (Node register : list) {
				// write index
				XmlTools.writeAttrOptions(register, "index", out);
				// write the expression
				XmlTools.writeAttrOptions(register, "value", out);
			}
		}

		// write the eventual <save target="..."/>
		Node setcard = node.get("save");
		if (setcard != null) {
			XmlTools.writeTestOn(out, setcard.getAttribute("target"));
			if (setcard.getAttribute("target2") != null) {
				XmlTools.writeTestOn(out, setcard.getAttribute("target2"));
			} else {
				TestOn.THIS.serialize(out);
			}
		} else {
			TestOn.THIS.serialize(out);
			TestOn.THIS.serialize(out);
		}

		if (node.get("triggered-ability") != null) {
			XmlTbs.getTbsComponent("triggeredability").buildMdb(
					node.get("triggered-ability"), out);
		} else if (node.get("replacement-ability") != null) {
			XmlTbs.getTbsComponent("replacementability").buildMdb(
					node.get("replacement-ability"), out);
		} else if (node.get("ability") != null) {
			XmlTbs.getTbsComponent("ability").buildMdb(node.get("ability"), out);
		} else {
			throw new InternalError("Unknow ability in 'createability' element");
		}

		// write the until events
		Node until = node.get("until");
		if (until == null) {
			// permanent attachment (until attached object remove this modifier)
			out.write(0);
		} else {
			until.addAttribute(new XmlParser.Attribute("zone", "play"));
			out.write(until.getNbNodes());
			for (Object obj : until) {
				if (obj instanceof Node) {
					Node child = (Node) obj;
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
