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
import net.sf.firemox.test.TestOn;
import net.sf.firemox.xml.XmlAction;
import net.sf.firemox.xml.XmlToMDB;
import net.sf.firemox.xml.XmlTools;
import net.sf.firemox.xml.XmlParser.Node;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.94
 */
public class Copycard implements XmlToMDB {

	/**
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>idAction [1]</li>
	 * <li>card to copy [TestOn]</li>
	 * <li>card to apply copy [TestOn]</li>
	 * <li>excludes name [boolean]</li>
	 * <li>excludes colors [ListExpression]</li>
	 * <li>excludes idcards [ListExpression]</li>
	 * </ul>
	 * 
	 * @param node
	 *          the XML action structure
	 * @param out
	 *          output stream where the card structure will be saved.
	 * @return the amount of written action in the output.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public int buildMdb(Node node, OutputStream out) throws IOException {
		XmlAction.buildMdb(Actiontype.COPY_CARD, node, out);
		TestOn.serialize(out, node.getAttribute("card"));
		TestOn.serialize(out, node.getAttribute("to"));
		writeList(node.get("excludes"), out);
		return 1;
	}

	private void writeList(Node node, OutputStream out) throws IOException {
		out.write((node != null && "true".equals(node.getAttribute("name"))) ? 1
				: 0);
		// TODO Copycard action : XmlTools.writeList(out, node, "properties");
		XmlTools.writeList(out, node, "color");
		XmlTools.writeList(out, node, "idcard");
	}

}
