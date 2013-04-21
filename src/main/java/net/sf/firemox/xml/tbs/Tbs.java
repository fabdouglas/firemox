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
package net.sf.firemox.xml.tbs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import net.sf.firemox.modifier.ModifierType;
import net.sf.firemox.operation.IdOperation;
import net.sf.firemox.token.IdPropertyType;
import net.sf.firemox.token.IdTokens;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.tools.PairStringInt;
import net.sf.firemox.ui.component.CardPropertiesPanel;
import net.sf.firemox.xml.XmlAction;
import net.sf.firemox.xml.XmlConfiguration;
import net.sf.firemox.xml.XmlModifier;
import net.sf.firemox.xml.XmlParser;
import net.sf.firemox.xml.XmlTbs;
import net.sf.firemox.xml.XmlTest;
import net.sf.firemox.xml.XmlToMDB;
import net.sf.firemox.xml.XmlTools;
import net.sf.firemox.xml.XmlParser.Node;

import org.apache.commons.io.FilenameUtils;

/**
 * This class represents an implementation of the XmlToMDB interface that
 * converts a XML tbs node to its binary form to the given OutputSource.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan</a>
 * @since 0.82
 * @since 0.83 damage types are exported
 */
public class Tbs implements XmlToMDB {

	/**
	 * Converts the given tbs XML node to its binary form writing in the given
	 * OutputStream.
	 * 
	 * @see net.sf.firemox.token.IdTokens#PLAYER_REGISTER_SIZE
	 * @see net.sf.firemox.stack.phasetype.PhaseType
	 * @see net.sf.firemox.clickable.ability.SystemAbility
	 * @see net.sf.firemox.tools.StatePicture
	 * @param node
	 *          the XML card structure
	 * @param out
	 *          output stream where the card structure will be saved
	 * @return the amount of written action in the output.
	 * @throws IOException
	 *           error during the writing.
	 * @see net.sf.firemox.deckbuilder.MdbLoader#loadMDB(String, int)
	 */
	public final int buildMdb(Node node, OutputStream out) throws IOException {
		final FileOutputStream fileOut = (FileOutputStream) out;
		referencedTest = new HashMap<String, Node>();
		referencedAbilities = new HashMap<String, Node>();
		referencedActions = new HashMap<String, List<Node>>();
		referencedAttachments = new HashMap<String, Node>();
		referencedNonMacroActions = new HashSet<String>();
		referencedNonMacroAttachments = new HashSet<String>();
		macroActions = new Stack<List<Node>>();
		resolveReferences = false;

		MToolKit.writeString(out, node.getAttribute("name"));
		System.out.println("Building " + node.getAttribute("name") + " rules...");
		MToolKit.writeString(out, node.getAttribute("version"));
		MToolKit.writeString(out, node.getAttribute("author"));
		MToolKit.writeString(out, node.getAttribute("comment"));

		// Write database configuration
		final Node database = node.get("database-properties");
		Map<String, IdPropertyType> properties = new HashMap<String, IdPropertyType>();
		for (java.lang.Object obj : database) {
			if (obj instanceof Node) {
				Node nodePriv = (Node) obj;
				String propertyType = nodePriv.getAttribute("type");
				IdPropertyType propertyId;
				if (propertyType == null || propertyType.equals(String.class.getName())) {
					if ("true".equalsIgnoreCase(nodePriv.getAttribute("translate"))) {
						propertyId = IdPropertyType.SIMPLE_TRANSLATABLE_PROPERTY;
					} else {
						propertyId = IdPropertyType.SIMPLE_PROPERTY;
					}
				} else if ("java.util.List".equals(propertyType)) {
					if ("true".equalsIgnoreCase(nodePriv.getAttribute("translate"))) {
						propertyId = IdPropertyType.COLLECTION_TRANSLATABLE_PROPERTY;
					} else {
						propertyId = IdPropertyType.COLLECTION_PROPERTY;
					}
				} else {
					throw new InternalError("Unknow property type '" + propertyType + "'");
				}
				properties.put(nodePriv.getAttribute("name"), propertyId);
			}
		}
		out.write(properties.size());
		for (Map.Entry<String, IdPropertyType> entry : properties.entrySet()) {
			entry.getValue().serialize(out);
			MToolKit.writeString(out, entry.getKey());
		}

		MToolKit.writeString(out, node.getAttribute("art-url"));
		MToolKit.writeString(out, node.getAttribute("back-picture"));
		MToolKit.writeString(out, node.getAttribute("damage-picture"));
		MToolKit.writeString(out, (String) node.get("licence").get(0));

		// Manas
		final Node manas = node.get("mana-symbols");

		// Colored manas
		final Node colored = manas.get("colored");
		MToolKit.writeString(out, colored.getAttribute("url"));
		MToolKit.writeString(out, colored.getAttribute("big-url"));
		for (java.lang.Object obj : colored) {
			if (obj instanceof Node) {
				Node nodePriv = (Node) obj;
				out.write(XmlTools.getValue(nodePriv.getAttribute("name")));
				MToolKit.writeString(out, nodePriv.getAttribute("picture"));
				MToolKit.writeString(out, nodePriv.getAttribute("big-picture"));
			}
		}

		// Colorless manas
		final Node colorless = manas.get("colorless");
		MToolKit.writeString(out, colorless.getAttribute("url"));
		MToolKit.writeString(out, colorless.getAttribute("big-url"));
		MToolKit.writeString(out, colorless.getAttribute("unknown"));
		out.write(colorless.getNbNodes());
		for (java.lang.Object obj : colorless) {
			if (obj instanceof Node) {
				final Node nodePriv = (Node) obj;
				out.write(Integer.parseInt(nodePriv.getAttribute("amount")));
				MToolKit.writeString(out, nodePriv.getAttribute("picture"));
			}
		}
		
		// Hybrid manas
		final Node hybrid = manas.get("hybrid");
		MToolKit.writeString(out, hybrid.getAttribute("url"));
		out.write(hybrid.getNbNodes());
		for (Object obj : hybrid) {
			if (obj instanceof Node) {
				final Node nodePriv = (Node) obj;
				MToolKit.writeString(out, nodePriv.getAttribute("name"));
				MToolKit.writeString(out, nodePriv.getAttribute("picture"));
			}
		}
		
		// Hybrid manas
		final Node phyrexian = manas.get("phyrexian");
		MToolKit.writeString(out, phyrexian.getAttribute("url"));
		out.write(phyrexian.getNbNodes());
		for (Object obj : phyrexian) {
			if (obj instanceof Node) {
				final Node nodePriv = (Node) obj;
				MToolKit.writeString(out, nodePriv.getAttribute("name"));
				MToolKit.writeString(out, nodePriv.getAttribute("picture"));
			}
		}

		// Prepare the shortcut to card's bytes offset
		final long shortcutCardBytes = fileOut.getChannel().position();
		MToolKit.writeInt24(out, 0);

		// Write the user defined deck constraints
		final Set<String> definedConstraints = new HashSet<String>();
		final Node deckConstraintRoot = XmlTools.getExternalizableNode(node,
				"deck-constraints");
		final List<Node> deckConstraints = deckConstraintRoot
				.getNodes("deck-constraint");
		XmlTools.writeAttrOptions(deckConstraintRoot, "deckbuilder-min-property",
				out);
		XmlTools.writeAttrOptions(deckConstraintRoot, "deckbuilder-max-property",
				out);
		XmlTools.writeAttrOptions(deckConstraintRoot, "master", out);
		out.write(deckConstraints.size());
		for (Node deckConstraint : deckConstraints) {
			// Write the constraint key name
			String deckName = deckConstraint.getAttribute("name");
			MToolKit.writeString(out, deckName);

			// Write extend
			String extend = deckConstraint.getAttribute("extends");
			MToolKit.writeString(out, extend);
			if (extend != null && extend.length() > 0
					&& !definedConstraints.contains(extend)) {
				throw new RuntimeException(
						"'"
								+ deckName
								+ "' is supposed extending '"
								+ extend
								+ "' but has not been found. Note that declaration order is important.");
			}

			// Write the constraint
			XmlTools.defaultOnMeTag = false;
			XmlTest.getTest("test").buildMdb(deckConstraint, out);
			definedConstraints.add(deckName);
		}
		// END OF HEADER

		// additional zones
		final List<XmlParser.Node> additionalZones = node.get("layouts").get(
				"zones").get("additional-zones").getNodes("additional-zone");
		out.write(additionalZones.size());
		int zoneId = IdZones.FIRST_ADDITIONAL_ZONE;
		for (XmlParser.Node additionalZone : additionalZones) {
			final String zoneName = additionalZone.getAttribute("name");
			if (zoneId > IdZones.LAST_ADDITIONAL_ZONE) {
				throw new RuntimeException("Cannot add more additional-zone ("
						+ zoneName + "), increase core limitation");
			}
			XmlTools.zones.put(zoneName, zoneId++);
			MToolKit.writeString(out, zoneName);
			MToolKit.writeString(out, additionalZone.getAttribute("layout-class"));
			MToolKit.writeString(out, additionalZone.getAttribute("constraint-you"));
			MToolKit.writeString(out, additionalZone
					.getAttribute("constraint-opponent"));
		}

		// Initial zone id
		out.write(XmlTools.getZone(node.get("layouts").get("zones").getAttribute(
				"default-zone")));

		// the references
		final Node references = XmlTools.getExternalizableNode(node, "references");

		// the tests references
		System.out.println("\tshared tests...");
		XmlTools.defaultOnMeTag = true;
		final Node tests = references.get("tests");
		if (tests == null) {
			out.write(0);
		} else {
			out.write(tests.getNbNodes());
			for (java.lang.Object obj : tests) {
				if (obj instanceof Node) {
					String ref = ((Node) obj).getAttribute("reference-name").toString();
					referencedTest.put(ref, (Node) obj);
					MToolKit.writeString(out, ref);
					for (java.lang.Object objTest : (Node) obj) {
						if (objTest instanceof Node) {
							final Node node1 = (Node) objTest;
							try {
								XmlTest.getTest(node1.getTag()).buildMdb(node1, out);
							} catch (Throwable ie) {
								XmlConfiguration.error("In referenced test '" + ref + "' : "
										+ ie.getMessage() + ". Context=" + objTest);
							}
							break;
						}
					}
				}
			}
		}

		// the action references (not included into MDB)
		final Node actions = references.get("actions");
		System.out.print("\tactions : references (inlined in MDB)");
		if (actions != null) {
			for (java.lang.Object obj : actions) {
				if (obj instanceof Node) {
					final String ref = ((Node) obj).getAttribute("reference-name");
					final String globalName = ((Node) obj).getAttribute("name");
					// do not accept macro?
					if ("false".equals(((Node) obj).getAttribute("macro"))) {
						referencedNonMacroActions.add(ref);
					}
					final List<Node> actionList = new ArrayList<Node>();
					boolean firstIsDone = false;
					for (java.lang.Object actionI : (Node) obj) {
						if (actionI instanceof Node) {
							final Node action = (Node) actionI;
							// add action to the action list and replace the name of
							// sub-actions
							if (action.getAttribute("name") == null) {
								if (globalName != null) {
									if (firstIsDone) {
										action.addAttribute(new XmlParser.Attribute("name", "%"
												+ globalName));
									} else {
										action.addAttribute(new XmlParser.Attribute("name",
												globalName));
										firstIsDone = true;
									}
								} else if (firstIsDone) {
									action
											.addAttribute(new XmlParser.Attribute("name", "%" + ref));
								} else {
									action.addAttribute(new XmlParser.Attribute("name", ref));
									firstIsDone = true;
								}
							} else if (!firstIsDone) {
								firstIsDone = true;
							}
							actionList.add((Node) actionI);
						}
					}
					// add this reference
					referencedActions.put(ref, actionList);

				}
			}
		}

		// the attachment references (not included into MDB)
		final Node attachments = references.get("attachments");
		System.out.print(", attachments");
		if (attachments != null) {
			for (java.lang.Object obj : attachments) {
				if (obj instanceof Node) {
					final String ref = ((Node) obj).getAttribute("reference-name");
					// do not accept macro?
					if ("false".equals(((Node) obj).getAttribute("macro"))) {
						referencedNonMacroAttachments.add(ref);
					}
					// add this reference
					referencedAttachments.put(ref, (Node) obj);
				}
			}
		}

		// action pictures
		final Node actionsPictures = node.get("action-pictures");
		System.out.print(", pictures");
		if (actionsPictures == null) {
			out.write(0);
			out.write('\0');
		} else {
			MToolKit.writeString(out, actionsPictures.getAttribute("url"));
			out.write(actionsPictures.getNbNodes());
			for (java.lang.Object obj : actionsPictures) {
				if (obj instanceof Node) {
					final Node actionPicture = (Node) obj;
					MToolKit.writeString(out, actionPicture.getAttribute("name"));
					MToolKit.writeString(out, actionPicture.getAttribute("picture"));
				}
			}
		}

		// constraints on abilities linked to action
		final Node constraints = node.get("action-constraints");
		System.out.println(", constraints");
		if (constraints == null) {
			out.write(0);
		} else {
			out.write(constraints.getNbNodes());
			for (java.lang.Object obj : constraints) {
				if (obj instanceof Node) {
					final Node constraint = (Node) obj;
					for (java.lang.Object objA : constraint.get("actions")) {
						if (objA instanceof Node) {
							XmlAction.getAction(((Node) objA).getTag()).buildMdb((Node) objA,
									out);
							break;
						}
					}
					if ("or".equals(constraint.getAttribute("operation"))) {
						IdOperation.OR.serialize(out);
					} else {
						IdOperation.AND.serialize(out);
					}
					XmlTest.getTest("test").buildMdb(constraint.get("test"), out);
				}
			}
		}

		// objects defined in this TBS
		System.out.println("\tobjects...");
		final List<Node> objects = node.get("objects").getNodes("object");
		out.write(objects.size());
		for (Node object : objects) {
			// write object
			final XmlParser.Attribute objectName = new XmlParser.Attribute("name",
					object.getAttribute("name"));
			boolean onePresent = false;
			for (java.lang.Object modifierTmp : object) {
				if (modifierTmp instanceof Node) {
					// write the 'has-next' flag
					if (onePresent) {
						out.write(1);
					}

					// write the object-modifier
					final Node modifier = (Node) modifierTmp;
					modifier.addAttribute(objectName);
					XmlModifier.getModifier(modifier.getTag()).buildMdb(modifier, out);

					// Write the 'paint' flag
					if (onePresent) {
						out.write(0);
					} else {
						out.write(1);
					}
					onePresent = true;
				}
			}
			if (!onePresent) {
				// no modifier associated to this object, we write a virtual ONE
				ModifierType.REGISTER_MODIFIER.serialize(out);
				XmlModifier.buildMdbModifier(object, out);

				// Write the modified register index && value
				XmlTools.writeConstant(out, 0);
				XmlTools.writeConstant(out, 0);
				IdOperation.ADD.serialize(out);

				// Write the 'paint' flag
				out.write(1);
			}

			// Write the 'has-next' flag
			out.write(0);
		}

		// the abilities references
		System.out.println("\tshared abilities...");
		Node abilities = references.get("abilities");
		out.write(abilities.getNbNodes());
		macroActions.push(null);
		for (Node ability : abilities.getNodes("ability")) {
			String ref = ability.getAttribute("reference-name").toString();
			referencedAbilities.put(ref, ability);
			MToolKit.writeString(out, ref);
			try {
				Node node1 = (Node) ability.get(1);
				XmlTbs.getTbsComponent(node1.getTag()).buildMdb(node1, out);
			} catch (Throwable ie) {
				XmlConfiguration.error("Error In referenced ability '" + ref + "' : "
						+ ie.getMessage() + "," + ie + ". Context=" + ability);
			}
		}
		macroActions.pop();

		// damage types name
		System.out.println("\tdamage types...");
		Collections.sort(XmlConfiguration.EXPORTED_DAMAGE_TYPES);
		int count = XmlConfiguration.EXPORTED_DAMAGE_TYPES.size();
		out.write(count);
		for (int i = 0; i < count; i++) {
			final PairStringInt pair = XmlConfiguration.EXPORTED_DAMAGE_TYPES.get(i);
			MToolKit.writeString(out, pair.text);
			MToolKit.writeInt16(out, pair.value);
		}

		/**
		 * <ul>
		 * CARD'S PART :
		 * <li>state pictures
		 * <li>tooltip filters
		 * <li>exported types name
		 * <li>exported sorted properties name
		 * <li>implemented cards
		 * </ul>
		 */

		// state pictures
		System.out.println("\tstates");
		final Node states = node.get("state-pictures");
		if (states == null) {
			out.write(0);
		} else {
			out.write(states.getNbNodes());
			for (java.lang.Object obj : states) {
				if (obj instanceof Node) {
					final Node ns = (Node) obj;
					MToolKit.writeString(out, ns.getAttribute("picture"));
					MToolKit.writeString(out, ns.getAttribute("name"));
					MToolKit.writeInt16(out, XmlTools.getInt(ns.getAttribute("state")));
					out.write(XmlTools.getInt(ns.getAttribute("index")));
					final int x = XmlTools.getInt(ns.getAttribute("x"));
					final int y = XmlTools.getInt(ns.getAttribute("y"));
					if (MToolKit.getConstant(x) == -1 && MToolKit.getConstant(y) != -1
							|| MToolKit.getConstant(x) != -1 && MToolKit.getConstant(y) == -1) {
						XmlConfiguration
								.error("In state-picture '-1' value is allowed if and only if x AND y have this value.");
					}
					MToolKit.writeInt16(out, XmlTools.getInt(ns.getAttribute("x")));
					MToolKit.writeInt16(out, XmlTools.getInt(ns.getAttribute("y")));
					MToolKit.writeInt16(out, XmlTools.getInt(ns.getAttribute("width")));
					MToolKit.writeInt16(out, XmlTools.getInt(ns.getAttribute("height")));
					XmlTest.getTest("test").buildMdb(node.get("display-test"), out);
				}
			}
		}

		// tooltip filters
		System.out.println("\ttooltip filters...");
		final Node ttFilters = node.get("tooltip-filters");
		XmlTools.defaultOnMeTag = false;
		if (ttFilters == null) {
			out.write(0);
		} else {
			out.write(ttFilters.getNbNodes());
			for (java.lang.Object obj : ttFilters) {
				if (obj instanceof Node) {
					buildMdbTooltipFilter((Node) obj, out);
				}
			}
		}

		// exported types name
		System.out.println("\texported type...");
		Collections.sort(XmlConfiguration.EXPORTED_TYPES);
		int nbTypes = XmlConfiguration.EXPORTED_TYPES.size();
		out.write(nbTypes);
		for (int i = 0; i < nbTypes; i++) {
			final PairStringInt pair = XmlConfiguration.EXPORTED_TYPES.get(i);
			MToolKit.writeString(out, pair.text);
			MToolKit.writeInt16(out, pair.value);
		}

		// exported sorted properties name and associated pictures
		System.out.println("\tproperties...");
		Collections.sort(XmlConfiguration.EXPORTED_PROPERTIES);
		final int nbProperties = XmlConfiguration.EXPORTED_PROPERTIES.size();
		MToolKit.writeInt16(out, nbProperties);
		for (int i = 0; i < nbProperties; i++) {
			final PairStringInt pair = XmlConfiguration.EXPORTED_PROPERTIES.get(i);
			MToolKit.writeInt16(out, pair.value);
			MToolKit.writeString(out, pair.text);
		}
		MToolKit.writeInt16(out, XmlConfiguration.PROPERTY_PICTURES.size());
		for (Map.Entry<Integer, String> entry : XmlConfiguration.PROPERTY_PICTURES
				.entrySet()) {
			MToolKit.writeInt16(out, entry.getKey());
			MToolKit.writeString(out, entry.getValue());
		}

		/**
		 * <ul>
		 * STACK MANAGER'S PART :
		 * <li>first player registers
		 * <li>second player registers
		 * <li>abortion zone
		 * </ul>
		 */
		// player registers
		System.out.println("\tinitial registers...");
		Node registers = node.get("registers-first-player");
		final byte[] registersBytes = new byte[IdTokens.PLAYER_REGISTER_SIZE];
		if (registers != null) {
			final List<Node> list = registers.getNodes("register");
			for (Node register : list) {
				registersBytes[XmlTools.getInt(register.getAttribute("index"))] = (byte) XmlTools
						.getInt(register.getAttribute("value"));
			}
		}
		out.write(registersBytes);
		registers = node.get("registers-second-player");
		final byte[] registersBytes2 = new byte[IdTokens.PLAYER_REGISTER_SIZE];
		if (registers != null) {
			final List<Node> list = registers.getNodes("register");
			for (Node register : list) {
				registersBytes2[XmlTools.getInt(register.getAttribute("index"))] = (byte) XmlTools
						.getInt(register.getAttribute("value"));
			}
		}
		out.write(registersBytes2);

		/*
		 * TODO abortion zone
		 */
		final Node refAbilities = node.get("abilities");
		out.write(XmlTools.getZone(refAbilities.getAttribute("abortionzone")));

		// additional costs
		System.out.println("\tadditional-costs...");
		final List<Node> additionalCosts = node.get("additional-costs").getNodes(
				"additional-cost");
		out.write(additionalCosts.size());
		for (Node additionalCost : additionalCosts) {
			XmlTest.getTest("test").buildMdb(additionalCost.get("test"), out);
			XmlTbs.writeActionList(additionalCost.get("cost"), out);
		}

		/**
		 * <ul>
		 * EVENT MANAGER'S PART :
		 * <li>phases
		 * <li>turn-structure
		 * <li>first phase index
		 * </ul>
		 */
		// phases
		System.out.println("\tphases...");
		final Node phases = node.get("phases");
		out.write(phases.getNbNodes());
		for (java.lang.Object obj : phases) {
			if (obj instanceof Node) {
				buildMdbPhaseType((Node) obj, out);
			}
		}

		// turn structure
		final String list = phases.getAttribute("turn-structure");
		System.out.println("\tturn structure...");
		final String[] arrayid = list.split(" ");
		out.write(arrayid.length);
		for (String id : arrayid) {
			out.write(XmlTools.getAliasValue(id));
		}

		// first phase index for first turn
		out.write(Integer.parseInt(phases.getAttribute("start")));

		// state based abilities
		System.out.println("\trule abilities...");
		out.write(refAbilities.getNbNodes());
		for (java.lang.Object obj : refAbilities) {
			if (obj instanceof Node) {
				try {
					XmlTbs.getTbsComponent(((Node) obj).getTag()).buildMdb((Node) obj,
							out);
				} catch (Throwable ie) {
					XmlConfiguration.error("Error in system ability '"
							+ ((Node) obj).getAttribute("name") + "' : " + ie.getMessage()
							+ ". Context=" + obj);
					break;
				}
			}
		}

		// static-modifiers
		System.out.println("\tstatic-modifiers...");
		final Node modifiers = node.get("static-modifiers");
		if (modifiers == null) {
			out.write(0);
		} else {
			out.write(modifiers.getNbNodes());
			for (java.lang.Object obj : modifiers) {
				if (obj instanceof Node) {
					XmlModifier.getModifier("static-modifier").buildMdb((Node) obj, out);
				}
			}
		}

		// layouts
		System.out.println("\tlayouts...");
		final Node layouts = node.get("layouts");

		// initialize task pane layout
		writeTaskPaneElement(layouts.get("common-panel").get("card-details").get(
				"properties"), out);

		// Sector of play zone for layouts
		List<Node> sectors = layouts.get("zones").get("play").getNodes("sector");
		out.write(sectors.size());
		for (XmlParser.Node sector : sectors) {
			XmlTest.getTest("test").buildMdb(sector, out);
			MToolKit.writeString(out, sector.getAttribute("constraint"));
		}

		// Update the shortcut to the first bytes of cards
		final long cardBytesPosition = fileOut.getChannel().position();
		fileOut.getChannel().position(shortcutCardBytes);
		MToolKit.writeInt24(out, (int) cardBytesPosition);
		fileOut.getChannel().position(cardBytesPosition);

		// cards bytes + names
		resolveReferences = true;
		System.out.println("Processing cards...");
		String xmlFile = node.getAttribute("xmlFile");
		try {
			final String baseName = FilenameUtils.getBaseName(xmlFile);
			XmlTbs.updateMdb(baseName, out);
		} catch (Throwable e2) {
			XmlConfiguration.error("Error found in cards parsing of rule '" + xmlFile
					+ "' : " + e2.getMessage());
		}

		System.out.println(node.getAttribute("name") + " rules finished");
		return 0;
	}

