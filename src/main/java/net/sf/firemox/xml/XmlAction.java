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
 * 
 */
package net.sf.firemox.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import net.sf.firemox.action.Actiontype;
import net.sf.firemox.operation.IdOperation;
import net.sf.firemox.token.IdTokens;
import net.sf.firemox.token.Register;
import net.sf.firemox.tools.MToolKit;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80
 */
public final class XmlAction {

	/**
	 * Create a new instance of this class.
	 */
	private XmlAction() {
		super();
	}

	/**
	 * @param out
	 * @param node
	 * @param withValue
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static void writeModifyRegister(OutputStream out, XmlParser.Node node,
			boolean withValue) throws IOException {

		// write the type
		Register register = Register.valueOfXsd(node.getAttribute("register"));
		if (register.isTargetable()) {
			XmlAction.buildMdb(Actiontype.MODIFY_TARGETABLE_REGISTER, node, out);
		} else {
			switch (register.ordinal()) {
			case IdTokens.REQUIRED_MANA:
				XmlAction.buildMdb(Actiontype.MODIFY_REQUIRED_MANA, node, out);
				break;
			case IdTokens.TARGET:
				XmlAction.buildMdb(Actiontype.MODIFY_TARGET_LIST_REGISTER, node, out);
				break;
			case IdTokens.DELAYED_REGISTERS:
				XmlAction.buildMdb(Actiontype.MODIFY_ABILITY_REGISTER, node, out);
				break;
			case IdTokens.STACK:
				XmlAction.buildMdb(Actiontype.MODIFY_STACK_REGISTER, node, out);
				break;
			case IdTokens.STATIC_REGISTER:
				XmlAction.buildMdb(Actiontype.MODIFY_STATIC_REGISTER, node, out);
				break;
			default:
				throw new InternalError("unknown register type : " + register.ordinal());
			}
		}

		XmlTools.writeAttrOptions(node, "index", out);

		final IdOperation idOperation = XmlTools.getOperation(node
				.getAttribute("operation"));
		idOperation.serialize(out);
		switch (idOperation) {
		case CLEAR:
		case COUNTER:
			XmlTools.writeConstant(out, 0);
			break;
		default:
			if (withValue) {
				XmlTools.writeAttrOptions(node, "value", out);
			} else {
				XmlTools.writeConstant(out, 0);
			}
		}

		if (register.isTargetable()) {
			register.getTargetable().serialize(out);
		}

	}

	/**
	 * Return the action writter from the given name.
	 * 
	 * @param actionName
	 *          the action name.
	 * @return the action writter from the given name.
	 */
	public static XmlToMDB getAction(String actionName) {
		return XmlConfiguration.getXmlClass(actionName, instances, XmlAction.class);
	}

	/**
	 * Write the action of the specified node to the given output stream.<br>
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>action type [Actiontype]</li>
	 * <li>action name [String]</li>
	 * <li>action debug data [String]</li>
	 * </ul>
	 * <br>
	 * 
	 * @param actionType
	 *          the action type.
	 * @param node
	 *          the XML test container structure
	 * @param out
	 *          output stream where the test container structure will be saved if
	 *          an illegal argument is given if a security error occurs
	 * @throws IOException
	 *           error while writing action.
	 */
	public static void buildMdb(Actiontype actionType, XmlParser.Node node,
			OutputStream out) throws IOException {
		if (actionType != null)
			actionType.write(out);
		if (node != null) {
			MToolKit.writeString(out, node.getAttribute("name"));
			MToolKit.writeString(out, node.toString());
		}
	}

	private static Map<String, XmlToMDB> instances = new HashMap<String, XmlToMDB>();

	/**
	 * Write some debug data to the given output stream.<br>
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>action debug data [String]</li>
	 * </ul>
	 * <br>
	 * 
	 * @param debugData
	 *          debug data to write.
	 * @param out
	 *          output stream where the test container structure will be saved if
	 *          an illegal argument is given if a security error occurs
	 */
	public static void writeDebug(String debugData, OutputStream out) {
		if (XmlConfiguration.isDebugEnable())
			MToolKit.writeString(out, debugData);
		else
			MToolKit.writeString(out, "");
	}

	static void clean() {
		instances.clear();
	}
}
