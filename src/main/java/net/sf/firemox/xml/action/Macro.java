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
package net.sf.firemox.xml.action;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import net.sf.firemox.xml.XmlConfiguration;
import net.sf.firemox.xml.XmlParser;
import net.sf.firemox.xml.XmlTbs;
import net.sf.firemox.xml.XmlToMDB;
import net.sf.firemox.xml.XmlParser.Node;
import net.sf.firemox.xml.tbs.Tbs;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.93
 */
public class Macro implements XmlToMDB {

	public int buildMdb(XmlParser.Node node, OutputStream out) throws IOException {
		List<Node> macroActions = Tbs.peekMacroAction();
		if (macroActions == null || macroActions.isEmpty()) {
			if (node.get("default") == null) {
				// no macro action(s) and no default node are set
				XmlConfiguration
						.error("Macro action must receive an action or contain a 'default' node");
				return 0;
			}
			// write the 'default' node
			return XmlTbs.writeActionListNoNb(node.get("default"), out);
		}
		// Write the given actions for this macro
		return XmlTbs.writeActionListNoNb(macroActions.remove(0), out);
	}
}
