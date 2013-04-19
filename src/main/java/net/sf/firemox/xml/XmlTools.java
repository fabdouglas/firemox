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
package net.sf.firemox.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.firemox.clickable.ability.Optimization;
import net.sf.firemox.clickable.ability.Priority;
import net.sf.firemox.expression.intlist.ListType;
import net.sf.firemox.operation.IdOperation;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.token.AbstractValue;
import net.sf.firemox.token.IdAbilities;
import net.sf.firemox.token.IdCardColors;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.token.IdMessageBox;
import net.sf.firemox.token.IdPositions;
import net.sf.firemox.token.IdTargets;
import net.sf.firemox.token.IdTokens;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.token.Register;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.xml.XmlParser.Node;

import org.apache.commons.lang.StringUtils;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public final class XmlTools {

	private XmlTools() {
		super();
	}

	private static Map<String, Integer> registerIndexName = new HashMap<String, Integer>();

	private static Map<String, Integer> targetMode = new HashMap<String, Integer>();

	private static Map<String, Integer> position = new HashMap<String, Integer>();

	/**
	 * The available zones.
	 * 
	 * @see net.sf.firemox.xml.tbs.Tbs#buildMdb(Node, OutputStream)
	 */
	public static Map<String, Integer> zones = new HashMap<String, Integer>();

	/**
	 * The available abilities
	 * 
	 * @see net.sf.firemox.xml.tbs.Tbs#buildMdb(Node, OutputStream)
	 */
	public static Map<String, Integer> abilities = new HashMap<String, Integer>();

	private static Map<String, Integer> definedValuesName = new HashMap<String, Integer>();

	private static Map<String, Integer> cardColorsName = new HashMap<String, Integer>();

	/**
	 * If there is neither attribute neither node with the given name, '-1' is
	 * written instead of throwing an exception.
	 * 
	 * @param node
	 *          the parent node.
	 * @param attribute
	 *          the attribute/node name to write.
	 * @param out
	 *          the output stream.
	 * @throws IOException
	 *           error while writing.
	 */
	public static void tryWriteExpression(XmlParser.Node node, String attribute,
			OutputStream out) throws IOException {
		if (node.getAttribute(attribute) == null && node.get(attribute) == null) {
			// no such attribute --> '-1', working on the current list
			XmlTools.writeConstant(out, -1);
		} else if ("-1".equals(node.getAttribute(attribute))) {
			// last index --> ALL, working the last saved list
			XmlTools.writeConstant(out, IdConst.ALL);
		} else {
			// last index --> ALL, working an indexed saved list
			XmlTools.writeAttrOptions(node, attribute, out);
		}
	}

	/**
	 * Fill the referenced HashMaps
	 */
	public static void initHashMaps() {
		clean();
		for (int i = IdTokens.REGISTER_INDEX_NAMES.length; i-- > 0;) {
			registerIndexName.put(IdTokens.REGISTER_INDEX_NAMES[i],
					IdTokens.REGISTER_INDEX_VALUES[i]);
		}
		for (int i = IdTargets.MODE_NAMES.length; i-- > 0;) {
			targetMode.put(IdTargets.MODE_NAMES[i], IdTargets.MODE_VALUES[i]);
		}
		for (int i = IdPositions.POSITION_NAMES.length; i-- > 0;) {
			position.put(IdPositions.POSITION_NAMES[i],
					IdPositions.POSITION_VALUES[i]);
		}
		for (int i = IdZones.ZONE_NAMES.length; i-- > 0;) {
			zones.put(IdZones.ZONE_NAMES[i], IdZones.ZONE_VALUES[i]);
		}
		for (int i = IdAbilities.ABILITIES_NAMES.length; i-- > 0;) {
			abilities.put(IdAbilities.ABILITIES_NAMES[i],
					IdAbilities.ABILITIES_VALUES[i]);
		}
		for (int i = IdConst.VALUES_NAME.length; i-- > 0;) {
			definedValuesName.put(IdConst.VALUES_NAME[i], IdConst.VALUES[i]);
		}
		for (int i = IdCardColors.CARD_COLOR_NAMES.length; i-- > 1;) {
			cardColorsName.put(IdCardColors.CARD_COLOR_NAMES[i],
					IdCardColors.CARD_COLOR_VALUES[i]);
		}
	}

	/**
	 * Clean tables.
	 */
	public static void clean() {
		registerIndexName.clear();
		targetMode.clear();
		position.clear();
		zones.clear();
		abilities.clear();
		definedValuesName.clear();
		cardColorsName.clear();
		aliasMap = null;
		XmlTest.clean();
		XmlAction.clean();
		XmlExpression.clean();
		XmlTbs.clean();
		XmlModifier.clean();
	}

	/**
	 * Write the TestOn instance corresponding to the given XSD attribute name.
	 * 
	 * @param out
	 *          the stream this enumeration would be written.
	 * @param xsdName
	 *          the XSD name of this TestOn.
	 * @throws IOException
	 *           error while writing.
	 */
	public static void writeTestOn(OutputStream out, String xsdName)
			throws IOException {
		if (xsdName != null) {
			try {
				TestOn.serialize(out, xsdName);
			} catch (IllegalArgumentException e) {
				XmlConfiguration.error(e.getMessage());
				TestOn.THIS.serialize(out);
			}
		} else if (defaultOnMeTag)
			TestOn.THIS.serialize(out);
		else
			TestOn.TESTED.serialize(out);
	}

	/**
	 * @param attribute
	 *          the optimization name.
	 * @return the optimization id.
	 */
	public static Optimization getOptimization(String attribute) {
		if (attribute == null)
			return net.sf.firemox.clickable.ability.Optimization.none;
		return Optimization.valueOf(attribute);
	}

	/**
	 * Return the operation code
	 * 
	 * @param operation
	 *          the alias
	 * @return the operation code
	 * @see net.sf.firemox.action.ModifyRegister
	 */
	public static IdOperation getOperation(String operation) {
		if (operation == null) {
			return IdOperation.ANY;
		}
		final IdOperation op = IdOperation.valueOfXsd(operation);
		if (op != null) {
			return op;
		}
		XmlConfiguration.error("Unknown operation : '" + operation + "'");
		return IdOperation.ANY;
	}

	/**
	 * Return the resolution code
	 * 
	 * @param resolution
	 *          the alias
	 * @return the resolution code
	 */
	public static Priority getPriority(String resolution) {
		if (resolution == null)
			return net.sf.firemox.clickable.ability.Priority.normal;
		return net.sf.firemox.clickable.ability.Priority.valueOf(resolution);
	}

	/**
	 * Return the corresponding code to the specified target mode.
	 * 
	 * @param modeAlias
	 *          the alias of mode
	 * @return the corresponding code to the specified target mode.
	 * @see IdTargets#MODE_NAMES
	 */
	public static int getTargetMode(String modeAlias) {
		if (modeAlias == null) {
			return IdTargets.CHOOSE;
		}
		final Integer obj = XmlTools.targetMode.get(modeAlias);
		if (obj != null) {
			return obj.intValue();
		}
		XmlConfiguration.error("Unknown target mode : '" + modeAlias + "'");
		return 0;
	}

	/**
	 * Return the corresponding code to the specified position name.
	 * 
	 * @param positionAlias
	 *          the alias of place
	 * @return the corresponding code to the specified position name.
	 * @see IdPositions#POSITION_NAMES
	 */
	public static Integer getPosition(String positionAlias) {
		if (positionAlias == null) {
			return IdPositions.ON_THE_TOP;
		}
		if(XmlTools.position.get(positionAlias) != null)
			return XmlTools.position.get(positionAlias);
		
		return Integer.parseInt(positionAlias);
	}

	/**
	 * Return the corresponding code to the specified zone name.
	 * 
	 * @param zoneAlias
	 *          the alias of place
	 * @return the corresponding code to the specified zone name.
	 * @see IdZones#ZONE_NAMES
	 */
	public static int getZone(String zoneAlias) {
		final Integer obj = XmlTools.zones.get(zoneAlias);
		if (obj != null) {
			return obj.intValue();
		}
		XmlConfiguration.error("Unknown zone : '" + zoneAlias + "'");
		return 0;
	}

	/**
	 * Return the corresponding code to the specified ability name.
	 * 
	 * @param abilityAlias
	 *          the alias of place
	 * @return the corresponding code to the specified ability name.
	 * @see IdAbilities#ABILITIES_NAMES
	 */
	public static int getAbility(String abilityAlias) {
		final Integer obj = XmlTools.abilities.get(abilityAlias);
		if (obj != null) {
			return obj.intValue();
		}
		XmlConfiguration.error("Unknown ability : '" + abilityAlias + "'");
		return 0;
	}

	/**
	 * Return the corresponding code to the specified string register index.
	 * 
	 * @param alias
	 *          the alias of value in it's string form
	 * @return the corresponding code to the specified string register index.
	 */
	public static int getValue(String alias) {
		final Integer value = getIntPriv(alias);
		if (value == null) {
			Integer obj = XmlTools.definedValuesName.get(alias);
			if (obj != null) {
				return obj.intValue();
			}
			obj = XmlTools.registerIndexName.get(alias);
			if (obj != null) {
				return obj.intValue();
			}
			// Thread.dumpStack();
			XmlConfiguration.error("Unknown alias : '" + alias + "'");
			return 0;
		}
		return value;
	}

	/**
	 * Return the corresponding code to the specified alias.
	 * 
	 * @param alias
	 *          the alias of value in it's string form
	 * @return the corresponding code to the specified alias.
	 */
	private static Integer getIntPriv(String alias) {
		if (alias == null) {
			XmlConfiguration.error("Null register/index.", true);
			return 0;
		}

		if (alias.length() > 0) {
			switch (alias.charAt(0)) {
			case '-':
				final int valueNeg = MToolKit.parseInt(alias);
				if (valueNeg == Integer.MIN_VALUE) {
					// alias not found
					return null;
				}
				return getNegativeConstant(valueNeg);
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
			case '0':
				final int value = MToolKit.parseInt(alias);
				if (value == Integer.MIN_VALUE) {
					// alias not found
					return null;
				}
				return value;
			default:
				// not managed number
			}
		}
		// it's an alias
		final Integer value = aliasMap.get(alias);
		if (value != null)
			return getNegativeConstant(value);
		return XmlTools.registerIndexName.get(alias);
	}

	private static int getNegativeConstant(int value) {
		if (value < 0) {
			if (value < -127) {
				throw new InternalError("Negative number cannot exceed -127 : " + value);
			}
			return IdConst.NEGATIVE_NUMBER_MASK | -value;
		}
		return value;
	}

	/**
	 * Return the corresponding code to the specified alias.
	 * 
	 * @param alias
	 *          the alias of value in it's string form
	 * @return the corresponding code to the specified alias.
	 */
	public static int getInt(String alias) {
		Integer value = getValue(alias);
		if (value == null) {
			XmlConfiguration.error("Unknown integer value : " + alias);
			return 0;
		}
		return value;
	}

	/**
	 * Return the corresponding code to the card color name
	 * 
	 * @param alias
	 *          the alias name.
	 * @return the alias value
	 */
	public static int getAliasValue(String alias) {
		int value = aliasMap.get(alias);
		if (value == Integer.MIN_VALUE) {
			// alias not found
			value = 0;
			XmlConfiguration.error("Unknown alias value '" + alias + "'");
		}
		return value;
	}

	/**
	 * Return the corresponding code to the card color name
	 * 
	 * @param colorName
	 *          the color alias
	 * @return the corresponding code to the card color name
	 */
	public static int getColor(String colorName) {
		final Integer obj = XmlTools.cardColorsName.get(colorName);
		if (obj != null) {
			return obj.intValue();
		}
		XmlConfiguration.error("Unknown color : '" + colorName + "'");
		return 0;
	}

	/**
	 * Return the corresponding code to the card type name
	 * 
	 * @param idCard
	 *          the card type name
	 * @return the corresponding code to the card type name
	 */
	public static int getIdCard(String idCard) {
		final Integer value = aliasMap.get(idCard);
		if (value == null || value == Integer.MIN_VALUE) {
			// alias not found
			XmlConfiguration.error("Unknown idcard : '" + idCard + "'");
			return 0;
		}
		return value;
	}

	/**
	 * Return the corresponding code to the list of card type name
	 * 
	 * @param list
	 *          is the list of card type name
	 * @return the corresponding code to the list of card type name.
	 */
	public static int getIdCards(String list) {
		// card type
		int idCard = 0;
		final String[] arrayid = list.split(" ");
		for (String id : arrayid) {
			idCard |= XmlTools.getIdCard(id);
		}
		return idCard;
	}

	/**
	 * Write to the specified output stream the values defined as linear list ' '
	 * separated values, or 'value' elements.
	 * 
	 * @param out
	 *          output stream where the card structure will be saved
	 * @param node
	 *          the XML test container structure
	 * @param tagAttr
	 * @throws IOException
	 *           error while writing.
	 */
	public static void writeList(OutputStream out, XmlParser.Node node,
			String tagAttr) throws IOException {
		if (node == null) {
			out.write(ListType.COLLECTION.ordinal());
			out.write(0);
			return;
		}
		final String listName;
		if (tagAttr.endsWith("y")) {
			listName = tagAttr + "ies";
		} else {
			listName = tagAttr + "s";
		}
		final String nodeAttr = node.getAttribute(listName);
		if (nodeAttr != null) {
			// TODO Support the java list and reference value

			// the value is directly specified : linear list / range
			final String[] values;
			if (nodeAttr.indexOf("..") != -1) {
				out.write(ListType.RANGE.ordinal());
				values = nodeAttr.split("\\.\\.");
			} else {
				out.write(ListType.COLLECTION.ordinal());
				values = nodeAttr.split(" ");
			}
			out.write(values.length);
			for (String value : values) {
				writeSimpleValue(out, value);
			}
		} else {
			// the value is defined if an inner-element, it's a complex value
			out.write(ListType.COLLECTION.ordinal());
			Node valuesNode = node.get(listName);
			if (valuesNode == null) {
				out.write(0);
			} else {
				final List<Node> values = valuesNode.getNodes("value");
				out.write(values.size());
				for (XmlParser.Node value : values) {
					writeComplexValue(out, value);
				}
			}
		}
	}

	/**
	 * Write to the specified output stream the 16bits integer value.
	 * 
	 * @param out
	 *          output stream where the card structure will be saved
	 * @param value
	 *          the simple value to write.
	 * @throws IOException
	 *           error while writing.
	 */
	public static void writeSimpleValue(OutputStream out, String value)
			throws IOException {
		final AbstractValue abstractValue = AbstractValue.valueOfXsd(value);
		if (abstractValue != null) {
			IdOperation.ABSTRACT_VALUE.serialize(out);
			abstractValue.serialize(out);
		} else {
			TestOn testOn = TestOn.valueOfXsd(value);
			if (testOn != null) {
				IdOperation.TEST_ON.serialize(out);
				testOn.serialize(out);
			} else {
				Integer intValue = getIntPriv(value);
				if (intValue == null) {
					XmlConfiguration.error("Unknown alias : '" + value + "'");
					writeConstant(out, 0);
				} else {
					writeConstant(out, intValue);
				}
			}
		}
	}

	/**
	 * Write to the specified output stream the 16bits integer value.
	 * 
	 * @param out
	 *          output stream where the card structure will be saved
	 * @param value
	 *          the simple value to write.
	 * @throws IOException
	 *           error while writing.
	 */
	public static void writeConstant(OutputStream out, int value)
			throws IOException {
		IdOperation.INT_VALUE.serialize(out);
		MToolKit.writeInt16(out, getNegativeConstant(value));
	}

	/**
	 * @param out
	 *          output stream where the card structure will be saved
	 * @param expr
	 *          complex expression node to write
	 * @throws IOException
	 *           error while writing.
	 */
	public static void writeComplexValue(OutputStream out, XmlParser.Node expr)
			throws IOException {
		// try getting the value as register access : register name + index
		String register = expr.getAttribute("register");
		if (register != null) {
			if ("true".equals(expr.getAttribute("base"))) {
				IdOperation.BASE_REGISTER_INT_VALUE.serialize(out);
			} else {
				IdOperation.REGISTER_ACCESS.serialize(out);
			}
			Register.valueOfXsd(register).serialize(out);
			writeAttrOptions(expr, "index", out);
		} else {
			// this is a complex expression : add, minus, method, counter, ...
			final Iterator<?> it = expr.iterator();
			while (it.hasNext()) {
				Object obj = it.next();
				if (obj instanceof XmlParser.Node) {
					XmlExpression.getExpression(((XmlParser.Node) obj).getTag())
							.buildMdb((XmlParser.Node) obj, out);
					return;
				}
			}
			XmlConfiguration
					.error("Element '"
							+ expr.getParent().getTag()
							+ "' must contain a complex operation, or have value defined as attribute. Context="
							+ expr.getParent());
		}

	}

	/**
	 * @param node
	 *          the XML test container structure
	 * @param tagAttr
	 * @param out
	 *          output stream where the card structure will be saved
	 * @throws IOException
	 *           error while writing.
	 */
	public static void writeAttrOptions(XmlParser.Node node, String tagAttr,
			OutputStream out) throws IOException {
		writeAttrOptionsDefault(node, tagAttr, out, null);
	}

	/**
	 * @param node
	 *          the XML test container structure
	 * @param tagAttr
	 * @param out
	 *          output stream where the card structure will be saved
	 * @param defaultValue
	 *          the default value to use if neither attribute, neither element has
	 *          been found.
	 * @throws IOException
	 *           error while writing.
	 */
	public static void writeAttrOptionsDefault(XmlParser.Node node,
			String tagAttr, OutputStream out, String defaultValue) throws IOException {
		if (defaultValue != null && node.getAttribute(tagAttr) == null
				&& node.get(tagAttr) == null) {
			node.addAttribute(new XmlParser.Attribute(tagAttr, defaultValue));
		}
		final String nodeAttr = node.getAttribute(tagAttr);
		if (nodeAttr != null) {
			// the value is directly specified : simple integer or register access
			if (node.getAttribute(tagAttr + "-class") != null
					&& !int.class.getName().equals(node.getAttribute(tagAttr + "-class"))
					&& !Integer.class.getName().equals(
							node.getAttribute(tagAttr + "-class"))) {
				// No integer expression
				IdOperation.OBJECT_VALUE.serialize(out);
				MToolKit.writeString(out, node.getAttribute(tagAttr + "-class"));
				MToolKit.writeString(out, node.getAttribute(tagAttr));
			} else if (nodeAttr.startsWith("%")) {
				// This is reference value
				IdOperation.REF_VALUE.serialize(out);
				MToolKit.writeString(out, nodeAttr);
			} else {
				writeSimpleValue(out, nodeAttr);
			}
		} else {
			// the value is defined if an inner-element, it's a complex value
			final XmlParser.Node expr = node.get(tagAttr);
			if (expr == null) {
				XmlConfiguration.error("Neither element '" + tagAttr
						+ "' neither attribute '" + tagAttr
						+ "' have been found in element '" + node.getTag()
						+ "'.\n\t Context:\n" + node);
				return;
			}
			writeComplexValue(out, expr);
		}
	}

	/**
	 * @param node
	 * @param tagAttr
	 * @param out
	 * @param nameSpace
	 * @throws IOException
	 *           error while writing.
	 */
	public static void writeAttrOptions(XmlParser.Node node, String tagAttr,
			OutputStream out, String nameSpace) throws IOException {
		final String colorAttr = node.getAttribute(tagAttr);
		if (colorAttr != null) {
			final String method = "get" + StringUtils.capitalize(nameSpace);
			try {
				final Integer retVal = (Integer) XmlTools.class.getMethod(method,
						String.class).invoke(null, colorAttr);
				if (retVal == null) {
					XmlConfiguration.error("Unknown " + nameSpace + " : '" + colorAttr
							+ "'");
					writeConstant(out, 0);
				} else {
					writeConstant(out, retVal);
				}
			} catch (Throwable e2) {
				XmlConfiguration.error("Error found in 'get" + nameSpace + "' : "
						+ e2.getMessage());
				writeConstant(out, 0);
			}
		} else {
			final XmlParser.Node expr = node.get(tagAttr);
			if (expr == null) {
				XmlConfiguration.error("Neither element '" + tagAttr
						+ "' neither attribute '" + tagAttr
						+ "' have been found in element '" + node.getTag() + "'. Context="
						+ node);
				return;
			}
			writeComplexValue(out, expr);
		}
	}

	/**
	 * @param attribute
	 *          the message name.
	 * @return the message id.
	 */
	public static IdMessageBox getMessageType(String attribute) {
		if (attribute == null) {
			return IdMessageBox.ok;
		}
		IdMessageBox messageBox = IdMessageBox.valueOf(attribute);
		if (messageBox == null) {
			return IdMessageBox.ok;
		}
		return messageBox;
	}

	/**
	 * The defined user alias
	 */
	public static Map<String, Integer> aliasMap = null;

	/**
	 * This field must be set by activated ability, triggered, and counters to
	 * known the default value for the 'on' attribute
	 */
	public static boolean defaultOnMeTag;

	/**
	 * This field must be set by actions needing some pre-check test such as
	 * target action. When is <code>true</code>, the last build test does not
	 * refer to a ability's runtime register such as 'stack' register.
	 */
	public static boolean testCanBePreempted;

	/**
	 * Return the named node from the given node. This node may be defined in an
	 * external xml file.
	 * 
	 * @param node
	 *          the parent node.
	 * @param nodeName
	 *          the node name.
	 * @return the named node from the given node.
	 */
	public static Node getExternalizableNode(Node node, String nodeName) {
		final Node references = node.get(nodeName);
		if (references.getAttribute("file") != null) {
			// Load the references file
			try {
				return new XmlParser(XmlConfiguration.getOptions().isXsdValidation())
						.parse(MToolKit.getUrl(references.getAttribute("file")).getPath());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return references;
	}
}