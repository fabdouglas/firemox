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

import net.sf.firemox.test.IdTest;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.xml.XmlParser.Node;

/**
 * Generic implementation of the XmlToMDB interface for managing XML elements
 * relative to tests. Mainly used to get the suitable XmlToMDB implementation
 * corresponding to a given action name (corresponding to the XML element name)
 * through the getTest() method.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan</a>
 * @since 0.80
 */
public final class XmlTest implements XmlToMDB {

	/**
	 * Creates a new instance of XmlTest.<br>
	 */
	private XmlTest() {
		super();
	}

	/**
	 * Write the contained binary test of the specified node to the given output
	 * stream.<br>
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>first test [...]</li>
	 * <li>second test [...]</li>
	 * </ul>
	 * 
	 * @param node
	 *          the XML test container structure
	 * @param out
	 *          output stream where the card structure will be saved
	 * @param idTests
	 *          test id
	 * @throws IOException
	 *           if an I/O error occurs if an illegal argument is given if a
	 *           security error occurs if an illegal access error occurs if an
	 *           invocation target error occurs
	 */
	public static void buildMdbBinary(XmlParser.Node node, OutputStream out,
			IdTest idTests) throws IOException {
		int nbNodes = node.getNbNodes();
		if (nbNodes < 2) {
			XmlConfiguration.error("2 tests must be provided to binary '"
					+ node.getTag() + "' test");
		}

		int counter = 0;
		for (Object obj : node) {
			if (obj instanceof XmlParser.Node) {
				if (++counter < nbNodes) {
					idTests.serialize(out);
				}
				XmlTest.getTest(((XmlParser.Node) obj).getTag()).buildMdb(
						(XmlParser.Node) obj, out);
			}
		}
	}

	public int buildMdb(XmlParser.Node node, OutputStream out) {
		throw new InternalError(LanguageManager.getString("notyetimplemented", node
				.getTag()));
	}

	/**
	 * Write the contained test relative to a card associated to a player of the
	 * specified node to the given output stream. The player information can be
	 * coded as an attribute or as a nested XML element.<br>
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>idTest [1]</li>
	 * <li>on [TestOn]</li>
	 * <li>other [Expression]</li>
	 * </ul>
	 * 
	 * @param node
	 *          the XML test container structure
	 * @param out
	 *          output stream where the test container structure will be saved
	 * @param idTestPlayer
	 *          test id relative to a player
	 * @throws IOException
	 *           if an I/O error occurs
	 */
	public static void buildMdbPlayer(XmlParser.Node node, OutputStream out,
			IdTest idTestPlayer) throws IOException {
		buildMdbCard(node, out, idTestPlayer);
		XmlTools.writeAttrOptions(node, "player", out);
	}

	/**
	 * Write the contained test relative to a card associated to a player of the
	 * specified node to the given output stream. The player information can be
	 * coded as an attribute.<br>
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>player [TestOn]</li>
	 * </ul>
	 * 
	 * @param node
	 *          the XML test container structure
	 * @param out
	 *          output stream where the test container structure will be saved
	 * @param idTestPlayer
	 *          test id relative to a player
	 * @throws IOException
	 *           if an I/O error occurs if an illegal argument is given if a
	 *           security error occurs
	 */
	public static void buildMdbPlayerplayer(XmlParser.Node node,
			OutputStream out, IdTest idTestPlayer) throws IOException {
		buildMdbObject(node, out, idTestPlayer);
		TestOn.serialize(out, node.getAttribute("player"));
	}

	/**
	 * Write the contained test relative to a card of the specified node to the
	 * given output stream.<br>
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>idTest [1]</li>
	 * <li>idTestedOn [1]</li>
	 * </ul>
	 * 
	 * @param node
	 *          the XML test container structure
	 * @param out
	 *          output stream where the card structure will be saved
	 * @param idTests
	 *          test id
	 * @throws IOException
	 *           if an I/O error occurs if an illegal argument is given if a
	 *           security error occurs
	 */
	public static void buildMdbObject(XmlParser.Node node, OutputStream out,
			IdTest idTests) throws IOException {
		idTests.serialize(out);
		XmlTools.writeTestOn(out, node.getAttribute("card"));
	}

	/**
	 * Write the contained test relative to a card of the specified node to the
	 * given output stream.<br>
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>idTest [1]</li>
	 * <li>idTestedOn [1]</li>
	 * </ul>
	 * 
	 * @param node
	 *          the XML test container structure
	 * @param out
	 *          output stream where the card structure will be saved
	 * @param idTests
	 *          test id relative to a card
	 * @throws IOException
	 *           if an I/O error occurs if an illegal argument is given if a
	 *           security error occurs
	 */
	public static void buildMdbCard(XmlParser.Node node, OutputStream out,
			IdTest idTests) throws IOException {
		buildMdbObject(node, out, idTests);
	}

	/**
	 * Returns a suitable XmlToMDB implementation for the given action name
	 * (corresponding to the XML element name). The corresponding class is fetched
	 * in the net.sf.firemox.xml.test package.
	 * 
	 * @param testName
	 *          the name of the test corresponding to the found XML element name.
	 * @return a suitable XmlToMDB implementation for the given action name
	 */
	public static XmlToMDB getTest(String testName) {
		return XmlConfiguration.getXmlClass(testName, instances, XmlTest.class);
	}

	/**
	 * Writes the contained test relative to a target of the given node to the
	 * given output stream.
	 * 
	 * @param node
	 *          the XML test container structure
	 * @param out
	 *          output stream where the test structure will be saved
	 * @param idTests
	 *          the type of target, can be either IdTests.IS_PLAYER,
	 *          IdTests.IS_SPELL or IdTests.IS_ABILITY
	 * @throws IOException
	 */
	public static void buildMdbTargetable(Node node, OutputStream out,
			IdTest idTests) throws IOException {
		idTests.serialize(out);
		XmlTools.writeTestOn(out, node.getAttribute("target"));
	}

	/**
	 * Already instanciated XmlToMDB implementations relative to tests.
	 */
	private static Map<String, XmlToMDB> instances = new HashMap<String, XmlToMDB>();

	static void clean() {
		instances.clear();
	}
}
