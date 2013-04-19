/*
 * Created on Nov 17, 2004 
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
 * 
 */
package net.sf.firemox.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import net.sf.firemox.event.Event;
import net.sf.firemox.event.phase.PhaseFilter;
import net.sf.firemox.token.IdTokens;

/**
 * This class is mainly used to convert events described in a XML form to a
 * binary form
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80
 */
public final class XmlEvent {

	/**
	 * Creates a new instance of XmlEvent <br>
	 */
	private XmlEvent() {
		super();
	}

	/**
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>idEvent [1]</li>
	 * <li>idZone [1]</li>
	 * <li>test [...]</li>
	 * <li>idCard [2]</li>
	 * </ul>
	 * 
	 * @param node
	 *          the XML event structure
	 * @param out
	 *          output stream where the card structure will be saved
	 * @throws IOException
	 *           error during the writing.
	 */
	public static void buildMdbTriggered(XmlParser.Node node, OutputStream out)
			throws IOException {
		// write the zone
		out.write(XmlTools.getZone(node.getParent().getAttribute("zone")));
	}

	/**
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>idZone [1]</li>
	 * <li>test [...]</li>
	 * <li>idPhase [2]</li>
	 * </ul>
	 * 
	 * @param node
	 *          the XML event structure
	 * @param out
	 *          output stream where the card structure will be saved
	 * @throws IOException
	 *           error during the writing.
	 */
	public static void buildMdbPhase(XmlParser.Node node, OutputStream out)
			throws IOException {
		buildMdbTriggered(node, out);

		// write the test
		XmlTest.getTest("test").buildMdb(node.get("test"), out);

		// write the phase name/index
		if (node.get("phase-index") != null
				|| node.getAttribute("phase-index") != null) {
			PhaseFilter.INDEX_PHASE_FILTER.write(out);
			XmlTools.writeAttrOptions(node, "phase-index", out);
		} else {
			PhaseFilter.ID_PHASE_FILTER.write(out);
			XmlTools.writeAttrOptions(node, "phase", out);
		}
	}

	/**
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>[super]</li>
	 * <li>idCard [Expression]</li>
	 * </ul>
	 * 
	 * @param node
	 *          the XML event structure
	 * @param out
	 *          output stream where the card structure will be saved
	 * @throws IOException
	 *           error during the writing.
	 */
	public static void buildMdbCanICast(XmlParser.Node node, OutputStream out)
			throws IOException {
		// write the idEvent
		Event.CAN_CAST_CARD.write(out);
		buildMdbEvent(node, out);

		// write the test
		XmlTools.defaultOnMeTag = true;
		XmlTest.getTest("test").buildMdb(node.get("test"), out);

		// write the playable idCard
		if ("this".equals(node.getAttribute("playable")))
			XmlTools.writeConstant(out, IdTokens.MYSELF);
		else
			XmlTools.writeAttrOptions(node, "playable", out);
	}

	/**
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>idZone [1]</li>
	 * </ul>
	 * 
	 * @param node
	 *          the XML event structure
	 * @param out
	 *          output stream where the card structure will be saved
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	private static void buildMdbEvent(XmlParser.Node node, OutputStream out)
			throws IOException {
		// write the zone
		out.write(XmlTools.getZone(node.getAttribute("zone")));
	}

	/**
	 * Return the event writter.
	 * 
	 * @param eventName
	 *          the expression name
	 * @return the expression writter.
	 */
	public static XmlToMDB getEvent(String eventName) {
		return XmlConfiguration.getXmlClass(eventName, instances, XmlEvent.class);
	}

	private static Map<String, XmlToMDB> instances = new HashMap<String, XmlToMDB>();
}