	private void writeTaskPaneElement(Node node, OutputStream out)
			throws IOException {
		final List<Node> elements = node.getNodes("menu-element");
		final List<Node> attributes = node.getNodes("menu-attribute");
		out.write((elements == null ? 0 : elements.size())
				+ (attributes == null ? 0 : attributes.size()));
		for (java.lang.Object nestedNode : node) {
			if (nestedNode instanceof Node) {
				final Node task = (Node) nestedNode;
				if ("menu-element".equals(task.getTag())) {
					out.write(CardPropertiesPanel.NESTED_ELEMENT);
					MToolKit.writeString(out, task.getAttribute("name"));
					writeTaskPaneElement(task, out);
				} else if ("menu-attribute".equals(task.getTag())) {
					out.write(CardPropertiesPanel.ATTRIBUTE);
					MToolKit.writeString(out, task.getAttribute("type").toLowerCase());
					MToolKit.writeString(out, task.getAttribute("name"));
					MToolKit.writeString(out, task.getAttribute("value"));
				} else {
					XmlConfiguration
							.warning("non supported task type : " + task.getTag());
				}
			}
		}

	}

	/**
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>phase name + '\0' [...]</li>
	 * <li>phase identifier [1]</li>
	 * <li>empty stack playable, idCards for current player [2]</li>
	 * <li>empty stack playable, idCards for non-current player [2]</li>
	 * <li>middle resolution, playable idCards for current player [2]</li>
	 * <li>middle resolution, playable idCards for non-current player [2]</li>
	 * </ul>
	 * 
	 * @param node
	 *          the XML phase type structure
	 * @param out
	 *          output stream where the card structure will be saved
	 * @throws IOException
	 *           error during the
	 * @see net.sf.firemox.stack.phasetype.PhaseType
	 */
	public static final void buildMdbPhaseType(Node node, OutputStream out)
			throws IOException {
		MToolKit.writeString(out, node.getAttribute("name"));
		out.write(XmlTools.getAliasValue(node.getAttribute("name")));
		MToolKit.writeInt16(out, XmlTools.getIdCards(node
				.getAttribute("playable-empty-stack-you")));
		MToolKit.writeInt16(out, XmlTools.getIdCards(node
				.getAttribute("playable-empty-stack-opponent")));
		MToolKit.writeInt16(out, XmlTools.getIdCards(node
				.getAttribute("playable-middle-resolution-you")));
		MToolKit.writeInt16(out, XmlTools.getIdCards(node
				.getAttribute("playable-middle-resolution-opponent")));
	}

