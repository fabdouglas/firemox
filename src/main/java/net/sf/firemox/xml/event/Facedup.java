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
package net.sf.firemox.xml.event;

import java.io.IOException;
import java.io.OutputStream;

import net.sf.firemox.event.Event;
import net.sf.firemox.xml.XmlEvent;
import net.sf.firemox.xml.XmlParser;
import net.sf.firemox.xml.XmlTest;
import net.sf.firemox.xml.XmlToMDB;

/**
 * @author <a href="mailto:hoani.cross@gmail.com">Hoani CROSS</a>
 * @since 0.90
 */
public class Facedup implements XmlToMDB {

	/**
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>idEvent [1]</li>
	 * <li>idZone [1]</li>
	 * <li>test [...]</li>
	 * </ul>
	 * 
	 * @param node
	 *          the XML event structure
	 * @param out
	 *          output stream where the card structure will be saved
	 * @return the amount of written action, so return always ZERO.
	 * @throws IOException
	 */
	public final int buildMdb(XmlParser.Node node, OutputStream out)
			throws IOException {
		// write the idEvent
		Event.FACED_UP.write(out);
		XmlEvent.buildMdbTriggered(node, out);

		// write the test
		XmlTest.getTest("test").buildMdb(node.get("test"), out);
		return 0;
	}

}
