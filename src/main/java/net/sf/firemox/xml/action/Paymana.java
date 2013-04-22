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
import net.sf.firemox.operation.IdOperation;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.token.IdCommonToken;
import net.sf.firemox.token.IdTokens;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.xml.XmlAction;
import net.sf.firemox.xml.XmlConfiguration;
import net.sf.firemox.xml.XmlParser;
import net.sf.firemox.xml.XmlToMDB;
import net.sf.firemox.xml.XmlTools;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class Paymana implements XmlToMDB {

	/**
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>idAction [1]</li>
	 * <li>the player paying this mana [TestOn]</li>
	 * <li>idNumber COLORLESS or IdTokens#REGISTERS[2]</li>
	 * <li>idNumber BLACK [2]</li>
	 * <li>idNumber BLUE [2]</li>
	 * <li>idNumber GREEN [2]</li>
	 * <li>idNumber RED [2]</li>
	 * <li>idNumber WHITE [2]</li>
	 * <li>colors of X = 2^ID__Color1+2^ID__Color2...[2]</li>
	 * </ul>
	 * 
	 * @param node
	 *          the XML action structure
	 * @param out
	 *          output stream where the card structure will be saved.
	 * @return the amount of written action in the output.
	 * @throws IOException
	 */
	public final int buildMdb(XmlParser.Node node, OutputStream out)
			throws IOException {
		XmlAction.buildMdb(Actiontype.PAY_MANA, node, out);
		// write the specified controller
		String controllerAttr = node.getAttribute("controller");
		if (controllerAttr == null) {
			TestOn.CONTROLLER.serialize(out);
		} else {
			TestOn.serialize(out, controllerAttr);
		}

		if (XmlConfiguration.isNoPayMana()) {
			// we write this action all required mana set to 0
			MToolKit.writeInt16(out, 0); // dummy mode
			for (int i = 0; i < IdCommonToken.PAYABLE_COLOR_NAMES.length; i++) {
				IdOperation.INT_VALUE.serialize(out);
				MToolKit.writeInt16(out, 0);
			}
		} else {
			String braAttr = node.getAttribute("value");
			if (braAttr != null) {
				// register mode
				MToolKit.writeInt16(out, IdTokens.MANA_POOL);
				XmlTools.writeTestOn(out, node.getAttribute("card"));
			} else {
				MToolKit.writeInt16(out, 0); // dummy mode
				for (int i = 0; i < IdCommonToken.PAYABLE_COLOR_NAMES.length; i++) {
					if (node.getAttribute(IdCommonToken.PAYABLE_COLOR_NAMES[i]) == null
							&& node.get(IdCommonToken.PAYABLE_COLOR_NAMES[i]) == null) {
						// no such mana to pay for this color, so 0
						IdOperation.INT_VALUE.serialize(out);
						MToolKit.writeInt16(out, 0);
					} else {
						XmlTools.writeAttrOptions(node, IdCommonToken.PAYABLE_COLOR_NAMES[i], out);
					}
				}
			}
		}
		return 1;
	}

}