	/**
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>display powerANDtoughness yes=1,no=0 [1]</li>
	 * <li>display states yes=1,no=0 [1]</li>
	 * <li>display types yes=1,no=0 [1]</li>
	 * <li>display colors yes=1,no=0 [1]</li>
	 * <li>display properties yes=1,no=0 [1]</li>
	 * <li>display damage yes=1,no=0 [1]</li>
	 * <li>filter [...]</li>
	 * </ul>
	 * 
	 * @param node
	 *          the XML structure
	 * @param out
	 *          output stream where the card structure will be saved
	 * @throws IOException
	 *           error during the writing.
	 */
	public static final void buildMdbTooltipFilter(Node node, OutputStream out)
			throws IOException {

		out.write("true".equals(node.getAttribute("powerANDtoughness")) ? 1 : 0);
		out.write("true".equals(node.getAttribute("states")) ? 1 : 0);
		out.write("true".equals(node.getAttribute("types")) ? 1 : 0);
		out.write("true".equals(node.getAttribute("colors")) ? 1 : 0);
		out.write("true".equals(node.getAttribute("properties")) ? 1 : 0);
		out.write("true".equals(node.getAttribute("damage")) ? 1 : 0);
		XmlTest.getTest("test").buildMdb(node.get("filter"), out);
	}

