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
import net.sf.firemox.xml.XmlParser;
import net.sf.firemox.xml.XmlTest;
import net.sf.firemox.xml.XmlToMDB;
import net.sf.firemox.xml.XmlTools;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 * @since 0.86 unique color give and restriction usage are supported
 */
public class Givemana implements XmlToMDB {

	/**
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>idAction GIVE_MANA_BASIC/GIVE_MANA_MULTI[1]</li>
	 * <li>restriction usage test [...]</li>
	 * <li>player receiving this mana [TestOn]</li>
	 * <li>For GIVE_MANA_BASIC option : mana color to give : idNumber [2]</li>
	 * <li>For GIVE_MANA_BASIC option : mana amount to give : expression [...]</li>
	 * <li>For GIVE_MANA_MULTI option : COLORLESS : expression [...] or
	 * (IdTokens#REGISTERS)[2]</li>
	 * <li>For GIVE_MANA_MULTI option : BLACK : expression[...]</li>
	 * <li>For GIVE_MANA_MULTI option : BLUE : expression[...]</li>
	 * <li>For GIVE_MANA_MULTI option : GREEN : expression[...]</li>
	 * <li>For GIVE_MANA_MULTI option : RED : expression[...]</li>
	 * <li>For GIVE_MANA_MULTI option : WHITE : expression[...]</li>
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
		// write the type
		final String uniqueColor = node.getAttribute("color");
		if (uniqueColor != null || node.get("color") != null)
			XmlAction.buildMdb(Actiontype.GIVE_MANA_BASIC, node, out);
		else
			XmlAction.buildMdb(Actiontype.GIVE_MANA_MULTI, node, out);

		// Write restriction usage test
		XmlTest.getTest("test").buildMdb(node.get("restriction"), out);

		// Write destination player
		if (node.getAttribute("to") == null) {
			TestOn.CONTROLLER.serialize(out);
		} else {
			XmlTools.writeTestOn(out, node.getAttribute("to"));
		}

		if (uniqueColor != null || node.get("color") != null) {
			// only one color to modify, write amount to add
			XmlTools.writeAttrOptions(node, "color", out);
			XmlTools.writeAttrOptions(node, "value", out);
		} else {
			final String braAttr = node.getAttribute("value");
			if (braAttr != null) {
				MToolKit.writeInt16(out, IdTokens.MANA_POOL); // manacost mode
				XmlTools.writeTestOn(out, node.getAttribute("card"));
			} else {
				MToolKit.writeInt16(out, 0); // non manacost mode
				for (int i = 0; i < IdCommonToken.COLOR_NAMES.length; i++) {
					if (node.getAttribute(IdCommonToken.COLOR_NAMES[i]) == null
							&& node.get(IdCommonToken.COLOR_NAMES[i]) == null) {
						// no such mana to add for this color, so 0
						IdOperation.INT_VALUE.serialize(out);
						MToolKit.writeInt16(out, 0);
					} else {
						XmlTools.writeAttrOptions(node, IdCommonToken.COLOR_NAMES[i], out);
					}
				}
			}
		}
		return 1;
	}

}
