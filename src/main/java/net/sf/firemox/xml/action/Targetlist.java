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
import net.sf.firemox.token.IdTargetList;
import net.sf.firemox.xml.XmlAction;
import net.sf.firemox.xml.XmlParser;
import net.sf.firemox.xml.XmlTest;
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
	 * <li>idAction [1]</li>
	 * <li>operation [1]</li>
	 * <li>{idType [1]}</li>
	 * <li>{list-index [Expression]}</li>
	 * <li>{index [Expression]}</li>
	 * <li>{value [Expression]}</li>
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
		Actiontype.TARGET_LIST.write(out);

		final IdOperation op = XmlTools
				.getOperation(node.getAttribute("operation"));
		op.serialize(out);
		switch (op) {
		case COUNTER:
			if (node.getAttribute("target") == null) {
				IdTargetList.OCCURENCE_LIST.serialize(out);
				XmlAction.buildMdb(null, node, out);
				XmlTools.tryWriteExpression(node, "list-index", out);
			} else {
				IdTargetList.OCCURENCE_TARGET.serialize(out);
				XmlAction.buildMdb(null, node, out);
				XmlTools.tryWriteExpression(node, "list-index", out);
				TestOn.serialize(out, node.getAttribute("target"));
			}
			break;
		case LOAD:
			if (node.getAttribute("index") != null || node.get("index") != null) {
				IdTargetList.LOAD_INDEX.serialize(out);
				XmlAction.buildMdb(null, node, out);
				XmlTools.tryWriteExpression(node, "list-index", out);
				XmlTools.writeAttrOptions(node, "index", out);
			} else {
				IdTargetList.LOAD_LIST.serialize(out);
				XmlAction.buildMdb(null, node, out);
				XmlTools.tryWriteExpression(node, "list-index", out);
			}
			break;
		case REMOVE_FIRST:
			XmlAction.buildMdb(null, node, out);
			XmlTools.tryWriteExpression(node, "list-index", out);
			break;
		case REMOVE_LAST:
			XmlAction.buildMdb(null, node, out);
			XmlTools.tryWriteExpression(node, "list-index", out);
			break;
		case COLLAPSE_COMBAT:
			XmlAction.buildMdb(null, node, out);
			XmlTools.writeAttrOptions(node, "list-index", out);
			XmlTools.writeAttrOptions(node, "nb-attacking-group", out);
			break;
		case CLEAR:
			XmlAction.buildMdb(null, node, out);
			XmlTools.tryWriteExpression(node, "list-index", out);
			break;
		case SAVE:
			if (node.getAttribute("target") != null) {
				IdTargetList.SAVE_TARGET.serialize(out);
				XmlAction.buildMdb(null, node, out);
				XmlTools.tryWriteExpression(node, "list-index", out);
				XmlTools.tryWriteExpression(node, "index", out);
				TestOn.serialize(out, node.getAttribute("target"));
			} else if (node.getAttribute("index") != null
					|| node.get("index") != null) {
				IdTargetList.SAVE_INDEX.serialize(out);
				XmlAction.buildMdb(null, node, out);
				XmlTools.tryWriteExpression(node, "list-index", out);
				XmlTools.writeAttrOptions(node, "index", out);
			} else {
				IdTargetList.SAVE_LIST.serialize(out);
				XmlAction.buildMdb(null, node, out);
				XmlTools.tryWriteExpression(node, "list-index", out);
			}
			break;
		case REMOVE:
			if (node.getAttribute("target") != null) {
				IdTargetList.REMOVE_TARGET.serialize(out);
				XmlAction.buildMdb(null, node, out);
				XmlTools.tryWriteExpression(node, "list-index", out);
				TestOn.serialize(out, node.getAttribute("target"));
			} else if (node.getAttribute("index") != null
					|| node.get("index") != null) {
				IdTargetList.REMOVE_INDEX.serialize(out);
				XmlAction.buildMdb(null, node, out);
				XmlTools.tryWriteExpression(node, "list-index", out);
				XmlTools.writeAttrOptions(node, "index", out);
			} else {
				IdTargetList.REMOVE_LIST.serialize(out);
				XmlAction.buildMdb(null, node, out);
				XmlTools.tryWriteExpression(node, "list-index", out);
			}
			break;
		case REMOVE_QUEUE:
		case REMOVE_TAIL:
		case ADD_ALL:
		case REMOVE_ALL:
			XmlAction.buildMdb(null, node, out);
			XmlTools.tryWriteExpression(node, "list-index", out);
			break;
		case FILTER:
			XmlAction.buildMdb(null, node, out);
			XmlTools.tryWriteExpression(node, "list-index", out);
			XmlTest.getTest("test").buildMdb(node.get("test"), out);
			break;
		default:
			throw new InternalError("\t>>Unsupported operation for target-list :"
					+ node.getAttribute("operation"));
		}
		return 1;
	}

}