	/**
	 * Push an action (accepting <code>null</code>) for macro.
	 * 
	 * @param node
	 *          the node to give to the next macro.
	 */
	public static void pushMacroAction(List<Node> node) {
		macroActions.push(node);
	}

	/**
	 * Return the last given action to a macro.
	 * 
	 * @return the last given action to a macro.
	 */
	public static List<Node> peekMacroAction() {
		return macroActions.peek();
	}

	/**
	 * Remove the last given action to a macro.
	 */
	public static void popMacroAction() {
		macroActions.pop();
	}

	/**
	 * Available ability references of this TBS
	 */
	public static Map<String, Node> referencedAbilities = null;

	/**
	 * Available test references of this TBS
	 */
	public static Map<String, Node> referencedTest = null;

	/**
	 * Available actions references of this TBS
	 */
	public static Map<String, List<Node>> referencedActions = null;

	/**
	 * Available actions references of this TBS do not accepting macro
	 */
	public static Set<String> referencedNonMacroActions = null;

	/**
	 * Available attachments references of this TBS do not accepting macro
	 */
	public static Set<String> referencedNonMacroAttachments = null;

	/**
	 * Available attachments references of this TBS.
	 */
	public static Map<String, Node> referencedAttachments = null;

	/**
	 * Available node for action of macro.
	 */
	public static Stack<List<Node>> macroActions = null;

	/**
	 * Are the references must be resolved.
	 */
	public static boolean resolveReferences;
}
