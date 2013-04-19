/*
 * Created on 10 mars 2005
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
import java.util.List;

import net.sf.firemox.xml.XmlAction;
import net.sf.firemox.xml.XmlConfiguration;
import net.sf.firemox.xml.XmlParser;
import net.sf.firemox.xml.XmlTbs;
import net.sf.firemox.xml.XmlToMDB;
import net.sf.firemox.xml.XmlParser.Node;
import net.sf.firemox.xml.expression.Counter;
import net.sf.firemox.xml.tbs.Tbs;
import net.sf.firemox.xml.test.And;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class Action implements XmlToMDB {

	public int buildMdb(Node node, OutputStream out) throws IOException {
		final String referenceName = node.getAttribute("ref");
		if (referenceName == null) {
			XmlConfiguration.error("Reference action's name is missing for " + node);
		}

		final List<Node> actions = XmlTbs.getReferencedActions(referenceName);
		final boolean isMacro = XmlTbs.isNotMacro(referenceName);
		int nbCost = 0;
		final boolean macroValueAttr = !isMacro ? false : node
				.getAttribute("value") != null;
		final boolean macroValueElt = !isMacro ? false : node.get("value") != null;
		final boolean macroTestElt = !isMacro ? false : node.get("test") != null;
		final boolean macroCounterTestElt = !isMacro ? false : node
				.get("counter-test") != null;
		for (Node action : actions) {

			// temporally add "value" attribute/element to this macro
			Node oldTest = null;
			Node oldCounterTest = null;

			// Macro VALUE
			boolean addedValue;
			if (action.getAttribute("value") == null && action.get("value") == null) {
				addedValue = true;
				if (macroValueAttr) {
					action.addFirstAttribute(new XmlParser.Attribute("value", node
							.getAttribute("value")));
				} else if (macroValueElt) {
					action.add(0, node.get("value"));
				}
			} else {
				addedValue = false;
			}

			// Macro TEST
			if (macroTestElt) {
				oldTest = action.get("test");
				if (oldTest != null) {
					action.removeElement(oldTest);
				}
				// check whether the test is a reference
				Node macroTest = node.get("test");
				String testReferenceName = macroTest.getAttribute("ref");
				if (testReferenceName != null) {
					macroTest = XmlTbs.getReferencedTest(testReferenceName);
				}
				// we append (AND) the given test to the existing one
				action.add(0, And.createAndNode(action, oldTest, macroTest));
			}

			// Macro COUNTER-TEST
			if (macroCounterTestElt) {
				oldCounterTest = Counter.macroCounterTest;
				Counter.macroCounterTest = node.get("counter-test");
			}

			// Push the macro action.
			Tbs.pushMacroAction(node.getNodes("actions"));

			nbCost += XmlAction.getAction(action.getTag()).buildMdb(action, out);

			// Pop the macro action.
			Tbs.popMacroAction();

			// Macro VALUE: remove the added "value" attribute/element from this macro
			if (addedValue) {
				if (macroValueAttr) {
					action.removeAttribute("value");
				} else if (macroValueElt) {
					action.removeElement(0);
				}
			}

			// Macro TEST: remove the added "test" attribute/element from this macro
			if (macroTestElt) {
				if (oldTest != null) {
					action.setElement(0, oldTest);
				} else {
					action.removeElement(0);
				}
			}

			// Macro COUNTER-TEST
			if (macroCounterTestElt) {
				Counter.macroCounterTest = oldCounterTest;
			}

		}
		return nbCost;
	}

}
