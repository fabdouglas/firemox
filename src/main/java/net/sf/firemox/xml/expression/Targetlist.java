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
package net.sf.firemox.xml.expression;

import java.io.IOException;
import java.io.OutputStream;

import net.sf.firemox.operation.IdOperation;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.xml.XmlParser;
import net.sf.firemox.xml.XmlToMDB;
import net.sf.firemox.xml.XmlTools;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class Targetlist implements XmlToMDB {

	/**
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>idOperation :IdOperations.ID__TARGET_LIST [1]</li>
	 * <li>idOperation of int-list [1]</li>
	 * <li>list index : Expression</li>
	 * <li>[iterate expression : Expression]</li>
	 * <li>[target : TestOn]</li>
	 * </ul>
	 * 
	 * @param node
	 *          the XML action structure
	 * @param out
	 *          output stream where the card structure will be saved
	 * @return the amount of written action, so return always ZERO.
	 * @throws IOException
	 */
	public final int buildMdb(XmlParser.Node node, OutputStream out)
			throws IOException {
		// write the operation type
		IdOperation.TARGET_LIST.serialize(out);
		IdOperation op = XmlTools.getOperation(node.getAttribute("operation"));
		op.serialize(out);
		XmlTools.tryWriteExpression(node, "list-index", out);
		switch (op) {
		case SIZE:
			break;
		case COUNTER:
		case INDEX_OF:
		case LAST_INDEX_OF:
		case INDEX_OF_SAVED_LIST:
			TestOn.serialize(out, node.getAttribute("target"));
			break;
		case ADD:
		case MINIMUM:
		case MAXIMUM:
			XmlTools.writeAttrOptions(node, "value", out);
			break;
		default:
			throw new InternalError("Unsupported operation for int-list :"
					+ node.getAttribute("operation"));
		}
		return 0;
	}

}
