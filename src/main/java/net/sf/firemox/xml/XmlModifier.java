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

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import net.sf.firemox.modifier.model.Layer;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.xml.XmlParser.Node;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80
 * @since 0.85 Recalculate attribute is supported
 */
public final class XmlModifier {

	/**
	 * Creates a new instance of XmlModifier <br>
	 */
	private XmlModifier() {
		super();
	}

	/**
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>modifier name [String]</li>
	 * <li>live update [boolean]</li>
	 * <li>linked to creator [boolean]</li>
	 * <li>activationZone [IdZone]</li>
	 * <li>activated while this condition [Test]</li>
	 * <li>exists until this triggered event [Event[]]</li>
	 * <li>layer [integer]</li>
	 * </ul>
	 * 
	 * @param node
	 *          the XML card structure
	 * @param out
	 *          output stream where the card structure will be saved
	 */
	public static void buildMdbModifier(Node node, OutputStream out) {
		// write the name
		try {
			MToolKit.writeString(out, node.getAttribute("name"));

			// write the "live-update" attribute
			out.write("false".equals(node.getAttribute("live-update")) ? 0 : 1);

			final String activationZone = node.getAttribute("activation-zone");
			if (StringUtils.isBlank(activationZone)) {
				out.write(IdZones.PLAY);
			} else {
				out.write(XmlTools.getZone(activationZone));
			}

			// write the while condition
			final boolean oldValue = XmlTools.defaultOnMeTag;
			XmlTools.defaultOnMeTag = false;
			XmlTest.getTest("test").buildMdb(node.get("while"), out);
			XmlTools.defaultOnMeTag = oldValue;

			// creator link
			String linked = node.getAttribute("linked");
			out.write(linked != null && "true".equals(linked) ? 1 : 0);

			// write the until events
			Node until = node.get("until");
			if (until == null) {
				// permanent attachment (until attached object remove this modifier)
				out.write(0);
			} else {
				until.addAttribute(new XmlParser.Attribute("zone", "play"));
				out.write(until.getNbNodes());
				for (java.lang.Object obj : until) {
					if (obj instanceof Node) {
						Node child = (Node) obj;
						if ("text".equals(child.getTag())) {
							XmlConfiguration
									.warning("text element in 'until condition' of modifier is not yet implement");
						} else {
							XmlEvent.getEvent(child.getTag()).buildMdb(child, out);
							break;
						}
					}
				}
			}

			// write the modifier positioning strategy (layer)
			Layer.valueOfXsd(node.getAttribute("layer")).serialize(out);
		} catch (Throwable e2) {
			XmlConfiguration.error("Error found in modifier '" + node.getTag()
					+ "' : " + e2.getMessage() + ". Context=" + node);
		}
	}

	/**
	 * @param modifierName
	 *          the modifier name.
	 * @return the modifier object to use to write it.
	 */
	public static XmlToMDB getModifier(String modifierName) {
		return XmlConfiguration.getXmlClass(modifierName, instances,
				XmlModifier.class);
	}

	private static Map<String, XmlToMDB> instances = new HashMap<String, XmlToMDB>();

	static void clean() {
		instances.clear();
	}
}
