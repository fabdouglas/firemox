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
import net.sf.firemox.token.IdTargetList;
import net.sf.firemox.xml.XmlAction;
import net.sf.firemox.xml.XmlParser;
import net.sf.firemox.xml.XmlToMDB;
import net.sf.firemox.xml.XmlTools;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class Intlist implements XmlToMDB {

	/**
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>idAction [1]</li>
	 * <li>operation [1]</li>
	 * <li>{idType [1]}</li>
	 * <li>{index : Expression [...]}</li>
	 * <li>{list-index : Expression [...]}</li>
	 * <li>{value : idTokent [2]}</li>
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
		// write the type
		Actiontype.INT_LIST.write(out);

		final IdOperation idOperation = XmlTools.getOperation(node
				.getAttribute("operation"));
		idOperation.serialize(out);
		switch (idOperation) {
		case COUNTER:
			if (node.getAttribute("value") != null || node.get("value") != null
					|| node.get("valueIScounter") != null) {
				out.write(IdTargetList.OCCURENCE_LIST.ordinal());
				XmlAction.buildMdb(null, node, out);
				XmlTools.tryWriteExpression(node, "list-index", out);
			} else {
				out.write(IdTargetList.OCCURENCE_TARGET.ordinal());
				XmlAction.buildMdb(null, node, out);
				XmlTools.tryWriteExpression(node, "list-index", out);
				XmlTools.writeAttrOptions(node, "value", out);
			}
			break;
		case LOAD:
			XmlAction.buildMdb(null, node, out);
			XmlTools.writeAttrOptions(node, "list-index", out);
			break;
		case REMOVE_FIRST:
			XmlAction.buildMdb(null, node, out);
			XmlTools.tryWriteExpression(node, "list-index", out);
			break;
		case REMOVE_LAST:
			XmlAction.buildMdb(null, node, out);
			XmlTools.tryWriteExpression(node, "list-index", out);
			break;
		case CLEAR:
			XmlAction.buildMdb(null, node, out);
			XmlTools.tryWriteExpression(node, "list-index", out);
			break;
		case SAVE:
			if (node.getAttribute("value") != null || node.get("value") != null) {
				out.write(IdTargetList.SAVE_TARGET.ordinal());
				XmlAction.buildMdb(null, node, out);
				XmlTools.tryWriteExpression(node, "index", out);
				XmlTools.tryWriteExpression(node, "list-index", out);
				XmlTools.writeAttrOptions(node, "value", out);
			} else if (node.getAttribute("index") != null
					|| node.get("index") != null) {
				out.write(IdTargetList.SAVE_INDEX.ordinal());
				XmlAction.buildMdb(null, node, out);
				XmlTools.writeAttrOptions(node, "index", out);
				XmlTools.tryWriteExpression(node, "list-index", out);
			} else {
				out.write(IdTargetList.SAVE_LIST.ordinal());
				XmlAction.buildMdb(null, node, out);
				XmlTools.tryWriteExpression(node, "list-index", out);
			}
			break;
		case REMOVE:
			if (node.getAttribute("value") != null || node.get("value") != null) {
				out.write(IdTargetList.REMOVE_TARGET.ordinal());
				XmlAction.buildMdb(null, node, out);
				XmlTools.tryWriteExpression(node, "list-index", out);
				XmlTools.writeAttrOptions(node, "value", out);
			} else if (node.getAttribute("index") != null
					|| node.get("index") != null) {
				out.write(IdTargetList.REMOVE_INDEX.ordinal());
				XmlAction.buildMdb(null, node, out);
				XmlTools.writeAttrOptions(node, "index", out);
				XmlTools.tryWriteExpression(node, "list-index", out);
			} else {
				out.write(IdTargetList.REMOVE_LIST.ordinal());
				XmlAction.buildMdb(null, node, out);
				XmlTools.tryWriteExpression(node, "list-index", out);
			}
			break;
		case REMOVE_QUEUE:
			XmlAction.buildMdb(null, node, out);
			XmlTools.tryWriteExpression(node, "list-index", out);
			break;
		case REMOVE_TAIL:
			XmlAction.buildMdb(null, node, out);
			XmlTools.tryWriteExpression(node, "list-index", out);
			break;
		case MINUS:
		case ADD:
		case MULT:
		case DIV_ROUNDED:
		case DIV_TRUNCATED:
		case OR:
		case AND:
		case XOR:
		case ADD_ROUNDED:
		case ADD_TRUNCATED:
		case SET:
			// value attribute is expected
			XmlAction.buildMdb(null, node, out);
			XmlTools.tryWriteExpression(node, "list-index", out);
			XmlTools.tryWriteExpression(node, "index", out);
			XmlTools.writeAttrOptions(node, "value", out);
			break;
		default:
			throw new InternalError("\t>>Unsupported operation for int-list :"
					+ node.getAttribute("operation"));
		}
		return 1;
	}

}
