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
package net.sf.firemox.xml.tbs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.sf.firemox.token.IdTokens;
import net.sf.firemox.tools.MToolKit;
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
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class Card implements XmlToMDB {

	/**
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>card model</li>
	 * </ul>
	 * 
	 * @param node
	 *          the XML card structure
	 * @param out
	 *          output stream where the card structure will be saved
	 * @return the amount of written action in the output.
	 * @see net.sf.firemox.clickable.target.card.MCard
	 * @throws IOException
	 *           error while writing.
	 */
	public final int buildMdb(Node node, OutputStream out) throws IOException {
		// card name
		String cardName = node.getAttribute("name");
		if (cardName == null || cardName.length() == 0) {
			XmlConfiguration.error("Null card name found in '"
					+ node.getAttribute("xmlFile") + "'");
			cardName = FilenameUtils.getBaseName(node.getAttribute("xmlFile"))
					.toLowerCase();
		}

		String xmlFile = node.getAttribute("xmlFile");
		if (xmlFile != null
				&& !FilenameUtils.getBaseName(xmlFile).toLowerCase().equalsIgnoreCase(
						MToolKit.getKeyName(cardName))) {
			XmlConfiguration.error("Card '" + cardName
					+ "' should be definied in it's own file, not '"
					+ node.getAttribute("xmlFile") + "'");
		}

		// Write the card name using the pager
		XmlTbs.cardPager.addReference(out, cardName);

		// credits
		final Node credits = node.get("rules-author-comment");
		if (credits != null && credits.get(0) != null) {
			if (credits.get(0).toString().startsWith("Oracle2Xml")) {
				XmlConfiguration.uncompleted();
			}
			MToolKit.writeString(out, credits.get(0).toString());
		} else {
			MToolKit.writeString(out, null);
		}

		// keywords
		Node keywords = node.get("keywords");
		if (keywords != null && keywords.get(0) != null) {
			String[] words = keywords.get(0).toString().split(" ");
			out.write(words.length);
			for (String keyword : words) {
				MToolKit.writeString(out, keyword);
			}
		} else {
			out.write(0);
		}

		final Node init = node.get("init");
		// initialize the additional modifier list
		final List<Node> modifierNodes = new ArrayList<Node>();
		// registers
		byte[] registersBytes = new byte[IdTokens.CARD_REGISTER_SIZE];
		if (init != null) {
			Node registers = init.get("registers");
			if (registers != null) {
				final List<Node> list = registers.getNodes("register");
				for (Node register : list) {
					if (register.getAttribute("value") == null) {
						// add this node that should contain a register modifier
						modifierNodes.add(register);
					} else {
						registersBytes[XmlTools.getInt(register.getAttribute("index"))] = (byte) XmlTools
								.getInt(register.getAttribute("value"));
					}
				}
			}
		}
		out.write(registersBytes);

		// id card
		int idCard = 0;
		int decr = 0;
		if (init != null) {
			Node idcards = init.get("idcards");
			if (idcards != null && idcards.get(0) != null) {
				String list = ((String) idcards.get(0)).trim();
				String[] arrayid = list.split(" ");
				for (String id : arrayid) {
					if (id.trim().length() > 0) {
						idCard |= XmlTools.getIdCard(id);
					}
				}
			}
		}
		MToolKit.writeInt16(out, idCard);

		// color
		int idColor = 0;
		if (init != null) {
			Node colors = init.get("colors");
			if (colors != null && colors.get(0) != null) {
				String list = ((String) colors.get(0)).trim();
				String[] arrayid = list.split(" ");
				for (String id : arrayid) {
					if (id.trim().length() > 0) {
						idColor |= XmlTools.getColor(id);
					}
				}
			}
		}
		out.write(idColor);

		// properties
		if (init != null) {
			Node properties = init.get("properties");
			if (properties == null) {
				out.write(0);
			} else {
				Object strProperties = properties.get(0);
				String list = null;
				if (strProperties == null) {
					list = "";
				} else {
					list = ((String) strProperties).trim();
				}
				String[] arrayid = list.split(" ");
				decr = 0;
				for (int i = arrayid.length; i-- > 0;) {
					arrayid[i] = arrayid[i].trim();
					if (arrayid[i].length() == 0) {
						decr++;
					}
				}
				int[] arrayIdSorted = new int[arrayid.length - decr];
				out.write(arrayIdSorted.length);
				// get int values associated to properties name
				decr = 0;
				for (String id : arrayid) {
					if (id.length() > 0) {
						arrayIdSorted[decr++] = XmlTools.getInt(id);
					}
				}
				// sort the properties values
				Arrays.sort(arrayIdSorted);
				// write the sorted properties values
				for (int id : arrayIdSorted) {
					MToolKit.writeInt16(out, id);
				}
			}
		} else {
			out.write(0);
		}

		// actions
		Node actions = node.get("actions");
		if (actions != null) {
			for (Object obj : actions) {
				if (obj instanceof Node) {
					String ref = ((Node) obj).getAttribute("reference-name");
					List<Node> actionList = new ArrayList<Node>();
					for (Object actionI : (Node) obj) {
						if (actionI instanceof Node) {
							// add action to the action list
							((Node) actionI)
									.addAttribute(new XmlParser.Attribute("name", ref));
							actionList.add((Node) actionI);
						}
					}
					// add this reference
					if (XmlTbs.referencedActions == null) {
						XmlTbs.referencedActions = new HashMap<String, List<Node>>();
					}
					XmlTbs.referencedActions.put(ref, actionList);
				}
			}
		}

		// abilities
		Node abilities = node.get("abilities");
		if (abilities == null) {
			out.write(0);
		} else {
			out.write(abilities.getNbNodes());
			for (Object obj : abilities) {
				if (obj instanceof Node) {
					Node ability = (Node) obj;
					String ref = ability.getAttribute("reference-name");
					if (ref != null && ref.length() > 0) {
						if (XmlTbs.referencedAbilities == null) {
							XmlTbs.referencedAbilities = new HashMap<String, Node>();
						}
						XmlTbs.referencedAbilities.put(ref, (Node) obj);
					}

					XmlTbs.getTbsComponent(ability.getTag()).buildMdb(ability, out);
					// add this reference
				}
			}
		}

		// register indirections
		Node modifiers = node.get("modifiers");
		if (modifiers == null) {
			out.write(modifierNodes.size());
		} else {
			out.write(modifiers.getNbNodes() + modifierNodes.size());

			// explicitly declared modifiers
			for (Object obj : modifiers) {
				if (obj instanceof Node) {
					XmlModifier.getModifier(((Node) obj).getTag()).buildMdb((Node) obj,
							out);
				}
			}
		}
		if (!modifierNodes.isEmpty()) {
			// there are some additional register indirections to add
			for (Node nodeModifier : modifierNodes) {
				if (nodeModifier.get("value") != null) {
					// this indirection has as value a counter
					boolean oldValue = XmlTools.defaultOnMeTag;
					XmlTools.defaultOnMeTag = false;
					XmlModifier.getModifier("register-indirection").buildMdb(
							nodeModifier, out);
					XmlTools.defaultOnMeTag = oldValue;
				} else {
					XmlConfiguration
							.error("The specified modifier for this register is unknown; "
									+ nodeModifier);
					return 0;
				}
			}
		}

		// attachment
		Node attachment = node.get("attachment");
		if (attachment == null) {
			out.write(0);
		} else {
			out.write(1);
			Node referenceModifiers = null;
			if (attachment.getAttribute("ref") != null) {
				referenceModifiers = attachment.get("modifiers");
				attachment = XmlTbs.getReferencedAttachment(attachment
						.getAttribute("ref"));
			}
			if (attachment == null)
				// reference error
				return 0;

			Node attachmentModifiers = attachment.get("modifiers");
			if (referenceModifiers == null) {
				if (attachmentModifiers == null) {
					out.write(0);
				} else {
					out.write(attachmentModifiers.getNbNodes());
				}
			} else {
				if (attachmentModifiers == null)
					out.write(referenceModifiers.getNbNodes());
				else {
					out.write(attachmentModifiers.getNbNodes()
							+ referenceModifiers.getNbNodes());
				}
				// macro modifiers
				for (Object obj : referenceModifiers) {
					if (obj instanceof Node) {
						XmlModifier.getModifier(((Node) obj).getTag()).buildMdb((Node) obj,
								out);
					}
				}
			}

			// explicitly declared modifiers
			if (attachmentModifiers != null) {
				for (Object obj : attachmentModifiers) {
					if (obj instanceof Node) {
						XmlModifier.getModifier(((Node) obj).getTag()).buildMdb((Node) obj,
								out);
					}
				}
			}
			XmlTest.getTest("test").buildMdb(attachment.get("valid-target"), out);
			XmlTest.getTest("test").buildMdb(attachment.get("valid-attachment"), out);
		}
		return 0;
	}
}
