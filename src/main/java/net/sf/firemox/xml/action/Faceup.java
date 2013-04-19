/*
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
import net.sf.firemox.token.VisibilityChange;
import net.sf.firemox.xml.XmlAction;
import net.sf.firemox.xml.XmlParser;
import net.sf.firemox.xml.XmlToMDB;

/**
 * @author <a href="mailto:hoani.cross@gmail.com">Hoani CROSS</a>
 * @since 0.90
 * @since 0.91 'for' constraint attribute, default [everyone] is added.
 */
public class Faceup implements XmlToMDB {

	/**
	 * <ul>
	 * Structure of stream :
	 * <li>idAction [1]</li>
	 * <li>int8 : 1 (means face up for Face action) [0]</li>
	 * <li>visibility [1]</li>
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
		XmlAction.buildMdb(Actiontype.FACE, node, out);
		out.write(1);
		String forId = node.getAttribute("for");
		if (forId == null) {
			VisibilityChange.everyone.serialize(out);
		} else {
			VisibilityChange.valueOf(forId).serialize(out);
		}
		return 1;
	}

}
