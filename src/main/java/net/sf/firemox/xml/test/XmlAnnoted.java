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
package net.sf.firemox.xml.test;

import java.io.IOException;
import java.io.OutputStream;

import net.sf.firemox.test.BinaryTest;
import net.sf.firemox.test.TestCard;
import net.sf.firemox.test.TestExpr;
import net.sf.firemox.test.TestObject;
import net.sf.firemox.xml.XmlTools;
import net.sf.firemox.xml.XmlParser.Node;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.94
 */
public class XmlAnnoted extends net.sf.firemox.xml.XmlAnnoted {

	@Override
	public int buildMdb(Node node, OutputStream out) throws IOException {
		xmlElement.id().serialize(out);
		if (annotedClass.getSuperclass() == TestCard.class) {
			XmlTools.writeTestOn(out, node.getAttribute("card"));
		} else if (annotedClass.getSuperclass() == TestExpr.class
				|| annotedClass.getSuperclass() == BinaryTest.class) {
			XmlTools.writeAttrOptions(node, "left", out);
			XmlTools.writeAttrOptions(node, "right", out);
		} else if (annotedClass.getSuperclass() == TestObject.class) {
			XmlTools.writeTestOn(out, node.getAttribute("target"));
		}
		return 0;
	}

}
